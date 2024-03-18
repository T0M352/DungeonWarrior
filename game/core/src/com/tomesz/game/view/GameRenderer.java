package com.tomesz.game.view;

import box2dLight.RayHandler;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.AnimationComponent;
import com.tomesz.game.ecs.components.B2DComponent;
import com.tomesz.game.ecs.components.GameObjectComponent;
import com.tomesz.game.map.Map;
import com.tomesz.game.map.MapListener;

import java.util.EnumMap;

import static com.tomesz.game.DungeonWarrior.UNIT_SCALE;

public class GameRenderer implements Disposable, MapListener {
    private final AssetManager assetManager;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final OrthographicCamera gameCamera;

    private final SpriteBatch spriteBatch;

    private final EnumMap<AnimationType, Animation<Sprite>> animationCache;
    private final ImmutableArray<Entity> animatedEntities;

    private final FitViewport  fitViewport;

    private final GLProfiler glProfiler;
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final World world;

    public static TiledMap tiledMap;
    private final Array<TiledMapTileLayer> tiledMapTileLayers;
    public Sprite testSprite;

    private ImmutableArray<Entity> gameObjectEntities;
    private IntMap<Animation<Sprite>> mapAnimation;

    private final DungeonWarrior context;

    private ObjectMap<String, TextureRegion[][]> regionCache;

    private final RayHandler rayHandler;

    public GameRenderer(final DungeonWarrior context) {
        this.context = context;



        assetManager = context.getAssetManager();
        fitViewport = context.getScreenViewport();
        gameCamera = context.getGameCamera();
        spriteBatch = context.getSpriteBatch();
        animationCache = new EnumMap<AnimationType, Animation<Sprite>>(AnimationType.class);
        regionCache = new ObjectMap<String, TextureRegion[][]>();

        gameObjectEntities = context.getEcsEngine().getEntitiesFor(Family.all(AnimationComponent.class, B2DComponent.class, GameObjectComponent.class).get());

        animatedEntities = context.getEcsEngine().getEntitiesFor(Family.all(AnimationComponent.class, B2DComponent.class).exclude(GameObjectComponent.class).get()); //tablica ktora wylapuje kazde entity majace komonent animacji i box2d



        mapRenderer = new OrthogonalTiledMapRenderer(null, UNIT_SCALE, spriteBatch);
        context.getMapManager().addListener(this);
        tiledMapTileLayers = new Array<TiledMapTileLayer>();




        glProfiler = new GLProfiler(Gdx.graphics);
        glProfiler.disable(); //RYSOWANIE GRAFIKI
        if(glProfiler.isEnabled()){
            box2DDebugRenderer = new Box2DDebugRenderer();
            world = context.getWorld();
        }else{
            box2DDebugRenderer = null;
            world = null;
        }
        rayHandler = context.getRayHandler();
    }

    @Override
    public void dispose() {
        if(box2DDebugRenderer != null){
            box2DDebugRenderer.dispose();
        }
        mapRenderer.dispose();
    }

    public void render(final float alpha){
        ScreenUtils.clear(0, 0, 0, 1);
        fitViewport.apply(false);

        mapRenderer.setView(gameCamera);
        spriteBatch.begin();

        if(mapRenderer.getMap() != null){
            AnimatedTiledMapTile.updateAnimationBaseTime(); // wynika to z dzialania metody

            for(final TiledMapTileLayer layer : tiledMapTileLayers){
                mapRenderer.renderTileLayer(layer);
            }
        }



            for(final Entity entity : gameObjectEntities){
                renderGameObject(entity, alpha);
            }



        for(final Entity entity : animatedEntities){
            renderEntity(entity, alpha);
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.K)){
            context.getMapManager().resetMap();
        }
        spriteBatch.end();

        rayHandler.setCombinedMatrix(gameCamera);
        rayHandler.updateAndRender();

