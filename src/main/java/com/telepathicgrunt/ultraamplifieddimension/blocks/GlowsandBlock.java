package com.telepathicgrunt.ultraamplifieddimension.blocks;

import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class GlowsandBlock extends SandBlock {
    public GlowsandBlock() {
        super(0xDBD3A0, BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.5F)
                .sound(SoundType.SAND)
                .lightLevel(state -> 15));
    }
}
