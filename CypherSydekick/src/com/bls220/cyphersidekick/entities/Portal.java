/**
 * 
 */
package com.bls220.cyphersidekick.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.bls220.cyphersidekick.GameState;
import com.bls220.cyphersidekick.MySidekick;

/**
 * @author bsmith
 * 
 */
public class Portal extends Entity {

	public static final int PORTAL_ID = 4;

	/**
	 * @param texturePath
	 * @param world
	 */
	protected Portal(String texturePath, World world) {
		this(texturePath, 0, 0, world);
	}

	/**
	 * @param texturePath
	 * @param x
	 * @param y
	 * @param world
	 */
	public Portal(String texturePath, float x, float y, World world) {
		this(new TextureRegion(new Texture(Gdx.files.internal(texturePath))),
				x, y, world);
	}

	/**
	 * @param textReg
	 * @param x
	 * @param y
	 * @param world
	 */
	public Portal(TextureRegion textReg, float x, float y, World world) {
		super(textReg, x, y, world, "circle");
		Fixture fixture = mBody.getFixtureList().get(0);

		Filter filter = fixture.getFilterData();
		filter.categoryBits = EEnityCategories.BOUNDARY.getValue();
		filter.maskBits = EEnityCategories.PLAYER.getValue();

		fixture.setFilterData(filter);
		mBody.setType(BodyType.StaticBody);
	}

	/**
	 * @param textReg
	 * @param world
	 */
	protected Portal(TextureRegion textReg, World world) {
		this(textReg, 0, 0, world);
	}

	@Override
	public void onCollisionStart(Contact contact, Fixture otherFixture) {
		super.onCollisionStart(contact, otherFixture);
		// Go to next level (Maybe animation)
		((MySidekick) Gdx.app.getApplicationListener()).state = GameState.LevelJustFinished;
	}

	@Override
	protected void updateBody() {
		mBody.setLinearVelocity(0, 0); // Just in case
	}

}
