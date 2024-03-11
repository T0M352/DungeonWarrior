package com.tomesz.game;

import box2dLight.Light;
import box2dLight.RayHandler;
import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.input.InputManager;
import com.tomesz.game.map.MapManager;
import com.tomesz.game.screen.AbstractScreen;
import com.tomesz.game.screen.ScreenType;
import com.tomesz.game.view.GameRenderer;

import java.util.EnumMap;

public class DungeonWarrior extends Game {
	public static final String TAG = DungeonWarrior.class.getSimpleName();

	private EnumMap<ScreenType, AbstractScreen> screenCache;
	private OrthographicCamera gameCamera;
	private FitViewport screenViewport;
	public static final BodyDef bodyDef = new BodyDef();
	public static final FixtureDef fixtureDef = new FixtureDef();

	private SpriteBatch spriteBatch;
	public static final float UNIT_SCALE = 1/32f;
	public static final short BIT_PLAYER = 1<<0;
	public static final short BIT_GROUND = 1<<1; // 2 bity w lewo w slowie binarnym
	public static final short BIT_GAME_OBJECT = 1<<2;
	public static final short BIT_LIGHT_OBJECT = 1<<3;
	public static final short BIT_TABLE = 1<<4;
	private World world;

	private float acumulator;
	private static final float FIXED_TIME_STEP = 1/60f;
	private Box2DDebugRenderer box2DDebugRenderer;
	private WorldContactListener worldContactListener;

	private AssetManager assetManager;
	private Stage stage;
	private Skin skin;
	private InputManager inputManager;
	private ECSEngine ecsEngine;
	private MapManager mapManager;

	private GameRenderer gameRenderer;

	private IntMap<Animation<Sprite>> mapAnimation;

	private RayHandler rayHandler;

	public MapManager getMapManager() {
		return mapManager;
	}
	public static boolean newLevel = false;

	@Override
	public void create() {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);



		acumulator = 0;
		spriteBatch = new SpriteBatch();
		Box2D.init();
		world = new World(new Vector2(0, 0), true);
		box2DDebugRenderer = new Box2DDebugRenderer();
		worldContactListener = new WorldContactListener();
		world.setContactListener(worldContactListener);
		rayHandler = new RayHandler(world);
		rayHandler.setAmbientLight(0, 0, 0, 0.4f);
		Light.setGlobalContactFilter(BIT_PLAYER, (short) 1, BIT_GROUND); //SWIATLO GRACZA DAJE CIEN TYLKO PRZY GROUND
		Light.setGlobalContactFilter(BIT_PLAYER, (short) 1, BIT_GAME_OBJECT);

		Light.setGlobalContactFilter(BIT_LIGHT_OBJECT, (short) 1, BIT_GROUND);
		Light.setGlobalContactFilter(BIT_LIGHT_OBJECT, (short) 1, BIT_GAME_OBJECT);
		Light.setGlobalContactFilter(BIT_LIGHT_OBJECT, (short) 1, BIT_PLAYER);



		assetManager = new AssetManager();
		assetManager.setLoader(TiledMap.class, new TmxMapLoader(assetManager.getFileHandleResolver()));
		intializeSkin();
		stage = new Stage(new FitViewport(1280, 720), spriteBatch);



		//input
		inputManager = new InputManager();
		Gdx.input.setInputProcessor(new InputMultiplexer(inputManager, stage));



		gameCamera = new OrthographicCamera();
		screenViewport = new FitViewport(16, 9, gameCamera);





		ecsEngine = new ECSEngine(this);
		mapManager = new MapManager(this);

		gameRenderer = new GameRenderer(this);


