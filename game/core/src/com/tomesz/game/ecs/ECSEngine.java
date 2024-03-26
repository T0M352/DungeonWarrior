package com.tomesz.game.ecs;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.components.*;
import com.tomesz.game.ecs.system.*;
import com.tomesz.game.map.GameObjectType;
import com.tomesz.game.view.AnimationType;

import static com.tomesz.game.DungeonWarrior.*;

public class ECSEngine extends PooledEngine {

    public static final ComponentMapper<PlayerComponent> playerCmpMapper = ComponentMapper.getFor(PlayerComponent.class);
    public static final ComponentMapper<B2DComponent> b2DComponentCmpMapper = ComponentMapper.getFor(B2DComponent.class);
    public static final ComponentMapper<AnimationComponent> animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
    public static final ComponentMapper<LegAnimationComponent> legAnimComponentMapper = ComponentMapper.getFor(LegAnimationComponent.class);
    public static final ComponentMapper<GameObjectComponent> gameObjectMapper = ComponentMapper.getFor(GameObjectComponent.class);
    public static final ComponentMapper<FireballComponent> fireballObjectMaper = ComponentMapper.getFor(FireballComponent.class);
    public static final ComponentMapper<EnemyFireball> enemyFireballComponentMapper = ComponentMapper.getFor(EnemyFireball.class);
    public static final ComponentMapper<EnemyComponent> enemyObjectMapper = ComponentMapper.getFor(EnemyComponent.class);
    public static final ComponentMapper<RoomEntranceComponent> roomEntranceComponentComponentMapper = ComponentMapper.getFor(RoomEntranceComponent.class);
    public static final ComponentMapper<DistanceEnemyComponent> distanceEnemyObjectMapper = ComponentMapper.getFor(DistanceEnemyComponent.class);

    final World world;

    private final Vector2 localPosition;
    private final Vector2 posBeforeRotation;
    private final Vector2 posAfterRotation;

    private final RayHandler rayHandler;

    private float recoil = 0.7f;



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
        this.addSystem(new LegAnimationSystem(context));
        this.addSystem(new LightSystem());
        this.addSystem(new FireballSystem(context));
        this.addSystem(new EnemyFireballSystem(context));
        this.addSystem(new RoomEntranceSystem(context));
        this.addSystem(new EnemySystem(context));
        this.addSystem(new DistanceEnemySystem(context));
        this.addSystem(new PlayerCollisionSystem(context));

