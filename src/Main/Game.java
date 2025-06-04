package Main;
import javax.swing.JFrame;
//javac -d bin src/GameState/*.java src/Main/*.java src/TileMap/*.java src/Entity/*.java src/Entity/Enemies/*.java
//java -cp bin;Resources Main.Game

public class Game{
    public static void main(String[] args){
        JFrame window = new JFrame("juegazo");
        window.setContentPane(new GamePanel());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.pack();
        window.setVisible(true);
    }

}
