package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ContainLiquidForOceans extends Feature<NoneFeatureConfiguration> {
    public ContainLiquidForOceans(Codec<NoneFeatureConfiguration> configFactory) {
        super(configFactory);
    }

    private static final BlockState ICE = Blocks.ICE.defaultBlockState();
    private static final BlockState SNOW = Blocks.SNOW.defaultBlockState();

    private static final BlockState[] DEAD_CORAL_ARRAY = {
            Blocks.DEAD_HORN_CORAL_BLOCK.defaultBlockState(),
            Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState(),
            Blocks.DEAD_BUBBLE_CORAL_BLOCK.defaultBlockState(),
            Blocks.DEAD_FIRE_CORAL_BLOCK.defaultBlockState(),
            Blocks.DEAD_TUBE_CORAL_BLOCK.defaultBlockState()
    };

    private record OceanSurfaceBlocks(BlockState top, BlockState under, BlockState underWater, boolean useCoralTop, boolean useCoralBottom) {
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos position = context.origin();

        Biome oceanBiome = getOceanInChunk(world, position);

        if (oceanBiome == null) {
            return false;
        }

        int sealevel = world.getSeaLevel();
        boolean containedFlag;
        BlockState currentblock;
        BlockState blockAbove;
        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos(position.getX(), 0, position.getZ());
        BlockPos.MutableBlockPos blockposMutableAbove = new BlockPos.MutableBlockPos().set(blockposMutable);
        ChunkAccess chunk = world.getChunk(position.getX() >> 4, position.getZ() >> 4);

        OceanSurfaceBlocks surfaceBlocks = getOceanSurfaceBlocks(oceanBiome, chunk.getBlockState(blockposMutable.set(position.getX(), sealevel - 1, position.getZ())));
        BlockState oceanTopBlock = surfaceBlocks.top();
        BlockState oceanUnderBlock = surfaceBlocks.under();
        boolean useCoralTop = surfaceBlocks.useCoralTop();
        boolean useCoralBottom = surfaceBlocks.useCoralBottom();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                blockposMutable.set(position.getX() + x, 256, position.getZ() + z);
                for (; blockposMutable.getY() >= sealevel; blockposMutable.move(Direction.DOWN)) {

                    currentblock = chunk.getBlockState(blockposMutable);

                    while (currentblock.getFluidState().isEmpty() && blockposMutable.getY() >= sealevel) {
                        blockposMutable.move(Direction.DOWN);
                        currentblock = chunk.getBlockState(blockposMutable);
                    }

                    if (blockposMutable.getY() < sealevel) {
                        break;
                    }

                    containedFlag = true;
                    for (Direction face : Direction.Plane.HORIZONTAL) {
                        blockposMutable.move(face);
                        if (blockposMutable.getX() >> 4 != chunk.getPos().x || blockposMutable.getZ() >> 4 != chunk.getPos().z) {
                            chunk = world.getChunk(blockposMutable);
                        }

                        currentblock = chunk.getBlockState(blockposMutable);

                        if ((!currentblock.canOcclude() && currentblock.getFluidState().isEmpty() && !currentblock.is(Blocks.ICE)) ||
                                currentblock.is(Blocks.SNOW)) {
                            containedFlag = false;
                            blockposMutable.move(face.getOpposite());
                            break;
                        }

                        blockposMutable.move(face.getOpposite());
                    }

                    blockposMutableAbove.set(blockposMutable).move(Direction.UP);
                    if (blockposMutable.getX() >> 4 != chunk.getPos().x || blockposMutable.getZ() >> 4 != chunk.getPos().z) {
                        chunk = world.getChunk(blockposMutable);
                    }

                    if (containedFlag) {
                        blockAbove = chunk.getBlockState(blockposMutableAbove);

                        if (blockAbove == oceanUnderBlock) {
                            if (useCoralBottom) {
                                chunk.setBlockState(blockposMutableAbove, DEAD_CORAL_ARRAY[random.nextInt(DEAD_CORAL_ARRAY.length)], false);
                            } else {
                                chunk.setBlockState(blockposMutableAbove, surfaceBlocks.underWater(), false);
                            }
                        }
                    } else {
                        if (blockposMutable.getY() < 256) {
                            blockAbove = chunk.getBlockState(blockposMutableAbove);

                            if (blockAbove.canOcclude() || !blockAbove.getFluidState().isEmpty()) {
                                chunk.setBlockState(blockposMutable, oceanUnderBlock, false);
                            } else {
                                if (useCoralTop) {
                                    chunk.setBlockState(blockposMutable, DEAD_CORAL_ARRAY[random.nextInt(DEAD_CORAL_ARRAY.length)], false);
                                } else {
                                    chunk.setBlockState(blockposMutable, oceanTopBlock, false);
                                }
                            }
                        } else if (useCoralTop) {
                            chunk.setBlockState(blockposMutable, DEAD_CORAL_ARRAY[random.nextInt(DEAD_CORAL_ARRAY.length)], false);
                        } else {
                            chunk.setBlockState(blockposMutable, oceanTopBlock, false);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Approximates old SurfaceBuilderConfig top/under/underwater blocks using biome temperature
     * and the solid block below the water column when available.
     */
    private OceanSurfaceBlocks getOceanSurfaceBlocks(Biome biome, BlockState blockBelow) {
        boolean useCoral = biome.getBaseTemperature() > 0.5F;

        BlockState top = Blocks.SAND.defaultBlockState();
        BlockState under = Blocks.STONE.defaultBlockState();
        BlockState underWater = Blocks.SAND.defaultBlockState();

        if (biome.getBaseTemperature() <= 0.15F || GeneralUtils.biomeHasTag(biome, BiomeTags.IS_TAIGA)) {
            top = Blocks.GRAVEL.defaultBlockState();
            underWater = Blocks.GRAVEL.defaultBlockState();
        }

        if (blockBelow.canOcclude() && blockBelow.getFluidState().isEmpty()) {
            if (blockBelow.is(Blocks.SAND) || blockBelow.is(Blocks.RED_SAND) || blockBelow.is(Blocks.GRAVEL) ||
                    blockBelow.is(Blocks.STONE) || blockBelow.is(Blocks.DIRT) || blockBelow.is(Blocks.GRASS_BLOCK)) {
                under = blockBelow;
            }
        }

        if (useCoral) {
            return new OceanSurfaceBlocks(DEAD_CORAL_ARRAY[0], under, underWater, true, true);
        }

        return new OceanSurfaceBlocks(top, under, underWater, false, false);
    }

    private Biome getOceanInChunk(LevelAccessor world, BlockPos originalPosition) {
        Biome biomeAtLocation;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                if ((x != 0 && x != 15) && (z != 0 && z != 15)) {
                    continue;
                }

                mutableBlockPos.set(originalPosition.getX() + x, 0, originalPosition.getZ() + z);
                biomeAtLocation = world.getBiome(mutableBlockPos).value();
                if (GeneralUtils.biomeHasTag(biomeAtLocation, BiomeTags.IS_OCEAN)) {
                    return biomeAtLocation;
                }
            }
        }

        return null;
    }
}
