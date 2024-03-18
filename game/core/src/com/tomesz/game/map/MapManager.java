package com.tomesz.game.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.B2DComponent;
import com.tomesz.game.ecs.components.PlayerComponent;
import com.tomesz.game.ecs.components.RemoveComponent;
import com.tomesz.game.map.dungeonGenerator.Room;
import com.tomesz.game.map.dungeonGenerator.RoomDungeonGenerator;
import com.tomesz.game.map.dungeonGenerator.WallByteTypes;

import java.awt.*;
import java.util.*;

import static com.tomesz.game.DungeonWarrior.*;

public class MapManager {
    private final World world;
    private final Array<Body> bodies;

    private  final AssetManager assetManager;
    private  Map currentMap;
    private final Array<Map> mapCache;
    private final Array<MapListener> listeners;

    private final DungeonWarrior context;

    private final ECSEngine ecsEngine;
    private Array<Entity> gameObjectToRemove;
    private HashMap<Vector2, String> decorationMap = new HashMap<Vector2,String>();

    private TiledMapTileLayer layerGround;


    private ImmutableArray<Entity> playerEntity;

    private TiledMapTileLayer layer;
    private TiledMapTileLayer layerCollision;
    private MapLayer decorateLayer;
    private MapLayer layerObjectCollision;
    private MapLayer playerStartLayrt;

    private StaticTiledMapTile floorTile, floorTile2, floorTile3, floorTile4, floorTile5, backgroundTile, wallInnerCornerDownLeft, wallInnerCornerDownRight,wallDiagonalCornerDownRight, wallDiagonalCornerDownLeft, wallDiagonalCornerUpRight,
            wallSideLeft, wallDiagonalCornerUpLeft, wallSideRight, wallTop, wallBottom, wallFull;

    private final RoomDungeonGenerator roomDungeonGenerator;

    private HashSet<Vector2> generatedDungeon; //podloga

    private HashSet<Vector2> generatedWalls; //sciany
    private HashSet<Vector2> generatedDecoration; //sciany
    private ArrayList<Room> generatedRooms; //sciany
    private ArrayList<Vector2> generatedCorridors; //scieżka;

    private Array<Point> rooms;

    public MapManager(DungeonWarrior context) {
        currentMap = null;
        world = context.getWorld();
        assetManager = context.getAssetManager();
        bodies = new Array<Body>();
        mapCache = new Array<Map>();
        listeners = new Array<MapListener>();
        this.context = context;
        ecsEngine = context.getEcsEngine();
        gameObjectToRemove = new Array<Entity>();
        playerEntity = ecsEngine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        roomDungeonGenerator = new RoomDungeonGenerator();
        generatedDecoration = new HashSet<Vector2>();
        rooms = new Array<Point>();

    }

    public void addListener(final MapListener listener){
        listeners.add(listener);
    }

    public void setupMap(){

        Gdx.app.debug("ASD", "Tworzenie mapy");

        final TiledMap tiledMap = assetManager.get("dungeon/map.tmx", TiledMap.class);

        if(currentMap!= null){
            world.getBodies(bodies);
            destroyCollisionAreas();
            desroyGameObjects();
        }
        layer = (TiledMapTileLayer) tiledMap.getLayers().get("Ground");
        layerCollision = (TiledMapTileLayer) tiledMap.getLayers().get("Dungeon");
        decorateLayer =  tiledMap.getLayers().get("ObjectLayer");
        layerObjectCollision =  tiledMap.getLayers().get("Collision");
        playerStartLayrt =  tiledMap.getLayers().get("PlayerStartLocation");
        TextureAtlas atlas = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class);
        floorTile = new StaticTiledMapTile(atlas.findRegion("floorTile1"));
        floorTile2 = new StaticTiledMapTile(atlas.findRegion("floorTile2"));
        floorTile3 = new StaticTiledMapTile(atlas.findRegion("floorTile3"));
        floorTile4 = new StaticTiledMapTile(atlas.findRegion("floorTile4"));
        floorTile5 = new StaticTiledMapTile(atlas.findRegion("floorTile5"));
        backgroundTile = new StaticTiledMapTile(atlas.findRegion("backgroundTile"));

        wallInnerCornerDownLeft = new StaticTiledMapTile(atlas.findRegion("wallCornerDownLeft"));
        wallInnerCornerDownRight = new StaticTiledMapTile(atlas.findRegion("wallCornerDownRight"));

        wallDiagonalCornerDownRight = new StaticTiledMapTile(atlas.findRegion("wallDiagonalCornerDownRight"));
        wallDiagonalCornerDownLeft = new StaticTiledMapTile(atlas.findRegion("wallDiagonalCornerDownLeft"));

        wallDiagonalCornerUpLeft = new StaticTiledMapTile(atlas.findRegion("wallDiagonalCornerUpLeft"));
        wallSideLeft = new StaticTiledMapTile(atlas.findRegion("wallDiagonalCornerUpLeft"));

