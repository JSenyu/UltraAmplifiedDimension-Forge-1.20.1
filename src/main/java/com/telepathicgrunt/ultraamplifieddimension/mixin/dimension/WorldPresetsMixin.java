package com.telepathicgrunt.ultraamplifieddimension.mixin.dimension;

import com.telepathicgrunt.ultraamplifieddimension.dimension.OverworldIntegration;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldPresets.class)
public class WorldPresetsMixin {

    @Inject(method = "createNormalWorldDimensions", at = @At("RETURN"), cancellable = true)
    private static void ultraamplifieddimension$replaceDefaultOverworld(
            RegistryAccess access,
            CallbackInfoReturnable<WorldDimensions> cir
    ) {
        WorldDimensions modified = OverworldIntegration.applyDefaultOverworldReplacement(access, cir.getReturnValue());
        if (modified != cir.getReturnValue()) {
            cir.setReturnValue(modified);
        }
    }
}
