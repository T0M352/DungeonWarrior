package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.AnimationComponent;
import com.tomesz.game.ecs.components.RemoveComponent;

public class AnimationSystem extends IteratingSystem {

    public AnimationSystem(DungeonWarrior context){
        super(Family.all(AnimationComponent.class).get());
    }
    @Override
    protected void processEntity(final Entity entity,final float v) {
       final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);
//       if(animationComponent.animationType != null){
//           animationComponent.animationTime += v;
//       }

       if(animationComponent.isAnimationg){
           animationComponent.animationTime += v;
       }
    }
}
