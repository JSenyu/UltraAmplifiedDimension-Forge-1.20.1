package com.telepathicgrunt.ultraamplifieddimension.dimension.terrain;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.function.Function;

/** Solid/fluid block choice for a density sample (nether/end via biome tags). */
public final class UADTerrainBlocks {
    private static final ResourceLocation DEEP_DARK_ID =
            ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "deep_dark");
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();

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
            BlockState solid;
            if (biome.is(BiomeTags.IS_NETHER)) {
                solid = Blocks.NETHERRACK.defaultBlockState();
            } else if (biome.is(BiomeTags.IS_END)) {
                solid = Blocks.END_STONE.defaultBlockState();
            } else {
                solid = defaultBlock;
            }
            return applyFloorAndDeepslate(solid, biome, x, y, z);
        }

        // Empty density: below Y=0 use dry caves (+ deep lava), not sea-level flooding.
        if (UADTerrainSampler.generateBelowZero() && y < 0) {
            if (y < -54) {
                return Blocks.LAVA.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
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

    /**
     * Config-aware bedrock floor + deepslate (below-zero band / deep dark pockets).
     */
    public static BlockState applyFloorAndDeepslate(BlockState state, Holder<Biome> biome, int x, int y, int z) {
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return state;
        }

        boolean belowZero = UADTerrainSampler.generateBelowZero();
        int floorY = UADTerrainSampler.minY();

        // Bedrock floor gradient (true at floorY, fades out over 5 blocks).
        if (y <= floorY) {
            return BEDROCK;
        }
        if (y < floorY + 5) {
            double progress = (double) (y - floorY) / 5.0D;
            if (gradientChance(x, y, z, 0xB3DF04) > progress) {
                return BEDROCK;
            }
        }

        boolean deepDark = isDeepDark(biome);
        boolean replaceable = state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.TUFF)
                || state.is(Blocks.NETHERRACK)
                || state.is(Blocks.END_STONE);

        if (!replaceable) {
            return state;
        }

        // Deep dark pockets always prefer deepslate (including above-zero ancient city areas).
        if (deepDark) {
            return DEEPSLATE;
        }

        if (belowZero) {
            // Vanilla-like: solid deepslate below Y=0, gradient to stone through Y=0..8.
            if (y < 0) {
                return DEEPSLATE;
            }
            if (y < 8) {
                double progress = (double) y / 8.0D;
                if (gradientChance(x, y, z, 0xDEE051A7) > progress) {
                    return DEEPSLATE;
                }
            }
        }

        return state;
    }

    public static boolean isDeepDark(Holder<Biome> biome) {
        return biome.unwrapKey()
                .map(key -> key.location().equals(DEEP_DARK_ID))
                .orElse(false);
    }

    /**
     * Deterministic 0..1 value for vertical gradients (avoids needing RandomSource in fill).
     */
    private static double gradientChance(int x, int y, int z, int salt) {
        long seed = Mth.getSeed(x, y, z) ^ (salt * 341873128712L);
        seed = (seed ^ (seed >>> 30)) * 0xBF58476D1CE4E5B9L;
        seed = (seed ^ (seed >>> 27)) * 0x94D049BB133111EBL;
        seed = seed ^ (seed >>> 31);
        return (seed & 0xFFFFFFL) / (double) 0x1000000L;
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
