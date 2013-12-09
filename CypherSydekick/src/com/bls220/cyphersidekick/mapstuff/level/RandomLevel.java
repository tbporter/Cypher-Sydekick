package com.bls220.cyphersidekick.mapstuff.level;

import java.util.ArrayList;
import java.util.Random;

import pong.client.core.BodyEditorLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.bls220.cyphersidekick.MySidekick;
import com.bls220.cyphersidekick.comm.MsgHandler;
import com.bls220.cyphersidekick.entities.Enemy;
import com.bls220.cyphersidekick.entities.Portal;
import com.bls220.cyphersidekick.entities.ai.Melee;
import com.bls220.cyphersidekick.entities.ai.Shooter;
import com.bls220.cyphersidekick.mapstuff.C;
import com.bls220.cyphersidekick.util.BodyFactory;
import com.github.tbporter.cypher_sydekick.database.UserKeyDOA;

public class RandomLevel extends Level {

	private final static String EMPTY_MAP_PATH = "maps/blank.tmx";
	private final float TILE_WIDTH, TILE_HEIGHT;

	private static final int WALL_ORANGE_ID = 2;
	private static final int WALL_GREEN_ID = 1;

	private static final int FLOOR_BLUE_ID = 9;
	private static final int FLOOR_RED_ID = 10;

	private static final int PORTAL_ID = Portal.PORTAL_ID;

	private static final int ENEMY_1_ID = 3;
	private static final int NPC_ID = 1;

	private static final int PILLAR_OFF_TOP_ID = 19;
	private static final int PILLAR_OFF_BOTTOM_ID = 27;

	public RandomLevel(MySidekick parent, MsgHandler remote, World world) {
		super(parent, EMPTY_MAP_PATH, remote);
		TiledMapTileLayer tileLayer = (TiledMapTileLayer) map.getLayers().get(
				C.TileLayer);
		MapLayer objectLayer = map.getLayers().get(C.ObjectLayer);
		TILE_WIDTH = tileLayer.getTileWidth();
		TILE_HEIGHT = tileLayer.getTileHeight();
		generateLevel(map.getTileSets(), tileLayer, objectLayer, world);
	}

	private void generateLevel(TiledMapTileSets tileSets,
			TiledMapTileLayer tileLayer, MapLayer objectLayer, World world) {

		Cell dirtCell = new Cell();
		dirtCell.setTile(tileSets.getTile(FLOOR_RED_ID));

		Cell wallCell = new Cell();
		wallCell.setTile(tileSets.getTile(WALL_ORANGE_ID));

		BodyEditorLoader loader = new BodyEditorLoader(
				Gdx.files.internal("test.json"));

		// Generate ground
		for (int y = 0; y < tileLayer.getHeight(); y++) {
			for (int x = 0; x < tileLayer.getWidth(); x++) {
				tileLayer.setCell(x, y, dirtCell);
			}
		}
		
		//Random block genration
		int choice = 0;
		Vector2 center = new Vector2(tileLayer.getWidth() / 2, tileLayer.getHeight() / 2);
		int centerDeadzone = 10;
		for (int y = 1; y < tileLayer.getHeight() - 1; y++) {
			for (int x = 1; x < tileLayer.getWidth() - 1 ; x++){
				if((x < center.x - centerDeadzone || x > center.x + centerDeadzone) && (y < center.y -centerDeadzone || y > center.y + centerDeadzone) ){
					choice = MathUtils.random(10);
					if(choice == 9){
						tileLayer.setCell(x, y, wallCell);
						BodyFactory.createBody(world, "tile", BodyType.StaticBody, x, y);
					}
				}
			}
		}
		
		// Generate walls
		for (int y = 0; y < tileLayer.getHeight(); y++) {
			tileLayer.setCell(0, y, wallCell);
			BodyFactory.createBody(world, "tile", BodyType.StaticBody, 0, y);

			tileLayer.setCell(tileLayer.getWidth() - 1, y, wallCell);
			BodyFactory.createBody(world, "tile", BodyType.StaticBody,
					(tileLayer.getWidth() - 1), y);
		}
		for (int x = 0; x < tileLayer.getWidth(); x++) {
			tileLayer.setCell(x, 0, wallCell);
			BodyFactory.createBody(world, "tile", BodyType.StaticBody, x, 0);

			tileLayer.setCell(x, tileLayer.getHeight() - 1, wallCell);
			BodyFactory.createBody(world, "tile", BodyType.StaticBody, x,
					(tileLayer.getHeight() - 1));
		}

		// Place enemies
		TextureRegion enemyTexReg = tileSets.getTile(ENEMY_1_ID)
				.getTextureRegion();
		for (int i = 0; i < 300; i++) {
			new Enemy(enemyTexReg,
					MathUtils.random(1, tileLayer.getWidth() - 2),
					MathUtils.random(1, tileLayer.getHeight() - 2), world);
		}

		// Make central room
		generateCentralRoom(tileSets, tileLayer, objectLayer, world, loader);

		// Make NPC
		UserKeyDOA datasource = new UserKeyDOA(
				((MySidekick) Gdx.app.getApplicationListener()).getContext());
		datasource.open();
		// TODO: enforce friends of friends
		ArrayList<String> usernames = datasource.getAllUsers();
		for (int i = 1; i < 6; i++) {
			// Pick a name randomly
			String name = "Anon " + i;
			String key = "hasKey";
			if (!usernames.isEmpty()) {
				name = usernames.get(MathUtils.random(usernames.size() - 1));
				// see if has key
				key = datasource.getKeyViaUsername(name);
				usernames.remove(name);
			}

			boolean hasKey = (key != null) && !key.isEmpty();
			makeNPC(name, hasKey, i,
					tileLayer.getWidth() / 2 - 5 + (2 * i - 1),
					tileLayer.getHeight() / 2 + 7, objectLayer.getObjects(),
					loader, world);
		}
		datasource.close();
	}

