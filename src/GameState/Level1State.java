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
import java.util.Iterator;

public class Level1State extends GameState {
    private TileMap tileMap;
    private Background bg;
    private ArrayList<Player> players;
    private ArrayList<Enemy> enemies;
    private GameServer server;
    private int localPlayerId;

    public Level1State(GameStateManager gsm) {
        this.gsm = gsm;
        localPlayerId = gsm.isHost() ? 0 : (gsm.getClient() != null ? gsm.getClient().getPlayerId() : 1);
        init();
    }

    public void init() {
        tileMap = new TileMap(16);
        tileMap.loadTiles("Resources/Tilesets/tileset1.png");
        tileMap.loadMap("Resources/Maps/level1.map");
        tileMap.setPosition(0, 0);

        bg = new Background("Resources/Backgrounds/SkyBackground.png", 0.1);

        players = new ArrayList<>();
        if (gsm.isHost()) {
            addPlayer();
        } else {
            addPlayer();
            addPlayer();
        }

        enemies = new ArrayList<>();
        Goomba goomba = new Goomba(tileMap);
        goomba.setPosition(100, 100);
        enemies.add(goomba);

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
        double xPos = 50 + (players.size() * 20);
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

    public ArrayList<Enemy> getEnemies() {
        return enemies;
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
<<<<<<< HEAD
        // Update players
=======
        // sincronizar todos los jugadores
>>>>>>> db9b5e968d4e9967e96f08e3f58404a553ae40bc
        for (int i = 0; i < state.players.size() && i < players.size(); i++) {
            NetworkData.PlayerData data = state.players.get(i);
            Player player = players.get(i);
            player.setPosition(data.x, data.y);
            player.setHealth(data.health);
            player.setScore(data.score);
            player.setFacingRight(data.facingRight);
        }
        // Update enemies (only update positions and health, don't recreate)
        if (state.enemies != null) {
            for (int i = 0; i < state.enemies.size() && i < enemies.size(); i++) {
                NetworkData.EnemyData enemyData = state.enemies.get(i);
                Enemy enemy = enemies.get(i);
                if (enemyData.dead) {
                    enemies.remove(i);
                } else {
                    enemy.setPosition(enemyData.x, enemyData.y);
                    enemy.setHealth(enemyData.health);
                }
            }
        }
    }

    public void update() {
        // Update players
        for (Player player : players) {
            player.update();
        }

        // Update enemies and check collisions (only on host)
        if (gsm.isHost()) {
            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                enemy.update();
                if (enemy.isDead()) {
                    enemyIterator.remove();
                    continue;
                }
                for (Player player : players) {
                    if (player.intersects(enemy)) {
                        if (player.getDy() > 0) { // Player is falling (jumping on enemy)
                            enemy.hit(1);
                            player.setScore(player.getScore() + 100);
                        } else {
                            player.hit(enemy.getDamage());
                        }
                    }
                }
            }
        } else {
            // Clients update enemies locally
            for (Enemy enemy : enemies) {
                enemy.update();
            }
        }

        // Center camera on local player
        Player localPlayer = getPlayer(localPlayerId);
        if (localPlayer != null) {
            tileMap.setPosition(
                GamePanel.WIDTH / 2 - localPlayer.getx(),
                GamePanel.HEIGHT / 2 - localPlayer.gety()
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

        if (gsm.isHost()) {
            updatePlayerInput(0, input);
        } else {
            gsm.sendInput(input);
        }
    }

    public void keyReleased(int k) {
        NetworkData.PlayerInput input = new NetworkData.PlayerInput();
        if (k == KeyEvent.VK_LEFT) input.left = false;
        if (k == KeyEvent.VK_RIGHT) input.right = false;
        if (k == KeyEvent.VK_UP) input.up = false;
        if (k == KeyEvent.VK_DOWN) input.down = false;
        if (k == KeyEvent.VK_W) input.jumping = false;

        if (gsm.isHost()) {
            updatePlayerInput(0, input);
        } else {
            gsm.sendInput(input);
        }
    }
}