        //createSampleObject();
    }

    public void createSampleObject(Vector2 position, Sprite sprite, GameObjectType type, short bitType) {
        final Entity gameObjEntity = this.createEntity();

        final GameObjectComponent gameObjectComponent = this.createComponent(GameObjectComponent.class);


        gameObjectComponent.animationIndex = 1;

        gameObjectComponent.type = type;

        gameObjectComponent.sprite = sprite;

        gameObjectComponent.health = 15;



        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = null;
        if(type == GameObjectType.TABLE){
            animationComponent.animationType = AnimationType.TABLE_END;
            animationComponent.width = 1f;
            animationComponent.height = 0.5f;

        }else if(type == GameObjectType.TABLE_UP){
            animationComponent.animationType = AnimationType.TABLEUP_END;
            animationComponent.width = 0.5f;
            animationComponent.height = 1f;
        }else if(type == GameObjectType.BARREL){
            animationComponent.animationType = AnimationType.BARREL_END;
            animationComponent.width = 0.5f;
            animationComponent.height = 0.5f;
        }else if(type == GameObjectType.BOX){
            animationComponent.animationType = AnimationType.BOX_END;
            animationComponent.width = 0.5f;
            animationComponent.height = 0.5f;
        }else if(type == GameObjectType.DIAMOND){
            animationComponent.width = 0.25f;
            animationComponent.height = 0.25f;
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
        DungeonWarrior.fixtureDef.filter.categoryBits = bitType;
        DungeonWarrior.fixtureDef.filter.maskBits = BIT_PLAYER | BIT_FIREBALL | BIT_ENEMY;
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
            b2DComponent.light = new PointLight(rayHandler, 64, new Color(1,0.8f,0.8f,0.5f), b2DComponent.lightDistance, b2DComponent.body.getPosition().x, b2DComponent.body.getPosition().y);
            b2DComponent.lightFluctuationTime= b2DComponent.light.getDirection() * 0.16f;
        }else if(type == GameObjectType.DIAMOND){
            b2DComponent.lightDistance = 1f;
            b2DComponent.lightFluctuationDistance = 1f;
            b2DComponent.lightFluctuationSpeed = 4;
            b2DComponent.light = new PointLight(rayHandler, 64, new Color(0,0,1,0.75f), b2DComponent.lightDistance, b2DComponent.body.getPosition().x, b2DComponent.body.getPosition().y);
            b2DComponent.lightFluctuationTime= b2DComponent.light.getDirection() * 0.16f;
        }else{
            b2DComponent.lightDistance = 0f;
            b2DComponent.lightFluctuationDistance = 0f;
            b2DComponent.lightFluctuationSpeed = 4;
            b2DComponent.light = new PointLight(rayHandler, 64, new Color(1,0,0,0.6f), b2DComponent.lightDistance, b2DComponent.body.getPosition().x, b2DComponent.body.getPosition().y);
            b2DComponent.lightFluctuationTime= b2DComponent.light.getDirection() * 0.16f;
        }


        gameObjEntity.add(b2DComponent);

        gameObjectComponent.position = new Vector2((int)(position.x * 2 + halfW), (int)(position.y * 2 + halfH));

        gameObjEntity.add(gameObjectComponent);

        this.addEntity(gameObjEntity);
    }

    public void createFireball(Vector2 playerPosition, Vector2 direction, Sprite sprite, int accuracy) {
        final Entity gameObjEntity = this.createEntity();

        final GameObjectComponent gameObjectComponent = this.createComponent(GameObjectComponent.class);
        gameObjectComponent.animationIndex = 1;

        gameObjectComponent.type = GameObjectType.FIREBALL;

        gameObjectComponent.sprite = sprite;
        gameObjectComponent.position = Vector2.Zero;
        gameObjEntity.add(gameObjectComponent);

        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = null;
            animationComponent.width = 0.25f;
            animationComponent.height = 0.25f;
        animationComponent.animationType = AnimationType.FIREBALL_END;

        gameObjEntity.add(animationComponent);

        DungeonWarrior.resetBodiesAndFixtureDefinitions();
        final float halfW = animationComponent.width * 0.5f;
        final float halfH = animationComponent.height * 0.5f;
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);







        DungeonWarrior.bodyDef.position.set(playerPosition.x + halfW, playerPosition.y+ halfH);
//        DungeonWarrior.bodyDef.position.set(fireballDirection.x + halfW, fireballDirection.y+ halfH);



        DungeonWarrior.bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2DComponent.body = world.createBody(DungeonWarrior.bodyDef);
        bodyDef.fixedRotation = true;
        b2DComponent.body.setUserData(gameObjEntity);
        b2DComponent.widht = animationComponent.width;
        b2DComponent.height = animationComponent.height;



        b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - animationComponent.width * 0.5f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f);






        //b2DComponent.renderPosition.set(position.x, position.y);
        DungeonWarrior.fixtureDef.filter.categoryBits = BIT_FIREBALL;//bitType;
        DungeonWarrior.fixtureDef.filter.maskBits = BIT_GROUND | BIT_DESTROYABLE | BIT_ENEMY;
        final PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(halfW, halfH);
        DungeonWarrior.fixtureDef.shape = pShape;
        b2DComponent.body.createFixture(DungeonWarrior.fixtureDef);
        pShape.dispose();


            b2DComponent.lightDistance = 0.5f;
            b2DComponent.lightFluctuationDistance = 0.3f;
            b2DComponent.lightFluctuationSpeed = 4;
            b2DComponent.light = new PointLight(rayHandler, 64, new Color(1,0,0,0.75f), b2DComponent.lightDistance, b2DComponent.body.getPosition().x, b2DComponent.body.getPosition().y);
            b2DComponent.lightFluctuationTime= b2DComponent.light.getDirection() * 0.16f;



        gameObjEntity.add(b2DComponent);

        final FireballComponent fireballComponent = this.createComponent(FireballComponent.class);

        //TWORZENIE OBRECZY DLA PRAWIDLOWEGO ODRZUTU
        // Oblicz kierunek od gracza do celu
        Vector2 directionToTarget = direction.cpy().sub(playerPosition).nor();
        // Przeskaluj kierunek do żądanej odległości
        Vector2 scaledDirection = directionToTarget.scl(5); // 5 to żądana odległość od gracza
        // Oblicz pozycję startową fireballa
        Vector2 fireballDirection = playerPosition.cpy().add(scaledDirection);

        if(accuracy == 0){
            int random = MathUtils.random(3);
            if(random == 0){
                fireballDirection = new Vector2(fireballDirection.x + MathUtils.random(recoil), fireballDirection.y + MathUtils.random(recoil));
            }else if(random == 1){
                fireballDirection = new Vector2(fireballDirection.x - MathUtils.random(recoil), fireballDirection.y - MathUtils.random(recoil));
            }else if(random == 3){
                fireballDirection = new Vector2(fireballDirection.x + MathUtils.random(recoil), fireballDirection.y - MathUtils.random(recoil));
            }
        }

        fireballComponent.setDirection(calculateDirection(b2DComponent.body.getPosition(), new Vector2(fireballDirection.x + 0.125f, fireballDirection.y + 0.125f)));
