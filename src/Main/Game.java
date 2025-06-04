package Main;

import javax.swing.*;
import java.awt.*;
//javac -d bin src/GameState/*.java src/Main/*.java src/TileMap/*.java src/Entity/*.java
//java -cp bin;Resources Main.Game
public class Game {
    public static void main(String[] args) {
        int port = 12345; // Puerto fijo para simplicidad
        String hostAddress = "localhost"; // Por defecto para el host

        // Preguntar si el usuario quiere ser host o cliente
        String[] options = {"Host", "Client"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "Do you want to host or join a game?",
            "Multiplayer Setup",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]
        );

        boolean isHost = (choice == 0);
        if (!isHost) {
            hostAddress = JOptionPane.showInputDialog("Enter the host IP address:");
            if (hostAddress == null || hostAddress.trim().isEmpty()) {
                hostAddress = "localhost";
            }
        }

        JFrame window = new JFrame("Juegazo");
        GamePanel gamePanel = new GamePanel(isHost, hostAddress, port);
        window.setContentPane(gamePanel);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.pack();
        window.setVisible(true);
    }
}