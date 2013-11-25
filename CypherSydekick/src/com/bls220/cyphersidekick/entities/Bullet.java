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
public class Bullet extends Entity {

	/**
	 * @param texturePath
	 * @param world
	 */
	protected Bullet(String texturePath, World world) {
		this(texturePath, 0, 0, world);
	}

	/**
	 * @param texturePath
	 * @param x
	 * @param y
	 * @param world
	 */
	public Bullet(String texturePath, float x, float y, World world) {
		this(new TextureRegion(new Texture(Gdx.files.internal(texturePath))),
				x, y, world);
	}

	/**
	 * @param textReg
	 * @param x
	 * @param y
	 * @param world
	 */
	public Bullet(TextureRegion textReg, float x, float y, World world) {
		super(textReg, x, y, world);
		mBody.setBullet(true); // More accurate collision detection
		Fixture fixture = mBody.getFixtureList().get(0);
		fixture.setDensity(100);
		fixture.setRestitution(1);
		fixture.setFriction(0);

		Filter filter = fixture.getFilterData();
		filter.categoryBits = EEnityCategories.BULLET.getValue();
		filter.maskBits = (short) (EEnityCategories.ALL.getValue() & ~EEnityCategories.BULLET
				.getValue());

		fixture.setFilterData(filter);
		mBody.setLinearDamping(0);
		mSpeed = 400000f;
	}

	/**
	 * @param textReg
	 * @param world
	 */
	public Bullet(TextureRegion textReg, World world) {
		this(textReg, 0, 0, world);
	}

	@Override
	public void onCollisionStart(Contact contact, Fixture otherFixture) {
		super.onCollisionStart(contact, otherFixture);
		shouldDelete = true;

	}

}
