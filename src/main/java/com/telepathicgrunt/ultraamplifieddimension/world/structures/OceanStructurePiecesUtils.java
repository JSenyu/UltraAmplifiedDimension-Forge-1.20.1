package com.telepathicgrunt.ultraamplifieddimension.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class OceanStructurePiecesUtils {
    private OceanStructurePiecesUtils() {
    }

    public static int getNewLedgeHeight(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random,
                                          StructureTemplate template, Rotation rotation, BlockPos templatePosition) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        int halfSizeX = template.getSize().getX() / 2;
        int halfSizeZ = template.getSize().getZ() / 2;

        BlockPos centerOffset = template.getZeroPositionWithTransform(
                new BlockPos(template.getSize().getX() / 2 - 1, 0, template.getSize().getZ() / 2 - 1),
                Mirror.NONE,
                rotation
        );
        mutable.set(templatePosition.offset(centerOffset));
        int highestHeight = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, mutable.getX(), mutable.getZ());
        int bottomOfSea = chunkGenerator.getSeaLevel() - 10;
        BlockState currentState;
        BlockState pastState = Blocks.STONE.defaultBlockState();

        int startHeight = Math.min(random.nextInt(random.nextInt(Math.max(highestHeight - bottomOfSea, 1)) + 1) + bottomOfSea + 5, UADStructureHeights.MAX_TERRAIN);

        for (mutable.move(Direction.UP, startHeight);
             mutable.getY() > Math.max(bottomOfSea - 20, UADStructureHeights.minSolid());
             mutable.move(Direction.DOWN)) {
            currentState = level.getBlockState(mutable);
            if (isSolidUnderwaterGround(level, mutable, currentState) && pastState.getFluidState().is(FluidTags.WATER)) {
                if (noAirAround(level, mutable.below(), (int) (halfSizeX * 0.8f), (int) (halfSizeZ * 0.8f))) {
                    return mutable.getY();
                }
            }
            pastState = currentState;
        }

        return bottomOfSea;
    }

    private static boolean isSolidUnderwaterGround(BlockGetter level, BlockPos pos, BlockState state) {
        return state.canOcclude() && !state.is(BlockTags.ICE);
    }

    public static boolean noAirAround(BlockGetter level, BlockPos blockpos, int xRange, int zRange) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -xRange; x <= xRange; x += xRange) {
            for (int z = -zRange; z <= zRange; z += zRange) {
                BlockState state = level.getBlockState(mutable.setWithOffset(blockpos, x, 0, z));
                if (state.isAir()) {
                    return false;
                }
            }
        }
        return true;
    }
}