package com.telepathicgrunt.ultraamplifieddimension.world.carver.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class RavineConfig extends CarverConfiguration {
    public static final Codec<RavineConfig> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((config) -> config.probability),
            spreadIntCodec().fieldOf("height_placement").forGetter((config) -> new SpreadValues(config.heightPlacementBase, config.heightPlacementSpread)),
            Codec.INT.fieldOf("cutoff_height").forGetter((config) -> config.cutoffHeight),
            spreadIntCodec().fieldOf("tallness").forGetter((config) -> new SpreadValues(config.tallnessBase, config.tallnessSpread))
    ).apply(builder, RavineConfig::new));

    public final int heightPlacementBase;
    public final int heightPlacementSpread;
    public final int cutoffHeight;
    public final int tallnessBase;
    public final int tallnessSpread;

    public RavineConfig(float probability, SpreadValues heightPlacement, int cutoffHeight, SpreadValues tallness) {
        super(
                probability,
                UniformHeight.of(VerticalAnchor.absolute(heightPlacement.base()), VerticalAnchor.absolute(heightPlacement.base())),
                ConstantFloat.of(1.0F),
                VerticalAnchor.aboveBottom(11),
                CarverDebugSettings.DEFAULT,
                BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.OVERWORLD_CARVER_REPLACEABLES)
        );
        this.heightPlacementBase = heightPlacement.base();
        this.heightPlacementSpread = heightPlacement.spread();
        this.cutoffHeight = cutoffHeight;
        this.tallnessBase = tallness.base();
        this.tallnessSpread = tallness.spread();
    }

    public int sampleHeightPlacement(RandomSource random) {
        return this.heightPlacementBase + random.nextInt(this.heightPlacementSpread + 1);
    }

    public int sampleTallness(RandomSource random) {
        return this.tallnessBase + random.nextInt(this.tallnessSpread + 1);
    }

    private static Codec<SpreadValues> spreadIntCodec() {
        return RecordCodecBuilder.create((builder) -> builder.group(
                Codec.INT.fieldOf("base").forGetter(SpreadValues::base),
                Codec.INT.fieldOf("spread").forGetter(SpreadValues::spread)
        ).apply(builder, SpreadValues::new));
    }

    private record SpreadValues(int base, int spread) {
    }
}
