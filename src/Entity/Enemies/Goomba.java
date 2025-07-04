package Entity.Enemies;

import Entity.*;
import TileMap.TileMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Goomba extends Enemy {

    private BufferedImage[] sprites;
    public Goomba(TileMap tm, double x, double y) {
        super(tm);
        moveSpeed = 0.3;
        maxSpeed = 0.3;
        fallSpeed = 0.2;
        maxFallSpeed = 10.0;

        width = 16;
        height = 16;
        cwidth = 16;
        cheight = 16;

        health = maxHealth = 1;
        damage = 1;
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Resources/Sprites/Enemies/Goomba.png"));
            sprites = new BufferedImage[2];
            for(int i = 0; i < sprites.length; i++) {
                sprites[i] = spritesheet.getSubimage(i * width, 0, width, height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        animation = new Animation();
        animation.setFrames(sprites);
        animation.setDelay(300);

        right = true;
        
        setPosition(x, y);
    }

    private void getNextPosition() {
        if(left) {
            dx -= moveSpeed;
            if(dx < -maxSpeed) {
                dx = -maxSpeed;
            }
        }
        else if(right) {
            dx += moveSpeed;
            if(dx > maxSpeed) {
                dx = maxSpeed;
            }
        }

        if(falling) {
            dy += fallSpeed;
            if(dy > maxFallSpeed) {
                dy = maxFallSpeed;
            }
        }
    }

    public void update() {
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if(right && dx == 0) {
            right = false;
            left = true;
            facingRight = false;
        } else if(left && dx == 0) {
            right = true;
            left = false;
            facingRight = true;
        }
        animation.update();
		super.update();
    }

    public void draw(java.awt.Graphics2D g) {
        setMapPosition();
        super.draw(g);
    }
}