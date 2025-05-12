
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.IOException;

package TileMap;

public class Background {

    private BufferedImage image;
    private double x, y;
    private double dx, dy;
    private double moveScale;

    public Background(String s, double ms) {
        try {
            image = ImageIO.read(getClass().getResourceAsStream(s));
            moveScale = ms;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPosition(double x, double y) {
        this.x = (x * moveScale) % image.getWidth();
        this.y = (y * moveScale) % image.getHeight();
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
        g.drawImage(image, (int)x, (int)y, null);
        // Draw second image for seamless scrolling
        if(x < 0) {
            g.drawImage(image, (int)x + image.getWidth(), (int)y, null);
        }
        if(x > 0) {
            g.drawImage(image, (int)x - image.getWidth(), (int)y, null);
        }
    }
}

