package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.utils.OpenSimplexNoise;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.PondConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class Pond extends Feature<PondConfig> {

    protected OpenSimplexNoise noiseGen;
    protected long seed;

    public void setSeed(long seed) {
        if (this.seed != seed || this.noiseGen == null) {
            this.noiseGen = new OpenSimplexNoise(seed);
            this.seed = seed;
        }
    }

    public Pond(Codec<PondConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<PondConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        PondConfig pondConfig = context.config();

        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos().set((position.getX() >> 4) << 4, position.getY(), (position.getZ() >> 4) << 4);
        blockpos.move(8, 0, 8);
        BlockPos centerPos = blockpos.immutable();
        ChunkAccess cachedChunk = level.getChunk(blockpos);

        for (int x = -7; x < 7; x++) {
            for (int z = -7; z < 7; z++) {
                for (int y = -3; y < 4; y++) {
                    double normX = x / 7d;
                    double normY = y / 4d;
                    double normZ = z / 7d;
                    if ((normX * normX) + (normY * normY) + (normZ * normZ) <= 0.60d) {
                        blockpos.set(centerPos).move(x, y, z);
                        BlockState blockState = cachedChunk.getBlockState(blockpos);

                        if (y >= 0 && !blockState.getFluidState().isEmpty()) {
                            return false;
                        } else if (!GeneralUtils.isFullCube(level, blockpos, blockState) && blockState != pondConfig.insideState) {
                            return false;
                        }
                    }
                }
            }
        }

        setSeed(level.getSeed());
        BlockState aboveState = null;
        for (int x = -8; x < 8; x++) {
            for (int z = -8; z < 8; z++) {
                for (int y = 4; y >= -4; y--) {
                    blockpos.set(centerPos).move(x, y, z);
                    double noiseVal = noiseGen.eval(blockpos.getX() * 0.21d, blockpos.getY() * 0.06d, blockpos.getZ() * 0.21d);
                    double normX = x / 8d;
                    double normY = y / 4d;
                    double normZ = z / 8d;
                    double lakeVal = (normX * normX) + (normY * normY) + (normZ * normZ) - ((noiseVal + 1) * 0.9d);

                    if (lakeVal < -0.065d) {
                        BlockState blockState1 = cachedChunk.getBlockState(blockpos);

                        if (y == 4) {
                            if (pondConfig.placeOutsideStateOften && GeneralUtils.isFullCube(level, blockpos, blockState1) && random.nextFloat() < 0.70f) {
                                aboveState = cachedChunk.getBlockState(blockpos.move(Direction.UP));
                                blockpos.move(Direction.DOWN);

                                if (aboveState.isAir()) {
                                    GeneralUtils.setChunkBlockState(cachedChunk, blockpos, pondConfig.topState);
                                } else {
                                    GeneralUtils.setChunkBlockState(cachedChunk, blockpos, pondConfig.outsideState);
                                }
                            }

                            aboveState = cachedChunk.getBlockState(blockpos);
                        }

                        if (GeneralUtils.isFullCube(level, blockpos, blockState1) || blockState1.is(BlockTags.ICE)) {
                            if (blockState1.hasBlockEntity()) {
                                continue;
                            }
                            if (x == -8 || z == -8 || x == 7 || z == 7 || lakeVal > -0.48d || y == -4) {
                                if (pondConfig.placeOutsideStateOften) {
                                    if (aboveState.isAir() || aboveState.is(Blocks.SNOW)) {
                                        GeneralUtils.setChunkBlockState(cachedChunk, blockpos, pondConfig.topState);
                                    } else {
                                        GeneralUtils.setChunkBlockState(cachedChunk, blockpos, pondConfig.outsideState);
                                    }
                                }
                            } else if (y <= 0) {
                                GeneralUtils.setChunkBlockState(cachedChunk, blockpos, pondConfig.insideState);

                                for (Direction direction : Direction.values()) {
                                    if (direction != Direction.UP) {
                                        BlockState blockState = cachedChunk.getBlockState(blockpos.move(direction));
                                        if (!blockState.hasBlockEntity()
                                                && !GeneralUtils.isFullCube(level, blockpos, blockState)
                                                && blockState != pondConfig.insideState) {
                                            GeneralUtils.setChunkBlockState(cachedChunk, blockpos, pondConfig.outsideState);
                                        }
                                        blockpos.move(direction.getOpposite());
                                    } else if (!pondConfig.insideState.getFluidState().isEmpty()) {
                                        BlockState blockState = cachedChunk.getBlockState(blockpos.move(direction));
                                        if (!blockState.hasBlockEntity()
                                                && !blockState.getFluidState().isEmpty()
                                                && blockState != pondConfig.insideState) {
                                            GeneralUtils.setChunkBlockState(cachedChunk, blockpos, pondConfig.outsideState);
                                        }
                                        blockpos.move(direction.getOpposite());
                                    }
                                }
                            } else {
                                if (!aboveState.getFluidState().isEmpty()) {
                                    GeneralUtils.setChunkBlockState(cachedChunk, blockpos, pondConfig.outsideState);
                                } else {
                                    GeneralUtils.setChunkBlockState(cachedChunk, blockpos, Blocks.CAVE_AIR.defaultBlockState());
                                }
                            }

                            BlockState plantCheckState = aboveState;
                            while (blockpos.getY() <= level.getMaxBuildHeight()
                                    && !GeneralUtils.isSolidBlock(plantCheckState)
                                    && !plantCheckState.canSurvive(level, blockpos)) {
                                GeneralUtils.setChunkBlockState(cachedChunk, blockpos, Blocks.AIR.defaultBlockState());
                                blockpos.move(Direction.UP);
                                plantCheckState = cachedChunk.getBlockState(blockpos);
                            }
                        }
                    } else {
                        aboveState = cachedChunk.getBlockState(blockpos);
                    }
                }
                aboveState = null;
            }
        }

        return true;
    }
}
