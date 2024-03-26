package com.tomesz.game.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.WorldContactListener;
import com.tomesz.game.screen.ScreenType;

public class DeathUI extends Table{
    final DungeonWarrior context;

    public DeathUI(final DungeonWarrior context) {
        super(context.getSkin());
        this.context = context;
        setFillParent(true);
        Texture backgroundTexture = new Texture(Gdx.files.internal("backImage.png"));
        Image backgroundImage = new Image(backgroundTexture);
        setBackground(backgroundImage.getDrawable());

        TextButton lose  = new TextButton("YOU LOSE", getSkin(), "huge");


        TextButton newGame  = new TextButton("play again", getSkin(), "big");
        newGame.setColor(0, 0, 0, 1f);


        newGame.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                context.setScreen(ScreenType.GAME);
                return true;
            }
        });
        newGame.addListener(new ClickListener(){
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                newGame.getLabel().setColor(1, 0, 0, 1);
                newGame.getLabel().setFontScale(1.3f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                newGame.getLabel().setColor(1, 1, 1, 1);
                newGame.getLabel().setFontScale(1);
            }
        });


        add(lose).fill().center().padBottom(150).row();
        add(newGame).fill().center().row();
    }

}
