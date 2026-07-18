package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import com.telepathicgrunt.ultraamplifieddimension.world.structures.UADStructureHeights;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetherFossilStructure.class)
public abstract class NetherFossilStructureMixin {

    @Redirect(
            method = "findGenerationPoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/heightproviders/HeightProvider;sample(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;)I"
            )
    )
    private int uad_clampFossilY(HeightProvider provider, RandomSource random, WorldGenerationContext generationContext, Structure.GenerationContext context) {
        int y = provider.sample(random, generationContext);
        if (context.chunkGenerator() instanceof UADChunkGenerator) {
            return UADStructureHeights.clampTerrainY(y);
        }
        return y;
    }
}
