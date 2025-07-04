package Main;

import javax.swing.*;
import java.awt.*;
//javac -d bin src/GameState/*.java src/Main/*.java src/TileMap/*.java src/Entity/*.java src/Entity/Enemies/*.java
//java -cp bin;Resources Main.Game

// Dear programmer:
// When I wrote this code, only god and I knew how it worked.
// Now, only god knows it!

// Therefore, if you are trying to optimize this routine and it fails (most surely),
// please increase this counter as a warning for the next person:

// total_gitHub_commits and hours wasted = 201 & 100.5 h

public class Game {
    public static void main(String[] args) {
        int port = 12345; 
        String hostAddress = "localhost";

        String[] options = {"Host", "Client"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "¿Quieres ser el host o un cliente?",
            "Setup Multijugador", 
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]
        );

        boolean isHost = (choice == 0);
        if (!isHost) {
            hostAddress = JOptionPane.showInputDialog("Ingresa la IP:");
            if (hostAddress == null || hostAddress.trim().isEmpty()) {
                hostAddress = "localhost";
            }
        }

        JFrame window = new JFrame("Mario Bros Multiplayer");
        GamePanel gamePanel = new GamePanel(isHost, hostAddress, port);
        window.setContentPane(gamePanel);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.pack();
        window.setVisible(true);
    }
}