package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutPiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwampHutPiece.class)
public abstract class SwampHutPieceMixin {

    @Inject(method = "postProcess", at = @At("HEAD"))
    private void uad_fixedYHeightForUAD(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                                        RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        if (chunkGenerator instanceof UADChunkGenerator) {
            BoundingBox boundingBox = ((SwampHutPiece) (Object) this).getBoundingBox();
            int centerX = boundingBox.minX() + boundingBox.getXSpan() / 2;
            int centerZ = boundingBox.minZ() + boundingBox.getZSpan() / 2;
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
            boundingBox.move(0, surfaceY - boundingBox.minY() + 2, 0);
        }
    }
}