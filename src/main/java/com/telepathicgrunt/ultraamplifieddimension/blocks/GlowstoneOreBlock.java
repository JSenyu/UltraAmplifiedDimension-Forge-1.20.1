package com.telepathicgrunt.ultraamplifieddimension.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class GlowstoneOreBlock extends Block {
    public GlowstoneOreBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .requiresCorrectToolForDrops()
                .strength(1.3F, 5.8F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 15));
    }
}
