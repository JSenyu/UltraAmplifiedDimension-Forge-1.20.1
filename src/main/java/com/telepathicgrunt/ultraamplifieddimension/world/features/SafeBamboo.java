package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.BambooConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class SafeBamboo extends Feature<BambooConfig> {

    private static final BlockState BAMBOO_DEFAULT = Blocks.BAMBOO.defaultBlockState()
            .setValue(BambooStalkBlock.AGE, 1)
            .setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE)
            .setValue(BambooStalkBlock.STAGE, 0);
    private static final BlockState BAMBOO_LEAVES_LARGE_TOP = BAMBOO_DEFAULT.setValue(BambooStalkBlock.LEAVES, BambooLeaves.LARGE).setValue(BambooStalkBlock.STAGE, 1);
    private static final BlockState BAMBOO_LEAVES_LARGE = BAMBOO_DEFAULT.setValue(BambooStalkBlock.LEAVES, BambooLeaves.LARGE);
    private static final BlockState BAMBOO_LEAVES_SMALL = BAMBOO_DEFAULT.setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL);

    public SafeBamboo(Codec<BambooConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<BambooConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        BambooConfig bambooConfig = context.config();

        int i = 0;
        int maxHeight = random.nextInt((bambooConfig.maxHeight + 1) - bambooConfig.minHeight) + bambooConfig.minHeight;
        int podzolRange = random.nextInt((bambooConfig.podzolMaxRadius + 1) - bambooConfig.podzolMinRadius) + bambooConfig.podzolMinRadius;
        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(position);
        ChunkAccess cachedChunk = level.getChunk(position);

        if (cachedChunk.getBlockState(blockposMutable).isAir()) {
            if (Blocks.BAMBOO.defaultBlockState().canSurvive(level, blockposMutable)) {
                for (int x = position.getX() - podzolRange; x <= position.getX() + podzolRange; ++x) {
                    for (int z = position.getZ() - podzolRange; z <= position.getZ() + podzolRange; ++z) {
                        for (int y = position.getY() - 2; y <= position.getY() + 2; ++y) {
                            int xDiff = x - position.getX();
                            int zDiff = z - position.getZ();
                            if (xDiff * xDiff + zDiff * zDiff <= podzolRange * podzolRange) {
                                blockposMutable.set(x, y, z);
                                if (random.nextFloat() < 0.4F && level.getBlockState(blockposMutable).is(Blocks.GRASS_BLOCK)) {
                                    level.setBlock(blockposMutable, Blocks.PODZOL.defaultBlockState(), 3);
                                }
                            }
                        }
                    }
                }

                blockposMutable.set(position);
                for (int height = 0; height < maxHeight && height <= level.getMaxBuildHeight() && cachedChunk.getBlockState(blockposMutable).isAir(); ++height) {
                    cachedChunk.setBlockState(blockposMutable, BAMBOO_DEFAULT, false);
                    blockposMutable.move(Direction.UP, 1);
                }

                if (cachedChunk.getBlockState(blockposMutable.move(Direction.DOWN)).is(Blocks.BAMBOO)) {
                    cachedChunk.setBlockState(blockposMutable, BAMBOO_LEAVES_LARGE_TOP, false);
                }
                if (cachedChunk.getBlockState(blockposMutable.move(Direction.DOWN)).is(Blocks.BAMBOO)) {
                    cachedChunk.setBlockState(blockposMutable, BAMBOO_LEAVES_LARGE, false);
                }
                if (cachedChunk.getBlockState(blockposMutable.move(Direction.DOWN)).is(Blocks.BAMBOO)) {
                    cachedChunk.setBlockState(blockposMutable, BAMBOO_LEAVES_SMALL, false);
                }
            }
            ++i;
        }
        return i > 0;
    }
}
