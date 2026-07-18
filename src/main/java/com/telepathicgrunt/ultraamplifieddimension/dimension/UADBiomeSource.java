package com.telepathicgrunt.ultraamplifieddimension.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.dimension.biomeprovider.BiomeGroup;
import com.telepathicgrunt.ultraamplifieddimension.dimension.biomeprovider.RegionManager;
import com.telepathicgrunt.ultraamplifieddimension.utils.OpenSimplexNoise;
import com.telepathicgrunt.ultraamplifieddimension.utils.WorldSeedHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class UADBiomeSource extends BiomeSource {
    public static final Codec<UADBiomeSource> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.optionalFieldOf("seed", 0L).forGetter(source -> source.configuredSeed),
                    Codec.intRange(1, 20).fieldOf("biome_size").forGetter(source -> source.biomeSize),
                    Codec.floatRange(0, 1).fieldOf("sub_biome_rate").forGetter(source -> source.subBiomeRate),
                    Codec.floatRange(0, 1).fieldOf("mutated_biome_rate").forGetter(source -> source.mutatedBiomeRate),
                    RegionManager.CODEC.fieldOf("regions").forGetter(source -> source.regionManager)
            ).apply(instance, instance.stable(UADBiomeSource::new))
    );

    private record BiomeSample(
            Holder<Biome> biome,
            Holder<Biome> mainBiome,
            RegionManager.Region region,
            double mutation
    ) {}

    private record NoiseFields(
            OpenSimplexNoise region,
            OpenSimplexNoise ocean,
            OpenSimplexNoise biomePick,
            OpenSimplexNoise detail,
            OpenSimplexNoise mutation,
            OpenSimplexNoise pocket
    ) {}

    // 0 = resolve to world seed on first sample (datapack may decode before WorldOptions).
    private final long configuredSeed;
    private final int biomeSize;
    private final float subBiomeRate;
    private final float mutatedBiomeRate;
    private final RegionManager regionManager;
    private final List<Holder<Biome>> possibleBiomes;
    private final Object noiseLock = new Object();
    private volatile NoiseFields noise;

    public UADBiomeSource(long seed, int biomeSize, float subBiomeRate, float mutatedBiomeRate, RegionManager regionManager) {
        super();
        this.configuredSeed = seed;
        this.biomeSize = biomeSize;
        this.subBiomeRate = subBiomeRate;
        this.mutatedBiomeRate = mutatedBiomeRate;
        this.regionManager = regionManager;
        this.possibleBiomes = List.copyOf(regionManager.allBiomes());
    }

    private NoiseFields noise() {
        NoiseFields local = this.noise;
        if (local != null) {
            return local;
        }
        synchronized (this.noiseLock) {
            if (this.noise == null) {
                long resolved = this.configuredSeed != 0L ? this.configuredSeed : WorldSeedHolder.getSeed();
                this.noise = new NoiseFields(
                        new OpenSimplexNoise(resolved),
                        new OpenSimplexNoise(resolved + 1337L),
                        new OpenSimplexNoise(resolved + 4242L),
                        new OpenSimplexNoise(resolved + 7919L),
                        new OpenSimplexNoise(resolved + 104729L),
                        new OpenSimplexNoise(resolved + 314159L)
                );
            }
            return this.noise;
        }
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
        BiomeSample center = sampleBaseBiome(quartX, quartZ);
        return applyEdgeVariants(quartX, quartZ, center);
    }

    private BiomeSample sampleBaseBiome(int quartX, int quartZ) {
        NoiseFields n = noise();
        double regionScale = 4.2D * this.biomeSize;
        double biomeScale = Math.max(1.0D, regionScale / 3.0D);
        double nx = quartX / regionScale;
        double nz = quartZ / regionScale;
        double bx = quartX / biomeScale;
        double bz = quartZ / biomeScale;

        RegionManager.Region region = smoothRegion(quartX, quartZ, computeRawRegion(n, quartX, quartZ, nx, nz));

        BiomeGroup group;
        if (region == RegionManager.Region.OCEAN) {
            double tempNoise = n.region().eval(quartX / 16.8D, quartZ / 16.8D) * 0.85D + 0.5D;
            group = regionManager.pickByNoise(region, tempNoise);
        } else {
            double pick = (n.biomePick().eval(bx, bz) + 1.0D) * 0.5D;
            group = regionManager.pickByNoise(region, pick);
        }

        Holder<Biome> mainBiome = group.getMainBiome();
        double detail = (n.detail().eval(bx * 1.7D, bz * 1.7D) + 1.0D) * 0.5D;
        double mutation = (n.mutation().eval(bx * 1.3D + 50.0D, bz * 1.3D - 50.0D) + 1.0D) * 0.5D;

        boolean useSub = detail < subBiomeRate;
        boolean useMutated = mutation < mutatedBiomeRate;

        Holder<Biome> biome;
        if (useMutated && useSub) {
            biome = group.getMutatedSubBiome().or(group::getMutatedBiome).orElse(mainBiome);
        } else if (useMutated) {
            biome = group.getMutatedBiome().orElse(mainBiome);
        } else if (useSub) {
            biome = group.getSubBiome().orElse(mainBiome);
        } else {
            biome = mainBiome;
        }

        return new BiomeSample(biome, mainBiome, region, mutation);
    }

    private Holder<Biome> applyEdgeVariants(int quartX, int quartZ, BiomeSample center) {
        BiomeSample north = sampleBaseBiome(quartX, quartZ - 1);
        BiomeSample south = sampleBaseBiome(quartX, quartZ + 1);
        BiomeSample west = sampleBaseBiome(quartX - 1, quartZ);
        BiomeSample east = sampleBaseBiome(quartX + 1, quartZ);

        boolean centerOcean = regionManager.isOceanBiome(center.mainBiome());
        for (BiomeSample neighbor : new BiomeSample[]{north, south, west, east}) {
            if (centerOcean != regionManager.isOceanBiome(neighbor.mainBiome())) {
                return regionManager.getShore(center.mainBiome()).orElse(center.biome());
            }
        }

        for (BiomeSample neighbor : new BiomeSample[]{north, south, west, east}) {
            if (center.region() == neighbor.region() && center.mainBiome() != neighbor.mainBiome()) {
                if (center.mutation() < mutatedBiomeRate) {
                    Optional<Holder<Biome>> mutatedBorder = regionManager.getMutatedBorder(center.mainBiome());
                    if (mutatedBorder.isPresent()) {
                        return mutatedBorder.get();
                    }
                }
                return regionManager.getBorder(center.mainBiome()).orElse(center.biome());
            }
        }

        return center.biome();
    }

    private RegionManager.Region computeRawRegion(NoiseFields n, int quartX, int quartZ, double nx, double nz) {
        double oceanValue = (n.ocean().eval(nx * (4.2D / 3.0D), nz * (4.2D / 3.0D)) + 1.0D) * 0.5D;
        if (oceanValue < 0.2D) {
            return RegionManager.Region.OCEAN;
        }

        double regionValue = (n.region().eval(nx, nz) * 0.75D) + 0.5D;
        if (regionValue < 0.3D) {
            return pocketRoll(n, quartX, quartZ, 25) ? RegionManager.Region.NETHER : RegionManager.Region.HOT;
        }
        if (regionValue < 0.5D) {
            return RegionManager.Region.WARM;
        }
        if (regionValue < 0.7D) {
            return pocketRoll(n, quartX, quartZ, 30) ? RegionManager.Region.END : RegionManager.Region.COOL;
        }
        return RegionManager.Region.ICY;
    }

    private RegionManager.Region smoothRegion(int quartX, int quartZ, RegionManager.Region center) {
        NoiseFields n = noise();
        double scale = 4.2D * biomeSize;
        RegionManager.Region north = computeRawRegion(n, quartX, quartZ - 1, quartX / scale, (quartZ - 1) / scale);
        RegionManager.Region south = computeRawRegion(n, quartX, quartZ + 1, quartX / scale, (quartZ + 1) / scale);
        RegionManager.Region west = computeRawRegion(n, quartX - 1, quartZ, (quartX - 1) / scale, quartZ / scale);
        RegionManager.Region east = computeRawRegion(n, quartX + 1, quartZ, (quartX + 1) / scale, quartZ / scale);

        if (center == RegionManager.Region.OCEAN) {
            if (north != RegionManager.Region.OCEAN
                    && south != RegionManager.Region.OCEAN
                    && west != RegionManager.Region.OCEAN
                    && east != RegionManager.Region.OCEAN
                    && smoothChance(n, quartX, quartZ, 11, 2)) {
                return north;
            }
        } else if (touchesOcean(north, south, west, east)) {
            if (center == RegionManager.Region.END || center == RegionManager.Region.NETHER) {
                RegionManager.Region replacement = firstLandRegion(north, south, west, east);
                return replacement != null ? replacement : RegionManager.Region.OCEAN;
            }
        }

        if (hasRegionNeighbor(north, south, west, east, RegionManager.Region.END) && smoothChance(n, quartX, quartZ, 23, 2)) {
            return RegionManager.Region.END;
        }
        if (hasRegionNeighbor(north, south, west, east, RegionManager.Region.NETHER) && smoothChance(n, quartX, quartZ, 37, 3)) {
            return RegionManager.Region.NETHER;
        }

        return center;
    }

    private static boolean touchesOcean(RegionManager.Region... regions) {
        for (RegionManager.Region region : regions) {
            if (region == RegionManager.Region.OCEAN) {
                return true;
            }
        }
        return false;
    }

    private static RegionManager.Region firstLandRegion(RegionManager.Region... regions) {
        for (RegionManager.Region region : regions) {
            if (region != RegionManager.Region.OCEAN
                    && region != RegionManager.Region.END
                    && region != RegionManager.Region.NETHER) {
                return region;
            }
        }
        return null;
    }

    private static boolean hasRegionNeighbor(RegionManager.Region north, RegionManager.Region south, RegionManager.Region west, RegionManager.Region east, RegionManager.Region target) {
        return north == target || south == target || west == target || east == target;
    }

    private static boolean pocketRoll(NoiseFields n, int quartX, int quartZ, int chance) {
        double value = (n.pocket().eval(quartX * 0.11D, quartZ * 0.11D) + 1.0D) * 0.5D;
        int bucket = (int) Math.floor(value * chance);
        if (bucket >= chance) {
            bucket = chance - 1;
        }
        return bucket == 0;
    }

    private static boolean smoothChance(NoiseFields n, int quartX, int quartZ, int salt, int chance) {
        double value = (n.pocket().eval(quartX * 0.11D + salt, quartZ * 0.11D + salt) + 1.0D) * 0.5D;
        return (int) Math.floor(value * chance) != 0;
    }
}
