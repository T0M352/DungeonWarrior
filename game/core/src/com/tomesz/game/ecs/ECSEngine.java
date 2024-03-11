package com.tomesz.game.ecs;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.components.AnimationComponent;
import com.tomesz.game.ecs.components.B2DComponent;
import com.tomesz.game.ecs.components.GameObjectComponent;
import com.tomesz.game.ecs.components.PlayerComponent;
import com.tomesz.game.ecs.system.*;
import com.tomesz.game.map.GameObject;
import com.tomesz.game.map.GameObjectType;
import com.tomesz.game.view.AnimationType;

import static com.tomesz.game.DungeonWarrior.*;

public class ECSEngine extends PooledEngine {

    public static final ComponentMapper<PlayerComponent> playerCmpMapper = ComponentMapper.getFor(PlayerComponent.class);
    public static final ComponentMapper<B2DComponent> b2DComponentCmpMapper = ComponentMapper.getFor(B2DComponent.class);
    public static final ComponentMapper<AnimationComponent> animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
    public static final ComponentMapper<GameObjectComponent> gameObjectMapper = ComponentMapper.getFor(GameObjectComponent.class);
    final World world;

    private final Vector2 localPosition;
    private final Vector2 posBeforeRotation;
    private final Vector2 posAfterRotation;

    private final RayHandler rayHandler;



    public ECSEngine(final DungeonWarrior context) {
        super();
        world = context.getWorld();
        localPosition = new Vector2();
        posBeforeRotation = new Vector2();
        posAfterRotation = new Vector2();
        rayHandler = context.getRayHandler();


        this.addSystem(new PlayerMovementSystem(context)); //kazdy system bedacy czescia silnika jest aktualizowany co klatke
        this.addSystem(new PlayerCameraSystem(context)); //kazdy system bedacy czescia silnika jest aktualizowany co klatke
        this.addSystem(new AnimationSystem(context));
        this.addSystem(new PlayerAnimationSystem(context));
        this.addSystem(new LightSystem());
        this.addSystem(new PlayerCollisionSystem(context));

        //createSampleObject();
    }

    public void createSampleObject(Vector2 position, Sprite sprite, GameObjectType type, short bitType) {
        final Entity gameObjEntity = this.createEntity();

        final GameObjectComponent gameObjectComponent = this.createComponent(GameObjectComponent.class);
        gameObjectComponent.animationIndex = 1;

        gameObjectComponent.type = type;

        gameObjectComponent.sprite = sprite;
        gameObjEntity.add(gameObjectComponent);

        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = null;
        if(type == GameObjectType.TABLE){
            animationComponent.width = 1f;
            animationComponent.height = 0.5f;

        }else{
            animationComponent.width = 0.5f;
            animationComponent.height = 0.5f;
        }

        gameObjEntity.add(animationComponent);

        DungeonWarrior.resetBodiesAndFixtureDefinitions();
        final float halfW = animationComponent.width * 0.5f;
        final float halfH = animationComponent.height * 0.5f;
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);

        DungeonWarrior.bodyDef.position.set(position.x + halfW, position.y+ halfH);
        //DungeonWarrior.bodyDef.position.set(0, 1);
        DungeonWarrior.bodyDef.type = BodyDef.BodyType.StaticBody;
        b2DComponent.body = world.createBody(DungeonWarrior.bodyDef);
        b2DComponent.body.setUserData(gameObjEntity);
        b2DComponent.widht = animationComponent.width;
        b2DComponent.height = animationComponent.height;


        //b2DComponent.body.setTransform(b2DComponent.body.getPosition().add(posBeforeRotation).sub(posAfterRotation), 0);
        b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - animationComponent.width * 0.5f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f);

        //b2DComponent.renderPosition.set(position.x, position.y);
        DungeonWarrior.fixtureDef.filter.categoryBits = BIT_GAME_OBJECT;//bitType;
        DungeonWarrior.fixtureDef.filter.maskBits = BIT_PLAYER;
        final PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(halfW, halfH);
        DungeonWarrior.fixtureDef.shape = pShape;
        b2DComponent.body.createFixture(DungeonWarrior.fixtureDef);
        pShape.dispose();

        //swiatlo
        if(type == GameObjectType.LAMP){
            b2DComponent.lightDistance = 6.5f;
            b2DComponent.lightFluctuationDistance = 0.5f;
            b2DComponent.lightFluctuationSpeed = 2;
            b2DComponent.light = new PointLight(rayHandler, 64, new Color(1,0.8f,0.8f,0.6f), b2DComponent.lightDistance, b2DComponent.body.getPosition().x, b2DComponent.body.getPosition().y);
            b2DComponent.lightFluctuationTime= b2DComponent.light.getDirection() * 0.16f;
        }else if(type == GameObjectType.DIAMOND){
            b2DComponent.lightDistance = 1f;
            b2DComponent.lightFluctuationDistance = 1f;
            b2DComponent.lightFluctuationSpeed = 4;
            b2DComponent.light = new PointLight(rayHandler, 64, new Color(0,0,1,0.75f), b2DComponent.lightDistance, b2DComponent.body.getPosition().x, b2DComponent.body.getPosition().y);
            b2DComponent.lightFluctuationTime= b2DComponent.light.getDirection() * 0.16f;
        }


        gameObjEntity.add(b2DComponent);

        this.addEntity(gameObjEntity);
    }

    public void createPlayer(final Vector2 playerSpawnLocation){ //final float widht, final float heiight
        final Entity player = this.createEntity();

        //komponenty
        player.add(this.createComponent(PlayerComponent.class));

        resetBodiesAndFixtureDefinitions();
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);
        bodyDef.position.set(playerSpawnLocation.x, playerSpawnLocation.y);
        bodyDef.fixedRotation =true;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2DComponent.body = world.createBody(bodyDef);
        b2DComponent.body.setUserData(player);
        b2DComponent.widht = 0.1f;
        b2DComponent.height = 0.2f;
        b2DComponent.renderPosition.set(b2DComponent.body.getPosition());

        fixtureDef.filter.categoryBits = BIT_PLAYER;
        fixtureDef.filter.maskBits = -1;
        final PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(0.1f, 0.2f);
        fixtureDef.shape = pShape;
        b2DComponent.body.createFixture(fixtureDef);
        pShape.dispose();

        //swiatlo
//        b2DComponent.lightDistance = 4;
//        b2DComponent.lightFluctuationDistance = 0;
//        b2DComponent.lightFluctuationSpeed = 2;
//        b2DComponent.light = new PointLight(rayHandler, 64, new Color(1,1,1,0.55f), b2DComponent.lightDistance, b2DComponent.body.getPosition().x, b2DComponent.body.getPosition().y);
//        b2DComponent.lightFluctuationTime= b2DComponent.light.getDirection() * 0.16f;
//        b2DComponent.light.attachToBody(b2DComponent.body);


        player.add(b2DComponent);

        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = AnimationType.MAGE_IDLE;
        animationComponent.width = 16 * UNIT_SCALE;
        animationComponent.height = 16 * UNIT_SCALE;
        player.add(animationComponent);
        this.addEntity(player);
    }





}
