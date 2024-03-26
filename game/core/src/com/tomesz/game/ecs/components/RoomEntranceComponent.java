package com.tomesz.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class RoomEntranceComponent implements Component, Pool.Poolable {
    public float minX;
    public float minY;

    public float maxX;
    public float maxY;

    public Vector2 position;

    public boolean createDoor = false;

    public int doorOrientation; //0 - pozioma      1 - pionowa

    public int roomID;
    @Override
    public void reset() {

    }
}
