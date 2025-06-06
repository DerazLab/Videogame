package Main;

import GameState.GameStateManager;
import Main.NetworkData.*;
import java.io.*;
import java.net.*;
import GameState.*;
import Entity.Player;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int playerId;
    private GamePanel gamePanel;
    private boolean connected;
    private NetworkData.PlayerInput lastInput;
    private long lastInputTime;

    public GameClient(String host, int port, GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        lastInput = new NetworkData.PlayerInput();
        lastInputTime = System.nanoTime();
        try {
            socket = new Socket();
            socket.setSoTimeout(10000);
            socket.connect(new InetSocketAddress(host, port), 10000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;

            playerId = (Integer) in.readObject();
            System.out.println("Connected to server with player ID: " + playerId);

            new Thread(this::receiveGameState).start();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void sendInput(NetworkData.PlayerInput input) {
        try {
            GameStateManager gsm = gamePanel.getGameStateManager();
            if (gsm.getGameStates().get(GameStateManager.INLEVEL) instanceof Level1State) {
                Level1State level = (Level1State) gsm.getGameStates().get(GameStateManager.INLEVEL);
                level.updatePlayerInput(playerId, input);
            }
            long currentTime = System.nanoTime();
            if (!input.equals(lastInput) || (currentTime - lastInputTime) / 1_000_000 >= 100) {
                //System.out.println("Sending input for player " + playerId + ": left=" + input.left + ", right=" + input.right + ", up=" + input.up + ", down=" + input.down + ", jumping=" + input.jumping);
                out.writeObject(input);
                out.flush();
                lastInput = new NetworkData.PlayerInput();
                lastInput.left = input.left;
                lastInput.right = input.right;
                lastInput.up = input.up;
                lastInput.down = input.down;
                lastInput.jumping = input.jumping;
                lastInputTime = currentTime;
            }
        } catch (IOException e) {
            System.err.println("Failed to send input for player " + playerId + ": " + e.getMessage());
            e.printStackTrace();
            disconnect();
        }
    }

    private void receiveGameState() {
        try {
            while (connected) {
                try {
                    Object obj = in.readObject();
                    //System.out.println("Received object for player " + playerId + ": " + obj.getClass().getSimpleName());
                    if (obj instanceof String) {
                        if (obj.equals("START_GAME")) {
                            System.out.println("Received START_GAME signal for player " + playerId);
                            gamePanel.getGameStateManager().setState(GameStateManager.INLEVEL);
                        } else if (obj.equals("KEEP_ALIVE")) {
                            System.out.println("Received KEEP_ALIVE for player " + playerId);
                        } else {
                            System.err.println("Unexpected string received for player " + playerId + ": " + obj);
                        }
                    } else if (obj instanceof GameStateData) {
                        GameStateData state = (GameStateData) obj;
                        //System.out.println("Received GameStateData for player " + playerId + ": players=" + state.players.size() + ", enemies=" + state.enemies.size());
                        Level1State level = (Level1State) gamePanel.getGameStateManager().getGameStates().get(GameStateManager.INLEVEL);
                        for (int i = 0; i < state.players.size() && i < level.getPlayers().size(); i++) {
                            Player player = level.getPlayer(i);
                            PlayerData data = state.players.get(i);
                            if (i != playerId || data.dead || data.holdingFlag) {
                                player.setPosition(data.x, data.y);
                            }
                            player.setHealth(data.health);
                            player.setScore(data.score);
                            player.setFacingRight(data.facingRight);
                            player.setHoldingFlag(data.holdingFlag);
                            if (data.awaitingRespawn) {
                                player.setHealth(0); // Ensure dead state
                            }
                        }
                        level.updateGameState(state);
                    } else if (obj instanceof StateChange) {
                        StateChange stateChange = (StateChange) obj;
                        System.out.println("Received StateChange for player " + playerId + ": newState=" + stateChange.newState);
                        gamePanel.getGameStateManager().setState(stateChange.newState);
                    } else {
                        System.err.println("Unexpected object received for player " + playerId + ": " + obj);
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timeout for player " + playerId + ": waiting for server data");
                    continue;
                } catch (EOFException e) {
                    System.err.println("Server connection closed unexpectedly for player " + playerId + ": EOF reached");
                    disconnect();
                    break;
                } catch (IOException e) {
                    System.err.println("IO error while receiving game state for player " + playerId + ": " + e.getMessage());
                    e.printStackTrace();
                    disconnect();
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found while receiving game state for player " + playerId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        if (!connected) return;
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("Client " + playerId + " disconnected cleanly");
        } catch (IOException e) {
            System.err.println("Error closing client connection for player " + playerId + ": " + e.getMessage());
            e.printStackTrace();
        }
        gamePanel.getGameStateManager().setState(GameStateManager.INMENU);
    }

    public GameStateManager getGameStateManager() {
        return gamePanel.getGameStateManager();
    }
}