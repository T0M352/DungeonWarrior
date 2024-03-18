package com.tomesz.game.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.tomesz.game.DungeonWarrior;

import static com.tomesz.game.DungeonWarrior.UNIT_SCALE;

public class Map {
    private final TiledMap tiledMap;
    private  Array<CollisionArea> collisionAreas;
    private Vector2 startLocation;
    private Array<GameObject> gameObjects;
    private IntMap<Animation<Sprite>> mapAnimation;


    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public Array<GameObject> getGameObjects() {
        return gameObjects;
    }

    public IntMap<Animation<Sprite>> getMapAnimation() {
        return mapAnimation;
    }



    public Map(final TiledMap tiledMap, DungeonWarrior context){
        this.tiledMap = tiledMap;
        collisionAreas = new Array<CollisionArea>();
        startLocation = new Vector2();
        mapAnimation = new IntMap<Animation<Sprite>>();
        gameObjects = new Array<GameObject>();

        //
        parseCollisionLayer();
        parsePlayerStartLocation();


    }


    private void parseGameObjectLayer() {
        final MapLayer gameObjectLayer = tiledMap.getLayers().get("ObjectLayer");
        if(gameObjectLayer == null){
            Gdx.app.debug("BLAD", "BRAK WARSTWY GAMEOBEJCT");
            return;
        }

        final MapObjects objects = gameObjectLayer.getObjects();
        for(final MapObject mapObject : objects){
            if(!(mapObject instanceof TiledMapTileMapObject)){
                continue;
            }

            final TiledMapTileMapObject tileMapObject = (TiledMapTileMapObject) mapObject;
            final MapProperties tiledMapProperties = tileMapObject.getProperties();
            final MapProperties tileProperties = tileMapObject.getTile().getProperties();
            final GameObjectType gameObjectType;
            if(tiledMapProperties.containsKey("type")){
                gameObjectType = GameObjectType.valueOf(tiledMapProperties.get("type", String.class));
            }else if(tiledMapProperties.containsKey("type")){
                gameObjectType = GameObjectType.valueOf(tileProperties.get("type", String.class));
            }else{
                Gdx.app.log("DEBUg", "BRAK OBIEKTU O ZDEFINIOWANYM TYPIE");
                continue;
            }

            final int animationIndex = tileMapObject.getTile().getId();
            if(!createAnimation(animationIndex, tileMapObject.getTile())){
                Gdx.app.log("DEBUG", "NIE UDALO SIE STWORZYC ANIMACJI DLA KAFELAK O ID ");
                continue;
            }

            final float height = tiledMapProperties.get("height", Float.class) * UNIT_SCALE;
            final float width = tiledMapProperties.get("width", Float.class) * UNIT_SCALE;

            Gdx.app.debug("BLAD", "DODAJE OBIEKT: " + gameObjectType.toString() + ", " +tileMapObject.getX() * UNIT_SCALE + ", " + tileMapObject.getY() * UNIT_SCALE + ", " +width+ ", " +height+ ", " + tileMapObject.getRotation() + ", " + animationIndex);
            gameObjects.add(new GameObject(gameObjectType, new Vector2(tileMapObject.getX() * UNIT_SCALE, tileMapObject.getY() * UNIT_SCALE), width,
                    height, tileMapObject.getRotation() ,animationIndex));

        }
    }

    private boolean createAnimation(int animationIndex, TiledMapTile tile) {
        Animation<Sprite> animation = mapAnimation.get(animationIndex);
        if (animation == null) {
            Gdx.app.debug("MAP", "TWORZE NOWA ANIMACJE DLA KAFELKA " + tile.getId());
            if(tile instanceof AnimatedTiledMapTile){

                final AnimatedTiledMapTile aniTile = (AnimatedTiledMapTile) tile;
                final Sprite[] keyFrames = new Sprite[aniTile.getFrameTiles().length];
                int i = 0;
                for(final StaticTiledMapTile staticTile : aniTile.getFrameTiles()){
                    keyFrames[i++] = new Sprite(staticTile.getTextureRegion());
                }
                animation = new Animation<Sprite>(aniTile.getAnimationIntervals()[0] * 0.001f, keyFrames);
                animation.setPlayMode(Animation.PlayMode.LOOP);
                mapAnimation.put(animationIndex, animation);

            }else if(tile instanceof  StaticTiledMapTile){
                animation = new Animation<Sprite>(0, new Sprite(tile.getTextureRegion()));
                mapAnimation.put(animationIndex, animation);
            }else{
                Gdx.app.debug("MAP", "OBIEKT O KAFELKU " + tile + " NIE WSPIERANY DLA OBIEKTOW/ANIMACJI MAPY");
                return false;
            }

        }
        return true;
    }

    public void resetMapToDefault(){
        collisionAreas.clear();
        parseCollisionLayer();
    }

    public void parsePlayerStartLocation() {
        final MapLayer startLocationLayer = tiledMap.getLayers().get("PlayerStartLocation");
        if(startLocationLayer == null){
            Gdx.app.debug("MAP", "Brak warstwy startu gracza");
            return;
        }

        final MapObjects objects = startLocationLayer.getObjects();
        for(final MapObject mapObj : objects){
            if(mapObj instanceof RectangleMapObject){
                final RectangleMapObject playerStartLocation = (RectangleMapObject) mapObj;
                final Rectangle rec = playerStartLocation.getRectangle();
                startLocation.set(rec.x * UNIT_SCALE, rec.y * UNIT_SCALE);
            }
        }
    }

    public Vector2 getStartLocation() {
        return startLocation;
    }

    public void parseCollisionLayer() {
        final MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
        if(collisionLayer == null){
            Gdx.app.debug("MAP", "Brak warstwy kolizji");
            return;
        }

        final MapObjects mapObject = collisionLayer.getObjects();
        if(mapObject==null){
            Gdx.app.debug("MAP", "Brak obiektów kolizji");
            return;
        }

        for(final MapObject mapObj : mapObject){
            if(mapObj instanceof  RectangleMapObject){

                final RectangleMapObject rectangleMapObject = (RectangleMapObject) mapObj;
                final Rectangle rectangle = rectangleMapObject.getRectangle();
                final float[] rectVertices = new float[10];

                //left-bottom
                rectVertices[0] = 0;
                rectVertices[1] = 0;

                //left-top
                rectVertices[2] = 0;
                rectVertices[3] = rectangle.height;

                //right-top
                rectVertices[4] = rectangle.width;
                rectVertices[5] = rectangle.height;

                //right-bottom
                rectVertices[6] = rectangle.width;
                rectVertices[7] = 0;

//                //left-bottom
//                rectVertices[8] = 0;
//                rectVertices[9] = 0;

                collisionAreas.add(new CollisionArea(rectangle.x, rectangle.y, rectVertices));
            }else{
                Gdx.app.debug("MAP", "Obiekt " + mapObj + " nie wspierany w systemie kolizji");
            }
        }
    }

    public Array<CollisionArea> getCollisionAreas() {
        return collisionAreas;
    }


}
