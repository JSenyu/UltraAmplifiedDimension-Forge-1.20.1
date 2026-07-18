package com.telepathicgrunt.ultraamplifieddimension.dimension.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.function.Function;

/** Solid/fluid block choice for a density sample (nether/end via biome tags). */
public final class UADTerrainBlocks {
    private UADTerrainBlocks() {
    }

    public static BlockState terrainBlock(
            double noiseValue,
            Holder<Biome> biome,
            int x,
            int y,
            int z,
            int seaLevel,
            NoiseGeneratorSettings settings,
            Function<BlockPos, Holder<Biome>> biomeLookup
    ) {
        BlockState defaultBlock = settings.defaultBlock();
        BlockState defaultFluid = settings.defaultFluid();

        if (noiseValue > 0.0D) {
            if (biome.is(BiomeTags.IS_NETHER)) {
                return Blocks.NETHERRACK.defaultBlockState();
            }
            if (biome.is(BiomeTags.IS_END)) {
                return Blocks.END_STONE.defaultBlockState();
            }
            return defaultBlock;
        }

        if (y < seaLevel) {
            if (biome.is(BiomeTags.IS_NETHER)) {
                if (isSurroundedByNether(biomeLookup, x, z)) {
                    if (y > seaLevel - 7) {
                        return defaultFluid;
                    }
                    if (y == seaLevel - 7) {
                        return Blocks.MAGMA_BLOCK.defaultBlockState();
                    }
                    return Blocks.LAVA.defaultBlockState();
                }
                if (y <= seaLevel - 6) {
                    return Blocks.OBSIDIAN.defaultBlockState();
                }
                return defaultFluid;
            }
            return defaultFluid;
        }

        return Blocks.AIR.defaultBlockState();
    }

    private static boolean isSurroundedByNether(Function<BlockPos, Holder<Biome>> biomeLookup, int x, int z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int xOffset = -2; xOffset <= 2; xOffset++) {
            for (int zOffset = -2; zOffset <= 2; zOffset++) {
                if (Math.abs(xOffset * zOffset) == 2) {
                    Holder<Biome> neighbor = biomeLookup.apply(mutable.set(x + xOffset, 0, z + zOffset));
                    if (!neighbor.is(BiomeTags.IS_NETHER)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
