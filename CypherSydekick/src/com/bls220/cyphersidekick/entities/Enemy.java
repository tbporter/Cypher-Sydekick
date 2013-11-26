/**
 * 
 */
package com.bls220.cyphersidekick.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

/**
 * @author bsmith
 * 
 */
public class Enemy extends Entity {

	float mHealth;

	private static final float MAX_HEALTH = 100;
	private static final float SPEED = 7;

	/**
	 * @param texturePath
	 * @param world
	 */
	protected Enemy(String texturePath, World world) {
		this(texturePath, 0, 0, world);
	}

	/**
	 * @param texturePath
	 * @param x
	 * @param y
	 * @param world
	 */
	public Enemy(String texturePath, float x, float y, World world) {
		this(new TextureRegion(new Texture(Gdx.files.internal(texturePath))),
				x, y, world);
	}

	/**
	 * @param textReg
	 * @param x
	 * @param y
	 * @param world
	 */
	public Enemy(TextureRegion textReg, float x, float y, World world) {
		super(textReg, x, y, world);
		mBody.setBullet(true); // More accurate collision detection
		Fixture fixture = mBody.getFixtureList().get(0);
		fixture.setDensity(0.1f);
		fixture.setRestitution(1);
		fixture.setFriction(0);

		Filter filter = fixture.getFilterData();
		filter.categoryBits = EEnityCategories.ENEMY.getValue();
		filter.maskBits = (short) (EEnityCategories.ALL.getValue() & ~EEnityCategories.ENEMY
				.getValue());
		fixture.setFilterData(filter);

		mSpeed = SPEED;
		mHealth = MAX_HEALTH;
	}

	/**
	 * @param textReg
	 * @param world
	 */
	public Enemy(TextureRegion textReg, World world) {
		this(textReg, 0, 0, world);
	}

	@Override
	/**
	 * @see com.bls220.cyphersidekick.entities.Entity#onCollisionStart(com.badlogic.gdx.physics.box2d.Contact,
	 *      com.badlogic.gdx.physics.box2d.Fixture)
	 */
	public void onCollisionStart(Contact contact, Fixture otherFixture) {
		super.onCollisionStart(contact, otherFixture);
		Object userData = otherFixture.getUserData();
		if (userData instanceof Bullet) {
			// Do Damage
			mHealth -= ((Bullet) userData).getDamage();
			if (mHealth <= 0) {
				shouldDelete = true;
			}
		}
	}

	@Override
	/**
	 * @see com.bls220.cyphersidekick.entities.Entity#onCollisionEnd(com.badlogic.gdx.physics.box2d.Contact,
	 *      com.badlogic.gdx.physics.box2d.Fixture)
	 */
	public void onCollisionEnd(Contact contact, Fixture otherFixture) {
		super.onCollisionEnd(contact, otherFixture);
	}

}
