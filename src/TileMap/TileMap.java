package TileMap;

import java.awt.*;
import java.awt.image.*;
import Main.GamePanel;

import java.io.*;
import javax.imageio.ImageIO;

public class TileMap
{

    //Position
    private double x;
    private double y;

    //bounds
    private int xmin;
    private int ymin;
    private int xmax;
    private int ymax;

    //tween is for smootly scroll
    //camera towards player
    private double tween;

    // map
    private int[][] map;
    private int tileSize;
    private int numRows;
    private int numCols;
    private int width;
    private int height;

    //tileSet
    private BufferedImage tileset;
    private int numTilesAcross;
    private Tile[][] tiles;

    //drawing
    private int rowOffset;
    private int colOffset;
    private int numRowsToDraw;
    private int numColsToDraw;

    public TileMap(int tileSize)
    {
        this.tileSize = tileSize;
        numRowsToDraw = GamePanel.HEIGHT / tileSize + 2;
        numColsToDraw = GamePanel.WIDTH / tileSize + 2;
        tween = 0.07;
		
		xmin = 0;
		ymin = 0;
		xmax = 0;
		ymax = 0;

        
    }

    public void loadTiles(String s) 
    {
       try
       {
            tileset = ImageIO.read(new File(s));
            numTilesAcross = tileset.getWidth() / tileSize;
            tiles = new Tile[2][numTilesAcross];

            BufferedImage subimage;
            for (int col = 0; col < numTilesAcross; col++)
            {
               subimage = tileset.getSubimage(col * tileSize, 0, tileSize, tileSize);
               tiles[0][col] = new Tile(subimage, Tile.NORMAL);
               subimage = tileset.getSubimage(col * tileSize, tileSize, tileSize, tileSize);
               tiles[1][col] = new Tile(subimage, Tile.BLOCKED);
            
            
            }
       } 
       catch (Exception e)
       {
            e.printStackTrace();
       }
    }

   public void loadMap(String s) 
{
    try
    {
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
        // Set bounds to allow full map visibility
        xmin = GamePanel.WIDTH  - width;
        ymin = GamePanel.HEIGHT - height;
        xmax = 0;
        ymax = 0t;

        String delims = "\\s+";
        for (int row = 0; row < numRows; row++)
        {
            String line = br.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of file in " + s);
            }
            String[] tokens = line.split(delims);
            for (int col = 0; col < numCols; col++)
            {
                map[row][col] = Integer.parseInt(tokens[col]);
            }
        }
        br.close();
    }
    catch (Exception e)
    {
        e.printStackTrace();
    }
}

    public int getTileSize() 
    {
        return tileSize;
    }
    public int getX() 
    {
        return (int)x;
    }
    public int getY() 
    {
        return (int)y;
    }
    public int getWidth() 
    {
        return width;
    }
    public int getHeight() 
    {
        return height;
    }
    public int getType(int row, int col) 
    {
        int rc = map[row][col];
        int r = rc / numTilesAcross;
        int c = rc % numTilesAcross;
        return tiles[r][c].getType();
    }

    public void setPosition(double x, double y) 
    {
        this.x += (x - this.x) * tween;
        this.y += (y - this.y) * tween;

        fixBounds();

        colOffset = (int)-this.x / tileSize;
        rowOffset = (int)-this.y / tileSize;
    }

    public void fixBounds()
    {
        // make sure position is within bounds
        if (this.x < xmin) this.x = xmin;
        if (this.y < ymin) this.y = ymin;
        if (this.x > xmax - GamePanel.WIDTH) this.x = xmax - GamePanel.WIDTH;
        if (this.y > ymax - GamePanel.HEIGHT) this.y = ymax - GamePanel.HEIGHT;
    }

    public void draw(Graphics2D g) 
    {
        for (int row = rowOffset; row < rowOffset + numRowsToDraw; row++)
        {
            if (row >= numRows) break;
            if (row < 0) continue;

            for (int col = colOffset; col < colOffset + numColsToDraw; col++)
            {
                if (col >= numCols) break;

                int rc = map[row][col];

                int r = rc / numTilesAcross;
                int c = rc % numTilesAcross;

                g.drawImage(tiles[r][c].getImage(), (int)x + col * tileSize, (int)y + row * tileSize, null);
            }
        }
    }


}