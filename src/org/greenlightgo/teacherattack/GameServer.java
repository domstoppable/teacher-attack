package org.greenlightgo.teacherattack;

import java.io.*;
import java.util.*;
import java.net.*;

import javax.swing.JOptionPane;

public class GameServer{
	static long objectCount;
	
	public static void main(String[] args) throws Exception{
		try{
			new GameServer(Integer.parseInt(args[0]));
		}catch(Exception exc){
			new GameServer(Integer.parseInt(JOptionPane.showInputDialog(null, "Enter port number")));
		}
	}
	
	Game game;
	LinkedList<Client> clients = new LinkedList<Client>();
	ServerSocket serverSocket;
	public GameServer(int port) throws Exception{
		game = new Game();
		serverSocket = new ServerSocket(port);
		System.err.println("Waiting for connections");
		while(true){
			Client c = new Client(serverSocket.accept());
			synchronized(clients){
				clients.add(c);
			}
			c.start();
		}
	}
	
	public synchronized long add(GameObject o){
		long objectID = objectCount++;
		game.addObject(objectID, o);
		return objectID;
	}
	
	public void broadcast(Client from, String message){
		message = "c\t" + from.clientID + "\t" + message;
		synchronized(clients){
			for(Client c : clients){
				if(c != null) c.addMessage(message);
			}
		}
	}

	class Client extends Thread{
		Socket socket;
		
		PlayableCharacter player;
		
		long clientID;
		
		LinkedList<String> messages = new LinkedList<String>();
		public Client(Socket socket){
			this.socket = socket;
		}
		
		public void addMessage(String m){
			synchronized(messages){
				messages.add(m);
			}
		}
		
		public void run() {
			try {
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				String[] playerOptions = in.readLine().split("\t");
				if(playerOptions[1].equals("dom")){
					player = new BadGuy(playerOptions[0], playerOptions[1]);
					player.x = 500.0f;
					player.y = -384.0f;
					player.health = 100.0f;
				}else{
					player = new PlayableCharacter(playerOptions[0], playerOptions[1]);
					player.x = 162.0f + (float)(Math.random() * 700.0f);
					player.y = 680.0f;
					player.health = 75.0f;
				}
				add(player);
				clientID = player.objectID;
				
				out.println(clientID + "\t" + player.x + "\t" + player.y + "\t" + player.direction);
				System.out.println("Assigned " + clientID + " to " + player.name + "/" + player.type);
				broadcast(this, "j\t" + player.name + "\t" + player.type);
				
				for(GameObject obj : game.objects.values()){
					if(obj instanceof PlayableCharacter){
						PlayableCharacter character = (PlayableCharacter)obj;
						out.println("c\t" + obj.objectID + "\tj\t" + character.name + "\t" + character.type);
						out.println("c\t" + obj.objectID + "\tp\t" + character.x + "\t" + character.y + "\t" + character.direction);
						out.println("c\t" + obj.objectID + "\th\t" + character.health);
					}else if(obj instanceof AttackObject){
						AttackObject attack = (AttackObject)obj;
						out.println("c\t" + attack.ownedBy.objectID + "\ta\t" + attack.x + "\t" + attack.y + "\t" + attack.speed + "\t" + attack.direction + "\t" + attack.type + "\t" + attack.objectID);
					}
				}
				out.println("");

				String inputLine;
				do{
					inputLine = in.readLine();
					if(inputLine != null && !inputLine.equals("")){
						//System.out.println("Received " + inputLine);
						String[] tokens = inputLine.split("\t");
						if(tokens.length > 0){
							if(tokens[0].equals("p")){
								player.x = Float.parseFloat(tokens[1]);
								player.y = Float.parseFloat(tokens[2]);
								player.direction = Integer.parseInt(tokens[3]);
								broadcast(this, inputLine);
							}else if(tokens[0].equals("a")){
								AttackObject obj = new AttackObject(
									Float.parseFloat(tokens[1]),
									Float.parseFloat(tokens[2]),
									Float.parseFloat(tokens[3]),
									Integer.parseInt(tokens[4]),
									tokens[5]
								);
								obj.ownedBy = player;
								add(obj);
								broadcast(this, inputLine + "\t" + obj.objectID);
							}else if(tokens[0].equals("b")){
								FBomb obj = new FBomb(
									Float.parseFloat(tokens[1]),
									Float.parseFloat(tokens[2]),
									Float.parseFloat(tokens[3]),
									Integer.parseInt(tokens[4]),
									Integer.parseInt(tokens[5])
								);
								obj.ownedBy = player;
								add(obj);
								broadcast(this, inputLine + "\t" + obj.objectID);
							}else if(tokens[0].equals("x")){
								FExplosion obj = new FExplosion(
									Float.parseFloat(tokens[1]),
									Float.parseFloat(tokens[2])
								);
								obj.ownedBy = player;
								add(obj);
								broadcast(this, inputLine + "\t" + obj.objectID);
							}else if(tokens[0].equals("h")){
								player.health = Math.min(Float.parseFloat(tokens[1]), 100);
								broadcast(this, inputLine);
							}else if(tokens[0].equals("d")){
								GameObject o = game.removeObject(Long.parseLong(tokens[1]));
								broadcast(this, inputLine);
							}else{
								System.err.println("Don't know how to handle " + tokens[0]);
							}
						}
					}
					synchronized(messages){
						for(String m : messages){
							out.println(m);
						}
						messages.clear();
					}
				}while(inputLine != null && player.health > 0.0f);
				
				out.close();
				in.close();
				socket.close();
				
				clients.remove(this);
			} catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}
	}
}
