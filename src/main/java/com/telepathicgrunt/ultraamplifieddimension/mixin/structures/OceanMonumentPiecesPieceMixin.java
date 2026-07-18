package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces$OceanMonumentPiece")
public abstract class OceanMonumentPiecesPieceMixin {

    @Inject(method = "generateWaterBox", at = @At("HEAD"), cancellable = true)
    private void uad_noWater(WorldGenLevel level, BoundingBox boundingBox, int x1, int y1, int z1, int x2, int y2, int z2, CallbackInfo ci) {
        ServerLevel serverLevel = level.getLevel();
        if (serverLevel.getChunkSource().getGenerator() instanceof UADChunkGenerator) {
            if (Math.abs(x1 - x2) > 6 || Math.abs(y1 - y2) > 6 || Math.abs(z1 - z2) > 6) {
                ci.cancel();
            }
        }
    }
}