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
import com.badlogic.gdx.math.Vector2;
import com.karien.taco.mapstuff.C;
import com.karien.taco.mapstuff.MapActions;

public class Player {

	Sprite mSprite;
	Texture playerTextures[];

	Vector2 mPos;
	final MapLayer mCollisionLayer;
	final MapActions mActions;

	Vector2 mVelocity;
	float mSpeed = 8f;
	MapObject mGrabbedObj;

	public static enum EFacing {
		N, S, E, W
	};

	EFacing mFacing;

	public final float TILE_WIDTH, TILE_HEIGHT;

	public Player(String[] spritePaths, TiledMap map, MapActions actions) {
		this(spritePaths, map, actions, 0, 0);
	}

	public Player(String[] spritePaths, TiledMap map, MapActions actions,
			int x, int y) {

		MapLayers layers = map.getLayers();
		TiledMapTileLayer tiles = (TiledMapTileLayer) layers.get(C.TileLayer);
		TILE_WIDTH = tiles.getTileWidth();
		TILE_HEIGHT = tiles.getTileWidth();

		mCollisionLayer = layers.get(C.ObjectLayer);
		mActions = actions;

		mFacing = EFacing.S;
		playerTextures = new Texture[4];
		for (int i = 0; i < spritePaths.length; i++) {
			playerTextures[i] = new Texture(spritePaths[i]);
		}
		mSprite = new Sprite(playerTextures[mFacing.ordinal()]);
		mPos = new Vector2();
		mVelocity = new Vector2();
		setPosition(x, y);

		mGrabbedObj = null;
	}

	public void setPosition(float x, float y) {
		mPos.set(x, y);
		mSprite.setPosition((int) x * TILE_WIDTH, (int) y * TILE_HEIGHT);
	}

	public Vector2 getHeading() {
		return mVelocity.cpy();
	}

	public void setVelocity(float x, float y) {
		mVelocity.set(x, y).nor();
	}

	public void setFacing(EFacing direction) {
		mFacing = direction;
	}

	public EFacing getFacing() {
		return EFacing.valueOf(mFacing.name());
	}

	/**
	 * @return X pos in tiles
	 */
	public int getX() {
		return (int) (mPos.x);
	}

	/**
	 * @return Y pos in tiles
	 */
	public int getY() {
		return (int) (mPos.y);
	}

	public void grab() {
		// Toggle grab
		if (mGrabbedObj == null) {
			// Find obj that the player is facing
			MapObject obj = null;
			switch (mFacing) {
			case E:
				obj = findObj(getX() + 1, getY());
				break;
			case N:
				obj = findObj(getX(), getY() + 1);
				break;
			case W:
				obj = findObj(getX() - 1, getY());
				break;
			case S:
			default:
				obj = findObj(getX(), getY() - 1);
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
		mActions.activate(getX(), getY());
		switch (mFacing) {
		case E:
			mActions.activate(getX() + 1, getY());
			break;
		case N:
			mActions.activate(getX(), getY() + 1);
			break;
		case W:
			mActions.activate(getX() - 1, getY());
			break;
		case S:
		default:
			mActions.activate(getX(), getY() - 1);
			break;
		}
	}

	public boolean isBlocking(MapObject obj) {
		Boolean visible = false;
		Boolean blocked = false;
		// Find object
		MapProperties props = obj.getProperties();
		// Get Properties
		visible = (Boolean) props.get(C.Visible);
		blocked = (Boolean) props.get(C.Blocked);

		return visible && blocked;
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

	/**
	 * Try to move to tile at x,y
	 */
	public boolean move(float x, float y) {
		int tileX = (int) x;
		int tileY = (int) y;
		int oldX = getX();
		int oldY = getY();
		MapObject obj = findObj(tileX, tileY);

		if (obj != null && isBlocking(obj) && obj != mGrabbedObj) {
			return false;
		}

		// See if object is grabbed and can move
		if (mGrabbedObj != null) {
			int grabX = (int) ((Integer) mGrabbedObj.getProperties().get("x") / TILE_WIDTH);
			int grabY = (int) ((Integer) mGrabbedObj.getProperties().get("y") / TILE_HEIGHT);
			int obj2X = (int) (grabX + tileX - oldX);
			int obj2Y = (int) (grabY + tileY - oldY);
			MapObject obj2 = findObj(obj2X, obj2Y);
			if (obj2 != null && isBlocking(obj2)) {
				// Can't move
				return false;
			}

			// Move obj
			mGrabbedObj.getProperties().put("x", (int) (obj2X * TILE_WIDTH));
			mGrabbedObj.getProperties().put("y", (int) (obj2Y * TILE_HEIGHT));
			// Do exit/enter
			mActions.exit(grabX, grabY);
			mActions.enter(obj2X, obj2Y);
		}

		// Move Player

		setPosition(x, y);
		// Do exit/enter
		mActions.exit(oldX, oldY);
		mActions.enter(tileX, tileY);

		return true;
	}

	public void update(float delta) {
		int curX = getX();
		int curY = getY();
		float X, Y;

		if (mVelocity.x != 0) {
			X = mPos.x + mVelocity.x * delta * mSpeed;
			if ((int) X - curX != 0) {
				move(X, curY);
			} else {
				// Update partial position
				setPosition(X, mPos.y);
			}
		} else if (mVelocity.y != 0) {
			Y = mPos.y + mVelocity.y * delta * mSpeed;
			if ((int) Y - curY != 0) {
				move(curX, Y);
			} else {
				// Update partial position
				setPosition(mPos.x, Y);
			}
		}

		// Update image based on Facing
		mSprite.setTexture(playerTextures[mFacing.ordinal()]);

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
