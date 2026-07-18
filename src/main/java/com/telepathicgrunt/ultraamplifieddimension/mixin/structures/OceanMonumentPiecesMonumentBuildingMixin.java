package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import com.telepathicgrunt.ultraamplifieddimension.world.structures.OceanMonumentPiecesUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OceanMonumentPieces.MonumentBuilding.class)
public abstract class OceanMonumentPiecesMonumentBuildingMixin {

    @Inject(method = "postProcess", at = @At("HEAD"))
    private void uad_customWaterBox(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                                    RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        if (chunkGenerator instanceof UADChunkGenerator) {
            OceanMonumentPiecesUtils.generateWaterBox(level, chunkGenerator,
                    (OceanMonumentPieces.MonumentBuilding) (Object) this, box);
        }
    }
}