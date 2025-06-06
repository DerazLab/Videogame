package Entity;

import TileMap.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.sound.sampled.*;

public class Player extends MapObject {
    private int health;
    private int maxHealth;
    private int score;
    private boolean dead;
    private boolean flinching;
    private long flinchTimer;
    private ArrayList<BufferedImage[]> sprites;
    private final int[] numFrames = {1, 3, 1, 1, 1, 1}; 
    private boolean holdingFlag;
    private boolean descendingFlag;
    private boolean descentComplete;
    private long descentStartTime;
    private double flagpoleX;
    private double groundY;
    private static final double DESCENT_SPEED = 1.0; 
    private boolean deathJump;
    private long deathJumpTimer;
    private long respawnTimer;
    private boolean awaitingRespawn;
    private boolean deathAnimationComplete;
    private static final long DEATH_JUMP_DURATION = 500_000_000;
    private static final long DEATH_FALL_DURATION = 1_500_000_000; 
    private static final double DEATH_JUMP_SPEED = -3.0;
    private static final long RESPAWN_DURATION = 10_000_000_000L; 
    private double spawnX, spawnY;

    private int playerId;

    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int JUMPING = 2;
    private static final int FALLING = 2;
    private static final int DEAD = 3;
    private static final int HOLDING_FLAG = 4;
    private static final int FLAG_DESCENT = 5;

