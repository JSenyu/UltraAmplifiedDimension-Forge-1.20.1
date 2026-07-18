package com.telepathicgrunt.ultraamplifieddimension.world.structures;

import com.telepathicgrunt.ultraamplifieddimension.mixin.structures.StructurePieceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;

public final class OceanMonumentPiecesUtils {
    private OceanMonumentPiecesUtils() {
    }

    public static void generateWaterBox(WorldGenLevel level, ChunkGenerator chunkGenerator,
                                        OceanMonumentPieces.MonumentBuilding monument, BoundingBox chunkBox) {
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState prismarineDark = Blocks.DARK_PRISMARINE.defaultBlockState();
        BlockState prismarineBricks = Blocks.PRISMARINE_BRICKS.defaultBlockState();
        BlockState prismarineRough = Blocks.PRISMARINE.defaultBlockState();
        BlockState seaLantern = Blocks.SEA_LANTERN.defaultBlockState();
        StructurePieceAccessor piece = (StructurePieceAccessor) monument;

        piece.uad_callGenerateBox(level, chunkBox, 0, 1, 0, 24, 1, 57, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 1, 2, 1, 23, 2, 56, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 2, 3, 2, 22, 3, 55, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 3, 4, 3, 21, 4, 54, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 7, 5, 7, 17, 5, 54, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 8, 6, 8, 16, 6, 54, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 9, 7, 9, 15, 7, 54, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 10, 8, 10, 14, 8, 54, water, water, false);

        piece.uad_callGenerateBox(level, chunkBox, 33, 1, 0, 57, 1, 57, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 34, 2, 1, 56, 2, 56, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 35, 3, 2, 55, 3, 55, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 36, 4, 3, 54, 4, 54, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 40, 5, 7, 50, 5, 54, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 41, 6, 8, 49, 6, 54, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 42, 7, 9, 48, 7, 54, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 43, 8, 10, 47, 8, 54, water, water, false);

        piece.uad_callGenerateBox(level, chunkBox, 14, 1, 22, 44, 1, 57, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 14, 2, 22, 44, 2, 56, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 14, 1, 22, 44, 8, 54, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 15, 8, 21, 43, 9, 42, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 16, 10, 21, 42, 10, 41, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 17, 11, 21, 41, 11, 40, water, water, false);

        piece.uad_callGenerateBox(level, chunkBox, 21, 12, 21, 36, 12, 36, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 22, 13, 22, 35, 13, 35, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 23, 14, 23, 34, 14, 34, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 24, 15, 24, 33, 15, 33, water, water, false);

        piece.uad_callGenerateBox(level, chunkBox, 25, 0, 10, 32, 3, 20, prismarineDark, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 25, 0, 10, 32, 0, 10, prismarineBricks, prismarineBricks, false);
        piece.uad_callGenerateBox(level, chunkBox, 32, 0, 10, 32, 0, 20, prismarineBricks, prismarineBricks, false);
        piece.uad_callGenerateBox(level, chunkBox, 25, 0, 10, 25, 0, 20, prismarineBricks, prismarineBricks, false);
        piece.uad_callGenerateBox(level, chunkBox, 25, 3, 10, 32, 3, 10, prismarineBricks, prismarineBricks, false);
        piece.uad_callGenerateBox(level, chunkBox, 32, 3, 10, 32, 3, 20, prismarineBricks, prismarineBricks, false);
        piece.uad_callGenerateBox(level, chunkBox, 25, 3, 10, 25, 3, 20, prismarineBricks, prismarineBricks, false);
        piece.uad_callGenerateBox(level, chunkBox, 25, 1, 10, 25, 2, 10, prismarineRough, prismarineRough, false);
        piece.uad_callGenerateBox(level, chunkBox, 32, 1, 10, 32, 2, 10, prismarineRough, prismarineRough, false);
        piece.uad_callPlaceBlock(level, seaLantern, 25, 1, 12, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 32, 1, 12, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 25, 1, 14, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 32, 1, 14, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 25, 1, 16, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 32, 1, 16, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 25, 1, 18, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 32, 1, 18, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 25, 1, 20, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 32, 1, 20, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 27, 1, 10, chunkBox);
        piece.uad_callPlaceBlock(level, seaLantern, 30, 1, 10, chunkBox);
        piece.uad_callGenerateBox(level, chunkBox, 26, 3, 11, 31, 3, 20, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 26, 1, 20, 31, 3, 20, water, water, false);
        piece.uad_callGenerateBox(level, chunkBox, 26, 1, 21, 31, 3, 21, water, water, false);

        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
        for (int y = 4; y < 16; y++) {
            for (int x = 26; x < 32; x++) {
                for (int z = 11; z < 21; z++) {
                    BlockState blockState = piece.uad_callGetBlock(level, x, y, z, chunkBox);
                    int worldX = piece.uad_callGetWorldX(x, z);
                    int worldY = piece.uad_callGetWorldY(y);
                    int worldZ = piece.uad_callGetWorldZ(x, z);
                    blockpos.set(worldX, worldY, worldZ);
                    int offset = 0;

                    if (blockState.canOcclude() || !blockState.canSurvive(level, blockpos)) {
                        piece.uad_callPlaceBlock(level, water, x, y, z, chunkBox);

                        offset++;
                        blockpos.move(Direction.UP);
                        while (blockpos.getY() < chunkGenerator.getSeaLevel() && !blockState.canSurvive(level, blockpos)) {
                            piece.uad_callPlaceBlock(level, water, x, y + offset, z, chunkBox);
                            blockpos.move(Direction.UP);
                            offset++;
                        }
                    } else {
                        x = 32;
                        y = 16;
                        break;
                    }
                }
            }
        }
    }
}
