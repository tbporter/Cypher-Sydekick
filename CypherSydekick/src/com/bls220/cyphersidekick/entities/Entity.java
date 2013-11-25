package com.bls220.cyphersidekick.entities;

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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
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

	public boolean shouldDelete; // bodies must be removed when world = unlocked

	public static enum EEnityCategories {
		BOUNDARY(0x1), BULLET(0x2), ENEMY(0x4), PLAYER(0x8), ALL(0xFFFF);
		private final short category;

		EEnityCategories(int val) {
			this.category = (short) val;
		}

		public short getValue() {
			return category;
		}
	}

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

	/**
	 * 
	 * @param textReg
	 *            - sprite image
	 * @param x
	 *            - in tiles
	 * @param y
	 *            - in tiles
	 * @param world
	 *            - the world
	 */
	public Entity(TextureRegion textReg, float x, float y, World world) {
		mHeading = new Vector2();

		// Create physics body
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DynamicBody;

		FixtureDef fd = new FixtureDef();
		fd.density = 10;
		fd.friction = 0.9f;
		fd.restitution = 0.3f;
		fd.filter.categoryBits = EEnityCategories.BOUNDARY.getValue();
		fd.filter.maskBits = EEnityCategories.ALL.getValue();

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.5f, 0.5f);
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

	public Body getBody() {
		return mBody;
	}

	public static final TiledMapTile getTileRegion(int id) {
		return TILES.getTile(id);
	}

	/**
	 * 
	 * @param x
	 *            - in tiles (meters)
	 * @param y
	 *            - in tiles (meters)
	 */
	public void setPosition(float x, float y) {
		mBody.setTransform(x + 0.5f, y + 0.5f, 0);
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
	 * @return X pos in tiles
	 */
	public float getX() {
		return (mBody.getPosition().x - 0.5f);
	}

	/**
	 * @return Y pos in tiles
	 */
	public float getY() {
		return (mBody.getPosition().y - 0.5f);
	}

	protected void updateSprite() {
		mSprite.setPosition(getX() * TILE_WIDTH, getY() * TILE_HEIGHT);
		mSprite.setRotation(mBody.getAngle() * MathUtils.radiansToDegrees);
	}

	/**
	 * Apply forces to body.
	 */
	protected void updateBody() {
		mBody.applyLinearImpulse(getHeading().scl(mSpeed),
				mBody.getWorldPoint(mBody.getLocalCenter()), true);
	}

	public void update(float delta) {
		// Update Body
		updateBody();

		// Update image
		updateSprite();
	}

	public void draw(SpriteBatch spriteBatch) {
		update(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		mSprite.draw(spriteBatch);
	}

	public void onCollisionStart(Contact contact, Fixture otherFixture) {

	}

	public void onCollisionEnd(Contact contact, Fixture otherFixture) {

	}

	public void dispose() {
		mSprite.getTexture().dispose();
	}
}
