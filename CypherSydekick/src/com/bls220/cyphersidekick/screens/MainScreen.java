package com.bls220.cyphersidekick.screens;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.bls220.cyphersidekick.MySidekick;
import com.bls220.cyphersidekick.entities.Entity;
import com.bls220.cyphersidekick.entities.Player;
import com.bls220.cyphersidekick.mapstuff.C;
import com.bls220.cyphersidekick.mapstuff.MapActions;
import com.bls220.cyphersidekick.mapstuff.level.Level;

public class MainScreen implements Screen, GestureListener, ContactListener {

	private static final String TAG = "MainScreen";

	private final TiledMap map;
	private final MapActions acts;
	private final MySidekick parent;

	private final Box2DDebugRenderer debugBox2DRenderer;

	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private float lastZoomDistance;

	private ArrayList<Entity> tmpDelList = new ArrayList<Entity>();

	Player mPlayer;

	private Stage stage;
	private Skin skin;
	private Touchpad mJoystick[];

	private static final float DEADZONE_RADIUS = 10f;

	public void createUI() {
		stage = new Stage();

		// A skin can be loaded via JSON or defined programmatically, either is
		// fine. Using a skin is optional but strongly
		// recommended solely for the convenience of getting a texture, region,
		// etc as a drawable, tinted drawable, etc.
		skin = parent.getDefaultSkin();

		// Create Controls
		mJoystick = new Touchpad[] { new Touchpad(DEADZONE_RADIUS, skin),
				new Touchpad(DEADZONE_RADIUS, skin) };
		float w = MySidekick.SCREEN_WIDTH / 7f;
		mJoystick[0].setBounds(20, 20, w, w);
		mJoystick[1].setBounds(MySidekick.SCREEN_WIDTH - w, 20, w, w);

		stage.addActor(mJoystick[0]);
		stage.addActor(mJoystick[1]);
	}

	public MainScreen(Level lvl) {
		this.map = lvl.map;
		this.acts = lvl.acts;
		this.parent = lvl.parent;
		Entity.setup(map);
		if (MySidekick.DEBUG_MODE) {
			debugBox2DRenderer = new Box2DDebugRenderer();
			debugBox2DRenderer.setDrawContacts(true);
		} else {
			debugBox2DRenderer = null;
		}
	}

	@Override
	public void render(float delta) {
		// step
		MySidekick.getWorld().step(Math.min(delta, 1 / 30f), 8, 3);
		for (Entity e : Entity.mEntities) {
			// check deletion
			if (e.shouldDelete) {
				tmpDelList.add(e);
				MySidekick.getWorld().destroyBody(e.getBody());
			}
		}
		for (Entity e : tmpDelList) {
			Entity.mEntities.remove(e);
		}
		tmpDelList.clear();
		stage.act(delta);
		acts.checkRemoteMessage();

		// update player
		Vector2 heading = new Vector2();
		heading.x = mJoystick[0].getKnobPercentX();
		heading.y = mJoystick[0].getKnobPercentY();
		mPlayer.setHeading(heading.x, heading.y);

		heading.x = mJoystick[1].getKnobPercentX();
		heading.y = mJoystick[1].getKnobPercentY();
		if (heading.angle() != 0) {
			float radAngle = heading.angle() * MathUtils.degreesToRadians;
			mPlayer.setRotation(radAngle);
			mPlayer.shoot();
		}

		// Update camera
		camera.position.lerp(new Vector3(mPlayer.getX() * Entity.TILE_WIDTH,
				mPlayer.getY() * Entity.TILE_HEIGHT, 0), 0.5f);
		camera.update();

		// Draw
		renderer.setView(camera);
		renderer.getSpriteBatch().begin();
		renderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(
				C.TileLayer));
		drawObjects(C.ActionLayer);
		for (Entity e : Entity.mEntities) {
			e.draw(renderer.getSpriteBatch());
		}
		drawObjects(C.ObjectLayer);
		renderer.getSpriteBatch().end();

