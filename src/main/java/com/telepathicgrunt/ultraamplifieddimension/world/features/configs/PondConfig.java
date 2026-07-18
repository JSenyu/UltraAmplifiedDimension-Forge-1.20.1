package com.telepathicgrunt.ultraamplifieddimension.world.features.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class PondConfig implements FeatureConfiguration {
    public static final Codec<PondConfig> CODEC = RecordCodecBuilder.create((cactusConfigInstance) -> cactusConfigInstance.group(
            BlockState.CODEC.fieldOf("top_state").forGetter((pondConfig) -> pondConfig.topState),
            BlockState.CODEC.fieldOf("inside_state").forGetter((pondConfig) -> pondConfig.insideState),
            BlockState.CODEC.fieldOf("outside_state").forGetter((pondConfig) -> pondConfig.outsideState),
            Codec.BOOL.fieldOf("place_outside_state_often").forGetter((pondConfig) -> pondConfig.placeOutsideStateOften)
    ).apply(cactusConfigInstance, PondConfig::new));

    public final BlockState topState;
    public final BlockState insideState;
    public final BlockState outsideState;
    public final boolean placeOutsideStateOften;

    public PondConfig(BlockState topState, BlockState insideState, BlockState outsideState, boolean placeOutsideStateOften) {
        this.topState = topState;
        this.insideState = insideState;
        this.outsideState = outsideState;
        this.placeOutsideStateOften = placeOutsideStateOften;
    }
}
