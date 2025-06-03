package GameState;

import TileMap.TileMap;
import Main.GamePanel;

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
        tileMap = new TileMap(16);
        tileMap.loadTiles("Resources/Tilesets/TileMap.png");
        tileMap.loadMap("Resources/Maps/level1.map");
        tileMap.setPosition(0, 0);
    }

    public void update() {}
    public void draw(Graphics2D g) 
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        tileMap.draw(g);
    }
    public void keyPressed(int k) {}
    public void keyReleased(int k) {}
}