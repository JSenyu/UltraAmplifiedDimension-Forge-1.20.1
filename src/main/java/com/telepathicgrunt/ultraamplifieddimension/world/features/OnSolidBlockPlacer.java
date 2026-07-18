package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.BlockWithRuleReplaceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class OnSolidBlockPlacer extends Feature<BlockWithRuleReplaceConfig> {

    public OnSolidBlockPlacer(Codec<BlockWithRuleReplaceConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockWithRuleReplaceConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        BlockWithRuleReplaceConfig replaceBlockConfig = context.config();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(position);

        if (replaceBlockConfig.target.test(level.getBlockState(mutable), context.random())
                && level.getBlockState(mutable.move(Direction.DOWN)).isFaceSturdy(level, mutable, Direction.UP)) {
            level.setBlock(mutable.move(Direction.UP), replaceBlockConfig.state, 2);
            return true;
        }

        return false;
    }
}
