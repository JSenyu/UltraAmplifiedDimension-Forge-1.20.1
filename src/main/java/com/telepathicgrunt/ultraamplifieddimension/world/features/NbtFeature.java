package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.NbtFeatureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class NbtFeature extends Feature<NbtFeatureConfig> {

    public NbtFeature(Codec<NbtFeatureConfig> configFactory) {
        super(configFactory);
    }

    private final BlockIgnoreProcessor ignoreStructureVoid = new BlockIgnoreProcessor(ImmutableList.of(Blocks.STRUCTURE_VOID));

    @Override
    public boolean place(FeaturePlaceContext<NbtFeatureConfig> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos position = context.origin();
        NbtFeatureConfig config = context.config();

        if (config.nbtResourcelocationsAndWeights.isEmpty()) {
            return false;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(position);

        int radius = config.solidLandRadius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x * z) > radius && Math.abs(x * z) < radius * 2) {
                    mutable.set(position).move(-x, -1, -z);
                    if (!world.getBlockState(mutable).canOcclude()) {
                        return false;
                    }
                }
            }
        }

        ServerLevel serverLevel = world.getLevel();
        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        ResourceLocation nbtRL = GeneralUtils.getRandomEntry(config.nbtResourcelocationsAndWeights, random);
        StructureTemplate template = templateManager.get(nbtRL).orElse(null);

        if (template == null) {
            UltraAmplifiedDimension.LOGGER.warn("{} NBT does not exist!", config.nbtResourcelocationsAndWeights);
            return false;
        }

        BlockPos halfLengths = new BlockPos(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .addProcessor(ignoreStructureVoid)
                .setRotation(Rotation.getRandom(random))
                .setRotationPivot(halfLengths)
                .setIgnoreEntities(false);
        if (config.processor != null) {
            world.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST)
                    .getHolder(ResourceKey.create(Registries.PROCESSOR_LIST, config.processor))
                    .ifPresent(holder -> holder.value().list().forEach(settings::addProcessor));
        }

        BlockPos placePos = mutable.set(position).move(-halfLengths.getX(), 0, -halfLengths.getZ());
        template.placeInWorld(world, placePos, placePos, settings, random, 2);

        return true;
    }
}
