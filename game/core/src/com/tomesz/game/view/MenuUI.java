package com.tomesz.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.input.GameKeys;
import com.tomesz.game.map.GameObjectType;
import com.tomesz.game.screen.GameScreen;
import com.tomesz.game.screen.ScreenType;

public class MenuUI extends Table {
    DungeonWarrior context;
    private boolean isFullScreen;


    public MenuUI(DungeonWarrior context, ScreenType type) {
        super(context.getSkin());
        this.context = context;
        setFillParent(true);

        Texture backgroundTexture = new Texture(Gdx.files.internal("backImage.png"));
        Image backgroundImage = new Image(backgroundTexture);
        setBackground(backgroundImage.getDrawable());



        TextButton newGame  = new TextButton("New game", getSkin(), "huge");
        newGame.setColor(0, 0, 0, 1f);


        newGame.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                context.setScreen(ScreenType.LOADING);
                return true;
            }
        });
        newGame.addListener(new ClickListener(){
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                newGame.getLabel().setColor(1, 0, 0, 1);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                newGame.getLabel().setColor(1, 1, 1, 1);
            }
        });




        TextButton continueBtn = new TextButton("Continue", getSkin(), "huge");
        continueBtn.setColor(0, 0, 0, 1f);
        continueBtn.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(type == ScreenType.MENU_IN_GAME){
                    context.setScreen(ScreenType.GAME);
                }
                return true;
            }
        });

        continueBtn.addListener(new ClickListener(){
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                continueBtn.getLabel().setColor(1, 0, 0, 1);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                continueBtn.getLabel().setColor(1, 1, 1, 1);
            }
        });

        TextButton fullScreen  = new TextButton("Fullscreen: OFF", getSkin(), "huge");
        fullScreen.setColor(0, 0, 0, 1f);
        fullScreen.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(!isFullScreen){
                    fullScreen.setText("Fullscreen: ON");
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    isFullScreen = true;
                }else{
                    fullScreen.setText("Fullscreen: OFF");
                    Gdx.graphics.setWindowedMode(1280, 720);
                    isFullScreen = false;
                }


                return true;
            }
        });
        fullScreen.addListener(new ClickListener(){
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                fullScreen.getLabel().setColor(1, 0, 0, 1);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                fullScreen.getLabel().setColor(1, 1, 1, 1);
            }
        });




        TextButton exit  = new TextButton("Exit", getSkin(), "huge");
        exit.setColor(0, 0, 0, 1f);
        exit.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.exit();
                return true;
            }
        });
        exit.addListener(new ClickListener(){
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                exit.getLabel().setColor(1, 0, 0, 1);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                exit.getLabel().setColor(1, 1, 1, 1);
            }
        });

        TextButton TM  = new TextButton("Tomasz Morgas", getSkin(), "small");

        add(newGame)
                .fill()
                .center()
                .padBottom(44)
                .padTop(250)
                .row();
        add(continueBtn)
                .fill()
                .center()
                .padBottom(44)
                .row();
        add(fullScreen)
                .fill()
                .center()
                .padBottom(44)
                .row();
        add(exit)
                .fill()
                .center()
                .padBottom(44)
                .row();
        add(TM)
                .expandX()
                .right()
                .expandY()
                .bottom();




    }
}
