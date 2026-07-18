package com.telepathicgrunt.ultraamplifieddimension.dimension.biomeprovider;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RegionManager {
    public static final Codec<RegionManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeGroup.CODEC.listOf().fieldOf("ocean_biomes").forGetter(r -> r.oceanList),
            BiomeGroup.CODEC.listOf().fieldOf("end_biomes").forGetter(r -> r.endList),
            BiomeGroup.CODEC.listOf().fieldOf("nether_biomes").forGetter(r -> r.netherList),
            BiomeGroup.CODEC.listOf().fieldOf("hot_biomes").forGetter(r -> r.hotList),
            BiomeGroup.CODEC.listOf().fieldOf("warm_biomes").forGetter(r -> r.warmList),
            BiomeGroup.CODEC.listOf().fieldOf("cool_biomes").forGetter(r -> r.coolList),
            BiomeGroup.CODEC.listOf().fieldOf("icy_biomes").forGetter(r -> r.icyList)
    ).apply(instance, RegionManager::new));

    private final List<BiomeGroup> oceanList;
    private final List<BiomeGroup> endList;
    private final List<BiomeGroup> netherList;
    private final List<BiomeGroup> hotList;
    private final List<BiomeGroup> warmList;
    private final List<BiomeGroup> coolList;
    private final List<BiomeGroup> icyList;

    private final int oceanWeight;
    private final int endWeight;
    private final int netherWeight;
    private final int hotWeight;
    private final int warmWeight;
    private final int coolWeight;
    private final int icyWeight;

    private final Map<Holder<Biome>, Holder<Biome>> subBiomeMap = new HashMap<>();
    private final Map<Holder<Biome>, Holder<Biome>> shoreMap = new HashMap<>();
    private final Map<Holder<Biome>, Holder<Biome>> borderMap = new HashMap<>();
    private final Map<Holder<Biome>, Holder<Biome>> mutatedMap = new HashMap<>();
    private final Map<Holder<Biome>, Holder<Biome>> mutatedSubBiomeMap = new HashMap<>();
    private final Map<Holder<Biome>, Holder<Biome>> mutatedBorderBiomeMap = new HashMap<>();
    private final Set<Holder<Biome>> oceanBiomes = new HashSet<>();

    public RegionManager(
            List<BiomeGroup> oceanList,
            List<BiomeGroup> endList,
            List<BiomeGroup> netherList,
            List<BiomeGroup> hotList,
            List<BiomeGroup> warmList,
            List<BiomeGroup> coolList,
            List<BiomeGroup> icyList
    ) {
        this.oceanList = prepareAndPopulateMaps(oceanList, true);
        this.endList = prepareAndPopulateMaps(endList, false);
        this.netherList = prepareAndPopulateMaps(netherList, false);
        this.hotList = prepareAndPopulateMaps(hotList, false);
        this.warmList = prepareAndPopulateMaps(warmList, false);
        this.coolList = prepareAndPopulateMaps(coolList, false);
        this.icyList = prepareAndPopulateMaps(icyList, false);

        this.oceanWeight = sumWeight(this.oceanList);
        this.endWeight = sumWeight(this.endList);
        this.netherWeight = sumWeight(this.netherList);
        this.hotWeight = sumWeight(this.hotList);
        this.warmWeight = sumWeight(this.warmList);
        this.coolWeight = sumWeight(this.coolList);
        this.icyWeight = sumWeight(this.icyList);
    }

    private List<BiomeGroup> prepareAndPopulateMaps(List<BiomeGroup> input, boolean trackOceanBiomes) {
        if (input == null || input.isEmpty()) {
            throw new JsonSyntaxException("Empty biome region in Ultra Amplified Dimension dimension JSON.");
        }
        List<BiomeGroup> copy = new ArrayList<>(input);
        copy.sort(BiomeGroup::compareTo);

        for (BiomeGroup group : copy) {
            Holder<Biome> mainBiome = group.getMainBiome();
            if (trackOceanBiomes) {
                registerOceanBiome(mainBiome);
                group.getSubBiome().ifPresent(this::registerOceanBiome);
                group.getBorderBiome().ifPresent(this::registerOceanBiome);
                group.getMutatedBiome().ifPresent(this::registerOceanBiome);
                group.getMutatedSubBiome().ifPresent(this::registerOceanBiome);
                group.getMutatedBorderBiome().ifPresent(this::registerOceanBiome);
            }

            group.getShoreBiome().ifPresent(shore -> checkBeforeAddingBiome(shoreMap, mainBiome, shore));
            group.getBorderBiome().ifPresent(border -> checkBeforeAddingBiome(borderMap, mainBiome, border));
            group.getSubBiome().ifPresent(sub -> checkBeforeAddingBiome(subBiomeMap, mainBiome, sub));
            group.getMutatedBiome().ifPresent(mutated -> checkBeforeAddingBiome(mutatedMap, mainBiome, mutated));
            group.getMutatedSubBiome().ifPresent(mutatedSub -> checkBeforeAddingBiome(mutatedSubBiomeMap, mainBiome, mutatedSub));
            group.getMutatedBorderBiome().ifPresent(mutatedBorder -> checkBeforeAddingBiome(mutatedBorderBiomeMap, mainBiome, mutatedBorder));
        }
        return copy;
    }

    private void registerOceanBiome(Holder<Biome> biome) {
        oceanBiomes.add(biome);
    }

    private void checkBeforeAddingBiome(Map<Holder<Biome>, Holder<Biome>> biomeMap, Holder<Biome> mainBiome, Holder<Biome> biomeToAdd) {
        Holder<Biome> existing = biomeMap.get(mainBiome);
        if (existing != null) {
            if (existing != biomeToAdd) {
                throw new JsonSyntaxException("A single biome was found multiple times in the \"main_biome\" entry with different border/sub/mutated biomes for it in Ultra Amplified Dimension's dimension json.");
            }
        } else {
            biomeMap.put(mainBiome, biomeToAdd);
        }
    }

    private static int sumWeight(List<BiomeGroup> list) {
        return list.stream().mapToInt(BiomeGroup::getWeight).sum();
    }

    public BiomeGroup pick(Region region, RandomSource random) {
        Pair<List<BiomeGroup>, Integer> pair = regionPair(region);
        return pickWeighted(pair.getLeft(), pair.getRight(), random.nextInt(Math.max(pair.getRight(), 1)));
    }

    public BiomeGroup pickByNoise(Region region, double noise01) {
        Pair<List<BiomeGroup>, Integer> pair = regionPair(region);
        double clamped = Math.max(0.0D, Math.min(0.999999999D, noise01));
        int target = (int) Math.floor(clamped * Math.max(pair.getRight(), 1));
        if (target >= pair.getRight()) {
            target = pair.getRight() - 1;
        }
        return pickWeighted(pair.getLeft(), pair.getRight(), target);
    }

    private Pair<List<BiomeGroup>, Integer> regionPair(Region region) {
        return switch (region) {
            case OCEAN -> Pair.of(oceanList, oceanWeight);
            case END -> Pair.of(endList, endWeight);
            case NETHER -> Pair.of(netherList, netherWeight);
            case HOT -> Pair.of(hotList, hotWeight);
            case WARM -> Pair.of(warmList, warmWeight);
            case COOL -> Pair.of(coolList, coolWeight);
            case ICY -> Pair.of(icyList, icyWeight);
        };
    }

    private static BiomeGroup pickWeighted(List<BiomeGroup> list, int totalWeight, int weightIndex) {
        int index = 0;
        for (int remaining = Math.floorMod(weightIndex, Math.max(totalWeight, 1)); index < list.size() - 1; ++index) {
            remaining -= list.get(index).getWeight();
            if (remaining < 0) {
                break;
            }
        }
        return list.get(index);
    }

    public boolean isOceanBiome(Holder<Biome> biome) {
        return oceanBiomes.contains(biome);
    }

    public Optional<Holder<Biome>> getShore(Holder<Biome> mainBiome) {
        return Optional.ofNullable(shoreMap.get(mainBiome));
    }

    public Optional<Holder<Biome>> getBorder(Holder<Biome> mainBiome) {
        return Optional.ofNullable(borderMap.get(mainBiome));
    }

    public Optional<Holder<Biome>> getMutatedBorder(Holder<Biome> mainBiome) {
        return Optional.ofNullable(mutatedBorderBiomeMap.get(mainBiome));
    }

    public List<Holder<Biome>> allBiomes() {
        List<Holder<Biome>> biomes = new ArrayList<>();
        addAll(biomes, oceanList);
        addAll(biomes, endList);
        addAll(biomes, netherList);
        addAll(biomes, hotList);
        addAll(biomes, warmList);
        addAll(biomes, coolList);
        addAll(biomes, icyList);
        return biomes;
    }

    private static void addAll(List<Holder<Biome>> out, List<BiomeGroup> groups) {
        for (BiomeGroup group : groups) {
            out.add(group.getMainBiome());
            group.getShoreBiome().ifPresent(out::add);
            group.getBorderBiome().ifPresent(out::add);
            group.getSubBiome().ifPresent(out::add);
            group.getMutatedBiome().ifPresent(out::add);
            group.getMutatedSubBiome().ifPresent(out::add);
            group.getMutatedBorderBiome().ifPresent(out::add);
        }
    }

    public enum Region {
        OCEAN,
        END,
        NETHER,
        HOT,
        WARM,
        COOL,
        ICY
    }
}
