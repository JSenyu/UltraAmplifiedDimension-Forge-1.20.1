package com.telepathicgrunt.ultraamplifieddimension.world.features.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

import java.util.function.Function;

/** base + random(0..spread) for datapack feature configs. */
public record FeatureSpread(int base, int spread) {

    public static final Codec<FeatureSpread> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("base").forGetter(FeatureSpread::base),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("spread").forGetter(FeatureSpread::spread)
    ).apply(instance, FeatureSpread::new));

    public static Codec<FeatureSpread> codec(int minBase, int maxBase, int maxSpread) {
        return CODEC.comapFlatMap(featureSpread -> {
            if (featureSpread.base < minBase || featureSpread.base > maxBase || featureSpread.spread > maxSpread) {
                return DataResult.error(() -> "Radius spread values are out of bounds");
            }
            return DataResult.success(featureSpread);
        }, Function.identity());
    }

    public int sample(RandomSource random) {
        return this.base + random.nextInt(this.spread + 1);
    }
}
