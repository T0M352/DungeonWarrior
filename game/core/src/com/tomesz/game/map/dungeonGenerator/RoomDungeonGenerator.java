package com.tomesz.game.map.dungeonGenerator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.awt.*;
import java.util.*;

public class RoomDungeonGenerator{
    private int minRoomWidth = 12;
    private int minRoomHeight = 12;
    private int dungeonWidth = 64;
    private int dungeonHeight = 64;
    private int offset = 2;
    private boolean randomWalkRooms = false ;
    private Vector2 startPosition = new Vector2(64, 64);

    //Random walk data
    private int iterations = 50;
    private int walkLength = 30;

    private Vector2 spawnLocation;

    private ArrayList<Vector2> corridors = new ArrayList<Vector2>();
    private ArrayList<Room> rooms = new ArrayList<>();
    public void runProceduralGeneration() {
        HashSet<Vector2> floorPositions = runRandomWalk(startPosition);
        createRooms();
    }

    public HashSet<Vector2> createRooms() {
        rooms = new ArrayList<>();
        ArrayList<Rectangle> roomList = binarySpacePartitioning(new Rectangle((int)startPosition.x, (int)startPosition.y, dungeonWidth, dungeonHeight), minRoomWidth, minRoomHeight);
        HashSet<Vector2> floor = new HashSet<>();
        if (randomWalkRooms) {
            floor = createRoomsRandomly(roomList);
        } else {
            floor = createSimpleRooms(roomList);
        }

        ArrayList<Vector2> roomCenters = new ArrayList<>();
        for (Rectangle room : roomList) {
            roomCenters.add(new Vector2((int) room.getCenterX(), (int) room.getCenterY()));
        }
        spawnLocation = roomCenters.get(0);

        corridors = connectRooms(roomCenters);

        floor.addAll(corridors);



        return floor;
    }

    public int getMinRoomWidth() {
        return minRoomWidth;
    }

    public int getMinRoomHeight() {
        return minRoomHeight;
    }

    public ArrayList<Vector2> getCorridors() {
        return corridors;
    }

    public Vector2 getSpawnLocation() {
        return spawnLocation;
    }

