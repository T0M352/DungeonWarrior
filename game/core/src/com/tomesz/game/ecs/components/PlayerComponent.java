package com.tomesz.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class PlayerComponent implements Component, Pool.Poolable {
    public static int diamonds;

    public int getDiamonds() {
        return diamonds;
    }

    public void addDiamond(){
        diamonds++;
    }

    @Override
    public void reset() {
        //tutaj resetujesz wlasciwosci do podstawowych
    }
}
