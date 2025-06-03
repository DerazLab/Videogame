package TileMap;

import Main.GamePanel;

import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;

public class Background {
	
	private BufferedImage image;
	
	private double x;
	private double y;
	private double dx;
	private double dy;
	
	private double moveScale;
	
	public Background(String s, double ms) {
		
		try {
			image = ImageIO.read(new File(s));
			moveScale = ms;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void setPosition(double x, double y) {
		this.x = (x * moveScale) % image.getWidth();
		this.y = (y * moveScale) % image.getHeight();
		/*this.x = (x * moveScale) % 3548;
		this.y = (y * moveScale) % GamePanel.HEIGHT;*/
	}
	
	public void setVector(double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	public void update() {
		x += dx;
		y += dy;
	}
	
	public void draw(Graphics2D g) {

        if(g == null){
            System.out.println("Graphics2D is null");
        }
		
		g.drawImage(image, (int)x, (int)y, null);
		
		if(x < 0) {
			g.drawImage(image, (int)x + 3584, (int)y, null);
		}
		if(x > 0) {
			g.drawImage(
				image,
				(int)x - 3584,
				(int)y,
				null
			);
		}
	}
	
}







