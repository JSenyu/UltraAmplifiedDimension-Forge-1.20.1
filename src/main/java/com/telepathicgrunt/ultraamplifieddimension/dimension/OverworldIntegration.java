package com.telepathicgrunt.ultraamplifieddimension.dimension;

import com.mojang.serialization.Lifecycle;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.config.UADimensionConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import javax.annotation.Nullable;
import java.util.Map;

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

    /**
     * Keep the separate {@code ultra_amplified_dimension} LevelStem only in classic mode
     * (portal destination while Overworld stays vanilla). When Overworld is already UAD,
     * that extra dimension is redundant and should not appear in {@code /execute in}.
     */
    public static boolean shouldKeepSeparateUadDimension() {
        if (UADimensionConfig.overrideVanillaOverworld.get()) {
            return false;
        }
        if (UADimensionConfig.setUadAsDefaultDimension.get()) {
            return false;
        }
        return UADimensionConfig.enableUadDimension.get();
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
                .map(LevelStem::generator)
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

    /** Drop a LevelStem from a frozen registry (used when baking world dimensions). */
    public static Registry<LevelStem> withoutLevelStem(Registry<LevelStem> registry, ResourceKey<LevelStem> remove) {
        if (!registry.containsKey(remove)) {
            return registry;
        }
        WritableRegistry<LevelStem> writable = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.experimental());
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            if (entry.getKey().equals(remove)) {
                continue;
            }
            writable.register(entry.getKey(), entry.getValue(), registry.lifecycle(entry.getValue()));
        }
        UltraAmplifiedDimension.LOGGER.info("Omitting separate dimension {}", remove.location());
        return writable.freeze();
    }
}
