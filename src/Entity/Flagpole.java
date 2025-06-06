package Entity;

import TileMap.TileMap;
import java.awt.Rectangle;

public class Flagpole extends MapObject {
    private boolean active;

    public Flagpole(TileMap tm, double x, double y) {
        super(tm);
        this.x = x;
        this.y = y;
        width = 16;
        height = 48; 
        cwidth = 8;
        cheight = 48;
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void draw(java.awt.Graphics2D g) {
        // Drawing handled by TileMap, so no need to implement
    }

    public Rectangle getRectangle() {
        return new Rectangle(
            (int)(x - cwidth / 2),
            (int)(y - cheight / 2),
            cwidth,
            cheight
        );
    }
}