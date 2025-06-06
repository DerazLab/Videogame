package Main;

import GameState.GameStateManager;
import GameState.Level1State;

import Main.NetworkData;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 240;
    public static final int SCALE = 2;

    private Thread thread;
    private boolean running;
    private int FPS = 30;
    private long milis = 1000 / FPS;

    private BufferedImage image;
    private Graphics2D g;
    private GameStateManager gsm;
    private GameClient client;

    public GamePanel(boolean isHost, String hostAddress, int port) {
        super();
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setFocusable(true);
        requestFocus();

        if (isHost) {
            gsm = new GameStateManager(true, port);
        } else {
            client = new GameClient(hostAddress, port, this);
            gsm = new GameStateManager(false, port);
            gsm.setClient(client);
        }
    }

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            addKeyListener(this);
            thread.start();
        }
    }

    private void init() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();
        running = true;
    }

    public void run() {
    init();

    long targetTime = 1000000000 / FPS; // Target time per frame in nanoseconds
    long lastTime = System.nanoTime();

    while (running) {
        long currentTime = System.nanoTime();
        long elapsed = currentTime - lastTime;

        if (elapsed >= targetTime) {
            update();
            draw();
            drawToScreen();
            lastTime += targetTime;
            // Sleep for a small amount to prevent busy-waiting
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // Sleep to avoid consuming too much CPU
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

    private void update() {
    gsm.update();
    if (gsm.getGameStates().get(GameStateManager.INLEVEL) instanceof Level1State && !gsm.isHost()) {
        ((Level1State) gsm.getGameStates().get(GameStateManager.INLEVEL)).updateClientInput();
    }
}

    private void draw() {
        gsm.draw(g);
    }

    private void drawToScreen() {
        Graphics g2 = getGraphics();
        g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g2.dispose();
    }

    public void updateGameState(NetworkData.GameStateData state) {
        gsm.updateGameState(state);
    }

    public void keyTyped(KeyEvent key) {}

    public void keyPressed(KeyEvent key) {
        gsm.keyPressed(key.getKeyCode());
    }

    public void keyReleased(KeyEvent key) {
        gsm.keyReleased(key.getKeyCode());
    }

    public GameClient getClient() {
        return client;
    }

    public GameStateManager getGameStateManager() {
        return gsm;
    }
}