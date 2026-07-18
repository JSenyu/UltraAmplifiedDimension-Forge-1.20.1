package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import com.telepathicgrunt.ultraamplifieddimension.world.structures.OceanStructurePiecesUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.OceanRuinPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OceanRuinPieces.OceanRuinPiece.class)
public abstract class OceanRuinPiecesPieceMixin {

    @Inject(
            method = "postProcess",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/TemplateStructurePiece;postProcess(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private void uad_fixedYHeightForUAD(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                                        RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        if (chunkGenerator instanceof UADChunkGenerator) {
            OceanRuinPieces.OceanRuinPiece piece = (OceanRuinPieces.OceanRuinPiece) (Object) this;
            BlockPos templatePosition = piece.templatePosition();
            if (templatePosition.getY() > 100 && random.nextFloat() > 0.25F) {
                int newHeight = OceanStructurePiecesUtils.getNewLedgeHeight(
                        level, chunkGenerator, random, piece.template(), piece.getRotation(), templatePosition);
                piece.move(0, newHeight - templatePosition.getY(), 0);
            }
        }
    }
}
