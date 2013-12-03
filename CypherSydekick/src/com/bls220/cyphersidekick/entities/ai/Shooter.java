package com.bls220.cyphersidekick.entities.ai;

import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.bls220.cyphersidekick.entities.Bullet;
import com.bls220.cyphersidekick.entities.Entity;
import com.bls220.cyphersidekick.entities.Entity.EEnityCategories;
import com.bls220.cyphersidekick.entities.Player;
import com.bls220.cyphersidekick.screens.MainScreen;

public class Shooter extends AI {
	enum States {
		idle, chasing, running, standing
	};

	States mState;
	private static final double DETECT_DIST = 7; // entity inactive until player
													// is within this range
	private static final double STOP_CHASE_DIST = 10; // entity will stop
														// chasing player
	private static final double SAFE_DIST = 3; // entity will try to keep this
												// distance
	private static final double SAFE_DIST_JITTER = .5; // so the entity doesn't
														// have to be exactly
														// precise

	public Shooter(Entity e) {
		super(e);
		mState = States.idle;
	}

	@Override
	public void update(float delta) {
		Player p = MainScreen.mPlayer;
		Double dist = Entity.getDist(mEntity, p);
		Bullet bullet = null;
		switch (mState) {
		case idle:
			if (dist <= DETECT_DIST) {
				mState = States.chasing;
			}
			mEntity.setHeading(0, 0);
			break;
		case chasing:
			if (dist >= STOP_CHASE_DIST)
				mState = States.idle;
			else if (dist <= SAFE_DIST)
				mState = States.running;
			else if (dist >= SAFE_DIST && dist <= SAFE_DIST + SAFE_DIST_JITTER)
				mState = States.standing;
			else {
				mEntity.setHeading(p.getX() - mEntity.getX(), p.getY()
						- mEntity.getY());
				facePoint(p);
				bullet = mEntity.shoot();
			}
			break;
		case running:
			if (dist >= SAFE_DIST)
				mState = States.chasing;
			else {
				mEntity.setHeading(mEntity.getX() - p.getX(), mEntity.getY()
						- p.getY());
				facePoint(p);
				bullet = mEntity.shoot();
			}
			break;
		case standing:
			if (dist <= SAFE_DIST)
				mState = States.running;
			else if (dist >= SAFE_DIST + SAFE_DIST_JITTER)
				mState = States.chasing;
			else {
				facePoint(p);
				bullet = mEntity.shoot();
			}
			break;
		}

		if (bullet != null) {
			// Make bullet ignore enemies
			Fixture fixture = bullet.getBody().getFixtureList().get(0);
			Filter filter = fixture.getFilterData();
			filter.maskBits &= ~EEnityCategories.ENEMY.getValue();
			fixture.setFilterData(filter);

		}

	}

}
