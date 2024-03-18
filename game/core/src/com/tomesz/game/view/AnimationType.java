package com.tomesz.game.view;

public enum AnimationType {
    MAGE_MOVE_LEFT("mage/mage.atlas", "mage_walk", 0.1f, 0),
    MAGE_MOVE_RIGHT("mage/mage.atlas", "mage_walk", 0.1f, 1),
    MAGE_IDLE("mage/mage.atlas", "mage_walk", 0.5f, 2),
    BOX_END("mage/mage.atlas", "boxEnd", 0.1f, 0),
    TABLE_END("mage/mage.atlas", "tableLongEnd", 0.1f, 0),
    TABLEUP_END("mage/mage.atlas", "tableUpEnd", 0.1f, 0),
    BARREL_END("mage/mage.atlas", "barrelEnd", 0.1f, 0),
    FIREBALL_END("mage/mage.atlas", "fireball_end", 0.1f, 0);

    private final String atlasPath;
    private final String atlasKey;
    private final float frameTime;
    private final int rowIndex;

    AnimationType(String atlasPath, String atlasKey, float frameTime, int rowIndex) {
        this.atlasPath = atlasPath;
        this.atlasKey = atlasKey;
        this.frameTime = frameTime;
        this.rowIndex = rowIndex;
    }

    public String getAtlasPath() {
        return atlasPath;
    }

    public String getAtlasKey() {
        return atlasKey;
    }

    public float getFrameTime() {
        return frameTime;
    }

    public int getRowIndex() {
        return rowIndex;
    }
}
