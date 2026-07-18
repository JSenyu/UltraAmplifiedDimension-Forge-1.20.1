package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import com.telepathicgrunt.ultraamplifieddimension.world.structures.UADStructureHeights;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.EndCityStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EndCityStructure.class)
public abstract class EndCityStructureStartMixin {

    @ModifyVariable(method = "findGenerationPoint", at = @At(value = "STORE"), ordinal = 0)
    private BlockPos uad_fixedYHeightForUAD(BlockPos pos, Structure.GenerationContext context) {
        if (context.chunkGenerator() instanceof UADChunkGenerator) {
            return new BlockPos(pos.getX(), UADStructureHeights.END_CITY_Y, pos.getZ());
        }
        return pos;
    }
}
