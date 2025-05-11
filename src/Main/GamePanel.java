package Main;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class GamePanel extends JPanel implements Runnable, KeyListener
{

    //dimensiones

    public static final int WIDTH = 320;
    public static final int HEIGHT = 240;
    public static final int SCALE = 2;

    // thread

    private Thread thread;
    private boolean running;
    private int FPS = 60;
    private long milis = 1000/FPS;

    //imagen

    private BufferedImage imagea;
    private Graphics2D g;

    public GamePanel()
    {
        super();
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));        
        setFocusable(true);
        requestFocus();
    }

    public void addNotify() // inicia el thread cuando carga
    {
        super.addNotify();
        if(thread == null)
        {
            thread = new Thread(this);
            addKeyListener(this);
            thread.start();
        }
    }

    private void init()     // inicializacion 
    {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) g;

        running = true;
    }

    public void run()
    {
        init();

        long startTime;
        long passedTime;
        long waitTime;

        while(running)
        {
            startTime = System.nanoTime();
            update();
            draw();
            drawToScreen();

            passedTime = System.nanoTime() - startTime;
            waitTime = milis - passedTime / 1000000;
            
            try
            {
                Thread.sleep(waitTime);
            }
            catch(Exception e) 
            {
                e.printStackTrace();
            }

        }
    }

    private void update()
    {

    }

    private void draw()
    {
        
    }

    private void drawToScreen()
    {
        Graphics g2 = getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    public void keyTiped(keyEvent key)
    {

    }
    public void keyPressed(keyEvent key)
    {

    }
    public void keyReleased(keyEvent key)
    {

    }
}
