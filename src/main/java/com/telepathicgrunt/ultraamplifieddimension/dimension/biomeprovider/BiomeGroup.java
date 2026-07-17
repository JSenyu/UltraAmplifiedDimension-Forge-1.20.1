package com.telepathicgrunt.ultraamplifieddimension.dimension.biomeprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;

public class BiomeGroup implements Comparable<BiomeGroup> {
    public static final Codec<BiomeGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.CODEC.fieldOf("main_biome").forGetter(g -> g.mainBiome),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(g -> g.weight),
            Biome.CODEC.optionalFieldOf("shore_biome").forGetter(g -> g.shoreBiome),
            Biome.CODEC.optionalFieldOf("border_biome").forGetter(g -> g.borderBiome),
            Biome.CODEC.optionalFieldOf("sub_biome").forGetter(g -> g.subBiome),
            Biome.CODEC.optionalFieldOf("mutated_biome").forGetter(g -> g.mutatedBiome),
            Biome.CODEC.optionalFieldOf("mutated_sub_biome").forGetter(g -> g.mutatedSubBiome),
            Biome.CODEC.optionalFieldOf("mutated_border_biome").forGetter(g -> g.mutatedBorderBiome)
    ).apply(instance, BiomeGroup::new));

    private final Holder<Biome> mainBiome;
    private final int weight;
    private final Optional<Holder<Biome>> shoreBiome;
    private final Optional<Holder<Biome>> borderBiome;
    private final Optional<Holder<Biome>> subBiome;
    private final Optional<Holder<Biome>> mutatedBiome;
    private final Optional<Holder<Biome>> mutatedSubBiome;
    private final Optional<Holder<Biome>> mutatedBorderBiome;

    public BiomeGroup(
            Holder<Biome> mainBiome,
            int weight,
            Optional<Holder<Biome>> shoreBiome,
            Optional<Holder<Biome>> borderBiome,
            Optional<Holder<Biome>> subBiome,
            Optional<Holder<Biome>> mutatedBiome,
            Optional<Holder<Biome>> mutatedSubBiome,
            Optional<Holder<Biome>> mutatedBorderBiome
    ) {
        this.mainBiome = mainBiome;
        this.weight = weight;
        this.shoreBiome = shoreBiome;
        this.borderBiome = borderBiome;
        this.subBiome = subBiome;
        this.mutatedBiome = mutatedBiome;
        this.mutatedSubBiome = mutatedSubBiome;
        this.mutatedBorderBiome = mutatedBorderBiome;
    }

    public Holder<Biome> getMainBiome() {
        return mainBiome;
    }

    public int getWeight() {
        return weight;
    }

    public Optional<Holder<Biome>> getShoreBiome() {
        return shoreBiome;
    }

    public Optional<Holder<Biome>> getBorderBiome() {
        return borderBiome;
    }

    public Optional<Holder<Biome>> getSubBiome() {
        return subBiome;
    }

    public Optional<Holder<Biome>> getMutatedBiome() {
        return mutatedBiome;
    }

    public Optional<Holder<Biome>> getMutatedSubBiome() {
        return mutatedSubBiome;
    }

    public Optional<Holder<Biome>> getMutatedBorderBiome() {
        return mutatedBorderBiome;
    }

    @Override
    public int compareTo(BiomeGroup other) {
        return Float.compare(other.mainBiome.value().getBaseTemperature(), this.mainBiome.value().getBaseTemperature());
    }
}
