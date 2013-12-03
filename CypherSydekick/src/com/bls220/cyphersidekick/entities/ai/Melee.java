package com.bls220.cyphersidekick.entities.ai;

import com.bls220.cyphersidekick.entities.Entity;
import com.bls220.cyphersidekick.entities.Player;
import com.bls220.cyphersidekick.screens.MainScreen;

public class Melee extends AI {

	
	private static final double DETECT_DIST = 7; //entity inactive until player is within this range
	private static final double STOP_CHASE_DIST = 10; //entity will stop chasing player
	
	enum States { idle, chasing};
	States mState;
	public Melee(Entity e) {
		super(e);
		mState = States.idle;
	}

	@Override
	public void update(float delta) {
		Player p = MainScreen.mPlayer;
		Double dist = Entity.getDist(mEntity, p);
		switch(mState){
		case idle:
			if(dist <= DETECT_DIST){
				mState = States.chasing;
			}
			mEntity.setHeading(0,0);
			break;
		case chasing:
			if(dist >= STOP_CHASE_DIST){
				mState = States.idle;
			}
			else{
				mEntity.setHeading(p.getX() - mEntity.getX(),p.getY() - mEntity.getY());	
			}
			break;
		}

	}

}
