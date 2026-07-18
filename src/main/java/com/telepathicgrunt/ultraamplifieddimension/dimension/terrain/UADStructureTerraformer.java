package com.telepathicgrunt.ultraamplifieddimension.dimension.terrain;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;

/** Applies structure terrain-adjustment kernels to density samples. */
public final class UADStructureTerraformer {
    private static final int RADIUS = 12;
    private static final int KERNEL_SIZE = 24;

    private static final float[] TERRAFORM_KERNEL = Util.make(new float[KERNEL_SIZE * KERNEL_SIZE * KERNEL_SIZE], (floats) -> {
        for (int x = 0; x < KERNEL_SIZE; ++x) {
            for (int z = 0; z < KERNEL_SIZE; ++z) {
                for (int y = 0; y < KERNEL_SIZE; ++y) {
                    floats[x * KERNEL_SIZE * KERNEL_SIZE + z * KERNEL_SIZE + y] = (float) terrainValue(z - RADIUS, y - RADIUS, x - RADIUS);
                }
            }
        }
    });

    private static final float[] GIANT_TERRAFORM_KERNEL = Util.make(new float[KERNEL_SIZE * KERNEL_SIZE * KERNEL_SIZE], (floats) -> {
        for (int x = 0; x < KERNEL_SIZE; ++x) {
            for (int z = 0; z < KERNEL_SIZE; ++z) {
                for (int y = 0; y < KERNEL_SIZE; ++y) {
                    floats[x * KERNEL_SIZE * KERNEL_SIZE + z * KERNEL_SIZE + y] = (float) giantTerrainValue(z - RADIUS, y - RADIUS, x - RADIUS);
                }
            }
        }
    });

    private final ObjectList<Piece> pieces;
    private final ObjectList<JigsawJunction> junctions;

    private UADStructureTerraformer(ObjectList<Piece> pieces, ObjectList<JigsawJunction> junctions) {
        this.pieces = pieces;
        this.junctions = junctions;
    }

    public static UADStructureTerraformer collect(StructureManager structureManager, ChunkPos chunkPos) {
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        ObjectList<Piece> pieces = new ObjectArrayList<>(10);
        ObjectList<JigsawJunction> junctions = new ObjectArrayList<>(32);

        // Same selection as Beardifier, plus always include ocean monument (old UAD added MONUMENT).
        structureManager.startsForStructure(chunkPos, structure ->
                structure.terrainAdaptation() != TerrainAdjustment.NONE
                        || structure instanceof OceanMonumentStructure
        ).forEach(start -> {
            boolean giantStructure = start.getStructure() instanceof OceanMonumentStructure;
            for (StructurePiece structurePiece : start.getPieces()) {
                if (!structurePiece.isCloseToChunk(chunkPos, RADIUS)) {
                    continue;
                }
                if (structurePiece instanceof PoolElementStructurePiece poolPiece) {
                    if (poolPiece.getElement().getProjection() == StructureTemplatePool.Projection.RIGID) {
                        pieces.add(new Piece(poolPiece.getBoundingBox(), poolPiece.getGroundLevelDelta(), false));
                    }
                    for (JigsawJunction junction : poolPiece.getJunctions()) {
                        int sx = junction.getSourceX();
                        int sz = junction.getSourceZ();
                        if (sx > minX - RADIUS && sz > minZ - RADIUS && sx < minX + 15 + RADIUS && sz < minZ + 15 + RADIUS) {
                            junctions.add(junction);
                        }
                    }
                } else {
                    pieces.add(new Piece(structurePiece.getBoundingBox(), 0, giantStructure));
                }
            }
        });

        return new UADStructureTerraformer(pieces, junctions);
    }

    public double apply(double noiseValue, int x, int y, int z) {
        ObjectListIterator<Piece> pieceIt = pieces.iterator();
        while (pieceIt.hasNext()) {
            Piece piece = pieceIt.next();
            BoundingBox box = piece.box;
            int pieceX = Math.max(0, Math.max(box.minX() - x, x - box.maxX()));
            int pieceY = y - (box.minY() + piece.groundLevelDelta);
            int pieceZ = Math.max(0, Math.max(box.minZ() - z, z - box.maxZ()));
            if (piece.giant) {
                pieceY -= 2;
                noiseValue += giantTerraformNoise(pieceX, pieceY, pieceZ) * 0.8D;
            } else {
                noiseValue += terraformNoise(pieceX, pieceY, pieceZ) * 0.8D;
            }
        }

        ObjectListIterator<JigsawJunction> junctionIt = junctions.iterator();
        while (junctionIt.hasNext()) {
            JigsawJunction junction = junctionIt.next();
            // Preserve old UAD quirk: pieceX used z - sourceX
            int pieceX = z - junction.getSourceX();
            int pieceY = y - junction.getSourceGroundY();
            int pieceZ = z - junction.getSourceZ();
            noiseValue += terraformNoise(pieceX, pieceY, pieceZ) * 0.4D;
        }
        return noiseValue;
    }

    private static double terraformNoise(int x, int y, int z) {
        int xPos = x + RADIUS;
        int yPos = y + RADIUS;
        int zPos = z + RADIUS;
        if (xPos >= 0 && xPos < KERNEL_SIZE && yPos >= 0 && yPos < KERNEL_SIZE && zPos >= 0 && zPos < KERNEL_SIZE) {
            return TERRAFORM_KERNEL[zPos * KERNEL_SIZE * KERNEL_SIZE + xPos * KERNEL_SIZE + yPos];
        }
        return 0.0D;
    }

    private static double giantTerraformNoise(int x, int y, int z) {
        int xPos = x + RADIUS;
        int zPos = z + RADIUS;
        if (xPos >= 0 && xPos < KERNEL_SIZE && y >= 0 && y < KERNEL_SIZE && zPos >= 0 && zPos < KERNEL_SIZE) {
            return GIANT_TERRAFORM_KERNEL[zPos * KERNEL_SIZE * KERNEL_SIZE + xPos * KERNEL_SIZE + y];
        }
        return 0.0D;
    }

    private static double terrainValue(int x, int y, int z) {
        double horizontalDist = (x * x) + (z * z);
        double offsetY = (double) y + 0.5D;
        double squaredOffsetY = offsetY * offsetY;
        double d3 = Math.pow(Math.E, -(squaredOffsetY / 16.0D + horizontalDist / 16.0D));
        double d4 = -offsetY * Mth.fastInvSqrt(squaredOffsetY / 2.0D + horizontalDist / 2.0D) / 2.0D;
        return d4 * d3;
    }

    private static double giantTerrainValue(int x, int y, int z) {
        double horizontalDist = (x * x) + (z * z) + 0.0001D;
        double v = (RADIUS - Math.abs(y)) * 0.08D;
        return -((Mth.fastInvSqrt(horizontalDist) * 1.1D) - 1D + v);
    }

    private record Piece(BoundingBox box, int groundLevelDelta, boolean giant) {
    }
}
