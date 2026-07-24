package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import com.telepathicgrunt.ultraamplifieddimension.world.structures.UADStructureHeights;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MineshaftStructure.class)
public abstract class MineshaftStructureMixin {

    @Inject(method = "generatePiecesAndAdjust", at = @At("RETURN"))
    private void uad_clampHeight(StructurePiecesBuilder builder, Structure.GenerationContext context, CallbackInfoReturnable<Integer> cir) {
        if (context.chunkGenerator() instanceof UADChunkGenerator) {
            builder.moveInsideHeights(context.random(), UADStructureHeights.undergroundMin(), UADStructureHeights.undergroundMax());
        }
    }
}
