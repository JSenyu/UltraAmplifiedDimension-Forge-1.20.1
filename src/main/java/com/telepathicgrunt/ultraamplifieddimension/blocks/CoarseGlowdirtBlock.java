package com.telepathicgrunt.ultraamplifieddimension.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class CoarseGlowdirtBlock extends Block {
    public CoarseGlowdirtBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.5F)
                .sound(SoundType.GRAVEL)
                .lightLevel(state -> 15));
    }
}
