package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class SwampCross extends Feature<NoneFeatureConfiguration> {

    public SwampCross(Codec<NoneFeatureConfiguration> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(position).move(Direction.DOWN, 2);

        for (int i = 0; i < 8; i++) {
            level.setBlock(mutable.move(Direction.UP), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
        }

        mutable.move(Direction.DOWN);
        for (int i = -2; i < 3; i++) {
            level.setBlock(mutable.offset(i, 0, 0), Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 18);
        }

        mutable.set(position).move(Direction.DOWN, 2).move(Direction.NORTH, 1);
        if (level.getBlockState(mutable).isFaceSturdy(level, mutable, Direction.UP)
                && level.getBlockState(mutable.above()).isFaceSturdy(level, mutable.above(), Direction.DOWN)) {
            if (random.nextFloat() < 0.1F) {
                level.setBlock(mutable, Blocks.WITHER_SKELETON_WALL_SKULL.defaultBlockState(), 2);
            } else {
                level.setBlock(mutable, Blocks.SKELETON_WALL_SKULL.defaultBlockState(), 2);
            }
        }

        mutable.set(position).move(Direction.DOWN, 3);
        if (level.getBlockState(mutable).isFaceSturdy(level, mutable, Direction.UP) && random.nextBoolean()) {
            level.removeBlock(mutable, false);
            level.setBlock(mutable, Blocks.CHEST.defaultBlockState(), 2);
            RandomizableContainerBlockEntity.setLootTable(level, random, mutable, BuiltInLootTables.SPAWN_BONUS_CHEST);
        }

        return true;
    }
}
