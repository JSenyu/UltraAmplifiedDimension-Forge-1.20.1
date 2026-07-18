package com.telepathicgrunt.ultraamplifieddimension.world.processors;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * FOR ELEMENTS USING legacy_single_pool_element AND WANTS AIR TO REPLACE TERRAIN.
 */
public class WaterloggingFixProcessor extends StructureProcessor {

    public static final Codec<WaterloggingFixProcessor> CODEC = Codec.unit(WaterloggingFixProcessor::new);

    private WaterloggingFixProcessor() { }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos pos, BlockPos pos2,
            StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2,
            StructurePlaceSettings settings) {

        // Workaround for https://bugs.mojang.com/browse/MC-130584
        // Due to a hardcoded field in Templates, any waterloggable blocks in structures replacing water in the world will become waterlogged.
        // Idea of workaround is detect if we are placing a waterloggable block and if so, remove the water in the world instead.
        // ONLY RUN THIS IF STRUCTURE BLOCK IS A DRY WATERLOGGABLE BLOCK
        ChunkPos currentChunkPos = new ChunkPos(infoIn2.pos());
        if (infoIn2.state().hasProperty(BlockStateProperties.WATERLOGGED) && !infoIn2.state().getValue(BlockStateProperties.WATERLOGGED)) {
            ChunkAccess currentChunk = worldReader.getChunk(currentChunkPos.x, currentChunkPos.z);
            if (worldReader.getFluidState(infoIn2.pos()).is(FluidTags.WATER)) {
                currentChunk.setBlockState(infoIn2.pos(), Blocks.STONE.defaultBlockState(), false);
            }

            // Remove water in adjacent blocks across chunk boundaries and above/below as well
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (Direction direction : Direction.values()) {
                mutable.set(infoIn2.pos()).move(direction);
                if (currentChunkPos.x != mutable.getX() >> 4 || currentChunkPos.z != mutable.getZ() >> 4) {
                    currentChunk = worldReader.getChunk(mutable);
                    currentChunkPos = new ChunkPos(mutable);
                }

                if (currentChunk.getFluidState(mutable).is(FluidTags.WATER)) {
                    currentChunk.setBlockState(mutable, Blocks.STONE.defaultBlockState(), false);
                }
            }
        }

        return infoIn2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return UADProcessors.WATER_FIX_PROCESSOR.get();
    }
}
