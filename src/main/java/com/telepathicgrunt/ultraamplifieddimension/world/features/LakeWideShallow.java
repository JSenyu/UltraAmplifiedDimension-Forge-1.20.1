package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class LakeWideShallow extends Feature<BlockStateConfiguration> {

    public LakeWideShallow(Codec<BlockStateConfiguration> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        BlockStateConfiguration configBlock = context.config();

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(position);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int y = 5;

                blockposMutable.set(position).move(x, y, z);

                BlockState blockState = level.getBlockState(blockposMutable);

                while (!isAcceptableSolid(blockState) && y > 0) {
                    y--;
                    blockState = level.getBlockState(blockposMutable.move(Direction.DOWN));
                }

                boolean containedFlag = checkIfValidSpot(level, blockposMutable);

                if (containedFlag) {
                    double normX = (x - 8) / 8d;
                    double normZ = (z - 8) / 8d;
                    double lakeVal = (normX * normX) + (normZ * normZ);

                    if (lakeVal < 0.8d) {
                        blockState = level.getBlockState(blockposMutable.below());

                        if (Feature.isDirt(blockState) && random.nextInt(5) == 0) {
                            level.setBlock(blockposMutable, Blocks.SEAGRASS.defaultBlockState(), 3);
                        } else {
                            level.setBlock(blockposMutable, configBlock.state, 3);
                        }

                        blockState = level.getBlockState(blockposMutable.move(Direction.UP));

                        while (blockposMutable.getY() < level.getMaxBuildHeight() && !blockState.canSurvive(level, blockposMutable)) {
                            level.setBlock(blockposMutable, Blocks.AIR.defaultBlockState(), 3);
                            blockState = level.getBlockState(blockposMutable.move(Direction.UP));
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean isUnacceptableSolid(BlockState state) {
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        return state.is(BlockTags.LEAVES)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.BAMBOO_SAPLING)
                || state.is(Blocks.COBWEB)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.ANVIL)
                || state.is(Blocks.DRAGON_EGG)
                || state.is(Blocks.BARRIER)
                || state.is(Blocks.CAKE)
                || state.is(Blocks.PUMPKIN)
                || state.is(Blocks.CARVED_PUMPKIN)
                || state.is(Blocks.JACK_O_LANTERN)
                || state.is(Blocks.MELON)
                || state.is(BlockTags.PLANKS)
                || state.getBlock() instanceof HugeMushroomBlock;
    }

    private static boolean isAcceptableSolid(BlockState state) {
        if (!state.getFluidState().isEmpty()) {
            return state.getFluidState().is(FluidTags.WATER);
        }
        return GeneralUtils.isSolidBlock(state) && !isUnacceptableSolid(state);
    }

    private boolean checkIfValidSpot(WorldGenLevel level, BlockPos blockposMutable) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(blockposMutable);

        for (int x2 = -1; x2 <= 1; x2++) {
            for (int z2 = -1; z2 <= 1; z2++) {
                if (x2 == 0 && z2 == 0) {
                    continue;
                }

                mutable.set(blockposMutable).move(x2, 0, z2);
                BlockState blockState = level.getBlockState(mutable);

                if (!isAcceptableSolid(blockState)
                        && blockState.getFluidState().isEmpty()
                        && !blockState.getFluidState().is(FluidTags.WATER)) {
                    return false;
                }
            }
        }

        BlockState blockState = level.getBlockState(mutable.set(blockposMutable).move(Direction.DOWN));
        if (!isAcceptableSolid(blockState)
                && blockState.getFluidState().isEmpty()
                && !blockState.getFluidState().is(FluidTags.WATER)) {
            return false;
        }

        blockState = level.getBlockState(mutable.move(Direction.UP, 2));
        return !GeneralUtils.isSolidBlock(blockState)
                && blockState.getFluidState().isEmpty()
                && !blockState.is(Blocks.RAIL);
    }
}
