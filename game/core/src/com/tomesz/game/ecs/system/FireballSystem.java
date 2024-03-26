package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.WorldContactListener;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.*;
import com.tomesz.game.view.AnimationType;

import java.util.Random;

public class FireballSystem extends IteratingSystem implements WorldContactListener.FireballCollisionListener{
    private final DungeonWarrior context;
    private float startOfAnimation;

    private final float fireballSpeed = 3f;
    public FireballSystem(DungeonWarrior context) {
        super(Family.all(FireballComponent.class).get());
        this.context = context;
        context.getWorldContactListener().addFireballCollisionListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float v) {
        final FireballComponent fireballComponent = ECSEngine.fireballObjectMaper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);
        final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(entity);
        b2DComponent.light.attachToBody(b2DComponent.body);

        Vector2 direction = fireballComponent.getDirection();
        float xFactor = direction.x;
        float yFactor = direction.y;

        if(!animationComponent.isAnimating){
            b2DComponent.body.applyLinearImpulse(
                    (xFactor * fireballSpeed - b2DComponent.body.getLinearVelocity().x) * b2DComponent.body.getMass(),
                    (yFactor * fireballSpeed - b2DComponent.body.getLinearVelocity().y) * b2DComponent.body.getMass(),
                    b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y, true
            );
            b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - b2DComponent.widht * 0.5f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f);
        }else{
            b2DComponent.body.setTransform(b2DComponent.renderPosition.x + 0.125f, b2DComponent.renderPosition.y + 0.125f, 0);
            if(animationComponent.animationTime >= 0.4f){
                entity.add(((ECSEngine)getEngine()).createComponent(RemoveComponent.class));
            }
        }






    }



    private Vector2 getWorldPositionFromScreen(Vector3 v) {
        Vector3 vc = context.getGameCamera().unproject(new Vector3(v.x,   v.y, v.z));
        return new Vector2(vc.x, vc.y);
    }

    @Override
    public void FireballCollision(Entity fireball, Entity contact) {
        //fireball.add(new RemoveComponent());
        final GameObjectComponent gameObjectComponent = ECSEngine.gameObjectMapper.get(contact);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(fireball);
        final AnimationComponent contactAnimationComponent = ECSEngine.animationComponentMapper.get(contact);
        final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(contact);
        gameObjectComponent.health -= 5; //TODO TUTAJ WSTAW ATAK GRACZA
        animationComponent.isAnimating = true;
        if(gameObjectComponent.health <= 0){
            b2DComponent.lightDistance = 1;
            b2DComponent.lightFluctuationDistance = 1.25f;
            contactAnimationComponent.isAnimating = true;
        }
        startOfAnimation = animationComponent.animationTime;




    }

    @Override
    public void FireballCollisionWithGround(Entity fireball) {
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(fireball);
        final FireballComponent fireballComponent = ECSEngine.fireballObjectMaper.get(fireball);
        animationComponent.isAnimating = true;
        startOfAnimation = animationComponent.animationTime;


        //fireball.add(((ECSEngine)getEngine()).createComponent(RemoveComponent.class));
    }

    @Override
    public void FireballWithEnemy(Entity fireball, Entity gameObject) {
        Gdx.app.debug("ASD", "UDERZONO WE WROGA");
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(fireball);
        animationComponent.isAnimating = true;
        startOfAnimation = animationComponent.animationTime;

        final EnemyComponent enemyComponent = ECSEngine.enemyObjectMapper.get(gameObject);
        final AnimationComponent enemyAnimationComponent = ECSEngine.animationComponentMapper.get(gameObject);

        if(enemyComponent != null){
            enemyComponent.addDamage(35);
        }
    }
}
