package com.telepathicgrunt.ultraamplifieddimension.blocks;

import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class RedGlowsandBlock extends SandBlock {
    public RedGlowsandBlock() {
        super(0xA97654, BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(0.5F)
                .sound(SoundType.SAND)
                .lightLevel(state -> 15));
    }
}
