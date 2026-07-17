package com.telepathicgrunt.ultraamplifieddimension.client;

import com.telepathicgrunt.ultraamplifieddimension.config.UADimensionConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class UADDimensionSpecialEffects extends DimensionSpecialEffects {
    public UADDimensionSpecialEffects() {
        // Defaults only — config is not loaded yet during RegisterDimensionSpecialEffectsEvent.
        super(245.0F, true, SkyType.NORMAL, false, false);
    }

    @Override
    public float getCloudHeight() {
        return UADimensionConfig.cloudHeight.get();
    }

    @Override
    public SkyType skyType() {
        try {
            return SkyType.valueOf(UADimensionConfig.skyType.get().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return SkyType.NORMAL;
        }
    }

    @Override
    public boolean forceBrightLightmap() {
        return UADimensionConfig.netherLighting.get();
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return UADimensionConfig.heavyFog.get();
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        float f = net.minecraft.util.Mth.clamp(net.minecraft.util.Mth.cos(sunHeight * ((float) Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
        float multiplier = getHeightBasedMultiplier();
        double r = color.x * (f * 0.94F + 0.06F) * multiplier;
        double g = color.y * (f * 0.94F + 0.06F) * multiplier;
        double b = color.z * (f * 0.91F + 0.09F) * multiplier;
        return new Vec3(r, g, b);
    }

    private float getHeightBasedMultiplier() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 1.0F;
        }
        int fullBrightnessFromTop = 56;
        int noBrightnessFromBottom = 90;
        float multiplier = (float) ((player.getEyePosition(1).y - noBrightnessFromBottom)
                / Math.max(player.clientLevel.dimensionType().logicalHeight() - fullBrightnessFromTop - noBrightnessFromBottom, 1));
        return Math.min(Math.max(multiplier, 0), 1);
    }
}
