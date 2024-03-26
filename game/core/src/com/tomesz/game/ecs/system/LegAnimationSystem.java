package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.AnimationComponent;
import com.tomesz.game.ecs.components.LegAnimationComponent;

public class LegAnimationSystem extends IteratingSystem {

    public LegAnimationSystem(DungeonWarrior context) {
        super(Family.all(LegAnimationComponent.class).get());
    }

    @Override
    protected void processEntity(final Entity entity, final float v) {
        final LegAnimationComponent legAnimationComponent = ECSEngine.legAnimComponentMapper.get(entity);
        if (legAnimationComponent.isAnimating) {
            legAnimationComponent.animationTime += v;
        }
    }
}
