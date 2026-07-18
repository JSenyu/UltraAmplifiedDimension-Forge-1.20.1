package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ContainUndergroundLiquids extends Feature<NoneFeatureConfiguration> {
    public ContainUndergroundLiquids(Codec<NoneFeatureConfiguration> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        BlockPos position = context.origin();

        BlockState replacementBlock;
        BlockState currentblock;
        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos();
        ChunkAccess chunk = world.getChunk(position.getX() >> 4, position.getZ() >> 4);
        int maxHeight = Math.min(61, world.getSeaLevel() - 1);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                blockposMutable.set(position.getX() + x, maxHeight, position.getZ() + z);
                while (blockposMutable.getY() > 10) {
                    currentblock = chunk.getBlockState(blockposMutable);

                    while (currentblock.getFluidState().isEmpty() && blockposMutable.getY() > 10) {
                        currentblock = chunk.getBlockState(blockposMutable.move(Direction.DOWN));
                    }

                    if (blockposMutable.getY() <= 10) {
                        break;
                    }

                    for (Direction face : Direction.values()) {
                        blockposMutable.move(face);
                        if (blockposMutable.getY() < maxHeight) {
                            // Prefer chunk-local reads when neighbor is in the same chunk
                            boolean sameChunk = (blockposMutable.getX() >> 4) == (position.getX() >> 4)
                                    && (blockposMutable.getZ() >> 4) == (position.getZ() >> 4);
                            currentblock = sameChunk ? chunk.getBlockState(blockposMutable) : world.getBlockState(blockposMutable);
                            if (currentblock.isAir()) {
                                Biome biome = world.getBiome(blockposMutable).value();
                                ResourceLocation rl = world.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome);

                                replacementBlock = GeneralUtils.carverFillerBlock(rl == null ? "" : rl.toString(), biome);
                                world.setBlock(blockposMutable, replacementBlock, 2);
                            }
                        }
                        blockposMutable.move(face.getOpposite());
                    }

                    blockposMutable.move(Direction.DOWN);
                }
            }
        }
        return true;
    }
}
