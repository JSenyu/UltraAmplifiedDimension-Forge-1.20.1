package com.telepathicgrunt.ultraamplifieddimension.world.processors;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * For removing stuff like floating tall grass or kelp
 */
public class RemoveFloatingBlocksProcessor extends StructureProcessor {

    public static final Codec<RemoveFloatingBlocksProcessor> CODEC = Codec.unit(RemoveFloatingBlocksProcessor::new);

    private RemoveFloatingBlocksProcessor() { }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos,
            StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld,
            StructurePlaceSettings structurePlacementData) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(structureBlockInfoWorld.pos());
        ChunkAccess cachedChunk = worldView.getChunk(mutable);

        // attempts to remove invalid floating plants
        if (structureBlockInfoWorld.state().isAir() || structureBlockInfoWorld.state().getBlock() instanceof LiquidBlock) {
            // set the block in the world so that canSurvive's result changes
            cachedChunk.setBlockState(mutable, structureBlockInfoWorld.state(), false);
            cachedChunk.removeBlockEntity(mutable);
            BlockState aboveWorldState = worldView.getBlockState(mutable.move(Direction.UP));

            // detects the first invalidly placed block before going into a while loop
            if (!aboveWorldState.canSurvive(worldView, mutable)) {
                cachedChunk.setBlockState(mutable, structureBlockInfoWorld.state(), false);
                cachedChunk.removeBlockEntity(mutable);
                aboveWorldState = worldView.getBlockState(mutable.move(Direction.UP));

                while (mutable.getY() < worldView.getMaxBuildHeight() && !aboveWorldState.canSurvive(worldView, mutable)) {
                    cachedChunk.setBlockState(mutable, structureBlockInfoWorld.state(), false);
                    cachedChunk.removeBlockEntity(mutable);
                    aboveWorldState = worldView.getBlockState(mutable.move(Direction.UP));
                }
            }
        }

        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return UADProcessors.REMOVE_FLOATING_BLOCKS_PROCESSOR.get();
    }
}
