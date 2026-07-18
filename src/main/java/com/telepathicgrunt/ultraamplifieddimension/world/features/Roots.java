package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.OpenSimplexNoise;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.RootConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class Roots extends Feature<RootConfig> {
    public Roots(Codec<RootConfig> configFactory) {
        super(configFactory);
    }

    protected long seed;
    protected OpenSimplexNoise noiseGen;

    public void setSeed(long seed) {
        if (this.seed != seed || this.noiseGen == null) {
            this.noiseGen = new OpenSimplexNoise(seed);
            this.seed = seed;
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<RootConfig> context) {
        WorldGenLevel world = context.level();
        RandomSource rand = context.random();
        BlockPos position = context.origin();
        RootConfig blockConfig = context.config();

        setSeed(world.getSeed());

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(position);
        ChunkAccess cachedChunk = world.getChunk(blockposMutable);
        BlockState currentBlockState;
        int xOffset;
        int zOffset;
        int yOffset;

        int numOfRoots = 1 + (position.getY() - world.getSeaLevel()) / 50;
        int rootLength = 2 + (position.getY() - world.getSeaLevel()) / 22;

        for (int rootNum = 1; rootNum < numOfRoots + 1; rootNum++) {
            blockposMutable.set(position);
            if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                cachedChunk = world.getChunk(blockposMutable);
            }

            for (int length = 0; length < rootLength; length++) {
                currentBlockState = cachedChunk.getBlockState(blockposMutable);
                if (blockposMutable.getY() <= position.getY() &&
                        (blockConfig.rootReplaceTarget.test(currentBlockState, rand) ||
                                currentBlockState == blockConfig.rootBlock ||
                                currentBlockState == Blocks.VINE.defaultBlockState())) {
                    boolean isUnderLedge = false;
                    int upwardOffset = 1;
                    blockposMutable.move(Direction.UP);
                    for (; upwardOffset < 8; upwardOffset++) {
                        BlockState blockState = cachedChunk.getBlockState(blockposMutable);
                        if (blockConfig.validAboveState.test(blockState, rand)) {
                            isUnderLedge = true;
                            break;
                        }
                        blockposMutable.move(Direction.UP);
                    }

                    if (isUnderLedge) {
                        blockposMutable.move(Direction.DOWN, upwardOffset);

                        cachedChunk.setBlockState(blockposMutable, blockConfig.rootBlock, false);

                        if (rand.nextFloat() < 0.05F) {
                            generateTinyVine(world, cachedChunk, rand, blockposMutable);
                        }
                    }
                } else {
                    break;
                }

                xOffset = (int) Mth.clamp(this.noiseGen.eval(blockposMutable.getX() * 1D + 20000 * rootNum, blockposMutable.getZ() * 1D + 20000 * rootNum, blockposMutable.getY() * 0.20D + 20000 * rootNum) * 15.0D, -1, 1);
                zOffset = (int) Mth.clamp(this.noiseGen.eval(blockposMutable.getX() * 1D + 10000 * rootNum, blockposMutable.getZ() * 1D + 10000 * rootNum, blockposMutable.getY() * 0.20D + 10000 * rootNum) * 15.0D, -1, 1);
                yOffset = (int) Mth.clamp(this.noiseGen.eval(blockposMutable.getX() * 0.85D - 10000 * rootNum, blockposMutable.getZ() * 0.85D - 10000 * rootNum, blockposMutable.getY() * 0.5D - 10000) * 15.0D * rootNum - 5.0D, -1, 1);

                blockposMutable.move(xOffset, yOffset, zOffset);

                if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                    cachedChunk = world.getChunk(blockposMutable);
                }
            }
        }

        return true;
    }

    private void generateTinyVine(LevelAccessor world, ChunkAccess cachedChunkIn, RandomSource rand, BlockPos.MutableBlockPos originalPosition) {
        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(originalPosition);

        int length = 0;
        blockposMutable.move(Direction.Plane.HORIZONTAL.getRandomDirection(rand));

        ChunkAccess cachedChunk = cachedChunkIn;
        if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
            cachedChunk = world.getChunk(blockposMutable);
        }

        for (; blockposMutable.getY() > 1 && length < 5; blockposMutable.move(Direction.DOWN)) {
            if (cachedChunk.getBlockState(blockposMutable).isAir()) {

                BlockState aboveState = cachedChunk.getBlockState(blockposMutable.move(Direction.UP));
                blockposMutable.move(Direction.DOWN);

                if (aboveState.is(Blocks.VINE)) {
                    cachedChunk.setBlockState(blockposMutable, aboveState, false);
                    length++;
                    continue;
                }

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockState currentVineBlockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction), true);
                    if (currentVineBlockState.canSurvive(world, blockposMutable)) {
                        cachedChunk.setBlockState(blockposMutable, currentVineBlockState, false);
                        length++;
                        break;
                    }
                }
            } else {
                break;
            }
        }
    }
}
