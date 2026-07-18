package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.HeightConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class VinesShort extends Feature<HeightConfig> {

    public VinesShort(Codec<HeightConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<HeightConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        HeightConfig config = context.config();

        int length = 0;
        boolean extendingVine = false;

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(position);
        ChunkAccess chunk = level.getChunk(position);

        while (blockposMutable.getY() > 15 && length < config.height) {
            if (chunk.getBlockState(blockposMutable).isAir()) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockState blockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction), true);
                    if (blockState.canSurvive(level, blockposMutable)) {
                        chunk.setBlockState(blockposMutable, blockState, false);
                        length++;
                        extendingVine = true;
                        break;
                    } else if (extendingVine) {
                        BlockState aboveBlockstate = chunk.getBlockState(blockposMutable.move(Direction.UP));
                        blockposMutable.move(Direction.DOWN);
                        if (aboveBlockstate.is(Blocks.VINE)) {
                            chunk.setBlockState(blockposMutable, aboveBlockstate, false);
                            length++;
                            break;
                        }
                    }
                }
            }
            blockposMutable.move(Direction.DOWN);
        }

        return true;
    }
}
