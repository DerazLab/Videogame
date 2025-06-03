package GameState;

import TileMap.*;
import Main.GamePanel;
import Entity.*;

import java.awt.*;


public class Level1State extends GameState
{

    private TileMap tileMap;
    private Background bg;

    private Player player;
    
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

        bg = new Background ("Resources/Backgrounds/SkyBackground.png", 0.1);

        player = new Player(tileMap);
        player.setPosition(100, 100);
    }

    public void update() 
    {
        player.update();
    }

    public void draw(Graphics2D g) 
    {
        //draw background
        bg.draw(g);
        //g.setColor(Color.RED);
        //g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        tileMap.draw(g);

        player.draw(g);
    }
    public void keyPressed(int k) {}
    public void keyReleased(int k) {}
}