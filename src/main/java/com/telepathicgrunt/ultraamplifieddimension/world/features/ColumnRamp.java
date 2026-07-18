package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADBlocks;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.ColumnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.Set;

public class ColumnRamp extends Feature<ColumnConfig> {
    public final Set<Block> irreplacableBlocks;

    public ColumnRamp(Codec<ColumnConfig> configFactory) {
        super(configFactory);

        irreplacableBlocks = ImmutableSet.of(Blocks.BEEHIVE, Blocks.AIR, Blocks.CAVE_AIR, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK, Blocks.MUSHROOM_STEM, Blocks.CACTUS, UADBlocks.BIG_CACTUS_BODY_BLOCK.get(), UADBlocks.BIG_CACTUS_CORNER_BLOCK.get(), UADBlocks.BIG_CACTUS_MAIN_BLOCK.get());
    }

    @Override
    public boolean place(FeaturePlaceContext<ColumnConfig> context) {
        WorldGenLevel world = context.level();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        RandomSource rand = context.random();
        BlockPos position = context.origin();
        ColumnConfig columnConfig = context.config();

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(position);
        int minWidth = 4;
        int ceilingHeight;
        int bottomFloorHeight;
        int topFloorHeight;
        int heightDiff;
        ChunkAccess cachedChunk = world.getChunk(blockposMutable);

        while (!GeneralUtils.isFullCube(world, blockposMutable, cachedChunk.getBlockState(blockposMutable))) {
            if (blockposMutable.getY() > world.getMaxBuildHeight() - 1) {
                return false;
            }
            blockposMutable.move(Direction.UP, 2);
        }
        ceilingHeight = blockposMutable.getY();

        while (GeneralUtils.isFullCube(world, blockposMutable, cachedChunk.getBlockState(blockposMutable))) {
            if (blockposMutable.getY() > world.getMaxBuildHeight() - 1) {
                return false;
            }
            blockposMutable.move(Direction.UP);
        }
        topFloorHeight = blockposMutable.getY();

        int ledgeThickness = topFloorHeight - ceilingHeight;
        if (ledgeThickness > 7 || ledgeThickness < 2) {
            return false;
        }

        blockposMutable.set(position);
        while (!GeneralUtils.isFullCube(world, blockposMutable, cachedChunk.getBlockState(blockposMutable))) {
            if (blockposMutable.getY() < 70) {
                return false;
            }
            blockposMutable.move(Direction.DOWN, 2);
        }
        bottomFloorHeight = blockposMutable.getY();

        heightDiff = ceilingHeight - bottomFloorHeight;
        if (heightDiff > 27 || heightDiff < 8) {
            return false;
        }

        float randFloat = rand.nextFloat();
        float xTurningValue = (float) Math.sin(randFloat * Math.PI * 2);
        float zTurningValue = (float) Math.cos(randFloat * Math.PI * 2);

        int widthAtHeight = getWidthAtHeight(0, heightDiff + 5, minWidth);

        int xPosCeiling = position.getX() + getOffsetAtHeight(heightDiff + 1, heightDiff, xTurningValue);
        int zPosCeiling = position.getZ() + getOffsetAtHeight(0, heightDiff, zTurningValue);
        int xPosFloor = position.getX() - getOffsetAtHeight(heightDiff - 1, heightDiff, xTurningValue);
        int zPosFloor = position.getZ() + getOffsetAtHeight(0, heightDiff, zTurningValue);

        for (int x = -widthAtHeight; x <= widthAtHeight; x++) {
            for (int z = -widthAtHeight; z <= widthAtHeight; z++) {
                if (x * x + z * z > widthAtHeight * widthAtHeight * 0.85 && x * x + z * z < widthAtHeight * widthAtHeight) {

                    if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                        cachedChunk = world.getChunk(blockposMutable);
                    }

                    BlockState block1 = cachedChunk.getBlockState(blockposMutable.set(xPosCeiling + x, ceilingHeight + 2, zPosCeiling + z));
                    BlockState block2 = cachedChunk.getBlockState(blockposMutable.set(xPosFloor + x, bottomFloorHeight - 2, zPosFloor + z));

                    if (!GeneralUtils.isFullCube(world, blockposMutable, block1) || !GeneralUtils.isFullCube(world, blockposMutable, block2)) {
                        return false;
                    }
                }
            }
        }

        int xOffset;
        int zOffset;
        int xDiff;
        int zDiff;
        BlockPos.MutableBlockPos tempMutable = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos tempPos2 = new BlockPos.MutableBlockPos();

