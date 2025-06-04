package GameState;

import TileMap.*;
import Main.GamePanel;
import Entity.*;
import Entity.Enemies.Goomba;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;


public class Level1State extends GameState
{

    private TileMap tileMap;
    private Background bg;
    private HUD hud;

    private Player player;

    private ArrayList<Enemy> enemies;
    
    public Level1State(GameStateManager gsm)
    {
        this.gsm = gsm;
        init();
    }

    public void init() 
    {
        tileMap = new TileMap(16);
        tileMap.loadTiles("Resources/Tilesets/tileset1.png");
        tileMap.loadMap("Resources/Maps/level1.map");
        tileMap.setPosition(0, 0);

        bg = new Background ("Resources/Backgrounds/SkyBackground.png", 0.1);

        player = new Player(tileMap);
        player.setPosition(50, 100);

        enemies = new ArrayList<Enemy>();
        Goomba goomba;
        goomba = new Goomba(tileMap);
        goomba.setPosition(100, 100);
        enemies.add(goomba);

        hud = new HUD(player);
		
		tileMap.setPosition(
        GamePanel.WIDTH / 2 - player.getx(), 
        GamePanel.HEIGHT / 2 - player.gety()
    );
    }

    public void update() 
    {
        player.update();
        tileMap.setPosition(GamePanel.WIDTH / 2 - player.getx(), GamePanel.HEIGHT / 2 - player.gety());
        for(int i = 0; i < enemies.size(); i++) {
            enemies.get(i).update();
        }
    }

    public void draw(Graphics2D g) 
    {
        //draw background
        bg.draw(g);
        //g.setColor(Color.RED);
        //g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        tileMap.draw(g);

        player.draw(g);
        for(int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(g);
        }

        hud.draw(g);
    }
    
    
    
    
    public void keyPressed(int k) {
		if(k == KeyEvent.VK_LEFT) player.setLeft(true);
		if(k == KeyEvent.VK_RIGHT) player.setRight(true);
		if(k == KeyEvent.VK_UP) player.setUp(true);
		if(k == KeyEvent.VK_DOWN) player.setDown(true);
		if(k == KeyEvent.VK_W) player.setJumping(true);
		//if(k == KeyEvent.VK_E) player.setGliding(true);
		//if(k == KeyEvent.VK_R) player.setScratching();
		//if(k == KeyEvent.VK_F) player.setFiring();
	}
	
	public void keyReleased(int k) {
		if(k == KeyEvent.VK_LEFT) player.setLeft(false);
		if(k == KeyEvent.VK_RIGHT) player.setRight(false);
		if(k == KeyEvent.VK_UP) player.setUp(false);
		if(k == KeyEvent.VK_DOWN) player.setDown(false);
		if(k == KeyEvent.VK_W) player.setJumping(false);
		//if(k == KeyEvent.VK_E) player.setGliding(false);
	}

}