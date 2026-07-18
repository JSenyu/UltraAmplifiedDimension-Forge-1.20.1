package com.telepathicgrunt.ultraamplifieddimension.world.carver.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class CaveConfig extends CarverConfiguration {
    public static final Codec<CaveConfig> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((config) -> config.probability),
            Codec.INT.fieldOf("cutoff_height").forGetter((config) -> config.cutoffHeight)
    ).apply(builder, CaveConfig::new));

    public final int cutoffHeight;

    public CaveConfig(float probability, int cutoffHeight) {
        super(
                probability,
                UniformHeight.of(VerticalAnchor.absolute(34), VerticalAnchor.absolute(34)),
                ConstantFloat.of(1.0F),
                VerticalAnchor.aboveBottom(11),
                CarverDebugSettings.DEFAULT,
                BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.OVERWORLD_CARVER_REPLACEABLES)
        );
        this.cutoffHeight = cutoffHeight;
    }
}
