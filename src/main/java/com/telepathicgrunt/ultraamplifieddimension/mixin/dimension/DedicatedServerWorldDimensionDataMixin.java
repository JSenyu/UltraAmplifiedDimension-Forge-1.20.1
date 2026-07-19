package com.telepathicgrunt.ultraamplifieddimension.mixin.dimension;

import com.telepathicgrunt.ultraamplifieddimension.dimension.OverworldIntegration;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dedicated servers build dimensions via level-type -> WorldPreset.createWorldDimensions(),
 * not WorldPresets.createNormalWorldDimensions(). Apply config-driven overworld replacement here.
 */
@Mixin(targets = "net.minecraft.server.dedicated.DedicatedServerProperties$WorldDimensionData")
public class DedicatedServerWorldDimensionDataMixin {

    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private void ultraamplifieddimension$replaceOverworld(
            RegistryAccess access,
            CallbackInfoReturnable<WorldDimensions> cir
    ) {
        WorldDimensions modified = OverworldIntegration.applyDefaultOverworldReplacement(access, cir.getReturnValue());
        if (modified != cir.getReturnValue()) {
            cir.setReturnValue(modified);
        }
    }
}
