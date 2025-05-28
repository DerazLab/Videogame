package Main;

import GameState.GameStateManager;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

//javac -d bin -sourcepath src src/Main/*.java
//java -cp bin Main.Game

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

    // imagen

    private BufferedImage image;
    private Graphics2D g;

    //  estado del juego

    private GameStateManager gsm;

    public GamePanel()
    {
        super();
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));        
        setFocusable(true);
        requestFocus();
    }

    public void addNotify() // inicia el thread cuando carga.
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
        g = (Graphics2D) image.getGraphics();

        running = true;

        gsm = new GameStateManager();
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
            if(waitTime < 0)
            {
                waitTime = 5; // si el tiempo de espera es negativo, espera un poco para no saturar la CPU
            }
            
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
        gsm.update();
    }

    private void draw()
    {
        gsm.draw(g);
    }

    private void drawToScreen()
    {
        Graphics g2 = getGraphics();
        g2.drawImage(image, 0, 0, getWidth(), getHeight(), null); // <- Escala al tamaño actual del panel
        g2.dispose();
    }

    public void keyTyped(KeyEvent key)
    {

    }
    public void keyPressed(KeyEvent key)
    {
        gsm.keyPressed(key.getKeyCode());
    }
    public void keyReleased(KeyEvent key)
    {
        gsm.keyReleased(key.getKeyCode());
    }
}
