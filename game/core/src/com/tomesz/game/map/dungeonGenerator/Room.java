package com.tomesz.game.map.dungeonGenerator;

import com.badlogic.gdx.math.Vector2;

import java.util.HashSet;

public class Room {
    public HashSet<Vector2> floors;
    public Vector2 center;
    public boolean playerStartRoom;


    public Room(HashSet<Vector2> floors, Vector2 center) {
        this.floors = floors;
        this.center = center;
    }

    public boolean isPlayerStartRoom() {
        return playerStartRoom;
    }

    public void setPlayerStartRoom(boolean playerStartRoom) {
        this.playerStartRoom = playerStartRoom;
    }
}
