package com.karien.tacobox.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.karien.taco.mapstuff.C;
import com.karien.taco.mapstuff.MapActions;

public class Player {

	Sprite mSprite;
	Texture playerTextures[];

	final MapLayer mCollisionLayer;
	final MapActions mActions;

	Vector2 mHeading;
	float mSpeed = 48000f;
	MapObject mGrabbedObj;

	Body mBody;

	public static enum EFacing {
		N, S, E, W
	};

	EFacing mFacing;

	public final float TILE_WIDTH, TILE_HEIGHT;

	public Player(String[] spritePaths, TiledMap map, MapActions actions,
			World world) {
		this(spritePaths, map, actions, 0, 0, world);
	}

	public Player(String[] spritePaths, TiledMap map, MapActions actions,
			int x, int y, World world) {

		MapLayers layers = map.getLayers();
		TiledMapTileLayer tiles = (TiledMapTileLayer) layers.get(C.TileLayer);
		TILE_WIDTH = tiles.getTileWidth();
		TILE_HEIGHT = tiles.getTileWidth();
		mHeading = new Vector2();

		mCollisionLayer = layers.get(C.ObjectLayer);
		mActions = actions;

		mFacing = EFacing.S;
		playerTextures = new Texture[4];
		for (int i = 0; i < spritePaths.length; i++) {
			playerTextures[i] = new Texture(spritePaths[i]);
		}
		mSprite = new Sprite(playerTextures[mFacing.ordinal()]);

		// Create physics body
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DynamicBody;

		FixtureDef fd = new FixtureDef();
		fd.density = 10;
		fd.friction = 0.9f;
		fd.restitution = 0.3f;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(TILE_WIDTH / 2, TILE_HEIGHT / 2);
		fd.shape = shape;

		mBody = world.createBody(bd);
		mBody.createFixture(fd);

		shape.dispose();

		mBody.setTransform(x * TILE_WIDTH + TILE_WIDTH / 2, y * TILE_HEIGHT
				+ TILE_HEIGHT / 2, 0);
		mBody.setFixedRotation(true);
		mBody.setAngularVelocity(0);
		mBody.setLinearVelocity(0, 0);
		mBody.setLinearDamping(3f);

		mGrabbedObj = null;
	}

	public void setPosition(float x, float y) {
		mBody.setTransform(x, y, 0);
	}

	public Vector2 getHeading() {
		return mHeading.cpy();
	}

	public void setHeading(float x, float y) {
		mHeading.set(x, y).nor();
	}

	public void setRotation(float radAngle) {
		mBody.setTransform(mBody.getPosition(), radAngle);
	}

	public void setFacing(EFacing direction) {
		mFacing = direction;
	}

	public EFacing getFacing() {
		return EFacing.valueOf(mFacing.name());
	}

	/**
	 * @return X pos in pixels
	 */
	public float getX() {
		return mBody.getPosition().x;
	}

	/**
	 * @return Y pos in pixels
	 */
	public float getY() {
		return mBody.getPosition().y;
	}

	public void grab() {
		// Toggle grab
		if (mGrabbedObj == null) {
			// Find obj that the player is facing
			MapObject obj = null;
			int posX = (int) (getX() / TILE_WIDTH), posY = (int) (getY() / TILE_HEIGHT);

			switch (mFacing) {
			case E:
				obj = findObj(posX + 1, posY);
				break;
			case N:
				obj = findObj(posX, posY + 1);
				break;
			case W:
				obj = findObj(posX - 1, posY);
				break;
			case S:
			default:
				obj = findObj(posX, posY - 1);
				break;
			}
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
		switch (mFacing) {
		case E:
			mActions.activate(posX + 1, posY);
			break;
		case N:
			mActions.activate(posX, posY + 1);
			break;
		case W:
			mActions.activate(posX - 1, posY);
			break;
		case S:
		default:
			mActions.activate(posX, posY - 1);
			break;
		}
	}

	private MapObject findObj(int x, int y) {
		MapObject obj = null;
		for (MapObject object : mCollisionLayer.getObjects()) {
			MapProperties props = object.getProperties();
			if ((Integer) props.get("x") / TILE_WIDTH == x
					&& (Integer) props.get("y") / TILE_HEIGHT == y) {
				obj = object;
				break;
			}
		}
		return obj;
	}

	public void update(float delta) {

		// Apply forces
		mBody.applyLinearImpulse(getHeading().scl(mSpeed),
				mBody.getWorldPoint(mBody.getLocalCenter()), true);

		// Update image based on Facing
		mSprite.setTexture(playerTextures[mFacing.ordinal()]);
		Vector2 bodyPos = mBody.getPosition();
		mSprite.setOrigin(TILE_WIDTH / 2, TILE_HEIGHT / 2);
		mSprite.setPosition(bodyPos.x - TILE_WIDTH / 2, bodyPos.y - TILE_HEIGHT
				/ 2);
		mSprite.setRotation(mBody.getAngle() * MathUtils.radiansToDegrees);

	}

	public void draw(SpriteBatch spriteBatch) {
		update(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		mSprite.draw(spriteBatch);
	}

	public void dispose() {
		for (Texture texture : playerTextures) {
			texture.dispose();
		}
	}

}
