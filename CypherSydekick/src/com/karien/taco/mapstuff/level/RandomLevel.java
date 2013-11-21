package com.karien.taco.mapstuff.level;

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
import com.karien.taco.mapstuff.C;
import com.karien.tacobox.MyTacoBox;
import com.karien.tacobox.comm.MsgHandler;

public class RandomLevel extends Level {

	private final static String EMPTY_MAP_PATH = "maps/blank.tmx";
	private final float TILE_WIDTH, TILE_HEIGHT;

	public RandomLevel(MyTacoBox parent, MsgHandler remote, World world) {
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
		dirtCell.setTile(tileSets.getTile(829));

		Cell wallCell = new Cell();
		wallCell.setTile(tileSets.getTile(914));

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
					0 * TILE_WIDTH + TILE_WIDTH / 2,
					y * TILE_HEIGHT + TILE_HEIGHT / 2, 0);

			tileLayer.setCell(tileLayer.getWidth() - 1, y, wallCell);
			createStaticTileBody(loader, world, "square").setTransform(
					(tileLayer.getWidth() - 1) * TILE_WIDTH + TILE_WIDTH / 2,
					y * TILE_HEIGHT + TILE_HEIGHT / 2, 0);
		}
		for (int x = 0; x < tileLayer.getWidth(); x++) {
			tileLayer.setCell(x, 0, wallCell);
			createStaticTileBody(loader, world, "square").setTransform(
					x * TILE_WIDTH + TILE_WIDTH / 2,
					0 * TILE_HEIGHT + TILE_HEIGHT / 2, 0);

			tileLayer.setCell(x, tileLayer.getHeight() - 1, wallCell);
			createStaticTileBody(loader, world, "square")
					.setTransform(
							x * TILE_WIDTH + TILE_WIDTH / 2,
							(tileLayer.getHeight() - 1) * TILE_HEIGHT
									+ TILE_HEIGHT / 2, 0);
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
		loader.attachFixture(body, name, fd, TILE_WIDTH);
		return body;
	}
}
