package GameState;

import TileMap.Background;
import Main.GameServer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;

public class MenuState extends GameState {
    private Background bg;
    private int currentChoice = 0;
    private String[] options = {"Jugar", "Cerrar"};
    private Color titleColor;
    private Font titleFont;
    private Font font;
    private GameServer server;
    private int connectedPlayers = 1;
    private long lastKeepAlive;
    private static final long KEEP_ALIVE_INTERVAL = 2000;
    private long bestTime = Long.MAX_VALUE; //Inicializar con maxvalue

    public MenuState(GameStateManager gsm) {
        this.gsm = gsm;
        try {
            bg = new Background("Resources/Backgrounds/background.png", 1);
            bg.setVector(-0.4, 0);
            titleColor = new Color(128, 0, 0);
            titleFont = new Font("Century Gothic", Font.PLAIN, 28);
            font = new Font("Arial", Font.PLAIN, 12);

            if (gsm.isHost()) {
                server = new GameServer(12345, this);
                new Thread(server::start).start();
            }
            // Cargar mejor tiempo
            loadBestTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        lastKeepAlive = System.currentTimeMillis();
    }

    public void clientConnected() {
        connectedPlayers++;
    }

    private void loadBestTime() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Resources/best_time.dat"))) {
            bestTime = ois.readLong();
        } catch (FileNotFoundException e) {
            System.out.println("No mejor tiempo encontrado...Inicializando");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        loadBestTime();
    }

    public void update() {
        bg.update();
        if (gsm.isHost() && server != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastKeepAlive >= KEEP_ALIVE_INTERVAL) {
                server.broadcastGameState();
                lastKeepAlive = currentTime;
            }
        }
    }

    public void draw(Graphics2D g) {
        bg.draw(g);
        g.setColor(titleColor);
        g.setFont(titleFont);
        g.drawString("Mario", 120, 70);
        g.setFont(font);
        for (int i = 0; i < options.length; i++) {
            if (i == currentChoice) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(Color.RED);
            }
            g.drawString(options[i], 145, 140 + i * 15);
        }
        g.setColor(Color.WHITE);
        g.drawString("Jugadores conectados: " + connectedPlayers, 20, 180);
        // Mostrar mejor tiempo
        String bestTimeStr = bestTime == Long.MAX_VALUE ? "Best Time: None" : "Best Time: " + bestTime + "s";
        g.drawString(bestTimeStr, 20, 200);
    }

    private void select() {
        if (currentChoice == 0) {
            if (gsm.isHost() && server != null) {
                server.notifyGameStart();
            }
            gsm.setState(GameStateManager.INLEVEL);
        }
        if (currentChoice == 1) {
            System.exit(0);
        }
    }

    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ENTER) {
            select();
        }
        if (k == KeyEvent.VK_UP) {
            currentChoice--;
            if (currentChoice == -1) {
                currentChoice = options.length - 1;
            }
        }
        if (k == KeyEvent.VK_DOWN) {
            currentChoice++;
            if (currentChoice == options.length) {
                currentChoice = 0;
            }
        }
    }

    public void keyReleased(int k) {}

    public GameServer getServer() {
        return server;
    }
}