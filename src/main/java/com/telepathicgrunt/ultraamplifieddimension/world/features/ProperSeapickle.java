package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.SeaPickleConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ProperSeapickle extends Feature<SeaPickleConfig> {

    public ProperSeapickle(Codec<SeaPickleConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SeaPickleConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        SeaPickleConfig config = context.config();
        int picklesPlaced = 0;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int i = 0; i < config.count; ++i) {
            int x = random.nextInt(8) - random.nextInt(8);
            int z = random.nextInt(8) - random.nextInt(8);
            int y = random.nextInt(8) - random.nextInt(8);
            mutable.set(pos).move(x, y, z);
            BlockState blockstate = Blocks.SEA_PICKLE.defaultBlockState()
                    .setValue(SeaPickleBlock.PICKLES, random.nextInt(config.maxPickles - (config.minPickles - 1)) + config.minPickles);
            if (level.getBlockState(mutable).is(Blocks.WATER) && blockstate.canSurvive(level, mutable)) {
                level.setBlock(mutable, blockstate, 2);
                ++picklesPlaced;
            }
        }

        return picklesPlaced > 0;
    }
}
