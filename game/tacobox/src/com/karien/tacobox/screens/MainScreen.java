package com.karien.tacobox.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.karien.taco.mapstuff.C;
import com.karien.taco.mapstuff.MapActions;
import com.karien.tacobox.MyTacoBox;
import com.karien.tacobox.entities.Player;
import com.karien.tacobox.entities.Player.EFacing;

public class MainScreen implements Screen, InputProcessor, GestureListener {

	private final TiledMap map;
	private final MapActions acts;
	private final MyTacoBox callback;

	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private float lastZoomDistance;

	Player mPlayer;

	public MainScreen(Level lvl) {
		this.map = lvl.map;
		this.acts = lvl.acts;
		this.callback = lvl.parent;
	}

	@Override
	public void render(float delta) {
		camera.position.lerp(new Vector3(mPlayer.getX() * mPlayer.TILE_WIDTH,
				mPlayer.getY() * mPlayer.TILE_HEIGHT, 0), 0.5f);
		camera.update();

		renderer.setView(camera);

		renderer.getSpriteBatch().begin();
		renderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(
				C.TileLayer));
		drawObjects(C.ActionLayer);
		mPlayer.draw(renderer.getSpriteBatch());
		drawObjects(C.ObjectLayer);
		renderer.getSpriteBatch().end();

		if (mPlayer.getX() == (Integer) map.getProperties().get("goalX")
				&& mPlayer.getY() == (Integer) map.getProperties().get("goalY")) {
			callback.goalReached();
		}

		acts.checkRemoteMessage();
	}

	public void drawObjects(String layerID) {
		for (MapObject obj : map.getLayers().get(layerID).getObjects()) {
			MapProperties props = obj.getProperties();
			if ((Boolean) props.get(C.Visible)) {
				int gid = (Integer) props.get("gid");
				TiledMapTile tile = map.getTileSets().getTile(gid);
				renderer.getSpriteBatch().draw(tile.getTextureRegion(),
						(Integer) props.get("x"), (Integer) props.get("y"));
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = MyTacoBox.SCREEN_WIDTH;
		camera.viewportHeight = MyTacoBox.SCREEN_HEIGHT;
	}

	@Override
	public void show() {
		renderer = new OrthogonalTiledMapRenderer(map);
		camera = new OrthographicCamera();

		mPlayer = new Player(new String[] { "man_back.png", "man_front.png",
				"man_right.png", "man_left.png" }, map, acts, (Integer) map
				.getProperties().get(C.SpawnX), (Integer) map.getProperties()
				.get(C.SpawnY));

		camera.position.set(mPlayer.getX() * mPlayer.TILE_WIDTH, mPlayer.getY()
				* mPlayer.TILE_HEIGHT, 0);

		GestureDetector gDetector = new GestureDetector(this);
		gDetector.setLongPressSeconds(0.25f);

		Gdx.input.setInputProcessor(new InputMultiplexer(gDetector, this));
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		map.dispose();
		renderer.dispose();
		mPlayer.dispose();
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		Vector3 touchPt = new Vector3(x, y, 0);
		camera.unproject(touchPt);
		Vector2 tileTouch = new Vector2((int) (touchPt.x / mPlayer.TILE_WIDTH),
				(int) (touchPt.y / mPlayer.TILE_HEIGHT));

		System.out.println(String.format(
				"touched Screen: (%f, %f) World: (%f,%f) Tiles: (%f,%f)", x, y,
				touchPt.x, touchPt.y, tileTouch.x, tileTouch.y));

		// Check for activation
		if (Math.abs(mPlayer.getX() - tileTouch.x) <= 1
				&& Math.abs(mPlayer.getY() - tileTouch.y) <= 1) {
			mPlayer.activate();
			return true;
		}

		// translate touch into movement
		Vector2 dir = new Vector2();
		if (mPlayer.getX() - tileTouch.x > 1) {
			dir.x = -1;
		} else if (tileTouch.x - mPlayer.getX() > 1) {
			dir.x = 1;
		} else if (mPlayer.getY() - tileTouch.y > 1) {
			dir.y = 1;
		} else if (tileTouch.y - mPlayer.getY() > 1) {
			dir.y = -1;
		}

		if (dir.y != 0) {
			if (dir.y == 1) {
				keyDown(Keys.DOWN);
			} else {
				keyDown(Keys.UP);
			}
		} else if (dir.x != 0) {
			if (dir.x == 1) {
				keyDown(Keys.RIGHT);
			} else {
				keyDown(Keys.LEFT);
			}
		}

		return true;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		Vector3 touchPt = new Vector3(x, y, 0);
		camera.unproject(touchPt);
		Vector2 tileTouch = new Vector2((int) (touchPt.x / mPlayer.TILE_WIDTH),
				(int) (touchPt.y / mPlayer.TILE_HEIGHT));

		System.out.println(String.format(
				"long press Screen: (%f, %f) World: (%f,%f) Tiles: (%f,%f)", x,
				y, touchPt.x, touchPt.y, tileTouch.x, tileTouch.y));

		// Check for activation
		if (Math.abs(mPlayer.getX() - tileTouch.x) <= 1
				&& Math.abs(mPlayer.getY() - tileTouch.y) <= 1) {
			keyDown(Keys.SPACE);
		}
		return true;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		if (Math.abs(initialDistance - distance) < 20) {
			lastZoomDistance = distance;
		}

		if (lastZoomDistance != distance) {
			camera.zoom -= (distance - lastZoomDistance) / 1000f;
			if (camera.zoom < 0) {
				camera.zoom = -camera.zoom;
			}
		}

		lastZoomDistance = distance;
		return true;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		Vector2 velocity = mPlayer.getHeading();
		switch (keycode) {
		case Keys.W:
		case Keys.UP:
			// Move up
			velocity.y = 1;
			mPlayer.setFacing(EFacing.N);
			break;
		case Keys.A:
		case Keys.LEFT:
			// Move left
			velocity.x = -1;
			mPlayer.setFacing(EFacing.W);
			break;
		case Keys.S:
		case Keys.DOWN:
			// Move down
			velocity.y = -1;
			mPlayer.setFacing(EFacing.S);
			break;
		case Keys.D:
		case Keys.RIGHT:
			// Move right
			velocity.x = 1;
			mPlayer.setFacing(EFacing.E);
			break;
		case Keys.E:
			// Action
			mPlayer.activate();
			break;
		case Keys.SPACE:
			// Grab
			mPlayer.grab();
			break;
		}
		mPlayer.setVelocity(velocity.x, velocity.y);
		System.out.println("Player Heading: " + mPlayer.getHeading());
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		Vector2 heading = mPlayer.getHeading();
		switch (keycode) {
		case Keys.W:
		case Keys.UP:
		case Keys.S:
		case Keys.DOWN:
			// Stop vertical movement
			mPlayer.setVelocity(heading.x, 0);
			break;
		case Keys.A:
		case Keys.LEFT:
		case Keys.D:
		case Keys.RIGHT:
			// Stop horizontal movement
			mPlayer.setVelocity(0, heading.y);
			break;
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		keyUp(Keys.UP);
		keyUp(Keys.DOWN);
		keyUp(Keys.LEFT);
		keyUp(Keys.RIGHT);
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		float zoom = lastZoomDistance + 50 * amount;
		System.out.println("Mouse Scroll: " + zoom);
		return zoom(0, zoom);
	}

}
