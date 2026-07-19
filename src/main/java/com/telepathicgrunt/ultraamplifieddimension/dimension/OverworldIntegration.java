package com.telepathicgrunt.ultraamplifieddimension.dimension;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.config.UADimensionConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import javax.annotation.Nullable;

public final class OverworldIntegration {
    public static final ResourceKey<WorldPreset> UAD_WORLD_PRESET = ResourceKey.create(
            Registries.WORLD_PRESET,
            ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "ultra_amplified")
    );

    public static final ResourceKey<Level> ORIGINAL_OVERWORLD_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "original_overworld")
    );

    private OverworldIntegration() {
    }

    public static boolean portalsEnabled() {
        // Override mode removes portal travel. Classic mode needs the extra dimension enabled.
        // setUadAsDefaultDimension keeps portals to the preserved vanilla overworld.
        if (UADimensionConfig.overrideVanillaOverworld.get()) {
            return false;
        }
        return UADimensionConfig.enableUadDimension.get() || UADimensionConfig.setUadAsDefaultDimension.get();
    }

    public static boolean shouldReplaceDefaultOverworld() {
        return UADimensionConfig.overrideVanillaOverworld.get() || UADimensionConfig.setUadAsDefaultDimension.get();
    }

    public static boolean isUadChunkGenerator(ChunkGenerator generator) {
        return generator instanceof UADChunkGenerator;
    }

    public static boolean isUadOverworld(ServerLevel level) {
        return level.dimension().equals(Level.OVERWORLD) && isUadChunkGenerator(level.getChunkSource().getGenerator());
    }

    public static boolean overworldIsUltraAmplified(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        return overworld != null && isUadOverworld(overworld);
    }

    public static boolean usesOriginalOverworldPortal(MinecraftServer server) {
        return portalsEnabled() && overworldIsUltraAmplified(server);
    }

    public static boolean shouldReplaceOverworldGenerator(ChunkGenerator currentOverworld) {
        if (isUadChunkGenerator(currentOverworld)) {
            return false;
        }
        if (UADimensionConfig.overrideVanillaOverworld.get()) {
            return !(currentOverworld instanceof FlatLevelSource) && !(currentOverworld instanceof DebugLevelSource);
        }
        if (UADimensionConfig.setUadAsDefaultDimension.get()) {
            return currentOverworld instanceof NoiseBasedChunkGenerator
                    && !(currentOverworld instanceof FlatLevelSource)
                    && !(currentOverworld instanceof DebugLevelSource);
        }
        return false;
    }

    @Nullable
    public static ChunkGenerator createUadOverworldGenerator(RegistryAccess access) {
        Registry<WorldPreset> presets = access.registryOrThrow(Registries.WORLD_PRESET);
        Holder.Reference<WorldPreset> holder = presets.getHolder(UAD_WORLD_PRESET).orElse(null);
        if (holder == null) {
            UltraAmplifiedDimension.LOGGER.error("Missing world preset {}", UAD_WORLD_PRESET.location());
            return null;
        }
        return holder.value().overworld()
                .map(stem -> stem.generator())
                .orElse(null);
    }

    public static WorldDimensions applyDefaultOverworldReplacement(RegistryAccess access, WorldDimensions dimensions) {
        ChunkGenerator current = dimensions.overworld();
        if (!shouldReplaceOverworldGenerator(current)) {
            return dimensions;
        }
        ChunkGenerator generator = createUadOverworldGenerator(access);
        if (generator == null) {
            return dimensions;
        }
        UltraAmplifiedDimension.LOGGER.info(
                "Applying Ultra Amplified overworld replacement (override={}, setAsDefault={})",
                UADimensionConfig.overrideVanillaOverworld.get(),
                UADimensionConfig.setUadAsDefaultDimension.get()
        );
        return dimensions.replaceOverworldGenerator(access, generator);
    }
}
