package org.greenlightgo.teacherattack;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;

public class GameClient extends Thread{
	LinkedList<String> inputQueue = new LinkedList<String>();
	LinkedList<String> outputQueue = new LinkedList<String>();
	
	Game game;
	PlayableCharacter player;
	
	long clientID;
	Socket socket;
	BufferedReader in;
	PrintWriter out;
	
	public GameClient(String host, int port, PlayableCharacter player, Game game) throws Exception{
		this.game = game;
		this.player = player;
		
		socket = new Socket(host, port);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		out.println(player.name + "\t" + player.type);
		String[] properties = in.readLine().split("\t");
		for(String s : properties) System.out.print(s + "\t");
		clientID = Long.parseLong(properties[0]);
		player.x = Float.parseFloat(properties[1]);
		player.y = Float.parseFloat(properties[2]);
		player.direction = Integer.parseInt(properties[3]);

		game.addObject(clientID, player);
		System.out.println("Received ClientID: " + clientID);
		update();
		start();
	}
	
	public void addMessage(String message){
		synchronized(outputQueue){
			outputQueue.add(message);
		}
	}
	
	public void run(){
		String inputLine;
		try{
			while ((inputLine = in.readLine()) != null) {
				if(!inputLine.equals("")){
					synchronized(inputQueue){
						inputQueue.add(inputLine);
					}
				}
			}
		}catch(Exception exc){
			throw new RuntimeException(exc);
		}
	}
	
	public void update() throws Exception{
		synchronized(outputQueue){
			for(String m : outputQueue){
				out.println(m);
			}
			outputQueue.clear();
			out.println("");
		}
		synchronized(inputQueue){
			for(String inputLine : inputQueue){
				String[] tokens = inputLine.split("\t");
				
				if(tokens[0].equals("c")){
					long clientID = Long.parseLong(tokens[1]);
					GameObject obj = game.getObject(clientID);
					if(tokens[2].equals("j")){
						if(clientID == this.clientID) continue;
						
						String name = tokens[3];
						String type = tokens[4];
						game.addObject(clientID, new PlayableCharacter(name, type));
					}else if(tokens[2].equals("p")){
						if(clientID == this.clientID) continue;
						
						obj.x = Float.parseFloat(tokens[3]);
						obj.y = Float.parseFloat(tokens[4]);
						obj.direction = Integer.parseInt(tokens[5]);
					}else if(tokens[2].equals("h")){
						((PlayableCharacter)obj).health = Float.parseFloat(tokens[3]);
					}else if(tokens[2].equals("a")){
						System.err.println(clientID + " ATTACK");
						AttackObject attack = new AttackObject(
							Float.parseFloat(tokens[3]),
							Float.parseFloat(tokens[4]),
							Float.parseFloat(tokens[5]),
							Integer.parseInt(tokens[6]),
							tokens[7]
						);
						game.addObject(Long.parseLong(tokens[8]), attack);
						if(clientID == this.clientID){
							attack.ownedBy = player;
						}
					}else if(tokens[2].equals("d")){
						game.removeObject(Long.parseLong(tokens[3]));
					}else{
						System.err.println("Don't know how to handle " + tokens[2]);
					}
				}
			}
			inputQueue.clear();
		}
	}
}