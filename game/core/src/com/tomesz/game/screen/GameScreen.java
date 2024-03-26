package com.tomesz.game.screen;

import com.badlogic.ashley.core.Entity;
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
import com.tomesz.game.PreferenceManager;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.PlayerComponent;
import com.tomesz.game.input.GameKeys;
import com.tomesz.game.input.InputManager;
import com.tomesz.game.map.*;
import com.tomesz.game.view.GameUI;

import static com.tomesz.game.DungeonWarrior.*;

public class GameScreen extends AbstractScreen<GameUI> implements MapListener {


    //private final Map map;
    //private final AssetManager assetManager;

    private final MapManager mapManager;

    private final PreferenceManager preferenceManager;
    private final Entity player;

    private float timer = 0;

    private boolean debugMode;


    public Entity getPlayer() {
        return player;
    }

    java.util.Map<CollisionArea, Body> mapOfCollision;
    public GameScreen(final DungeonWarrior context) {
        super(context);

        mapManager = context.getMapManager();
        mapManager.addListener(this);
        mapManager.setupMap();

        preferenceManager = context.getPreferenceManager();

        player = context.getEcsEngine().createPlayer(new Vector2(mapManager.getCurrentMap().getStartLocation()));
        if(context.loadGame){
            preferenceManager.loadGameState(player);
            screenUI.refreshDiamonds(player);
            screenUI.refreshHealth((int)player.getComponent(PlayerComponent.class).health);
        }


    }


    private Vector2 getWorldPositionFromScreen(Vector3 v) {
        Vector3 vc = context.getGameCamera().unproject(new Vector3(v.x,   v.y, v.z));
        return new Vector2(vc.x, vc.y);
    }

    @Override
    protected GameUI getScreenUI(DungeonWarrior context) {
        return new GameUI(context);
    }

    @Override
    public void render(float v) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)){
            debugMode = true;
            screenUI.showDebugInfo();
        }

        if(debugMode){
            if(Gdx.input.isKeyJustPressed(Input.Keys.Z)){
                preferenceManager.saveGameState(player, context.getMapManager());
            }
            else if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
                preferenceManager.loadGameState(player);
                screenUI.refreshDiamonds(player);
                screenUI.refreshHealth((int)player.getComponent(PlayerComponent.class).health);
            }
            else if(Gdx.input.isKeyJustPressed(Input.Keys.C)){
                player.getComponent(PlayerComponent.class).health = 100;
                screenUI.refreshHealth(player.getComponent(PlayerComponent.class).health);
            }
            else if(Gdx.input.isKeyJustPressed(Input.Keys.V)){
                player.getComponent(PlayerComponent.class).mana=99;
                screenUI.refreshHealth(player.getComponent(PlayerComponent.class).health);
            }
            else if(Gdx.input.isKeyJustPressed(Input.Keys.B)){
                Vector3 mouseAtScreen = new Vector3(Gdx.input.getX(),  Gdx.input.getY(), 0);
                Vector2 mouseAtWorld = getWorldPositionFromScreen(mouseAtScreen);
                Vector2 dir = new Vector2(mouseAtWorld.x - 0.125f, mouseAtWorld.y - 0.125f);
                context.getEcsEngine().createSampleObject(dir, context.getMapManager().getBox(), GameObjectType.BOX, BIT_DESTROYABLE);
            }

            else if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
                Vector3 mouseAtScreen = new Vector3(Gdx.input.getX(),  Gdx.input.getY(), 0);
                Vector2 mouseAtWorld = getWorldPositionFromScreen(mouseAtScreen);
                Vector2 dir = new Vector2(mouseAtWorld.x - 0.125f, mouseAtWorld.y - 0.125f);
                context.getEcsEngine().createDistEnemy(dir);
            }

            else if(Gdx.input.isKeyJustPressed(Input.Keys.N)){
                Vector3 mouseAtScreen = new Vector3(Gdx.input.getX(),  Gdx.input.getY(), 0);
                Vector2 mouseAtWorld = getWorldPositionFromScreen(mouseAtScreen);
                Vector2 dir = new Vector2(mouseAtWorld.x - 0.125f, mouseAtWorld.y - 0.125f);
                context.getEcsEngine().createEnemy(dir);
            }

            else if(Gdx.input.isKeyJustPressed(Input.Keys.H)){
                Vector3 mouseAtScreen = new Vector3(Gdx.input.getX(),  Gdx.input.getY(), 0);
                Vector2 mouseAtWorld = getWorldPositionFromScreen(mouseAtScreen);
                Vector2 dir = new Vector2(mouseAtWorld.x - 0.125f, mouseAtWorld.y - 0.125f);
                context.getMapManager().createPath(dir);
            }
        }





        timer += v; // Inkrementacja timera o czas od ostatniej klatki
        if (timer >= 0.1) {
            //            Gdx.app.debug("CZAS", "MANA: " + player.getComponent(PlayerComponent.class).mana);
            if(player.getComponent(PlayerComponent.class).mana <= 100){
                player.getComponent(PlayerComponent.class).addMana(1);
                screenUI.refreshMana((int)player.getComponent(PlayerComponent.class).mana);
                timer = 0;
            }
            if(player.getComponent(PlayerComponent.class).health <= 100){
                screenUI.refreshHealth((int)player.getComponent(PlayerComponent.class).health);
                timer = 0;
            }

        }


    }



    public interface PlayerManaChangeListener{
        void ManaChange(int manaCount);
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


