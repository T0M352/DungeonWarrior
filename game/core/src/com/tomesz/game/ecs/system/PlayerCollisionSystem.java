package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.WorldContactListener;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.GameObjectComponent;
import com.tomesz.game.ecs.components.PlayerComponent;
import com.tomesz.game.ecs.components.RemoveComponent;
import com.tomesz.game.map.GameObjectType;

public class PlayerCollisionSystem extends IteratingSystem implements WorldContactListener.PlayerCollisionListener {

    private final DungeonWarrior context;
    public PlayerCollisionSystem(final DungeonWarrior context) {
        super(Family.all(RemoveComponent.class).get());
        this.context =context;
        context.getWorldContactListener().addPlayerCollisionListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float v) {
        getEngine().removeEntity(entity);
    }

    @Override
    public void PlayerCollision(Entity player, Entity gameObject) {
        final GameObjectComponent gameObjectComponent = ECSEngine.gameObjectMapper.get(gameObject);
        if(gameObjectComponent.type == GameObjectType.DIAMOND){
            gameObject.add(((ECSEngine)getEngine()).createComponent(RemoveComponent.class));
            player.getComponent(PlayerComponent.class).addDiamond();
        }
        if(gameObjectComponent.type == GameObjectType.STAIRS){
            DungeonWarrior.newLevel = true;
        }

    }
}
