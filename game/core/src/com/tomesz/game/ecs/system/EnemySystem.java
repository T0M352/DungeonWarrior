package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.WorldContactListener;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.*;
import com.tomesz.game.map.GameObjectType;
import com.tomesz.game.map.PathFinder;
import com.tomesz.game.screen.GameScreen;
import com.tomesz.game.view.AnimationType;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.tomesz.game.DungeonWarrior.BIT_GAME_OBJECT;
import static jdk.vm.ci.code.RegisterAttributes.createMap;

public class EnemySystem extends IteratingSystem {
    private final DungeonWarrior context;
    private final World world;
    private ImmutableArray<Entity> playerEntities;
    private HashSet<Vector2> decoration;

//    private List<Vector2> path;

    private final PathFinder pathFinder;

    private float timer = 0;
    private float changeTimer = 0;
    private float deathTimer = 0;

    private boolean lastRight = true;

    private int moveDirection;

    private boolean isAttacking;

    private Sprite diamond;



    QueryCallback attackPlayerCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if(fixture.getBody().getUserData() instanceof Entity && ECSEngine.playerCmpMapper.get((Entity) fixture.getBody().getUserData()) != null){
                PlayerComponent playerComponent = ECSEngine.playerCmpMapper.get((Entity) fixture.getBody().getUserData());
                playerComponent.addDamage(5);
                playerComponent.markDamage();

            }
            return true;
        }
    };

