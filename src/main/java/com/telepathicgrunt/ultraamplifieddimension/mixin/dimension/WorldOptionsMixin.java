package com.telepathicgrunt.ultraamplifieddimension.mixin.dimension;

import com.telepathicgrunt.ultraamplifieddimension.utils.WorldSeedHolder;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(WorldOptions.class)
public class WorldOptionsMixin {

    /**
     * World seed for worldgen when not specified by JSON by Haven King
     * https://github.com/Hephaestus-Dev/seedy-behavior/blob/master/src/main/java/dev/hephaestus/seedy/mixin/world/gen/GeneratorOptionsMixin.java
     */
    @Inject(method = "<init>(JZZLjava/util/Optional;)V", at = @At("RETURN"))
    private void uad_captureWorldSeed(long seed, boolean generateStructures, boolean bonusChest, Optional<String> legacy, CallbackInfo ci) {
        WorldSeedHolder.setSeed(seed);
    }

    @Inject(method = "<init>(JZZ)V", at = @At("RETURN"))
    private void uad_captureWorldSeedSimple(long seed, boolean generateStructures, boolean bonusChest, CallbackInfo ci) {
        WorldSeedHolder.setSeed(seed);
    }
}
