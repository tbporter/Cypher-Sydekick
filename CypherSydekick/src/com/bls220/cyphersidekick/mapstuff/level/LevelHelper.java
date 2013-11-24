package com.bls220.cyphersidekick.mapstuff.level;

import java.io.IOException;

import com.badlogic.gdx.physics.box2d.World;
import com.bls220.cyphersidekick.MySidekick;
import com.bls220.cyphersidekick.comm.MsgHandler;
import com.bls220.cyphersidekick.mapstuff.map.MapID;

public class LevelHelper {
	private final MsgHandler msg;
	private final MySidekick listen;
	private final World world;

	/**
	 * Lock must be held to access this variable.
	 */
	private Level nextLevel;
	private boolean loading;

	public void loadNextLevel() {
		if (loading) {
			throw new RuntimeException("Already loading a level!");
		}
		loading = true;
		new lvlLoader(nextMapPath()).run();
		// new Thread(new lvlLoader("maps/lightTest.tmx")).start();
	}

	private String nextMapPath() {
		if (msg == null) {
			return MapID.Test.getPath(true);
		}

		try {
			return msg.syncAndGetMapPath();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public synchronized boolean isLevelLoaded() {
		if (!loading) {
			throw new RuntimeException("Not currently loading a level!");
		}
		return nextLevel != null;
	}

	public synchronized Level getLoadedLevel() {
		if (!loading) {
			throw new RuntimeException("Not currently loading a level!");
		} else if (nextLevel == null) {
			throw new RuntimeException("Level not finished loading!");
		}
		loading = false;
		return nextLevel;
	}

	public LevelHelper(MsgHandler msg, MySidekick listen, World world) {
		this.msg = msg;
		this.listen = listen;
		this.world = world;
	}

	private class lvlLoader implements Runnable {
		private final String path;

		lvlLoader(String path) {
			this.path = path;
		}

		@Override
		public void run() {
			Level ll = new RandomLevel(listen, msg, world);

			synchronized (LevelHelper.this) {
				nextLevel = ll;
			}
		}

	}
}
