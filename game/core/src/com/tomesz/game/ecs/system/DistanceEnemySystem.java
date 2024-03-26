package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.*;
import com.tomesz.game.map.GameObjectType;
import com.tomesz.game.map.PathFinder;
import com.tomesz.game.view.AnimationType;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.tomesz.game.DungeonWarrior.BIT_GAME_OBJECT;

public class DistanceEnemySystem extends IteratingSystem {
    private ImmutableArray<Entity> playerEntities;
    private final DungeonWarrior context;

    private final PathFinder pathFinder;

    private float timer;

    private Sprite fireball;

    private Sprite diamond;




    public DistanceEnemySystem(DungeonWarrior context) {
        super(Family.all(EnemyComponent.class, AnimationComponent.class, B2DComponent.class, DistanceEnemyComponent.class).get());
        this.context = context;
        pathFinder = new PathFinder();
    }

    @Override
    protected void processEntity(Entity entity, float v) {
        final EnemyComponent enemyComponent = ECSEngine.enemyObjectMapper.get(entity);
        final DistanceEnemyComponent distanceEnemy = ECSEngine.distanceEnemyObjectMapper.get(entity);
        final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);
        final LegAnimationComponent legAnimationComponent = ECSEngine.legAnimComponentMapper.get(entity);

        HandleSpriteChange(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, distanceEnemy,v);

        HandleMovement(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, v);

        HandleDeath(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, v, entity);

        HandleChangingPosition(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, v, entity);

        HandleDirectionSetting(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, v);

