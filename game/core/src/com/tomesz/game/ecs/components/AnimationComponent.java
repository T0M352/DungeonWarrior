package com.tomesz.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.tomesz.game.view.AnimationType;

public class AnimationComponent implements Component, Pool.Poolable {
    public AnimationType animationType;
    public float animationTime;
    public boolean isAnimationg;

    public float width;
    public float height;

    @Override
    public void reset() {
        animationTime = 0;
        animationType = null;
        width =height =0;
        isAnimationg = false;

    }
}
