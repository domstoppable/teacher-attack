package org.greenlightgo.teacherattack;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.imageio.*;
import java.awt.event.*;


public class PlayerApp extends JFrame implements MouseListener, MouseMotionListener{
	public static void main(String args[]) throws Exception{
		JFrame window = new CharacterSelect();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.pack();
	}
	
	TileMap map;
	MapRenderer mapRenderer;
	
	public PlayerApp(){}
	
	public void mouseClicked(MouseEvent e){}
	
	public void mouseMoved(MouseEvent e){
		mapRenderer.highlightSquareUnder(e.getPoint());
	}
	public void mousePressed(MouseEvent e){
	}

	public void mouseDragged(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
}

class CharacterSelect extends JFrame implements ActionListener{
	JTextField serverBox = new JTextField("localhost");
	JTextField portBox = new JTextField("1234");
	JTextField nameBox = new JTextField("dom");
	public CharacterSelect() throws Exception{
		JPanel container = new JPanel();
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		container.add(Box.createVerticalStrut(5));
		container.add(new JLabel("Server"));
		container.add(serverBox);
		container.add(new JLabel("Port"));
		container.add(portBox);
		container.add(Box.createVerticalStrut(20));
		container.add(new JLabel("Name"));
		container.add(nameBox);
		container.add(Box.createVerticalStrut(15));
		container.add(new JLabel("Character Type"));
		
		String[] characters = { "warrior", "wizard", "healer" };
		for(String character : characters){
			container.add(createCharacterButton(character));
		}
		
		add(container);
	}
	
	public void actionPerformed(ActionEvent ae){
		if(serverBox.getText().trim().equals("")){
			JOptionPane.showMessageDialog(this, "Please enter the server :)");
			return;
		}
		if(portBox.getText().trim().equals("")){
			JOptionPane.showMessageDialog(this, "Please enter the port :)");
			return;
		}
		if(nameBox.getText().trim().equals("")){
			JOptionPane.showMessageDialog(this, "Please enter your name :)");
			return;
		}
		String cmd = ae.getActionCommand();
		PlayableCharacter player;
		try{
			player = new PlayableCharacter(nameBox.getText().trim(), cmd);
			
			if(cmd.equals("warrior")){
				player.speed = 2;
			}
		}catch(Exception exc){
			throw new RuntimeException(exc);
		}

		if(cmd.equals("warrior")){
		}else if(cmd.equals("wizard")){
		}else if(cmd.equals("healer")){
		}
		try{
			new GameWindow(player, serverBox.getText(), Integer.parseInt(portBox.getText()));
		}catch(Exception exc){
			throw new RuntimeException(exc);
		}
	}
	
	private JButton createCharacterButton(String character) throws Exception{
		int cropSize = 48;
		BufferedImage image = ImageIO.read(new File("resources/" + character + ".png"));
		BufferedImage cropped = new BufferedImage(cropSize, cropSize, image.getType());
		cropped.getGraphics().drawImage(image, 0, 0, cropSize, cropSize, 0, 0, 16, 16, null);
		JButton b = new JButton(
			character,
			new ImageIcon(cropped)
		);
		b.addActionListener(this);
		return b;
	}
}

class GameWindow extends JFrame implements KeyListener{
	Game game;
	TileMap map;
	MapRenderer mapRenderer;
	PlayableCharacter player;
	GameClient client;

	boolean[] keyStates = new boolean[255];
	
	public GameWindow(PlayableCharacter player, String server, int port) throws Exception{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.player = player;
		
		map = TileMap.load(new File("resources/basic.map"), TileMap.loadTiles());
		game = new Game();
		mapRenderer = new MapRenderer(map, game);
		client = new GameClient(
			server, port,
			player,
			game
		);
		
		game.addObject(client.clientID, player);
		add(mapRenderer);
		
		addKeyListener(this);
		
		setVisible(true);
		pack();
		
		Thread t = new Thread(
			new Runnable(){
				public void run(){
					try{
						gameLoop();
					}catch(Exception exc){
						throw new RuntimeException(exc);
					}
				}
			}
		);
		t.start();
	}
	
	public void keyPressed(KeyEvent e){
		keyStates[e.getKeyCode()] = true;
	}
	public void keyReleased(KeyEvent e){
		keyStates[e.getKeyCode()] = false;
	}
	public void keyTyped(KeyEvent e){}
	
	public void inputUpdate(double delta){
		float[] adjust = {0, 0};
		
		if(keyStates[87] || keyStates[38]){
			adjust[1] = -player.speed;
			player.setDirection(GameObject.UP);
		}
		if(keyStates[65] || keyStates[37]){
			adjust[0] = -player.speed;
			player.setDirection(GameObject.LEFT);
		}
		if(keyStates[83] || keyStates[40]){
			adjust[1] = player.speed;
			player.setDirection(GameObject.DOWN);
		}
		if(keyStates[68] || keyStates[39]){
			adjust[0] = player.speed;
			player.setDirection(GameObject.RIGHT);
		}

		if(adjust[0] != 0 || adjust[1] != 0){
			float[] previous = {player.x, player.y};
			player.x += adjust[0];
			player.y += adjust[1];
			if(!map.checkPosition(player)){
				player.x = previous[0];
				player.y = previous[1];
			}else{
				player.flagForUpdate = true;
			}
		}
		if(keyStates[32]){
			float cx = player.x;
			float cy = player.y;
			float offset = 16;
			if(player.type.equals("healer")) offset = 24;

			if(player.direction == GameObject.UP) cy -= offset;
			if(player.direction == GameObject.LEFT) cx -= offset;
			if(player.direction == GameObject.DOWN) cy += offset;
			if(player.direction == GameObject.RIGHT) cx += offset;
			
			client.addMessage("a\t" + cx + "\t" + cy + "\t" + player.speed*4 + "\t" + player.direction + "\t" + player.type);
			keyStates[32] = false;
		}
		
		mapRenderer.centerOn((int)player.x, (int)player.y);
	}
	
	private int fps = 60;
	private int frameCount = 0;
	private long lastFpsTime;
	public void gameLoop() throws Exception{
		long lastLoopTime = System.nanoTime();
		final int TARGET_FPS = 60;
		final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

		while (true){
			long now = System.nanoTime();
			long updateLength = now - lastLoopTime;
			lastLoopTime = now;
			double delta = updateLength / ((double)OPTIMAL_TIME);

			lastFpsTime += updateLength;
			fps++;

			if (lastFpsTime >= 1000000000){
				System.out.println("(FPS: "+fps+")");
				lastFpsTime = 0;
				fps = 0;
			}

			synchronized(mapRenderer){
				inputUpdate(delta);
				game.update(delta, player);
				player.health = Math.min(player.health, 100);
				LinkedList<GameObject> toRemove = new LinkedList<GameObject>();
				for(GameObject o : game.objects.values()){
					if(o == player || o.ownedBy == player){
						if(o.flagForRemoval){
							client.addMessage("d\t" + o.objectID);
							toRemove.add(o);
						}else if(o.flagForUpdate){
							client.addMessage("p\t" + o.x + "\t" + o.y + "\t" + o.direction);
							if(o instanceof PlayableCharacter){
								client.addMessage("h\t" + ((PlayableCharacter)o).health);
							}
							o.flagForUpdate = false;
						}
					}
				}
				for(GameObject o : toRemove){
					game.removeObject(o.objectID);
				}
							

				client.update();
			}
			repaint();

			try{
				Thread.sleep((lastLoopTime-System.nanoTime() + OPTIMAL_TIME)/1000000);
			}catch(Exception exc){};
		}
	}
}
