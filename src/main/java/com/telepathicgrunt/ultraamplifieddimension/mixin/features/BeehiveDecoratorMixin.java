package com.telepathicgrunt.ultraamplifieddimension.mixin.features;

import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BeehiveDecorator always indexes logs.get(0). TreeFeature can still run decorators
  * when the trunk failed to place (dense UAD terrain / overlapping trees).
 */
@Mixin(BeehiveDecorator.class)
public class BeehiveDecoratorMixin {

    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/treedecorators/TreeDecorator$Context;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void uad_skipBeehiveWithoutLogs(TreeDecorator.Context context, CallbackInfo ci) {
        if (context.logs().isEmpty()) {
            ci.cancel();
        }
    }
}
