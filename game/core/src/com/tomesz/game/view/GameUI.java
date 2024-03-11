package com.tomesz.game.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.WorldContactListener;
import com.tomesz.game.ecs.components.PlayerComponent;

public class GameUI extends Table implements WorldContactListener.PlayerCollisionListener {
    private TextButton diamondsButton;

    public GameUI(final DungeonWarrior context) {
        super(context.getSkin());
        context.getWorldContactListener().addPlayerCollisionListener(this);
        setFillParent(true);
        diamondsButton  = new TextButton("Diamonds: 0", getSkin(), "huge");
        add(diamondsButton);
        bottom();
        right();
    }

    @Override
    public void PlayerCollision(Entity player, Entity gameObject) {
        int diamondsCount = player.getComponent(PlayerComponent.class).getDiamonds();
        diamondsButton.setText("Diamonds: " + diamondsCount);
    }
}