	private void generateCentralRoom(TiledMapTileSets tileSets,
			TiledMapTileLayer tileLayer, MapLayer objectLayer, World world,
			BodyEditorLoader loader) {
		Cell wallCell = new Cell();
		wallCell.setTile(tileSets.getTile(WALL_GREEN_ID));

		Cell floorCell = new Cell();
		floorCell.setTile(tileSets.getTile(FLOOR_BLUE_ID));

		Cell groundCell = new Cell();
		groundCell.setTile(tileSets.getTile(FLOOR_RED_ID));
		
		Cell pillarTopCell = new Cell();
		pillarTopCell.setTile(tileSets.getTile(PILLAR_OFF_TOP_ID));

		Cell pillarBottomCell = new Cell();
		pillarBottomCell.setTile(tileSets.getTile(PILLAR_OFF_BOTTOM_ID));

		int mapHeight = tileLayer.getHeight();
		int mapWidth = tileLayer.getWidth();
		Vector2 center = new Vector2(mapWidth / 2, mapHeight / 2);
		int roomWidth = 10;
		int borderWidth = roomWidth+5;
		// Generate ground
		for (int y = (int) (center.y - borderWidth / 2); y < center.y + borderWidth
				/ 2; y++) {
			for (int x = (int) (center.x - borderWidth / 2); x < center.x
					+ borderWidth / 2; x++) {
				tileLayer.setCell(x, y, groundCell);
			}
		}
		
		// Generate ground
		for (int y = (int) (center.y - roomWidth / 2); y < center.y + roomWidth
				/ 2; y++) {
			for (int x = (int) (center.x - roomWidth / 2); x < center.x
					+ roomWidth / 2; x++) {
				tileLayer.setCell(x, y, floorCell);
			}
		}

		// Generate walls
		for (int y = (int) (center.y - roomWidth / 2 + 1); y < center.y
				+ roomWidth / 2; y++) {
			tileLayer.setCell((int) (center.x - roomWidth / 2), y, wallCell);
			BodyFactory.createBody(world, "tile", BodyType.StaticBody,
					(center.x - roomWidth / 2), y);

			tileLayer.setCell((int) (center.x + roomWidth / 2), y, wallCell);
			BodyFactory.createBody(world, "tile", BodyType.StaticBody,
					(center.x + roomWidth / 2), y);
		}
		for (int x = (int) (center.x - roomWidth / 2); x < center.x + roomWidth
				/ 2 + 1; x++) {
			if (x != center.x) {
				tileLayer
						.setCell(x, (int) (center.y - roomWidth / 2), wallCell);
				BodyFactory.createBody(world, "tile", BodyType.StaticBody, x,
						(center.y - roomWidth / 2));
			}

			tileLayer.setCell(x, (int) (center.y + roomWidth / 2), wallCell);
			BodyFactory.createBody(world, "tile", BodyType.StaticBody, x,
					(center.y + roomWidth / 2));
		}

		// Place pillars
		makePillar(1, center.x, center.y + 2, objectLayer.getObjects(), loader,
				world);
		makePillar(2, center.x - 2, center.y + 1, objectLayer.getObjects(),
				loader, world);
		makePillar(3, center.x - 1.5f, center.y - 1, objectLayer.getObjects(),
				loader, world);
		makePillar(4, center.x + 2f, center.y + 1f, objectLayer.getObjects(),
				loader, world);
		makePillar(5, center.x + 1.5f, center.y - 1f, objectLayer.getObjects(),
				loader, world);

		// Make exit portal
		float tileX = tileLayer.getWidth() / 2;
		float tileY = tileLayer.getHeight() / 2;
		MapObject portal = new MapObject();
		portal.getProperties().put("x", (int) (tileX * TILE_WIDTH));
		portal.getProperties().put("y", (int) (tileY * TILE_HEIGHT));
		portal.getProperties().put("gid", PORTAL_ID);
		portal.getProperties().put("type", "portal");
		portal.setName("portal");
		portal.setVisible(false);

		objectLayer.getObjects().add(portal);
	}

