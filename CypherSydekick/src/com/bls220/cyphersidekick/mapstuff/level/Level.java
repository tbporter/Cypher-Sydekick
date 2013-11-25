package com.bls220.cyphersidekick.mapstuff.level;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.bls220.cyphersidekick.MySidekick;
import com.bls220.cyphersidekick.comm.MsgHandler;
import com.bls220.cyphersidekick.entities.Entity;
import com.bls220.cyphersidekick.mapstuff.MapActions;

public class Level {
	public final TiledMap map;
	public final MapActions acts;
	public final MySidekick parent;

	public Level(MySidekick parent, String mapPath, MsgHandler remote) {
		map = new TmxMapLoader().load(mapPath);
		acts = MapActions.procActions(map, remote);
		Entity.setup(map);
		this.parent = parent;
	}
}
