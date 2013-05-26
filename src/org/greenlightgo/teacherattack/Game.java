package org.greenlightgo.teacherattack;

import java.util.*;

class Game {
	HashMap<Long, GameObject> objects = new HashMap<Long, GameObject>();
	
	public Game(){
	}
	
	public GameObject getObject(long objectID){
		return objects.get(objectID);
	}
	
	public void addObject(long objectID, GameObject obj){
		objects.put(objectID, obj);
		obj.objectID = objectID;
	}
	
	public GameObject removeObject(long objectID){
		GameObject obj = objects.remove(objectID);
		
		return obj;
	}
	
	public void update(double delta, PlayableCharacter player){
		for(GameObject obj : objects.values()){
			obj.update(delta);
		}
		for(GameObject obj : objects.values()){
			if(obj instanceof AttackObject){
				AttackObject attack = (AttackObject)obj;
				if(obj.getRectangle().intersects(player.getRectangle())){
					if(attack.type.equals("healer")){
						player.health += 0.1f;
						player.flagForUpdate = true;
						attack.lifespan = 0;
						attack.flagForRemoval = true;
					}
				}
			}
		}
	}
}
