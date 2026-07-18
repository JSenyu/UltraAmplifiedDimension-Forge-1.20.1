package com.telepathicgrunt.ultraamplifieddimension.world.structures;

import com.telepathicgrunt.ultraamplifieddimension.mixin.structures.MineshaftRoomAccessor;
import com.telepathicgrunt.ultraamplifieddimension.mixin.structures.StructurePieceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;

public final class MineshaftPiecesUtils {
    private MineshaftPiecesUtils() {
    }

    public static void generateFloorRoom(WorldGenLevel level, MineshaftPieces.MineShaftRoom room, BoundingBox chunkBox) {
        BoundingBox box = room.getBoundingBox();
        StructurePieceAccessor piece = (StructurePieceAccessor) room;
        piece.uad_callGenerateBox(level, chunkBox, box.minX(), box.minY(), box.minZ(), box.maxX(), box.minY(), box.maxZ(),
                Blocks.COARSE_DIRT.defaultBlockState(), Blocks.CAVE_AIR.defaultBlockState(), false);
    }

    public static void generateLargeRoom(WorldGenLevel level, MineshaftPieces.MineShaftRoom room, BoundingBox chunkBox) {
        BoundingBox box = room.getBoundingBox();
        int newMaxY = box.maxY() + Math.min(140, 225 - box.minY());
        box.encapsulate(new BoundingBox(chunkBox.minX(), box.minY(), chunkBox.minZ(), chunkBox.maxX(), newMaxY, chunkBox.maxZ()));

        StructurePieceAccessor piece = (StructurePieceAccessor) room;
        piece.uad_callGenerateBox(level, chunkBox, box.minX(), box.minY(), box.minZ(), box.maxX() + 8, box.minY(), box.maxZ(),
                Blocks.COARSE_DIRT.defaultBlockState(), Blocks.CAVE_AIR.defaultBlockState(), false);
        piece.uad_callGenerateBox(level, chunkBox, box.minX() + 3, box.minY() + 1, box.minZ() + 3, box.maxX() - 1, box.minY() + 4, box.maxZ() - 1,
                Blocks.CAVE_AIR.defaultBlockState(), Blocks.CAVE_AIR.defaultBlockState(), false);

        for (BoundingBox entrance : ((MineshaftRoomAccessor) room).uad_getChildEntranceBoxes()) {
            piece.uad_callGenerateBox(level, chunkBox, entrance.minX(), entrance.maxY() - 2, entrance.minZ(), entrance.maxX(), entrance.maxY(), entrance.maxZ(),
                    Blocks.CAVE_AIR.defaultBlockState(), Blocks.CAVE_AIR.defaultBlockState(), false);
        }

        piece.uad_callGenerateUpperHalfSphere(level, chunkBox, box.minX() + 3, box.minY() + 4, box.minZ() + 3, box.maxX() - 3, box.maxY(), box.maxZ() - 3,
                Blocks.CAVE_AIR.defaultBlockState(), false);
        updateLiquidBlocks(room, level, box, box.minX() - 1, box.minY() + 4, box.minZ() - 1, box.maxX() + 1, box.maxY(), box.maxZ() + 1);
    }

    public static void updateLiquidBlocks(MineshaftPieces.MineShaftRoom room, WorldGenLevel level, BoundingBox boundingBox,
                                          int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        float f = maxX - minX + 1;
        float f1 = maxY - minY + 1;
        float f2 = maxZ - minZ + 1;
        float f3 = minX + f / 2.0F;
        float f4 = minZ + f2 / 2.0F;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        StructurePieceAccessor piece = (StructurePieceAccessor) room;

        for (int y = minY; y <= maxY; ++y) {
            float yPercent = (y - minY) / f1;
            for (int x = minX; x <= maxX; ++x) {
                float xPercent = (x - f3) / (f * 0.5F);
                for (int z = minZ; z <= maxZ; ++z) {
                    float zPercent = (z - f4) / (f2 * 0.5F);
                    if (!piece.uad_callGetBlock(level, x, y, z, boundingBox).getFluidState().isEmpty()) {
                        float threshold = (xPercent * xPercent) + (yPercent * yPercent) + (zPercent * zPercent);
                        if (threshold <= 1.05F) {
                            mutable.set(piece.uad_callGetWorldX(x, z), piece.uad_callGetWorldY(y), piece.uad_callGetWorldZ(x, z));
                            FluidState fluidState = level.getFluidState(mutable);
                            if (!fluidState.isEmpty()) {
                                level.scheduleTick(mutable, fluidState.getType(), 0);
                            }
                        }
                    }
                }
            }
        }
    }
}
