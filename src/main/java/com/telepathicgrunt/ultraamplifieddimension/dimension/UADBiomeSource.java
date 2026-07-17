package com.telepathicgrunt.ultraamplifieddimension.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.dimension.biomeprovider.BiomeGroup;
import com.telepathicgrunt.ultraamplifieddimension.dimension.biomeprovider.RegionManager;
import com.telepathicgrunt.ultraamplifieddimension.utils.OpenSimplexNoise;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.stream.Stream;

public class UADBiomeSource extends BiomeSource {
    public static final Codec<UADBiomeSource> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.optionalFieldOf("seed", 0L).forGetter(source -> source.seed),
                    Codec.intRange(1, 20).fieldOf("biome_size").forGetter(source -> source.biomeSize),
                    Codec.floatRange(0, 1).fieldOf("sub_biome_rate").forGetter(source -> source.subBiomeRate),
                    Codec.floatRange(0, 1).fieldOf("mutated_biome_rate").forGetter(source -> source.mutatedBiomeRate),
                    RegionManager.CODEC.fieldOf("regions").forGetter(source -> source.regionManager)
            ).apply(instance, instance.stable(UADBiomeSource::new))
    );

    private final long seed;
    private final int biomeSize;
    private final float subBiomeRate;
    private final float mutatedBiomeRate;
    private final RegionManager regionManager;
    private final List<Holder<Biome>> possibleBiomes;

    private final OpenSimplexNoise regionNoise;
    private final OpenSimplexNoise oceanNoise;
    private final OpenSimplexNoise biomePickNoise;
    private final OpenSimplexNoise detailNoise;
    private final OpenSimplexNoise mutationNoise;
    private final OpenSimplexNoise pocketNoise;

    public UADBiomeSource(long seed, int biomeSize, float subBiomeRate, float mutatedBiomeRate, RegionManager regionManager) {
        super();
        this.seed = seed;
        this.biomeSize = biomeSize;
        this.subBiomeRate = subBiomeRate;
        this.mutatedBiomeRate = mutatedBiomeRate;
        this.regionManager = regionManager;
        this.possibleBiomes = List.copyOf(regionManager.allBiomes());

        this.regionNoise = new OpenSimplexNoise(seed);
        this.oceanNoise = new OpenSimplexNoise(seed + 1337L);
        this.biomePickNoise = new OpenSimplexNoise(seed + 4242L);
        this.detailNoise = new OpenSimplexNoise(seed + 7919L);
        this.mutationNoise = new OpenSimplexNoise(seed + 104729L);
        this.pocketNoise = new OpenSimplexNoise(seed + 314159L);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return possibleBiomes.stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        double regionScale = 4.2D * this.biomeSize;
        double biomeScale = Math.max(1.0D, regionScale / 3.0D);
        double nx = quartX / regionScale;
        double nz = quartZ / regionScale;
        double bx = quartX / biomeScale;
        double bz = quartZ / biomeScale;

        RegionManager.Region region = sampleRegion(quartX, quartZ, nx, nz);
        double pick = (biomePickNoise.eval(bx, bz) + 1.0D) * 0.5D;
        BiomeGroup group = regionManager.pickByNoise(region, pick);

        double detail = (detailNoise.eval(bx * 1.7D, bz * 1.7D) + 1.0D) * 0.5D;
        double mutation = (mutationNoise.eval(bx * 1.3D + 50.0D, bz * 1.3D - 50.0D) + 1.0D) * 0.5D;

        boolean useSub = detail < subBiomeRate;
        boolean useMutated = mutation < mutatedBiomeRate;

        if (useMutated && useSub) {
            return group.getMutatedSubBiome().or(group::getMutatedBiome).orElseGet(group::getMainBiome);
        }
        if (useMutated) {
            return group.getMutatedBiome().orElseGet(group::getMainBiome);
        }
        if (useSub) {
            return group.getSubBiome().orElseGet(group::getMainBiome);
        }
        return group.getMainBiome();
    }

    private RegionManager.Region sampleRegion(int quartX, int quartZ, double nx, double nz) {
        double oceanValue = (oceanNoise.eval(nx * (4.2D / 3.0D), nz * (4.2D / 3.0D)) + 1.0D) * 0.5D;
        if (oceanValue < 0.2D) {
            return RegionManager.Region.OCEAN;
        }

        double regionValue = (regionNoise.eval(nx, nz) * 0.75D) + 0.5D;
        if (regionValue < 0.3D) {
            if (pocketRoll(quartX, quartZ, 25)) {
                return RegionManager.Region.NETHER;
            }
            return RegionManager.Region.HOT;
        }
        if (regionValue < 0.5D) {
            return RegionManager.Region.WARM;
        }
        if (regionValue < 0.7D) {
            if (pocketRoll(quartX, quartZ, 30)) {
                return RegionManager.Region.END;
            }
            return RegionManager.Region.COOL;
        }
        return RegionManager.Region.ICY;
    }

    private boolean pocketRoll(int quartX, int quartZ, int chance) {
        double noise = (pocketNoise.eval(quartX * 0.11D, quartZ * 0.11D) + 1.0D) * 0.5D;
        int bucket = (int) Math.floor(noise * chance);
        if (bucket >= chance) {
            bucket = chance - 1;
        }
        return bucket == 0;
    }
}
