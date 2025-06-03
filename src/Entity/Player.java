package Entity;

import TileMap.*;

import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends MapObject
{
    private int health;
    private int maxHealth;
    //private int fire;
    //private int maxFire;
    private boolean dead;
    private boolean flinching;
    private long flinchTime;

    // fireball
    //private boolean firing;
    //private int fireCost;
    //private int fireBallDamage;
    // private ArrayList<FireBall> fireBalls;
    
    private boolean scratching;
    private int scratchDamage;
    private int scratchRange;

    // gliding -- lo quitamos 
    private boolean gliding;

    // animation
    private ArrayList<BufferedImage[]> sprites;

    //frame para cada animation action
	private final int[] numFrames = {
        1, 3, 1, 1, 1
	};

    // animation actions

	private static final int IDLE = 0;
	private static final int WALKING = 1;
	private static final int JUMPING = 2;
	private static final int FALLING = 2;
    //private static final int SCRATCHING = 3; //Attack
    private static final int DEAD = 3;
	//private static final int GLIDING = 4;
	//private static final int FIREBALL = 5;

    public Player(TileMap tm)
    {
        super(tm);
		
        width = 16;
		height = 16;
		cwidth = 20;
		cheight = 20;


        moveSpeed = 0.3;
		maxSpeed = 1.6;
		stopSpeed = 0.4;
		fallSpeed = 0.15;
		maxFallSpeed = 4.0;
		jumpStart = -4.8;
		stopJumpSpeed = 0.3;

        facingRight = true;

        health = maxHealth = 5;

        //fire = maxFire = 2500;
		
		//fireCost = 200;
		//fireBallDamage = 5;
		//fireBalls = new ArrayList<FireBall>();

        //Ataque ------
        //scratchDamage = 8;
		//scratchRange = 40;

        // load sprites
		try {
			
			BufferedImage spritesheet = ImageIO.read(
				getClass().getResourceAsStream(
					"Resources/sprites/MarioSprites.png"
				)
			);
			
			sprites = new ArrayList<BufferedImage[]>();
			for(int i = 0; i < 7; i++) {
				
				BufferedImage[] bi =
					new BufferedImage[numFrames[i]];
				
				for(int j = 0; j < numFrames[i]; j++) {
					
					if(i != 6) {
						bi[j] = spritesheet.getSubimage(
								j * width,
								i * height,
								width,
								height
						);
					}
					else {
						bi[j] = spritesheet.getSubimage(
								j * width * 2,
								i * height,
								width,
								height
						);
					}
					
				}
				
				sprites.add(bi);
				
			}
			
		}



    }
	
}