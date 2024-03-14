package com.tomesz.game.screen;

import com.badlogic.gdx.utils.ScreenUtils;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.input.GameKeys;
import com.tomesz.game.input.InputListener;
import com.tomesz.game.input.InputManager;
import com.tomesz.game.view.MenuUI;

public class MenuInGame extends AbstractScreen<MenuUI> implements InputListener {
    public MenuInGame(DungeonWarrior context) {
        super(context);

    }

    @Override
    protected MenuUI getScreenUI(DungeonWarrior context) {
        return new MenuUI(context, ScreenType.MENU_IN_GAME);
    }

    @Override
    public void render(float v) {
        ScreenUtils.clear(0, 0, 0, 1);
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
}
