package com.telepathicgrunt.ultraamplifieddimension.world.processors;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADProcessors;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public class SpawnerRandomizingProcessor extends StructureProcessor {

    public static final Codec<SpawnerRandomizingProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.mapPair(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("resourcelocation"),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight"))
                    .codec()
                    .listOf()
                    .fieldOf("spawner_mob_entries")
                    .forGetter(spawnerRandomizingProcessor -> spawnerRandomizingProcessor.spawnerRandomizingProcessor)
    ).apply(instance, SpawnerRandomizingProcessor::new));

    public final List<Pair<EntityType<?>, Integer>> spawnerRandomizingProcessor;

    private SpawnerRandomizingProcessor(List<Pair<EntityType<?>, Integer>> spawnerRandomizingProcessor) {
        this.spawnerRandomizingProcessor = spawnerRandomizingProcessor;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos,
            StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld,
            StructurePlaceSettings structurePlacementData) {
        if (structureBlockInfoWorld.state().getBlock() instanceof SpawnerBlock) {
            BlockPos worldPos = structureBlockInfoWorld.pos();
            RandomSource random = RandomSource.create(worldPos.asLong() * worldPos.getY());
            return new StructureTemplate.StructureBlockInfo(
                    worldPos,
                    structureBlockInfoWorld.state(),
                    setMobSpawnerEntity(random, structureBlockInfoWorld.nbt()));
        }
        return structureBlockInfoWorld;
    }

    /**
     * Writes 1.20+ spawner NBT ({@code SpawnData.entity} / weighted {@code data}+{@code weight}).
     */
    private CompoundTag setMobSpawnerEntity(RandomSource random, CompoundTag nbt) {
        EntityType<?> entity = GeneralUtils.getRandomEntry(spawnerRandomizingProcessor, random);
        if (entity != null) {
            String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity).toString();

            CompoundTag compound = new CompoundTag();
            compound.putShort("Delay", (short) 20);
            compound.putShort("MinSpawnDelay", (short) 200);
            compound.putShort("MaxSpawnDelay", (short) 800);
            compound.putShort("SpawnCount", (short) 4);
            compound.putShort("MaxNearbyEntities", (short) 6);
            compound.putShort("RequiredPlayerRange", (short) 16);
            compound.putShort("SpawnRange", (short) 4);

            CompoundTag entityTag = new CompoundTag();
            entityTag.putString("id", entityId);

            CompoundTag spawnData = new CompoundTag();
            spawnData.put("entity", entityTag);
            compound.put("SpawnData", spawnData);

            CompoundTag potentialData = new CompoundTag();
            potentialData.put("entity", entityTag.copy());

            CompoundTag listEntry = new CompoundTag();
            listEntry.put("data", potentialData);
            listEntry.putInt("weight", 1);

            ListTag listTag = new ListTag();
            listTag.add(listEntry);
            compound.put("SpawnPotentials", listTag);

            return compound;
        }

        UltraAmplifiedDimension.LOGGER.warn("EntityType in a dungeon does not exist in registry! : {}", spawnerRandomizingProcessor);
        return nbt;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return UADProcessors.SPAWNER_RANDOMIZING_PROCESSOR.get();
    }
}
