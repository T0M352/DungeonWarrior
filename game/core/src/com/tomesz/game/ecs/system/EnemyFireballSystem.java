package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.WorldContactListener;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.*;

public class EnemyFireballSystem extends IteratingSystem implements WorldContactListener.EnemyFireballCollisionListener {
    private final DungeonWarrior context;
    private float startOfAnimation;

    private final float fireballSpeed = 3f;

    public EnemyFireballSystem(DungeonWarrior context) {
        super(Family.all(EnemyFireball.class).get());
        this.context = context;
        context.getWorldContactListener().addFireballCollisionListener(this);

    }

    @Override
    protected void processEntity(Entity entity, float v) {
        final EnemyFireball fireballComponent = ECSEngine.enemyFireballComponentMapper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);
        final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(entity);
        b2DComponent.light.attachToBody(b2DComponent.body);

        Vector2 direction = fireballComponent.getDirection();
        float xFactor = direction.x;
        float yFactor = direction.y;

        if (!animationComponent.isAnimating) {
            b2DComponent.body.applyLinearImpulse(
                    (xFactor * fireballSpeed - b2DComponent.body.getLinearVelocity().x) * b2DComponent.body.getMass(),
                    (yFactor * fireballSpeed - b2DComponent.body.getLinearVelocity().y) * b2DComponent.body.getMass(),
                    b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y, true
            );
            b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - b2DComponent.widht * 0.5f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f);
        } else {
            b2DComponent.body.setTransform(b2DComponent.renderPosition.x + 0.125f, b2DComponent.renderPosition.y + 0.125f, 0);
            if (animationComponent.animationTime >= 0.4f) {
                entity.add(((ECSEngine) getEngine()).createComponent(RemoveComponent.class));
            }
        }


    }

    @Override
    public void FireballCollisionWithGround(Entity fireball) {
        Gdx.app.debug("ASD", "DZIALA FIREBALL");

        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(fireball);
        final EnemyFireball fireballComponent = ECSEngine.enemyFireballComponentMapper.get(fireball);
        animationComponent.isAnimating = true;
        startOfAnimation = animationComponent.animationTime;
    }

    @Override
    public void FireballWithPlayer(Entity fireball, Entity player) {
        Gdx.app.debug("ASD", "DZIALA FIREBALL");
        final PlayerComponent playerComponent = ECSEngine.playerCmpMapper.get(player);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(fireball);

        playerComponent.addDamage(5);
        playerComponent.markDamage();
        animationComponent.isAnimating = true;
        startOfAnimation = animationComponent.animationTime;
    }
}
