package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.LootTableConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class MarkedTreasureChest extends Feature<LootTableConfig> {

    public MarkedTreasureChest(Codec<LootTableConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<LootTableConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        LootTableConfig config = context.config();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(position);

        if (!level.getBlockState(mutable).isFaceSturdy(level, mutable, Direction.UP)
                || level.getBlockState(mutable.above()).getFluidState().isEmpty()) {
            return false;
        }

        for (Direction face : Direction.values()) {
            if (face == Direction.UP) {
                continue;
            }

            if (!level.getBlockState(mutable.below().relative(face)).isFaceSturdy(level, mutable, Direction.UP)) {
                return false;
            }
        }

        int size = 5;
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                int absx = Math.abs(x);
                int absz = Math.abs(z);

                if (absx == size && absz == size) {
                    continue;
                }

                if (random.nextFloat() < 0.85F && Math.abs(absx - absz) < 2) {
                    level.setBlock(mutable.set(position).move(x, 0, z), Blocks.RED_SAND.defaultBlockState(), 2);
                }
            }
        }

        mutable.set(position).move(Direction.DOWN);
        level.setBlock(mutable, GeneralUtils.orientateChest(level, mutable, Blocks.CHEST.defaultBlockState()), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, mutable, config.lootTable);

        return true;
    }
}
