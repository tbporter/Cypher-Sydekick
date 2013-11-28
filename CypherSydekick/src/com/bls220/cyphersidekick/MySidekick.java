package com.bls220.cyphersidekick;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.bls220.cyphersidekick.mapstuff.level.LevelHelper;
import com.bls220.cyphersidekick.screens.LoadingScreen;
import com.bls220.cyphersidekick.screens.MainScreen;
import com.bls220.cyphersidekick.screens.MenuScreen;

public class MySidekick extends Game {
	private LevelHelper lvls;
	public GameState state = GameState.LoadFirstLevel;
	private Skin skin;
	private static World world;

	public static final String TITLE = "Cypher Sidekick";
	public static final int SCREEN_WIDTH = 480;
	public static final int SCREEN_HEIGHT = 320;
	public static boolean DEBUG_MODE;

	public MySidekick(boolean debuggerConnected) {
		DEBUG_MODE = debuggerConnected;
	}

	@Override
	public void create() {
		// A skin can be loaded via JSON or defined programmatically, either is
		// fine. Using a skin is optional but strongly
		// recommended solely for the convenience of getting a texture, region,
		// etc as a drawable, tinted drawable, etc.
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		world = new World(new Vector2(0, 0), true);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		switch (state) {
		case Title:
			setScreen(new MenuScreen(this));
			state = GameState.WaitForAction;
			break;
		case WaitForAction:
			// Wait
			break;
		case LoadFirstLevel:
			setScreen(new LoadingScreen());
			lvls = new LevelHelper(null, this, world);
			lvls.loadNextLevel();
			state = GameState.WaitLoadFirstLevel;
			break;
		case WaitLoadFirstLevel:
			if (lvls.isLevelLoaded()) {
				setScreen(new MainScreen(lvls.getLoadedLevel()));
				state = GameState.Level;
			} else {
				System.out.println("Loading next level");
			}
			break;
		case Level:
			// super.render();
			// nothing special other than rendering
			break;
		case LevelJustFinished:
			System.out.println("You beat the level!");
			lvls.loadNextLevel();
			setScreen(new LoadingScreen());
			state = GameState.LevelFinished;
			break;
		case LevelFinished:
			if (lvls.isLevelLoaded()) {
				state = GameState.NextLevelReady;
			} else {
				System.out.println("Still loading next level");
			}
			break;
		case NextLevelReady:
			System.out.println("Starting level!");
			setScreen(new MainScreen(lvls.getLoadedLevel()));
			state = GameState.Level;
			break;

		default:
			throw new RuntimeException("Invalid state!");
		}
		super.render();
	}

	public void endGame() {
		state = GameState.LevelJustFinished;
	}

	public void menuChoice(String action, String... params) {
		if (action.equals("start")) {
			if (state != GameState.WaitForAction) {
				throw new RuntimeException(
						"Invalid state to call this function: " + state);
			}
			state = GameState.LoadFirstLevel;
		} else if (action.equals("quit")) {
			if (state != GameState.WaitForAction) {
				throw new RuntimeException(
						"Invalid state to call this function: " + state);
			}
			skin.dispose();
			world.dispose();
			Gdx.app.exit();
		}
	}

	/**
	 * @return the default skin for the application
	 */
	public Skin getDefaultSkin() {
		return skin;
	}

	/**
	 * @return the physics world
	 */
	public static World getWorld() {
		return world;
	}
}
