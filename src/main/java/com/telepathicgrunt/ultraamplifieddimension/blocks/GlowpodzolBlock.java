package com.telepathicgrunt.ultraamplifieddimension.blocks;

import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class GlowpodzolBlock extends SnowyDirtBlock {
    public GlowpodzolBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PODZOL)
                .strength(0.5F)
                .sound(SoundType.GRAVEL)
                .lightLevel(state -> 15));
    }
}
