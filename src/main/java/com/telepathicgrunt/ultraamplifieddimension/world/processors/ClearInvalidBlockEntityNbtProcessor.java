package com.telepathicgrunt.ultraamplifieddimension.world.processors;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Drops NBT when the final block cannot have a block entity.
 * Vanilla RuleProcessor keeps input NBT unless output_nbt is set, which can leave
 * chest/spawner data on ice after rule replacements.
 */
public class ClearInvalidBlockEntityNbtProcessor extends StructureProcessor {

    public static final ClearInvalidBlockEntityNbtProcessor INSTANCE = new ClearInvalidBlockEntityNbtProcessor();
    public static final Codec<ClearInvalidBlockEntityNbtProcessor> CODEC = Codec.unit(() -> INSTANCE);

    private ClearInvalidBlockEntityNbtProcessor() { }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos,
            StructureTemplate.StructureBlockInfo structureBlockInfoLocal,
            StructureTemplate.StructureBlockInfo structureBlockInfoWorld,
            StructurePlaceSettings structurePlacementData) {
        if (structureBlockInfoWorld.nbt() != null && !structureBlockInfoWorld.state().hasBlockEntity()) {
            return new StructureTemplate.StructureBlockInfo(
                    structureBlockInfoWorld.pos(),
                    structureBlockInfoWorld.state(),
                    null);
        }
        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return UADProcessors.CLEAR_INVALID_BLOCK_ENTITY_NBT_PROCESSOR.get();
    }
}
