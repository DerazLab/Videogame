package TileMap;

import java.awt.*;
import java.awt.image.*;
import Main.GamePanel;
import java.io.*;
import javax.imageio.ImageIO;

public class TileMap {
    // Position
    private double x;
    private double y;

    // Bounds
    private int xmin;
    private int ymin;
    private int xmax;
    private int ymax;

    // Tween is for smooth scroll
    private double tween;

    // Map
    private int[][] map;
    private int tileSize;
    private int numRows;
    private int numCols;
    private int width;
    private int height;

    // TileSet
    private BufferedImage tileset;
    private int numTilesAcross;
    private Tile[][] tiles;

    // Drawing
    private int rowOffset;
    private int colOffset;
    private int numRowsToDraw;
    private int numColsToDraw;

    public TileMap(int tileSize) {
        this.tileSize = tileSize;
        numRowsToDraw = GamePanel.HEIGHT / tileSize + 2;
        numColsToDraw = GamePanel.WIDTH / tileSize + 2;
        tween = 0.07;
        xmin = 0;
        ymin = 0;
        xmax = 0;
        ymax = 0;
    }

    public void loadTiles(String s) {
        try {
            tileset = ImageIO.read(new File(s));
            numTilesAcross = tileset.getWidth() / tileSize;
            tiles = new Tile[3][numTilesAcross]; // Updated to include FLAGPOLE type

            BufferedImage subimage;
            for (int col = 0; col < numTilesAcross; col++) {
                // Normal tiles
                subimage = tileset.getSubimage(col * tileSize, 0, tileSize, tileSize);
                tiles[0][col] = new Tile(subimage, Tile.NORMAL);
                // Blocked tiles
                subimage = tileset.getSubimage(col * tileSize, tileSize, tileSize, tileSize);
                tiles[1][col] = new Tile(subimage, Tile.BLOCKED);
                // Flagpole tiles (assuming third row in tileset)
                if (col == 10 || col == 12 || col == 13) {
                    subimage = tileset.getSubimage(col * tileSize, 0, tileSize, tileSize);
                    tiles[2][col] = new Tile(subimage, Tile.FLAGPOLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String s) {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(s);
            if (in == null) {
                throw new IOException("Resource not found: " + s);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            numCols = Integer.parseInt(br.readLine());
            numRows = Integer.parseInt(br.readLine());
            map = new int[numRows][numCols];
            width = numCols * tileSize;
            height = numRows * tileSize;

            xmin = GamePanel.WIDTH - width;
            ymin = GamePanel.HEIGHT - height;
            xmax = 304;
            ymax = 600;

            String delims = "\\s+";
            for (int row = 0; row < numRows; row++) {
                String line = br.readLine();
                if (line == null) {
                    throw new IOException("Fin inesperado al leer el archivo: " + s);
                }
                String[] tokens = line.split(delims);
                for (int col = 0; col < numCols; col++) {
                    map[row][col] = Integer.parseInt(tokens[col]);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getX() {
        return (int)x;
    }

    public int getY() {
        return (int)y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getType(int row, int col) {
    if (row < 0 || row >= numRows || col < 0 || col >= numCols) {
        return Tile.NORMAL; // Trata las posiciones fuera de los limites del mapa como bloques sin colision
    }
	//System.out.println("Row: " + row + "MaxRows: " + numRows);
	//System.out.println("Col: " + col + "MaxCols: " + numCols);
	
    int rc = map[row][col];
    int r = rc / numTilesAcross;
    int c = rc % numTilesAcross;
    if (c == 10 || c == 12 || c == 13) {
        return Tile.FLAGPOLE;
    }
    return tiles[r][c].getType();
}

    public void setPosition(double x, double y) {
        this.x += (x - this.x) * tween;
        this.y += (y - this.y) * tween;

        fixBounds();

        colOffset = (int)-this.x / tileSize;
        rowOffset = (int)-this.y / tileSize;
    }

    public void fixBounds() {
        if (this.x < xmin) this.x = xmin;
        if (this.y < ymin) this.y = ymin;
        if (this.x > xmax - GamePanel.WIDTH) this.x = xmax - GamePanel.WIDTH;
        if (this.y > ymax - GamePanel.HEIGHT) this.y = ymax - GamePanel.HEIGHT;
    }

    public void draw(Graphics2D g) {
        for (int row = rowOffset; row < rowOffset + numRowsToDraw; row++) {
            if (row >= numRows) break;
            if (row < 0) continue;

            for (int col = colOffset; col < colOffset + numColsToDraw; col++) {
                if (col >= numCols) break;

                int rc = map[row][col];
                int r = rc / numTilesAcross;
                int c = rc % numTilesAcross;

                g.drawImage(tiles[r][c].getImage(), (int)x + col * tileSize, (int)y + row * tileSize, null);
            }
        }
    }
}