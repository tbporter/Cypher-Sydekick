package com.karien.taco.mapstuff.level;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.karien.taco.mapstuff.C;
import com.karien.tacobox.MyTacoBox;
import com.karien.tacobox.comm.MsgHandler;

public class RandomLevel extends Level {

	private final static String EMPTY_MAP_PATH = "maps/blank.tmx";

	public RandomLevel(MyTacoBox parent, MsgHandler remote) {
		super(parent, EMPTY_MAP_PATH, remote);
		generateLevel(map.getTileSets(), (TiledMapTileLayer) map.getLayers()
				.get(C.TileLayer));
	}

	private void generateLevel(TiledMapTileSets tileSets,
			TiledMapTileLayer tileLayer) {
		Cell dirtCell = new Cell();
		dirtCell.setTile(tileSets.getTile(829));

		for (int y = 0; y < tileLayer.getHeight(); y++) {
			for (int x = 0; x < tileLayer.getWidth(); x++) {
				tileLayer.setCell(x, y, dirtCell);
			}
		}
	}
}
