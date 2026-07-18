package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.utils.OpenSimplexNoise;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.ColumnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ColumnVertical extends Feature<ColumnConfig> {
    protected OpenSimplexNoise noiseGen;
    protected long seed;

    public void setSeed(long seed) {
        if (this.seed != seed || this.noiseGen == null) {
            this.noiseGen = new OpenSimplexNoise(seed);
            this.seed = seed;
        }
    }

    public ColumnVertical(Codec<ColumnConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<ColumnConfig> context) {
        WorldGenLevel world = context.level();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        RandomSource rand = context.random();
        BlockPos position = context.origin();
        ColumnConfig columnConfig = context.config();

        setSeed(world.getSeed());
        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(position);
        int minWidth = 3;
        int maxWidth = 10;
        int ceilingHeight;
        int floorHeight;
        int heightDiff;
        ChunkAccess cachedChunk = world.getChunk(blockposMutable);

        while (!GeneralUtils.isFullCube(world, blockposMutable, cachedChunk.getBlockState(blockposMutable))) {
            if (blockposMutable.getY() > world.getMaxBuildHeight() - 1) {
                return false;
            }
            blockposMutable.move(Direction.UP, 2);
        }
        ceilingHeight = blockposMutable.getY();

        blockposMutable.set(position);
        while (!GeneralUtils.isFullCube(world, blockposMutable, cachedChunk.getBlockState(blockposMutable))) {
            if (blockposMutable.getY() < 3) {
                return false;
            }
            blockposMutable.move(Direction.DOWN, 2);
        }
        floorHeight = blockposMutable.getY();

        heightDiff = ceilingHeight - floorHeight;
        if (heightDiff > 100 || heightDiff < 10) {
            return false;
        }

        int thinnestWidth = (int) (maxWidth * ((heightDiff) / 100F));
        if (thinnestWidth < minWidth) {
            thinnestWidth = minWidth;
        }

        int widthAtHeight;
        int currentWidth;
        widthAtHeight = getWidthAtHeight(0, heightDiff, thinnestWidth);

        for (int x = position.getX() - widthAtHeight; x <= position.getX() + widthAtHeight; x += 3) {
            for (int z = position.getZ() - widthAtHeight; z <= position.getZ() + widthAtHeight; z += 3) {
                int xDiff = x - position.getX();
                int zDiff = z - position.getZ();
                if (xDiff * xDiff + zDiff * zDiff <= (widthAtHeight) * (widthAtHeight)) {

                    if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                        cachedChunk = world.getChunk(blockposMutable);
                    }

                    BlockState block1 = cachedChunk.getBlockState(blockposMutable.set(x, ceilingHeight + 3, z));
                    BlockState block2 = cachedChunk.getBlockState(blockposMutable.set(x, floorHeight - 2, z));

                    if (!GeneralUtils.isFullCube(world, blockposMutable, block1) ||
                            !GeneralUtils.isFullCube(world, blockposMutable, block2)) {
                        return false;
                    }
                }
            }
        }

        int xMod;
        int zMod;
        boolean flagImperfection1 = rand.nextBoolean();
        boolean flagImperfection2 = rand.nextBoolean();

        if (flagImperfection1 && flagImperfection2) {
            xMod = heightDiff / 20 + 1;
            zMod = heightDiff / 20 + 1;
        } else if (flagImperfection1) {
            xMod = heightDiff / 20 + 1;
            zMod = 0;
        } else if (flagImperfection2) {
            xMod = 0;
            zMod = heightDiff / 20 + 1;
        } else {
            xMod = 0;
            zMod = 0;
        }

        for (int y = -2; y <= heightDiff + 2; y++) {
            widthAtHeight = getWidthAtHeight(y, heightDiff, thinnestWidth);

            for (int x = position.getX() - widthAtHeight - xMod - 1; x <= position.getX() + widthAtHeight + xMod + 1; ++x) {
                for (int z = position.getZ() - widthAtHeight - zMod - 1; z <= position.getZ() + widthAtHeight + zMod + 1; ++z) {
                    int xDiff = x - position.getX();
                    int zDiff = z - position.getZ();
                    blockposMutable.set(x, y + floorHeight, z);

                    boolean flagImperfection3 = this.noiseGen.eval(x * 0.06D, z * 0.6D, y * 0.02D) < 0;
                    if (flagImperfection3 && (widthAtHeight > thinnestWidth || (widthAtHeight == thinnestWidth && rand.nextInt(4) == 0))) {
                        currentWidth = widthAtHeight - 1;
                    } else {
                        currentWidth = widthAtHeight;
                    }

                    int xzDiffSquaredStretched = (xMod + 1) * (xDiff * xDiff) + (zMod + 1) * (zDiff * zDiff);
                    int xzDiffSquared = (xDiff * xDiff) + (zDiff * zDiff);
                    if (xzDiffSquaredStretched <= (currentWidth - 1) * (currentWidth - 1)) {

                        if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                            cachedChunk = world.getChunk(blockposMutable);
                        }

                        BlockState currentState = cachedChunk.getBlockState(blockposMutable);
                        if (currentState.isAir() || !GeneralUtils.isFullCube(world, blockposMutable, currentState)) {
                            cachedChunk.setBlockState(blockposMutable, columnConfig.insideBlock, false);
                        }
                    } else if (y < heightDiff / 2 && xzDiffSquared <= (widthAtHeight + 2) * (widthAtHeight + 2)) {
                        for (int downward = 0; downward < 6 && y - downward >= -3; downward++) {

                            if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                                cachedChunk = world.getChunk(blockposMutable);
                            }

                            if (cachedChunk.getBlockState(blockposMutable) == columnConfig.insideBlock) {
                                if (downward == 1 && blockposMutable.getY() >= world.getSeaLevel() - 1) {
                                    if (!columnConfig.snowy) {
                                        cachedChunk.setBlockState(blockposMutable, columnConfig.topBlock, false);
                                    } else {
                                        cachedChunk.setBlockState(blockposMutable.move(Direction.UP), Blocks.SNOW.defaultBlockState(), false);
                                        blockposMutable.move(Direction.DOWN);

                                        if (columnConfig.topBlock.hasProperty(SnowyDirtBlock.SNOWY)) {
                                            cachedChunk.setBlockState(blockposMutable, columnConfig.topBlock.setValue(SnowyDirtBlock.SNOWY, true), false);
                                        } else {
                                            cachedChunk.setBlockState(blockposMutable, columnConfig.topBlock, false);
                                        }
                                    }
                                } else {
                                    cachedChunk.setBlockState(blockposMutable, columnConfig.middleBlock, false);
                                }
                            }

                            blockposMutable.move(Direction.DOWN);
                        }
                    }
                }
            }
        }

        return true;
    }

    private int getWidthAtHeight(int y, int heightDiff, int thinnestWidth) {
        if (heightDiff > 80) {
            float yFromCenter = Math.abs(y - heightDiff / 2F) - 2;
            return thinnestWidth + (int) ((yFromCenter / 4F) * (yFromCenter / 4F) / 10);
        } else if (heightDiff > 60) {
            float yFromCenter = Math.abs(y - heightDiff / 2F) - 1;
            return thinnestWidth + (int) ((yFromCenter / 3F) * (yFromCenter / 3F) / 9);
        } else if (heightDiff > 30) {
            float yFromCenter = Math.abs(y - heightDiff / 2F);
            return thinnestWidth + (int) ((yFromCenter / 2.6F) * (yFromCenter / 2.6F) / 6);
        } else if (heightDiff > 18) {
            float yFromCenter = Math.abs(y - heightDiff / 2F) + 1;
            return thinnestWidth + (int) ((yFromCenter / 2.8F) * (yFromCenter / 2.8F) / 3);
        } else {
            float yFromCenter = Math.abs(y - heightDiff / 2F) + 3;
            return thinnestWidth + (int) ((yFromCenter / 2.7f) * (yFromCenter / 2.7f) / 3);
        }
    }
}
