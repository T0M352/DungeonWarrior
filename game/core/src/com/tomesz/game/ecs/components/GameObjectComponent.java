package com.tomesz.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.tomesz.game.map.GameObjectType;

public class GameObjectComponent  implements Component, Pool.Poolable {
    public GameObjectType type;
    public int animationIndex;
    public Sprite sprite;
    public int health;

    public Vector2 position;

    @Override
    public void reset() {
        type = null;
        animationIndex = -1;
        sprite = null;
        health = 10;
        position = Vector2.Zero;
    }


}
