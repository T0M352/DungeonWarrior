package com.tomesz.game.view;

public enum AnimationType {
    MAGE_MOVE_LEFT("mage/mage.atlas", "mage_walk", 0.1f, 0),
    MAGE_MOVE_RIGHT("mage/mage.atlas", "mage_walk", 0.1f, 1),
    MAGE_IDLE("mage/mage.atlas", "mage_walk", 0.5f, 2),
    BOX_END("mage/mage.atlas", "boxEnd", 0.1f, 0),
    TABLE_END("mage/mage.atlas", "tableLongEnd", 0.1f, 0),
    TABLEUP_END("mage/mage.atlas", "tableUpEnd", 0.1f, 0),
    BARREL_END("mage/mage.atlas", "barrelEnd", 0.1f, 0),
    FIREBALL_END("mage/mage.atlas", "fireball_end", 0.1f, 0),
    KOBOLD_RIGHT("mage/mage.atlas", "kobold_animation", 0.3f, 0),
    KOBOLD_LEFT("mage/mage.atlas", "kobold_animation", 0.3f, 1),
    KOBOLD_FRONT("mage/mage.atlas", "kobold_animation", 0.3f, 2),
    KOBOLD_BACK("mage/mage.atlas", "kobold_animation", 0.3f, 3),


    KOBOLD_ATTACK_RIGHT("mage/mage.atlas", "kobold_animation", 0.17f, 4),
    KOBOLD_ATTACK_LEFT("mage/mage.atlas", "kobold_animation", 0.17f, 5),
    KOBOLD_ATTACK_FRONT("mage/mage.atlas", "kobold_animation", 0.17f, 6),
    KOBOLD_ATTACK_BACK("mage/mage.atlas", "kobold_animation", 0.17f, 7),

    KOBOLD_DEATH("mage/mage.atlas", "kobold_animation", 0.1f, 8),

    KOBOLD_LEGS_IDLE_RIGHT("mage/mage.atlas", "koboldLegs", 0.1f, 0),
    KOBOLD_LEGS_WALK_RIGHT("mage/mage.atlas", "koboldLegs", 0.1f, 1),
    KOBOLD_LEGS_IDLE_LEFT("mage/mage.atlas", "koboldLegs", 0.1f, 2),
    KOBOLD_LEGS_WALK_LEFT("mage/mage.atlas", "koboldLegs", 0.1f, 3),
    KOBOLD_LEGS_WALK_FRONT("mage/mage.atlas", "koboldLegs", 0.1f, 4),
    KOBOLD_LEGS_WALK_BACK("mage/mage.atlas", "koboldLegs", 0.1f, 5),

    GOBLIN_SHAMAN_RIGHT("mage/mage.atlas", "goblin_shaman_animation", 0.4f, 0),
    GOBLIN_SHAMAN_LEFT("mage/mage.atlas", "goblin_shaman_animation", 0.4f, 2),
    GOBLIN_SHAMAN_ATTACK_LEFT("mage/mage.atlas", "goblin_shaman_animation", 0.1f, 1),
    GOBLIN_SHAMAN_ATTACK_RIGHT("mage/mage.atlas", "goblin_shaman_animation", 0.1f, 3),
    GOBLIN_SHAMAN_LEGS_LEFT("mage/mage.atlas", "shaman_legs", 0.1f, 3),
    GOBLIN_SHAMAN_LEGS_RIGHT("mage/mage.atlas", "shaman_legs", 0.1f, 1),
    GOBLIN_SHAMAN_LEGS_LEFT_WALK("mage/mage.atlas", "shaman_legs", 0.1f, 2),
    GOBLIN_SHAMAN_LEGS_RIGHT_WALK("mage/mage.atlas", "shaman_legs", 0.1f, 0),
    GOBLIN_SHAMAN_DEATH("mage/mage.atlas", "goblin_shaman_animation", 0.1f, 4);






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
