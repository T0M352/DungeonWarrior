package com.tomesz.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sun.org.apache.xpath.internal.operations.Or;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.input.GameKeys;
import com.tomesz.game.input.InputManager;
import com.tomesz.game.map.CollisionArea;
import com.tomesz.game.map.Map;
import com.tomesz.game.map.MapListener;
import com.tomesz.game.map.MapManager;
import com.tomesz.game.view.GameUI;

import static com.tomesz.game.DungeonWarrior.*;

public class GameScreen extends AbstractScreen<GameUI> implements MapListener {


    //private final Map map;
    //private final AssetManager assetManager;

    private final MapManager mapManager;


    java.util.Map<CollisionArea, Body> mapOfCollision;
    public GameScreen(final DungeonWarrior context) {
        super(context);

        mapManager = context.getMapManager();
        mapManager.addListener(this);
        mapManager.setupMap();


        context.getEcsEngine().createPlayer(new Vector2(mapManager.getCurrentMap().getStartLocation()));



    }



    @Override
    protected GameUI getScreenUI(DungeonWarrior context) {
        return new GameUI(context);
    }

    @Override
    public void render(float v) {
        Vector3 mouseAtScreen = new Vector3(Gdx.input.getX(),  Gdx.input.getY(), 0);
        Vector2 mouseAtWorld = getWorldPositionFromScreen(mouseAtScreen);
//        Gdx.app.debug("MYSZ", "pozycja myszy w swiecie gry x: " + mouseAtWorld.x + " y: " + mouseAtWorld.y);
    }

    private Vector2 getWorldPositionFromScreen(Vector3 mouseAtScreen) {
        OrthographicCamera orthographicCamera = context.getGameCamera();
        Vector3 vc = orthographicCamera.unproject(new Vector3(mouseAtScreen.x,   mouseAtScreen.y, mouseAtScreen.z));
        return new Vector2(vc.x, vc.y);
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }


    @Override
    public void dispose() {

    }

    @Override
    public void keyPressed(InputManager manager, GameKeys key) {

    }

    @Override
    public void keyUp(InputManager manager, GameKeys key) {

    }

    @Override
    public void mapChange(Map map) {

    }
}
