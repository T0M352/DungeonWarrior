package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.B2DComponent;
import com.tomesz.game.ecs.components.PlayerComponent;

public class PlayerCameraSystem extends IteratingSystem {
    private final OrthographicCamera camera;
    public PlayerCameraSystem(final DungeonWarrior context) {
        super(Family.all(PlayerComponent.class, B2DComponent.class).get());
        camera = context.getGameCamera();
    }

    @Override
    protected void processEntity(final Entity entity,final float v) {
        camera.position.set(ECSEngine.b2DComponentCmpMapper.get(entity).renderPosition, 0);
        camera.update();
    }
}
