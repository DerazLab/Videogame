package TileMap;

import java.awt.*;
import java.awt.image.*;

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

        
    }

    public void loadTiles(String s) 
    {
       try
       {
            tileset = ImageIO.read(getClass().getResourcesAsStream(s));
            numTilesAcross = tileset.getWidth() / tileSize;
            tiles = new Tile[2][numTilesAcross];

            BufferedImage subimage;
            for (int col = 0; col < numTilesAcross; col++)
            {
               subimage = tileset.getSubimage(col * tileSize, 0, tileSize, tile);
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
            InputStream in = getClass().getResourceAsStream(s);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
        
            //VIDEO 14:29

        }
        catch (Exception)
        {
            e.printStackTrace();
        }
    }




}