        HandleShooting(b2DComponent, distanceEnemy, enemyComponent, v);


    }

    private void HandleShooting(B2DComponent b2DComponent, DistanceEnemyComponent distanceEnemy, EnemyComponent enemyComponent, float v) {

        if(enemyComponent.playerInSight){
            if(fireball == null){
                fireball = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("fireball01");
            }
            if(playerEntities == null){
                playerEntities = context.getEcsEngine().getEntitiesFor(Family.all(PlayerComponent.class).get());
            }


            distanceEnemy.shootSerieDelay+=v;

            if(distanceEnemy.shootSerieDelay > 2 + new Random().nextInt(2)){
                distanceEnemy.shoot=true;

            }

            if(distanceEnemy.shoot){
                distanceEnemy.shootTimer += v;
                if(distanceEnemy.shootTimer >= 0.2){
                    context.getEcsEngine().createEnemyFireball(b2DComponent.body.getWorldCenter(), playerEntities.get(0).getComponent(B2DComponent.class).body.getWorldCenter(), fireball, 0);
                    distanceEnemy.shootTimer = 0;
                    distanceEnemy.shootInRows++;
                }
                if(distanceEnemy.shootInRows >= 7){
                    distanceEnemy.shootSerieDelay = 0;
                    distanceEnemy.shootInRows = 0;
                    distanceEnemy.shoot = false;
                }
            }
        }


    }

    private void HandleDirectionSetting(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, float v) {
        enemyComponent.pathTimer += v;
        if(enemyComponent.pathTimer > 5){
            enemyComponent.pathTimer = new Random().nextInt(2);
            Vector2 positionToChange = SetRandomPositionOnRoom(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, 7f, 2, 6, v);
            if(positionToChange != null){
                enemyComponent.posToChange = positionToChange;
                enemyComponent.pathStep = 1;
                enemyComponent.changingPosition = true;
//                enemyComponent.pathTimer = 5;
                enemyComponent.path = null;
            }
        }

    }

    private void HandleChangingPosition(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, float v, Entity entity) {


        if(enemyComponent.changingPosition){
            Point enemy = new Point((int) (b2DComponent.body.getWorldCenter().x), (int) (b2DComponent.body.getWorldCenter().y));
            Point enemyToDestination = new Point((int) (b2DComponent.body.getWorldCenter().x * 2), (int) (b2DComponent.body.getWorldCenter().y * 2));
            Point destination = new Point((int) (enemyComponent.posToChange.x), (int) (enemyComponent.posToChange.y));
            if(enemyComponent.path == null){
                enemyComponent.path = createPath(b2DComponent.body.getPosition(), enemyComponent.posToChange, PathFinder.NeighborsMode.WITH_CORRECTION);


//                context.getMapManager().repaintDung();
//                for(Vector2 ve:enemyComponent.path){
//                    context.getMapManager().deleteTile(ve);
//                }
            }

            if (enemyComponent.pathStep < enemyComponent.path.size()) {
                enemyComponent.direction = new Vector2((enemyComponent.path.get(enemyComponent.pathStep).x + 0.5f) / 2, (enemyComponent.path.get(enemyComponent.pathStep).y + 0.5f) / 2);

                Point direction = new Point((int) enemyComponent.direction.x, (int) enemyComponent.direction.y);
                double distance = enemy.distance(direction);

                if ((distance <= 0.1d) && enemyComponent.pathStep < enemyComponent.path.size() - 1) {
                    enemyComponent.pathStep++;
                }

            }
//                enemyComponent.pathTimer = 0;
//                enemyComponent.pathStep = 1;
//                enemyComponent.path = createPath(b2DComponent.body.getPosition(), enemyComponent.posToChange, PathFinder.NeighborsMode.WITH_CORRECTION);
////                enemyComponent.path = createPath(b2DComponent.body.getPosition(), enemyComponent.posToChange, PathFinder.NeighborsMode.STANDARD);
//                context.getMapManager().repaintDung();
//                for(Vector2 ve:enemyComponent.path){
//                    context.getMapManager().deleteTile(ve);
//                }
//
//            }

            if(enemyToDestination.distance(destination) < 0.1f){
                enemyComponent.changingPosition = false;
            }

        }else{
            enemyComponent.direction = null;
            enemyComponent.posToChange = null;
            b2DComponent.body.setLinearVelocity(Vector2.Zero);
        }



    }

    public List<Vector2> createPath(Vector2 position, Vector2 endPosition, PathFinder.NeighborsMode mode) { //

        Vector2 startPos = new Vector2((int) (position.x * 2), (int) (position.y * 2));

        HashMap<Vector2, Integer> map = createLocalMap();

        List<Vector2> path = pathFinder.findPath(startPos, endPosition, map, mode);
        return path;
    }


    private Vector2 SetRandomPositionOnRoom(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, double minDistance, double minDistanceToEnemy, double maxDistanceToEnemy , float v) {
        if(playerEntities == null){
            playerEntities = context.getEcsEngine().getEntitiesFor(Family.all(PlayerComponent.class).get());
        }

        Entity playerEntity = playerEntities.get(0);

        Vector2 playerPos = new Vector2((int) (playerEntity.getComponent(B2DComponent.class).body.getPosition().x * 2), (int) (playerEntity.getComponent(B2DComponent.class).body.getPosition().y * 2));
        HashMap<Vector2, Integer> map = createLocalMap();

        return findRandomPosition(playerPos,new Vector2(b2DComponent.body.getWorldCenter().x*2, b2DComponent.body.getWorldCenter().y*2), minDistance, minDistanceToEnemy, maxDistanceToEnemy, map);

//        enemyComponent.path = pathFinder.findPath(b2DComponent.body.getWorldCenter(), position, map, PathFinder.NeighborsMode.WITH_CORRECTION);




    }

    public static Vector2 findRandomPosition(Vector2 playerPosition, Vector2 enemyPosition, double minDistance, double minDistanceToEnemy,double maxDistanceToEnemy, HashMap<Vector2, Integer> map) {
        List<Vector2> availablePositions = new ArrayList<>();

        for (Map.Entry<Vector2, Integer> entry : map.entrySet()) {
            Vector2 point = entry.getKey();
            double distance = calculateDistance(playerPosition, point);
            double distanceToEnemy = calculateDistance(enemyPosition, point);

            if (distance >= minDistance && entry.getValue() == 0 && distanceToEnemy > minDistanceToEnemy && distanceToEnemy < maxDistanceToEnemy) {
                    availablePositions.add(point);
            }


        }

        if (availablePositions.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(availablePositions.size());

        return availablePositions.get(randomIndex);
    }

    private static double calculateDistance(Vector2 position1, Vector2 position2) {
        Point pos1 = new Point((int)position1.x, (int)position1.y);
        Point pos2 = new Point((int)position2.x, (int)position2.y);
        return pos1.distance(pos2);
    }

    private HashMap<Vector2, Integer> createLocalMap() {
        Array<Body> bodies = new Array<Body>();
        HashMap<Vector2, Integer> map = new HashMap<Vector2, Integer>();

//        world.QueryAABB(); SCZYTUJE CIALA Z ZADANEGO OBSZARU TO DLA OPTYMALIZACJI TODO
        context.getWorld().getBodies(bodies);
        for (Point pos : context.getMapManager().getRooms()) {
            map.put(new Vector2(pos.x, pos.y), 0);
        }
        for (Body body : bodies) {
            if (body.getUserData() instanceof Entity) {
                Entity gameObject = (Entity) body.getUserData();
                if (gameObject.getComponent(GameObjectComponent.class) != null) {
                    GameObjectComponent gameObjectComponent = ECSEngine.gameObjectMapper.get(gameObject);
                    if (gameObjectComponent.type != GameObjectType.LAMP) {
                        if (gameObjectComponent.type == GameObjectType.TABLE) {
                            map.replace(new Vector2((int) gameObjectComponent.position.x, (int) gameObjectComponent.position.y), 0, 1);
                            map.replace(new Vector2((int) gameObjectComponent.position.x + 1, (int) gameObjectComponent.position.y), 0, 1);
                        } else if (gameObjectComponent.type == GameObjectType.TABLE_UP) {

                            map.replace(new Vector2((int) gameObjectComponent.position.x, (int) gameObjectComponent.position.y), 0, 1);
                            map.replace(new Vector2((int) gameObjectComponent.position.x, (int) gameObjectComponent.position.y + 1), 0, 1);
                        } else {
                            map.replace(new Vector2((int) gameObjectComponent.position.x, (int) gameObjectComponent.position.y), 0, 1);

                        }
                    }
                }
            }
        }
        return map;
    }

    private void HandleMovement(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, float v) {
        if (enemyComponent.direction != null) {
            Vector2 direction = calculateDirection(b2DComponent.body.getPosition(), enemyComponent.direction);

            float xFactor = direction.x;
            float yFactor = direction.y;
            b2DComponent.body.applyLinearImpulse(
                    (xFactor * 1 - b2DComponent.body.getLinearVelocity().x) * b2DComponent.body.getMass(),
                    (yFactor * 1 - b2DComponent.body.getLinearVelocity().y) * b2DComponent.body.getMass(),
                    b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y, true);
        }



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

    private void HandleDeath(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, float v, Entity entity) {
        if (enemyComponent.health < 0) {
            if(diamond == null){
                diamond = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("diamond");
            }

            b2DComponent.body.setLinearVelocity(0, 0);
            animationComponent.animationTime = enemyComponent.deathTimer;
            animationComponent.animationType = AnimationType.GOBLIN_SHAMAN_DEATH;
            enemyComponent.deathTimer += v;
            legAnimationComponent.height = 0;
            legAnimationComponent.width = 0;
            if (enemyComponent.deathTimer >= 0.8f) {
                if(new Random().nextInt(8) == 1){
                    context.getEcsEngine().createSampleObject(b2DComponent.body.getWorldCenter(), diamond, GameObjectType.DIAMOND, BIT_GAME_OBJECT);
                }

                entity.add(((ECSEngine) getEngine()).createComponent(RemoveComponent.class));
                enemyComponent.deathTimer = 0;
            }
        }
    }

    private void HandleSpriteChange(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, DistanceEnemyComponent distanceEnemyComponent, final float v) {
        enemyComponent.changeTimer += v;
            enemyComponent.attackDelay = 0;
            if(b2DComponent.body.getLinearVelocity().equals(Vector2.Zero)){
                if(distanceEnemyComponent.shoot){
                    animationComponent.animationType = AnimationType.GOBLIN_SHAMAN_ATTACK_LEFT; //TODO HARDCODE
                }else{
                    animationComponent.animationType = enemyComponent.animationRight;
                }
                legAnimationComponent.animationType = enemyComponent.legsIdleRight;
            }else if(b2DComponent.body.getLinearVelocity().x>0){
                if(distanceEnemyComponent.shoot){
                    animationComponent.animationType = AnimationType.GOBLIN_SHAMAN_ATTACK_LEFT; //TODO HARDCODE
                }else{
                    animationComponent.animationType = enemyComponent.animationRight;
                }
                legAnimationComponent.animationType = enemyComponent.legsRight;
            }else{
                if(distanceEnemyComponent.shoot){
                    animationComponent.animationType = AnimationType.GOBLIN_SHAMAN_ATTACK_RIGHT; //TODO HARDCODE
                }else{
                    animationComponent.animationType = enemyComponent.animationLeft;
                }
                legAnimationComponent.animationType = enemyComponent.legsLeft;
            }



    }


}
