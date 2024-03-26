package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.WorldContactListener;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.*;

public class RoomEntranceSystem extends IteratingSystem implements WorldContactListener.EntranceCollisionListener {
    private final DungeonWarrior context;
    private ImmutableArray<Entity> entranceEntities;

    private boolean createDoors;

    private boolean startChecking;

    private float checkTimer;

    private boolean roomIsClean = false;

    private boolean deleteDoors = false;

    RoomEntranceComponent roomEntranceComponent;
    QueryCallback playerInSightCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if(fixture.getBody().getUserData() instanceof Entity && ECSEngine.enemyObjectMapper.get((Entity) fixture.getBody().getUserData()) != null){
                EnemyComponent enemyComponent = ECSEngine.enemyObjectMapper.get((Entity) fixture.getBody().getUserData());
                enemyComponent.playerInSight = true;

            }
            return true;
        }
    };

    QueryCallback cleanRoom = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if(fixture.getBody().getUserData() instanceof Entity && ECSEngine.enemyObjectMapper.get((Entity) fixture.getBody().getUserData()) != null){
//                ((Entity) fixture.getBody().getUserData()).add(context.getEcsEngine().createComponent(RemoveComponent.class));
                roomIsClean = false;
            }
            return true;
        }
    };

    public RoomEntranceSystem(final DungeonWarrior context) {
        super(Family.all(RoomEntranceComponent.class).get());
        this.context =context;
        context.getWorldContactListener().addEntranceCollisionListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float v) {
        final RoomEntranceComponent roomEntranceComponent = ECSEngine.roomEntranceComponentComponentMapper.get(entity);
        if(roomEntranceComponent.roomID != -1){
            if(roomEntranceComponent.createDoor){
                context.getEcsEngine().createDungeonDoor(roomEntranceComponent.position, roomEntranceComponent.doorOrientation);
                startChecking = true;
                roomEntranceComponent.createDoor = false;
            }

            if(startChecking){
                checkTimer += v;
                if(checkTimer > 8){
                    checkTimer = 0;
                    roomIsClean = true;
                    context.getWorld().QueryAABB(cleanRoom, this.roomEntranceComponent.minX / 2, this.roomEntranceComponent.minY / 2, this.roomEntranceComponent.maxX / 2, this.roomEntranceComponent.maxY / 2);
                    if(roomIsClean){
                        Gdx.app.debug("POKOJ", "WYCZYSZCZONO");
                        deleteDoors = true;
                    }else{
                        Gdx.app.debug("POKOJ", "Z WROGAMI");
                    }
                }
            }

            if(deleteDoors){
                removeEntrancesByID(-1);
                deleteDoors = false;
                roomEntranceComponent.createDoor = false;
            }
        }


    }


    @Override
    public void EnterToRoom(Entity entrance) {
        final RoomEntranceComponent roomEntranceComponent = ECSEngine.roomEntranceComponentComponentMapper.get(entrance);
        this.roomEntranceComponent = roomEntranceComponent;


        context.getWorld().QueryAABB(playerInSightCallback, roomEntranceComponent.minX / 2, roomEntranceComponent.minY / 2, roomEntranceComponent.maxX / 2, roomEntranceComponent.maxY / 2);

        removeEntrancesByID(roomEntranceComponent.roomID);


    }

    private void removeEntrancesByID(int roomID) {
        entranceEntities = context.getEcsEngine().getEntitiesFor(Family.all(RoomEntranceComponent.class).get());
        for(Entity entity : entranceEntities){
            final RoomEntranceComponent roomEntranceComponent = ECSEngine.roomEntranceComponentComponentMapper.get(entity);
            if(roomEntranceComponent.roomID == roomID){
                entity.add(((ECSEngine) getEngine()).createComponent(RemoveComponent.class));
                roomEntranceComponent.createDoor = true;
            }
        }
    }
}
