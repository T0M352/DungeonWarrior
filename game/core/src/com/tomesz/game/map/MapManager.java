package com.tomesz.game.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Version;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
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
import com.tomesz.game.map.dungeonGenerator.MapPainter;
import com.tomesz.game.map.dungeonGenerator.Room;
import com.tomesz.game.screen.ScreenType;

import java.util.ArrayList;
import java.util.HashMap;

import static com.tomesz.game.DungeonWarrior.*;

public class MapManager {
    private final World world;
    private final Array<Body> bodies;

    private  final AssetManager assetManager;
    private  Map currentMap;
    private final Array<Map> mapCache;
    private final Array<MapListener> listeners;

    private final DungeonWarrior context;
    private MapPainter mapPainter;
    private final ECSEngine ecsEngine;
    private Array<Entity> gameObjectToRemove;
    private HashMap<Vector2, String> decorationMap = new HashMap<Vector2,String>();

    private ImmutableArray<Entity> playerEntity;

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
        mapPainter = null;
        playerEntity = ecsEngine.getEntitiesFor(Family.all(PlayerComponent.class).get());
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

        if(mapPainter == null){
            mapPainter = new MapPainter(context, tiledMap);
        }


        mapPainter.paintFloors();


        currentMap = new Map(tiledMap);
        mapCache.add(currentMap);



        makeCollisionAreas();



        makeObjects();

        for(final MapListener listener: listeners){
            listener.mapChange(currentMap);
        }
    }

    private void desroyGameObjects() {
        for(final Entity entity : ecsEngine.getEntities()){
            if(ecsEngine.gameObjectMapper.get(entity) != null){
                gameObjectToRemove.add(entity);
            }
        }
        for(final Entity entity : gameObjectToRemove){
            ecsEngine.removeEntity(entity);
        }
        gameObjectToRemove.clear();
    }

    private void makeObjects() {
//        for(final GameObject gameObject : currentMap.getGameObjects()){
//            ecsEngine.createGameObject(gameObject);
//        }
        decorationMap = mapPainter.decorateDungeon();
        Sprite barrel = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("barrel");
        Sprite table = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("tableLong");//("table");
        Sprite box = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("box");
        Sprite lamp = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("lamp");
        Sprite diamond = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("diamond");
        Sprite stairs = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("fireball01");

        for (java.util.Map.Entry<Vector2, String> entry : decorationMap.entrySet()) {
            //Gdx.app.debug("MakeObjects", entry.getKey() + " " + entry.getValue());
            Vector2 position = new Vector2(entry.getKey().x / 2, entry.getKey().y / 2);
            switch (entry.getValue()) {
                case "BARREL":
                    ecsEngine.createSampleObject(position, barrel, GameObjectType.BARREL, BIT_GAME_OBJECT);
                    break;
                case "LAMP":
                    ecsEngine.createSampleObject(position, lamp, GameObjectType.LAMP, BIT_LIGHT_OBJECT);
                    break;
                case "BOX":
                    ecsEngine.createSampleObject(position, box, GameObjectType.BOX, BIT_GAME_OBJECT);
                    break;
                case "TABLE":
                    ecsEngine.createSampleObject(position, table, GameObjectType.TABLE, BIT_TABLE);
                    break;
                case "DIAMOND":
                    ecsEngine.createSampleObject(position, diamond, GameObjectType.DIAMOND, BIT_LIGHT_OBJECT);
                    break;
                case "STAIRS":
                    ecsEngine.createSampleObject(position, stairs, GameObjectType.STAIRS, BIT_GAME_OBJECT);

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

    public void resetMap(){
        mapPainter.clearMap();
        destroyCollisionAreas();
        mapPainter.paintFloors();

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


        final ArrayList<Room> generatedRooms = mapPainter.getGeneratedRooms(); //zle wyjscie na razie do testu
        playerEntity.get(0).getComponent(B2DComponent.class).body.setTransform(generatedRooms.get(0).center.x / 2, generatedRooms.get(0).center.y / 2, 0);

        mapPainter.decorateDungeon();
        makeObjects();


        makeCollisionAreas();




    }

    public Map getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(Map currentMap) {
        this.currentMap = currentMap;
    }
}
