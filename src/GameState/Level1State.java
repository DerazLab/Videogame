package GameState;

import TileMap.*;
import Main.GamePanel;
import Main.GameServer;
import Main.NetworkData;
import Entity.*;
import Entity.Enemies.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Level1State extends GameState {
    private TileMap tileMap;
    private Background bg;
    private ArrayList<Player> players;
    private ArrayList<Enemy> enemies;
    private GameServer server;

    public Level1State(GameStateManager gsm) {
        this.gsm = gsm;
        init();
    }

    public void init() {
        tileMap = new TileMap(16);
        tileMap.loadTiles("Resources/Tilesets/tileset1.png");
        tileMap.loadMap("Resources/Maps/level1.map");
        tileMap.setPosition(0, 0);

        bg = new Background("Resources/Backgrounds/SkyBackground.png", 0.1);

        players = new ArrayList<>();
        //Siempre agregar un jugador para el host
        addPlayer(); // Jugador inicial en posición (50, 100)

        enemies = new ArrayList<>();
        Goomba goomba = new Goomba(tileMap);
        goomba.setPosition(100, 100);
        enemies.add(goomba);

        // Vincular el servidor si es host
        if (gsm.isHost()) {
            server = ((MenuState) gsm.getGameStates().get(GameStateManager.INMENU)).getServer();
            server.setGameState(this);
        }
    }

    public GameServer getServer() {
        return server;
    }

    public void addPlayer() {
        Player newPlayer = new Player(tileMap);
        // Establecer posición inicial diferente para cada jugador
        double xPos = 50 + (players.size() * 20); // 50, 70, 90, etc.
        newPlayer.setPosition(xPos, 100);
        players.add(newPlayer);
    }

    public Player getPlayer(int index) {
        if (index >= 0 && index < players.size()) {
            return players.get(index);
        }
        return null;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void updatePlayerInput(int playerId, NetworkData.PlayerInput input) {
        Player player = getPlayer(playerId);
        if (player != null) {
            player.setLeft(input.left);
            player.setRight(input.right);
            player.setUp(input.up);
            player.setDown(input.down);
            player.setJumping(input.jumping);
        }
    }

    public void updateGameState(NetworkData.GameStateData state) {
        for (int i = 0; i < state.players.size() && i < players.size(); i++) {
            NetworkData.PlayerData data = state.players.get(i);
            Player player = players.get(i);
            player.setPosition(data.x, data.y);
            player.setHealth(data.health);
            player.setScore(data.score);
            player.setFacingRight(data.facingRight);
        }
    }

    public void update() {
        for (Player player : players) {
            player.update();
        }
        for (Enemy enemy : enemies) {
            enemy.update();
        }
        //Solo actualizar la posición del mapa si hay al menos un jugador 
        if (!players.isEmpty()) {
            tileMap.setPosition(
                GamePanel.WIDTH / 2 - players.get(0).getx(),
                GamePanel.HEIGHT / 2 - players.get(0).gety()
            );
        }

        if (server != null) {
            server.broadcastGameState();
        }
    }

    public void draw(Graphics2D g) {
        bg.draw(g);
        tileMap.draw(g);
        for (Player player : players) {
            player.draw(g);
        }
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
    }

    public void keyPressed(int k) {
        NetworkData.PlayerInput input = new NetworkData.PlayerInput();
        if (k == KeyEvent.VK_LEFT) input.left = true;
        if (k == KeyEvent.VK_RIGHT) input.right = true;
        if (k == KeyEvent.VK_UP) input.up = true;
        if (k == KeyEvent.VK_DOWN) input.down = true;
        if (k == KeyEvent.VK_W) input.jumping = true;
        gsm.sendInput(input);
    }

    public void keyReleased(int k) {
        NetworkData.PlayerInput input = new NetworkData.PlayerInput();
        if (k == KeyEvent.VK_LEFT) input.left = false;
        if (k == KeyEvent.VK_RIGHT) input.right = false;
        if (k == KeyEvent.VK_UP) input.up = false;
        if (k == KeyEvent.VK_DOWN) input.down = false;
        if (k == KeyEvent.VK_W) input.jumping = false;
        gsm.sendInput(input);
    }
}