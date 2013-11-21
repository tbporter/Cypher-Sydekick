package com.karien.tacobox.screens;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.karien.taco.mapstuff.C;
import com.karien.taco.mapstuff.MapActions;
import com.karien.taco.mapstuff.level.Level;
import com.karien.tacobox.MyTacoBox;
import com.karien.tacobox.entities.Player;
import com.karien.tacobox.entities.Player.EFacing;

public class MainScreen implements Screen, GestureListener {

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

		this.callback.getWorld().step(delta, 10, 10);

		camera.position.lerp(new Vector3(mPlayer.getX(), mPlayer.getY(), 0),
				0.5f);
		camera.update();

		renderer.setView(camera);

		renderer.getSpriteBatch().begin();
		renderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(
				C.TileLayer));
		drawObjects(C.ActionLayer);
		mPlayer.draw(renderer.getSpriteBatch());
		drawObjects(C.ObjectLayer);
		renderer.getSpriteBatch().end();

		// if (mPlayer.getX() == (Integer) map.getProperties().get("goalX")
		// && mPlayer.getY() == (Integer) map.getProperties().get("goalY")) {
		// callback.goalReached();
		// }

		new Box2DDebugRenderer().render(this.callback.getWorld(),
				camera.combined);
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
				.get(C.SpawnY), this.callback.getWorld());

		camera.position.set(mPlayer.getX() * mPlayer.TILE_WIDTH, mPlayer.getY()
				* mPlayer.TILE_HEIGHT, 0);

		GestureDetector gDetector = new GestureDetector(this);
		gDetector.setLongPressSeconds(0.25f);

		Gdx.input.setInputProcessor(gDetector);
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
		// TODO Auto-generated method stub
		return false;
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
			mPlayer.grab();
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
		Vector3 touchPt = new Vector3(x, y, 0);
		camera.unproject(touchPt);
		Vector2 tileTouch = new Vector2((int) (touchPt.x / mPlayer.TILE_WIDTH),
				(int) (touchPt.y / mPlayer.TILE_HEIGHT));

		System.out.println(String.format(
				"touched Screen: (%f, %f) World: (%f,%f) Tiles: (%f,%f)", x, y,
				touchPt.x, touchPt.y, tileTouch.x, tileTouch.y));

		int posX = (int) (mPlayer.getX() / mPlayer.TILE_WIDTH);
		int posY = (int) (mPlayer.getY() / mPlayer.TILE_HEIGHT);

		// Check for activation
		if (Math.abs(posX - tileTouch.x) <= 1
				&& Math.abs(posY - tileTouch.y) <= 1) {
			mPlayer.activate();
			return true;
		}

		// translate touch into movement
		Vector2 dir = new Vector2();

		dir = tileTouch.sub(new Vector2(posX, posY));

		if (dir.y != 0) {
			if (dir.y >= 1) {
				mPlayer.setFacing(EFacing.N);
			} else {
				mPlayer.setFacing(EFacing.S);
			}
		} else if (dir.x != 0) {
			if (dir.x >= 1) {
				mPlayer.setFacing(EFacing.E);
			} else {
				mPlayer.setFacing(EFacing.W);
			}
		}

		mPlayer.setHeading(dir.x, dir.y);

		return true;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		mPlayer.setHeading(0, 0);
		return true;
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

}
