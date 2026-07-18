package com.telepathicgrunt.ultraamplifieddimension.world.features.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class LootTableConfig implements FeatureConfiguration {

    public static final Codec<LootTableConfig> CODEC = RecordCodecBuilder.create(cactusConfigInstance -> cactusConfigInstance.group(
            ResourceLocation.CODEC.fieldOf("loot_table").forGetter(config -> config.lootTable)
    ).apply(cactusConfigInstance, LootTableConfig::new));

    public final ResourceLocation lootTable;

    public LootTableConfig(ResourceLocation lootTable) {
        this.lootTable = lootTable;
    }
}
