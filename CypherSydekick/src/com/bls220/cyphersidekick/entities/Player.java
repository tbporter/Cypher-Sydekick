package com.bls220.cyphersidekick.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.bls220.cyphersidekick.MySidekick;
import com.bls220.cyphersidekick.mapstuff.C;
import com.bls220.cyphersidekick.mapstuff.MapActions;

public class Player extends Entity {

	final MapActions mActions;
	MapObject mGrabbedObj;

	private float mHealth;

	private long shootTime;

	private static final float SPEED = 4f; // m/s
	private static final long SHOOT_DELAY = 400; // ms
	private static final float MAX_HEALTH = 100f;

	public Player(String[] spritePaths, TiledMap map, MapActions actions,
			World world) {
		this(spritePaths, map, actions, 0, 0, world);
	}

	public Player(String[] spritePaths, TiledMap map, MapActions actions,
			int x, int y, World world) {
		super(spritePaths[1], x, y, world);
		Gdx.app.log("Player", String.format("Player spawned at (%d, %d)", x, y));
		mBody.setFixedRotation(true);

		Fixture fixture = mBody.getFixtureList().get(0);
		Filter filter = fixture.getFilterData();
		filter.categoryBits = EEnityCategories.PLAYER.getValue();
		fixture.setFilterData(filter);

		mActions = actions;
		mGrabbedObj = null;
		mSpeed = SPEED;
		mHealth = MAX_HEALTH;
	}

	public Bullet shoot() {
		long curTime = System.currentTimeMillis();
		Bullet bullet = null;
		if (curTime - SHOOT_DELAY >= shootTime) {
			float angle = mBody.getAngle();
			Vector2 heading = new Vector2(MathUtils.cos(angle),
					MathUtils.sin(angle)).nor().scl(1.2f);
			bullet = new Bullet(Entity.getTileRegion(17).getTextureRegion(),
					getX() + heading.x, getY() + heading.y,
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
			int posX = (int) (getX()), posY = (int) (getY());

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

	public void activate(Vector2 tileTouch, TiledMap map) {
		// Look for object at touch position
		Vector2 screenTouch = tileTouch.cpy().scl(TILE_WIDTH);
		Gdx.app.log("Player", String.format(" + Activating (%f, %f)",
				screenTouch.x, screenTouch.y));
		for (MapObject o : map.getLayers().get(C.ObjectLayer).getObjects()) {
			if ((Integer) o.getProperties().get("x") == screenTouch.x
					&& (Integer) o.getProperties().get("y") == screenTouch.y) {
				String type = (String) o.getProperties().get("type");
				if (type == null)
					break;
				if (type.equals("npc")) {
					Gdx.app.log("Player", "Activating NPC");
					// TODO: Activate NPC
				}
			}
		}
	}

	public float getHealth() {
		return mHealth;
	}

	/**
	 * @see com.bls220.cyphersidekick.entities.Entity#onCollisionStart(com.badlogic.gdx.physics.box2d.Contact,
	 *      com.badlogic.gdx.physics.box2d.Fixture)
	 */
	@Override
	public void onCollisionStart(Contact contact, Fixture otherFixture) {
		super.onCollisionStart(contact, otherFixture);
		Object userData = otherFixture.getUserData();
		if (userData instanceof Harmful) {
			// Do Damage
			mHealth -= ((Harmful) userData).getDamage();
			mSpeed = SPEED;
		}
	}

	/**
	 * @see com.bls220.cyphersidekick.entities.Entity#onCollisionEnd(com.badlogic.gdx.physics.box2d.Contact,
	 *      com.badlogic.gdx.physics.box2d.Fixture)
	 */
	@Override
	public void onCollisionEnd(Contact contact, Fixture otherFixture) {
		super.onCollisionEnd(contact, otherFixture);
	}

}
