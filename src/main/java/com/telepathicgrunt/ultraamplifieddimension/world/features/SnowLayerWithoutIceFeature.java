package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SnowLayerWithoutIceFeature extends Feature<NoneFeatureConfiguration> {
    public SnowLayerWithoutIceFeature(Codec<NoneFeatureConfiguration> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        BlockPos position = context.origin();

        Biome biome = world.getBiome(position).value();
        BlockPos.MutableBlockPos blockposMutable1 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos blockposMutable2 = new BlockPos.MutableBlockPos();
        ChunkAccess cachedChunk = world.getChunk(position);

        int y = cachedChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, position.getX(), position.getZ()) + 1;
        blockposMutable1.set(position.getX(), y, position.getZ());
        blockposMutable2.set(blockposMutable1).move(Direction.DOWN);
        BlockState blockState1 = cachedChunk.getBlockState(blockposMutable1);

        if (blockState1.isAir()) {
            if (biome.shouldSnow(world, blockposMutable1)) {
                cachedChunk.setBlockState(blockposMutable1, Blocks.SNOW.defaultBlockState(), false);
                BlockState blockStateBottom = cachedChunk.getBlockState(blockposMutable2);

                int xMod = blockposMutable1.getX() & 0x000F;
                int zMod = blockposMutable1.getZ() & 0x000F;
                if (blockStateBottom.is(BlockTags.LEAVES) && (xMod == 0 || xMod == 15 || zMod == 0 || zMod == 15)) {
                    SnowIceLayerHandlerFeature.placeSnowOnNearbyLeaves(world, biome, blockposMutable1, cachedChunk);
                }

                if (blockStateBottom.hasProperty(SnowyDirtBlock.SNOWY)) {
                    cachedChunk.setBlockState(blockposMutable2, blockStateBottom.setValue(SnowyDirtBlock.SNOWY, true), false);
                }
            }
        }

        return true;
    }
}
