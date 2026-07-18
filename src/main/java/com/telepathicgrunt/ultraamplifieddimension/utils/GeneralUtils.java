package com.telepathicgrunt.ultraamplifieddimension.utils;

import com.mojang.datafixers.util.Pair;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneralUtils {

    public static String biomeIDString(String biomeName) {
        return UltraAmplifiedDimension.MODID + ":" + biomeName;
    }

    public static <T> T getRandomEntry(List<Pair<T, Integer>> rlList, RandomSource random) {
        if (rlList.isEmpty()) {
            return null;
        }

        double totalWeight = 0.0;
        for (Pair<T, Integer> pair : rlList) {
            totalWeight += pair.getSecond();
        }

        int index = 0;
        for (double randomWeightPicked = random.nextFloat() * totalWeight; index < rlList.size() - 1; ++index) {
            randomWeightPicked -= rlList.get(index).getSecond();
            if (randomWeightPicked <= 0.0) {
                break;
            }
        }

        return rlList.get(index).getFirst();
    }

    private static final Map<String, BlockState> FILLER_BIOME_MAP = new HashMap<>();

    static {
        FILLER_BIOME_MAP.put(biomeIDString("iced_terrain"), Blocks.ICE.defaultBlockState());
        FILLER_BIOME_MAP.put(biomeIDString("ice_spikes"), Blocks.ICE.defaultBlockState());
        FILLER_BIOME_MAP.put(biomeIDString("deep_frozen_ocean"), Blocks.ICE.defaultBlockState());
        FILLER_BIOME_MAP.put(biomeIDString("frozen_ocean"), Blocks.ICE.defaultBlockState());
    }

    private static final Map<String, BlockState> LAVA_FLOOR_BIOME_MAP = new HashMap<>();

    static {
        LAVA_FLOOR_BIOME_MAP.put(biomeIDString("iced_terrain"), Blocks.OBSIDIAN.defaultBlockState());
        LAVA_FLOOR_BIOME_MAP.put(biomeIDString("ice_spikes"), Blocks.MAGMA_BLOCK.defaultBlockState());
        LAVA_FLOOR_BIOME_MAP.put(biomeIDString("relic_snowy_taiga"), Blocks.MAGMA_BLOCK.defaultBlockState());
        LAVA_FLOOR_BIOME_MAP.put(biomeIDString("snowy_rocky_taiga"), Blocks.MAGMA_BLOCK.defaultBlockState());
        LAVA_FLOOR_BIOME_MAP.put(biomeIDString("snowy_taiga"), Blocks.MAGMA_BLOCK.defaultBlockState());
        LAVA_FLOOR_BIOME_MAP.put(biomeIDString("snowy_tundra"), Blocks.MAGMA_BLOCK.defaultBlockState());
        LAVA_FLOOR_BIOME_MAP.put(biomeIDString("frozen_desert"), Blocks.MAGMA_BLOCK.defaultBlockState());
        LAVA_FLOOR_BIOME_MAP.put(biomeIDString("deep_frozen_ocean"), Blocks.MAGMA_BLOCK.defaultBlockState());
        LAVA_FLOOR_BIOME_MAP.put(biomeIDString("frozen_ocean"), Blocks.MAGMA_BLOCK.defaultBlockState());
    }

    public static BlockState carverLavaReplacement(String biomeIDString, Biome biome) {
        BlockState replacementBlock = LAVA_FLOOR_BIOME_MAP.get(biomeIDString);

        if (replacementBlock == null) {
            if (isIcyBiome(biome)) {
                if (biome.getBaseTemperature() < -0.5F) {
                    LAVA_FLOOR_BIOME_MAP.put(biomeIDString, Blocks.OBSIDIAN.defaultBlockState());
                } else {
                    LAVA_FLOOR_BIOME_MAP.put(biomeIDString, Blocks.MAGMA_BLOCK.defaultBlockState());
                }
            } else {
                LAVA_FLOOR_BIOME_MAP.put(biomeIDString, Blocks.LAVA.defaultBlockState());
            }

            replacementBlock = LAVA_FLOOR_BIOME_MAP.get(biomeIDString);
        }

        return replacementBlock;
    }

    public static BlockState carverFillerBlock(String biomeIDString, Biome biome) {
        BlockState replacementBlock = FILLER_BIOME_MAP.get(biomeIDString);

        if (replacementBlock == null) {
            if (biomeHasTag(biome, BiomeTags.IS_END)) {
                FILLER_BIOME_MAP.put(biomeIDString, Blocks.END_STONE.defaultBlockState());
            } else if (biomeHasTag(biome, BiomeTags.IS_NETHER)) {
                FILLER_BIOME_MAP.put(biomeIDString, Blocks.NETHERRACK.defaultBlockState());
            } else if (isIcyBiome(biome) && biome.getBaseTemperature() < -0.5F) {
                FILLER_BIOME_MAP.put(biomeIDString, Blocks.ICE.defaultBlockState());
            } else {
                FILLER_BIOME_MAP.put(biomeIDString, Blocks.STONE.defaultBlockState());
            }

            replacementBlock = FILLER_BIOME_MAP.get(biomeIDString);
        }

        return replacementBlock;
    }

    private static final Map<BlockState, Boolean> IS_FULLCUBE_MAP = new HashMap<>();

    public static boolean isFullCube(BlockGetter world, BlockPos pos, BlockState state) {
        if (!IS_FULLCUBE_MAP.containsKey(state)) {
            boolean isFullCube = Block.isShapeFullBlock(state.getCollisionShape(world, pos));
            IS_FULLCUBE_MAP.put(state, isFullCube);
        }
        return IS_FULLCUBE_MAP.get(state);
    }

    public static BlockState orientateChest(BlockGetter blockView, BlockPos blockPos, BlockState blockState) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        Direction bestDirection = blockState.getValue(HorizontalDirectionalBlock.FACING);

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            mutable.set(blockPos).move(facing);

            if (isFullCube(blockView, mutable, blockView.getBlockState(mutable))) {
                bestDirection = facing;

                mutable.move(facing.getOpposite(), 2);
                if (!isFullCube(blockView, mutable, blockView.getBlockState(mutable))) {
                    break;
                }
            }
        }

        return blockState.setValue(HorizontalDirectionalBlock.FACING, bestDirection.getOpposite());
    }

    public static boolean isSolidBlock(BlockState state) {
        return state.canOcclude();
    }

    public static boolean isSurfaceBlock(BlockState state) {
        return !state.isAir() && state.canOcclude() && !state.is(net.minecraft.tags.BlockTags.LEAVES);
    }

    public static boolean biomeHasTag(Biome biome, TagKey<Biome> tag) {
        ResourceLocation id = ForgeRegistries.BIOMES.getKey(biome);
        if (id == null) {
            return false;
        }
        return ForgeRegistries.BIOMES.tags().getTag(tag).contains(biome);
    }

    private static boolean isIcyBiome(Biome biome) {
        return biomeHasTag(biome, BiomeTags.IS_TAIGA) || biome.getBaseTemperature() <= 0.15F;
    }
}
