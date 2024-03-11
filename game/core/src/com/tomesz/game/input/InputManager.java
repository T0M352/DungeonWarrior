package com.tomesz.game.input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;

public class InputManager implements InputProcessor {
    private final GameKeys[] keyMapping;
    private final boolean[] keyStates;
    private final Array<InputListener> listeners;

    public InputManager() {
        this.keyMapping = new GameKeys[256];
        for(final GameKeys gameKey: GameKeys.values()){
            for(final int code:gameKey.keyCode){
                keyMapping[code] = gameKey;
            }
        }
        keyStates = new boolean[GameKeys.values().length];
        listeners = new Array<InputListener>();
    }

    public void addInputListener(final InputListener listener){
        listeners.add(listener);
    }

    public void removeInputListener(final InputListener listener){
        listeners.removeValue(listener, true);
    }

    @Override
    public boolean keyDown(final int keycode) {
        final GameKeys gameKey = keyMapping[keycode];
        if(gameKey==null){
            //wcisniety przycisk nie jest funkcyjny
            return false;
        }
        notifyKeyDown(gameKey);


        return false;
    }

    public void notifyKeyDown(GameKeys gameKey) {
        keyStates[gameKey.ordinal()] = true;
        for(final InputListener listner : listeners){
            listner.keyPressed(this, gameKey);
        }
    }

    @Override
    public boolean keyUp(final int keycode) {
        final GameKeys gameKey = keyMapping[keycode];
        if(gameKey==null){
            //wcisniety przycisk nie jest funkcyjny
            return false;
        }
        notifyKeyUp(gameKey);


        return false;
    }

    private void notifyKeyUp(GameKeys gameKey) {
        keyStates[gameKey.ordinal()] = false;
        for(final InputListener listner : listeners){
            listner.keyUp(this, gameKey);
        }
    }

    public boolean isKeyPressed(final GameKeys gameKey){
        return keyStates[gameKey.ordinal()];
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        return false;
    }
}
