package com.telepathicgrunt.ultraamplifieddimension.world.carver;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.world.carver.configs.RavineConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class RavineCarver extends WorldCarver<RavineConfig> {
    private static final int CARVER_RANGE = 255;

    private final float[] wallLedges = new float[1024];
    private static final Set<Block> CARVABLE_BLOCKS = createCarvableBlocks();

    public RavineCarver(Codec<RavineConfig> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(RavineConfig config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }

    @Override
    public boolean carve(CarvingContext context, RavineConfig config, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor, RandomSource random, Aquifer aquifer, ChunkPos chunkPos, CarvingMask mask) {
        ChunkPos writingChunk = chunk.getPos();
        int i = (CARVER_RANGE * 2 - 1) * 16;
        double xpos = chunkPos.getMinBlockX() + random.nextInt(16);
        double height = config.sampleHeightPlacement(random);
        double zpos = chunkPos.getMinBlockZ() + random.nextInt(16);
        float xzNoise2 = random.nextFloat() * ((float) Math.PI * 2.0F);
        float xzCosNoise = (random.nextFloat() - 0.5F) / 8.0F;
        float widthHeightBase = (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
        int maxIteration = i - random.nextInt(i / 4);
        this.doCarve(context, config, chunk, biomeAccessor, random.nextLong(), writingChunk.x, writingChunk.z, xpos, height, zpos, widthHeightBase, xzNoise2, xzCosNoise, maxIteration, config.sampleTallness(random) / 10.0D, mask, aquifer);
        return true;
    }

    private void doCarve(CarvingContext context, RavineConfig config, ChunkAccess world, Function<BlockPos, Holder<Biome>> biomeAccessor, long randomSeed, int mainChunkX, int mainChunkZ, double randomBlockX, double randomBlockY, double randomBlockZ, float widthHeightBase, float xzNoise2, float xzCosNoise, int maxIteration, double heightMultiplier, CarvingMask mask, Aquifer aquifer) {
        RandomSource random = RandomSource.create(randomSeed);
        float f = 1.0F;

        for (int i = 0; i < config.cutoffHeight; ++i) {
            if (i == 0 || random.nextInt(3) == 0) {
                f = 1.0F + random.nextFloat() * random.nextFloat();
            }

            this.wallLedges[i] = f * f;
        }

        float f4 = 0.0F;
        float f1 = 0.0F;

        for (int j = 0; j < maxIteration; ++j) {
            double placementXZBound = 2.0D + Mth.sin(j * (float) Math.PI / maxIteration) * widthHeightBase;
            double placementYBound = placementXZBound * heightMultiplier;
            placementXZBound *= (random.nextFloat() * 0.15D + 0.65D);
            placementYBound *= 0.8D;
            float f2 = Mth.cos(xzCosNoise);
            randomBlockX += Mth.cos(xzNoise2) * f2;
            randomBlockZ += Mth.sin(xzNoise2) * f2;
            xzCosNoise = xzCosNoise * 0.8F + f1 * 0.08F;
            xzNoise2 += f4 * 0.1F;
            f1 = f1 * 0.8F + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 1.5F;
            f4 = f4 * 0.5F + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 3.0F;

            if (random.nextInt(4) != 0) {
                if (!canReach(new ChunkPos(mainChunkX, mainChunkZ), randomBlockX, randomBlockZ, j, maxIteration, widthHeightBase)) {
                    return;
                }

                this.carveAtTarget(context, config, world, biomeAccessor, random, mainChunkX, mainChunkZ, randomBlockX, randomBlockY, randomBlockZ, placementXZBound, placementYBound, mask, aquifer);
            }
        }
    }

    protected void carveAtTarget(CarvingContext context, RavineConfig config, ChunkAccess world, Function<BlockPos, Holder<Biome>> biomeAccessor, RandomSource random, int mainChunkX, int mainChunkZ, double xRange, double yRange, double zRange, double placementXZBound, double placementYBound, CarvingMask mask, Aquifer aquifer) {
        double d0 = mainChunkX * 16 + 8;
        double d1 = mainChunkZ * 16 + 8;
        if (xRange < d0 - 16.0D - placementXZBound * 2.0D || zRange < d1 - 16.0D - placementXZBound * 2.0D || xRange > d0 + 16.0D + placementXZBound * 2.0D || zRange > d1 + 16.0D + placementXZBound * 2.0D) {
            return;
        }

        int i = Math.max(Mth.floor(xRange - placementXZBound) - mainChunkX * 16 - 1, 0);
        int j = Math.min(Mth.floor(xRange + placementXZBound) - mainChunkX * 16 + 1, 16);
        int minY = Math.max(Mth.floor(yRange - placementYBound) - 1, 9);
        int maxY = Math.min(Mth.floor(yRange + placementYBound) + 1, config.cutoffHeight);
        int i1 = Math.max(Mth.floor(zRange - placementXZBound) - mainChunkZ * 16 - 1, 0);
        int j1 = Math.min(Mth.floor(zRange + placementXZBound) - mainChunkZ * 16 + 1, 16);

        if (i > j || minY > maxY || i1 > j1) {
            return;
        }

        BlockState fillerBlock;
        BlockState secondaryFloorBlockstate;
        BlockPos.MutableBlockPos blockpos$Mutable = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos blockpos$Mutableup = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos blockpos$Mutabledown = new BlockPos.MutableBlockPos();

        for (int xInChunk = i; xInChunk < j; ++xInChunk) {
            int x = xInChunk + mainChunkX * 16;
            double xSquaringModified = (x + 0.5D - xRange) / placementXZBound;

            for (int zInChunk = i1; zInChunk < j1; ++zInChunk) {
                int z = zInChunk + mainChunkZ * 16;
                double zSquaringModified = (z + 0.5D - zRange) / placementXZBound;
                double xzSquaredModified = xSquaringModified * xSquaringModified + zSquaringModified * zSquaringModified;

                if (xzSquaredModified >= 1.0D) {
                    continue;
                }

                blockpos$Mutable.set(x, 60, z);

                if (maxY >= 60 || minY < 11) {
                    Holder<Biome> biomeHolder = biomeAccessor.apply(blockpos$Mutable);
                    Biome biome = biomeHolder.value();
                    String biomeIDString = biomeHolder.unwrapKey().map(key -> key.location().toString()).orElse("");
                    fillerBlock = GeneralUtils.carverFillerBlock(biomeIDString, biome);
                    secondaryFloorBlockstate = GeneralUtils.carverLavaReplacement(biomeIDString, biome);
                } else {
                    fillerBlock = Blocks.STONE.defaultBlockState();
                    secondaryFloorBlockstate = Blocks.LAVA.defaultBlockState();
                }

                for (int y = maxY; y > minY; --y) {
                    double d4 = (y - 1 + 0.5D - yRange) / placementYBound;

                    if (xzSquaredModified * this.wallLedges[y - 1] + d4 * d4 / 6.0D >= 1.0D) {
                        continue;
                    }

                    blockpos$Mutable.set(x, y, z);
                    BlockState currentBlockstate = world.getBlockState(blockpos$Mutable);
                    blockpos$Mutableup.set(blockpos$Mutable).move(Direction.UP);
                    blockpos$Mutabledown.set(blockpos$Mutable).move(Direction.DOWN);
                    BlockState aboveBlockstate = world.getBlockState(blockpos$Mutableup);

                    if (y >= 60 && !aboveBlockstate.getFluidState().isEmpty()) {
                        world.setBlockState(blockpos$Mutable, fillerBlock, false);
                        world.setBlockState(blockpos$Mutableup, fillerBlock, false);
                        world.setBlockState(blockpos$Mutabledown, fillerBlock, false);
                    } else if (!mask.get(xInChunk, y, zInChunk) && canCarveBlock(currentBlockstate, aboveBlockstate)) {
                        if (y < 11) {
                            currentBlockstate = Blocks.LAVA.defaultBlockState();
                            if (secondaryFloorBlockstate.is(Blocks.OBSIDIAN)) {
                                currentBlockstate = Blocks.MAGMA_BLOCK.defaultBlockState();
                            }

                            if (random.nextFloat() > 0.35F) {
                                if (y == 10) {
                                    currentBlockstate = secondaryFloorBlockstate;
                                } else if (y == 9 && random.nextFloat() < 0.35F) {
                                    currentBlockstate = secondaryFloorBlockstate;
                                }
                            }

                            world.setBlockState(blockpos$Mutable, currentBlockstate, false);
                        } else {
                            world.setBlockState(blockpos$Mutable, CAVE_AIR, false);
                        }

                        mask.set(xInChunk, y, zInChunk);
                    }
                }
            }
        }
    }

    private static boolean canCarveBlock(BlockState state, BlockState aboveState) {
        return CARVABLE_BLOCKS.contains(state.getBlock());
    }

    private static Set<Block> createCarvableBlocks() {
        Set<Block> blocks = new HashSet<>();
        blocks.add(Blocks.NETHERRACK);
        blocks.add(Blocks.ICE);
        blocks.add(Blocks.SNOW_BLOCK);
        blocks.add(Blocks.END_STONE);
        blocks.add(Blocks.LAVA);
        blocks.add(Blocks.STONE);
        blocks.add(Blocks.GRANITE);
        blocks.add(Blocks.DIORITE);
        blocks.add(Blocks.ANDESITE);
        blocks.add(Blocks.DEEPSLATE);
        blocks.add(Blocks.TUFF);
        blocks.add(Blocks.DIRT);
        blocks.add(Blocks.GRASS_BLOCK);
        blocks.add(Blocks.SAND);
        blocks.add(Blocks.GRAVEL);
        blocks.add(Blocks.WATER);
        return blocks;
    }
}
