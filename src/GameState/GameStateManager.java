package GameState;

import java.util.ArrayList;
import java.awt.Graphics2D;

public class GameStateManager
{
    private ArrayList<GameState> gameStates;
    private int currentState;

    public static final int INMENU = 0;
    public static final int INLEVEL = 1;

    public GameStateManager()
    {
        gameStates = new ArrayList<GameState>();

        currentState = INMENU;
        gameStates.add(new MenuState(this));
        gameStates.add(new Level1State(this));

    }

    public void setState(int state)
    {
        this.currentState = state;
        gameStates.get(currentState).init();
    }

    public void update()
    {
        gameStates.get(currentState).update();
    }

    public void draw(Graphics2D g)
    {
        gameStates.get(currentState).draw(g);
    }

    public void keyPressed(int k)
    {
        gameStates.get(currentState).keyPressed(k);
    }

    public void keyReleased(int k)
    {
        gameStates.get(currentState).keyReleased(k);       
    }
}
