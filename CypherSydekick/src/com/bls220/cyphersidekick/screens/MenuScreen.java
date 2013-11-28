package com.bls220.cyphersidekick.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.bls220.cyphersidekick.MySidekick;

public class MenuScreen implements Screen {
	private final MySidekick parent;

	private Stage stage;
	private Skin skin;
	Label titleLabel;

	public MenuScreen(MySidekick callback) {
		parent = callback;
		skin = parent.getDefaultSkin();
	}

	public void createUI() {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		// Create a table that fills the screen. Everything else will go inside
		// this table.
		Table container = new Table();
		container.debug();
		container.align(Align.left | Align.top);
		container.setFillParent(true);
		stage.addActor(container);

		// Create labels
		titleLabel = new Label(MySidekick.TITLE, skin);
		titleLabel.setFontScale(3f);
		titleLabel.setAlignment(Align.center);

		// create buttons
		TextButton quitBtn = new TextButton("Quit", skin);
		quitBtn.getLabel().setFontScale(2f);
		quitBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				parent.menuChoice("quit");
			}
		});

		TextButton startBtn = new TextButton("Start", skin);
		startBtn.getLabel().setFontScale(2f);
		startBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				parent.menuChoice("start");
			}
		});

		container.add(titleLabel).align(Align.center).expandX();
		container.row();
		container.add(startBtn).align(Align.center | Align.bottom).expand();
		container.row();
		container.add(quitBtn).align(Align.center | Align.top).expand();
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(MySidekick.SCREEN_WIDTH, MySidekick.SCREEN_HEIGHT,
				true);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();

		// Window.drawDebug(stage);
		// Table.drawDebug(stage); // This is optional, but enables debug lines
		// for tables.
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	@Override
	public void show() {
		createUI();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

}
