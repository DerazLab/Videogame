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
	private int score;
    //private int fire;
    //private int maxFire;
    private boolean dead;
    private boolean flinching;
    private long flinchTimer;

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

        //fire = maxFire = 2500;
		
		//fireCost = 200;
		//fireBallDamage = 5;
		//fireBalls = new ArrayList<FireBall>();

        //Ataque ------
        //scratchDamage = 8;
		//scratchRange = 40;

        // load sprites
		try 
		{
			
			BufferedImage spritesheet = ImageIO.read(
				getClass().getResourceAsStream(
					"/Resources/Sprites/Player/MarioSprites.png"
				)
			);
			
			sprites = new ArrayList<BufferedImage[]>();
			for(int i = 0; i < numFrames.length; i++) 
			{
				
				BufferedImage[] bi = new BufferedImage[numFrames[i]];
				
				for(int j = 0; j < numFrames[i]; j++) 
				{
					
						bi[j] = spritesheet.getSubimage(
								j * width,
								i * height,
								width,
								height
						);
					
				}
				
				sprites.add(bi);
				
			}
			
		}
		catch(Exception e) 
		{
			System.err.println("Error al cargar sprites: " + e.getMessage());
			e.printStackTrace();
		}

		animation = new Animation();
		currentAction = IDLE;


		//DEBUG
		if (sprites == null || sprites.isEmpty()) 
		{
    		throw new IllegalStateException("Lista de sprites no inicializada");
		}
		// -------------------------------------------------

		animation.setFrames(sprites.get(IDLE));
		animation.setDelay(400);




    }

	// METODOS PARA USARLOS EN LA HUD --------
	public int getHealth() { return health; }
	public int getMaxHealth() { return maxHealth; }
	public int getScore() { return score; }
	//public int getFire() { return fire; }
	//public int getMaxFire() { return maxFire; }


	// NO APLICABLES AL PERSONAJE DE MARIO ----------------------------------------
	/* 
	public void setFiring() { 
		firing = true;
	}
	public void setScratching() {
		scratching = true;
	}
	public void setGliding(boolean b) { 
		gliding = b;
	}
	*/ 


	private void getNextPosition() 
	{
		
		// movement
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
		else {
			if(dx > 0) {
				dx -= stopSpeed;
				if(dx < 0) {
					dx = 0;
				}
			}
			else if(dx < 0) {
				dx += stopSpeed;
				if(dx > 0) {
					dx = 0;
				}
			}
		}
		
		// No se puede mover mientras se ataca
		// (Mario no ataca pero se deja por si acaso queremos que ataque)

		/*
		if(
		(currentAction == SCRATCHING || currentAction == FIREBALL) && !(jumping || falling)) 
		{
			dx = 0; // NO MOVE
		}
		*/
		
		// jumping
		if(jumping && !falling) {
			dy = jumpStart;
			falling = true;	
			System.out.println("falling True");
		}
		
		// falling
		if(falling) {
			
			//if(dy > 0 && gliding) dy += fallSpeed * 0.1; //CAER LENTO
			//else 
			dy += fallSpeed;
			
			if(dy > 0) jumping = false;
			System.out.println("falling false");
			if(dy < 0 && !jumping) dy += stopJumpSpeed;
			
			if(dy > maxFallSpeed) dy = maxFallSpeed;
			
		}
		
	}



	public void update() {
		
		// Actualizar Posicion
		getNextPosition();
		checkTileMapCollision();
		setPosition(xtemp, ytemp);
		
		// set animacion
		
		/*if(scratching) 
		{
			if(currentAction != SCRATCHING) 
			{
				currentAction = SCRATCHING;
				animation.setFrames(sprites.get(SCRATCHING));
				animation.setDelay(50);
				width = 16;
			}
		}
		else if(firing) {
			if(currentAction != FIREBALL) 
			{
				currentAction = FIREBALL;
				animation.setFrames(sprites.get(FIREBALL));
				animation.setDelay(100);
				width = 16;
			}
		}*/

		//FALLING ------------------------
		//ELSE IF ->
		if(dy > 0) 
		{
			/*if(gliding) {
				if(currentAction != GLIDING) {
					currentAction = GLIDING;
					animation.setFrames(sprites.get(GLIDING));
					animation.setDelay(100);
					width = 16;
				}
			}*/
			//ELSE IF ->
			if(currentAction != FALLING) {
				currentAction = FALLING;
				animation.setFrames(sprites.get(FALLING));
				animation.setDelay(100);
				width = 16;
			}
		}

		//JUMP -----------------
		else if(dy < 0) 
		{
			if(currentAction != JUMPING) 
			{
				currentAction = JUMPING;
				animation.setFrames(sprites.get(JUMPING));
				animation.setDelay(-1);
				width = 16;
			}
		}
		//MOVER IZQUIERDA O DERECHA (WALK ANIM)
		else if(left || right) 
		{
			if(currentAction != WALKING) 
			{
				currentAction = WALKING;
				animation.setFrames(sprites.get(WALKING));
				animation.setDelay(40);
				width = 16;
			}
		}
		// IDLE -------------------------------------
		else 
		{
			if(currentAction != IDLE) 
			{
				currentAction = IDLE;
				animation.setFrames(sprites.get(IDLE));
				animation.setDelay(400);
				width = 16;
			}
		}
		
		animation.update();
		
		// set direction
		//if(currentAction != SCRATCHING && currentAction != FIREBALL) {
		if(right) facingRight = true;
		if(left) facingRight = false;
		//}
		
	}




	public void draw(Graphics2D g) 
	{
		
		setMapPosition();
		
		// draw player
		if(flinching) //WHEN PLAYER GETS DAMAGE
		{
			long elapsed =
				(System.nanoTime() - flinchTimer) / 1000000;
			if(elapsed / 100 % 2 == 0) {
				return;
			}
		}
		
		super.draw(g);
		
	}



	
}