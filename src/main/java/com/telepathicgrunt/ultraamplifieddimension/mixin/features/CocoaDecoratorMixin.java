package com.telepathicgrunt.ultraamplifieddimension.mixin.features;

import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla CocoaDecorator assumes at least one log exists, but TreeFeature can still
 * run decorators when only leaves were placed (e.g. trunk column already occupied by logs
 * so placeLog skips while foliage still generates). Dense UAD jungles hit that path often.
 */
@Mixin(CocoaDecorator.class)
public class CocoaDecoratorMixin {

	@Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/treedecorators/TreeDecorator$Context;)V",
			at = @At("HEAD"),
			cancellable = true)
	private void uad_skipCocoaWithoutLogs(TreeDecorator.Context context, CallbackInfo ci) {
		if (context.logs().isEmpty()) {
			ci.cancel();
		}
	}
}