    public Player(TileMap tm, int playerId) {
        super(tm);

        this.playerId = playerId;

        width = 16;
        height = 16;
        cwidth = 16;
        cheight = 16;

        moveSpeed = 0.3;
        maxSpeed = 6;
        stopSpeed = 0.4;
        fallSpeed = 0.15;
        maxFallSpeed = 4.0;
        jumpStart = -4.8;
        stopJumpSpeed = 0.3;

        facingRight = true;

        health = maxHealth = 1;
        score = 0;
        spawnX = 50 + (playerId * 20);
        spawnY = 100;

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

    private void playSound(String soundFile) {
        try {
            String soundPath = "/Resources/Sounds/" + soundFile;
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                getClass().getResourceAsStream(soundPath)
            );
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playJumpSound() {
        playSound("MarioJumps.wav");
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getScore() { return score; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isDead() { return dead; }
    public boolean isHoldingFlag() { return holdingFlag; }
    public boolean isAwaitingRespawn() { return awaitingRespawn; }
    public long getRespawnTimer() { return respawnTimer; }
    public boolean isDeathAnimationComplete() { return deathAnimationComplete; }
    public boolean isDescendingFlag() { return descendingFlag; }
    public boolean isDescentComplete() { return descentComplete; }

    public void setHealth(int health) { 
        this.health = health;
        if (health <= 0 && !dead) {
            dead = true;
            holdingFlag = false;
            descendingFlag = false;
            descentComplete = false;
            deathJump = true;
            deathAnimationComplete = false;
            deathJumpTimer = System.nanoTime();
            respawnTimer = System.nanoTime();
            awaitingRespawn = true;
            dx = 0;
            dy = DEATH_JUMP_SPEED;
            currentAction = DEAD;
            animation.setFrames(sprites.get(DEAD));
            animation.setDelay(-1);
            playSound("MarioDies.wav");
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
            deathJump = false;
            awaitingRespawn = false;
            deathAnimationComplete = false;
            descendingFlag = true;
            descentStartTime = System.nanoTime();
            currentAction = FLAG_DESCENT;
            animation.setFrames(sprites.get(FLAG_DESCENT));
            animation.setDelay(-1);
            score += 1000;
        }
    }

    public void respawn() {
        if (!awaitingRespawn) return;
        dead = false;
        awaitingRespawn = false;
        deathAnimationComplete = false;
        health = maxHealth;
        setPosition(spawnX, spawnY);
        dx = 0.5;
        dy = 0;
        left = false;
        right = true;
        jumping = false;
        falling = false;
        currentAction = WALKING;
        animation.setFrames(sprites.get(WALKING));
        animation.setDelay(40);
        new Thread(() -> {
            try {
                Thread.sleep(500);
                dx = 0;
                right = false;
                currentAction = IDLE;
                animation.setFrames(sprites.get(IDLE));
                animation.setDelay(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void hit(int damage) {
        if (dead || flinching || holdingFlag || descendingFlag) return;
        health -= damage;
        if (health < 0) health = 0;
        if (health == 0) {
            setHealth(0);
        } else {
            flinching = true;
            flinchTimer = System.nanoTime();
            playSound("MarioDamage.wav");
        }
    }

    public double getDy() { return dy; }

    public void setDy(double dy) {
        if (dead || holdingFlag || descendingFlag) return;
        this.dy = dy;
        if (dy < 0) {
            jumping = true;
        } else if (dy > 0) {
            falling = true;
        }
    }

    private void getNextPosition() {
        if (dead || holdingFlag || descendingFlag) {
            if (dead) {
                if (deathJump) {
                    long elapsed = (System.nanoTime() - deathJumpTimer) / 1_000_000;
                    if (elapsed < DEATH_JUMP_DURATION / 1_000_000) {
                        dy = DEATH_JUMP_SPEED;
                    } else {
                        deathJump = false;
                        dy = maxFallSpeed;
                    }
                } else if (awaitingRespawn) {
                    long elapsed = (System.nanoTime() - deathJumpTimer) / 1_000_000;
                    if (elapsed >= (DEATH_JUMP_DURATION + DEATH_FALL_DURATION) / 1_000_000) {
                        deathAnimationComplete = true;
                    }
                    dx = 0;
                    dy = maxFallSpeed;
                }
            } else if (descendingFlag) {
                dx = 0;
                if (y < groundY) {
                    dy = DESCENT_SPEED;
                } else {
                    dy = 0;
                    y = groundY;
                    descendingFlag = false;
                    descentComplete = true;
                    currentAction = IDLE;
                    animation.setFrames(sprites.get(IDLE));
                    animation.setDelay(400);
                }
            }
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
        if (holdingFlag || descendingFlag || dead) return;

        int currCol = (int)x / tileSize;
        int currRow = (int)y / tileSize;

        for (int row = Math.max(0, currRow - 1); row <= currRow + 1 && row < tileMap.getHeight() / tileSize; row++) {
            for (int col = Math.max(0, currCol - 1); col <= currCol + 1 && col < tileMap.getWidth() / tileSize; col++) {
                if (tileMap.getType(row, col) == Tile.FLAGPOLE) {
                    flagpoleX = col * tileSize + tileSize / 2;
                    int groundRow = row;
                    while (groundRow < tileMap.getHeight() / tileSize - 1 && 
                           tileMap.getType(groundRow + 1, col) != Tile.BLOCKED) {
                        groundRow++;
                    }
                    groundY = groundRow * tileSize;
                    setHoldingFlag(true);
                    setPosition(flagpoleX, y);
                    return;
                }
            }
        }
    }

    public void update() {
        if (dead) {
            if (awaitingRespawn) {
                long elapsed = (System.nanoTime() - respawnTimer) / 1_000_000;
                if (elapsed >= RESPAWN_DURATION / 1_000_000 && !holdingFlag && !descendingFlag) {
                    respawn();
                }
            }
            getNextPosition();
            ytemp = y + dy;
            setPosition(x, ytemp);
            animation.update();
            return;
        }

        if (!holdingFlag && !descendingFlag) {
            getNextPosition();
            checkTileMapCollision();
            checkFlagpoleCollision();
            setPosition(xtemp, ytemp);
        } else if (descendingFlag) {
            getNextPosition();
            ytemp = y + dy;
            setPosition(flagpoleX, ytemp);
        }

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) {
                flinching = false;
            }
        }

        if (holdingFlag && !descendingFlag) {
            if (currentAction != HOLDING_FLAG) {
                currentAction = HOLDING_FLAG;
                animation.setFrames(sprites.get(HOLDING_FLAG));
                animation.setDelay(-1);
                width = 16;
            }
        } else if (descendingFlag) {
            if (currentAction != FLAG_DESCENT) {
                currentAction = FLAG_DESCENT;
                animation.setFrames(sprites.get(FLAG_DESCENT));
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

    public void draw(Graphics2D bell) {
        setMapPosition();
        if (flinching && !dead && !holdingFlag && !descendingFlag) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed / 100 % 2 == 0) {
                return;
            }
        }
        if (!dead || deathJump || (awaitingRespawn && !deathAnimationComplete)) {
            super.draw(bell);
        }
    }
}