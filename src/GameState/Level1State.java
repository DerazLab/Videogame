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
    private Flagpole flagpole;
    private Font countdownFont;

    private Goomba goomba, goomba2, goomba3, goomba4, goomba5, goomba6;
    private Goomba g, g1, g2, g3, g4, g5, g6, g7, g8;

    public Level1State(GameStateManager gsm) {
        this.gsm = gsm;
        localPlayerId = gsm.isHost() ? 0 : (gsm.getClient() != null ? gsm.getClient().getPlayerId() : 1);
        playerInputs = new HashMap<>();
        lastInputTimes = new HashMap<>();
        countdownFont = new Font("Arial", Font.PLAIN, 12);
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
            addPlayer(0);
        } else {
            addPlayer(0);
            addPlayer(1);
        }

         //AGREGAR ENEMIGOS ---------------------------------------------
        enemies = new ArrayList<>();
        goomba = new Goomba(tileMap, 300, 100);
        enemies.add(goomba);

        goomba2 = new Goomba(tileMap, 700, 50);
        enemies.add(goomba2);

        goomba3 = new Goomba(tileMap, 800, 50);
        enemies.add(goomba3);

        goomba4 = new Goomba(tileMap, 750, 50);
        enemies.add(goomba4);

        goomba5 = new Goomba(tileMap, 900, 50);
        enemies.add(goomba5);

        goomba6 = new Goomba(tileMap, 950, 50);
        enemies.add(goomba6);

        g = new Goomba(tileMap, 1050, 50);
        enemies.add(g);

        g2 = new Goomba(tileMap, 1100, 50);
        enemies.add(g2);

        g3 = new Goomba(tileMap, 1250, 50);
        enemies.add(g3);

        g4 = new Goomba(tileMap, 1350, 50);
        enemies.add(g4);

        g5 = new Goomba(tileMap, 1550, 50);
        enemies.add(g5);

        g6 = new Goomba(tileMap, 1600, 50);
        enemies.add(g6);

        g7 = new Goomba(tileMap, 1800, 50);
        enemies.add(g7);

        g8 = new Goomba(tileMap, 1850, 50);
        enemies.add(g8);
        // -------------------------------------------------------------


        flagpole = new Flagpole(tileMap, 300, 100);
        flagpole.setPosition(300, 100);

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
        playerInputs.put(players.size() - 1, new NetworkData.PlayerInput());
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
        if (player != null && !player.isDead() && !player.isHoldingFlag() && !player.isDescendingFlag()) {
            NetworkData.PlayerInput newInput = new NetworkData.PlayerInput();
            newInput.left = input.left;
            newInput.right = input.right;
            newInput.up = input.up;
            newInput.down = input.down;
            newInput.jumping = input.jumping;
            playerInputs.put(playerId, newInput);
            player.setLeft(newInput.left);
            player.setRight(newInput.right);
            player.setUp(newInput.up);
            player.setDown(newInput.down);
            player.setJumping(newInput.jumping);
            lastInputTimes.put(playerId, System.nanoTime());
        }
    }

    public void updateGameState(NetworkData.GameStateData state) {
        for (int i = 0; i < state.players.size() && i < players.size(); i++) {
            NetworkData.PlayerData data = state.players.get(i);
            Player player = players.get(i);
            if (i != localPlayerId || data.dead || data.holdingFlag || data.descendingFlag) {
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

    public void updateClientInput() {
        if (!gsm.isHost()) {
            NetworkData.PlayerInput input = playerInputs.get(localPlayerId);
            if (input != null) {
                gsm.sendInput(input);
            }
        }
    }

    public void update() {
        boolean allDead = true;
        boolean allAnimationsComplete = true;
        for (Player player : players) {
            player.update();
            if (!player.isDead()) {
                allDead = false;
            }
            if (!player.isDeathAnimationComplete()) {
                allAnimationsComplete = false;
            }
        }

        if (gsm.isHost() && allDead && allAnimationsComplete) {
            gsm.setState(GameStateManager.GAMEOVER);
            if (server != null) {
                server.notifyStateChange(GameStateManager.GAMEOVER);
            }
            return;
        }

        if (gsm.isHost() && !allDead) {
            boolean allDescended = true;
            for (Player player : players) {
                if (!player.isDead() && !player.isDescentComplete()) {
                    allDescended = false;
                    break;
                }
            }
            if (allDescended) {
                gsm.setState(GameStateManager.WIN);
                if (server != null) {
                    server.notifyStateChange(GameStateManager.WIN);
                }
                return;
            }
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update();
            if (gsm.isHost() && enemy.isDead()) {
                enemyIterator.remove();
                continue;
            }
            for (Player player : players) {
                if (!player.isDead() && !player.isHoldingFlag() && !player.isDescendingFlag() && player.intersects(enemy)) {
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

        Player localPlayer = getPlayer(localPlayerId);
        if (localPlayer != null && !localPlayer.isDead()) {
            tileMap.setPosition(
                GamePanel.WIDTH / 2 - localPlayer.getx(),
                GamePanel.HEIGHT / 2 - localPlayer.gety()
            );
        } else {
            boolean centered = false;
            for (Player player : players) {
                if (!player.isDead()) {
                    tileMap.setPosition(
                        GamePanel.WIDTH / 2 - player.getx(),
                        GamePanel.HEIGHT / 2 - player.gety()
                    );
                    centered = true;
                    break;
                }
            }
            if (!centered) {
                tileMap.setPosition(0, 0);
            }
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
            if (player.isAwaitingRespawn() && !player.isDeathAnimationComplete()) {
                long elapsed = (System.nanoTime() - player.getRespawnTimer()) / 1_000_000;
                int secondsLeft = (int) ((10_000 - elapsed) / 1000);
                if (secondsLeft >= 0) {
                    g.setFont(countdownFont);
                    g.setColor(Color.WHITE);
                    g.drawString("Reapareciendo en: " + secondsLeft, (int)(player.getx() + tileMap.getX()), (int)(player.gety() + tileMap.getY() - 20));
                }
            }
        }
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
    }

    public void keyPressed(int k) {
        Player localPlayer = getPlayer(localPlayerId);
        if (localPlayer != null && (localPlayer.isDead() || localPlayer.isHoldingFlag() || localPlayer.isDescendingFlag())) {
            return;
        }

        NetworkData.PlayerInput input = playerInputs.get(localPlayerId);
        if (input == null) {
            input = new NetworkData.PlayerInput();
            playerInputs.put(localPlayerId, input);
        }

        if (k == KeyEvent.VK_LEFT) {
            input.left = true;
        }
        if (k == KeyEvent.VK_RIGHT) {
            input.right = true;
        }
        if (k == KeyEvent.VK_UP) {
            input.up = true;
        }
        if (k == KeyEvent.VK_DOWN) {
            input.down = true;
        }
        if (k == KeyEvent.VK_W) {
            input.jumping = true;
            localPlayer.playJumpSound(); // Play jump sound locally
        }

        if (gsm.isHost()) {
            updatePlayerInput(localPlayerId, input);
        } else {
            gsm.sendInput(input);
        }
    }

    public void keyReleased(int k) {
        Player localPlayer = getPlayer(localPlayerId);
        if (localPlayer != null && (localPlayer.isDead() || localPlayer.isHoldingFlag() || localPlayer.isDescendingFlag())) {
            return;
        }

        NetworkData.PlayerInput input = playerInputs.get(localPlayerId);
        if (input == null) {
            input = new NetworkData.PlayerInput();
            playerInputs.put(localPlayerId, input);
        }

        if (k == KeyEvent.VK_LEFT) {
            input.left = false;
        }
        if (k == KeyEvent.VK_RIGHT) {
            input.right = false;
        }
        if (k == KeyEvent.VK_UP) {
            input.up = false;
        }
        if (k == KeyEvent.VK_DOWN) {
            input.down = false;
        }
        if (k == KeyEvent.VK_W) {
            input.jumping = false;
        }

        if (gsm.isHost()) {
            updatePlayerInput(localPlayerId, input);
        } else {
            gsm.sendInput(input);
        }
    }
}