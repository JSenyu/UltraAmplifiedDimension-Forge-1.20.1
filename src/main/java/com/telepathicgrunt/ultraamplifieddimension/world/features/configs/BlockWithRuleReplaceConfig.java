package com.telepathicgrunt.ultraamplifieddimension.world.features.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class BlockWithRuleReplaceConfig implements FeatureConfiguration {
    public static final Codec<BlockWithRuleReplaceConfig> CODEC = RecordCodecBuilder.create((columnConfigInstance) -> columnConfigInstance.group(
            RuleTest.CODEC.fieldOf("target").forGetter((config) -> config.target),
            BlockState.CODEC.fieldOf("state").forGetter((config) -> config.state)
        ).apply(columnConfigInstance, BlockWithRuleReplaceConfig::new));

    public final RuleTest target;
    public final BlockState state;

    public BlockWithRuleReplaceConfig(RuleTest target, BlockState state) {
        this.state = state;
        this.target = target;
    }
}