        for (int y = -2; y <= heightDiff + 3; y++) {
            widthAtHeight = getWidthAtHeight(y, heightDiff + 2, minWidth);

            if (heightDiff < 16) {
                xOffset = (int) (getOffsetAtHeight(y, heightDiff, xTurningValue) - Math.signum(getOffsetAtHeight(y, heightDiff, xTurningValue) / 2f) * 2);
                zOffset = (int) (getOffsetAtHeight(y, heightDiff, zTurningValue) - Math.signum(getOffsetAtHeight(y, heightDiff, zTurningValue) / 2f) * 2);
            } else if (heightDiff < 21) {
                xOffset = (int) (getOffsetAtHeight(y, heightDiff, xTurningValue) - Math.signum(getOffsetAtHeight(y, heightDiff, xTurningValue) / 3f) * 4);
                zOffset = (int) (getOffsetAtHeight(y, heightDiff, zTurningValue) - Math.signum(getOffsetAtHeight(y, heightDiff, zTurningValue) / 3f) * 4);
            } else {
                xOffset = (int) (getOffsetAtHeight(y, heightDiff, xTurningValue) - Math.signum(getOffsetAtHeight(y, heightDiff, xTurningValue) / 3f) * 6);
                zOffset = (int) (getOffsetAtHeight(y, heightDiff, zTurningValue) - Math.signum(getOffsetAtHeight(y, heightDiff, zTurningValue) / 3f) * 6);
            }

            for (int x = position.getX() - widthAtHeight - 1; x <= position.getX() + widthAtHeight + 1; ++x) {
                for (int z = position.getZ() - widthAtHeight - 1; z <= position.getZ() + widthAtHeight + 1; ++z) {
                    xDiff = x - position.getX();
                    zDiff = z - position.getZ();
                    blockposMutable.set(x + xOffset, y + bottomFloorHeight + 3, z + zOffset);

                    int xzDiffSquaredStretched = (xDiff * xDiff) + (zDiff * zDiff);
                    int circleBounds = (int) ((widthAtHeight - 1) * (widthAtHeight - 1) - 0.5F);

                    if (y > heightDiff) {
                        circleBounds *= (0.6f / (y - heightDiff));
                    }

                    if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                        cachedChunk = world.getChunk(blockposMutable);
                    }

                    BlockState block = cachedChunk.getBlockState(blockposMutable);
                    if (!block.is(BlockTags.LEAVES) && !block.is(BlockTags.LOGS) && !irreplacableBlocks.contains(block.getBlock()) && xzDiffSquaredStretched <= circleBounds) {
                        if (blockposMutable.getY() < world.getSeaLevel()) {
                            cachedChunk.setBlockState(blockposMutable, Blocks.WATER.defaultBlockState(), false);
                        } else {

                            tempMutable.set(blockposMutable).move(Direction.DOWN);
                            if (columnConfig.snowy && Blocks.SNOW.defaultBlockState().canSurvive(world, blockposMutable)) {
                                cachedChunk.setBlockState(blockposMutable, Blocks.SNOW.defaultBlockState(), false);

                                BlockState belowBlock = cachedChunk.getBlockState(tempMutable);
                                if (belowBlock.hasProperty(SnowyDirtBlock.SNOWY)) {
                                    cachedChunk.setBlockState(tempMutable, belowBlock.setValue(SnowyDirtBlock.SNOWY, true), false);
                                }
                            } else {
                                BlockState aboveBlock = cachedChunk.getBlockState(blockposMutable.move(Direction.UP));
                                blockposMutable.move(Direction.DOWN);
                                if (!aboveBlock.is(BlockTags.LOGS)) {
                                    cachedChunk.setBlockState(blockposMutable, Blocks.AIR.defaultBlockState(), false);
                                }
                            }
                        }

                        tempMutable.set(blockposMutable).move(Direction.UP);
                        block = cachedChunk.getBlockState(tempMutable);
                        while (tempMutable.getY() < world.getMaxBuildHeight() && !block.canSurvive(world, tempMutable)) {
                            cachedChunk.setBlockState(tempMutable, Blocks.AIR.defaultBlockState(), false);
                            block = cachedChunk.getBlockState(tempMutable.move(Direction.UP));
                        }

                        BlockState blockBelowAir = cachedChunk.getBlockState(blockposMutable.move(Direction.DOWN));
                        BlockState blockBelowBelowAir = cachedChunk.getBlockState(blockposMutable.move(Direction.DOWN));
                        blockposMutable.move(Direction.UP);

                        if (GeneralUtils.isFullCube(world, blockposMutable, blockBelowAir)) {
                            if ((columnConfig.topBlock.getBlock() instanceof FallingBlock && blockBelowBelowAir.isAir()) || blockposMutable.getY() < world.getSeaLevel()) {
                                cachedChunk.setBlockState(blockposMutable, columnConfig.middleBlock, false);
                            } else {
                                cachedChunk.setBlockState(blockposMutable, columnConfig.topBlock, false);
                                tempMutable.set(blockposMutable).move(Direction.UP);
                                BlockState aboveBlock = cachedChunk.getBlockState(tempMutable);

                                if (columnConfig.snowy && aboveBlock.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(world, tempMutable)) {
                                    cachedChunk.setBlockState(tempMutable, Blocks.SNOW.defaultBlockState(), false);

                                    if (columnConfig.topBlock.hasProperty(SnowyDirtBlock.SNOWY)) {
                                        cachedChunk.setBlockState(blockposMutable, columnConfig.topBlock.setValue(SnowyDirtBlock.SNOWY, true), false);
                                    }
                                }
                            }
                        }

                        blockposMutable.move(Direction.UP);
                    }
                }
            }
        }

        for (int y = -2; y <= heightDiff + 4; y++) {
            widthAtHeight = getWidthAtHeight(y, heightDiff + 5, minWidth);
            xOffset = getOffsetAtHeight(y, heightDiff, xTurningValue);
            zOffset = getOffsetAtHeight(y, heightDiff, zTurningValue);

            for (int x = position.getX() - widthAtHeight - 1; x <= position.getX() + widthAtHeight + 1; ++x) {
                for (int z = position.getZ() - widthAtHeight - 1; z <= position.getZ() + widthAtHeight + 1; ++z) {
                    xDiff = x - position.getX();
                    zDiff = z - position.getZ();
                    blockposMutable.set(x + xOffset, y + bottomFloorHeight, z + zOffset);

                    int xzDiffSquaredStretched = (xDiff * xDiff) + (zDiff * zDiff);
                    int circleBounds = (int) ((widthAtHeight - 1) * (widthAtHeight - 1) - 0.5F);

                    if (y > heightDiff - 3) {
                        circleBounds *= (0.8f / (y - (heightDiff - 3)));
                    }

                    if (y <= heightDiff && xzDiffSquaredStretched <= circleBounds) {
                        if (!GeneralUtils.isFullCube(world, blockposMutable, world.getBlockState(blockposMutable))) {
                            world.setBlock(blockposMutable, columnConfig.insideBlock, 2);
                        }
                    } else if (y > heightDiff || xzDiffSquaredStretched <= (widthAtHeight + 3) * (widthAtHeight + 3)) {
                        for (int downward = 0; downward < 6 && y - downward >= -3; downward++) {
                            tempMutable.set(blockposMutable).move(Direction.DOWN, downward);
                            BlockState block = world.getBlockState(tempMutable);
                            BlockState blockBelow = world.getBlockState(tempMutable.move(Direction.DOWN));
                            tempMutable.move(Direction.UP);

                            if (block == columnConfig.insideBlock) {
                                if (tempMutable.getY() >= world.getSeaLevel() - 1 && downward == 1 && !(columnConfig.topBlock.getBlock() instanceof FallingBlock && blockBelow.isAir())) {
                                    world.setBlock(tempMutable, columnConfig.topBlock, 2);

                                    tempPos2.set(tempMutable).move(Direction.UP);
                                    BlockState aboveBlock = world.getBlockState(tempPos2);

                                    if (columnConfig.snowy && aboveBlock.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(world, tempPos2)) {
                                        world.setBlock(tempPos2, Blocks.SNOW.defaultBlockState(), 2);

                                        if (columnConfig.topBlock.hasProperty(SnowyDirtBlock.SNOWY)) {
                                            world.setBlock(tempMutable, columnConfig.topBlock.setValue(SnowyDirtBlock.SNOWY, true), 2);
                                        }
                                    }
                                } else {
                                    world.setBlock(tempMutable, columnConfig.middleBlock, 2);
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private int getWidthAtHeight(int y, int heightDiff, int thinnestWidth) {
        float yFromCenter = y - heightDiff * 0.5F;
        yFromCenter = Math.abs(yFromCenter * 0.4F) + 3;

        return thinnestWidth + (int) ((yFromCenter * yFromCenter) / 8);
    }

    private int getOffsetAtHeight(int y, int heightDiff, float turningValue) {
        float yFromCenter = y - heightDiff / 2F;
        return (int) (turningValue * yFromCenter);
    }
}
