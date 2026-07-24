package com.telepathicgrunt.ultraamplifieddimension.world.placement;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.dimension.terrain.UADTerrainSampler;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADHeightProviders;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

/**
 * Ancient city start height depends on generateBelowZero:
 * below-zero worlds place near vanilla Y=-27 band; otherwise above Y=0 in deep dark pockets.
 */
public class ConfigAncientCityHeight extends HeightProvider {
    public static final ConfigAncientCityHeight INSTANCE = new ConfigAncientCityHeight();
    public static final Codec<ConfigAncientCityHeight> CODEC = Codec.unit(() -> INSTANCE);

    private ConfigAncientCityHeight() {
    }

    @Override
    public int sample(RandomSource random, WorldGenerationContext context) {
        if (UADTerrainSampler.generateBelowZero()) {
            // Vanilla is absolute -27. Keep well above bedrock (-64..-59) and beard_box dig.
            return Mth.randomBetweenInclusive(random, -34, -22);
        }
        return Mth.randomBetweenInclusive(random, 16, 40);
    }

    @Override
    public HeightProviderType<?> getType() {
        return UADHeightProviders.CONFIG_ANCIENT_CITY.get();
    }
}
