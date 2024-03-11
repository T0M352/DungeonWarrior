package com.tomesz.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.tomesz.game.ecs.components.GameObjectComponent;


import static com.tomesz.game.DungeonWarrior.BIT_GAME_OBJECT;
import static com.tomesz.game.DungeonWarrior.BIT_PLAYER;

public class WorldContactListener implements ContactListener {
    private final Array<PlayerCollisionListener> listeners;
    public WorldContactListener(){
        listeners = new Array<PlayerCollisionListener>();
    }
    public void addPlayerCollisionListener(final PlayerCollisionListener listener){
        listeners.add(listener);
    }
    @Override
    public void beginContact(Contact contact) {
        final Entity player;
        final Entity gameObject;
        final Body bodyA = contact.getFixtureA().getBody();
        final Body bodyB = contact.getFixtureB().getBody();
        final int catFixA = contact.getFixtureA().getFilterData().categoryBits;
        final int catFixB = contact.getFixtureB().getFilterData().categoryBits;

        if((catFixA & BIT_PLAYER) == BIT_PLAYER){
            player =(Entity) bodyA.getUserData();
        }else if ((catFixB & BIT_PLAYER) == BIT_PLAYER){
            player =(Entity) bodyB.getUserData();
        }else{
            return;
        }

        if((catFixA & BIT_GAME_OBJECT) == BIT_GAME_OBJECT){
            gameObject =(Entity) bodyA.getUserData();
        }else if ((catFixB & BIT_GAME_OBJECT) == BIT_GAME_OBJECT){
            gameObject =(Entity) bodyB.getUserData();
        }else{
            return;
        }

//        Gdx.app.debug("CONTACT", "KONTAKT Z GAMEOBJECT " + gameObject.getComponent(GameObjectComponent.class).type);

        for(final PlayerCollisionListener listener : listeners){
            listener.PlayerCollision(player, gameObject);
        }


    }

    @Override
    public void endContact(Contact contact) {
//        final Fixture fixtureA = contact.getFixtureA();
//        final Fixture fixtureB = contact.getFixtureB();
//
//        Gdx.app.debug("CONTACT ", "END: " + fixtureA.getBody().getUserData() + " " + fixtureA.isSensor());
//        Gdx.app.debug("CONTACT ", "END: " + fixtureB.getBody().getUserData() + " " + fixtureB.isSensor());
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }

    public interface PlayerCollisionListener{
        void PlayerCollision(final Entity player, final Entity gameObject);
    }
}
