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
            System.out.println("Conectado con la id " + playerId);

            new Thread(this::receiveGameState).start();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Fallo al conectarse al servidor: " + e.getMessage());
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
            e.printStackTrace();
            disconnect();
        }
    }

    private void receiveGameState() {
        try {
            while (connected) {
                try {
                    Object obj = in.readObject();
                    System.out.println("Objeto recibido: " + obj.getClass().getSimpleName());
                    if (obj instanceof String) {
                        if (obj.equals("START_GAME")) {
                            System.out.println("Juego iniciado");
                            gamePanel.getGameStateManager().setState(GameStateManager.INLEVEL);
                        } else if (obj.equals("KEEP_ALIVE")) {
                            System.out.println("Latido recibido del jugador " + playerId);
                        } else {
                            System.err.println("String recibido: " + obj);
                        }
                    } else if (obj instanceof GameStateData) {
                        GameStateData state = (GameStateData) obj;
                        Level1State level = (Level1State) gamePanel.getGameStateManager().getGameStates().get(GameStateManager.INLEVEL);
                        for (int i = 0; i < state.players.size() && i < level.getPlayers().size(); i++) {
                            Player player = level.getPlayer(i);
                            PlayerData data = state.players.get(i);
                            if (i != playerId || data.dead || data.holdingFlag || data.descendingFlag) {
                                player.setPosition(data.x, data.y);
                            }
                            player.setHealth(data.health);
                            player.setScore(data.score);
                            player.setFacingRight(data.facingRight);
                            player.setHoldingFlag(data.holdingFlag);
                            if (data.dead) {
                                player.setHealth(0);
                            }
                        }
                        level.updateGameState(state);
                    } else if (obj instanceof StateChange) {
                        StateChange stateChange = (StateChange) obj;
                        System.out.println("Cambio de estado recibido, nuevo estado: " + stateChange.newState);
                        gamePanel.getGameStateManager().setState(stateChange.newState);
                    } else {
                        System.err.println("Objeto recibido del jugador " + playerId + ": " + obj);
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout del jugador " + playerId + ": no se recibieron datos en 10 segundos");
                    continue;
                } catch (EOFException e) {
                    System.err.println("Conexión perdida " + playerId + ": Error EOF");
                    disconnect();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnect();
                    break;
                } catch (ClassNotFoundException e) {
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
			System.out.println("Cliente " + playerId + " desconectado");
		} catch (IOException e) {
			System.err.println("Error desconectando al jugador " + playerId + ": " + e.getMessage());
			e.printStackTrace();
		}
		GameStateManager gsm = gamePanel.getGameStateManager();
		if (gsm != null) {
			gsm.setState(GameStateManager.INMENU);
		} else {
			System.out.println("gsm no inicializado, reinicia el juego");
		}
	}

    public GameStateManager getGameStateManager() {
        return gamePanel.getGameStateManager();
    }
}