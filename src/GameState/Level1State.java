package GameState;

import java.awt.*;

public class Level1State extends GameState
{

    private TileMap tileMap;

    public Level1State(GameStateManager gsm)
    {
        this.gsm = gsm;
        init();
    }

    public void init() 
    {
        tileMap = new TileMap(30);
    }
    public void update() {}
    public void draw(Graphics2D g) {}
    public void keyPressed(int k) {}
    pblic void keyReleased(int k) {}
}