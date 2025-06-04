package Main;

import java.io.Serializable;
import java.util.List;

public class NetworkData {
    public static class GameStateData implements Serializable {
        public List<PlayerData> players;
        public List<EnemyData> enemies;
    }

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

    public static class EnemyData implements Serializable {
        public double x, y;
        public int health;
        public boolean dead;

        public EnemyData(double x, double y, int health, boolean dead) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.dead = dead;
        }
    }

    public static class PlayerInput implements Serializable {
        public boolean left, right, up, down, jumping;
    }
}