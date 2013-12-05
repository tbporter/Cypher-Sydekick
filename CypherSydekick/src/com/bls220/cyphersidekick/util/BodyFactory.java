/**
 * 
 */
package com.bls220.cyphersidekick.util;

import pong.client.core.BodyEditorLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * @author bsmith
 * 
 */
public class BodyFactory {

	static final BodyEditorLoader LOADER = new BodyEditorLoader(
			Gdx.files.internal("test.json"));

	public BodyFactory() {
		// TODO Auto-generated constructor stub
	}

	static public Body createBody(World world, String shapeName,
			BodyType bodyType, float tileX, float tileY) {
		// 2. Create a FixtureDef, as usual.
		FixtureDef fd = new FixtureDef();
		fd.density = 1;
		fd.friction = 0.5f;
		fd.restitution = 0.3f;

		return createBody(world, shapeName, bodyType, tileX, tileY, fd);
	}

	static public Body createBody(World world, String shapeName,
			BodyType bodyType, float tileX, float tileY, FixtureDef fd) {
		// 1. Create a BodyDef, as usual.
		BodyDef bd = new BodyDef();
		bd.type = bodyType;

		// 3. Create a Body, as usual.
		Body body = world.createBody(bd);

		// 4. Create the body fixture automatically by using the loader.
		LOADER.attachFixture(body, shapeName, fd, 1);
		Vector2 origin = LOADER.getOrigin(shapeName, 1);
		body.setTransform(tileX + origin.x, tileY + origin.y, 0);
		return body;
	}

}
