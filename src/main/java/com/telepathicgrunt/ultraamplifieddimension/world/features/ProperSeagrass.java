package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.ProbabilityAndCountConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ProperSeagrass extends Feature<ProbabilityAndCountConfig> {

    public ProperSeagrass(Codec<ProbabilityAndCountConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityAndCountConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        ProbabilityAndCountConfig config = context.config();
        boolean flag = false;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int i = 0; i < config.count; i++) {
            int x = random.nextInt(8) - random.nextInt(8);
            int z = random.nextInt(8) - random.nextInt(8);
            int y = random.nextInt(8) - random.nextInt(8);
            mutable.set(pos).move(x, y, z);

            if (level.getBlockState(mutable).is(Blocks.WATER)) {
                boolean spawnTallGrass = random.nextFloat() < config.probability;
                BlockState blockstate = spawnTallGrass ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();

                if (blockstate.canSurvive(level, mutable)) {
                    if (spawnTallGrass) {
                        BlockState blockstate1 = blockstate.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
                        if (level.getBlockState(mutable.move(Direction.UP)).is(Blocks.WATER)) {
                            level.setBlock(mutable.move(Direction.DOWN), blockstate, 2);
                            level.setBlock(mutable.move(Direction.UP), blockstate1, 2);
                        }
                    } else {
                        level.setBlock(mutable, blockstate, 2);
                    }

                    flag = true;
                }
            }
        }

        return flag;
    }
}
