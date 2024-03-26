package com.tomesz.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class FireballComponent implements Component, Pool.Poolable{
    Vector2 direction;

    @Override
    public void reset() {
        direction = null;
    }

    public Vector2 getDirection() {
        return new Vector2(direction.x, direction.y);
    }

    public void setDirection(Vector2 direction) {
        this.direction = direction;
    }
}
