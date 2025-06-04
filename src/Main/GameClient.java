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
    private NetworkData.PlayerInput lastInput; // Track last sent input

    public GameClient(String host, int port, GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        lastInput = new NetworkData.PlayerInput(); // Initialize default input
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
            // Only send if input has changed to reduce network load
            if (!input.equals(lastInput)) {
                out.writeObject(input);
                out.flush();
                lastInput = new NetworkData.PlayerInput();
                lastInput.left = input.left;
                lastInput.right = input.right;
                lastInput.up = input.up;
                lastInput.down = input.down;
                lastInput.jumping = input.jumping;
                System.out.println("Sent input: left=" + input.left + ", right=" + input.right + ", jumping=" + input.jumping);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveGameState() {
        try {
            while (connected) {
                Object obj = in.readObject();
                if (obj instanceof String && obj.equals("START_GAME")) {
                    gamePanel.getGameStateManager().setState(GameStateManager.INLEVEL);
                } else if (obj instanceof GameStateData) {
                    // Update game state, but preserve local player's input-driven position
                    GameStateData state = (GameStateData) obj;
                    Level1State level = (Level1State) gamePanel.getGameStateManager().getGameStates().get(GameStateManager.INLEVEL);
                    Player localPlayer = level.getPlayer(playerId);
                    double localX = localPlayer.getx();
                    double localY = localPlayer.gety();
                    level.updateGameState(state);
                    // Restore local player's position to avoid jitter
                    localPlayer.setPosition(localX, localY);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            connected = false;
        }
    }

    public void disconnect() {
        try {
            connected = false;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener GameStateManager
    public GameStateManager getGameStateManager() {
        return gamePanel.getGameStateManager();
    }
}