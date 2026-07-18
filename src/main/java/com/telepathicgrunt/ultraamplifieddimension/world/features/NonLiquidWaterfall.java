package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.TwoBlockStateConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class NonLiquidWaterfall extends Feature<TwoBlockStateConfig> {

    public NonLiquidWaterfall(Codec<TwoBlockStateConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<TwoBlockStateConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        TwoBlockStateConfig config = context.config();

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(position);
        ChunkAccess cachedChunk = level.getChunk(blockposMutable);

        BlockState blockState = cachedChunk.getBlockState(blockposMutable.move(Direction.UP));
        if (!GeneralUtils.isFullCube(level, blockposMutable, blockState) || !blockState.getFluidState().isEmpty()) {
            return false;
        }

        int numberOfSolidSides = 0;
        int neededNumberOfSides;

        blockState = cachedChunk.getBlockState(blockposMutable.set(position).move(Direction.DOWN));
        if (!blockState.getFluidState().isEmpty()) {
            return false;
        }

        if (!GeneralUtils.isFullCube(level, blockposMutable, blockState)) {
            neededNumberOfSides = 4;
        } else {
            neededNumberOfSides = 3;
        }

        Direction emptySpot = null;
        for (Direction face : Direction.Plane.HORIZONTAL) {
            blockposMutable.set(position).move(face);
            if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                cachedChunk = level.getChunk(blockposMutable);
            }

            blockState = cachedChunk.getBlockState(blockposMutable);
            if (!blockState.getFluidState().isEmpty()) {
                return false;
            }

            if (GeneralUtils.isFullCube(level, blockposMutable, blockState)) {
                ++numberOfSolidSides;
            } else {
                emptySpot = face;
            }
        }

        if (numberOfSolidSides != neededNumberOfSides) {
            return false;
        }

        blockposMutable.set(position);
        if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
            cachedChunk = level.getChunk(blockposMutable);
        }

        cachedChunk.setBlockState(blockposMutable, config.state1, false);

        if (emptySpot != null) {
            blockposMutable.move(emptySpot);
            if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                cachedChunk = level.getChunk(blockposMutable);
            }

            cachedChunk.setBlockState(blockposMutable, config.state1, false);
        }

        int ledgeOffsets = 0;
        boolean deadEnd;

        while (blockposMutable.getY() > 1) {
            if (ledgeOffsets > 3) {
                break;
            }

            blockposMutable.move(Direction.DOWN);
            BlockState belowBlockState = cachedChunk.getBlockState(blockposMutable);

            if (!GeneralUtils.isFullCube(level, blockposMutable, belowBlockState) && belowBlockState.getFluidState().isEmpty()) {
                cachedChunk.setBlockState(blockposMutable, config.state1, false);
                continue;
            }

            blockposMutable.move(Direction.UP);

            deadEnd = true;
            for (Direction face : Direction.Plane.HORIZONTAL) {
                blockposMutable.move(face);
                if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                    cachedChunk = level.getChunk(blockposMutable);
                }
                BlockState sideBlockState = cachedChunk.getBlockState(blockposMutable);

                if (!GeneralUtils.isFullCube(level, blockposMutable, sideBlockState) && sideBlockState.getFluidState().isEmpty()) {
                    blockposMutable.move(Direction.DOWN);
                    BlockState belowSideBlockState = cachedChunk.getBlockState(blockposMutable);

                    if (!GeneralUtils.isFullCube(level, blockposMutable, belowSideBlockState) && belowSideBlockState.getFluidState().isEmpty()) {
                        blockposMutable.move(Direction.UP);
                        cachedChunk.setBlockState(blockposMutable, config.state1, false);
                        cachedChunk.setBlockState(blockposMutable.move(Direction.DOWN), config.state1, false);

                        ledgeOffsets++;
                        deadEnd = false;
                        if (blockposMutable.getY() <= 1) {
                            return false;
                        } else {
                            break;
                        }
                    }

                    blockposMutable.move(Direction.UP);
                }

                blockposMutable.move(face.getOpposite());
            }

            if (deadEnd) {
                break;
            }
        }

        position = blockposMutable.immutable();
        int width = random.nextInt(2) + 2;
        for (int y = -2; y < 0; y++) {
            for (int x = -width; x <= width; x++) {
                for (int z = -width; z <= width; z++) {
                    if ((x * x) + (z * z) <= width * width) {
                        if (position.getY() + y > 1 && position.getY() + y < level.getMaxBuildHeight()) {
                            blockposMutable.set(position).move(x, y, z);
                            if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                                cachedChunk = level.getChunk(blockposMutable);
                            }

                            BlockState blockStateAtPuddlePos = cachedChunk.getBlockState(blockposMutable);

                            if (GeneralUtils.isFullCube(level, blockposMutable, blockStateAtPuddlePos) || !blockStateAtPuddlePos.getFluidState().isEmpty()) {
                                BlockState aboveBlockState = cachedChunk.getBlockState(blockposMutable.move(Direction.UP));
                                boolean isAboveFullCube = GeneralUtils.isFullCube(level, blockposMutable, aboveBlockState);
                                blockposMutable.move(Direction.DOWN);

                                if (GeneralUtils.isSurfaceBlock(blockStateAtPuddlePos) && !isAboveFullCube) {
                                    cachedChunk.setBlockState(blockposMutable, config.state2, false);

                                    if (aboveBlockState.is(Blocks.SNOW)) {
                                        cachedChunk.setBlockState(blockposMutable.above(), Blocks.AIR.defaultBlockState(), false);
                                    }
                                } else {
                                    if (config.state1.is(BlockTags.ICE) && blockStateAtPuddlePos.getFluidState().is(FluidTags.LAVA)) {
                                        cachedChunk.setBlockState(blockposMutable, Blocks.OBSIDIAN.defaultBlockState(), false);
                                    } else {
                                        cachedChunk.setBlockState(blockposMutable, config.state1, false);
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            }

            width++;
        }

        return true;
    }
}
