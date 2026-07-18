package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class NetherSeaAdjuster extends Feature<NoneFeatureConfiguration> {
    public NetherSeaAdjuster(Codec<NoneFeatureConfiguration> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        BlockPos position = context.origin();

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos(position.getX(), 0, position.getZ());
        BlockPos.MutableBlockPos blockposMutableTemp = new BlockPos.MutableBlockPos();
        ChunkAccess cachedChunk = world.getChunk(position.getX() >> 4, position.getZ() >> 4);
        int seaLevel = world.getSeaLevel();

        for (int x = -6; x < 22; ++x) {
            for (int z = -6; z < 22; ++z) {

                if (!world.getBiome(blockposMutable.set(position).move(x, seaLevel - 7, z)).is(BiomeTags.IS_NETHER)) {
                    continue;
                }

                if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                    cachedChunk = world.getChunk(blockposMutable);
                }

                BlockState prevBlockState = Blocks.AIR.defaultBlockState();
                for (int y = seaLevel - 7; y <= seaLevel; ++y) {

                    BlockState currentBlockState = cachedChunk.getBlockState(blockposMutable);
                    if (currentBlockState.getFluidState().is(FluidTags.WATER)) {

                        for (Direction direction : Direction.values()) {
                            blockposMutableTemp.set(blockposMutable).move(direction);
                            BlockState neighboringBlock;
                            if (blockposMutableTemp.getX() >> 4 != cachedChunk.getPos().x || blockposMutableTemp.getZ() >> 4 != cachedChunk.getPos().z) {
                                neighboringBlock = world.getBlockState(blockposMutableTemp);
                            } else {
                                neighboringBlock = cachedChunk.getBlockState(blockposMutableTemp);
                            }

                            if (neighboringBlock.getFluidState().is(FluidTags.LAVA)) {
                                cachedChunk.setBlockState(blockposMutableTemp, Blocks.OBSIDIAN.defaultBlockState(), false);
                                prevBlockState = Blocks.OBSIDIAN.defaultBlockState();
                                blockposMutable.move(Direction.UP);
                                break;
                            }
                        }

                        if (prevBlockState.is(Blocks.MAGMA_BLOCK) || prevBlockState.is(Blocks.BUBBLE_COLUMN)) {
                            cachedChunk.setBlockState(blockposMutable, Blocks.BUBBLE_COLUMN.defaultBlockState(), false);
                            prevBlockState = Blocks.BUBBLE_COLUMN.defaultBlockState();
                            blockposMutable.move(Direction.UP);
                            continue;
                        }
                    }

                    prevBlockState = currentBlockState;
                    blockposMutable.move(Direction.UP);
                }
            }
        }

        return true;
    }
}
