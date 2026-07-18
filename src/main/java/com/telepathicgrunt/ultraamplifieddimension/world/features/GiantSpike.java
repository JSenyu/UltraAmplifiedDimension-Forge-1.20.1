package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.GiantSpikeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class GiantSpike extends Feature<GiantSpikeConfig> {

    public GiantSpike(Codec<GiantSpikeConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<GiantSpikeConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        GiantSpikeConfig config = context.config();

        BlockState startBlockState = level.getBlockState(position);
        if (!config.target.test(startBlockState, random)
                && !startBlockState.equals(config.aboveSeaState)
                && !startBlockState.equals(config.belowSeaState)) {
            return false;
        }

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos().set(position);
        mutableBlockPos.move(Direction.UP, random.nextInt(4));
        int headHeightOffset = random.nextInt(4) + 7;
        int tailWidthOffset = headHeightOffset / 4 + random.nextInt(2);
        int finalHeight;

        if (random.nextFloat() < config.giantSpikeChance) {
            int extraHeight = random.nextInt(config.giantSpikeMaxHeight - config.giantSpikeMinHeight) + config.giantSpikeMinHeight;

            if (mutableBlockPos.getY() + extraHeight > level.getMaxBuildHeight() - 10) {
                mutableBlockPos.move(Direction.UP, level.getMaxBuildHeight() - 10 - mutableBlockPos.getY());
            } else {
                mutableBlockPos.move(Direction.UP, extraHeight);
            }
        }

        finalHeight = mutableBlockPos.getY();
        for (int y = 0; y < headHeightOffset; ++y) {
            float maxWidth = (1.0F - (float) y / (float) headHeightOffset) * tailWidthOffset;
            int range = Mth.ceil(maxWidth);

            for (int x = -range; x <= range; ++x) {
                float xWidth = Mth.abs(x) - 0.25F;

                for (int z = -range; z <= range; ++z) {
                    float zWidth = Mth.abs(z) - 0.25F;

                    if ((x == 0 && z == 0 || (xWidth * xWidth) + (zWidth * zWidth) <= maxWidth * maxWidth)
                            && ((x != -range && x != range && z != -range && z != range) || random.nextFloat() <= 0.75F)) {
                        BlockPos topPos = mutableBlockPos.offset(x, y, z);
                        BlockPos bottomPos = mutableBlockPos.offset(x, -y, z);
                        BlockState currentBlockState = level.getBlockState(topPos);
                        if (config.target.test(currentBlockState, random) && topPos.getY() >= context.chunkGenerator().getSeaLevel() - 1) {
                            this.setBlock(level, topPos, config.aboveSeaState);
                        } else if (!currentBlockState.getFluidState().isEmpty()) {
                            this.setBlock(level, topPos, config.belowSeaState);
                        }

                        if (y != 0 && range > 1) {
                            currentBlockState = level.getBlockState(bottomPos);

                            if (config.target.test(currentBlockState, random) && bottomPos.getY() >= context.chunkGenerator().getSeaLevel() - 1) {
                                this.setBlock(level, bottomPos, config.aboveSeaState);
                            } else if (!currentBlockState.getFluidState().isEmpty()) {
                                this.setBlock(level, bottomPos, config.belowSeaState);
                            }
                        }
                    }
                }
            }
        }

        int maxWidth = 1;
        for (int x = -maxWidth; x <= maxWidth; ++x) {
            for (int z = -maxWidth; z <= maxWidth; ++z) {
                mutableBlockPos.set(position.getX() + x, finalHeight - 1, position.getZ() + z);
                int modeThreshold = Integer.MAX_VALUE;
                boolean placingMode = true;

                if (Math.abs(x) == maxWidth && Math.abs(z) == maxWidth) {
                    modeThreshold = random.nextInt(5);
                }

                while (mutableBlockPos.getY() > 5) {
                    BlockState currentBlockState = level.getBlockState(mutableBlockPos);
                    if (mutableBlockPos.getY() != finalHeight - 1 && !config.target.test(currentBlockState, random)) {
                        break;
                    }

                    if (placingMode) {
                        if (mutableBlockPos.getY() < context.chunkGenerator().getSeaLevel() - 1) {
                            this.setBlock(level, mutableBlockPos, config.belowSeaState);
                        } else {
                            this.setBlock(level, mutableBlockPos, config.aboveSeaState);
                        }
                    } else if (mutableBlockPos.getY() == context.chunkGenerator().getSeaLevel() - 1) {
                        this.setBlock(level, mutableBlockPos, config.aboveSeaState);
                    }

                    mutableBlockPos.move(Direction.DOWN);

                    if (modeThreshold <= 0) {
                        ++modeThreshold;

                        if (placingMode) {
                            placingMode = false;
                            modeThreshold = -(random.nextInt(6) - 1);
                        }
                    } else {
                        --modeThreshold;

                        if (!placingMode) {
                            placingMode = true;
                            modeThreshold = random.nextInt(5);
                        }
                    }
                }
            }
        }

        return true;
    }
}