		screenCache = new EnumMap<ScreenType, AbstractScreen>(ScreenType.class);
		setScreen(ScreenType.LOADING);


	}

	public static void resetBodiesAndFixtureDefinitions(){
		bodyDef.position.set(0, 0);
		bodyDef.gravityScale = 1;
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.fixedRotation = false;

		fixtureDef.density = 0;
		fixtureDef.isSensor = false;
		fixtureDef.restitution = 0;
		fixtureDef.friction = 0.2f;
		fixtureDef.filter.categoryBits = 0x0001;
		fixtureDef.filter.maskBits = -1;
		fixtureDef.shape = null;
	}



	public ECSEngine getEcsEngine() {
		return ecsEngine;
	}

	public InputManager getInputManager() {
		return inputManager;
	}

	public void setInputManager(InputManager inputManager) {
		this.inputManager = inputManager;
	}

	public Stage getStage() {
		return stage;
	}

	public Skin getSkin() {
		return skin;
	}

	private void intializeSkin() {
		//markup colors
		Colors.put("Red", Color.RED);
		Colors.put("Blue", Color.BLUE);
		Colors.put("Green", Color.GREEN);

		//generacja bitmap ttf
		final ObjectMap<String, Object> resources = new ObjectMap<String, Object>();
		final FreeTypeFontGenerator freeTypeFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("UI/font.ttf"));
		final FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		fontParameter.minFilter = Texture.TextureFilter.Linear;
		fontParameter.magFilter = Texture.TextureFilter.Linear;
		final int[] sizesToCreate = {16, 20, 26, 32};
		for(int size:sizesToCreate){
			fontParameter.size = size;
			final BitmapFont bitmapFont = freeTypeFontGenerator.generateFont(fontParameter);
			bitmapFont.getData().markupEnabled = true;
;			resources.put(("font_" + size), bitmapFont);
		}
		freeTypeFontGenerator.dispose();

		//zaladuj skina
		final SkinLoader.SkinParameter skinParameter = new SkinLoader.SkinParameter("UI/hud.atlas", resources);
		assetManager.load("UI/hud.json", Skin.class, skinParameter);
		assetManager.finishLoading();
		skin = assetManager.get("UI/hud.json", Skin.class);


	}

	public WorldContactListener getWorldContactListener() {
		return worldContactListener;
	}

	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}

	public OrthographicCamera getGameCamera() {
		return gameCamera;
	}

	public World getWorld(){
		return world;
	}
	public FitViewport getScreenViewport(){
		return screenViewport;
	}
	public void setScreen(final ScreenType screenType){
		final Screen screen = screenCache.get(screenType);
		if(screen==null){
            try {
				Gdx.app.debug(TAG, "Tworze nowy ekran: " + screenType);
                final AbstractScreen newScreen =(AbstractScreen) ClassReflection.getConstructor(screenType.getScreenClass(), DungeonWarrior.class).newInstance(this);
				screenCache.put(screenType, newScreen);
				setScreen(newScreen);
            } catch (ReflectionException e) {
                throw new GdxRuntimeException("Ekran: " + screenType + " nie mogl zostac stworzony, " + e);
            }
        }else{
			Gdx.app.debug(TAG, "Zamieniam na ekran: " + screenType);
			setScreen(screen);
		}
	}

	public Box2DDebugRenderer getBox2DDebugRenderer() {
		return box2DDebugRenderer;
	}

	@Override
	public void dispose() {
		super.dispose();
		world.dispose();
		gameRenderer.dispose();
		rayHandler.dispose();
		box2DDebugRenderer.dispose();
		assetManager.dispose();
		spriteBatch.dispose();
		stage.dispose();

	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	@Override
	public void render() {
		super.render();
		final float deltaTime = Math.min(0.25f, Gdx.graphics.getDeltaTime());
		ecsEngine.update(deltaTime);

		//ustawianie sztywnego czasu renderowania dla plynnosci liczenia fizyki
		acumulator += deltaTime;
		while(acumulator >= FIXED_TIME_STEP){
			world.step(FIXED_TIME_STEP, 6, 2);
			acumulator -= FIXED_TIME_STEP;
		}

		if(newLevel){
			newLevel = false;
			mapManager.resetMap();
		}

		gameRenderer.render(acumulator/FIXED_TIME_STEP);
		//final float alpha = acumulator / FIXED_TIME_STEP;
		stage.getViewport().apply();
		stage.act(deltaTime);
		stage.draw();
	}

	public RayHandler getRayHandler() {
		return rayHandler;
	}
}
