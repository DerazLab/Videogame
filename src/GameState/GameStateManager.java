package GameState;

import Main.GameClient;
import Main.GameServer;
import Main.NetworkData;
import java.util.ArrayList;
import java.awt.Graphics2D;

public class GameStateManager {
    private ArrayList<GameState> gameStates;
    private int currentState;
    private GameClient client;
    private boolean isHost;
    private int port;

    public static final int INMENU = 0;
    public static final int INLEVEL = 1;
    public static final int GAMEOVER = 2;

    public GameStateManager(boolean isHost, int port) {
        this.isHost = isHost;
        this.port = port;
        gameStates = new ArrayList<>();
        currentState = INMENU;
        gameStates.add(new MenuState(this));
        gameStates.add(new Level1State(this));
        gameStates.add(new GameOverState(this));
    }

    public ArrayList<GameState> getGameStates() {
        return gameStates;
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    public void setState(int state) {
        this.currentState = state;
        gameStates.get(currentState).init();
    }

    public void update() {
        gameStates.get(currentState).update();
    }

    public void draw(Graphics2D g) {
        gameStates.get(currentState).draw(g);
    }

    public void keyPressed(int k) {
        gameStates.get(currentState).keyPressed(k);
    }

    public void keyReleased(int k) {
        gameStates.get(currentState).keyReleased(k);
    }

    public void sendInput(NetworkData.PlayerInput input) {
        if (client != null && client.isConnected()) {
            client.sendInput(input);
        }
    }

    public void updateGameState(NetworkData.GameStateData state) {
        if (currentState == INLEVEL) {
            ((Level1State) gameStates.get(INLEVEL)).updateGameState(state);
        }
    }

    public boolean isHost() {
        return isHost;
    }

    public GameClient getClient() {
        return client;
    }

    public GameServer getServer() {
        if (isHost && currentState == INMENU) {
            return ((MenuState) gameStates.get(INMENU)).getServer();
        }
        return null;
    }
}