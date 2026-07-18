package com.telepathicgrunt.ultraamplifieddimension.world.features.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADTreeDecoratorTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class DiskGroundDecorator extends TreeDecorator {

    public static final Codec<DiskGroundDecorator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("provider").forGetter(config -> config.blockStateProvider),
            Codec.intRange(0, 36).fieldOf("radius").forGetter(config -> config.radius)
    ).apply(instance, DiskGroundDecorator::new));

    private final BlockStateProvider blockStateProvider;
    private final int radius;

    public DiskGroundDecorator(BlockStateProvider blockStateProvider, int radius) {
        this.blockStateProvider = blockStateProvider;
        this.radius = radius;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return UADTreeDecoratorTypes.DISK_GROUND_DECORATOR.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        if (context.logs().isEmpty() || !(context.level() instanceof WorldGenLevel)) {
            return;
        }

        int minY = context.logs().iterator().next().getY();
        context.logs().stream().filter(pos -> pos.getY() == minY).forEach(pos -> this.genBlob(context, pos));
    }

    private void genBlob(TreeDecorator.Context context, BlockPos centerBlockPos) {
        RandomSource random = context.random();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -this.radius; x <= this.radius; ++x) {
            for (int z = -this.radius; z <= this.radius; ++z) {
                if ((x * x) + (z * z) <= (this.radius * this.radius)) {
                    this.setBlobBlock(context, random, mutable.set(centerBlockPos).move(x, 0, z));
                }
            }
        }
    }

    private void setBlobBlock(TreeDecorator.Context context, RandomSource random, BlockPos startBlockPos) {
        WorldGenLevel world = (WorldGenLevel) context.level();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(startBlockPos).move(Direction.UP, 2);
        for (int y = 2; y >= -3; --y) {
            BlockState state = world.getBlockState(mutable);
            if (state.is(BlockTags.DIRT)) {
                world.setBlock(mutable, this.blockStateProvider.getState(random, startBlockPos), 19);
                break;
            }

            if (!state.isAir() && y < 0) {
                break;
            }

            mutable.move(Direction.DOWN);
        }
    }
}