//        fireballComponent.setDirection(calculateDirection(b2DComponent.body.getPosition(), new Vector2(fireballDirection.x + 2f, fireballDirection.y + 2f)));
        gameObjEntity.add(fireballComponent);



        this.addEntity(gameObjEntity);
    }


    public void createEnemy(Vector2 position) {
        final Entity enemyEntity = this.createEntity();

        final LegAnimationComponent legAnimationComponent = this.createComponent(LegAnimationComponent.class);
        legAnimationComponent.width = 1f;
        legAnimationComponent.height = 1f;
        legAnimationComponent.animationType = AnimationType.KOBOLD_LEGS_IDLE_RIGHT;
        legAnimationComponent.isAnimating = true;
        legAnimationComponent.oneFrameResolution = 64;
        enemyEntity.add(legAnimationComponent);



        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = null;
        animationComponent.width = 1f;
        animationComponent.height = 1f;
        animationComponent.animationType = AnimationType.KOBOLD_RIGHT;
        animationComponent.isAnimating = true;
        animationComponent.oneFrameResolution = 64;
        enemyEntity.add(animationComponent);






        final EnemyComponent enemyComponent = this.createComponent(EnemyComponent.class);
        enemyComponent.damage = 10;
        enemyComponent.health = 100;
        enemyComponent.legsBack = AnimationType.KOBOLD_LEGS_WALK_BACK;;
        enemyComponent.legsFront = AnimationType.KOBOLD_LEGS_WALK_FRONT;
        enemyComponent.legsLeft = AnimationType.KOBOLD_LEGS_WALK_LEFT;
        enemyComponent.legsRight = AnimationType.KOBOLD_LEGS_WALK_RIGHT;
        enemyComponent.animationRight = AnimationType.KOBOLD_RIGHT;
        enemyComponent.animationFront = AnimationType.KOBOLD_FRONT;
        enemyComponent.animationLeft = AnimationType.KOBOLD_LEFT;
        enemyComponent.animationBack = AnimationType.KOBOLD_BACK;
        enemyComponent.legsIdleLeft= AnimationType.KOBOLD_LEGS_IDLE_LEFT;
        enemyComponent.legsIdleRight = AnimationType.KOBOLD_LEGS_IDLE_RIGHT;

        enemyEntity.add(enemyComponent);




        final float halfW = animationComponent.width * 0.5f;
        final float halfH = animationComponent.height * 0.5f;


        //COMPONENT DO KOLIZJI Z OTOCZENIEM
        DungeonWarrior.resetBodiesAndFixtureDefinitions();
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);




        DungeonWarrior.bodyDef.position.set(position.x + halfW, position.y+ halfH);
