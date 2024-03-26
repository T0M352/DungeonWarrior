package com.tomesz.game.screen;

import com.badlogic.gdx.utils.ScreenUtils;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.input.GameKeys;
import com.tomesz.game.input.InputListener;
import com.tomesz.game.input.InputManager;
import com.tomesz.game.view.DeathUI;
import com.tomesz.game.view.MenuUI;

public class DeathScreen extends AbstractScreen<DeathUI> implements InputListener {
    public DeathScreen(DungeonWarrior context) {
        super(context);

    }

    @Override
    protected DeathUI getScreenUI(DungeonWarrior context) {
        return new DeathUI(context);
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
