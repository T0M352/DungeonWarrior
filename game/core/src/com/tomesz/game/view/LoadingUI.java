package com.tomesz.game.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.StringBuilder;
import com.tomesz.game.DungeonWarrior;
import com.tomesz.game.input.GameKeys;

public class LoadingUI extends Table {
    private final ProgressBar progressBar;
    private final TextButton pressAnyKeyButton;
    private final TextButton textButton;
    public LoadingUI(final DungeonWarrior context) {
        super(context.getSkin());
        setFillParent(true);

        progressBar = new ProgressBar(0, 1, 0.01f, false, getSkin(), "default");
        //progressBar.setAnimateDuration(1);

        pressAnyKeyButton = new TextButton("Press any key...", getSkin(), "normal");
        pressAnyKeyButton.getLabel().setWrap(true);
        pressAnyKeyButton.setVisible(false);
        pressAnyKeyButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                context.getInputManager().notifyKeyDown(GameKeys.SELECT);
                return true;
            }
        });

        textButton = new TextButton("Loading...", getSkin(), "huge");
        textButton.getLabel().setWrap(true);

        add(pressAnyKeyButton)
                .expand()
                .fill()
                .center()
                .row();
        add(textButton)
                .expandX()
                .fillX()
                .bottom()
                .row();
        add(progressBar)
                .expandX()
                .fillX()
                .bottom()
                .pad(20, 25, 20, 25);




        //setDebug(true, true);
    }

    public void setProgress(final float progress){
        progressBar.setValue(progress);

        final StringBuilder sb = textButton.getLabel().getText();
        sb.setLength(0);
        sb.append("Loading... (");
        sb.append(((int)progress*100));
        sb.append("%)");
        textButton.getLabel().invalidateHierarchy(); // potrzebne do przerenderowania zmienionego tekstu

        if(progress >= 0.99 && !pressAnyKeyButton.isVisible()){
            pressAnyKeyButton.setVisible(true);
            pressAnyKeyButton.setColor(1,1,1,0);
            pressAnyKeyButton.addAction(Actions.forever(Actions.sequence(Actions.alpha(1, 1), Actions.alpha(0,1))));
        }
    }
}
