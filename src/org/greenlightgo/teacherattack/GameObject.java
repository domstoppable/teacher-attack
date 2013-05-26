package org.greenlightgo.teacherattack;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.imageio.*;


abstract class GameObject{
	public static final int DOWN = 0;
	public static final int UP = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;

	GameObject ownedBy = null;
	
	long objectID;
	
	float x, y;
	float speed = 1;
	int direction;
	
	boolean flagForRemoval = false;
	boolean flagForUpdate = false;
	
	public GameObject(){}

	public abstract void update(double delta);
	public abstract void render(Graphics2D g);
	public abstract Rectangle getRectangle();
}


class PlayableCharacter extends GameObject{
	static HashMap<String, Image> tilesets = new HashMap<String, Image>();
	
	float health;
	
	Image tileset;
	String name;
	String type;
	String currentMessage;
	long messageStartTime = 0l;
	
	public PlayableCharacter(String name, String type) throws Exception{
		if(!tilesets.containsKey(type)){
			tilesets.put(type, ImageIO.read(new File("resources/" + type + ".png")));
		}
		
		this.name = name;
		this.tileset = tilesets.get(type);
		this.type = type;
		
		if(type.equals("warrior")){
			speed = 2.0f;
		}else if(type.equals("healer")){
			speed = 1.5f;
		}
	}
	
	public Rectangle getRectangle(){
		return new Rectangle((int)x+2, (int)y+2, 12, 12);
	}
	
	public void setDirection(int direction){
		this.direction = direction;
	}
	
	public void render(Graphics2D g){
		int frameOffset = (int)((System.currentTimeMillis() / 500) % 2);
		int[] imageSource = {16*frameOffset, 16*direction};
		
		g.drawImage(
			tileset,
			(int)x, (int)y,
			(int)x+16, (int)y+16,
			imageSource[0], imageSource[1],
			imageSource[0]+16, imageSource[1]+16,
			null
		);
		if(currentMessage != null){
			g.drawString(currentMessage, x-10, y-10);
			if(System.currentTimeMillis() - messageStartTime > 6000l){
				currentMessage = null;
			}
		}
		g.drawString(name, x-10, y+24);
		g.setColor(Color.RED);
		g.fillRect((int)x-4, (int)y-8, 24, 4);
		g.setColor(Color.GREEN);
		g.fillRect((int)x-4, (int)y-8, (int)(24 * health / 100), 4);
		g.setColor(Color.BLACK);
	}
	
	public void update(double delta){
	}
	
	public void setCurrentMessage(String message){
		currentMessage = message;
		messageStartTime = System.currentTimeMillis();
	}

}

class AttackObject extends AnimatedSprite{
	String type;
	double lifespan;
	
	public AttackObject(float x, float y, float speed, int direction, String type) throws Exception{
		super(x, y, ImageIO.read(new File("resources/attack.png")), 2, 6);
		this.speed = speed;
		this.direction = direction;
		this.type = type;
		if(type.equals("wizard")){
			lifespan = 128.0d;
		}else{
			lifespan = 8.0d;
		}
	}
	
	public void update(double delta){
		lifespan -= delta;
		if(lifespan < 0){
			flagForRemoval = true;
		}
		if(type.equals("wizard")){
			if(direction == UP){
				this.y -= speed * delta;
			}else if(direction == DOWN){
				this.y += speed * delta;
			}else if(direction == LEFT){
				this.x -= speed * delta;
			}else if(direction == RIGHT){
				this.x += speed * delta;
			}
		}
	}
}

class AnimatedSprite extends GameObject{
	Image image;
	int fps, frames;
	int frame;
	
	public AnimatedSprite(float x, float y, Image image, int frames, int fps){
		this.x = x;
		this.y = y;
		this.image = image;
		this.fps = fps;
		this.frames = frames;
	}
	
	public Rectangle getRectangle(){
		return new Rectangle((int)x, (int)y, 16, 16);
	}
	
	public void render(Graphics2D g){
		frame = (int)((System.currentTimeMillis() / (1000 / fps)) % frames);
		int frameOffset = 16*frame;
		
		g.drawImage(
			image,
			(int)x, (int)y,
			(int)x+16, (int)y+16,
			frameOffset, 0,
			frameOffset+16, 16,
			null
		);
	}
	
	public void update(double delta){
	}
}
