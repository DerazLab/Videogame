package GameState;

import TileMap.Background;
import Main.GameServer;
import java.awt.*;
import java.awt.event.*;

public class MenuState extends GameState {
    private Background bg;
    private int currentChoice = 0;
    private String[] options = {"Start", "Quit"};
    private Color titleColor;
    private Font titleFont;
    private Font font;
    private GameServer server; // Para el servidor del host
    private int connectedPlayers = 1; // Incluye al host

    public MenuState(GameStateManager gsm) {
        this.gsm = gsm;
        try {
            bg = new Background("Resources/Backgrounds/background.png", 1);
            bg.setVector(-0.4, 0);
            titleColor = new Color(128, 0 , 0);
            titleFont = new Font("Century Gothic", Font.PLAIN, 28);
            font = new Font("Arial", Font.PLAIN, 12);

            // Iniciar servidor si es host
            if (gsm.isHost()) {
                server = new GameServer(12345, this);
                new Thread(server::start).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clientConnected() {
        connectedPlayers++;
    }

    public void init() {}

    public void update() {
        bg.update();
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
        // Mostrar número de jugadores conectados
        g.setColor(Color.WHITE);
        g.drawString("Players Connected: " + connectedPlayers, 20, 180);
    }

    private void select() {
        if (currentChoice == 0) {
            // Notificar a todos los clientes que el juego comienza
            if (gsm.isHost() && server != null) {
                server.notifyGameStart();
            }
            gsm.setState(GameStateManager.INLEVEL);
            // Agregar segundo jugador si es cliente
            if (!gsm.isHost() && gsm.getClient() != null && gsm.getClient().isConnected()) {
                ((Level1State) gsm.getGameStates().get(GameStateManager.INLEVEL)).addPlayer();
            }
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

    // Añadir método getter para el servidor
    public GameServer getServer() {
        return server;
    }
}