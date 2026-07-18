package com.telepathicgrunt.ultraamplifieddimension.world.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.terrain.UADTerrainSampler;
import net.minecraft.util.Mth;

/**
 * Vertical bounds for vanilla/custom structures inside UAD terrain (Y 0..243, bedrock ~0-5).
 */
public final class UADStructureHeights {
    public static final int MIN_SOLID = 5;
    public static final int MAX_TERRAIN = UADTerrainSampler.MAX_TERRAIN_Y;
    public static final int MAX_MANSION = 180;
    public static final int UNDERGROUND_MIN = 16;
    public static final int UNDERGROUND_MAX = 140;
    public static final int NETHER_MIN = 88;
    public static final int NETHER_MAX = 135;
    public static final int END_CITY_Y = 105;

    private UADStructureHeights() {
    }

    public static int clampTerrainY(int y) {
        return Mth.clamp(y, MIN_SOLID, MAX_TERRAIN);
    }

    public static int clampMansionY(int y) {
        return Mth.clamp(y, MIN_SOLID, MAX_MANSION);
    }
}
