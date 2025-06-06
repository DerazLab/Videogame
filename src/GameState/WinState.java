package GameState;

import TileMap.Background;
import Main.GamePanel;
import java.awt.*;
import java.awt.event.KeyEvent;

public class WinState extends GameState {
    private Background bg;
    private Font titleFont;
    private Font font;

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

    public void init() {}

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