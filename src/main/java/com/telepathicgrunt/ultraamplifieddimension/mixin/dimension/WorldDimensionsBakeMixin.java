package com.telepathicgrunt.ultraamplifieddimension.mixin.dimension;

import com.telepathicgrunt.ultraamplifieddimension.dimension.OverworldIntegration;
import com.telepathicgrunt.ultraamplifieddimension.dimension.UADDimension;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Datapack dimensions are merged in {@link WorldDimensions#bake}. When Overworld is already UAD,
 * strip the redundant separate UAD LevelStem so it does not appear in {@code /execute in}.
 */
@Mixin(WorldDimensions.class)
public class WorldDimensionsBakeMixin {

    @Inject(method = "bake", at = @At("RETURN"), cancellable = true)
    private void ultraamplifieddimension$omitRedundantUadDimension(
            Registry<LevelStem> datapackDimensions,
            CallbackInfoReturnable<WorldDimensions.Complete> cir
    ) {
        if (OverworldIntegration.shouldKeepSeparateUadDimension()) {
            return;
        }

        WorldDimensions.Complete complete = cir.getReturnValue();
        Registry<LevelStem> filtered = OverworldIntegration.withoutLevelStem(
                complete.dimensions(),
                UADDimension.UAD_LEVEL_STEM_KEY
        );
        if (filtered == complete.dimensions()) {
            return;
        }
        cir.setReturnValue(new WorldDimensions.Complete(filtered, complete.specialWorldProperty()));
    }
}
