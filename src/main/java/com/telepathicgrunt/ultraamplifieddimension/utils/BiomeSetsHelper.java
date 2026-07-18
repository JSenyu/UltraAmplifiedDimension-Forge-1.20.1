package com.telepathicgrunt.ultraamplifieddimension.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class BiomeSetsHelper {

    public static final Set<Biome> FROZEN_BIOMES = new HashSet<>();
    public static final Set<Biome> COLD_OCEAN_BIOMES = new HashSet<>();

    public static void generateBiomeSets() {
        FROZEN_BIOMES.clear();
        COLD_OCEAN_BIOMES.clear();

        for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
            ResourceLocation id = ForgeRegistries.BIOMES.getKey(biome);
            if (id == null) {
                continue;
            }

            String path = id.getPath();

            if (biome.getBaseTemperature() <= 0.05F || path.contains("frozen")) {
                FROZEN_BIOMES.add(biome);
            }

            if (GeneralUtils.biomeHasTag(biome, BiomeTags.IS_OCEAN) && path.contains("cold")) {
                COLD_OCEAN_BIOMES.add(biome);
            }
        }
    }
}
