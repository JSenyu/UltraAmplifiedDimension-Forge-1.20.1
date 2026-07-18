package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.blocks.BigCactusBodyBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.BigCactusCornerBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.BigCactusMainBlock;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADBlocks;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.HeightConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class BigCactus extends Feature<HeightConfig> {

    public BigCactus(Codec<HeightConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<HeightConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        HeightConfig betterCactusConfig = context.config();
        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos().set(position);
        ChunkAccess cachedChunk = level.getChunk(blockpos);
        Direction cactusFacing = Direction.from2DDataValue(random.nextInt(4));

        int maxHeight = betterCactusConfig.height + random.nextInt(2);
        int frontSideHeight = 2 + random.nextInt(maxHeight - 4);
        int backSideHeight = 2 + random.nextInt(maxHeight - 4);

        while (blockpos.getY() <= position.getY() + betterCactusConfig.height + 1) {
            if (!cachedChunk.getBlockState(blockpos).isAir()
                    || (blockpos.getY() >= position.getY() + frontSideHeight
                    && (!cachedChunk.getBlockState(blockpos.move(cactusFacing)).isAir()
                    || !cachedChunk.getBlockState(blockpos.move(cactusFacing)).isAir()))
                    || (blockpos.getY() >= position.getY() + backSideHeight
                    && (!cachedChunk.getBlockState(blockpos.move(cactusFacing.getOpposite(), 3)).isAir()
                    || !cachedChunk.getBlockState(blockpos.move(cactusFacing.getOpposite())).isAir()))) {
                return false;
            }

            blockpos.move(Direction.UP).move(cactusFacing, 2);
        }
        blockpos.set(position);

        if (cachedChunk.getBlockState(blockpos.move(Direction.DOWN)).is(BlockTags.SAND)) {
            blockpos.move(Direction.UP);

            for (int currentHeight = 0; currentHeight < maxHeight && cachedChunk.getBlockState(blockpos).isAir(); currentHeight++) {
                if (blockpos.getY() <= 254 && (currentHeight == frontSideHeight || currentHeight == backSideHeight)) {
                    if (frontSideHeight == backSideHeight) {
                        cachedChunk.setBlockState(blockpos, UADBlocks.BIG_CACTUS_BODY_BLOCK.get().defaultBlockState()
                                .setValue(BigCactusBodyBlock.FACING, cactusFacing), false);
                    } else {
                        cachedChunk.setBlockState(blockpos, UADBlocks.BIG_CACTUS_CORNER_BLOCK.get().defaultBlockState()
                                .setValue(BigCactusCornerBlock.FACING, currentHeight == frontSideHeight ? cactusFacing.getOpposite() : cactusFacing), false);
                    }

                    if (currentHeight == frontSideHeight) {
                        createBranch(level, blockpos, cactusFacing, random.nextInt(maxHeight - frontSideHeight - 2) + 2);
                    }
                    if (currentHeight == backSideHeight) {
                        createBranch(level, blockpos, cactusFacing.getOpposite(), random.nextInt(maxHeight - backSideHeight - 2) + 2);
                    }
                } else {
                    cachedChunk.setBlockState(blockpos, UADBlocks.BIG_CACTUS_MAIN_BLOCK.get().defaultBlockState()
                            .setValue(BigCactusMainBlock.FACING, Direction.UP), false);
                }

                blockpos.move(Direction.UP);
            }
        }

        return true;
    }

    private void createBranch(WorldGenLevel level, BlockPos position, Direction branchDirection, int maxHeightUp) {
        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos().set(position).move(branchDirection);
        if (level.isEmptyBlock(blockpos)) {
            level.setBlock(blockpos, UADBlocks.BIG_CACTUS_MAIN_BLOCK.get().defaultBlockState()
                    .setValue(BigCactusMainBlock.FACING, branchDirection), 3);
        } else {
            return;
        }

        blockpos.move(branchDirection);
        if (level.isEmptyBlock(blockpos)) {
            level.setBlock(blockpos, UADBlocks.BIG_CACTUS_CORNER_BLOCK.get().defaultBlockState()
                    .setValue(BigCactusCornerBlock.FACING, branchDirection), 3);
        } else {
            return;
        }

        blockpos.move(Direction.UP);
        ChunkAccess cachedChunk = level.getChunk(blockpos);
        for (int currentHeight = 1; currentHeight < maxHeightUp && blockpos.getY() <= level.getMaxBuildHeight(); currentHeight++) {
            if (cachedChunk.getBlockState(blockpos).isAir()) {
                cachedChunk.setBlockState(blockpos, UADBlocks.BIG_CACTUS_MAIN_BLOCK.get().defaultBlockState()
                        .setValue(BigCactusMainBlock.FACING, Direction.UP), false);
            } else {
                return;
            }
            blockpos.move(Direction.UP);
        }
    }
}
