package Entity;

import TileMap.TileMap;

public class Enemy extends MapObject
{
    protected int maxHealth;
    protected int health;
    protected boolean dead;
    protected int damage;
    protected boolean flinching;
    protected long flinchTimer;

    public Enemy(TileMap tm)
    {
        super(tm);
    }

    public boolean isDead() { return dead; }

    public int getDamage() { return damage; }

    public int getHealth() { return health; }

    public void setHealth(int health) 
    {
        this.health = health;
        if (health <= 0) 
        {
            dead = true;
        }
    }

    public void hit(int damage)
    {
        if(dead || flinching) return;
        health -= damage;
        if(health < 0) health = 0;
        if(health == 0) {
            dead = true;
        } else {
            flinching = true;
            flinchTimer = System.nanoTime();
        }
    }

    public void update() {
    if (y > tileMap.getHeight()) {
        setHealth(0);
    }
}
}