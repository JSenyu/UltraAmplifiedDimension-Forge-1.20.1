package com.telepathicgrunt.ultraamplifieddimension.world.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.terrain.UADTerrainSampler;
import net.minecraft.util.Mth;

/**
 * Vertical bounds for vanilla/custom structures inside UAD terrain.
 * Default floor is Y=0; with generateBelowZero the floor is Y=-64.
 */
public final class UADStructureHeights {
    public static final int MAX_TERRAIN = UADTerrainSampler.MAX_TERRAIN_Y;
    public static final int MAX_MANSION = 180;
    public static final int UNDERGROUND_MIN = 16;
    public static final int UNDERGROUND_MAX = 140;
    public static final int NETHER_MIN = 88;
    public static final int NETHER_MAX = 135;
    public static final int END_CITY_Y = 105;

    private UADStructureHeights() {
    }

    public static int minSolid() {
        return UADTerrainSampler.minY() + 5;
    }

    public static int clampTerrainY(int y) {
        return Mth.clamp(y, minSolid(), MAX_TERRAIN);
    }

    public static int clampMansionY(int y) {
        return Mth.clamp(y, minSolid(), MAX_MANSION);
    }
}
