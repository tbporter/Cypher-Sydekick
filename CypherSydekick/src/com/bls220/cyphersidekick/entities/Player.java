package com.bls220.cyphersidekick.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.bls220.cyphersidekick.MySidekick;
import com.bls220.cyphersidekick.mapstuff.C;
import com.bls220.cyphersidekick.mapstuff.MapActions;
import com.bls220.cyphersidekick.screens.MainScreen;

public class Player extends Entity implements Living {

	final MapActions mActions;

	private float mHealth;

	private int mPillarCount;

	private static final float SPEED = 4f; // m/s
	private static final long SHOOT_DELAY = 400; // ms
	private static final float MAX_HEALTH = 100f;

	private static final String TAG = "Player";

	public Player(String[] spritePaths, TiledMap map, MapActions actions,
			World world) {
		this(spritePaths, map, actions, 0, 0, world);
	}

	public Player(String[] spritePaths, TiledMap map, MapActions actions,
			int x, int y, World world) {
		super(spritePaths[1], x, y, world, "circle");
		Gdx.app.log(TAG, String.format("Player spawned at (%d, %d)", x, y));
		mBody.setFixedRotation(true);

		Fixture fixture = mBody.getFixtureList().get(0);
		Filter filter = fixture.getFilterData();
		filter.categoryBits = EEnityCategories.PLAYER.getValue();
		fixture.setFilterData(filter);

		mActions = actions;
		mSpeed = SPEED;
		mHealth = MAX_HEALTH;
	}

	@Override
	public Bullet shoot() {
		return super.shoot(SHOOT_DELAY);
	}

	/**
	 * Handles players interaction with other objects, excluding physics
	 * 
	 * @param tileTouch
	 *            - Which tile is to be activated
	 * @param mapa
	 *            - The current level's map
	 */
	public void activate(Vector2 tileTouch, TiledMap map) {
		// Look for object at touch position
		Vector2 screenTouch = tileTouch.cpy().scl(TILE_WIDTH);
		Gdx.app.log(TAG, String.format(" + Activating (%f, %f)", screenTouch.x,
				screenTouch.y));
		MainScreen screen = (MainScreen) ((MySidekick) Gdx.app
				.getApplicationListener()).getScreen();
		// Look for objects to interact with
		for (MapObject o : map.getLayers().get(C.ObjectLayer).getObjects()) {
			MapProperties props = o.getProperties();
			String type = (String) props.get("type");
			if (type == null)
				continue; // Can't determine type of object
			if ((Integer) props.get("x") == screenTouch.x
					&& (Integer) props.get("y") == screenTouch.y
					&& o.isVisible()) {
				if (type.equals("npc")) { // NPC
					Gdx.app.log(TAG, "Activating NPC");
					// Activate NPC
					String npcName = o.getName();
					// check if user has key for npc
					boolean hasKey = props.get("hasKey", false, Boolean.class);
					if (hasKey) {
						screen.setMsgBoxText(npcName
								+ ": Happy travels comrade.");
						if ((Boolean) (props.get("used")) == false) {
							// TODO: activate a pillar
							Gdx.app.log(
									TAG,
									"Activating pillar "
											+ props.get("pillarNum"));
							mPillarCount += 1;
						}
						props.put("used", true);
					} else {
						((MainScreen) (((MySidekick) Gdx.app
								.getApplicationListener()).getScreen()))
								.setMsgBoxText(npcName
										+ " doesn't trust you, yet.");
					}
				}
			}
		}
		if (mPillarCount >= 5) {
			MapObject portal = map.getLayers().get(C.ObjectLayer).getObjects()
					.get("portal");
			if (portal != null) {
				Gdx.app.log(TAG, "Unlocking portal");
				portal.setVisible(true);
				// Make entity for collision purposes
				TiledMapTileLayer tileLayer = ((TiledMapTileLayer) screen.map
						.getLayers().get(C.TileLayer));
				Vector2 center = new Vector2(tileLayer.getWidth() / 2,
						tileLayer.getHeight() / 2);
				new Portal(screen.map.getTileSets().getTile(Portal.PORTAL_ID)
						.getTextureRegion(), center.x, center.y,
						MySidekick.getWorld());
			}
		}
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

	@Override
	public float getHealth() {
		return mHealth;
	}

	@Override
	public void setHealth(float newHealth) {
		mHealth = newHealth;
	}

	@Override
	public float getMaxHealth() {
		return MAX_HEALTH;
	}

}