    private HashSet<Vector2> createRoomsRandomly(ArrayList<Rectangle> roomList) {
        HashSet<Vector2> floor = new HashSet<>();
        for (Rectangle roomBounds : roomList) {
            Vector2 roomCenter = new Vector2((int) roomBounds.getCenterX(), (int) roomBounds.getCenterY());
            HashSet<Vector2> roomFloor = runRandomWalk(roomCenter);

            for (Vector2 position : roomFloor) {
                if (position.x >= (roomBounds.getMinX() + offset) && position.x <= (roomBounds.getMaxX() - offset) &&
                        position.y >= (roomBounds.getMinY() - offset) && position.y <= (roomBounds.getMaxY() - offset)) {
                    floor.add(position);
                }
            }

        }
        return floor;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    private ArrayList<Vector2> connectRooms(ArrayList<Vector2> roomCenters) {
        ArrayList<Vector2> corridors = new ArrayList<>();
        Vector2 currentRoomCenter = roomCenters.get(new Random().nextInt(roomCenters.size()));
        roomCenters.remove(currentRoomCenter);
        //Gdx.app.debug("generacja, ", "startuje z punktu" + currentRoomCenter.toString());
        while (!roomCenters.isEmpty()) {
            Vector2 closest = findClosestPoint(currentRoomCenter, roomCenters);
            //Gdx.app.debug("generacja, ", "znalazłem najbliższy punkt" + closest.toString());
            roomCenters.remove(closest);
            ArrayList<Vector2> newCorridor = createCorridor(currentRoomCenter, closest);
            currentRoomCenter = closest;
            corridors.addAll(newCorridor);
        }
        return corridors;
    }

    private ArrayList<Vector2> createCorridor(Vector2 currentRoomCenter, Vector2 destination) {
        ArrayList<Vector2> corridor = new ArrayList<>();
        Vector2 position = new Vector2(currentRoomCenter.x, currentRoomCenter.y);
        corridor.add(position);
        while (position.y != destination.y) {
            if (destination.y > position.y) {
                position.add(0, 1);
            } else if (destination.y < position.y) {
                position.add(0, -1);
            }
            corridor.add(new Vector2(position));
        }
        while (position.x != destination.x) {
            if (destination.x > position.x) {
                position.add(1, 0);
            } else if (destination.x < position.x) {
                position.add(-1, 0);
            }
            corridor.add(new Vector2(position));
        }

        return corridor;
    }

    private Vector2 findClosestPoint(Vector2 currentRoomCenter, ArrayList<Vector2> roomCenters) {
        Vector2 closest = null;
        double length = Double.MAX_VALUE;
        for (Vector2 position : roomCenters) {
            double currentDistance = Math.sqrt(Math.pow(position.x - currentRoomCenter.x, 2) + Math.pow(position.y - currentRoomCenter.y, 2));
            if (currentDistance < length) {
                length = currentDistance;
                closest = position;
            }
        }
        return closest;
    }

    private HashSet<Vector2> createSimpleRooms(ArrayList<Rectangle> roomList) {
        HashSet<Vector2> floor = new HashSet<>();
        for (Rectangle room : roomList) {
            HashSet<Vector2> roomSet = new HashSet<Vector2>();
            for (int col = offset; col < room.width - offset; col++) {
                for (int row = offset; row < room.height - offset; row++) {
                    Vector2 position = new Vector2(room.x + col, room.y + row);
                    roomSet.add(position);
                    floor.add(position);
                }
            }
            rooms.add(new Room(roomSet, new Vector2((float)room.getCenterX(), (float)room.getCenterY())));
        }
        return floor;
    }

    private ArrayList<Rectangle> binarySpacePartitioning(Rectangle spaceToSplit, int minWidth, int minHeight) {
        ArrayList<Rectangle> roomsList = new ArrayList<>();
        Queue<Rectangle> roomsQueue = new LinkedList<>();
        roomsQueue.add(spaceToSplit);

        while (!roomsQueue.isEmpty()) {
            Rectangle room = roomsQueue.poll();
            if (room.height >= minHeight && room.width >= minWidth) {
                if (Math.random() < 0.5) {
                    if (room.height >= minHeight * 2) {
                        splitHorizontally(minHeight, roomsQueue, room);
                    } else if (room.width >= minWidth * 2) {
                        splitVertically(minWidth, roomsQueue, room);
                    } else if (room.width >= minWidth && room.height >= minHeight) {
                        roomsList.add(room);
                    }
                } else {
                    if (room.width >= minWidth * 2) {
                        splitVertically(minWidth, roomsQueue, room);
                    } else if (room.height >= minHeight * 2) {
                        splitHorizontally(minHeight, roomsQueue, room);
                    } else if (room.width >= minWidth && room.height >= minHeight) {
                        roomsList.add(room);
                    }
                }
            }
        }
        return roomsList;
    }

    private void splitVertically(int minWidth, Queue<Rectangle> roomsQueue, Rectangle room) {
        int xSplit = new Random().nextInt(room.width - 1) + 1;
        Rectangle room1 = new Rectangle(room.x, room.y, xSplit, room.height);
        Rectangle room2 = new Rectangle(room.x + xSplit, room.y, room.width - xSplit, room.height);
        roomsQueue.add(room1);
        roomsQueue.add(room2);
    }

    private void splitHorizontally(int minHeight, Queue<Rectangle> roomsQueue, Rectangle room) {
        int ySplit = new Random().nextInt(room.height - 1) + 1;
        Rectangle room1 = new Rectangle(room.x, room.y, room.width, ySplit);
        Rectangle room2 = new Rectangle(room.x, room.y + ySplit, room.width, room.height - ySplit);
        roomsQueue.add(room1);
        roomsQueue.add(room2);
    }

    private HashSet<Vector2> runRandomWalk(Vector2 position) {
        Vector2 currentPosition = new Vector2(position.x, position.y);
        HashSet<Vector2> floorPositions = new HashSet<>();
        for (int i = 0; i < iterations; i++) {
            HashSet<Vector2> path = simpleRandomWalk(currentPosition, walkLength);
            floorPositions.addAll(path);
        }
        rooms.add(new Room(floorPositions, position));
        return floorPositions;
    }

    public HashSet<Vector2> simpleRandomWalk(Vector2 startPosition, int walkLength) {
        HashSet<Vector2> path = new HashSet<>();
        path.add(startPosition);
        Vector2 previousPosition = new Vector2(startPosition.x, startPosition.y);

        for (int i = 0; i < walkLength; i++) {
            Vector2 newPosition = new Vector2(previousPosition.x + getRandomCardinalDirection().x, previousPosition.y + getRandomCardinalDirection().y);
            path.add(newPosition);
            previousPosition = newPosition;
        }
        return path;
    }

    private Vector2 getRandomCardinalDirection(){
        ArrayList<Vector2> cardinalDirectionsList = new ArrayList<Vector2>() {{
            add(new Vector2(0, 1));  // góra
            add(new Vector2(1, 0));  // prawo
            add(new Vector2(0, -1)); // dół
            add(new Vector2(-1, 0)); // lewo
        }};
        return cardinalDirectionsList.get(new Random().nextInt(cardinalDirectionsList.size()));
    }

}

