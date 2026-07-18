package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class IcebergWithoutAir extends Feature<BlockStateConfiguration> {

    public IcebergWithoutAir(Codec<BlockStateConfiguration> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos position = context.origin();
        BlockStateConfiguration icebergConfig = context.config();

        boolean flag = random.nextDouble() > 0.7D;
        BlockState iblockstate = icebergConfig.state;
        double d0 = random.nextDouble() * 2.0D * Math.PI;
        int i = 11 - random.nextInt(5);
        int j = 3 + random.nextInt(3);
        boolean flag1 = random.nextDouble() > 0.7D;
        int upperHeight = flag1 ? random.nextInt(6) + 6 : random.nextInt(15) + 3;
        if (!flag1 && random.nextDouble() > 0.9D) {
            upperHeight += random.nextInt(19) + 7;
        }

        int downHeight = Math.min(upperHeight + random.nextInt(11), 18);
        int maxheight = Math.min(upperHeight + random.nextInt(7) - random.nextInt(5), 11);
        int radius = flag1 ? i : 11;

        for (int x = -radius; x < radius; ++x) {
            for (int z = -radius; z < radius; ++z) {
                for (int y = 0; y < upperHeight; ++y) {
                    int k2 = flag1 ? this.func_205178_b(y, upperHeight, maxheight) : this.func_205183_a(random, y, upperHeight, maxheight);
                    if (flag1 || x < k2) {
                        this.func_205181_a(world, random, position, upperHeight, x, y, z, k2, radius, flag1, j, d0, flag, iblockstate);
                    }
                }
            }
        }

        this.func_205186_a(world, position, maxheight, upperHeight, flag1, i);

        for (int x = -radius; x < radius; ++x) {
            for (int z = -radius; z < radius; ++z) {
                for (int y = -1; y > -downHeight; --y) {
                    int l3 = flag1 ? Mth.ceil(radius * (1.0F - (float) Math.pow(y, 2.0D) / (downHeight * 8.0F))) : radius;
                    int l2 = this.func_205187_b(random, -y, downHeight, maxheight);
                    if (x < l2) {
                        this.func_205181_a(world, random, position, downHeight, x, y, z, l2, l3, flag1, j, d0, flag, iblockstate);
                    }
                }
            }
        }

        boolean flag2 = flag1 ? random.nextDouble() > 0.1D : random.nextDouble() > 0.7D;
        if (flag2) {
            this.func_205184_a(random, world, maxheight, upperHeight, position, flag1, i, d0, j);
        }

        return true;
    }

    private void func_205184_a(RandomSource random, LevelAccessor world, int maxheight, int height, BlockPos position, boolean flag, int int1, double double1, int int2) {
        int x = random.nextBoolean() ? -1 : 1;
        int z = random.nextBoolean() ? -1 : 1;
        int randomHeightBasedMultiplier = random.nextInt(Math.max(maxheight / 2 - 2, 1));
        if (random.nextBoolean()) {
            randomHeightBasedMultiplier = maxheight / 2 + 1 - random.nextInt(Math.max(maxheight - maxheight / 2 - 1, 1));
        }

        int l = random.nextInt(Math.max(maxheight / 2 - 2, 1));
        if (random.nextBoolean()) {
            l = maxheight / 2 + 1 - random.nextInt(Math.max(maxheight - maxheight / 2 - 1, 1));
        }

        if (flag) {
            randomHeightBasedMultiplier = l = random.nextInt(Math.max(int1 - 5, 1));
        }

        BlockPos blockpos = new BlockPos(x * randomHeightBasedMultiplier, 0, z * l);
        double double2 = flag ? double1 + (Math.PI / 2D) : random.nextDouble() * 2.0D * Math.PI;

        for (int currentHeight = 0; currentHeight < height - 3; ++currentHeight) {
            int heightThickness = this.func_205183_a(random, currentHeight, height, maxheight);
            this.func_205174_a(heightThickness, currentHeight, position, world, false, double2, blockpos, int1, int2);
        }

        for (int belowCenterY = -1; belowCenterY > -height + random.nextInt(5); --belowCenterY) {
            int heightThickness = this.func_205187_b(random, -belowCenterY, height, maxheight);
            this.func_205174_a(heightThickness, belowCenterY, position, world, true, double2, blockpos, int1, int2);
        }
    }

    private void func_205174_a(int heightThickness, int belowCenterY, BlockPos position, LevelAccessor world, boolean placeWater, double double1, BlockPos pos2, int int1, int int2) {
        int radius = heightThickness + 1 + int1 / 3;
        int int3 = Math.min(heightThickness - 3, 3) + int2 / 2 - 1;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(position);
        BlockState blockState;

        for (int x = -radius; x < radius; ++x) {
            for (int z = -radius; z < radius; ++z) {
                double d0 = this.func_205180_a(x, z, pos2, radius, int3, double1);
                if (d0 < 0.0D) {
                    mutable.set(position).move(x, belowCenterY, z);
                    blockState = world.getBlockState(mutable);
                    if (blockState.is(BlockTags.ICE) || blockState.is(Blocks.SNOW_BLOCK)) {
                        this.setBlock(world, mutable, appropriateBlockForNeighbors(world, mutable));
                        if (!placeWater) {
                            this.removeSnowLayer(world, mutable);
                        }
                    }
                }
            }
        }
    }

    private void removeSnowLayer(LevelAccessor world, BlockPos.MutableBlockPos mutableBlockPos) {
        if (world.getBlockState(mutableBlockPos.move(Direction.UP)).is(Blocks.SNOW)) {
            this.setBlock(world, mutableBlockPos, appropriateBlockForNeighbors(world, mutableBlockPos.below()));
        }
        mutableBlockPos.move(Direction.DOWN);
    }

    private void func_205181_a(LevelAccessor world, RandomSource random, BlockPos position, int maxHeight, int xPos, int yPos, int zPos, int int1, int int2, boolean flag1, int int3, double double1, boolean flag2, BlockState defaultState) {
        double noise = flag1 ? this.func_205180_a(xPos, zPos, BlockPos.ZERO, int2, this.func_205176_a(yPos, maxHeight, int3), double1) : this.func_205177_a(xPos, zPos, BlockPos.ZERO, int1, random);
        if (noise < 0.0D) {
            BlockPos blockpos1 = position.offset(xPos, yPos, zPos);
            double randomizer = flag1 ? -0.5D : -6 - random.nextInt(3);
            if (noise > randomizer && random.nextDouble() > 0.9D) {
                return;
            }

            this.func_205175_a(blockpos1, world, random, maxHeight - yPos, maxHeight, flag1, flag2, defaultState);
        }
    }

    private void func_205175_a(BlockPos position, LevelAccessor world, RandomSource random, int minHeight, int maxHeight, boolean flag1, boolean flag2, BlockState defaultState) {
        BlockState currentBlockState = world.getBlockState(position);

        if (currentBlockState.isAir() || currentBlockState.is(Blocks.SNOW_BLOCK) || currentBlockState.is(BlockTags.ICE) || !currentBlockState.getFluidState().isEmpty()) {
            boolean flag = !flag1 || random.nextDouble() > 0.05D;
            int i = flag1 ? 3 : 2;
            if (flag2 && currentBlockState.getFluidState().isEmpty() && minHeight <= random.nextInt(Math.max(1, maxHeight / i)) + maxHeight * 0.6D && flag) {
                this.setBlock(world, position, Blocks.SNOW_BLOCK.defaultBlockState());
            } else {
                this.setBlock(world, position, defaultState);
            }
        }
    }

    private BlockState appropriateBlockForNeighbors(LevelAccessor world, BlockPos position) {
        boolean bordersWater = false;
        boolean bordersAir = false;
        BlockState blockState;

        for (Direction face : Direction.values()) {
            if (face == Direction.UP) {
                continue;
            }

            blockState = world.getBlockState(position.relative(face));
            if (!blockState.is(BlockTags.ICE)) {
                if (blockState.isAir()) {
                    bordersAir = true;
                } else if (!blockState.getFluidState().isEmpty()) {
                    bordersWater = true;
                }
            }
        }

        if (bordersWater && bordersAir) {
            return Blocks.STONE.defaultBlockState();
        } else if (bordersWater) {
            return Blocks.WATER.defaultBlockState();
        } else if (bordersAir) {
            return Blocks.AIR.defaultBlockState();
        } else {
            return Blocks.PACKED_ICE.defaultBlockState();
        }
    }

    private void func_205186_a(LevelAccessor world, BlockPos position, int smallRadiusIn, int height, boolean flag, int largeRadiusIn) {
        int radius = flag ? largeRadiusIn : smallRadiusIn / 2;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(position);

        for (int x = -radius; x <= radius; ++x) {
            for (int z = -radius; z <= radius; ++z) {
                for (int y = 0; y <= height; ++y) {
                    mutable.set(position).move(x, y, z);
                    BlockState blockState = world.getBlockState(mutable);
                    if (blockState.is(BlockTags.ICE) || blockState.is(Blocks.SNOW)) {
                        if (isAirBelow(world, mutable)) {
                            this.setBlock(world, mutable, appropriateBlockForNeighbors(world, mutable));
                            this.setBlock(world, mutable.move(Direction.UP), appropriateBlockForNeighbors(world, mutable));
                        } else if (blockState.is(BlockTags.ICE)) {
                            int notIceCounter = 0;

                            for (Direction direction : Direction.Plane.HORIZONTAL) {
                                BlockState neighboringState = world.getBlockState(mutable.move(direction));
                                if (!neighboringState.is(BlockTags.ICE)) {
                                    ++notIceCounter;
                                }
                                mutable.move(direction.getOpposite());
                            }

                            if (notIceCounter >= 3) {
                                this.setBlock(world, mutable, appropriateBlockForNeighbors(world, mutable));
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isAirBelow(BlockGetter worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.below()).isAir();
    }

    public int func_205178_b(int y, int heightDiff, int thinnestWidth) {
        float f1 = (1.0F - (float) Math.pow(y, 2.0D) / ((float) heightDiff * 1.0F)) * thinnestWidth;
        return Mth.ceil(f1 / 2.0F);
    }

    public int func_205183_a(RandomSource rand, int y, int heightDiff, int thinnestWidth) {
        float f = 3.5F - rand.nextFloat() * 0.1f;
        float f1 = (1.0F - (float) Math.pow(y, 2.0D) / ((float) heightDiff * f)) * thinnestWidth;
        if (heightDiff > 15) {
            int i = y < 3 ? y / 2 : y;
            f1 = (1.0F - (float) i / ((float) heightDiff * f * 0.4F)) * thinnestWidth;
        }

        return Mth.ceil(f1 / 2.0F);
    }

    public int func_205187_b(RandomSource rand, int y, int heightDiff, int thinnestWidth) {
        float f = 1.0F + rand.nextFloat() / 2.0F;
        float f1 = (1.0F - (float) y / ((float) heightDiff * f)) * thinnestWidth;
        return Mth.ceil(f1 / 2.0F);
    }

    public double func_205180_a(int xIn, int zIn, BlockPos pos, int radiusX, int radiusZ, double angle) {
        return Math.pow(((double) (xIn - pos.getX()) * Math.cos(angle) - (double) (zIn - pos.getZ()) * Math.sin(angle)) / radiusX, 2.0D) + Math.pow(((double) (xIn - pos.getX()) * Math.sin(angle) + (double) (zIn - pos.getZ()) * Math.cos(angle)) / radiusZ, 2.0D) - 1.0D;
    }

    public int func_205176_a(int y, int heightDiff, int thinnestWidth) {
        int i = thinnestWidth;
        if (y > 0 && heightDiff - y <= 3) {
            i = thinnestWidth - (4 - (heightDiff - y));
        }

        return i;
    }

    public double func_205177_a(int x, int z, BlockPos pos, int radius, RandomSource random) {
        float f = 10.0F * Mth.clamp(random.nextFloat(), 0.2F, 0.8F) / radius;
        return f + Math.pow(x - pos.getX(), 2.0D) + Math.pow(z - pos.getZ(), 2.0D) - Math.pow(radius, 2.0D);
    }
}
