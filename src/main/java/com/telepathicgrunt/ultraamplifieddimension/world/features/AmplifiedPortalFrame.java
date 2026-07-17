package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.google.common.collect.ImmutableList;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class AmplifiedPortalFrame {
    private static final ResourceLocation PORTAL_RL = ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "auto_generated_portal");

    public boolean generate(WorldGenLevel level, BlockPos pos) {
        ServerLevel serverLevel = level.getLevel();
        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        StructureTemplate template = templateManager.get(PORTAL_RL).orElse(null);
        if (template == null) {
            UltraAmplifiedDimension.LOGGER.warn("{} NBT does not exist!", PORTAL_RL);
            return false;
        }

        BlockPos halfLengths = new BlockPos(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .addProcessor(new BlockIgnoreProcessor(ImmutableList.of(Blocks.STRUCTURE_VOID)))
                .setIgnoreEntities(false)
                .setRotation(Rotation.getRandom(level.getRandom()))
                .setRotationPivot(halfLengths);

        BlockPos placePos = pos.offset(-halfLengths.getX(), 0, -halfLengths.getZ());
        template.placeInWorld(level, placePos, placePos, settings, RandomSource.create(level.getSeed()), 2);
        return true;
    }
}
