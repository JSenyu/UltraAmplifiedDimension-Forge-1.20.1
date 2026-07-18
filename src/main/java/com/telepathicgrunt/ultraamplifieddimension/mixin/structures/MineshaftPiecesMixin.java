package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import com.telepathicgrunt.ultraamplifieddimension.world.structures.MineshaftPiecesUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MineshaftPieces.MineShaftRoom.class)
public abstract class MineshaftPiecesMixin {

    @Inject(method = "postProcess", at = @At("TAIL"))
    private void uad_giantRoom(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                               RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        if (chunkGenerator instanceof UADChunkGenerator) {
            MineshaftPieces.MineShaftRoom room = (MineshaftPieces.MineShaftRoom) (Object) this;
            if (random.nextFloat() < 0.25F) {
                MineshaftPiecesUtils.generateLargeRoom(level, room, box);
            } else {
                MineshaftPiecesUtils.generateFloorRoom(level, room, box);
            }
        }
    }
}
