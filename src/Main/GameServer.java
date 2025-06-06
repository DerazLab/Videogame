package Main;

import Entity.Player;
import Entity.Enemy;
import GameState.Level1State;
import GameState.MenuState;
import Main.NetworkData.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private MenuState menuState;
    private Level1State gameState;
    private int port;
    private List<Integer> playerIds = new ArrayList<>();

    public GameServer(int port, MenuState menuState) {
        this.port = port;
        this.menuState = menuState;
    }

    public void setGameState(Level1State gameState) {
        this.gameState = gameState;
        for (int i = 0; i < clients.size(); i++) {
            gameState.addPlayer(i + 1);
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (clients.size() < 2) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress() + ", assigning player ID: " + (clients.size() + 1));
                    ClientHandler client = new ClientHandler(clientSocket, clients.size() + 1);
                    clients.add(client);
                    playerIds.add(clients.size());
                    new Thread(client).start();
                    menuState.clientConnected();
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void notifyGameStart() {
        try {
            for (ClientHandler client : clients) {
                client.sendGameStart();
            }
        } catch (Exception e) {
            System.err.println("Error notifying game start: " + e.getMessage());
            e.printStackTrace();
        }
    }
	
	public void notifyStateChange(int newState) {
        try {
            StateChange stateChange = new StateChange(newState);
            for (ClientHandler client : clients) {
                client.sendStateChange(stateChange);
            }
            System.out.println("Notified state change to clients: newState=" + newState);
        } catch (Exception e) {
            System.err.println("Error notifying state change: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void broadcastGameState() {
        if (gameState == null) {
            // Enviar un mensaje "KEEP_ALIVE" si no hay estado del juego
            for (ClientHandler client : clients) {
                client.sendKeepAlive();
            }
            return;
        }
        try {
            GameStateData state = new GameStateData();
            state.players = new ArrayList<>();
            state.enemies = new ArrayList<>();
            for (int i = 0; i < gameState.getPlayerCount(); i++) {
                Player player = gameState.getPlayer(i);
                if (player != null) {
                    state.players.add(new PlayerData(
                        player.getx(), 
                        player.gety(), 
                        player.getHealth(), 
                        player.getScore(), 
                        player.isFacingRight(), 
                        player.isDead(),
                        player.isHoldingFlag()
                    ));
                }
            }
            for (Enemy enemy : gameState.getEnemies()) {
                state.enemies.add(new EnemyData(enemy.getx(), enemy.gety(), enemy.getHealth(), enemy.isDead()));
            }
            //System.out.println("Broadcasting game state: players=" + state.players.size() + ", enemies=" + state.enemies.size());
            for (ClientHandler client : clients) {
                client.sendGameState(state);
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting game state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private int playerId;

        public ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
            try {
                socket.setSoTimeout(10000); // 5-second timeout
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Error initializing streams for client " + playerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
		
		public void sendStateChange(StateChange stateChange) {
            try {
                out.writeObject(stateChange);
                out.flush();
                System.out.println("Sent StateChange to client " + playerId + ": newState=" + stateChange.newState);
            } catch (IOException e) {
                System.err.println("Error sending state change to client " + playerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                out.writeObject(playerId);
                out.flush();
                System.out.println("Sent player ID " + playerId + " to client");

                while (true) {
                    try {
                        Object obj = in.readObject();
                        System.out.println("Received object from client " + playerId + ": " + obj.getClass().getSimpleName());
                        if (obj instanceof NetworkData.PlayerInput && gameState != null) {
                            NetworkData.PlayerInput input = (NetworkData.PlayerInput) obj;
                            System.out.println("Received input from client " + playerId + ": left=" + input.left + ", right=" + input.right + ", up=" + input.up + ", down=" + input.down + ", jumping=" + input.jumping);
                            gameState.updatePlayerInput(playerId, input);
                        } else {
                            System.err.println("Unexpected object received from client " + playerId + ": " + obj);
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("Socket timeout for client " + playerId + ": " + e.getMessage());
                        // Opcionalmente, mantener al cliente conectado o tomar otra acción
                        continue; // Mantener el bucle en ejecución
                    } catch (EOFException e) {
                        System.out.println("Client " + playerId + " disconnected: EOF reached");
                        break;
                    } catch (IOException e) {
                        System.out.println("IO error for client " + playerId + ": " + e.getMessage());
                        e.printStackTrace();
                        break;
                    } catch (ClassNotFoundException e) {
                        System.err.println("Class not found for client " + playerId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.out.println("Initial connection error for client " + playerId + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                clients.remove(this);
                if (gameState != null) {
                    Player player = gameState.getPlayer(playerId);
                    if (player != null) {
                        gameState.getPlayers().remove(player);
                        System.out.println("Removed player " + playerId + " from game state");
                    }
                }
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (socket != null) socket.close();
                    System.out.println("Client " + playerId + " connection closed cleanly");
                } catch (IOException e) {
                    System.err.println("Error closing connection for client " + playerId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        public void sendGameState(GameStateData state) {
            try {
                out.writeObject(state);
                out.flush();
                //System.out.println("Sent game state to client " + playerId);
            } catch (IOException e) {
                System.err.println("Error sending game state to client " + playerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void sendGameStart() {
            try {
                out.writeObject("START_GAME");
                out.flush();
                System.out.println("Sent START_GAME to client " + playerId);
            } catch (IOException e) {
                System.err.println("Error sending game start to client " + playerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void sendKeepAlive() {
            try {
                out.writeObject("KEEP_ALIVE");
                out.flush();
                System.out.println("Sent KEEP_ALIVE to client " + playerId);
            } catch (IOException e) {
                System.err.println("Error sending keep-alive to client " + playerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public int getPlayerCount() {
        return clients.size() + 1;
    }
}