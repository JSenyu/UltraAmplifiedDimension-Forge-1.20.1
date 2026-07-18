package com.telepathicgrunt.ultraamplifieddimension.world.carver;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.utils.OpenSimplexNoise;
import com.telepathicgrunt.ultraamplifieddimension.world.carver.configs.CaveConfig;
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

public class CaveCavityCarver extends WorldCarver<CaveConfig> {
    private static final int CARVER_RANGE = 250;

    private final float[] ledgeWidthArrayYIndex = new float[1024];
    protected static long NOISE_SEED;
    protected static OpenSimplexNoise NOISE_GEN;

    private static final Set<Block> CARVABLE_BLOCKS = createCarvableBlocks();

    public static void setSeed(long seed) {
        if (NOISE_SEED != seed || NOISE_GEN == null) {
            NOISE_GEN = new OpenSimplexNoise(seed);
            NOISE_SEED = seed;
        }
    }

    public CaveCavityCarver(Codec<CaveConfig> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(CaveConfig config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }

    @Override
    public boolean carve(CarvingContext context, CaveConfig config, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor, RandomSource random, Aquifer aquifer, ChunkPos chunkPos, CarvingMask mask) {
        if (NOISE_GEN == null) {
            // Prefer world seed from UltraAmplifiedDimension; last-resort avoid per-chunk seeds
            setSeed(chunk.getPos().toLong() ^ 0x5DEECE66DL);
        }
        // chunkPos = origin chunk that started this carve; chunk = chunk currently being written
        ChunkPos writingChunk = chunk.getPos();
        int maxIterations = (CARVER_RANGE * 2 - 1) * 16;
        double xpos = chunkPos.getMinBlockX() + random.nextInt(16);
        double height = random.nextInt(random.nextInt(2) + 1) + 34;
        double zpos = chunkPos.getMinBlockZ() + random.nextInt(16);
        float xzNoise2 = random.nextFloat() * ((float) Math.PI);
        float xzCosNoise = (random.nextFloat() - 0.5F) / 16.0F;
        float widthHeightBase = (random.nextFloat() + random.nextFloat()) / 16.0F;
        this.carveCavity(chunk, biomeAccessor, random, writingChunk.x, writingChunk.z, xpos, height, zpos, widthHeightBase, xzNoise2, xzCosNoise, maxIterations, random.nextDouble() + 20.0D, mask, config);
        return true;
    }

    private void carveCavity(ChunkAccess world, Function<BlockPos, Holder<Biome>> biomeAccessor, RandomSource random, int mainChunkX, int mainChunkZ, double randomBlockX, double randomBlockY, double randomBlockZ, float widthHeightBase, float xzNoise2, float xzCosNoise, int maxIteration, double heightMultiplier, CarvingMask mask, CaveConfig config) {
        float ledgeWidth = 1.0F;

        for (int currentHeight = 0; currentHeight <= config.cutoffHeight; ++currentHeight) {
            if (currentHeight > 44 && currentHeight < 60) {
                ledgeWidth = 1.0F + random.nextFloat() * 0.3F;
                ledgeWidth = (float) (ledgeWidth + Math.max(0, Math.pow((currentHeight - 44) * 0.15F, 2)));
            } else if (currentHeight == 0 || random.nextInt(3) == 0) {
                ledgeWidth = 1.0F + random.nextFloat() * 0.5F;
            }

            this.ledgeWidthArrayYIndex[currentHeight] = ledgeWidth;
        }

        double placementXZBound = 2.0D + Mth.sin((float) Math.PI / maxIteration) * widthHeightBase;
        double placementYBound = placementXZBound * heightMultiplier;
        placementXZBound *= 32.0D;
        placementYBound *= 2.2D;

        this.carveAtTarget(world, biomeAccessor, random, mainChunkX, mainChunkZ, randomBlockX, randomBlockY, randomBlockZ, placementXZBound, placementYBound, mask, config);
    }

    protected void carveAtTarget(ChunkAccess world, Function<BlockPos, Holder<Biome>> biomeAccessor, RandomSource random, int mainChunkX, int mainChunkZ, double xRange, double yRange, double zRange, double placementXZBound, double placementYBound, CarvingMask mask, CaveConfig config) {
        double xPos = mainChunkX * 16 + 8;
        double zPos = mainChunkZ * 16 + 8;
        double multipliedXZBound = placementXZBound * 2.0D;

        if (xRange < xPos - 16.0D - multipliedXZBound || zRange < zPos - 16.0D - multipliedXZBound || xRange > xPos + 16.0D + multipliedXZBound || zRange > zPos + 16.0D + multipliedXZBound) {
            return;
        }

        int xMin = Math.max(Mth.floor(xRange - placementXZBound) - mainChunkX * 16 - 1, 0);
        int xMax = Math.min(Mth.floor(xRange + placementXZBound) - mainChunkX * 16 + 1, 16);
        int yMin = Math.max(Mth.floor(yRange - placementYBound) - 1, 5);
        int yMax = Math.min(Mth.floor(yRange + placementYBound) + 1, config.cutoffHeight);
        int zMin = Math.max(Mth.floor(zRange - placementXZBound) - mainChunkZ * 16 - 1, 0);
        int zMax = Math.min(Mth.floor(zRange + placementXZBound) - mainChunkZ * 16 + 1, 16);

        if (xMin > xMax || yMin > yMax || zMin > zMax) {
            return;
        }

        BlockState fillerBlock;
        BlockState secondaryFloorBlockstate;
        BlockState currentBlockstate;
        BlockState aboveBlockstate;
        BlockPos.MutableBlockPos blockpos$Mutable = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos blockpos$Mutableup = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos blockpos$Mutabledown = new BlockPos.MutableBlockPos();
        double stalagmiteDouble = 0;

        for (int xInChunk = xMin; xInChunk < xMax; ++xInChunk) {
            int x = xInChunk + mainChunkX * 16;
            double xSquaringModified = (x + 0.5D - xRange) / placementXZBound;

            for (int zInChunk = zMin; zInChunk < zMax; ++zInChunk) {
                int z = zInChunk + mainChunkZ * 16;
                double zSquaringModified = (z + 0.5D - zRange) / placementXZBound;
                double xzSquaredModified = xSquaringModified * xSquaringModified + zSquaringModified * zSquaringModified;

                if (xzSquaredModified >= 1.0D) {
                    continue;
                }

                if (yMax < yMin) {
                    continue;
                }

                blockpos$Mutable.set(x, 60, z);

                if (yMax >= 60 || yMin < 11) {
                    Holder<Biome> biomeHolder = biomeAccessor.apply(blockpos$Mutable);
                    Biome biome = biomeHolder.value();
                    String biomeIDString = biomeHolder.unwrapKey().map(key -> key.location().toString()).orElse("");
                    fillerBlock = GeneralUtils.carverFillerBlock(biomeIDString, biome);
                    secondaryFloorBlockstate = GeneralUtils.carverLavaReplacement(biomeIDString, biome);
                } else {
                    fillerBlock = Blocks.STONE.defaultBlockState();
                    secondaryFloorBlockstate = Blocks.LAVA.defaultBlockState();
                }

                for (int y = yMax; y > yMin; y--) {
                    if (mask.get(xInChunk, y, zInChunk)) {
                        continue;
                    }

                    double ySquaringModified = (y - 1 + 0.5D - yRange) / placementYBound;
                    if (xzSquaredModified * this.ledgeWidthArrayYIndex[y - 1] + ySquaringModified * ySquaringModified / 6.0D + random.nextFloat() * 0.015F >= 1.0D) {
                        continue;
                    }

                    double yPillarModifier = y;

                    if (y > 30) {
                        yPillarModifier = (Math.pow((yPillarModifier - 30.0D) * 0.033333D, 2) * 30.0D - (y * 0.016666D)) * 18.0D;
                    } else {
                        yPillarModifier = Math.pow(Math.pow(yPillarModifier - 30.0D, 2) * 0.033333D, 2) * 2.8D;
                    }

                    if (yPillarModifier <= 0) {
                        yPillarModifier = 0.00001D;
                    } else if (y < 10) {
                        yPillarModifier -= 50.0D;
                    }

                    if (y < 60) {
                        boolean flagPillars = NOISE_GEN.eval(
                                x * 0.045D,
                                z * 0.045D,
                                y * 0.015D) - (yPillarModifier * 0.001D) +
                                (random.nextDouble() * 0.01D)
                                > -0.32D;

                        if (!flagPillars) {
                            continue;
                        }

                        if (y > 30) {
                            stalagmiteDouble = NOISE_GEN.eval(x * 0.25D, z * 0.25D, 0) * 15.0D + (500.0D / y);

                            if (y > 48) {
                                stalagmiteDouble -= (y - 53.0D) / 3.0D;
                            }

                            if (stalagmiteDouble <= 5.3D) {
                                continue;
                            }
                        }
                    }

                    blockpos$Mutable.set(x, y, z);
                    currentBlockstate = world.getBlockState(blockpos$Mutable);
                    blockpos$Mutableup.set(blockpos$Mutable).move(Direction.UP);
                    blockpos$Mutabledown.set(blockpos$Mutable).move(Direction.DOWN);
                    aboveBlockstate = world.getBlockState(blockpos$Mutableup);

                    if (y >= 60) {
                        if (!currentBlockstate.getFluidState().isEmpty()) {
                            world.setBlockState(blockpos$Mutable, fillerBlock, false);
                        } else if (!aboveBlockstate.getFluidState().isEmpty()) {
                            world.setBlockState(blockpos$Mutable, fillerBlock, false);
                            world.setBlockState(blockpos$Mutableup, fillerBlock, false);
                            world.setBlockState(blockpos$Mutabledown, fillerBlock, false);
                        }
                    } else if (canCarveBlock(currentBlockstate, aboveBlockstate)) {
                        if (y < 11) {
                            currentBlockstate = Blocks.LAVA.defaultBlockState();
                            if (secondaryFloorBlockstate.is(Blocks.OBSIDIAN)) {
                                currentBlockstate = Blocks.MAGMA_BLOCK.defaultBlockState();
                            }

                            if (stalagmiteDouble > 13.5D) {
                                if (y == 10) {
                                    currentBlockstate = secondaryFloorBlockstate;
                                } else if (y == 9 && random.nextBoolean()) {
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
