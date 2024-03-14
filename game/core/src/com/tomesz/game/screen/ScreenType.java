package com.tomesz.game.screen;

import com.badlogic.gdx.Screen;

public enum ScreenType {
    GAME(GameScreen.class),
    LOADING(LoadingScreen.class),
    MENU(MenuScreen.class),
    MENU_IN_GAME(MenuInGame.class);
    private final Class<?extends AbstractScreen> screenClass;

    ScreenType(final Class<? extends AbstractScreen> screenClass){
        this.screenClass = screenClass;
    }

    public Class<? extends Screen> getScreenClass() {
        return screenClass;
    }
}
