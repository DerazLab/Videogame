package GameState;

import TileMap.Background;
import Main.GamePanel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;

public class WinState extends GameState {
    private Background bg;
    private Font titleFont;
    private Font font;
    private long completionTime;

    public WinState(GameStateManager gsm) {
        this.gsm = gsm;
        try {
            bg = new Background("Resources/Backgrounds/SkyBackground.png", 1);
            bg.setVector(-0.4, 0);
            titleFont = new Font("Century Gothic", Font.PLAIN, 28);
            font = new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        // Get completion time from Level1State
        Level1State level = (Level1State) gsm.getGameStates().get(GameStateManager.INLEVEL);
        completionTime = level.getCurrentTime();
        // Update best time if necessary
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Resources/best_time.dat"))) {
            long bestTime = ois.readLong();
            if (completionTime < bestTime) {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Resources/best_time.dat"))) {
                    oos.writeLong(completionTime);
                }
            }
        } catch (FileNotFoundException e) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Resources/best_time.dat"))) {
                oos.writeLong(completionTime);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        bg.update();
    }

    public void draw(Graphics2D g) {
        bg.draw(g);
        g.setColor(Color.GREEN);
        g.setFont(titleFont);
        g.drawString("You Win!", 100, 100);
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString("Time: " + completionTime + "s", 100, 120);
        g.drawString("Press ENTER to return to menu", 100, 140);
    }

    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ENTER) {
            gsm.setState(GameStateManager.INMENU);
            if (gsm.isHost() && gsm.getServer() != null) {
                gsm.getServer().notifyStateChange(GameStateManager.INMENU);
            }
        }
    }

    public void keyReleased(int k) {}
}