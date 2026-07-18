package com.telepathicgrunt.ultraamplifieddimension.world.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * FOR ELEMENTS USING legacy_single_pool_element AND WANTS AIR TO REPLACE TERRAIN.
 */
public class CeilingVinePostProcessor extends StructureProcessor {

    public static final Codec<CeilingVinePostProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("probability").forGetter(ceilingVinePostProcessor -> ceilingVinePostProcessor.probability),
            BlockState.CODEC.fieldOf("blockstate").forGetter(ceilingVinePostProcessor -> ceilingVinePostProcessor.blockState))
            .apply(instance, CeilingVinePostProcessor::new));

    private final float probability;
    private final BlockState blockState;

    public CeilingVinePostProcessor(float probability, BlockState blockState) {
        this.probability = probability;
        this.blockState = blockState;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos,
            StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld,
            StructurePlaceSettings structurePlacementData) {
        // Place vines only in air space
        if (structureBlockInfoWorld.state().isAir()) {
            RandomSource random = structurePlacementData.getRandom(structureBlockInfoWorld.pos());
            ChunkAccess centerChunk = worldView.getChunk(structureBlockInfoWorld.pos());
            BlockState centerState = centerChunk.getBlockState(structureBlockInfoWorld.pos());
            BlockPos abovePos = structureBlockInfoWorld.pos().above();
            BlockState aboveState = centerChunk.getBlockState(abovePos);

            if (random.nextFloat() < probability
                    && centerState.isAir()
                    && Block.isFaceFull(aboveState.getCollisionShape(worldView, abovePos), Direction.DOWN)) {

                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                List<Direction> shuffledDirectionList = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
                Collections.shuffle(shuffledDirectionList, new java.util.Random(random.nextLong()));
                for (Direction facing : shuffledDirectionList) {
                    mutable.set(structureBlockInfoWorld.pos()).move(facing);
                    BlockState worldState = worldView.getChunk(mutable).getBlockState(mutable);

                    // Vines only get placed if side block is empty and top block is solid.
                    if (!worldState.canOcclude()) {
                        // side block to hold vine
                        worldView.getChunk(mutable).setBlockState(mutable, blockState, false);

                        // ceiling vine
                        BlockState vineBlock = Blocks.VINE.defaultBlockState()
                                .setValue(VineBlock.getPropertyForFace(facing), true)
                                .setValue(VineBlock.UP, true);
                        mutable.move(facing.getOpposite()); // Move back to center
                        centerChunk.setBlockState(mutable, vineBlock, false);

                        // hanging vines
                        vineBlock = vineBlock.setValue(VineBlock.UP, false);
                        for (int depth = random.nextInt(4); depth < 3; depth++) {
                            mutable.move(Direction.DOWN);
                            if (!centerChunk.getBlockState(mutable).isAir()) {
                                break;
                            }
                            centerChunk.setBlockState(mutable, vineBlock, false);
                        }
                        break;
                    }
                }
            }
        }
        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return UADProcessors.CEILING_VINE_POST_PROCESSOR.get();
    }
}
