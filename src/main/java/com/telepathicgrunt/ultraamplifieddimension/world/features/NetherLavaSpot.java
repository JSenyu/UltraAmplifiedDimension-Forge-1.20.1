package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public class NetherLavaSpot extends Feature<NoneFeatureConfiguration> {

    public NetherLavaSpot(Codec<NoneFeatureConfiguration> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockState blockstate = level.getBlockState(pos);
        boolean generateLava = false;
        int solidSurrounding = 0;

        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(mutable.set(pos).move(Direction.DOWN).move(side))
                    .isFaceSturdy(level, mutable, Direction.UP)) {
                ++solidSurrounding;
            }
        }

        if (solidSurrounding < 3) {
            return false;
        }

        if (blockstate.is(Blocks.GRAVEL)) {
            mutable.set(pos).move(Direction.DOWN);
            if (level.getBlockState(mutable).isFaceSturdy(level, mutable, Direction.UP)) {
                generateLava = true;
            }
        } else if (blockstate.is(Blocks.SOUL_SAND)) {
            if (random.nextFloat() < 0.33F) {
                generateLava = true;
            }
        } else if (blockstate.is(Blocks.NETHERRACK)) {
            if (random.nextFloat() < 0.033F) {
                generateLava = true;
            }
        }

        if (generateLava) {
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 2);
            level.scheduleTick(pos, Fluids.LAVA, 0);
        }
        return true;
    }
}
