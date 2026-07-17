package com.telepathicgrunt.ultraamplifieddimension.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class UADDimension {
    public static final ResourceKey<Level> UAD_WORLD_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, UltraAmplifiedDimension.MODID)
    );

    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, UltraAmplifiedDimension.MODID);

    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, UltraAmplifiedDimension.MODID);

    public static final RegistryObject<Codec<UADChunkGenerator>> UAD_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("terrain", () -> UADChunkGenerator.CODEC);

    public static final RegistryObject<Codec<UADBiomeSource>> UAD_BIOME_SOURCE =
            BIOME_SOURCES.register("biome_source", () -> UADBiomeSource.CODEC);

    private UADDimension() {
    }

    public static void setupDimension() {
    }

    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel && !serverLevel.isClientSide()) {
            UADWorldSavedData.tick(serverLevel);
        }
    }

    public static class UADChunkGenerator extends NoiseBasedChunkGenerator {
        public static final Codec<UADChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                        NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.noiseSettings)
                ).apply(instance, instance.stable(UADChunkGenerator::new))
        );

        private final Holder<NoiseGeneratorSettings> noiseSettings;

        public UADChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
            super(biomeSource, settings);
            this.noiseSettings = settings;
        }

        @Override
        protected Codec<? extends ChunkGenerator> codec() {
            return CODEC;
        }
    }
}
