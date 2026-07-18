package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
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

public class SnowIceAllLayers extends Feature<NoneFeatureConfiguration> {
    public SnowIceAllLayers(Codec<NoneFeatureConfiguration> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        BlockPos pos = context.origin();

        Biome biome = world.getBiome(pos).value();
        BlockPos.MutableBlockPos blockposMutable1 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos blockposMutable2 = new BlockPos.MutableBlockPos();
        ChunkAccess cachedChunk = world.getChunk(pos);

        for (int y = world.getMaxBuildHeight(); y >= world.getSeaLevel(); --y) {
            blockposMutable1.set(pos.getX(), y, pos.getZ());
            blockposMutable2.set(blockposMutable1).move(Direction.DOWN);

            BlockState blockStateTop = cachedChunk.getBlockState(blockposMutable1);
            BlockState blockStateBottom = cachedChunk.getBlockState(blockposMutable2);
            if ((blockStateTop.isAir() || blockStateTop.is(Blocks.VINE)) && !blockStateBottom.isAir()) {

                if (!blockStateBottom.getFluidState().isEmpty() && biome.shouldFreeze(world, blockposMutable2, false)) {
                    cachedChunk.setBlockState(blockposMutable2, Blocks.ICE.defaultBlockState(), false);
                }

                if (SnowIceLayerHandlerFeature.doesSnowGenerate(world, biome, blockposMutable1)) {
                    int xMod = blockposMutable1.getX() & 0x000F;
                    int zMod = blockposMutable1.getZ() & 0x000F;
                    if (blockStateBottom.is(BlockTags.LEAVES) && (xMod == 0 || xMod == 15 || zMod == 0 || zMod == 15)) {
                        SnowIceLayerHandlerFeature.placeSnowOnNearbyLeaves(world, biome, blockposMutable1, cachedChunk);
                    }

                    cachedChunk.setBlockState(blockposMutable1, Blocks.SNOW.defaultBlockState(), false);
                    if (blockStateBottom.hasProperty(SnowyDirtBlock.SNOWY)) {
                        cachedChunk.setBlockState(blockposMutable2, blockStateBottom.setValue(SnowyDirtBlock.SNOWY, true), false);
                    }
                }
            }
        }
        return true;
    }
}
