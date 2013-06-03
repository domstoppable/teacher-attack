package org.greenlightgo.teacherattack;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.imageio.*;
import java.awt.event.*;

public class TileMap {
	Tile[][] tiles;
	Image bg = null;
	int tileSize;
	
	public TileMap(int tileSize, int width, int height){
		this.tileSize = tileSize;
		tiles = new Tile[width][height];
	}
	
	public void setBackgroundImage(Image bg){
		this.bg = bg;
	}
	
	public void setTile(int x, int y, Tile tile){
		this.tiles[x][y] = tile;
	}
	
	public void render(Graphics2D g, int[] offset, Dimension size){
		g.translate(offset[0], offset[1]);
		if(bg != null) g.drawImage(bg, 0, 0, null);

		for(int x=0; x<tiles.length; x++){
			for(int y=0; y<tiles[0].length; y++){
				if(tiles[x][y] != null){
					g.drawImage(tiles[x][y].image, x*tileSize, y*tileSize, null);
				}
			}
		}
	}
	
	public boolean checkPosition(GameObject object){
		Rectangle rect = object.getRectangle();
		
		int xStart = rect.x / tileSize;
		int xStop = (rect.x + rect.width) / tileSize;
		int yStart = rect.y / tileSize;
		int yStop = (rect.y + rect.height) / tileSize;

		boolean collide = false;
		
		if(tiles[xStart][yStart] != null && tiles[xStart][yStart].onCollide(object)) collide = true;
		if(tiles[xStop][yStart] != null && tiles[xStop][yStart].onCollide(object)) collide = true;
		if(tiles[xStart][yStop] != null && tiles[xStart][yStop].onCollide(object)) collide = true;
		if(tiles[xStop][yStop] != null && tiles[xStop][yStop].onCollide(object)) collide = true;

		return !collide;
	}
	
	public void save(File file, Tile[] tiles){
		try{
			PrintWriter writer = new PrintWriter(file);
			for(int i=0; i<tiles.length; i++){
				tiles[i].id = i;
				writer.println(tiles[i]);
			}
			writer.println("");
			writer.println(this.tileSize + "\t" + this.tiles.length + "\t" + this.tiles[0].length);
			for(int y=0; y<this.tiles[0].length; y++){
				for(int x=0; x<this.tiles.length; x++){
					if(this.tiles[x][y] != null){
						writer.print(this.tiles[x][y].id + " ");
					}else{
						writer.print("-1 ");
					}
				}
				writer.println("");
			}
			
			writer.close();
			
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}

	public static TileMap load(String mapName, Tile[] tiles){
		return load(new InputStreamReader(Game.class.getClassLoader().getResourceAsStream(mapName)), tiles);
	}

	public static TileMap load(File file, Tile[] tiles) throws Exception{
		return load(new FileReader(file), tiles);
	}
	
	public static TileMap load(Reader input, Tile[] tiles){
		try{
			BufferedReader reader = new BufferedReader(input);
			String line;
			ArrayList<Tile> tileLookup = new ArrayList<Tile>(tiles.length);
			do{
				line = reader.readLine();
				if(line.equals("")) break;
				
				boolean found = false;
				for(Tile t : tiles){
					if(t.toString().equals(line)){
						tileLookup.add(t);
						found = true;
						break;
					}
				}
				if(!found){
					tileLookup.add(null);
				}
			}while(!line.equals(""));
			
			// now get tile size and map size
			line = reader.readLine();
			String[] items = line.split("\t");
			TileMap map = new TileMap(
				Integer.parseInt(items[0]),
				Integer.parseInt(items[1]),
				Integer.parseInt(items[2])
			);
			//map.setBackgroundImage(ImageIO.read(new File("resources/background.png")));
			map.setBackgroundImage(ImageIO.read(Game.class.getClassLoader().getResource("resources/background.png")));
			
			// now fill in the data
			int y = 0;
			while(reader.ready()){
				int x = 0;
				line = reader.readLine();
				String[] tileIDs = line.split(" ");
				for(String tileID : tileIDs){
					int id = Integer.parseInt(tileID);
					if(id != -1){
						map.tiles[x][y] = tileLookup.get(id);
					}
					x++;
				}
				y++;
			}
			
			return map;
			
		}catch(Exception exc){
			System.err.format("Exception: %s%n", exc);
			throw new RuntimeException(exc);
		}
	}

	public static Tile[] loadTiles(){
		String[] images = new String[]{"hole", "rock", "tree1", "tree3", "tree", "plant", "sign", "tree2", "tree4"};
		Tile[] tiles = new Tile[images.length];
		for(int i=0; i<images.length; i++){
			tiles[i] = new Tile("tile-" + images[i] + ".png");
		}
		
		return tiles;
	}
}

class Tile{
	public Image image;
	public int id;
	
	String name;
	
	public Tile(File f){
		this.name = f.getName();
		try{
			image = ImageIO.read(f);
		}catch(Exception exc){
			throw new RuntimeException(exc);
		}
	}
	
	public Tile(String name){
		this.name = name;
		try{
			image = ImageIO.read(Game.class.getClassLoader().getResource("resources/" + name));
		}catch(Exception exc){
			throw new RuntimeException(exc);
		}
	}
	
	public String toString() {
		return name;
	}
	
	public boolean onCollide(GameObject object){
		if(name.equals("tile-hole.png")){
			//System.err.println("hole");
		}
		return true;
	}
}
