package Main;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class NetworkData {
    public static class GameStateData implements Serializable {
        public List<PlayerData> players;
        public List<EnemyData> enemies;
        public long levelStartTime;
        public boolean timerStopped;
        public long levelEndTime;
        public long elapsedTime; 

        public GameStateData() {
            this.levelStartTime = System.nanoTime();
            this.timerStopped = false;
            this.levelEndTime = 0;
            this.elapsedTime = 0;
        }
    }

    public static class PlayerData implements Serializable {
        public double x, y;
        public int health, score;
        public boolean facingRight;
        public boolean dead;
        public boolean holdingFlag;
        public boolean awaitingRespawn;
        public long respawnTimer;
        public boolean deathAnimationComplete;
        public boolean descendingFlag;
        public boolean descentComplete;

        public PlayerData(double x, double y, int health, int score, boolean facingRight, boolean dead, boolean holdingFlag, boolean awaitingRespawn, long respawnTimer, boolean deathAnimationComplete, boolean descendingFlag, boolean descentComplete) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.score = score;
            this.facingRight = facingRight;
            this.dead = dead;
            this.holdingFlag = holdingFlag;
            this.awaitingRespawn = awaitingRespawn;
            this.respawnTimer = respawnTimer;
            this.deathAnimationComplete = deathAnimationComplete;
            this.descendingFlag = descendingFlag;
            this.descentComplete = descentComplete;
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

    public static class StateChange implements Serializable {
        public int newState;

        public StateChange(int newState) {
            this.newState = newState;
        }
    }
}