//        DungeonWarrior.bodyDef.position.set(position.x, position.y);
//        DungeonWarrior.bodyDef.position.set(fireballDirection.x + halfW, fireballDirection.y+ halfH);



        DungeonWarrior.bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2DComponent.body = world.createBody(DungeonWarrior.bodyDef);
        bodyDef.fixedRotation = false;
        b2DComponent.body.setUserData(enemyEntity);
        b2DComponent.widht = animationComponent.width;
        b2DComponent.height = animationComponent.height;

        b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - animationComponent.width * 0.7f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f); //TODO ZROB TO PRAWIDLOWO


        b2DComponent.renderPosition.set(position.x, position.y);
        DungeonWarrior.fixtureDef.filter.categoryBits = BIT_ENEMY;//bitType;
        DungeonWarrior.fixtureDef.filter.maskBits = BIT_PLAYER | BIT_GROUND | BIT_FIREBALL | BIT_ENEMY;
        final PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(halfW/4f, halfH/2f);

        DungeonWarrior.fixtureDef.shape = pShape;
        b2DComponent.body.createFixture(DungeonWarrior.fixtureDef);


        pShape.dispose();




        enemyEntity.add(b2DComponent);


        this.addEntity(enemyEntity);
    }

    public void createDistEnemy(Vector2 position) {
        final Entity enemyEntity = this.createEntity();

        final LegAnimationComponent legAnimationComponent = this.createComponent(LegAnimationComponent.class);
        legAnimationComponent.width = 1f;
        legAnimationComponent.height = 1f;
        legAnimationComponent.animationType = AnimationType.KOBOLD_LEGS_IDLE_RIGHT;
        legAnimationComponent.isAnimating = true;
        legAnimationComponent.oneFrameResolution = 64;
        enemyEntity.add(legAnimationComponent);



        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = null;
        animationComponent.width = 1f;
        animationComponent.height = 1f;
        animationComponent.animationType = AnimationType.KOBOLD_RIGHT;
        animationComponent.isAnimating = true;
        animationComponent.oneFrameResolution = 64;
        enemyEntity.add(animationComponent);


        final DistanceEnemyComponent distEnemyComponent = this.createComponent(DistanceEnemyComponent.class);
        distEnemyComponent.shootInRows = 0;
        enemyEntity.add(distEnemyComponent);


        final EnemyComponent enemyComponent = this.createComponent(EnemyComponent.class);
        enemyComponent.damage = 10;
        enemyComponent.health = 100;
        enemyComponent.legsBack = AnimationType.GOBLIN_SHAMAN_LEGS_RIGHT_WALK;;
        enemyComponent.legsFront = AnimationType.GOBLIN_SHAMAN_LEGS_LEFT_WALK;
        enemyComponent.legsLeft = AnimationType.GOBLIN_SHAMAN_LEGS_LEFT_WALK;
        enemyComponent.legsRight = AnimationType.GOBLIN_SHAMAN_LEGS_RIGHT_WALK;

        enemyComponent.animationRight = AnimationType.GOBLIN_SHAMAN_RIGHT;
        enemyComponent.animationFront = AnimationType.GOBLIN_SHAMAN_LEFT;
        enemyComponent.animationLeft = AnimationType.GOBLIN_SHAMAN_LEFT;
        enemyComponent.animationBack = AnimationType.GOBLIN_SHAMAN_RIGHT;

        enemyComponent.legsIdleLeft= AnimationType.GOBLIN_SHAMAN_LEGS_LEFT;
        enemyComponent.legsIdleRight = AnimationType.GOBLIN_SHAMAN_LEGS_RIGHT;

        enemyEntity.add(enemyComponent);



        DungeonWarrior.resetBodiesAndFixtureDefinitions();
        final float halfW = animationComponent.width * 0.5f;
        final float halfH = animationComponent.height * 0.5f;
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);







        DungeonWarrior.bodyDef.position.set(position.x + halfH, position.y + halfW);
//        DungeonWarrior.bodyDef.position.set(fireballDirection.x + halfW, fireballDirection.y+ halfH);



        DungeonWarrior.bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2DComponent.body = world.createBody(DungeonWarrior.bodyDef);
        bodyDef.fixedRotation = false;
        b2DComponent.body.setUserData(enemyEntity);
        b2DComponent.widht = animationComponent.width;
        b2DComponent.height = animationComponent.height;



