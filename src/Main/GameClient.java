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
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;

            // Recibir el ID del jugador
            playerId = (Integer) in.readObject();
            System.out.println("Connected to server with player ID: " + playerId);

            // Iniciar hilo para recibir actualizaciones del servidor
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
        // Apply input locally for immediate response
        GameStateManager gsm = gamePanel.getGameStateManager();
        if (gsm.getGameStates().get(GameStateManager.INLEVEL) instanceof Level1State) {
            Level1State level = (Level1State) gsm.getGameStates().get(GameStateManager.INLEVEL);
            level.updatePlayerInput(playerId, input);
        }
        // Send input only if changed and at least 50ms have passed
        long currentTime = System.nanoTime();
        long lastSendTime = lastInputTime; // Add a field to track last send time
        if (!input.equals(lastInput) && (currentTime - lastSendTime) / 1000000 >= 50) {
            out.writeObject(input);
            out.flush();
            lastInput = new NetworkData.PlayerInput();
            lastInput.left = input.left;
            lastInput.right = input.right;
            lastInput.up = input.up;
            lastInput.down = input.down;
            lastInput.jumping = input.jumping;
            lastInputTime = currentTime; // Update last send time
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
                    if (obj instanceof String && obj.equals("START_GAME")) {
                        gamePanel.getGameStateManager().setState(GameStateManager.INLEVEL);
                    } else if (obj instanceof GameStateData) {
                        GameStateData state = (GameStateData) obj;
                        Level1State level = (Level1State) gamePanel.getGameStateManager().getGameStates().get(GameStateManager.INLEVEL);
                        level.updateGameState(state);
                    }
                } catch (EOFException e) {
                    System.err.println("Server connection closed unexpectedly: EOF reached");
                    disconnect();
                    break;
                } catch (IOException e) {
                    System.err.println("IO error while receiving game state: " + e.getMessage());
                    e.printStackTrace();
                    disconnect();
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found while receiving game state: " + e.getMessage());
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
            System.err.println("Error closing client connection: " + e.getMessage());
            e.printStackTrace();
        }
        // Notify user or transition to menu
        gamePanel.getGameStateManager().setState(GameStateManager.INMENU);
    }

    public GameStateManager getGameStateManager() {
        return gamePanel.getGameStateManager();
    }
}