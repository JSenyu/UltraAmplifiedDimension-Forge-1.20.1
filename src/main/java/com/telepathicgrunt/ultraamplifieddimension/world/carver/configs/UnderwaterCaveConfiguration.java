package com.telepathicgrunt.ultraamplifieddimension.world.carver.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class UnderwaterCaveConfiguration extends CaveCarverConfiguration {
    public static final Codec<UnderwaterCaveConfiguration> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((config) -> config.probability)
    ).apply(builder, UnderwaterCaveConfiguration::new));

    public UnderwaterCaveConfiguration(float probability) {
        super(
                probability,
                UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(47)),
                ConstantFloat.of(1.0F),
                VerticalAnchor.aboveBottom(11),
                BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.OVERWORLD_CARVER_REPLACEABLES),
                UniformFloat.of(1.0F, 1.0F),
                UniformFloat.of(1.0F, 1.0F),
                UniformFloat.of(-1.0F, -1.0F)
        );
    }
}