        wallDiagonalCornerUpRight = new StaticTiledMapTile(atlas.findRegion("wallDiagonalCornerUpRight"));
        wallSideRight = new StaticTiledMapTile(atlas.findRegion("wallDiagonalCornerUpRight"));

        wallTop = new StaticTiledMapTile(atlas.findRegion("wallTop"));
        wallBottom = new StaticTiledMapTile(atlas.findRegion("wallBottom"));
        wallFull = new StaticTiledMapTile(atlas.findRegion("wallFull"));



        paintFloors();


        currentMap = new Map(tiledMap, context);


        mapCache.add(currentMap);



        makeCollisionAreas();
        makeObjects();



        for(final MapListener listener: listeners){
            listener.mapChange(currentMap);
        }
    }

    public Array<Point> getRooms() {
        return rooms;
    }

    public HashMap<Vector2, String> decorateDungeon() {
//        generatedCorridors = roomDungeonGenerator.getCorridors();
//        generatedRooms = roomDungeonGenerator.getRooms();
        decorationMap = new HashMap<Vector2, String>();
        int maxTables;
        int maxBoxes;
        int maxBarrels;
        int maxDiamonds;
        int maxLamps;

        for(Room room : roomDungeonGenerator.getRooms()){
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
                            decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), "BARREL");
                            generatedDecoration.add(floorPos);
                            maxBarrels--;
                        }

                        if(checkForLampsOnWall(floorPos) && maxLamps > 0){
                            decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), "LAMP");
                            generatedDecoration.add(floorPos);
                            maxLamps--;
                        }

                    }else{
                        int random = new Random().nextInt(30);
                        if(random == 4 && maxBoxes > 0 && checkForNeighborDec(floorPos, 0)){
                            decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), "BOX");
                            generatedDecoration.add(floorPos);
                            maxBoxes--;
                        }else if(random == 2 && maxTables>0 && checkForNeighborDec(floorPos, 1)){
                            decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), "TABLE");
                            generatedDecoration.add(floorPos);
                            generatedDecoration.add(new Vector2(floorPos.x + 1, floorPos.y));
                            maxTables--;
                        }else if(random == 23 && maxTables>0 && checkForNeighborDec(floorPos, 1)){
                            decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), "TABLE_UP");
                            generatedDecoration.add(floorPos);
                            generatedDecoration.add(new Vector2(floorPos.x, floorPos.y + 1));
                            maxTables--;
                        }else if(random == 1 && maxDiamonds>0 && checkForNeighborDec(floorPos, 1)){
                            decorationMap.put(new Vector2((int)floorPos.x,(int)floorPos.y), "DIAMOND");
                            generatedDecoration.add(floorPos);
                            maxDiamonds--;
                        }

                    }
                }
            }
        }
        decorationMap.put(getStairsLocation(), "STAIRS");
        return decorationMap;
    }

    public void testClearMap(int type){
        if(type == 0){
            for(Vector2 v : generatedDungeon){
                layer.setCell((int)v.x, (int)v.y, null);
                Gdx.app.debug("PIERWSZA KOLEKCJA", "X: " + v.x + ", Y:" + v.y);
            }
        }else if(type == 1){
            for(Vector2 v : generatedDungeon){
                TiledMapTileLayer.Cell newCell = new TiledMapTileLayer.Cell();
                newCell.setTile(wallInnerCornerDownLeft);
                layer.setCell((int)v.x, (int)v.y, newCell);
                Gdx.app.debug("DRUGA KOLEKCJA", "X: " + v.x + ", Y:" + v.y);
            }
        }else if(type == 2){
            for(Point v : rooms){
                layer.setCell((int)v.x, (int)v.y, null);
            }
        }



    }

    public Vector2 getStairsLocation() {
        return generatedRooms.get(generatedRooms.size()-1).center;

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
                    if(decorationMap.get(positionToCheck).equals("LAMP"))
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



    private void paintFloors() {
        generatedDungeon = roomDungeonGenerator.createRooms();
        rooms = new Array<Point>();
        for(Vector2 v: generatedDungeon){
            rooms.add(new Point((int)v.x,(int)v.y));
        }
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

    public void setPlayerSpawnPoint() {
        Vector2 location = generatedRooms.get(0).center;
        generatedRooms.get(0).setPlayerStartRoom(true);


        int posX = (int)location.x * 16;
        int posY = (int)location.y * 16;
        Rectangle point = new Rectangle(posX, posY, 0, 0);

        RectangleMapObject rectangleMapObject = new RectangleMapObject();
        rectangleMapObject.getRectangle().set(point);

        playerStartLayrt.getObjects().add(rectangleMapObject);

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
        int tileSize = layer.getTileHeight();
        int rectX = (int)(position.x * tileSize);
        int rectY = (int)(position.y * tileSize);
        // Utwórz obiekt MapObject reprezentujący prostokąt
        Rectangle rectangle = new Rectangle(rectX, rectY, 0.5f, tileSize);
        RectangleMapObject rectangleMapObject = new RectangleMapObject();
        rectangleMapObject.getRectangle().set(rectangle);
        layerObjectCollision.getObjects().add(rectangleMapObject);

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

    private void paintSingleTile(TiledMapTileLayer layer, TiledMapTileLayer.Cell cell, Vector2 position){
        layer.setCell((int)position.x, (int)position.y, cell);
    }

    public void desroyGameObjects() {
        for(final Entity entity : ecsEngine.getEntities()){
            if(ECSEngine.gameObjectMapper.get(entity) != null){
                gameObjectToRemove.add(entity);
            }
        }
        for(final Entity entity : gameObjectToRemove){
            ecsEngine.removeEntity(entity);
        }
        gameObjectToRemove.clear();
    }

    private void makeObjects() {
        decorationMap = decorateDungeon();
        Sprite barrel = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("barrel");
        Sprite table = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("tableLong");//("table");
        Sprite tableUp = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("tableUp");//("table");
        Sprite box = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("box");
        Sprite lamp = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("lamp");
        Sprite diamond = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("diamond");
        Sprite stairs = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("fireball01");

        for (java.util.Map.Entry<Vector2, String> entry : decorationMap.entrySet()) {
            //Gdx.app.debug("MakeObjects", entry.getKey() + " " + entry.getValue());
            Vector2 position = new Vector2(entry.getKey().x / 2, entry.getKey().y / 2);
//            if (Objects.requireNonNull(entry.getValue()) == "BARREL") {
            switch (entry.getValue()) {
                case "BARREL":
                    ecsEngine.createSampleObject(position, barrel, GameObjectType.BARREL, BIT_DESTROYABLE);
                    break;
                case "LAMP":
                    ecsEngine.createSampleObject(position, lamp, GameObjectType.LAMP, BIT_LIGHT_OBJECT);
                    break;
                case "BOX":
                    ecsEngine.createSampleObject(position, box, GameObjectType.BOX, BIT_DESTROYABLE);
                    break;
                case "TABLE":
                    ecsEngine.createSampleObject(position, table, GameObjectType.TABLE, BIT_DESTROYABLE);
                    break;
                case "TABLE_UP":
                    ecsEngine.createSampleObject(position, tableUp, GameObjectType.TABLE_UP, BIT_DESTROYABLE);
                    break;
                case "DIAMOND":
                    ecsEngine.createSampleObject(position, diamond, GameObjectType.DIAMOND, BIT_GAME_OBJECT);
                    break;
                case "STAIRS":
                    ecsEngine.createSampleObject(position, stairs, GameObjectType.STAIRS, BIT_GAME_OBJECT);
                    break;
                default:
                    Gdx.app.debug("MakeObjects", "Obiekt o nieobsługiwanym kluczu");
                    break;
            }
        }



    }

    private void destroyCollisionAreas() {
        Array<Body> list = new Array<>();
        world.getBodies(list);
        for(Body body : list){
            if (body != null && body.getUserData() != null && body.getUserData().equals("GROUND")) {
                world.destroyBody(body);
            }
        }

    }

    public void makeCollisionAreas(){
        for(final CollisionArea collisionArea : currentMap.getCollisionAreas()){
            resetBodiesAndFixtureDefinitions();

            bodyDef.position.set(collisionArea.getX(), collisionArea.getY());
            bodyDef.fixedRotation = true;
            final Body body = world.createBody(bodyDef);
            body.setUserData("GROUND");

            fixtureDef.filter.categoryBits = BIT_GROUND;
            fixtureDef.filter.maskBits = -1;
            final ChainShape chainShape = new ChainShape();
            chainShape.createChain(collisionArea.getVertices());
            fixtureDef.shape = chainShape;
            body.createFixture(fixtureDef);
            chainShape.dispose();
        }
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
        for(MapObject mapObject : playerStartLayrt.getObjects()){
            playerStartLayrt.getObjects().remove(mapObject);
        }
        Array<MapObject> objectsToRemove = new Array<MapObject>();
        for(MapObject mapObject : layerObjectCollision.getObjects()){
            objectsToRemove.add(mapObject);
        }
        for(MapObject objToRemove : objectsToRemove){
            layerObjectCollision.getObjects().remove(objToRemove);
        }

        generatedDecoration = new HashSet<Vector2>();
        generatedCorridors = new ArrayList<Vector2>();
        generatedWalls = new HashSet<Vector2>();
        generatedRooms = new ArrayList<Room>();
        generatedDungeon = new HashSet<Vector2>();
        decorationMap = new HashMap<Vector2, String>();

    }

    public void resetMap(){
        clearMap();
        destroyCollisionAreas();
        paintFloors();

        currentMap.resetMapToDefault();
        Array<Body> bodiesToRemove = new Array<Body>();
        world.getBodies(bodiesToRemove);
        for(Body body : bodiesToRemove){
            if(body.getUserData() instanceof Entity){
                final Entity entity = (Entity) body.getUserData();
                if(!playerEntity.contains(entity, false)){
                    entity.add(new RemoveComponent());
                }

            }
        }


        final ArrayList<Room> generatedRooms = getGeneratedRooms(); //zle wyjscie na razie do testu
        playerEntity.get(0).getComponent(B2DComponent.class).body.setTransform(generatedRooms.get(0).center.x / 2, generatedRooms.get(0).center.y / 2, 0);

        decorateDungeon();
        makeObjects();
        makeCollisionAreas();
    }

    public void loadMap(HashSet<Vector2> savedDungeon, ArrayList<Vector2> savedCorridors, HashMap<Vector2, String> decorationMap){

        clearMap();
        destroyCollisionAreas();
        loadFloors(savedDungeon, savedCorridors);

        currentMap.resetMapToDefault();
        Array<Body> bodiesToRemove = new Array<Body>();
        world.getBodies(bodiesToRemove);
        for(Body body : bodiesToRemove){
            if(body.getUserData() instanceof Entity){
                final Entity entity = (Entity) body.getUserData();
                if(!playerEntity.contains(entity, false)){
                    entity.add(new RemoveComponent());
                }

            }
        }




        loadObjects(decorationMap);
        makeCollisionAreas();


    }

    public void loadFloors(HashSet<Vector2> savedDungeon, ArrayList<Vector2> savedCorridors){
        generatedDungeon = savedDungeon;
        rooms = new Array<Point>();
        for(Vector2 v: generatedDungeon){
            rooms.add(new Point((int)v.x,(int)v.y));
        }
        generatedCorridors = savedCorridors;
        for(Vector2 cell : savedDungeon){
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

        CreateWalls(savedDungeon);



    }

    private void loadObjects(HashMap<Vector2, String> decorationMap) {

        Sprite barrel = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("barrel");
        Sprite table = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("tableLong");//("table");
        Sprite tableUp = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("tableUp");//("table");
        Sprite box = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("box");
        Sprite lamp = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("lamp");
        Sprite diamond = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("diamond");
        Sprite stairs = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("fireball01");

        for (java.util.Map.Entry<Vector2, String> entry : decorationMap.entrySet()) {
            //Gdx.app.debug("MakeObjects", entry.getKey() + " " + entry.getValue());
            Vector2 position = new Vector2(entry.getKey().x / 2, entry.getKey().y / 2);
//            if (Objects.requireNonNull(entry.getValue()) == "BARREL") {
            switch (entry.getValue()) {
                case "BARREL":
                    ecsEngine.createSampleObject(position, barrel, GameObjectType.BARREL, BIT_DESTROYABLE);
                    break;
                case "LAMP":
                    ecsEngine.createSampleObject(position, lamp, GameObjectType.LAMP, BIT_LIGHT_OBJECT);
                    break;
                case "BOX":
                    ecsEngine.createSampleObject(position, box, GameObjectType.BOX, BIT_DESTROYABLE);
                    break;
                case "TABLE":
                    ecsEngine.createSampleObject(position, table, GameObjectType.TABLE, BIT_DESTROYABLE);
                    break;
                case "TABLE_UP":
                    ecsEngine.createSampleObject(position, tableUp, GameObjectType.TABLE_UP, BIT_DESTROYABLE);
                    break;
                case "DIAMOND":
                    ecsEngine.createSampleObject(position, diamond, GameObjectType.DIAMOND, BIT_GAME_OBJECT);
                    break;
                case "STAIRS":
                    ecsEngine.createSampleObject(position, stairs, GameObjectType.STAIRS, BIT_GAME_OBJECT);
                    break;
                default:
                    Gdx.app.debug("MakeObjects", "Obiekt o nieobsługiwanym kluczu");
                    break;
            }

        }
    }



    public Map getCurrentMap() {
        return currentMap;
    }

    public HashSet<Vector2> getGeneratedDungeon() {
        return generatedDungeon;
    }

    public HashSet<Vector2> getGeneratedWalls() {
        return generatedWalls;
    }

    public HashSet<Vector2> getGeneratedDecoration() {
        return generatedDecoration;
    }

    public ArrayList<Room> getGeneratedRooms() {
        return generatedRooms;
    }

    public ArrayList<Vector2> getGeneratedCorridors() {
        return generatedCorridors;
    }

    public void setCurrentMap(Map currentMap) {
        this.currentMap = currentMap;
    }
}