		if (MySidekick.DEBUG_MODE)
			debugBox2DRenderer.render(MySidekick.getWorld(), camera.combined);
		stage.draw();
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
		camera.viewportWidth = MySidekick.SCREEN_WIDTH;
		camera.viewportHeight = MySidekick.SCREEN_HEIGHT;
		stage.setViewport(MySidekick.SCREEN_WIDTH, MySidekick.SCREEN_HEIGHT,
				true);
	}

	@Override
	public void show() {
		renderer = new OrthogonalTiledMapRenderer(map);
		camera = new OrthographicCamera();

		createUI();

		mPlayer = new Player(new String[] { "man_back.png", "man_front.png",
				"man_right.png", "man_left.png" }, map, acts, 3, 15,
				MySidekick.getWorld());

		camera.position.set(mPlayer.getX() * Entity.TILE_WIDTH, mPlayer.getY()
				* Entity.TILE_HEIGHT, 0);

		GestureDetector gDetector = new GestureDetector(this);
		gDetector.setLongPressSeconds(0.25f);

		MySidekick.getWorld().setContactListener(this);

		Gdx.input.setInputProcessor(new InputMultiplexer(stage, gDetector));
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
		stage.dispose();
		if (debugBox2DRenderer != null)
			debugBox2DRenderer.dispose();
	}

	@Override
	public boolean longPress(float x, float y) {
		Vector3 touchPt = new Vector3(x, y, 0);
		camera.unproject(touchPt);
		Vector2 tileTouch = new Vector2((int) (touchPt.x / Entity.TILE_WIDTH),
				(int) (touchPt.y / Entity.TILE_HEIGHT));

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
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		Vector3 touchPt = new Vector3(x, y, 0);
		camera.unproject(touchPt);
		Vector2 tileTouch = new Vector2((int) (touchPt.x / Entity.TILE_WIDTH),
				(int) (touchPt.y / Entity.TILE_HEIGHT));

		System.out.println(String.format(
				"touched Screen: (%f, %f) World: (%f,%f) Tiles: (%f,%f)", x, y,
				touchPt.x, touchPt.y, tileTouch.x, tileTouch.y));

		int posX = (int) (mPlayer.getX() / Entity.TILE_WIDTH);
		int posY = (int) (mPlayer.getY() / Entity.TILE_HEIGHT);

		// Check for activation
		if (Math.abs(posX - tileTouch.x) <= 1
				&& Math.abs(posY - tileTouch.y) <= 1) {
			mPlayer.activate();
			return true;
		}

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
	public void beginContact(Contact contact) {
		Fixture fA = contact.getFixtureA();
		Fixture fB = contact.getFixtureB();

		Gdx.app.log(TAG, "Contact initiated.");

		if (fA.getUserData() == null && fB.getUserData() == null) {
			// No user data, nothing to do
			return;
		}

		Gdx.app.log(TAG, " + Found userdata");

		if (fA.getUserData() instanceof Entity) {
			((Entity) fA.getUserData()).onCollisionStart(contact, fB);
		}
		if (fB.getUserData() instanceof Entity) {
			((Entity) fB.getUserData()).onCollisionStart(contact, fA);
		}
	}

	@Override
	public void endContact(Contact contact) {

		if (contact == null) {
			return;
		}

		Fixture fA = contact.getFixtureA();
		Fixture fB = contact.getFixtureB();

		Gdx.app.log(TAG, "Contact ended.");

		if (fA == null || fB == null) {
			return;
		}

		if (fA.getUserData() == null && fB.getUserData() == null) {
			// No user data, nothing to do
			return;
		}

		Gdx.app.log(TAG, " + Found userdata");

		if (fA.getUserData() instanceof Entity) {
			((Entity) fA.getUserData()).onCollisionEnd(contact, fB);
		}
		if (fB.getUserData() instanceof Entity) {
			((Entity) fB.getUserData()).onCollisionEnd(contact, fA);
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		// TODO Auto-generated method stub
		return false;
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

}
