package com.bls220.cyphersidekick.mapstuff.level;

import pong.client.core.BodyEditorLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.bls220.cyphersidekick.MySidekick;
import com.bls220.cyphersidekick.comm.MsgHandler;
import com.bls220.cyphersidekick.mapstuff.C;

public class RandomLevel extends Level {

	private final static String EMPTY_MAP_PATH = "maps/blank.tmx";
	private final float TILE_WIDTH, TILE_HEIGHT;

	private static final int WALL_ID = 2;
	private static final int FLOOR_ID = 9;

	public RandomLevel(MySidekick parent, MsgHandler remote, World world) {
		super(parent, EMPTY_MAP_PATH, remote);
		TiledMapTileLayer tileLayer = (TiledMapTileLayer) map.getLayers().get(
				C.TileLayer);
		TILE_WIDTH = tileLayer.getTileWidth();
		TILE_HEIGHT = tileLayer.getTileHeight();
		generateLevel(map.getTileSets(), tileLayer, world);
	}

	private void generateLevel(TiledMapTileSets tileSets,
			TiledMapTileLayer tileLayer, World world) {

		Cell dirtCell = new Cell();
		dirtCell.setTile(tileSets.getTile(FLOOR_ID));

		Cell wallCell = new Cell();
		wallCell.setTile(tileSets.getTile(WALL_ID));

		BodyEditorLoader loader = new BodyEditorLoader(
				Gdx.files.internal("test.json"));

		// Generate ground
		for (int y = 0; y < tileLayer.getHeight() + 4; y++) {
			for (int x = 0; x < tileLayer.getWidth(); x++) {
				tileLayer.setCell(x, y, dirtCell);
			}
		}

		// Generate walls
		for (int y = 0; y < tileLayer.getHeight(); y++) {
			tileLayer.setCell(0, y, wallCell);
			createStaticTileBody(loader, world, "square").setTransform(
					0 + 0.5f, y + 0.5f, 0);

			tileLayer.setCell(tileLayer.getWidth() - 1, y, wallCell);
			createStaticTileBody(loader, world, "square").setTransform(
					(tileLayer.getWidth() - 1) + 0.5f, y + 0.5f, 0);
		}
		for (int x = 0; x < tileLayer.getWidth(); x++) {
			tileLayer.setCell(x, 0, wallCell);
			createStaticTileBody(loader, world, "square").setTransform(
					x + 0.5f, 0 + 0.5f, 0);

			tileLayer.setCell(x, tileLayer.getHeight() - 1, wallCell);
			createStaticTileBody(loader, world, "square").setTransform(
					x + 0.5f, (tileLayer.getHeight() - 1) + 0.5f, 0);
		}
	}

	private Body createStaticTileBody(BodyEditorLoader loader, World world,
			String name) {

		// 1. Create a BodyDef, as usual.
		BodyDef bd = new BodyDef();
		bd.type = BodyType.StaticBody;

		// 2. Create a FixtureDef, as usual.
		FixtureDef fd = new FixtureDef();
		fd.density = 1;
		fd.friction = 0.5f;
		fd.restitution = 0.3f;

		// 3. Create a Body, as usual.
		Body body = world.createBody(bd);

		// 4. Create the body fixture automatically by using the loader.
		loader.attachFixture(body, name, fd, 1);
		return body;
	}
}
