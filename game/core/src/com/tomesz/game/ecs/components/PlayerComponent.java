package com.tomesz.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class PlayerComponent implements Component, Pool.Poolable {
    public static int diamonds;

    public float mana = 100;

    public int health = 100;

    public int accuracy = 0;

    public B2DComponent b2DComponent;

    public float markDamageTimer = 0;


    public int getDiamonds() {
        return diamonds;
    }

    public void addDiamond(){
        diamonds++;
    }

    @Override
    public void reset() {
        diamonds = 0;
        mana = 100;
        health = 100;
        accuracy = 0;
        markDamageTimer = 0;
        b2DComponent = null;
    }

    public void setDiamonds(int diamonds) {
        PlayerComponent.diamonds = diamonds;
    }

    public void addMana(int manaCount){
        mana += manaCount;
    }
    public void addDamage(int damageCount){
        health -= damageCount;
        if(health < 0){
            health = 0;
        }
    }

    public void markDamage(){
        markDamageTimer = 0;
        b2DComponent.lightFluctuationDistance = 0.5f;
        b2DComponent.lightFluctuationSpeed = 8;
        b2DComponent.lightFluctuationTime = 0;
        b2DComponent.light.setColor(1,0,0,0.7f);
    }



}
