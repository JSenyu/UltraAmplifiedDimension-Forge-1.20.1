package com.telepathicgrunt.ultraamplifieddimension.utils;

/**
 * World seed for worldgen when not specified by JSON by Haven King
 * https://github.com/Hephaestus-Dev/seedy-behavior/blob/master/src/main/java/dev/hephaestus/seedy/mixin/world/gen/GeneratorOptionsMixin.java
 */
public final class WorldSeedHolder {
    private static long SEED = 0;

    private WorldSeedHolder() {
    }

    public static long getSeed() {
        return SEED;
    }

    public static long setSeed(long seed) {
        SEED = seed;
        return seed;
    }
}
