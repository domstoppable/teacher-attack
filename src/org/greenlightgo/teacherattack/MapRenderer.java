package org.greenlightgo.teacherattack;

import java.util.*;
import java.awt.*;
import javax.swing.*;

public class MapRenderer extends JPanel{
	Game game;
	TileMap tileMap;
	int[] offset = {0, 0};
	Point highlight = new Point(-10, -10);
	
	public MapRenderer(TileMap map, Game game){
		setPreferredSize(new Dimension(800, 600));
		tileMap = map;
		this.game = game;
	}
	
	public void centerOn(int x, int y){
		int w = getWidth();
		int h = getHeight();
		
		if(w > tileMap.tileSize * tileMap.tiles.length){
			offset[0] = (w - tileMap.tileSize * tileMap.tiles.length) / 2;
		}else{
			offset[0] = (w/2-x);
			offset[0] = Math.min(tileMap.tileSize, offset[0]);		
			offset[0] = Math.max(w -(tileMap.tiles.length+1) * tileMap.tileSize, offset[0]);
		}
		if(h > tileMap.tileSize * tileMap.tiles[0].length){
			offset[1] = (h - tileMap.tileSize * tileMap.tiles[0].length) / 2;
		}else{
			offset[1] = h/2-y;
			offset[1] = Math.min(tileMap.tileSize, offset[1]);
			offset[1] = Math.max(h -(tileMap.tiles[0].length+1) * tileMap.tileSize, offset[1]);
		}
		
	}
	
	public void adjustOffset(int x, int y){
		offset[0] += x;
		offset[1] += y;
		repaint();
	}
	
	public void highlightSquareUnder(Point p){
		highlight.x = p.x - offset[0];
		highlight.y = p.y - offset[1];
		repaint();
	}
	
	public void paint(Graphics g){
		g.fillRect(0, 0, getWidth(), getHeight());
		
		tileMap.render((Graphics2D)g, offset, getSize());
		if(highlight != null){
			int size = tileMap.tileSize;
			g.drawRect(
				(highlight.x) - (highlight.x % size),
				(highlight.y) - (highlight.y % size),
				size,
				size
			);
		}
		
		synchronized(this){
			for(GameObject object : game.objects.values()){
				object.render((Graphics2D)g);
			}
		}
	}
}

