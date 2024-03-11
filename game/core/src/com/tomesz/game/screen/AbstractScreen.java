package com.tomesz.game.screen;

import box2dLight.RayHandler;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.input.InputListener;
import com.tomesz.game.input.InputManager;
import sun.tools.jconsole.Tab;

public abstract class AbstractScreen<T extends Table> implements Screen, InputListener {
    protected final DungeonWarrior context;
    protected final FitViewport viewport;
    protected  final World world;
    protected  final RayHandler rayHandler;
    protected final Box2DDebugRenderer box2DDebugRenderer;
    protected final T screenUI;
    protected final Stage stage;
    protected final InputManager inputManager;

    public AbstractScreen(final DungeonWarrior context){
        this.context = context;
        viewport = context.getScreenViewport();
        world = context.getWorld();
        rayHandler = context.getRayHandler();
        box2DDebugRenderer = context.getBox2DDebugRenderer();
        stage = context.getStage();
        screenUI = getScreenUI(context);
        inputManager = context.getInputManager();
    }

    protected abstract T getScreenUI(final DungeonWarrior context);

    @Override
    public void resize(final int width, final int height) {
        viewport.update(width, height);
        stage.getViewport().update(width,height,true);
        rayHandler.useCustomViewport(viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
    }

    @Override
    public void show() {
        inputManager.addInputListener(this);
        stage.addActor(screenUI);
    }

    @Override
    public void hide() {
        inputManager.removeInputListener(this);
        stage.getRoot().removeActor(screenUI);
    }
}
