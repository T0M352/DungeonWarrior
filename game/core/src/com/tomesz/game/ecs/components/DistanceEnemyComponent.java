package com.tomesz.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class DistanceEnemyComponent implements Component, Pool.Poolable {
    public float shootTimer = 0;

    public float shootSerieDelay = 3;
    public int shootInRows = 0;

    public boolean shoot;
    @Override
    public void reset() {
        shootTimer = 0;
        shootInRows = 0;
        shootSerieDelay = 3;
        shoot = false;
    }
}
