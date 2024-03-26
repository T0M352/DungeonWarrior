package com.tomesz.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.tomesz.game.view.AnimationType;

import java.util.List;

public class EnemyComponent implements Component, Pool.Poolable {
    public int health;
    public int damage;
    public Vector2 direction;
    public Vector2 posToChange;

    public boolean changingPosition;

    public List<Vector2> path;

    public int pathStep = 1;

    public float deathTimer;

    public boolean isAttacking;

    public int moveDirection;

    public float attackDelay;

    public float changeTimer;

    public float pathTimer;

    public boolean playerInSight;

    public AnimationType animationRight;
    public AnimationType animationLeft;
    public AnimationType animationFront;
    public AnimationType animationBack;
    public AnimationType legsRight;
    public AnimationType legsLeft;
    public AnimationType legsFront;
    public AnimationType legsBack;
    public AnimationType legsIdleRight;
    public AnimationType legsIdleLeft;


    @Override
    public void reset() {
        health = 100;
        damage = 10;
        direction = null;
        pathStep = 1;
        path = null;
        deathTimer = 0;
    }

    public void addDamage(int i) {
        health -= i;
    }
}