        if(glProfiler.isEnabled()){
            glProfiler.reset();
            box2DDebugRenderer.render(world, gameCamera.combined);
        }

    }



    private void renderGameObject(Entity entity, float alpha) {
        final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);
        final GameObjectComponent gameObjectComponent = ECSEngine.gameObjectMapper.get(entity);
        //if(b2DComponent != null && animationComponent != null && gameObjectComponent != null){
        if(!animationComponent.isAnimating){
            final Sprite frame = new Sprite();//animation.getKeyFrame(animationComponent.animationTime);
            frame.set(gameObjectComponent.sprite);
            frame.setBounds(b2DComponent.renderPosition.x, b2DComponent.renderPosition.y, animationComponent.width, animationComponent.height); //JEZELI TRZEBA PRZESUNAC SPRITE TO TUTAJ
            frame.setOriginCenter();
            frame.setRotation(b2DComponent.body.getAngle() * MathUtils.radDeg);
            frame.draw(spriteBatch);
        }else{
            final Animation<Sprite> animation = getAnimationOfGO(animationComponent.animationType);
            final Sprite frame = animation.getKeyFrame(animationComponent.animationTime);
            frame.setBounds(b2DComponent.renderPosition.x, b2DComponent.renderPosition.y, animationComponent.width, animationComponent.height);
            frame.setOriginCenter();
            frame.setRotation(b2DComponent.body.getAngle() * MathUtils.radDeg);
            frame.draw(spriteBatch);
        }

    }

    private void renderEntity(final Entity entity,final float alpha) {
        final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(entity);
        final AnimationComponent animationComponent = ECSEngine.animationComponentMapper.get(entity);

        if(animationComponent.animationType != null){
           final Animation<Sprite> animation= getAnimation(animationComponent.animationType);
           final Sprite frame = animation.getKeyFrame(animationComponent.animationTime);
           frame.setBounds(b2DComponent.renderPosition.x-0.2f, b2DComponent.renderPosition.y-0.2f, animationComponent.width, animationComponent.height); //JEZELI TRZEBA PRZESUNAC SPRITE TO TUTAJ
           frame.draw(spriteBatch);
        }
        b2DComponent.renderPosition.lerp(b2DComponent.body.getPosition(), alpha);
    }

    private Animation<Sprite> getAnimation(AnimationType animationType) {
        Animation<Sprite> animation = animationCache.get(animationType);
        if(animation == null){
            Gdx.app.debug("ANIMATION", "TWORZYMY ANIMACJE O TYPIE " + animationType);
            TextureRegion[][] textureRegions = regionCache.get(animationType.getAtlasKey());
            if(textureRegions == null){
                Gdx.app.debug("ANIMATION", "TWORZYMY REGION O TYPIE " + animationType.getAtlasKey());
                final TextureAtlas.AtlasRegion region = assetManager.get(animationType.getAtlasPath(), TextureAtlas.class).findRegion(animationType.getAtlasKey());
                textureRegions = region.split(48, 48);
                regionCache.put(animationType.getAtlasKey(), textureRegions);
            }
            animation = new Animation<Sprite>(animationType.getFrameTime(), getKeyFrames(textureRegions[animationType.getRowIndex()]));
            animation.setPlayMode(Animation.PlayMode.LOOP);
            animationCache.put(animationType, animation);
        }
        return animation;
    }

    private Animation<Sprite> getAnimationOfGO(AnimationType animationType) {
        Animation<Sprite> animation = animationCache.get(animationType);
        if(animation == null){
            Gdx.app.debug("ANIMATION", "TWORZYMY ANIMACJE O TYPIE " + animationType);
            TextureRegion[][] textureRegions = regionCache.get(animationType.getAtlasKey());
            if(textureRegions == null){
                Gdx.app.debug("ANIMATION", "TWORZYMY REGION O TYPIE " + animationType.getAtlasKey());
                final TextureAtlas.AtlasRegion region = assetManager.get(animationType.getAtlasPath(), TextureAtlas.class).findRegion(animationType.getAtlasKey());
                if(animationType == AnimationType.TABLEUP_END){
                    textureRegions = region.split(16, 32);
                }else if(animationType == AnimationType.TABLE_END){
                    textureRegions = region.split(32, 16);
                }else{
                    textureRegions = region.split(16, 16);
                }

                regionCache.put(animationType.getAtlasKey(), textureRegions);
            }
            animation = new Animation<Sprite>(animationType.getFrameTime(), getKeyFrames(textureRegions[animationType.getRowIndex()]));
            animation.setPlayMode(Animation.PlayMode.LOOP);
            animationCache.put(animationType, animation);
        }
        return animation;
    }

    private Sprite[] getKeyFrames(TextureRegion[] textureRegion) {
        final Sprite[]  keyFrames = new Sprite[textureRegion.length];
        int i = 0;
        for(final TextureRegion region : textureRegion){
            final Sprite sprite = new Sprite(region);
            sprite.setOriginCenter();
            keyFrames[i++]  = sprite;
        }
        return keyFrames;
    }

    @Override
    public void mapChange(Map map) {
        mapRenderer.setMap(map.getTiledMap());
        map.getTiledMap().getLayers().getByType(TiledMapTileLayer.class, tiledMapTileLayers);
        mapAnimation = map.getMapAnimation();
//        if(testSprite==null){
//            Gdx.app.debug("asd", "dziala");
//            testSprite = assetManager.get("mage/mage.atlas", TextureAtlas.class).createSprite("mag_idle01");
//            testSprite.setOriginCenter(); // TODO jezeli chcesz zrobic bron to pokombinuj z punktem rotacji dla obracania sie broni wokol postacji
//        }
    }
}
