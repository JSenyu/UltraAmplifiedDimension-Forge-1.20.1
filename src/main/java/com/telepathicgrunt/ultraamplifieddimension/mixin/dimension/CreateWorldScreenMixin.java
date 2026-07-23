package com.telepathicgrunt.ultraamplifieddimension.mixin.dimension;

import com.telepathicgrunt.ultraamplifieddimension.dimension.OverworldIntegration;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Final gate before baking dimensions when the player clicks Create.
 * Ensures config-driven overworld replacement applies even if the player never switched world types.
 */
@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {

    @Shadow
    @Final
    WorldCreationUiState uiState;

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void ultraamplifieddimension$applyOverworldReplacementBeforeCreate(CallbackInfo ci) {
        WorldCreationContext context = this.uiState.getSettings();
        WorldDimensions selected = context.selectedDimensions();
        WorldDimensions modified = OverworldIntegration.applyDefaultOverworldReplacement(
                context.worldgenLoadContext(),
                selected
        );
        if (modified != selected) {
            this.uiState.setSettings(context.withSettings(context.options(), modified));
        }
    }
}
