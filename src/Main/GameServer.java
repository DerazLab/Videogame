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
    private long lastBroadcastTime;

    public GameServer(int port, MenuState menuState) {
        this.port = port;
        this.menuState = menuState;
        this.lastBroadcastTime = System.nanoTime();
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
            System.out.println("Servidor creado en el puerto " + port);

            while (clients.size() < 2) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nuevo cliente conectado: " + clientSocket.getInetAddress().getHostAddress() + ", asignando id: " + (clients.size() + 1));
                    ClientHandler client = new ClientHandler(clientSocket, clients.size() + 1);
                    clients.add(client);
                    playerIds.add(clients.size());
                    new Thread(client).start();
                    menuState.clientConnected();
                } catch (IOException e) {
                    System.err.println("Error recibiendo al cliente: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Fallo al iniciar el server");
            e.printStackTrace();
        }
    }

    public void notifyGameStart() {
        try {
            for (ClientHandler client : clients) {
                client.sendGameStart();
            }
        } catch (Exception e) {
            System.err.println("No se envio el inicio del juego: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void notifyStateChange(int newState) {
        try {
            StateChange stateChange = new StateChange(newState);
            for (ClientHandler client : clients) {
                client.sendStateChange(stateChange);
            }
            System.out.println("Cambio de estado enviado: " + newState);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el cambio de estado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void broadcastGameState() {
        long currentTime = System.nanoTime();
        if ((currentTime - lastBroadcastTime) / 1_000_000 < 33) {
            return;
        }
        lastBroadcastTime = currentTime;

        if (gameState == null) {
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
                        player.isHoldingFlag(),
                        player.isAwaitingRespawn(),
                        player.getRespawnTimer(),
                        player.isDeathAnimationComplete(),
                        player.isDescendingFlag(),
                        player.isDescentComplete()
                    ));
                }
            }
            for (Enemy enemy : gameState.getEnemies()) {
                state.enemies.add(new EnemyData(enemy.getx(), enemy.gety(), enemy.getHealth(), enemy.isDead()));
            }
            // Add timer data
            state.levelStartTime = gameState.getCurrentTime() * 1_000_000_000;
            state.timerStopped = gameState.timerStopped;
            state.levelEndTime = gameState.levelEndTime;
            for (ClientHandler client : clients) {
                client.sendGameState(state);
            }
        } catch (Exception e) {
            System.err.println("Error enviando estado: " + e.getMessage());
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
                socket.setSoTimeout(10000);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendStateChange(StateChange stateChange) {
            try {
                out.writeObject(stateChange);
                out.flush();
                System.out.println("Cambio de estado enviado: " + stateChange.newState);
            } catch (IOException e) {
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
                        if (obj instanceof NetworkData.PlayerInput && gameState != null) {
                            NetworkData.PlayerInput input = (NetworkData.PlayerInput) obj;
                            gameState.updatePlayerInput(playerId, input);
                        } else {
                            System.err.println("Objeto recibido: " + obj);
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("Timeout del cliente " + playerId + ": " + e.getMessage());
                        continue;
                    } catch (EOFException e) {
                        System.out.println("Cliente " + playerId + " desconectado");
                        break;
                    } catch (OptionalDataException e) {
                        if (e.eof) {
                            break;
                        } else {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        System.err.println("Error IO");
                        e.printStackTrace();
                        break;
                    } catch (ClassNotFoundException e) {
                        System.err.println("Clase no encontrada");
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error de conexion inicial");
                e.printStackTrace();
            } finally {
                clients.remove(this);
                if (gameState != null) {
                    Player player = gameState.getPlayer(playerId);
                    if (player != null) {
                        gameState.getPlayers().remove(player);
                        System.out.println("Eliminado jugador " + playerId + " del estado");
                    }
                }
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (socket != null) socket.close();
                    System.out.println("Conexion del cliente " + playerId + " cerrada");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendGameState(GameStateData state) {
            try {
                out.writeObject(state);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendGameStart() {
            try {
                out.writeObject("START_GAME");
                out.flush();
                System.out.println("Enviado inicio al cliente " + playerId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendKeepAlive() {
            try {
                out.writeObject("Alive");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPlayerCount() {
        return clients.size() + 1;
    }
}