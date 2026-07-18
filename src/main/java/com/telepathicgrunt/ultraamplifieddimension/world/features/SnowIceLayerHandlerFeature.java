package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADFeatures;
import com.telepathicgrunt.ultraamplifieddimension.utils.BiomeSetsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Optional;

public class SnowIceLayerHandlerFeature extends Feature<NoneFeatureConfiguration> {
    public SnowIceLayerHandlerFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        ChunkGenerator generator = context.chunkGenerator();
        RandomSource random = context.random();
        BlockPos position = context.origin();
        NoneFeatureConfiguration config = context.config();

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos();

        for (int xOffset = 0; xOffset < 16; xOffset++) {
            for (int zOffset = 0; zOffset < 16; zOffset++) {
                blockposMutable.set(position).move(xOffset, 0, zOffset);
                Biome biome = world.getBiome(blockposMutable).value();
                if (BiomeSetsHelper.FROZEN_BIOMES.contains(biome)) {
                    UADFeatures.SNOW_ICE_ALL_LAYERS.get().place(new FeaturePlaceContext<>(Optional.empty(), world, generator, random, blockposMutable, NoneFeatureConfiguration.INSTANCE));
                } else if (BiomeSetsHelper.COLD_OCEAN_BIOMES.contains(biome)) {
                    UADFeatures.SNOW_LAYER_WITHOUT_ICE.get().place(new FeaturePlaceContext<>(Optional.empty(), world, generator, random, blockposMutable, NoneFeatureConfiguration.INSTANCE));
                } else {
                    UADFeatures.SNOW_ICE_TOP_LAYER.get().place(new FeaturePlaceContext<>(Optional.empty(), world, generator, random, blockposMutable, NoneFeatureConfiguration.INSTANCE));
                }
            }
        }

        return true;
    }

    public static void placeSnowOnNearbyLeaves(WorldGenLevel world, Biome biome, BlockPos.MutableBlockPos blockposMutable1, ChunkAccess cachedChunk) {
        BlockPos.MutableBlockPos nearbyPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos nearbyPosBelow = new BlockPos.MutableBlockPos();
        ChunkAccess chunk = cachedChunk;
        int range = 5;
        for (int xOffset = -range; xOffset <= range; xOffset++) {
            for (int zOffset = -range; zOffset <= range; zOffset++) {
                nearbyPos.set(blockposMutable1).move(xOffset, 0, zOffset);
                nearbyPosBelow.set(nearbyPos).move(Direction.DOWN);

                if (nearbyPos.getX() >> 4 != cachedChunk.getPos().x || nearbyPos.getZ() >> 4 != cachedChunk.getPos().z) {
                    if (nearbyPos.getX() >> 4 != chunk.getPos().x || nearbyPos.getZ() >> 4 != chunk.getPos().z) {
                        chunk = world.getChunk(nearbyPos);
                    }

                    BlockState nearbyBlockStateTop = chunk.getBlockState(nearbyPos);
                    BlockState nearbyBlockStateBottom = chunk.getBlockState(nearbyPosBelow);

                    if ((nearbyBlockStateTop.isAir() || nearbyBlockStateTop.is(Blocks.VINE)) &&
                            doesSnowGenerate(world, biome, nearbyPos) &&
                            nearbyBlockStateBottom.is(BlockTags.LEAVES)) {
                        chunk.setBlockState(nearbyPos, Blocks.SNOW.defaultBlockState(), false);

                        if (nearbyBlockStateBottom.hasProperty(SnowyDirtBlock.SNOWY)) {
                            chunk.setBlockState(nearbyPosBelow, nearbyBlockStateBottom.setValue(SnowyDirtBlock.SNOWY, true), false);
                        }
                    }
                }
            }
        }
    }

    public static boolean doesSnowGenerate(LevelReader worldIn, Biome biome, BlockPos pos) {
        if (biome.getBaseTemperature() < 0.15F) {
            if (pos.getY() >= worldIn.getMinBuildHeight() && pos.getY() < worldIn.getMaxBuildHeight()) {
                return Blocks.SNOW.defaultBlockState().canSurvive(worldIn, pos);
            }
        }
        return false;
    }
}
