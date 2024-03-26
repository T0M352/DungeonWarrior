package com.tomesz.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.B2DComponent;
import com.tomesz.game.ecs.components.GameObjectComponent;
import com.tomesz.game.ecs.components.PlayerComponent;
import com.tomesz.game.map.GameObjectType;
import com.tomesz.game.map.MapManager;
import com.tomesz.game.map.dungeonGenerator.Room;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class PreferenceManager implements Json.Serializable {
    private final Preferences preferences;
    private final Json json;

    private final JsonReader jsonReader;
    private final DungeonWarrior context;

    private HashSet<Vector2> testList;


    private final Vector2 playerPos;
    private int diamonds;
    private int mana;
    private int health;

//    private HashSet<Vector2> savedDungeon;
    private Array<Point> savedDungeon;
    private ArrayList<Vector2> savedCorridors;
    private ArrayList<Room> savedRooms;
    private HashSet<Vector2> savedWalls;
    private HashMap<Vector2, String> decorationMap;
    private HashSet<String> decorationMapAsString;

    public PreferenceManager(DungeonWarrior context){
        preferences = Gdx.app.getPreferences("DungeonWarrior");
        json = new Json();
        jsonReader = new JsonReader();
        playerPos = new Vector2();
        this.context = context;
        decorationMapAsString = new HashSet<>();

    }



    public boolean containsKey(final String key){
        return preferences.contains(key);
    }

    public void setFloatValue(final String key, final float value){
        preferences.putFloat(key,value);
        preferences.flush();
    }

    public float getFloatValue(final String key){
        return  preferences.getFloat(key);
    }

    public void saveGameState(final Entity player, final MapManager mapManager){
//        preferences.putString("GAME_STATE", new Json().toJson(PlayerComponent));   <-- mozna tak zrobic i zapisuje wszystkie pola PlayerComponenta
        decorationMap = new HashMap<Vector2, String>();
        decorationMapAsString = new HashSet<String>();
        //pozycja gracza
        playerPos.set(ECSEngine.b2DComponentCmpMapper.get(player).body.getPosition()); ;

        //diamenty gracza
        diamonds = ECSEngine.playerCmpMapper.get(player).getDiamonds();
        mana = (int)ECSEngine.playerCmpMapper.get(player).mana;
        health = (int)ECSEngine.playerCmpMapper.get(player).health;
        //mapa
        savedDungeon = mapManager.getRooms();
        savedCorridors = mapManager.getGeneratedCorridors();
//        savedRooms = context.getMapManager().getMapPainter().getGeneratedRooms();


//        for (java.util.Map.Entry<Vector2, String> entry : decorationMap.entrySet()) {
//            int x = (int)entry.getKey().x;
//            int y = (int)entry.getKey().y;
//            String s = x + "/" + y + "/" + entry.getValue();
//            decorationMapAsString.add(s);
//        }
        Array<Body> bodies = new Array<Body>();
        context.getWorld().getBodies(bodies);
        for(Body body : bodies){
            if(body.getUserData() instanceof Entity){
                Entity entity = (Entity) body.getUserData();
                if(entity.getComponent(GameObjectComponent.class) != null){
                    GameObjectComponent component = entity.getComponent(GameObjectComponent.class);
                    String type = component.type.toString();
                    String s = getString(body, component, type);
                    decorationMapAsString.add(s);
                }
            }

        }

        preferences.putString("GAME_STATE", new Json().toJson(this)); //potem z tego robimy zapis w metodzie write


        preferences.flush();
    }

    private static String getString(Body body, GameObjectComponent component, String type) {
        int x, y;
        if(component.type == GameObjectType.TABLE){
            x = (int) ((body.getPosition().x * 2) - 0.25f);
            y = (int) ((body.getPosition().y * 2));
        }else if(component.type == GameObjectType.TABLE_UP){
            x = (int) ((body.getPosition().x * 2));
            y = (int) ((body.getPosition().y * 2) - 0.25f);
        }else{
            x = (int) ((body.getPosition().x * 2) + 0.125f);
            y = (int) ((body.getPosition().y * 2) + 0.125f);
        }
        String s = x + "/" + y + "/" + type;
        return s;
    }

    public void loadGameState(final Entity player){
        final JsonValue savedJsonString = jsonReader.parse(preferences.getString("GAME_STATE"));
        //pozycja gracza
        final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(player);
        b2DComponent.body.setTransform(savedJsonString.getFloat("PLAYER_X", 0f), savedJsonString.getFloat("PLAYER_Y", 0f), b2DComponent.body.getAngle());

        //diamenty
        PlayerComponent playerComponent = ECSEngine.playerCmpMapper.get(player);
        playerComponent.setDiamonds(savedJsonString.getInt("DIAMONDS_COUNT",  0));
        playerComponent.mana=savedJsonString.getInt("SAVED_MANA",  0);
        playerComponent.health=savedJsonString.getInt("SAVED_HP",  0);


        //mapa
        HashSet<Vector2> SavedDungeon = new HashSet<Vector2>();
        JsonValue dungeonArray = savedJsonString.get("SAVED_DUNGEON");
        for (JsonValue vectorValue : dungeonArray) {
            float x = vectorValue.getInt("x", 0);
            float y = vectorValue.getInt("y", 0);
            SavedDungeon.add(new Vector2(x, y));
        }

        ArrayList<Vector2> SavedCorridors = new ArrayList<Vector2>();
        JsonValue corridorArray = savedJsonString.get("SAVED_CORRIDORS");
        for (JsonValue vectorValue : dungeonArray) {
            float x = vectorValue.getFloat("x", 0f);
            float y = vectorValue.getFloat("y", 0f);
            SavedDungeon.add(new Vector2(x, y));
        }

        HashMap<Vector2, String> DecorationMap = new HashMap<>();
        JsonValue decorationArray = savedJsonString.get("SAVED_DECORATION");
        for (JsonValue vectorValue : decorationArray) {

            String [] splitedStr = vectorValue.getString(1).split("/");
            DecorationMap.put(new Vector2(Integer.parseInt(splitedStr[0]), Integer.parseInt(splitedStr[1])), splitedStr[2]);
        }


        context.getMapManager().loadMap(SavedDungeon, SavedCorridors, DecorationMap);
    }

    @Override
    public void write(Json json) {
        json.writeValue("PLAYER_X", playerPos.x);
        json.writeValue("PLAYER_Y", playerPos.y);
        json.writeValue("DIAMONDS_COUNT", diamonds);
        json.writeValue("SAVED_DUNGEON", savedDungeon);
        json.writeValue("SAVED_CORRIDORS", savedCorridors);
        json.writeValue("SAVED_DECORATION", decorationMapAsString);
        json.writeValue("SAVED_MANA", mana);
        json.writeValue("SAVED_HP", health);
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {

    }
}
