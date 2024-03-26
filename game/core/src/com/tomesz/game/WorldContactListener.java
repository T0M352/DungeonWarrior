package com.tomesz.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.tomesz.game.ecs.components.GameObjectComponent;

import static com.tomesz.game.DungeonWarrior.*;

public class WorldContactListener implements ContactListener {
    private final Array<PlayerCollisionListener> listeners;
    private final Array<FireballCollisionListener> fireballListeners;
    private final Array<EnemyFireballCollisionListener> enemyFireballCollisionListeners;
    private final Array<EntranceCollisionListener> entranceCollisionListeners;
    public WorldContactListener(){
        listeners = new Array<PlayerCollisionListener>();
        fireballListeners = new Array<FireballCollisionListener>();
        enemyFireballCollisionListeners = new Array<EnemyFireballCollisionListener>();
        entranceCollisionListeners = new Array<EntranceCollisionListener>();
    }
    public void addPlayerCollisionListener(final PlayerCollisionListener listener){
        listeners.add(listener);
    }
    public void addFireballCollisionListener(final FireballCollisionListener listener){
        fireballListeners.add(listener);
    }

    public void addEntranceCollisionListener(final EntranceCollisionListener listener){
        entranceCollisionListeners.add(listener);
    }

    public void addFireballCollisionListener(final EnemyFireballCollisionListener listener){
        enemyFireballCollisionListeners.add(listener);
    }


    @Override
    public void beginContact(Contact contact) {
        Entity player = null;
        Entity fireball = null;
        Entity enemyFireball = null;
        Entity gameObject = null;
        Entity entrance = null;
        final Body bodyA = contact.getFixtureA().getBody();
        final Body bodyB = contact.getFixtureB().getBody();
        final int catFixA = contact.getFixtureA().getFilterData().categoryBits;
        final int catFixB = contact.getFixtureB().getFilterData().categoryBits;


//        if ((catFixA & (BIT_PLAYER | BIT_FIREBALL)) == 0 && (catFixB & (BIT_PLAYER | BIT_FIREBALL)) == 0) {
//            return;
//        }

        if ((catFixA & BIT_PLAYER) == BIT_PLAYER) {
            player = (Entity) bodyA.getUserData();
        } else if ((catFixB & BIT_PLAYER) == BIT_PLAYER) {
            player = (Entity) bodyB.getUserData();
        }

        if ((catFixA & BIT_FIREBALL) == BIT_FIREBALL) {
            fireball = (Entity) bodyA.getUserData();
        } else if ((catFixB & BIT_FIREBALL) == BIT_FIREBALL) {
            fireball = (Entity) bodyB.getUserData();
        }


        if ((catFixA & BIT_ENEMY_FIREBALL) == BIT_ENEMY_FIREBALL) {
            enemyFireball = (Entity) bodyA.getUserData();
        } else if ((catFixB & BIT_ENEMY_FIREBALL) == BIT_ENEMY_FIREBALL) {
            enemyFireball = (Entity) bodyB.getUserData();
        }

        // Sprawdzenie kontaktu z graczem
        if (player != null) {
            if ((catFixA & BIT_GAME_OBJECT) == BIT_GAME_OBJECT) {
                gameObject = (Entity) bodyA.getUserData();
            } else if ((catFixB & BIT_GAME_OBJECT) == BIT_GAME_OBJECT) {
                gameObject = (Entity) bodyB.getUserData();
            }
            if (gameObject != null) {
                for (final PlayerCollisionListener listener : listeners) {
                    listener.PlayerCollision(player, gameObject);
                }
            }

            if ((catFixA & BIT_ENTRANCE) == BIT_ENTRANCE) {
                entrance = (Entity) bodyA.getUserData();
            } else if ((catFixB & BIT_ENTRANCE) == BIT_ENTRANCE) {
                entrance = (Entity) bodyB.getUserData();
            }
            if (entrance != null) {
                for (final EntranceCollisionListener listener : entranceCollisionListeners) {
                    listener.EnterToRoom(entrance);
                }
            }

            if (enemyFireball != null) {
                for (final EnemyFireballCollisionListener listener : enemyFireballCollisionListeners) {
                    listener.FireballWithPlayer(enemyFireball, player);
                }
            }
        }

        if (enemyFireball != null) {
            if ((catFixA & BIT_GROUND) == BIT_GROUND) {
                for (final EnemyFireballCollisionListener listener : enemyFireballCollisionListeners) {
                    listener.FireballCollisionWithGround(enemyFireball);
                }
            } else if ((catFixB & BIT_GROUND) == BIT_GROUND) {
                for (final EnemyFireballCollisionListener listener : enemyFireballCollisionListeners) {
                    listener.FireballCollisionWithGround(enemyFireball);
                }
            }
        }



            // Sprawdzenie kontaktu z ognistą kulą
        if (fireball != null) {
            if ((catFixA & BIT_GROUND) == BIT_GROUND || (catFixB & BIT_GROUND) == BIT_GROUND) {
                for (final FireballCollisionListener listener : fireballListeners) {
                    listener.FireballCollisionWithGround(fireball);
                }
            } else if ((catFixA & BIT_DESTROYABLE) == BIT_DESTROYABLE) {
                gameObject = (Entity) bodyA.getUserData();
                for (final FireballCollisionListener listener : fireballListeners) {
                    listener.FireballCollision(fireball, gameObject);
                }
            } else if ((catFixB & BIT_DESTROYABLE) == BIT_DESTROYABLE) {
                gameObject = (Entity) bodyB.getUserData();
                for (final FireballCollisionListener listener : fireballListeners) {
                    listener.FireballCollision(fireball, gameObject);
                }
            }else if ((catFixB & BIT_ENEMY) == BIT_ENEMY) {
                gameObject = (Entity) bodyB.getUserData();
                for (final FireballCollisionListener listener : fireballListeners) {
                    listener.FireballWithEnemy(fireball, gameObject);
                }
            }else if ((catFixA & BIT_ENEMY) == BIT_ENEMY) {
                gameObject = (Entity) bodyA.getUserData();
                for (final FireballCollisionListener listener : fireballListeners) {
                    listener.FireballWithEnemy(fireball, gameObject);
                }
            }
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

    public interface FireballCollisionListener{
        void FireballCollision(final Entity fireball, final Entity contact);
        void FireballCollisionWithGround(final Entity fireball);

        void FireballWithEnemy(Entity fireball, Entity gameObject);
    }

    public interface EnemyFireballCollisionListener{
        void FireballCollisionWithGround(final Entity fireball);

        void FireballWithPlayer(Entity fireball, Entity player);
    }

    public interface EntranceCollisionListener{
        void EnterToRoom(Entity entrance);

    }
}
