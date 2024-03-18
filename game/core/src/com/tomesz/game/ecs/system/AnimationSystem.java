package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.AnimationComponent;
import com.tomesz.game.ecs.components.RemoveComponent;
import com.tomesz.game.view.AnimationType;

public class AnimationSystem extends IteratingSystem {

    public AnimationSystem(DungeonWarrior context){
        super(Family.all(AnimationComponent.class).get());
    }
    @Override
    protected void processEntity(final Entity entity,final float v) {
       final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);

       if(animationComponent.isAnimating){
           animationComponent.animationTime += v;
       }

       if(animationComponent.animationType == AnimationType.BARREL_END || animationComponent.animationType == AnimationType.BOX_END){
           if(animationComponent.animationTime >= 0.8f){
               entity.add(((ECSEngine)getEngine()).createComponent(RemoveComponent.class));
           }
       }

        if(animationComponent.animationType == AnimationType.TABLEUP_END || animationComponent.animationType == AnimationType.TABLE_END){
            if(animationComponent.animationTime >= 0.9f){
                entity.add(((ECSEngine)getEngine()).createComponent(RemoveComponent.class));
            }
        }
    }
}
