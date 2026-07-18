package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.ProbabilityAndCountConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ProperKelp extends Feature<ProbabilityAndCountConfig> {

    public ProperKelp(Codec<ProbabilityAndCountConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityAndCountConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        int placedKelp = 0;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(pos);
        ChunkAccess chunk = level.getChunk(mutable);
        if (chunk.getBlockState(mutable).is(Blocks.WATER)) {
            BlockState kelpState = Blocks.KELP.defaultBlockState();
            BlockState kelpState2 = Blocks.KELP_PLANT.defaultBlockState();
            int height = 1 + random.nextInt(10);

            for (int currentHeight = 0; currentHeight <= height; ++currentHeight) {
                if (chunk.getBlockState(mutable).is(Blocks.WATER)
                        && chunk.getBlockState(mutable.above()).is(Blocks.WATER)
                        && kelpState2.canSurvive(level, mutable)) {
                    if (currentHeight == height) {
                        chunk.setBlockState(mutable, kelpState.setValue(KelpBlock.AGE, random.nextInt(4) + 20), false);
                        ++placedKelp;
                    } else {
                        level.setBlock(mutable, kelpState2, 2);
                    }
                } else if (currentHeight > 0) {
                    BlockPos blockpos1 = mutable.below();

                    if (kelpState.canSurvive(level, blockpos1)
                            && !chunk.getBlockState(blockpos1.below()).is(Blocks.KELP)) {
                        chunk.setBlockState(blockpos1, kelpState.setValue(KelpBlock.AGE, random.nextInt(4) + 20), false);
                        ++placedKelp;
                    }
                    break;
                }

                mutable.move(Direction.UP);
            }
        }

        return placedKelp > 0;
    }
}
