package com.telepathicgrunt.ultraamplifieddimension.mixin.blocks;

import com.telepathicgrunt.ultraamplifieddimension.config.UADimensionConfig;
import com.telepathicgrunt.ultraamplifieddimension.dimension.UADDimension;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {

	/**
	 * Make it so that Nether Portals can be created and activated in Ultra Amplified Dimension
	 * @author TelepathicGrunt
	 */
	@Inject(method = "inPortalDimension(Lnet/minecraft/world/level/Level;)Z",
			at = @At(value = "RETURN"),
			cancellable = true)
	private static void uad_allowNetherPortal(Level level, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() && level.dimension().equals(UADDimension.UAD_WORLD_KEY) && UADimensionConfig.allowNetherPortal.get()) {
			cir.setReturnValue(true);
		}
	}
}
