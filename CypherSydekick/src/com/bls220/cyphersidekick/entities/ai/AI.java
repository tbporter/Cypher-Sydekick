package com.bls220.cyphersidekick.entities.ai;

import com.badlogic.gdx.math.Vector2;
import com.bls220.cyphersidekick.entities.Entity;

public class AI {
	Entity mEntity;
	
	AI(Entity e){
		mEntity = e;
	}
	
	public void update(float delta){
		
	}
	
	public void facePoint(Entity m){
		Vector2 v = new Vector2(m.getX()-mEntity.getX(),m.getY()-mEntity.getY());
		Vector2 top = new Vector2(1,0);
		mEntity.setRotation((float)(Math.atan2(v.y, v.x) - Math.atan2(top.y, top.x)));
	}
}
