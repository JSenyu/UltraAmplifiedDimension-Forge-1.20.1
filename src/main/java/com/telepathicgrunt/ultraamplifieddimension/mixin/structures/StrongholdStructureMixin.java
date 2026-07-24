package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import com.telepathicgrunt.ultraamplifieddimension.world.structures.UADStructureHeights;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StrongholdStructure.class)
public abstract class StrongholdStructureMixin {

    @Inject(method = "generatePieces", at = @At("TAIL"))
    private static void uad_clampHeight(StructurePiecesBuilder builder, Structure.GenerationContext context, CallbackInfo ci) {
        if (context.chunkGenerator() instanceof UADChunkGenerator) {
            builder.moveInsideHeights(context.random(), UADStructureHeights.undergroundMin(), UADStructureHeights.undergroundMax());
        }
    }
}
