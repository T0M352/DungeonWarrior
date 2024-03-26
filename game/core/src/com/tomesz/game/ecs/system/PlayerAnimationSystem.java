package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.AnimationComponent;
import com.tomesz.game.ecs.components.B2DComponent;
import com.tomesz.game.ecs.components.PlayerComponent;
import com.tomesz.game.view.AnimationType;

public class PlayerAnimationSystem extends IteratingSystem {
    public PlayerAnimationSystem(DungeonWarrior context){
        super(Family.all(PlayerComponent.class, AnimationComponent.class, B2DComponent.class).get());
    }
    @Override
    protected void processEntity(final Entity entity, final float v) {
        final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);
        final PlayerComponent playerComponent = ECSEngine.playerCmpMapper.get(entity);
        if(b2DComponent.body.getLinearVelocity().equals(Vector2.Zero)){
            animationComponent.animationType = AnimationType.MAGE_IDLE;
        } else if(b2DComponent.body.getLinearVelocity().x < 0){
            animationComponent.animationType = AnimationType.MAGE_MOVE_LEFT;
        }else{
            animationComponent.animationType = AnimationType.MAGE_MOVE_RIGHT;
        }

        //optymalizacja zeby wiecznie nie zwiekszac
        if(playerComponent.markDamageTimer < 1){
            playerComponent.markDamageTimer+=v;
        }
        if(playerComponent.markDamageTimer > 0.8f){
            b2DComponent.lightDistance = 0;
            b2DComponent.lightFluctuationDistance = 0;
        }


    }


}
