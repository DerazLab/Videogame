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
            gameState.addPlayer();
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (clients.size() < 2) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler client = new ClientHandler(clientSocket, clients.size() + 1);
                clients.add(client);
                playerIds.add(clients.size());
                new Thread(client).start();
                menuState.clientConnected();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyGameStart() {
        try {
            for (ClientHandler client : clients) {
                client.sendGameStart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcastGameState() {
        if (gameState == null) return;
        try {
            GameStateData state = new GameStateData();
            state.players = new ArrayList<>();
            state.enemies = new ArrayList<>();
            for (int i = 0; i < gameState.getPlayerCount(); i++) {
                Player player = gameState.getPlayer(i);
                if (player != null) {
                    state.players.add(new PlayerData(player.getx(), player.gety(), player.getHealth(), player.getScore(), player.isFacingRight()));
                }
            }
            for (Enemy enemy : gameState.getEnemies()) {
                state.enemies.add(new EnemyData(enemy.getx(), enemy.gety(), enemy.getHealth(), enemy.isDead()));
            }
            for (ClientHandler client : clients) {
                client.sendGameState(state);
            }
        } catch (Exception e) {
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
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                out.writeObject(playerId);
                out.flush();

                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof NetworkData.PlayerInput && gameState != null) {
                        NetworkData.PlayerInput input = (NetworkData.PlayerInput) obj;
                        gameState.updatePlayerInput(playerId, input);
                        // Debug log to verify received input
                        System.out.println("Received input for player " + playerId + ": left=" + input.left + ", right=" + input.right + ", jumping=" + input.jumping);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPlayerCount() {
        return clients.size() + 1;
    }
}