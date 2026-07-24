package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.DiskDryConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class DiskDry extends Feature<DiskDryConfig> {

    public DiskDry(Codec<DiskDryConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskDryConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        DiskDryConfig config = context.config();
        int placedBlocks = 0;
        int radius = config.radius.sample(random);
        if (radius > 2) {
            radius = random.nextInt(radius - 2) + 2;
        }

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(position);
        ChunkAccess cachedChunk = level.getChunk(blockposMutable);

        for (int x = position.getX() - radius; x <= position.getX() + radius; ++x) {
            for (int z = position.getZ() - radius; z <= position.getZ() + radius; ++z) {
                int trueX = x - position.getX();
                int trueZ = z - position.getZ();
                if (trueX * trueX + trueZ * trueZ <= radius * radius) {
                    blockposMutable.set(x, position.getY(), z);
                    if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                        cachedChunk = level.getChunk(blockposMutable);
                    }

                    blockposMutable.move(Direction.DOWN, config.half_height);
                    for (int y = -config.half_height; y <= config.half_height; ++y) {
                        BlockState aboveBlockState = cachedChunk.getBlockState(blockposMutable.move(Direction.UP));
                        BlockState blockState = cachedChunk.getBlockState(blockposMutable.move(Direction.DOWN));

                        if (!config.exposedOnly || !aboveBlockState.canOcclude()) {
                            if (!blockState.hasBlockEntity()) {
                                for (BlockState targetBlockState : config.targets) {
                                    if (blockState.is(targetBlockState.getBlock())) {
                                        GeneralUtils.setChunkBlockState(cachedChunk, blockposMutable, config.state);
                                        ++placedBlocks;

                                        if (aboveBlockState.is(Blocks.SNOW) && !aboveBlockState.canSurvive(level, blockposMutable)) {
                                            GeneralUtils.setChunkBlockState(cachedChunk, blockposMutable.move(Direction.UP), Blocks.AIR.defaultBlockState());
                                            blockposMutable.move(Direction.DOWN);
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        blockposMutable.move(Direction.UP);
                    }
                }
            }
        }

        return placedBlocks > 0;
    }
}
