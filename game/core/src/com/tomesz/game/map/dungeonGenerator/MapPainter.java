package com.tomesz.game.map.dungeonGenerator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.map.GameObjectType;


import java.util.*;

public class MapPainter implements Disposable {


    public AssetManager assetManager;

    private HashSet<Vector2> generatedDungeon; //podloga
    private HashSet<Vector2> generatedWalls; //sciany
    private HashSet<Vector2> generatedDecoration; //sciany
    private ArrayList<Room> generatedRooms; //sciany
    private ArrayList<Vector2> generatedCorridors; //scieżka;
    private RoomDungeonGenerator roomDungeonGenerator;



    TiledMapTileLayer layer;
    TiledMapTileLayer layerCollision;
    MapLayer decorateLayer;
    TiledMapTileSet tileset;
    TiledMap map;

    private TiledMapTile floorTile, wallTop, wallSideRight, wallSideLeft, wallBottom, wallFull, wallInnerCornerDownLeft,
            wallInnerCornerDownRight, wallDiagonalCornerDownRight, wallDiagonalCornerDownLeft, wallDiagonalCornerUpRight,
            wallDiagonalCornerUpLeft, backgroundTile, floorTile2, floorTile3, floorTile4, floorTile5;
    private TiledMapTile barrelTile, lampTile, boxTile, tableTile;

    private HashMap<Vector2, GameObjectType> decorationMap = new HashMap<>();


    public MapPainter(DungeonWarrior context, TiledMap tiledMap) {

        assetManager = context.getAssetManager();
        roomDungeonGenerator = new RoomDungeonGenerator();

//        generatedDungeon = roomDungeonGenerator.createRooms();
//        generatedCorridors = roomDungeonGenerator.getCorridors();
//        generatedRooms = roomDungeonGenerator.getRooms();

        map = tiledMap;
        generatedDecoration = new HashSet<Vector2>();

        layer = (TiledMapTileLayer) map.getLayers().get("Ground");
        layerCollision = (TiledMapTileLayer) map.getLayers().get("Dungeon");
        decorateLayer =  map.getLayers().get("ObjectLayer");


        tileset = map.getTileSets().getTileSet("dungeon");
        floorTile = tileset.getTile(52);
        floorTile2 = tileset.getTile(124);
        floorTile3 = tileset.getTile(103);
        floorTile4 = tileset.getTile(59);
        floorTile5 = tileset.getTile(68);
        backgroundTile = tileset.getTile(18);

        wallInnerCornerDownLeft = tileset.getTile(66);
        wallInnerCornerDownRight = tileset.getTile(37);
        wallDiagonalCornerDownRight = tileset.getTile(42);
        wallDiagonalCornerDownLeft = tileset.getTile(41);
        wallDiagonalCornerUpRight =  tileset.getTile(22);
        wallDiagonalCornerUpLeft = tileset.getTile(105);

        wallTop = tileset.getTile(82);
        wallBottom = tileset.getTile(69);
        wallFull = tileset.getTile(83);
        wallSideLeft = tileset.getTile(3);
        wallSideRight = tileset.getTile(20);
        barrelTile = tileset.getTile(109);
        boxTile = tileset.getTile(120);
        lampTile = tileset.getTile(88);
        tableTile = tileset.getTile(102);
    }

    public ArrayList<Room> getGeneratedRooms() {
        return generatedRooms;
    }
    public void paintFloors(){
        generatedDungeon = roomDungeonGenerator.createRooms();
        generatedCorridors = roomDungeonGenerator.getCorridors();
        generatedRooms = roomDungeonGenerator.getRooms();
        for(Vector2 cell : generatedDungeon){
                TiledMapTileLayer.Cell newCell = new TiledMapTileLayer.Cell();
                int random = new Random().nextInt(10);
                if(random == 1){
                    newCell.setTile(floorTile2);
                }else if(random == 2){
                    newCell.setTile(floorTile3);
                }else if(random == 3){
                    newCell.setTile(floorTile4);
                }else if(random == 4){
                    newCell.setTile(floorTile5);
                }else{
                    newCell.setTile(floorTile);
                }

            paintSingleTile(layer, newCell, cell);
        }

        generatedWalls = CreateWalls(generatedDungeon);



        setPlayerSpawnPoint();




    }

