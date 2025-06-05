package Entity;

import TileMap.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends MapObject {
    private int health;
    private int maxHealth;
    private int score;
    private boolean dead;
    private boolean flinching;
    private long flinchTimer;
    private boolean scratching;
    private int scratchDamage;
    private int scratchRange;
    private boolean gliding;
    private ArrayList<BufferedImage[]> sprites;
    private final int[] numFrames = {1, 3, 1, 1, 1}; // Added HOLDING_FLAG
    private boolean holdingFlag;

    private int playerId;

    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int JUMPING = 2;
    private static final int FALLING = 2;
    private static final int DEAD = 3;
    private static final int HOLDING_FLAG = 4;

    public Player(TileMap tm, int playerId) {
        super(tm);

        this.playerId = playerId;

        width = 16;
        height = 16;
        cwidth = 16;
        cheight = 16;

        moveSpeed = 0.3;
        maxSpeed = 1.6;
        stopSpeed = 0.4;
        fallSpeed = 0.15;
        maxFallSpeed = 4.0;
        jumpStart = -4.8;
        stopJumpSpeed = 0.3;

        facingRight = true;

        health = maxHealth = 5;
        score = 0;

        try {
            String spritePath = playerId == 0 ? 
                "/Resources/Sprites/Player/MarioSprites.png" : 
                "/Resources/Sprites/Player/LuigiSprites.png";
            BufferedImage spritesheet = ImageIO.read(
                getClass().getResourceAsStream(spritePath)
            );
            sprites = new ArrayList<>();
            for (int i = 0; i < numFrames.length; i++) {
                BufferedImage[] bi = new BufferedImage[numFrames[i]];
                for (int j = 0; j < numFrames[i]; j++) {
                    bi[j] = spritesheet.getSubimage(j * width, i * height, width, height);
                }
                sprites.add(bi);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar sprites para player " + playerId + ": " + e.getMessage());
            e.printStackTrace();
        }

        animation = new Animation();
        currentAction = IDLE;
        if (sprites == null || sprites.isEmpty()) {
            throw new IllegalStateException("Lista de sprites no inicializada");
        }
        animation.setFrames(sprites.get(IDLE));
        animation.setDelay(400);
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getScore() { return score; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isDead() { return dead; }
    public boolean isHoldingFlag() { return holdingFlag; }

    public void setHealth(int health) { 
        this.health = health;
        if (health <= 0) {
            dead = true;
            holdingFlag = false;
            currentAction = DEAD;
            animation.setFrames(sprites.get(DEAD));
            animation.setDelay(-1);
        }
    }

    public void setScore(int score) { this.score = score; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public void setHoldingFlag(boolean holding) {
        this.holdingFlag = holding;
        if (holding) {
            dx = 0;
            dy = 0;
            jumping = false;
            falling = false;
            left = false;
            right = false;
            up = false;
            down = false;
            currentAction = HOLDING_FLAG;
            animation.setFrames(sprites.get(IDLE));
            animation.setDelay(-1); // Static frame
        }
    }

    public void hit(int damage) {
        if (dead || flinching || holdingFlag) return;
        health -= damage;
        if (health < 0) health = 0;
        if (health == 0) {
            dead = true;
            holdingFlag = false;
            currentAction = DEAD;
            animation.setFrames(sprites.get(DEAD));
            animation.setDelay(-1);
        } else {
            flinching = true;
            flinchTimer = System.nanoTime();
        }
    }

    public double getDy() { return dy; }

    public void setDy(double dy) {
        if (dead || holdingFlag) return;
        this.dy = dy;
        if (dy < 0) jumping = true;
        else if (dy > 0) falling = true;
    }

    private void getNextPosition() {
        if (dead || holdingFlag) {
            dx = 0;
            dy = 0;
            return;
        }
        if (left && !right) {
            dx -= moveSpeed;
            if (dx < -maxSpeed) {
                dx = -maxSpeed;
            }
        } else if (right && !left) {
            dx += moveSpeed;
            if (dx > maxSpeed) {
                dx = maxSpeed;
            }
        } else if (left && right) {
            dx = 0;
        } else {
            if (dx > 0) {
                dx -= stopSpeed;
                if (dx < 0) {
                    dx = 0;
                }
            } else if (dx < 0) {
                dx += stopSpeed;
                if (dx > 0) {
                    dx = 0;
                }
            }
        }

        if (jumping && !falling) {
            dy = jumpStart;
            falling = true;
        }

        if (falling) {
            dy += fallSpeed;
            if (dy > 0) jumping = false;
            if (dy < 0 && !jumping) dy += stopJumpSpeed;
            if (dy > maxFallSpeed) dy = maxFallSpeed;
        }
    }

    public void checkFlagpoleCollision() {
        if (holdingFlag || dead) return;

        int currCol = (int)x / tileSize;
        int currRow = (int)y / tileSize;

        // Check surrounding tiles for flagpole
        for (int row = Math.max(0, currRow - 1); row <= currRow + 1 && row < tileMap.getHeight() / tileSize; row++) {
            for (int col = Math.max(0, currCol - 1); col <= currCol + 1 && col < tileMap.getWidth() / tileSize; col++) {
                if (tileMap.getType(row, col) == Tile.FLAGPOLE) {
                    setHoldingFlag(true);
                    setPosition(col * tileSize + tileSize / 2, row * tileSize + tileSize / 2);
                    return;
                }
            }
        }
    }

    public void update() {
        if (dead) {
            animation.update();
            return;
        }

        if (!holdingFlag) {
            getNextPosition();
            checkTileMapCollision();
            checkFlagpoleCollision();
            setPosition(xtemp, ytemp);
        }

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
            if (elapsed > 1000) {
                flinching = false;
            }
        }

        if (holdingFlag) {
            if (currentAction != HOLDING_FLAG) {
                currentAction = HOLDING_FLAG;
                animation.setFrames(sprites.get(HOLDING_FLAG));
                animation.setDelay(-1);
                width = 16;
            }
        } else if (dy > 0) {
            if (currentAction != FALLING) {
                currentAction = FALLING;
                animation.setFrames(sprites.get(FALLING));
                animation.setDelay(100);
                width = 16;
            }
        } else if (dy < 0) {
            if (currentAction != JUMPING) {
                currentAction = JUMPING;
                animation.setFrames(sprites.get(JUMPING));
                animation.setDelay(-1);
                width = 16;
            }
        } else if (left || right) {
            if (currentAction != WALKING) {
                currentAction = WALKING;
                animation.setFrames(sprites.get(WALKING));
                animation.setDelay(40);
                width = 16;
            }
        } else {
            if (currentAction != IDLE) {
                currentAction = IDLE;
                animation.setFrames(sprites.get(IDLE));
                animation.setDelay(400);
                width = 16;
            }
        }

        animation.update();

        if (right) facingRight = true;
        if (left) facingRight = false;
    }

    public void draw(Graphics2D g) {
        setMapPosition();
        if (flinching && !dead && !holdingFlag) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
            if (elapsed / 100 % 2 == 0) {
                return;
            }
        }
        super.draw(g);
    }
}