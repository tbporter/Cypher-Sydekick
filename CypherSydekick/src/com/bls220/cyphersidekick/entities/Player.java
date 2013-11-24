package com.bls220.cyphersidekick.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.bls220.cyphersidekick.MySidekick;
import com.bls220.cyphersidekick.mapstuff.C;
import com.bls220.cyphersidekick.mapstuff.MapActions;

public class Player extends Entity {

	final MapActions mActions;
	MapObject mGrabbedObj;

	private static final float SPEED = 48000f;

	private long shootTime;

	private static final long SHOOT_DELAY = 500; // ms

	public Player(String[] spritePaths, TiledMap map, MapActions actions,
			World world) {
		this(spritePaths, map, actions, 0, 0, world);
	}

	public Player(String[] spritePaths, TiledMap map, MapActions actions,
			int x, int y, World world) {
		super(spritePaths[1], x, y, world);
		Gdx.app.log("Player", String.format("Player spawned at (%d, %d)", x, y));
		mBody.setFixedRotation(true);
		mSpeed = SPEED;

		mActions = actions;
		mGrabbedObj = null;
	}

	public Bullet shoot() {
		long curTime = System.currentTimeMillis();
		Bullet bullet = null;
		if (curTime - SHOOT_DELAY >= shootTime) {
			float angle = mBody.getAngle();
			Vector2 heading = new Vector2(MathUtils.cos(angle),
					MathUtils.sin(angle)).nor();
			bullet = new Bullet(Entity.getTileRegion(17).getTextureRegion(),
					getX() - 0.5f + heading.x, getY() - 0.5f + heading.y,
					MySidekick.getWorld());
			bullet.setRotation(angle);
			bullet.setHeading(heading.x, heading.y);
			shootTime = curTime;
		}
		return bullet;
	}

	public void grab() {
		// Toggle grab
		if (mGrabbedObj == null) {
			// Find obj that the player is facing
			MapObject obj = null;
			int posX = (int) (getX() / TILE_WIDTH), posY = (int) (getY() / TILE_HEIGHT);

			// TODO: find object in front of player

			// Is the object moveable
			if (obj != null) {
				boolean moveable = (Boolean) obj.getProperties()
						.get(C.Moveable);
				if (moveable) {
					mGrabbedObj = obj;
				}
			}
		} else {
			mGrabbedObj = null;
		}
	}

	public void activate() {
		int posX = (int) (getX() / TILE_WIDTH), posY = (int) (getY() / TILE_HEIGHT);
		mActions.activate(posX, posY);

		// TODO: activate object in front of player

	}

}
