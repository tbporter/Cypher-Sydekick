package com.karien.tacobox.entities;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Entity {

	protected Vector2 mHeading;
	protected float mSpeed;

	protected Body mBody;
	protected Sprite mSprite;

	public static float TILE_WIDTH, TILE_HEIGHT;
	public static ArrayList<Entity> mEntities;
	private static TiledMapTileSet TILES;

	public Entity(String texturePath, World world) {
		this(texturePath, 0, 0, world);
	}

	public Entity(String texturePath, float x, float y, World world) {
		this(new TextureRegion(new Texture(Gdx.files.internal(texturePath))),
				x, y, world);
	}

	public Entity(TextureRegion textReg, World world) {
		this(textReg, 0, 0, world);
	}

	public Entity(TextureRegion textReg, float x, float y, World world) {
		mHeading = new Vector2();

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
		mBody.createFixture(fd).setUserData(this);

		shape.dispose();

		setPosition(x, y);
		mBody.setAngularVelocity(0);
		mBody.setLinearVelocity(0, 0);
		mBody.setLinearDamping(3f);

		mSprite = new Sprite(textReg);
		mSprite.setOrigin(TILE_WIDTH / 2, TILE_HEIGHT / 2);

		mEntities.add(this);
	}

	public static final void setup(TiledMap map) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
		TILE_WIDTH = layer.getTileWidth();
		TILE_HEIGHT = layer.getTileHeight();
		TILES = map.getTileSets().getTileSet(0);
		mEntities = new ArrayList<Entity>(20);
	}

	public static final TiledMapTile getTileRegion(int id) {
		return TILES.getTile(id);
	}

	public void setPosition(float x, float y) {
		mBody.setTransform(x * TILE_WIDTH + TILE_WIDTH / 2, y * TILE_HEIGHT
				+ TILE_HEIGHT / 2, 0);
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

	/**
	 * @return X pos in pixels
	 */
	public float getX() {
		return mBody.getPosition().x / TILE_WIDTH;
	}

	/**
	 * @return Y pos in pixels
	 */
	public float getY() {
		return mBody.getPosition().y / TILE_HEIGHT;
	}

	public void update(float delta) {

		// Apply forces
		mBody.applyLinearImpulse(getHeading().scl(mSpeed),
				mBody.getWorldPoint(mBody.getLocalCenter()), true);

		// Update image
		Vector2 bodyPos = mBody.getPosition();
		mSprite.setPosition(bodyPos.x - TILE_WIDTH / 2, bodyPos.y - TILE_HEIGHT
				/ 2);
		mSprite.setRotation(mBody.getAngle() * MathUtils.radiansToDegrees);

	}

	public void draw(SpriteBatch spriteBatch) {
		update(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		mSprite.draw(spriteBatch);
	}

	public void dispose() {
		mSprite.getTexture().dispose();
	}
}
