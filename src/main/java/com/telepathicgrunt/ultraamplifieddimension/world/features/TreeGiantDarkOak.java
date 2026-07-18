package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class TreeGiantDarkOak extends Feature<TreeConfiguration> {
    public TreeGiantDarkOak(Codec<TreeConfiguration> config) {
        super(config);
    }

    @Override
    public boolean place(FeaturePlaceContext<TreeConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos pos = context.origin();
        TreeConfiguration config = context.config();

        // Decorators need tracked log/leaf positions; this feature places without tracking.
        // Skip decorator pass rather than calling them with empty sets (crashes CocoaDecorator).
        return this.placeTree(world, context.chunkGenerator(), random, pos, config);
    }

    private boolean placeTree(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos position, TreeConfiguration config) {
        int height = 11 + random.nextInt(3);

        if (!this.isSpaceAt(world, chunkGenerator, position, height + 4)) {
            return false;
        }

        BlockPos soilPos = position.below();
        if (!Feature.isGrassOrDirt(world, soilPos)) {
            return false;
        }

        if (!this.placeTreeOfHeight(world, position, height)) {
            return false;
        }

        BlockState trunk = config.trunkProvider.getState(random, position);
        BlockState leaves = config.foliageProvider.getState(random, position).setValue(LeavesBlock.DISTANCE, 1);

        for (int x = -1; x < 3; x++) {
            for (int z = -1; z < 3; z++) {
                if (x + z != -2 && x * z != -2 && x + z != 4) {
                    world.setBlock(soilPos.offset(x, 0, z), Blocks.DIRT.defaultBlockState(), 2);
                }
            }
        }

        this.createCrown(world, position.getX(), position.getZ(), position.getY() + height, leaves);
        this.createWoodCrown(world, position.getX(), position.getZ(), position.getY() + height, trunk);

        int yMax = height + position.getY();
        if (position.getY() > 3) {
            position = position.below(2);
        }

        this.placeColumnOfWood(world, yMax, random, position, trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(1, 0, 0), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(1, 0, 1), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(0, 0, 1), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(-1, 0, 0), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(0, 0, -1), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(-1, 0, 1), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(1, 0, -1), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(0, 0, 2), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(1, 0, 2), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(2, 0, 0), trunk, leaves);
        this.placeColumnOfWood(world, yMax, random, position.offset(2, 0, 1), trunk, leaves);

        return true;
    }

    private void createCrown(LevelAccessor world, int x, int z, int y, BlockState leaves) {
        int crownHeight = 4;

        for (int k = y - crownHeight; k <= y + 3; ++k) {
            int layer = y - k;
            int radius = Mth.floor((float) layer / (float) crownHeight * 1.5F);
            this.growLeavesLayerStrict(world, new BlockPos(x, k, z), radius + (int) ((layer > 0 && (k & 1) == 0 ? 0.9 : 1) * 5.5), leaves);
        }

        this.growLeavesLayerStrict(world, new BlockPos(x, y + 4, z), 1, leaves);
    }

    private void createWoodCrown(LevelAccessor world, int x, int z, int y, BlockState trunk) {
        int crownHeight = 2;

        for (int k = y - (crownHeight + 4); k <= y - 1; ++k) {
            int layer = y - k;
            int radius;

            if (layer < 3) {
                radius = 4;
            } else if (layer < 5) {
                radius = 3;
            } else {
                radius = 2;
            }

            this.growWoodLayerStrict(world, new BlockPos(x, k, z), radius, trunk);
        }
    }

    private void createMiniCrown(LevelAccessor world, int x, int z, int y, RandomSource random, BlockState leaves) {
        int crownHeight = random.nextInt(2) + 1;

        for (int k = y - crownHeight; k <= y + 1; ++k) {
            int layer = y - k;
            int radius = Mth.floor((float) layer / (float) crownHeight * 1.5F);
            this.growLeavesLayerStrict(world, new BlockPos(x, k, z), radius + (int) ((layer > 0 && (k & 1) == 0 ? 0.9 : 1) * 2), leaves);
        }
    }

    protected void growLeavesLayerStrict(LevelAccessor world, BlockPos layerCenter, int width, BlockState leaves) {
        int radiusSquared = width * width;

        for (int j = -width; j <= width + 1; ++j) {
            for (int k = -width; k <= width + 1; ++k) {
                int l = j - 1;
                int i1 = k - 1;

                if (j * j + k * k <= radiusSquared || l * l + i1 * i1 <= radiusSquared || j * j + i1 * i1 <= radiusSquared || l * l + k * k <= radiusSquared) {
                    BlockPos blockPos = layerCenter.offset(j, 0, k);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir() || state.is(BlockTags.LEAVES)) {
                        world.setBlock(blockPos, leaves, 2);
                    }
                }
            }
        }
    }

    protected void growWoodLayerStrict(LevelAccessor world, BlockPos layerCenter, int width, BlockState trunk) {
        int radiusSquared = width * width;

        for (int j = -width; j <= width + 1; ++j) {
            for (int k = -width; k <= width + 1; ++k) {
                int l = j - 1;
                int i1 = k - 1;

                if (j * j + k * k <= radiusSquared || l * l + i1 * i1 <= radiusSquared || j * j + i1 * i1 <= radiusSquared || l * l + k * k <= radiusSquared) {
                    BlockPos blockPos = layerCenter.offset(j, 0, k);
                    BlockState state = world.getBlockState(blockPos);

                    if (state.isAir() || state.is(BlockTags.LEAVES)) {
                        world.setBlock(blockPos, trunk, 2);
                    }
                }
            }
        }
    }

    private void placeColumnOfWood(LevelAccessor world, int yMax, RandomSource random, BlockPos tempPos, BlockState trunk, BlockState leaves) {
        while (tempPos.getY() < yMax) {
            tempPos = tempPos.above();
            BlockState currentState = world.getBlockState(tempPos);

            if (currentState.isAir() || currentState.is(BlockTags.LEAVES)) {
                if (random.nextInt(70) == 0) {
                    this.createMiniCrown(world, tempPos.getX(), tempPos.getZ(), tempPos.getY(), random, leaves);
                } else {
                    world.setBlock(tempPos, trunk, 2);
                }
            }
        }
    }

    private boolean placeTreeOfHeight(LevelReader world, BlockPos pos, int height) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int layer = 0; layer <= height + 1; ++layer) {
            int radius = 1;
            if (layer == 0) {
                radius = 0;
            }

            if (layer >= height - 1) {
                radius = 2;
            }

            for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
                for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
                    if (cannotBeReplacedByLogs(world, mutable.set(x + xOffset, y + layer, z + zOffset))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean isSpaceAt(LevelReader world, ChunkGenerator chunkGenerator, BlockPos leavesPos, int height) {
        if (leavesPos.getY() < world.getMinBuildHeight() || leavesPos.getY() + height + 1 > world.getMaxBuildHeight()) {
            return false;
        }

        for (int layer = 0; layer <= 1 + height; ++layer) {
            int radius = 2;
            if (layer == 0) {
                radius = 1;
            } else if (layer >= 1 + height - 2) {
                radius = 2;
            }

            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    int y = leavesPos.getY() + layer;
                    if (y < world.getMinBuildHeight() || y >= world.getMaxBuildHeight() || cannotBeReplacedByLogs(world, leavesPos.offset(x, layer, z))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    protected static boolean cannotBeReplacedByLogs(LevelReader reader, BlockPos pos) {
        BlockState state = reader.getBlockState(pos);
        return !state.isAir()
                && !state.is(BlockTags.LEAVES)
                && !state.is(BlockTags.DIRT)
                && !state.is(Blocks.GRASS_BLOCK)
                && !state.is(Blocks.PODZOL)
                && !state.is(Blocks.MYCELIUM)
                && !state.is(BlockTags.LOGS)
                && !state.is(BlockTags.SAPLINGS)
                && state.getBlock() != Blocks.VINE;
    }
}
