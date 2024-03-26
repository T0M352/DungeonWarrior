package com.tomesz.game.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.WorldContactListener;
import com.tomesz.game.ecs.components.PlayerComponent;
import com.tomesz.game.screen.GameScreen;
import com.tomesz.game.screen.ScreenType;

public class GameUI extends Table implements WorldContactListener.PlayerCollisionListener {
    private TextButton diamondsButton;
    private TextButton manaButton;
    private TextButton debugInfo;

    private final ProgressBar progressBar;
    private final ProgressBar hpProgressBar;

    private final DungeonWarrior context;

    public GameUI(final DungeonWarrior context) {
        super(context.getSkin());
        this.context = context;
        TextureAtlas atlas = context.getAssetManager().get("mage/mage.atlas", TextureAtlas.class);

        debugInfo = new TextButton("", getSkin(), "small");
        debugInfo.setPosition(280, Gdx.graphics.getHeight() - debugInfo.getHeight() - 45); // Ustawienie pozycji przycisku w lewym górnym rogu
        debugInfo.getLabel().setAlignment(Align.left);


        Image manaImage = new Image(atlas.findRegion("fireball01"));
        Image diamondImage = new Image(atlas.findRegion("diamond"));
        Image hpImage = new Image(atlas.findRegion("hearth"));

        progressBar = new ProgressBar(0, 1, 0.01f, false, getSkin(), "default");
        hpProgressBar = new ProgressBar(0, 1, 0.01f, false, getSkin(), "default");
        hpProgressBar.setValue(1);

        context.getWorldContactListener().addPlayerCollisionListener(this);
        setFillParent(true);
        diamondsButton  = new TextButton(" 0", getSkin(), "huge");
        manaButton  = new TextButton("Mana: 100", getSkin(), "huge");

        addActor(debugInfo);

        Table leftColumn = new Table();
        leftColumn.add(hpImage).size(hpImage.getWidth() * 4, hpImage.getHeight() * 4).padLeft(15);
        leftColumn.add(hpProgressBar).left(); // Pasek postępu many nad tekstem many
        leftColumn.row(); // Przejdź do kolejnego wiersza
        leftColumn.add(manaImage).size(manaImage.getWidth() * 4, manaImage.getHeight() * 4).padLeft(15).padBottom(20);
        leftColumn.add(progressBar).left(); // Pasek postępu many nad tekstem many
        add(leftColumn).expandX().left();

        // Druga kolumna na prawej stronie
        Table rightColumn = new Table();
        rightColumn.add(diamondImage).size(manaImage.getWidth() * 3, manaImage.getHeight() * 3).padLeft(15).padBottom(15);
        rightColumn.add(diamondsButton).padRight(20).padBottom(10);
        add(rightColumn).expandX().right().bottom();




        bottom();



    }

    @Override
    public void PlayerCollision(Entity player, Entity gameObject) {
        int diamondsCount = player.getComponent(PlayerComponent.class).getDiamonds();
        diamondsButton.setText(" " + diamondsCount);
    }

    public void refreshDiamonds(Entity player){
        int diamondsCount = player.getComponent(PlayerComponent.class).getDiamonds();
        diamondsButton.setText(" " + diamondsCount);
    }

    public void refreshHealth(Entity player){
        float health = player.getComponent(PlayerComponent.class).health;
        float progress = health / 100f;
        hpProgressBar.setValue(progress);
    }


    public void refreshMana(int mana){
        manaButton.setText("Mana: " + mana);
        float progress = mana / 100f;
        progressBar.setValue(progress);
    }

    public void refreshHealth(int health) {
        float progress = health / 100f;
        hpProgressBar.setValue(progress);


    }

    public void showDebugInfo(){
        debugInfo.setText("k - resetuj mape\nz - zapis\nx - wczytanie\nc - napelnij zdrowie\nv - napelnij mane\nb - utworz pudlo w miejscu kursora\nn - utworz wroga\nm- utworz wroga dyst");
    }
}
