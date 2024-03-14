package com.tomesz.game.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.ecs.ECSEngine;
import com.tomesz.game.ecs.components.B2DComponent;
import com.tomesz.game.ecs.components.PlayerComponent;
import com.tomesz.game.input.GameKeys;
import com.tomesz.game.input.InputListener;
import com.tomesz.game.input.InputManager;
import com.tomesz.game.map.GameObjectType;
import com.tomesz.game.screen.GameScreen;
import com.tomesz.game.screen.MenuInGame;
import com.tomesz.game.screen.ScreenType;

import static com.tomesz.game.DungeonWarrior.BIT_GAME_OBJECT;

public class PlayerMovementSystem extends IteratingSystem implements InputListener {
    private boolean directionChange;
    private boolean shoot;
    private int xFactor;
    private int yFactor;
    private  final DungeonWarrior context;
    Sprite fireball;
    public PlayerMovementSystem(final DungeonWarrior context) {
        super(Family.all(PlayerComponent.class, B2DComponent.class).get());
        this.context = context;
        context.getInputManager().addInputListener(this);
        directionChange = false;
        xFactor = yFactor = 0;
    }

    @Override
    protected void processEntity(final Entity entity,final float v) {
        //entity.getComponent(PlayerComponent.class); wolny sposob


        //if(directionChange)
        //{
            final PlayerComponent playerComponent =  ECSEngine.playerCmpMapper.get(entity);
            final B2DComponent b2DComponent = ECSEngine.b2DComponentCmpMapper.get(entity);
        //    directionChange = false;
            b2DComponent.body.applyLinearImpulse(
                    (xFactor * 2 - b2DComponent.body.getLinearVelocity().x) * b2DComponent.body.getMass(),
                    (yFactor * 2 - b2DComponent.body.getLinearVelocity().y) * b2DComponent.body.getMass(),
                    b2DComponent.body.getWorldCenter().x, b2DComponent.body.getWorldCenter().y, true
            );



        //}
        if(shoot){
            if(fireball == null){
                fireball = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class).createSprite("fireball01");

            }
            Vector3 mouseAtScreen = new Vector3(Gdx.input.getX(),  Gdx.input.getY(), 0);
            Vector2 mouseAtWorld = getWorldPositionFromScreen(mouseAtScreen);
            Vector2 dir = new Vector2(mouseAtWorld.x - 0.125f, mouseAtWorld.y - 0.125f);
            Vector2 playerLocation = b2DComponent.body.getPosition();
            context.getEcsEngine().createFireball(playerLocation, dir, fireball);
            shoot = false;
        }
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) { //do poprawienia
            shoot = true;
        }

    }

    private Vector2 getWorldPositionFromScreen(Vector3 v) {
        Vector3 vc = context.getGameCamera().unproject(new Vector3(v.x,   v.y, v.z));
        return new Vector2(vc.x, vc.y);
    }

    @Override
    public void keyPressed(InputManager manager, GameKeys key) {
        switch (key) {
            case LEFT:
                directionChange = true;
                xFactor = -1;
                break;
            case RIGHT:
                directionChange = true;
                xFactor = 1;
                break;
            case UP:
                directionChange = true;
                yFactor = 1;
                break;
            case DOWN:
                directionChange = true;
                yFactor = -1;
                break;
            default:
                return;
        }
    }

    @Override
    public void keyUp(InputManager manager, GameKeys key) {
        switch (key) {
            case LEFT:
                directionChange = true;
                xFactor = manager.isKeyPressed(GameKeys.RIGHT) ? 1 : 0;
                break;
            case RIGHT:
                directionChange = true;
                xFactor = manager.isKeyPressed(GameKeys.LEFT) ? -1 : 0;
                break;
            case UP:
                directionChange = true;
                yFactor = manager.isKeyPressed(GameKeys.DOWN) ? -1 : 0;
                break;
            case DOWN:
                directionChange = true;
                yFactor = manager.isKeyPressed(GameKeys.UP) ? 1 : 0;
                break;
            case BACK:
                if(context.getScreen().getClass() == GameScreen.class){
                    context.setScreen(ScreenType.MENU_IN_GAME);
                }else if(context.getScreen().getClass() == MenuInGame.class){
                    context.setScreen(ScreenType.GAME);
                }
                break;
            default:
                return;
        }
    }


}
