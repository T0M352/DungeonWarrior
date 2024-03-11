package com.tomesz.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Pool;
import com.tomesz.game.map.GameObjectType;

public class GameObjectComponent  implements Component, Pool.Poolable {
    public GameObjectType type;
    public int animationIndex;
    public Sprite sprite;

    @Override
    public void reset() {
        type = null;
        animationIndex = -1;
        sprite = null;
    }
}