	/**
	 * Create an npc object on the map
	 * 
	 * @param pillarNum
	 *            - the pillar this npc controls
	 * @param tileX
	 *            - npc x pos in tiles
	 * @param tileY
	 *            - npc y pos in tiles
	 * @param objs
	 *            - the maps objects
	 * @param loader
	 *            - body loader to load body
	 * @param world
	 *            - physics world
	 */
	private void makeNPC(String name, boolean hasKey, int pillarNum,
			float tileX, float tileY, MapObjects objs, BodyEditorLoader loader,
			World world) {

		MapObject npc = new MapObject();

		npc.getProperties().put("x", (int) (tileX * TILE_WIDTH));
		npc.getProperties().put("y", (int) (tileY * TILE_HEIGHT));
		npc.getProperties().put("gid", NPC_ID);
		npc.getProperties().put("type", "npc");
		npc.getProperties().put("used", false);
		npc.getProperties().put("pillarNum", pillarNum);
		npc.getProperties().put("hasKey", hasKey);
		npc.setName(name);
		BodyFactory
				.createBody(world, "tile", BodyType.StaticBody, tileX, tileY);

		objs.add(npc);

	}

	/**
	 * Create a pillar object on the map
	 * 
	 * @param pillarNum
	 *            - the number of this pillar
	 * @param tileX
	 *            - pillar x pos in tiles
	 * @param tileY
	 *            - pillar y pos in tiles
	 * @param objs
	 *            - the maps objects
	 * @param loader
	 *            - body loader to load body
	 * @param world
	 *            - physics world
	 */
	private void makePillar(int pillarNum, float tileX, float tileY,
			MapObjects objs, BodyEditorLoader loader, World world) {
		MapObject pillarTop = new MapObject();
		MapObject pillarBottom = new MapObject();

		pillarTop.getProperties().put("x", (int) (tileX * TILE_WIDTH));
		pillarTop.getProperties().put("y", (int) ((tileY + 1) * TILE_HEIGHT));
		pillarTop.getProperties().put("pillarNum", pillarNum);
		pillarTop.getProperties().put("gid", PILLAR_OFF_TOP_ID);

		pillarBottom.getProperties().put("x", (int) (tileX * TILE_WIDTH));
		pillarBottom.getProperties().put("y", (int) (tileY * TILE_HEIGHT));
		pillarBottom.getProperties().put("gid", PILLAR_OFF_BOTTOM_ID);
		pillarBottom.getProperties().put("pillarNum", pillarNum);
		BodyFactory.createBody(world, "pillar", BodyType.StaticBody, tileX,
				tileY);

		objs.add(pillarBottom);
		objs.add(pillarTop);
	}
}