//    int pathStep;
    public EnemySystem(DungeonWarrior context) {
        super(Family.all(EnemyComponent.class, AnimationComponent.class, B2DComponent.class).exclude(DistanceEnemyComponent.class).get());
        this.context = context;
        pathFinder = new PathFinder();
        world = context.getWorld();
//        path = new ArrayList<Vector2>();
//        pathStep = 1;
    }


    @Override
    protected void processEntity(Entity entity, float v) {
        final EnemyComponent enemyComponent = ECSEngine.enemyObjectMapper.get(entity);
        final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);
        final LegAnimationComponent legAnimationComponent = ECSEngine.legAnimComponentMapper.get(entity);


        HandleSpriteChange(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, v);

        HandleDirectionSetting(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, v);

        HandleAttackState(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, v);

        HandleMovement(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, v);

        HandleDeath(enemyComponent, b2DComponent, animationComponent, legAnimationComponent, v, entity);

        
        if(Gdx.input.isKeyJustPressed(Input.Keys.L)){
            enemyComponent.playerInSight = true;
            enemyComponent.path = createPath(b2DComponent.body.getPosition(), PathFinder.NeighborsMode.WITH_CORRECTION);//PRZY ZAKRECIE DODAJE JESZCZE JEDNO POLE NA PRZOD ZEBY DOBRZE ZAWINĄĆ
        }


    }



    public static Vector2 findRandomPosition(Vector2 playerPosition, double minDistance, HashMap<Vector2, Integer> map) {
        List<Vector2> availablePositions = new ArrayList<>();

        for (Map.Entry<Vector2, Integer> entry : map.entrySet()) {
            Vector2 point = entry.getKey();
            double distance = calculateDistance(playerPosition, point);

            if (distance >= minDistance && entry.getValue() == 0) {
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

    private void HandleDeath(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, float v, Entity entity) {

        if (enemyComponent.health < 0) {
            if(diamond == null){
                diamond = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("diamond");
            }


            b2DComponent.body.setLinearVelocity(0, 0);
            animationComponent.animationType = AnimationType.KOBOLD_DEATH;
            animationComponent.animationTime = enemyComponent.deathTimer;
            enemyComponent.deathTimer += v;
            legAnimationComponent.height = 0;
            legAnimationComponent.width = 0;
            if (enemyComponent.deathTimer >= 0.8f) {
                if(new Random().nextInt(6) == 1){
                    context.getEcsEngine().createSampleObject(b2DComponent.body.getWorldCenter(), diamond, GameObjectType.DIAMOND, BIT_GAME_OBJECT);
                }



                entity.add(((ECSEngine) getEngine()).createComponent(RemoveComponent.class));
                enemyComponent.deathTimer = 0;
            }
        }
    }

    private void HandleAttackState(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, float v) {
        if (enemyComponent.isAttacking) {
            enemyComponent.attackDelay += v;
            animationComponent.animationTime = enemyComponent.attackDelay;
            if (enemyComponent.attackDelay >= 0.8f) {
                enemyComponent.attackDelay = 0;
                if (enemyComponent.moveDirection == 0) {
//                            world.QueryAABB(attackPlayerCallback, b2DComponent.body.getWorldCenter().x - 0.7f, b2DComponent.body.getWorldCenter().y, b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y + 0.3f);
                    world.QueryAABB(attackPlayerCallback, b2DComponent.body.getWorldCenter().x - 0.8f, b2DComponent.body.getWorldCenter().y, b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y + 0.5f);
                } else if (enemyComponent.moveDirection == 1) {
//                            world.QueryAABB(attackPlayerCallback, b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y, b2DComponent.body.getWorldCenter().x + 0.7f, b2DComponent.body.getWorldCenter().y + 0.3f);
                    world.QueryAABB(attackPlayerCallback, b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y, b2DComponent.body.getWorldCenter().x + 0.8f, b2DComponent.body.getWorldCenter().y + 0.5f);
                } else if (enemyComponent.moveDirection == 2) {
//                            world.QueryAABB(attackPlayerCallback, b2DComponent.body.getWorldCenter().x - 0.2f, b2DComponent.body.getWorldCenter().y - 0.45f, b2DComponent.body.getWorldCenter().x + 0.4f, b2DComponent.body.getWorldCenter().y);
                    world.QueryAABB(attackPlayerCallback, b2DComponent.body.getWorldCenter().x - 0.35f, b2DComponent.body.getWorldCenter().y - 0.7f, b2DComponent.body.getWorldCenter().x + 0.5f, b2DComponent.body.getWorldCenter().y);
                } else if (enemyComponent.moveDirection == 3) {
//                            world.QueryAABB(attackPlayerCallback, b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y, b2DComponent.body.getWorldCenter().x + 0.4f, b2DComponent.body.getWorldCenter().y + 0.7f);
                    world.QueryAABB(attackPlayerCallback, b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y, b2DComponent.body.getWorldCenter().x + 0.5f, b2DComponent.body.getWorldCenter().y + 0.8f);
                }
                enemyComponent.isAttacking = false;
            } else {
                if (enemyComponent.moveDirection == 0) {
                    animationComponent.animationType = AnimationType.KOBOLD_ATTACK_LEFT;
                } else if (enemyComponent.moveDirection == 1) {
                    animationComponent.animationType = AnimationType.KOBOLD_ATTACK_RIGHT;
                } else if (enemyComponent.moveDirection == 2) {
                    animationComponent.animationType = AnimationType.KOBOLD_ATTACK_BACK;
                } else if (enemyComponent.moveDirection == 3) {
                    animationComponent.animationType = AnimationType.KOBOLD_ATTACK_FRONT;
                }
            }

        }

    }

    private void HandleDirectionSetting(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, float v) {

        if (enemyComponent.playerInSight) {

            if(playerEntities == null){
                playerEntities = context.getEcsEngine().getEntitiesFor(Family.all(PlayerComponent.class).get());
            }
            Entity playerEntity = playerEntities.get(0);

            enemyComponent.isAttacking = false;
            Point player = new Point((int) (playerEntity.getComponent(B2DComponent.class).body.getPosition().x * 2), (int) (playerEntity.getComponent(B2DComponent.class).body.getPosition().y * 2));
            Point enemy = new Point((int) (b2DComponent.body.getWorldCenter().x), (int) (b2DComponent.body.getWorldCenter().y));
            Point enemyToPlayer = new Point((int) (b2DComponent.body.getWorldCenter().x * 2), (int) (b2DComponent.body.getWorldCenter().y * 2));
            double distanceToPlayer = player.distance(enemyToPlayer);

            if (distanceToPlayer > 1.5f) {
                //LOGIKA GANIANIA


                if (enemyComponent.path == null) {
                    if (distanceToPlayer >= 3) {
                        enemyComponent.path = createPath(b2DComponent.body.getPosition(), PathFinder.NeighborsMode.WITH_CORRECTION);
                    } else {
                        enemyComponent.path = createPath(b2DComponent.body.getPosition(), PathFinder.NeighborsMode.STANDARD);

                    }
                }

                if (enemyComponent.pathStep < enemyComponent.path.size()) {
                    enemyComponent.direction = new Vector2((enemyComponent.path.get(enemyComponent.pathStep).x + 0.5f) / 2, (enemyComponent.path.get(enemyComponent.pathStep).y + 0.5f) / 2);

                    Point direction = new Point((int) enemyComponent.direction.x, (int) enemyComponent.direction.y);
                    double distance = enemy.distance(direction);

                    if ((distance == 0) && enemyComponent.pathStep < enemyComponent.path.size() - 1) {
                        enemyComponent.pathStep++;
                    }

                }

                enemyComponent.pathTimer += v;
                if (enemyComponent.pathTimer >= 1.5f) {
                    enemyComponent.pathTimer = 0;
                    enemyComponent.pathStep = 1;
                    enemyComponent.path = createPath(b2DComponent.body.getPosition(), PathFinder.NeighborsMode.WITH_CORRECTION);
//                    enemyComponent.direction = new Vector2((enemyComponent.path.get(enemyComponent.pathStep).x + 0.5f) / 2, (enemyComponent.path.get(enemyComponent.pathStep).y + 0.5f) / 2);
                    enemyComponent.direction = playerEntity.getComponent(B2DComponent.class).body.getPosition();

//                    context.getMapManager().repaintDung();
//                         for(Vector2 ve:enemyComponent.path){
//                             context.getMapManager().deleteTile(ve);
//                         }
                }
            } else if (distanceToPlayer <= 1.5f) {
                enemyComponent.direction = playerEntity.getComponent(B2DComponent.class).body.getPosition();
                enemyComponent.isAttacking = true;
            }


        }


    }

    private void HandleSpriteChange(EnemyComponent enemyComponent, B2DComponent b2DComponent, AnimationComponent animationComponent, LegAnimationComponent legAnimationComponent, final float v) {
        enemyComponent.changeTimer += v;
        if (!enemyComponent.isAttacking) {
            enemyComponent.attackDelay = 0;

            if (b2DComponent.body.getLinearVelocity().equals(Vector2.Zero)) {
                animationComponent.animationType = enemyComponent.animationRight;
                legAnimationComponent.animationType = enemyComponent.legsIdleRight;


            } else {
                float velocityX = b2DComponent.body.getLinearVelocity().x;
                float velocityY = b2DComponent.body.getLinearVelocity().y;

                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (enemyComponent.changeTimer > 0.15f) {
                        enemyComponent.changeTimer = 0;
                        if (velocityX < 0) {
                            animationComponent.animationType = enemyComponent.animationLeft;
                            legAnimationComponent.animationType = enemyComponent.legsLeft;
                            enemyComponent.moveDirection = 0;
                        } else {
                            animationComponent.animationType = enemyComponent.animationRight;
                            legAnimationComponent.animationType = enemyComponent.legsRight;
                            enemyComponent.moveDirection = 1;
                        }
                    }

                } else {
                    if (enemyComponent.changeTimer > 0.15f) {
                        enemyComponent.changeTimer = 0;
                        if (velocityY > 0) {
                            animationComponent.animationType = enemyComponent.animationBack;
                            legAnimationComponent.animationType = enemyComponent.legsBack;
                            enemyComponent.moveDirection = 2;
                        } else {
                            animationComponent.animationType = enemyComponent.animationFront;
                            legAnimationComponent.animationType = enemyComponent.legsFront;
                            enemyComponent.moveDirection = 3;
                        }
                    }

                }
            }
        }

    }


    public List<Vector2> createPath(Vector2 position, PathFinder.NeighborsMode mode) { //
        if(playerEntities == null){
            playerEntities = context.getEcsEngine().getEntitiesFor(Family.all(PlayerComponent.class).get());
        }

        Entity playerEntity = playerEntities.get(0);
        Vector2 endPos = new Vector2((int) (playerEntity.getComponent(B2DComponent.class).body.getPosition().x * 2), (int) (playerEntity.getComponent(B2DComponent.class).body.getPosition().y * 2));
        Vector2 startPos = new Vector2((int) (position.x * 2), (int) (position.y * 2));

        HashMap<Vector2, Integer> map = createLocalMap();


        List<Vector2> path = pathFinder.findPath(startPos, endPos, map, mode);
        return path;
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




}
