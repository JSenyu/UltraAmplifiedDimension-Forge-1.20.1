package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADBlocks;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.CountConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.HashMap;
import java.util.Map;

public class GlowPatch extends Feature<CountConfig> {

    private static Map<BlockState, BlockState> GLOWBLOCK_MAP;

    public static void setFillerMap() {
        if (GLOWBLOCK_MAP == null) {
            GLOWBLOCK_MAP = new HashMap<>();
            GLOWBLOCK_MAP.put(Blocks.DIRT.defaultBlockState(), UADBlocks.GLOWDIRT.get().defaultBlockState());
            GLOWBLOCK_MAP.put(Blocks.COARSE_DIRT.defaultBlockState(), UADBlocks.COARSE_GLOWDIRT.get().defaultBlockState());
            GLOWBLOCK_MAP.put(Blocks.GRASS_BLOCK.defaultBlockState(), UADBlocks.GLOWGRASS_BLOCK.get().defaultBlockState());
            GLOWBLOCK_MAP.put(Blocks.MYCELIUM.defaultBlockState(), UADBlocks.GLOWMYCELIUM.get().defaultBlockState());
            GLOWBLOCK_MAP.put(Blocks.STONE.defaultBlockState(), UADBlocks.GLOWSTONE_ORE.get().defaultBlockState());
            GLOWBLOCK_MAP.put(Blocks.PODZOL.defaultBlockState(), UADBlocks.GLOWPODZOL.get().defaultBlockState());
            GLOWBLOCK_MAP.put(Blocks.SAND.defaultBlockState(), UADBlocks.GLOWSAND.get().defaultBlockState());
            GLOWBLOCK_MAP.put(Blocks.RED_SAND.defaultBlockState(), UADBlocks.RED_GLOWSAND.get().defaultBlockState());
        }
    }

    public GlowPatch(Codec<CountConfig> configFactory) {
        super(configFactory);
        setFillerMap();
    }

    @Override
    public boolean place(FeaturePlaceContext<CountConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        CountConfig countConfig = context.config();
        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos();
        ChunkAccess cachedChunk = level.getChunk(position);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            blockposMutable.set(position).move(direction, 6);
            int nearbyLandY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockposMutable.getX(), blockposMutable.getZ());

            if (nearbyLandY < position.getY()) {
                return false;
            }
        }

        for (int attempts = 0; attempts < countConfig.count; ++attempts) {
            int gausX = (int) (Math.max(Math.min(random.nextGaussian() * 3, 15), -15));
            int gausY = random.nextInt(4) - random.nextInt(4);
            int gausZ = (int) (Math.max(Math.min(random.nextGaussian() * 3, 15), -15));

            blockposMutable.set(position).move(gausX, gausY + 1, gausZ);
            if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                cachedChunk = level.getChunk(blockposMutable);
            }

            BlockState chosenAboveBlock = cachedChunk.getBlockState(blockposMutable);
            BlockState chosenBlock = cachedChunk.getBlockState(blockposMutable.move(Direction.DOWN));

            if (chosenBlock.is(Blocks.STONE)) {
                cachedChunk.setBlockState(blockposMutable, GLOWBLOCK_MAP.get(chosenBlock), false);
            } else if (GLOWBLOCK_MAP.containsKey(chosenBlock) && chosenAboveBlock.isAir()) {
                cachedChunk.setBlockState(blockposMutable, GLOWBLOCK_MAP.get(chosenBlock), false);
            }
        }

        return true;
    }
}