//        b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - animationComponent.width * 0.7f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f); //TODO ZROB TO PRAWIDLOWO





        //b2DComponent.renderPosition.set(position.x, position.y);
        DungeonWarrior.fixtureDef.filter.categoryBits = BIT_ENEMY;//bitType;
        DungeonWarrior.fixtureDef.filter.maskBits = BIT_PLAYER | BIT_GROUND | BIT_FIREBALL;
        final PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(halfW/3f, halfH/2f);
        DungeonWarrior.fixtureDef.shape = pShape;
        b2DComponent.body.createFixture(DungeonWarrior.fixtureDef);
        pShape.dispose();



        enemyEntity.add(b2DComponent);


        this.addEntity(enemyEntity);
    }



    public Vector2 calculateDirection(Vector2 a, Vector2 b) {
        // Obliczanie różnicy wektorów
        float deltaX = b.x - a.x;
        float deltaY = b.y - a.y;

        // Obliczanie długości wektora
        float length = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Obliczanie wektora kierunku
        float directionX = deltaX / length;
        float directionY = deltaY / length;

        return new Vector2(directionX, directionY);
    }

    public Entity createPlayer(final Vector2 playerSpawnLocation){ //final float widht, final float heiight
        final Entity player = this.createEntity();


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

//        swiatlo
        b2DComponent.lightDistance = 2;
        b2DComponent.lightFluctuationDistance = 0;
        b2DComponent.lightFluctuationSpeed = 2;
        b2DComponent.light = new PointLight(rayHandler, 64, new Color(1,1,1,0.3f), b2DComponent.lightDistance, b2DComponent.body.getPosition().x, b2DComponent.body.getPosition().y);
        b2DComponent.lightFluctuationTime= b2DComponent.light.getDirection() * 0.16f;
        b2DComponent.light.attachToBody(b2DComponent.body);


        player.add(b2DComponent);

        final PlayerComponent playerComponent = this.createComponent(PlayerComponent.class);
        playerComponent.b2DComponent = b2DComponent;

        //komponenty
        player.add(playerComponent);


        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = AnimationType.MAGE_IDLE;
        animationComponent.width = 16 * UNIT_SCALE;
        animationComponent.height = 16 * UNIT_SCALE;
        animationComponent.isAnimating = true;
        animationComponent.oneFrameResolution = 48;
        player.add(animationComponent);
        this.addEntity(player);
        return player;
    }


    public void createEnemyFireball(Vector2 startLoc, Vector2 endLoc, Sprite fireball, int accuracy) {
        final Entity gameObjEntity = this.createEntity();

        final GameObjectComponent gameObjectComponent = this.createComponent(GameObjectComponent.class);
        gameObjectComponent.animationIndex = 1;

        gameObjectComponent.type = GameObjectType.FIREBALL;

        gameObjectComponent.sprite = fireball;
        gameObjectComponent.position = Vector2.Zero;
        gameObjEntity.add(gameObjectComponent);

        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.animationType = null;
        animationComponent.width = 0.2f;
        animationComponent.height = 0.2f;
        animationComponent.animationType = AnimationType.FIREBALL_END;

        gameObjEntity.add(animationComponent);

        DungeonWarrior.resetBodiesAndFixtureDefinitions();
        final float halfW = animationComponent.width * 0.5f;
        final float halfH = animationComponent.height * 0.5f;
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);







        DungeonWarrior.bodyDef.position.set(startLoc.x + halfW, startLoc.y+ halfH);
