package com.karien.tacobox.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.karien.tacobox.MyTacoBox;

public class MenuScreen implements Screen {
	private final MyTacoBox parent;

	private Stage stage;
	private Skin skin;
	Label titleLabel;

	public MenuScreen(MyTacoBox callback) {
		parent = callback;
		skin = parent.getDefaultSkin();
	}

	public void createUI() {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		// Create Dialog
		Label ipLabel = new Label("Host Address: ", skin);
		final TextField ipTextField = new TextField("", skin);
		TextButton connectBtn = new TextButton("Connect", skin);
		connectBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				parent.menuChoice("join", ipTextField.getText());
			}
		});

		final Window joinDialog = new Window("Connect to...", skin);
		joinDialog.debug();
		joinDialog.getButtonTable().add(new TextButton("X", skin))
				.height(joinDialog.getPadTop());
		joinDialog.setPosition(0, 0);
		joinDialog.defaults().spaceBottom(10);
		joinDialog.row().fill().expandX();
		joinDialog.add(ipLabel);
		joinDialog.add(ipTextField);
		joinDialog.row();
		joinDialog.add(connectBtn).colspan(2).align(Align.center);
		joinDialog.pack();

		// Create a table that fills the screen. Everything else will go inside
		// this table.
		Table container = new Table();
		container.debug();
		container.align(Align.left | Align.top);
		container.setFillParent(true);
		stage.addActor(container);

		// Create labels
		titleLabel = new Label("TacoBox", skin);
		titleLabel.setFontScale(3f);
		titleLabel.setAlignment(Align.center);

		// create buttons
		TextButton quitBtn = new TextButton("Quit", skin);
		quitBtn.getLabel().setFontScale(2f);
		quitBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				throw new RuntimeException("Quit");
			}
		});

		TextButton hostBtn = new TextButton("Host", skin);
		hostBtn.getLabel().setFontScale(2f);
		hostBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				parent.menuChoice("host");
			}
		});

		TextButton joinBtn = new TextButton("Join", skin);
		joinBtn.getLabel().setFontScale(2f);
		joinBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				stage.addActor(joinDialog);
			}
		});

		container.add(titleLabel).align(Align.center).expandX();
		container.row();
		container.add(hostBtn).align(Align.center | Align.bottom).expand();
		container.row();
		container.add(joinBtn).align(Align.center).expandX();
		container.row();
		container.add(quitBtn).align(Align.center | Align.top).expand();
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(MyTacoBox.SCREEN_WIDTH, MyTacoBox.SCREEN_HEIGHT, true);
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
		skin.dispose();
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
