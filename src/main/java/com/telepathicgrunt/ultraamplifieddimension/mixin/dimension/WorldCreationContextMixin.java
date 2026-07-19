package com.telepathicgrunt.ultraamplifieddimension.mixin.dimension;

import com.telepathicgrunt.ultraamplifieddimension.dimension.OverworldIntegration;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldCreationContext.class)
public class WorldCreationContextMixin {

    @Unique
    private static final ThreadLocal<Boolean> ULTRAAMPLIFIEDDIMENSION$APPLYING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "withSettings", at = @At("RETURN"), cancellable = true)
    private void ultraamplifieddimension$applyOverworldReplacement(
            WorldOptions options,
            WorldDimensions dimensions,
            CallbackInfoReturnable<WorldCreationContext> cir
    ) {
        if (ULTRAAMPLIFIEDDIMENSION$APPLYING.get()) {
            return;
        }

        WorldCreationContext result = cir.getReturnValue();
        WorldDimensions selected = result.selectedDimensions();
        WorldDimensions modified = OverworldIntegration.applyDefaultOverworldReplacement(
                result.worldgenLoadContext(),
                selected
        );
        if (modified == selected) {
            return;
        }

        ULTRAAMPLIFIEDDIMENSION$APPLYING.set(true);
        try {
            cir.setReturnValue(result.withSettings(result.options(), modified));
        } finally {
            ULTRAAMPLIFIEDDIMENSION$APPLYING.set(false);
        }
    }
}
