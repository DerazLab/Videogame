package Main;

import java.io.Serializable;
import java.util.List;

public class NetworkData {
    // Clase para enviar datos del estado del juego
    public static class GameStateData implements Serializable {
        public List<PlayerData> players;
    }

    // Clase para datos del jugador
    public static class PlayerData implements Serializable {
        public double x, y;
        public int health, score;
        public boolean facingRight;

        public PlayerData(double x, double y, int health, int score, boolean facingRight) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.score = score;
            this.facingRight = facingRight;
        }
    }

    // Clase para recibir comandos de entrada del cliente
    public static class PlayerInput implements Serializable {
        public boolean left, right, up, down, jumping;
    }
}