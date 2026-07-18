package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeSwampHorned extends Feature<TreeConfiguration> {

    public TreeSwampHorned(Codec<TreeConfiguration> config) {
        super(config);
    }

    @Override
    public boolean place(FeaturePlaceContext<TreeConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos blockPos = context.origin();
        TreeConfiguration config = context.config();

        Set<BlockPos> logPositions = new HashSet<>();
        Set<BlockPos> leavesPositions = new HashSet<>();

        if (!this.generate(world, context.chunkGenerator(), random, blockPos, logPositions, leavesPositions, config)) {
            return false;
        }

        if (!config.decorators.isEmpty() && !logPositions.isEmpty()) {
            TreeDecorator.Context decoratorContext = new TreeDecorator.Context(
                    world,
                    (pos, state) -> world.setBlock(pos, state, 19),
                    random,
                    logPositions,
                    leavesPositions,
                    new java.util.HashSet<>()
            );
            for (TreeDecorator decorator : config.decorators) {
                decorator.place(decoratorContext);
            }
        }

        return true;
    }

    private boolean generate(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos position, Set<BlockPos> logPositions, Set<BlockPos> leavesPositions, TreeConfiguration config) {
        int height = config.trunkPlacer.getTreeHeight(random);

        if (!this.isSpaceAt(world, chunkGenerator, position, height)) {
            return false;
        }

        if (world.getBlockState(position.below()).getFluidState().is(FluidTags.WATER)) {
            position = position.below();
        }

        if (position.getY() < world.getMinBuildHeight() || position.getY() + height + 1 > world.getMaxBuildHeight()) {
            return false;
        }

        for (int y = position.getY(); y <= position.getY() + 1 + height; ++y) {
            int radius = 1;

            if (y == position.getY()) {
                radius = 0;
            }

            if (y >= position.getY() + 1 + height - 2) {
                radius = 3;
            }

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int x = position.getX() - radius; x <= position.getX() + radius; ++x) {
                for (int z = position.getZ() - radius; z <= position.getZ() + radius; ++z) {
                    if (y < world.getMinBuildHeight() || y >= world.getMaxBuildHeight()) {
                        return false;
                    }

                    mutable.set(x, y, z);
                    if (!isAirOrLeaves(world, mutable)) {
                        if (y > position.getY() && !isWater(world, mutable)) {
                            return false;
                        }
                    }
                }
            }
        }

        if (!isDirtOrGrass(world, position.below()) || position.getY() >= world.getMaxBuildHeight() - height - 1) {
            return false;
        }

        for (int currentHeight = position.getY() - 4 + height; currentHeight <= position.getY() + height; ++currentHeight) {
            int heightDiff = currentHeight - (position.getY() + height);
            int leavesWidth = 2 - heightDiff / 2;

            for (int x = position.getX() - leavesWidth - 1; x <= position.getX() + leavesWidth; ++x) {
                int xPos = x - position.getX();

                for (int z = position.getZ() - leavesWidth - 1; z <= position.getZ() + leavesWidth; ++z) {
                    int zPos = z - position.getZ();
                    int cornerCount = 0;

                    if (xPos == leavesWidth) {
                        cornerCount++;
                    }
                    if (zPos == leavesWidth) {
                        cornerCount++;
                    }
                    if (xPos == -leavesWidth - 1) {
                        cornerCount++;
                    }
                    if (zPos == -leavesWidth - 1) {
                        cornerCount++;
                    }

                    if (cornerCount == 2 || random.nextInt(3) < 2 && heightDiff != 0) {
                        BlockPos leafPos = new BlockPos(x, currentHeight, z);

                        if (isAirOrLeaves(world, leafPos) || isReplaceablePlant(world, leafPos)) {
                            world.setBlock(leafPos, config.foliageProvider.getState(random, leafPos), 2);
                            leavesPositions.add(leafPos);
                        }
                    }
                }
            }
        }

        this.genTrunk(world, position, height, random, logPositions, leavesPositions, config);
        this.genTrunk(world, position.west(), height, random, logPositions, leavesPositions, config);
        this.genTrunk(world, position.north(), height, random, logPositions, leavesPositions, config);
        this.genTrunk(world, position.west().north(), height, random, logPositions, leavesPositions, config);

        return true;
    }

    private void genTrunk(WorldGenLevel world, BlockPos position, int height, RandomSource random, Set<BlockPos> logPositions, Set<BlockPos> leavesPositions, TreeConfiguration config) {
        world.setBlock(position.below(), Blocks.DIRT.defaultBlockState(), 2);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(position);

        for (int currentHeight = 0; currentHeight < height; currentHeight++) {
            BlockState currentState = world.getBlockState(mutable);

            if (!currentState.canOcclude()) {
                if (currentHeight != height - 1) {
                    world.setBlock(mutable, config.trunkProvider.getState(random, mutable), 2);
                    logPositions.add(mutable.immutable());
                } else {
                    BlockPos topPos = mutable.immutable();
                    world.setBlock(mutable, config.foliageProvider.getState(random, mutable), 2);
                    leavesPositions.add(topPos);
                }
            }

            mutable.move(Direction.UP);
        }
    }

    private boolean isSpaceAt(LevelReader world, ChunkGenerator chunkGenerator, BlockPos leavesPos, int height) {
        if (leavesPos.getY() < world.getMinBuildHeight() || leavesPos.getY() + height + 1 > world.getMaxBuildHeight()) {
            return false;
        }

        for (int y = 0; y <= 1 + height; ++y) {
            int radius = y == 0 ? 1 : 2;

            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    int checkY = leavesPos.getY() + y;
                    if (checkY < world.getMinBuildHeight() || checkY >= world.getMaxBuildHeight() || !canTreeReplace(world, leavesPos.offset(x, y, z))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean canTreeReplace(LevelReader world, BlockPos pos) {
        return canReplace(world, pos) || world.getBlockState(pos).is(BlockTags.LOGS);
    }

    private static boolean isWater(LevelReader world, BlockPos pos) {
        return world.getBlockState(pos).is(Blocks.WATER);
    }

    public static boolean isAirOrLeaves(LevelReader world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.LEAVES);
    }

    private static boolean isDirtOrGrass(LevelReader world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.FARMLAND);
    }

    private static boolean isReplaceablePlant(LevelReader world, BlockPos pos) {
        return world.getBlockState(pos).is(BlockTags.REPLACEABLE_BY_TREES);
    }

    public static boolean canReplace(LevelReader world, BlockPos pos) {
        return isAirOrLeaves(world, pos) || isReplaceablePlant(world, pos) || isWater(world, pos);
    }
}
