package com.tomesz.game.map.dungeonGenerator;

import com.badlogic.gdx.math.Vector2;

import java.util.HashSet;

public class Room {
    public HashSet<Vector2> floors;
    public Vector2 center;
    public boolean playerStartRoom;
    public int id;

    public float minX = 9999;
    public float minY = 9999;
    public float maxY = -1;
    public float maxX = -1;







    public Room(HashSet<Vector2> floors, Vector2 center, int id) {
        this.floors = floors;
        this.center = center;
        this.id = id;

        for(Vector2 v : floors){
            if(v.x < minX){
                minX = v.x;
            }else if(v.x > maxX){
                maxX = v.x;
            }
            if(v.y < minY){
                minY = v.y;
            }else if(v.y > maxY){
                maxY = v.y;
            }
        }

    }

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
