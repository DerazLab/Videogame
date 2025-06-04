package Entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class HUD 
{
    private Player player;

    private BufferedImage world;
    private BufferedImage timer;
    private BufferedImage numbers;

    private Font font;

    public HUD(Player player) {
        this.player = player;

        try {
            world = ImageIO.read(getClass().getResourceAsStream("Resources/HUD/world.png"));
            timer = ImageIO.read(getClass().getResourceAsStream("Resources/HUD/timer.png"));
            numbers = ImageIO.read(getClass().getResourceAsStream("Resources/HUD/numbers.png"));

            font = new Font("Arial", Font.PLAIN, 24);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void draw(Graphics g) {
        // Draw the world image
        g.drawImage(world, 0, 0, null);
        
        // Draw the timer image
        g.drawImage(timer, 100, 100, null);
        
        // Draw the player's score
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString("Score: " + player.getScore(), 20, 50);
        
        // Draw the player's health
        g.drawString("Health: " + player.getHealth(), 20, 80);
    }
}