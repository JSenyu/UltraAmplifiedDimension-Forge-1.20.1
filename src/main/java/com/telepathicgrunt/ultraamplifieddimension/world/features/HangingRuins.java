package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class HangingRuins extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation HANGING_RUINS_RL = ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "hanging_ruins");

    public HangingRuins(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos position = context.origin();

        if (position.getY() < world.getSeaLevel() + 5) {
            return false;
        }

        BlockPos.MutableBlockPos mutableMain = new BlockPos.MutableBlockPos().set(position);
        BlockPos.MutableBlockPos mutableTemp = new BlockPos.MutableBlockPos();

        var currentBlock = world.getBlockState(mutableMain);
        while ((currentBlock.is(BlockTags.LOGS) || !currentBlock.canOcclude()) &&
                mutableMain.getY() < world.getMaxBuildHeight()) {
            mutableMain.move(net.minecraft.core.Direction.UP);
            currentBlock = world.getBlockState(mutableMain);
        }

        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                if (Math.abs(x * z) > 9 && Math.abs(x * z) < 20) {
                    mutableTemp.set(mutableMain).move(x, 1, z);
                    if (!world.getBlockState(mutableTemp).canOcclude()) {
                        return false;
                    }
                }
            }
        }

        if (shouldMoveDownOne(world, mutableMain)) {
            mutableMain.move(net.minecraft.core.Direction.DOWN);
        }

        StructureTemplateManager templateManager = world.getLevel().getStructureManager();
        StructureTemplate template = templateManager.get(HANGING_RUINS_RL).orElse(null);

        if (template == null) {
            UltraAmplifiedDimension.LOGGER.warn("hanging ruins NBT does not exist!");
            return false;
        }

        if (mutableMain.getY() == world.getMaxBuildHeight() ||
                !world.getBlockState(mutableMain.below(template.getSize().getY())).isAir() ||
                !world.getBlockState(mutableMain.below(template.getSize().getY() + 5)).isAir()) {
            return false;
        }

        BlockPos halfLengths = new BlockPos(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2);
        StructurePlaceSettings placementSettings = new StructurePlaceSettings()
                .setRotationPivot(halfLengths)
                .setRotation(Rotation.getRandom(random))
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false);

        BlockPos placePos = mutableMain.move(-halfLengths.getX(), -8, -halfLengths.getZ());
        template.placeInWorld(world, placePos, placePos, placementSettings, random, 2);
        return true;
    }

    private boolean shouldMoveDownOne(LevelAccessor world, BlockPos.MutableBlockPos mutableMain) {
        BlockPos.MutableBlockPos mutableTemp = new BlockPos.MutableBlockPos();
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                mutableTemp.set(mutableMain).move(x, 2, z);
                if (Math.abs(x * z) < 20 && !world.getBlockState(mutableTemp).canOcclude()) {
                    return true;
                }
            }
        }
        return false;
    }
}
