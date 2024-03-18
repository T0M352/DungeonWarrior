package com.tomesz.game.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.input.GameKeys;
import com.tomesz.game.input.InputManager;
import com.tomesz.game.view.LoadingUI;

public class LoadingScreen extends AbstractScreen<LoadingUI> {
    private final AssetManager assetManager;
    public LoadingScreen(final DungeonWarrior context) {
        super(context) ;

        assetManager = context.getAssetManager();
        assetManager.load("mage/mage.atlas", TextureAtlas.class);

        assetManager.load("dungeon/map.tmx", TiledMap.class);


    }

    @Override
    protected LoadingUI getScreenUI(DungeonWarrior context) {
        return new LoadingUI(context);
    }

    @Override
    public void render(float v) {
        ScreenUtils.clear(0, 0, 0, 1);


        assetManager.update();
        screenUI.setProgress(assetManager.getProgress());
    }

    @Override
    public void keyPressed(InputManager manager, GameKeys key) {
        if(assetManager.getProgress() >= 1){
            context.setScreen(ScreenType.GAME);
        }
    }

    @Override
    public void keyUp(InputManager manager, GameKeys key) {

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
}