//        DungeonWarrior.bodyDef.position.set(fireballDirection.x + halfW, fireballDirection.y+ halfH);



        DungeonWarrior.bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2DComponent.body = world.createBody(DungeonWarrior.bodyDef);
        bodyDef.fixedRotation = true;
        b2DComponent.body.setUserData(gameObjEntity);
        b2DComponent.widht = animationComponent.width;
        b2DComponent.height = animationComponent.height;



        b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - animationComponent.width * 0.5f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f);






        //b2DComponent.renderPosition.set(position.x, position.y);
        DungeonWarrior.fixtureDef.filter.categoryBits = BIT_ENEMY_FIREBALL;//bitType;
        DungeonWarrior.fixtureDef.filter.maskBits = BIT_GROUND | BIT_PLAYER;
        final PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(halfW, halfH);
        DungeonWarrior.fixtureDef.shape = pShape;
        b2DComponent.body.createFixture(DungeonWarrior.fixtureDef);
        pShape.dispose();


        b2DComponent.lightDistance = 0.2f;
        b2DComponent.lightFluctuationDistance = 0.3f;
        b2DComponent.lightFluctuationSpeed = 4;
        b2DComponent.light = new PointLight(rayHandler, 64, new Color(1,0,1,0.75f), b2DComponent.lightDistance, b2DComponent.body.getPosition().x, b2DComponent.body.getPosition().y);
        b2DComponent.lightFluctuationTime= b2DComponent.light.getDirection() * 0.16f;



        gameObjEntity.add(b2DComponent);

        final EnemyFireball enemyFireball = this.createComponent(EnemyFireball.class);

        //TWORZENIE OBRECZY DLA PRAWIDLOWEGO ODRZUTU
        // Oblicz kierunek od gracza do celu
        Vector2 directionToTarget = endLoc.cpy().sub(startLoc).nor();
        // Przeskaluj kierunek do żądanej odległości
        Vector2 scaledDirection = directionToTarget.scl(5); // 5 to żądana odległość od gracza
        // Oblicz pozycję startową fireballa
        Vector2 fireballDirection = startLoc.cpy().add(scaledDirection);

        if(accuracy == 0){
            int random = MathUtils.random(3);
            if(random == 0){
                fireballDirection = new Vector2(fireballDirection.x + MathUtils.random(recoil), fireballDirection.y + MathUtils.random(recoil));
            }else if(random == 1){
                fireballDirection = new Vector2(fireballDirection.x - MathUtils.random(recoil), fireballDirection.y - MathUtils.random(recoil));
            }else if(random == 3){
                fireballDirection = new Vector2(fireballDirection.x + MathUtils.random(recoil), fireballDirection.y - MathUtils.random(recoil));
            }
        }

        enemyFireball.setDirection(calculateDirection(b2DComponent.body.getPosition(), new Vector2(fireballDirection.x + 0.125f, fireballDirection.y + 0.125f)));
//        fireballComponent.setDirection(calculateDirection(b2DComponent.body.getPosition(), new Vector2(fireballDirection.x + 2f, fireballDirection.y + 2f)));
        gameObjEntity.add(enemyFireball);



        this.addEntity(gameObjEntity);
    }

    public void createDungeonDoor(Vector2 startLoc, int orientation){

        final Entity gameObjEntity = this.createEntity();


        final GameObjectComponent gameObjectComponent = this.createComponent(GameObjectComponent.class);
        gameObjectComponent.animationIndex = 1;

        gameObjectComponent.type = GameObjectType.ENTRANCE;

        gameObjectComponent.position = Vector2.Zero;
        gameObjEntity.add(gameObjectComponent);

        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.isAnimating = true;
        if(orientation == 0 || orientation == 1){
            animationComponent.width = 0.1f;
            animationComponent.height = 0.5f;
        }else{
            animationComponent.width = 0.5f;
            animationComponent.height = 0.1f;
        }

        animationComponent.animationType = AnimationType.FIREBALL_END;

        gameObjEntity.add(animationComponent);

        DungeonWarrior.resetBodiesAndFixtureDefinitions();
        final float halfW = animationComponent.width * 0.5f;
        final float halfH = animationComponent.height * 0.5f;
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);







        DungeonWarrior.bodyDef.position.set(startLoc.x + halfW, startLoc.y+ halfH);
