package org.greenlightgo.teacherattack;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.imageio.*;
import java.awt.event.*;


public class TileEditor extends JFrame implements MouseListener, MouseMotionListener, ActionListener{
	public static void main(String args[]) throws Exception{
		JFrame window = new TileEditor(TileMap.loadTiles());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.pack();
	}
	
	Tile[] tiles;
	TileSelector selector;
	TileMap map;
	MapRenderer mapRenderer;
	Point mouseDownPoint;
	
	public TileEditor(Tile[] tiles){
		this.tiles = tiles;
		
		setLayout(new BorderLayout());
		selector = new TileSelector(tiles);
		map = new TileMap(16, 64, 48);
		try{
			map.setBackgroundImage(ImageIO.read(new File("resources/background.png")));
		}catch(Exception exc){}
		mapRenderer = new MapRenderer(map, new Game());
		mapRenderer.addMouseListener(this);
		mapRenderer.addMouseMotionListener(this);

		add(selector, BorderLayout.EAST);
		add(mapRenderer, BorderLayout.CENTER);
		
		JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("File");
		m.add(createMenuItem("Open"));
		m.add(createMenuItem("Save"));
		
		mb.add(m);
		setJMenuBar(mb);
	}
	
	JFileChooser fileDialog = new JFileChooser();
	FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Tile Map", "map");
	public void actionPerformed(ActionEvent ae){
		fileDialog.setFileFilter(fileFilter);
		
		String cmd = ae.getActionCommand();
		if(cmd.equals("Open")){
			int returnVal = fileDialog.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("Opening: " + fileDialog.getSelectedFile());
				map = TileMap.load(fileDialog.getSelectedFile(), tiles);
				mapRenderer.tileMap = map;
			}
		}else if(cmd.equals("Save")){
			int returnVal = fileDialog.showSaveDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("Saving: " + fileDialog.getSelectedFile());
				map.save(fileDialog.getSelectedFile(), tiles);
			}
		}
	}
	
	private JMenuItem createMenuItem(String name){
		JMenuItem item = new JMenuItem(name);
		item.addActionListener(this);
		return item;
	}
	
	public void load(TileMap map){}
	
	public void mouseClicked(MouseEvent e){
		map.setTile(
			(e.getX() - mapRenderer.offset[0]) / map.tileSize,
			(e.getY() - mapRenderer.offset[1]) / map.tileSize,
			selector.selectedButton.tile
		);
		mapRenderer.repaint();
	}
	
	public void mouseMoved(MouseEvent e){
		mapRenderer.highlightSquareUnder(e.getPoint());
	}

	public void mouseDragged(MouseEvent e){
		mapRenderer.adjustOffset(
			e.getX() - mouseDownPoint.x,
			e.getY() - mouseDownPoint.y
		);
		mousePressed(e);
	}
	public void mousePressed(MouseEvent e){
		mouseDownPoint = e.getPoint();
	}
	
	public void mouseExited(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}

}

class TileSelector extends JPanel {
	TileButton selectedButton;
	public TileSelector(Tile[] tiles){
		setLayout(new GridLayout((int)Math.ceil(tiles.length/2.0d), 2));
		
		for(Tile tile : tiles){
			TileButton b = new TileButton(tile);
			add(b);
		}
	}
	
	public void setButton(TileButton button){
		this.selectedButton = button;
	}
	
	class TileButton extends JButton{
		Tile tile;
		TileButton(Tile t){
			super(new ImageIcon(t.image));
			tile = t;
			
			addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						TileSelector.this.setButton(TileButton.this);
					}
				}
			);
		}
	}
}

