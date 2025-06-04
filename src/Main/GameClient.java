package Main;

import GameState.GameStateManager;
import Main.NetworkData.*;
import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int playerId;
    private GamePanel gamePanel;
    private boolean connected;

    public GameClient(String host, int port, GamePanel gamePanel) {
        this.gamePanel = gamePanel;
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

    public void sendInput(PlayerInput input) {
        try {
            out.writeObject(input);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveGameState() {
        try {
            while (connected) {
                Object obj = in.readObject();
                if (obj instanceof String && obj.equals("START_GAME")) {
                    // Cambiar al estado de juego cuando el servidor lo indique
                    gamePanel.getGameStateManager().setState(GameStateManager.INLEVEL);
                } else if (obj instanceof GameStateData) {
                    gamePanel.updateGameState((GameStateData) obj);
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