//        DungeonWarrior.bodyDef.position.set(fireballDirection.x + halfW, fireballDirection.y+ halfH);



        DungeonWarrior.bodyDef.type = BodyDef.BodyType.StaticBody;
        b2DComponent.body = world.createBody(DungeonWarrior.bodyDef);
        bodyDef.fixedRotation = true;
        b2DComponent.body.setUserData(gameObjEntity);
        b2DComponent.widht = animationComponent.width;
        b2DComponent.height = animationComponent.height;



        b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - animationComponent.width * 0.5f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f);






        //b2DComponent.renderPosition.set(position.x, position.y);
        DungeonWarrior.fixtureDef.filter.categoryBits = BIT_GROUND;//bitType;
        DungeonWarrior.fixtureDef.filter.maskBits =  BIT_PLAYER;
        final PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(halfW, halfH);
        DungeonWarrior.fixtureDef.shape = pShape;
        b2DComponent.body.createFixture(DungeonWarrior.fixtureDef);
        pShape.dispose();



        gameObjEntity.add(b2DComponent);

        final RoomEntranceComponent roomEntranceComponent = this.createComponent(RoomEntranceComponent.class);
        roomEntranceComponent.roomID = -1;

        gameObjEntity.add(roomEntranceComponent);


        this.addEntity(gameObjEntity);
    }


    public void createDungeonEntrance(Vector2 startLoc, float minX, float minY, float maxX, float maxY, int roomID, int orientation){

        final Entity gameObjEntity = this.createEntity();




        final GameObjectComponent gameObjectComponent = this.createComponent(GameObjectComponent.class);
        gameObjectComponent.animationIndex = 1;

        gameObjectComponent.type = GameObjectType.ENTRANCE;

        gameObjectComponent.position = Vector2.Zero;
        gameObjEntity.add(gameObjectComponent);

        final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
        animationComponent.isAnimating = true;
        animationComponent.width = 0f;
        animationComponent.height = 0f;
        animationComponent.animationType = AnimationType.FIREBALL_END;

        gameObjEntity.add(animationComponent);

        DungeonWarrior.resetBodiesAndFixtureDefinitions();
        final float halfW = 0.5f * 0.5f;
        final float halfH = 0.5f * 0.5f;
        final B2DComponent b2DComponent = this.createComponent(B2DComponent.class);







        DungeonWarrior.bodyDef.position.set(startLoc.x + halfW, startLoc.y+ halfH);
//        DungeonWarrior.bodyDef.position.set(fireballDirection.x + halfW, fireballDirection.y+ halfH);



        DungeonWarrior.bodyDef.type = BodyDef.BodyType.StaticBody;
        b2DComponent.body = world.createBody(DungeonWarrior.bodyDef);
        bodyDef.fixedRotation = true;
        b2DComponent.body.setUserData(gameObjEntity);
        b2DComponent.widht = animationComponent.width;
        b2DComponent.height = animationComponent.height;



        b2DComponent.renderPosition.set(b2DComponent.body.getPosition().x - 0.5f * 0.5f, b2DComponent.body.getPosition().y - b2DComponent.height * 0.5f);






        //b2DComponent.renderPosition.set(position.x, position.y);
        DungeonWarrior.fixtureDef.filter.categoryBits = BIT_ENTRANCE;//bitType;
        DungeonWarrior.fixtureDef.filter.maskBits =  BIT_PLAYER;
        final PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(halfW, halfH);
        DungeonWarrior.fixtureDef.shape = pShape;
        b2DComponent.body.createFixture(DungeonWarrior.fixtureDef);
        pShape.dispose();



        gameObjEntity.add(b2DComponent);

        final RoomEntranceComponent roomEntranceComponent = this.createComponent(RoomEntranceComponent.class);
        roomEntranceComponent.minX = minX;
        roomEntranceComponent.minY = minY;
        roomEntranceComponent.maxX = maxX;
        roomEntranceComponent.maxY = maxY;
        roomEntranceComponent.roomID = roomID;
        if(orientation == 0){ // lewa
            roomEntranceComponent.position = new Vector2(b2DComponent.body.getPosition().x + 0.4f, b2DComponent.body.getPosition().y - 0.25f) ;
            roomEntranceComponent.doorOrientation = 0;
        }else if(orientation == 1){ //prawa
            roomEntranceComponent.position = new Vector2(b2DComponent.body.getPosition().x - 0.5f, b2DComponent.body.getPosition().y - 0.25f) ;
            roomEntranceComponent.doorOrientation = 1;

        }else if(orientation == 2){  //dol
            roomEntranceComponent.position = new Vector2(b2DComponent.body.getPosition().x - 0.25f, b2DComponent.body.getPosition().y + 0.7f) ;
            roomEntranceComponent.doorOrientation = 2;

        }else if(orientation == 3){ //gora
            roomEntranceComponent.position = new Vector2(b2DComponent.body.getPosition().x - 0.25f, b2DComponent.body.getPosition().y - 0.7f) ;
            roomEntranceComponent.doorOrientation = 3;

        }


        gameObjEntity.add(roomEntranceComponent);




        this.addEntity(gameObjEntity);
    }
}
