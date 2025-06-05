package Main;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class NetworkData {
    public static class GameStateData implements Serializable {
        public List<PlayerData> players;
        public List<EnemyData> enemies;
    }

    public static class PlayerData implements Serializable {
        public double x, y;
        public int health, score;
        public boolean facingRight;
        public boolean dead;

        public PlayerData(double x, double y, int health, int score, boolean facingRight, boolean dead) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.score = score;
            this.facingRight = facingRight;
            this.dead = dead;
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

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PlayerInput other = (PlayerInput) obj;
            return left == other.left &&
                   right == other.right &&
                   up == other.up &&
                   down == other.down &&
                   jumping == other.jumping;
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, right, up, down, jumping);
        }
    }
}