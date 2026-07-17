package com.telepathicgrunt.ultraamplifieddimension.blocks;

import com.telepathicgrunt.ultraamplifieddimension.modInit.UADBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class GlowgrassBlock extends GrassBlock {
    public GlowgrassBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GRASS)
                .randomTicks()
                .strength(0.5F)
                .sound(SoundType.GRASS)
                .lightLevel(state -> 15));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 3)) {
            return;
        }
        // Covered blocks turn into glowdirt; otherwise defer to vanilla grass spreading.
        if (!level.getBlockState(pos.above()).canBeReplaced()) {
            level.setBlockAndUpdate(pos, UADBlocks.GLOWDIRT.get().defaultBlockState());
            return;
        }
        super.randomTick(state, level, pos, random);
    }
}
