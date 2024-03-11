package com.tomesz.game.input;

public interface InputListener {
    public void keyPressed(final InputManager manager, final GameKeys key);
    public void keyUp(final InputManager manager, final GameKeys key);
}
