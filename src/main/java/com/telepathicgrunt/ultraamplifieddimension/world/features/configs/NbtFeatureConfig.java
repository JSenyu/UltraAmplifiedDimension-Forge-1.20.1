package com.telepathicgrunt.ultraamplifieddimension.world.features.configs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;
import java.util.Optional;

public class NbtFeatureConfig implements FeatureConfiguration {

    public static final Codec<NbtFeatureConfig> CODEC = RecordCodecBuilder.create(configInstance -> configInstance.group(
            Codec.intRange(0, 16).fieldOf("solid_land_radius").orElse(3).forGetter(nbtFeatureConfig -> nbtFeatureConfig.solidLandRadius),
            Codec.mapPair(ResourceLocation.CODEC.fieldOf("resourcelocation"), Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight"))
                    .codec()
                    .listOf()
                    .fieldOf("nbt_entries")
                    .forGetter(nbtFeatureConfig -> nbtFeatureConfig.nbtResourcelocationsAndWeights),
            ResourceLocation.CODEC.optionalFieldOf("processors")
                    .forGetter(nbtFeatureConfig -> Optional.ofNullable(nbtFeatureConfig.processor))
    ).apply(configInstance, NbtFeatureConfig::new));

    public final int solidLandRadius;
    public final List<Pair<ResourceLocation, Integer>> nbtResourcelocationsAndWeights;
    public final ResourceLocation processor;

    public NbtFeatureConfig(int solidLandRadius, List<Pair<ResourceLocation, Integer>> nbtResourcelocationsAndWeights,
                            Optional<ResourceLocation> processor) {
        this.solidLandRadius = solidLandRadius;
        this.nbtResourcelocationsAndWeights = nbtResourcelocationsAndWeights;
        this.processor = processor.orElse(null);
    }
}