    public Vector2 getStairsLocation() {
        return generatedRooms.get(generatedRooms.size()-1).center;

    }

    public HashMap<Vector2, GameObjectType> decorateDungeon() {
//        generatedCorridors = roomDungeonGenerator.getCorridors();
//        generatedRooms = roomDungeonGenerator.getRooms();
        decorationMap = new HashMap<Vector2, GameObjectType>();
        int maxTables;
        int maxBoxes;
        int maxBarrels;
        int maxDiamonds;
        int maxLamps;

        for(Room room : generatedRooms){
            maxTables = 6;
            maxBoxes = 5;
            maxBarrels = 4;
            maxDiamonds = new Random().nextInt(3);
            maxLamps = 4;
                for(Vector2 floorPos : room.floors){

                    if(!generatedCorridors.contains(floorPos) && !generatedWalls.contains(floorPos)){

                        boolean isWallNeighbour = false;
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        boolean neighboorWall = false;
                        for(Vector2 direction : cardinalDirectionsList) {
                            Vector2 neighboorPos = floorPos.add(direction);
                            if (generatedWalls.contains(neighboorPos)) {
                                neighboorWall = true;
                            }
                        }
                            if(neighboorWall){
                                int random = new Random().nextInt(6);
                                if(random == 2 && maxBarrels > 0 && checkForNeighborDec(floorPos, 0)){
                                    decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), GameObjectType.BARREL);
                                    generatedDecoration.add(floorPos);
                                    maxBarrels--;
                                }

                                if(checkForLampsOnWall(floorPos) && maxLamps > 0){
                                    decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), GameObjectType.LAMP);
                                    generatedDecoration.add(floorPos);
                                    maxLamps--;
                                }

                            }else{
                                int random = new Random().nextInt(30);
                                if(random == 4 && maxBoxes > 0 && checkForNeighborDec(floorPos, 0)){
                                    decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), GameObjectType.BOX);
                                    generatedDecoration.add(floorPos);
                                    maxBoxes--;
                                }else if(random == 2 && maxTables>0 && checkForNeighborDec(floorPos, 1)){
                                    decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), GameObjectType.TABLE);
                                    generatedDecoration.add(floorPos);
                                    generatedDecoration.add(new Vector2(floorPos.x + 1, floorPos.y));
                                    maxTables--;
                                }else if(random == 23 && maxTables>0 && checkForNeighborDec(floorPos, 1)){
                                    decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), GameObjectType.TABLE_UP);
                                    generatedDecoration.add(floorPos);
                                    generatedDecoration.add(new Vector2(floorPos.x, floorPos.y + 1));
                                    maxTables--;
                                }else if(random == 1 && maxDiamonds>0 && checkForNeighborDec(floorPos, 1)){
                                    decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), GameObjectType.DIAMOND);
                                    generatedDecoration.add(floorPos);
                                    maxDiamonds--;
                                }

                            }
                        }
                    }
        }
        decorationMap.put(getStairsLocation(), GameObjectType.STAIRS);
        return decorationMap;
    }

    private boolean checkForLampsOnWall(Vector2 floorPos) {
        if(generatedWalls.contains(floorPos))
            return false;
        for (Vector2 pos : cardinalDirectionsList) {
            Vector2 positionToCheck = new Vector2(floorPos);
            for (int i = 0; i < 8; i++) {
//                if (generatedDecoration.contains(positionToCheck))
//                    return false;

                if(decorationMap.containsKey(positionToCheck))
                    if(decorationMap.get(positionToCheck) == GameObjectType.LAMP)
                        return false;

                positionToCheck.add(pos);
            }
        }
        return true;
    }

    private boolean checkForNeighborDec(Vector2 floorPos, int typeOfCheck) {
        Vector2 neigPos;
        Vector2 neigPos2;
        if(decorationMap.containsKey(floorPos))
            return false;

        if(typeOfCheck == 0){
            for(Vector2 dir : cardinalDirectionsList){
                neigPos = floorPos.add(dir);
                if(generatedDecoration.contains(neigPos))
                    return false;
            }
        }else if(typeOfCheck == 1){
            for(Vector2 dir : cardinalDirectionsList){
                neigPos = floorPos.add(dir);
                neigPos2 = neigPos.add(dir);
                if(generatedDecoration.contains(neigPos) || generatedDecoration.contains(neigPos2)
                        || generatedWalls.contains(neigPos) || generatedWalls.contains(neigPos2)
                        ||generatedCorridors.contains(neigPos) || generatedCorridors.contains(neigPos2))
                    return false;
            }
        }

        return true;
    }

    public void clearMap(){
        TiledMapTileLayer.Cell backgroundCell = new TiledMapTileLayer.Cell();
        backgroundCell.setTile(backgroundTile);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    // Ustawienie komórki na null usuwa kafelek z warstwy
                    layerCollision.setCell(x, y, null);
                }
                layer.setCell(x, y, backgroundCell);
            }
        }
        MapLayer startLayer = map.getLayers().get("PlayerStartLocation");
        MapLayer objectLayer = map.getLayers().get("Collision");
        for(MapObject mapObject : startLayer.getObjects()){
            startLayer.getObjects().remove(mapObject);
        }
        Array<MapObject> objectsToRemove = new Array<MapObject>();
        for(MapObject mapObject : objectLayer.getObjects()){
            objectsToRemove.add(mapObject);
        }
        for(MapObject objToRemove : objectsToRemove){
            objectLayer.getObjects().remove(objToRemove);
        }

        generatedDecoration = new HashSet<Vector2>();
        generatedCorridors = new ArrayList<Vector2>();
        generatedWalls = new HashSet<Vector2>();
        generatedRooms = new ArrayList<Room>();
        generatedDungeon = new HashSet<Vector2>();

    }

    public void setPlayerSpawnPoint() {
        Vector2 location = generatedRooms.get(0).center;
        generatedRooms.get(0).setPlayerStartRoom(true);

        MapLayer objectLayer = map.getLayers().get("PlayerStartLocation");
        int posX = (int)location.x * 16;
        int posY = (int)location.y * 16;
        Rectangle point = new Rectangle(posX, posY, 0, 0);

        RectangleMapObject rectangleMapObject = new RectangleMapObject();
        rectangleMapObject.getRectangle().set(point);

        objectLayer.getObjects().add(rectangleMapObject);

    }



    public HashSet<Vector2> CreateWalls(HashSet<Vector2> floorPositions)
    {
        HashSet<Vector2> basicWallPositions = FindWallsInDiretions(floorPositions, cardinalDirectionsList);
        HashSet<Vector2> cornerWallPositions = FindWallsInDiretions(floorPositions, diagonalDiretionList);
        CreateBasicWalls(basicWallPositions, floorPositions);
        CreateCornerWalls(cornerWallPositions, floorPositions);
        HashSet<Vector2> result = new HashSet<Vector2>();
        result.addAll(basicWallPositions);
        result.addAll(cornerWallPositions);
        return result;

    }

    private void CreateCornerWalls(HashSet<Vector2> cornerWallPositions, HashSet<Vector2> floorPositions)
    {
        for(Vector2 position : cornerWallPositions)
        {
            StringBuilder neighboursBinaryType = new StringBuilder();
            for (Vector2 direction : eightDiretionsList)
            {
                Vector2 neighbourPosition = new Vector2(position.x + direction.x, position.y + direction.y);  //position + direction;
                if (floorPositions.contains(neighbourPosition))
                {
                    neighboursBinaryType.append("1");
                }
                else
                {
                    neighboursBinaryType.append("0");
                }
            }
            PaitSingleCornerWall(position, neighboursBinaryType.toString());
        }
    }

    private void CreateBasicWalls(HashSet<Vector2> basicWallPositions, HashSet<Vector2> floorPositions)
    {
        for (Vector2 position : basicWallPositions)
        {
            StringBuilder neighboursBinaryType = new StringBuilder();
            for (Vector2 direction : cardinalDirectionsList)
            {
                Vector2 neighbourPosition = new Vector2(position.x + direction.x, position.y + direction.y);
                if (floorPositions.contains(neighbourPosition))
                {
                    neighboursBinaryType.append("1");
                }
                else
                {
                    neighboursBinaryType.append("0");
                }
            }
            PaintSingleBasicWall(position, neighboursBinaryType.toString());
        }
    }

    private static HashSet<Vector2> FindWallsInDiretions(HashSet<Vector2> floorPositions, ArrayList<Vector2> directionList)
    {
        HashSet<Vector2> wallPositions = new HashSet<Vector2>();
        for (Vector2 position : floorPositions)
        {
            for(Vector2 direction : directionList)
            {
                Vector2 neighbourPosition = new Vector2(position.x + direction.x, position.y + direction.y);
                if (!floorPositions.contains(neighbourPosition))
                    wallPositions.add(neighbourPosition);
            }
        }
        return wallPositions;
    }



    void PaintSingleBasicWall(Vector2 position, String binaryType)
    {
        int typeAsInt = Integer.parseInt(binaryType, 2);
        TiledMapTile tile = null;
        if (WallByteTypes.wallTop.contains(typeAsInt))
        {
            tile = wallTop;
        }
        else if (WallByteTypes.wallSideRight.contains(typeAsInt))
        {
            tile = wallSideRight;
        }
        else if (WallByteTypes.wallBottom.contains(typeAsInt))
        {
            tile = wallBottom;
        }
        else if (WallByteTypes.wallSideLeft.contains(typeAsInt))
        {
            tile = wallSideLeft;
        }
        else if (WallByteTypes.wallFull.contains(typeAsInt))
        {
            tile = wallFull;
        }



        if (tile!=null){
            TiledMapTileLayer.Cell newCell = new TiledMapTileLayer.Cell();
            newCell.setTile(tile);
            paintSingleTile(layerCollision, newCell, position);    //   (wallTilemap, tile, position);
            //DO ZOPTYMALIZOWANIA
            makeCollisionObject(position);


        }

    }


    void PaitSingleCornerWall(Vector2 position, String binaryType)
    {
        int typeAsInt = Integer.parseInt(binaryType, 2);
        TiledMapTile tile = null;
        if (WallByteTypes.wallInnerCornerDownLeft.contains(typeAsInt))
        {
            tile = wallInnerCornerDownLeft;
        }
        else if (WallByteTypes.wallInnerCornerDownRight.contains(typeAsInt))
        {
            tile = wallInnerCornerDownRight;
        }
        else if (WallByteTypes.wallDiagonalCornerDownLeft.contains(typeAsInt))
        {
            tile = wallDiagonalCornerDownLeft;
        }
        else if (WallByteTypes.wallDiagonalCornerDownRight.contains(typeAsInt))
        {
            tile = wallDiagonalCornerDownRight;
        }
        else if (WallByteTypes.wallDiagonalCornerUpRight.contains(typeAsInt))
        {
            tile = wallDiagonalCornerUpRight;
        }
        else if (WallByteTypes.wallDiagonalCornerUpLeft.contains(typeAsInt))
        {
            tile = wallDiagonalCornerUpLeft;
        }
        else if (WallByteTypes.wallFullEightDirections.contains(typeAsInt))
        {
            tile = wallFull;
        }
        else if (WallByteTypes.wallBottomEightDirections.contains(typeAsInt))
        {
            tile = wallFull;
        }

        if (tile!=null){
            TiledMapTileLayer.Cell newCell = new TiledMapTileLayer.Cell();
            newCell.setTile(tile);
            paintSingleTile(layerCollision, newCell, position);//
            //DO ZOPTYMALIZOWANIA TODO
            makeCollisionObject(position);

        }
    }

    private void makeCollisionObject(Vector2 position) {
        MapLayer objectLayer = map.getLayers().get("Collision");
        int tileSize = layer.getTileHeight();

        int rectX = (int)(position.x * tileSize);
        int rectY = (int)(position.y * tileSize);
        // Utwórz obiekt MapObject reprezentujący prostokąt
        Rectangle rectangle = new Rectangle(rectX, rectY, 0.5f, tileSize);
        RectangleMapObject rectangleMapObject = new RectangleMapObject();
        rectangleMapObject.getRectangle().set(rectangle);
        objectLayer.getObjects().add(rectangleMapObject);

    }

    private void paintSingleTile(TiledMapTileLayer layer, TiledMapTileLayer.Cell cell, Vector2 position){
        layer.setCell((int)position.x, (int)position.y, cell);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        map.dispose();

        // Zwolnienie zasobów graficznych
        floorTile.getTextureRegion().getTexture().dispose();
        wallTop.getTextureRegion().getTexture().dispose();
        wallSideRight.getTextureRegion().getTexture().dispose();
        wallSideLeft.getTextureRegion().getTexture().dispose();
        wallBottom.getTextureRegion().getTexture().dispose();
        wallFull.getTextureRegion().getTexture().dispose();
        wallInnerCornerDownLeft.getTextureRegion().getTexture().dispose();
        wallInnerCornerDownRight.getTextureRegion().getTexture().dispose();
        wallDiagonalCornerDownRight.getTextureRegion().getTexture().dispose();
        wallDiagonalCornerDownLeft.getTextureRegion().getTexture().dispose();
        wallDiagonalCornerUpRight.getTextureRegion().getTexture().dispose();
        wallDiagonalCornerUpLeft.getTextureRegion().getTexture().dispose();
        backgroundTile.getTextureRegion().getTexture().dispose();
        floorTile2.getTextureRegion().getTexture().dispose();
        floorTile3.getTextureRegion().getTexture().dispose();
        floorTile4.getTextureRegion().getTexture().dispose();
        floorTile5.getTextureRegion().getTexture().dispose();
        barrelTile.getTextureRegion().getTexture().dispose();
        lampTile.getTextureRegion().getTexture().dispose();
        boxTile.getTextureRegion().getTexture().dispose();
        tableTile.getTextureRegion().getTexture().dispose();

        // Zwolnienie innych zasobów
        disposeGeneratedDecoration();
    }

    private void disposeGeneratedDecoration() {
        // Zwolnienie zasobów wygenerowanych dekoracji
        for (Vector2 position : generatedDecoration) {
            TiledMapTileLayer.Cell cell = layer.getCell((int) position.x, (int) position.y);
            if (cell != null && cell.getTile() != null) {
                cell.getTile().getTextureRegion().getTexture().dispose();
            }
        }
    }

    public static ArrayList<Vector2> cardinalDirectionsList = new ArrayList<Vector2>() {{
        add(new Vector2(0, 1));  // góra
        add(new Vector2(1, 0));  // prawo
        add(new Vector2(0, -1)); // dół
        add(new Vector2(-1, 0)); // lewo
    }};

    public static ArrayList<Vector2> diagonalDiretionList = new ArrayList<Vector2>() {{
        add(new Vector2(1, 1));
        add(new Vector2(1, -1));
        add(new Vector2(-1, -1));
        add(new Vector2(-1, 1));
    }};

    public static ArrayList<Vector2> eightDiretionsList = new ArrayList<Vector2>() {{
        add(new Vector2(0, 1));  // gora
        add(new Vector2(1, 1));  // gora - prawo
        add(new Vector2(1, 0)); // prawo
        add(new Vector2(1, -1)); // dół - prawo
        add(new Vector2(0, -1));  // dol
        add(new Vector2(-1, -1));  // dół - lewo
        add(new Vector2(-1, 0)); // lewo
        add(new Vector2(-1, 1)); // gora - lewo
    }};
}
