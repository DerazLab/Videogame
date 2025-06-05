package GameState;

import TileMap.*;
import Main.GamePanel;
import Main.GameServer;
import Main.NetworkData;
import Entity.*;
import Entity.Enemies.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

public class Level1State extends GameState {
    private TileMap tileMap;
    private Background bg;
    private ArrayList<Player> players;
    private ArrayList<Enemy> enemies;
    private GameServer server;
    private int localPlayerId;
    private Map<Integer, NetworkData.PlayerInput> playerInputs;
    private Map<Integer, Long> lastInputTimes;

    public Level1State(GameStateManager gsm) {
        this.gsm = gsm;
        localPlayerId = gsm.isHost() ? 0 : (gsm.getClient() != null ? gsm.getClient().getPlayerId() : 1);
        playerInputs = new HashMap<>();
        lastInputTimes = new HashMap<>();
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
            addPlayer(0); // Host player (Mario)
        } else {
            addPlayer(0); // Host player (Mario)
            addPlayer(1); // Client player (Luigi)
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

    public void addPlayer(int playerId) {
        Player newPlayer = new Player(tileMap, playerId);
        double xPos = 50 + (players.size() * 20);
        newPlayer.setPosition(xPos, 100);
        players.add(newPlayer);
        playerInputs.put(players.size() - 1, new NetworkData.PlayerInput()); // Initialize input
    }

    public Player getPlayer(int index) {
        if (index >= 0 && index < players.size()) {
            return players.get(index);
        }
        return null;
    }

    public ArrayList<Player> getPlayers() {
        return players;
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
        // Create a new PlayerInput to avoid modifying the original
        NetworkData.PlayerInput newInput = new NetworkData.PlayerInput();
        newInput.left = input.left;
        newInput.right = input.right;
        newInput.up = input.up;
        newInput.down = input.down;
        newInput.jumping = input.jumping;
        playerInputs.put(playerId, newInput);
        //System.out.println("Applying input for player " + playerId + ": left=" + newInput.left + ", right=" + newInput.right + ", up=" + newInput.up + ", down=" + newInput.down + ", jumping=" + newInput.jumping);
        player.setLeft(newInput.left);
        player.setRight(newInput.right);
        player.setUp(newInput.up);
        player.setDown(newInput.down);
        player.setJumping(newInput.jumping);
        lastInputTimes.put(playerId, System.nanoTime());
    }
}

    public void updateGameState(NetworkData.GameStateData state) {
    // Synchronize all players
    for (int i = 0; i < state.players.size() && i < players.size(); i++) {
        NetworkData.PlayerData data = state.players.get(i);
        Player player = players.get(i);
        // Preserve local player's position for smoother movement
        if (i != localPlayerId) {
            player.setPosition(data.x, data.y);
        }
        player.setHealth(data.health);
        player.setScore(data.score);
        player.setFacingRight(data.facingRight);
    }
    // Update enemies
    if (state.enemies != null) {
        Iterator<Enemy> enemyIterator = enemies.iterator();
        int index = 0;
        while (enemyIterator.hasNext() && index < state.enemies.size()) {
            Enemy enemy = enemyIterator.next();
            NetworkData.EnemyData enemyData = state.enemies.get(index);
            if (enemyData.dead) {
                enemyIterator.remove();
            } else {
                enemy.setPosition(enemyData.x, enemyData.y);
                enemy.setHealth(enemyData.health);
            }
            index++;
        }
    }
}

	// In Level1State.java
	public void updateClientInput() {
		if (!gsm.isHost()) {
			NetworkData.PlayerInput input = playerInputs.get(localPlayerId);
			if (input != null) {
				gsm.sendInput(input);
			}
		}
	}

    public void update() {
    // Reset inputs for non-local players if no recent update
    

    // Update players
    for (Player player : players) {
        player.update();
    }

    // Update enemies and check collisions
    Iterator<Enemy> enemyIterator = enemies.iterator();
    while (enemyIterator.hasNext()) {
        Enemy enemy = enemyIterator.next();
        enemy.update();
        if (gsm.isHost() && enemy.isDead()) {
            enemyIterator.remove();
            continue;
        }
        for (Player player : players) {
            if (player.intersects(enemy)) {
                if (player.getDy() > 0 && player.gety() < enemy.gety()) {
                    if (gsm.isHost()) {
                        player.setDy(-5);
                        enemy.hit(1);
                        player.setScore(player.getScore() + 100);
                    } else {
                        player.setDy(-5);
                        player.setScore(player.getScore() + 100);
                    }
                } else {
                    if (gsm.isHost()) {
                        player.hit(enemy.getDamage());
                    }
                }
            }
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
        NetworkData.PlayerInput input = playerInputs.get(localPlayerId);
        if (input == null) {
            input = new NetworkData.PlayerInput();
            playerInputs.put(localPlayerId, input);
        }

        if (k == KeyEvent.VK_LEFT) {
            input.left = true;
            //System.out.println("Key pressed: LEFT for player " + localPlayerId);
        }
        if (k == KeyEvent.VK_RIGHT) {
            input.right = true;
            //System.out.println("Key pressed: RIGHT for player " + localPlayerId);
        }
        if (k == KeyEvent.VK_UP) {
            input.up = true;
            //System.out.println("Key pressed: UP for player " + localPlayerId);
        }
        if (k == KeyEvent.VK_DOWN) {
            input.down = true;
            //System.out.println("Key pressed: DOWN for player " + localPlayerId);
        }
        if (k == KeyEvent.VK_W) {
            input.jumping = true;
            //System.out.println("Key pressed: W (JUMP) for player " + localPlayerId);
        }

        if (gsm.isHost()) {
            updatePlayerInput(localPlayerId, input);
        } else {
            gsm.sendInput(input);
        }
    }

public void keyReleased(int k) {
        NetworkData.PlayerInput input = playerInputs.get(localPlayerId);
        if (input == null) {
            input = new NetworkData.PlayerInput();
            playerInputs.put(localPlayerId, input);
        }

        if (k == KeyEvent.VK_LEFT) {
            input.left = false;
            //System.out.println("Key released: LEFT for player " + localPlayerId);
        }
        if (k == KeyEvent.VK_RIGHT) {
            input.right = false;
            //System.out.println("Key released: RIGHT for player " + localPlayerId);
        }
        if (k == KeyEvent.VK_UP) {
            input.up = false;
            //System.out.println("Key released: UP for player " + localPlayerId);
        }
        if (k == KeyEvent.VK_DOWN) {
            input.down = false;
            //System.out.println("Key released: DOWN for player " + localPlayerId);
        }
        if (k == KeyEvent.VK_W) {
            input.jumping = false;
            //System.out.println("Key released: W (JUMP) for player " + localPlayerId);
        }

        if (gsm.isHost()) {
            updatePlayerInput(localPlayerId, input);
        } else {
            gsm.sendInput(input);
        }
    }
}