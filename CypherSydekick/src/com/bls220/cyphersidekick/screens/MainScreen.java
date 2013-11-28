package com.bls220.cyphersidekick.screens;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.bls220.cyphersidekick.GameState;
import com.bls220.cyphersidekick.MySidekick;
import com.bls220.cyphersidekick.entities.Enemy;
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

	private final FPSLogger fpsLogger;

	private Box2DDebugRenderer debugBox2DRenderer;
	private OrthogonalTiledMapRenderer renderer;

	private OrthographicCamera camera;
	private float lastZoomDistance;

	private ArrayList<Entity> tmpDelList = new ArrayList<Entity>();

	Player mPlayer;

	private Stage stage;
	private Skin skin;
	private Touchpad mJoystick[];
	private Label msgBox;

	private TextureRegion healthBarTR;
	private TextureRegion healthMaskTR;

	private static final float DEADZONE_RADIUS = 10f;

	public void createUI() {
		stage = new Stage(MySidekick.SCREEN_WIDTH, MySidekick.SCREEN_HEIGHT,
				true);

		// A skin can be loaded via JSON or defined programmatically, either is
		// fine. Using a skin is optional but strongly
		// recommended solely for the convenience of getting a texture, region,
		// etc as a drawable, tinted drawable, etc.
		skin = parent.getDefaultSkin();

		Table table = new Table(skin);
		table.setFillParent(true);
		table.align(Align.bottom | Align.left);
		// table.debug();
		stage.addActor(table);

		// Create Controls
		mJoystick = new Touchpad[] { new Touchpad(DEADZONE_RADIUS, skin),
				new Touchpad(DEADZONE_RADIUS, skin) };
		float w = MySidekick.SCREEN_WIDTH / 7f;
		mJoystick[0].setSize(w, w);
		mJoystick[1].setSize(w, w);

		// Create message area
		msgBox = new Label("Stuff Goes Here.", skin);
		msgBox.getStyle().background = skin.getDrawable("msgBack");
		msgBox.setAlignment(Align.center);

		// populate table
		table.add(mJoystick[0]).align(Align.bottom).size(w).pad(20);
		table.add(msgBox).expandX().fill();
		table.add(mJoystick[1]).align(Align.bottom).size(w).pad(20);
	}

	public MainScreen(Level lvl) {
		this.map = lvl.map;
		this.acts = lvl.acts;
		this.parent = lvl.parent;
		this.fpsLogger = new FPSLogger();
	}

	public void setMsgBoxText(CharSequence msg) {
		msgBox.setText(msg);
	}

	@Override
	public void render(float delta) {
		fpsLogger.log();
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

		// Check for player death
		if (mPlayer.getHealth() <= 0) {
			// TODO: trigger death
			parent.state = GameState.Title;
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
			if (e instanceof Enemy) {
				// Draw Health Bars
				float startX = e.getX() * Entity.TILE_WIDTH;
				float startY = (e.getY() + 1) * Entity.TILE_HEIGHT + 5;
				renderer.getSpriteBatch().draw(healthBarTR, startX, startY,
						Entity.TILE_WIDTH, 5);
				renderer.getSpriteBatch().draw(
						healthMaskTR,
						startX,
						startY,
						(1 - ((Enemy) e).getHealth() / Enemy.MAX_HEALTH)
								* Entity.TILE_WIDTH, 5);
			}
		}
		drawObjects(C.ObjectLayer);
		renderer.getSpriteBatch().end();

		if (MySidekick.DEBUG_MODE)
			debugBox2DRenderer.render(MySidekick.getWorld(), camera.combined
					.cpy().scl(Entity.TILE_WIDTH));
		stage.draw();
		Table.drawDebug(stage);
	}

	public void drawObjects(String layerID) {
		for (MapObject obj : map.getLayers().get(layerID).getObjects()) {
			MapProperties props = obj.getProperties();
			if (obj.isVisible()) {
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
		if (MySidekick.DEBUG_MODE) {
			debugBox2DRenderer = new Box2DDebugRenderer();
			debugBox2DRenderer.setDrawContacts(true);
		} else {
			debugBox2DRenderer = null;
		}

		createUI();

		healthBarTR = map.getTileSets().getTile(26).getTextureRegion();
		healthMaskTR = map.getTileSets().getTile(25).getTextureRegion();

		int mapWidth = ((TiledMapTileLayer) map.getLayers().get(0)).getWidth();
		int mapHeight = ((TiledMapTileLayer) map.getLayers().get(0))
				.getHeight();

		mPlayer = new Player(new String[] { "man_back.png", "man_front.png",
				"man_right.png", "man_left.png" }, map, acts, mapWidth / 2,
				mapHeight / 2, MySidekick.getWorld());

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
		healthBarTR.getTexture().dispose();
		healthMaskTR.getTexture().dispose();
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
		int posX = (int) (mPlayer.getX() + 0.5f);
		int posY = (int) (mPlayer.getY() + 0.5f);
		Vector2 diffPos = new Vector2(Math.abs(posX - tileTouch.x),
				Math.abs(posY - tileTouch.y));
		Gdx.app.log(TAG, String.format("Distance from Touch: (%f,%f)",
				diffPos.x, diffPos.y));
		if (diffPos.x <= 1 && diffPos.y <= 1) {
			Gdx.app.log(TAG, "Activating...");
			mPlayer.activate(tileTouch, map);
			return true;
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
