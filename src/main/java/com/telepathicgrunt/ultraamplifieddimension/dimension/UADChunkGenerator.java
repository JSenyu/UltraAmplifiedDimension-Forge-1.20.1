package com.telepathicgrunt.ultraamplifieddimension.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.dimension.terrain.UADStructureTerraformer;
import com.telepathicgrunt.ultraamplifieddimension.dimension.terrain.UADTerrainBlocks;
import com.telepathicgrunt.ultraamplifieddimension.dimension.terrain.UADTerrainSampler;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

/**
 * Fills terrain with custom column noise + structure kernels.
 * Still extends NoiseBasedChunkGenerator so surface rules and carvers get a NoiseChunk.
 */
public class UADChunkGenerator extends NoiseBasedChunkGenerator {
    public static final Codec<UADChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(UADChunkGenerator::generatorSettings)
            ).apply(instance, instance.stable(UADChunkGenerator::new))
    );

    private static final int BIOME_CACHE_LIMIT = 1024;

    private final int seaLevel;
    private final Aquifer.FluidPicker fluidPicker;
    private final Object samplerLock = new Object();
    private UADTerrainSampler sampler;
    private RandomState samplerState;
    private final ThreadLocal<Long2ObjectOpenHashMap<Holder<Biome>>> biomeCache =
            ThreadLocal.withInitial(Long2ObjectOpenHashMap::new);

    public UADChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.seaLevel = settings.value().seaLevel();
        this.fluidPicker = createFluidPicker(settings.value());
    }

    private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings settings) {
        Aquifer.FluidStatus lava = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int sea = settings.seaLevel();
        Aquifer.FluidStatus seaFluid = new Aquifer.FluidStatus(sea, settings.defaultFluid());
        return (x, y, z) -> y < Math.min(-54, sea) ? lava : seaFluid;
    }

    private NoiseChunk makeNoiseChunk(ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState randomState) {
        return NoiseChunk.forChunk(
                chunk,
                randomState,
                Beardifier.forStructuresInChunk(structureManager, chunk.getPos()),
                this.generatorSettings().value(),
                this.fluidPicker,
                blender
        );
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public int getSeaLevel() {
        return this.seaLevel;
    }

    /**
     * Structure placement uses generator minY; default floor is Y=0 so pieces are not pushed into the void.
     * When generateBelowZero is enabled, floor is Y=-64 to match dimension height.
     */
    @Override
    public int getMinY() {
        return UADTerrainSampler.minY();
    }

    private UADTerrainSampler sampler(RandomState randomState) {
        UADTerrainSampler local = this.sampler;
        if (local != null && this.samplerState == randomState) {
            return local;
        }
        synchronized (this.samplerLock) {
            if (this.sampler == null || this.samplerState != randomState) {
                RandomSource random = randomState
                        .getOrCreateRandomFactory(ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "uad_terrain"))
                        .at(0, 0, 0);
                this.sampler = new UADTerrainSampler(random);
                this.samplerState = randomState;
            }
            return this.sampler;
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Executor executor,
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunk
    ) {
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        int sectionTop = chunk.getSectionIndex(maxY - 1);
        int sectionBottom = chunk.getSectionIndex(minY);
        Set<LevelChunkSection> locked = new HashSet<>();
        for (int i = sectionTop; i >= sectionBottom; --i) {
            LevelChunkSection section = chunk.getSection(i);
            section.acquire();
            locked.add(section);
        }

        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("uad_fill_noise", () ->
                doUadFill(blender, structureManager, randomState, chunk)
        ), Util.backgroundExecutor()).whenCompleteAsync((result, error) -> {
            for (LevelChunkSection section : locked) {
                section.release();
            }
        }, executor);
    }

    private ChunkAccess doUadFill(
            Blender blender,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunk
    ) {
        chunk.getOrCreateNoiseChunk(access -> makeNoiseChunk(access, structureManager, blender, randomState));

        UADTerrainSampler terrainSampler = sampler(randomState);
        UADStructureTerraformer terraformer = UADStructureTerraformer.collect(structureManager, chunk.getPos());
        NoiseGeneratorSettings genSettings = this.generatorSettings().value();
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        int blockMinX = chunk.getPos().getMinBlockX();
        int blockMinZ = chunk.getPos().getMinBlockZ();
        int worldMaxY = chunk.getMaxBuildHeight();
        int worldMinY = chunk.getMinBuildHeight();
        int terrainMinY = UADTerrainSampler.minY();
        int noiseSizeY = UADTerrainSampler.noiseSizeY();
        biomeCache.get().clear();

        double[][][] columns = new double[2][UADTerrainSampler.NOISE_SIZE_XZ + 1][noiseSizeY + 1];
        for (int zNoise = 0; zNoise < UADTerrainSampler.NOISE_SIZE_XZ + 1; ++zNoise) {
            columns[0][zNoise] = new double[noiseSizeY + 1];
            terrainSampler.fillNoiseColumn(
                    columns[0][zNoise],
                    chunkX * UADTerrainSampler.NOISE_SIZE_XZ,
                    chunkZ * UADTerrainSampler.NOISE_SIZE_XZ + zNoise
            );
            columns[1][zNoise] = new double[noiseSizeY + 1];
        }

        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int xNoise = 0; xNoise < UADTerrainSampler.NOISE_SIZE_XZ; ++xNoise) {
            for (int zNoise = 0; zNoise < UADTerrainSampler.NOISE_SIZE_XZ + 1; ++zNoise) {
                terrainSampler.fillNoiseColumn(
                        columns[1][zNoise],
                        chunkX * UADTerrainSampler.NOISE_SIZE_XZ + xNoise + 1,
                        chunkZ * UADTerrainSampler.NOISE_SIZE_XZ + zNoise
                );
            }

            for (int zNoise = 0; zNoise < UADTerrainSampler.NOISE_SIZE_XZ; ++zNoise) {
                int sectionIndex = chunk.getSectionsCount() - 1;
                LevelChunkSection section = chunk.getSection(sectionIndex);

                for (int yNoise = noiseSizeY - 1; yNoise >= 0; --yNoise) {
                    double c00 = columns[0][zNoise][yNoise];
                    double c01 = columns[0][zNoise + 1][yNoise];
                    double c10 = columns[1][zNoise][yNoise];
                    double c11 = columns[1][zNoise + 1][yNoise];
                    double c00u = columns[0][zNoise][yNoise + 1];
                    double c01u = columns[0][zNoise + 1][yNoise + 1];
                    double c10u = columns[1][zNoise][yNoise + 1];
                    double c11u = columns[1][zNoise + 1][yNoise + 1];

                    for (int xSection = 0; xSection < UADTerrainSampler.CELL_WIDTH; ++xSection) {
                        int x = blockMinX + xNoise * UADTerrainSampler.CELL_WIDTH + xSection;
                        int xInChunk = x & 15;
                        double xd = (double) xSection / (double) UADTerrainSampler.CELL_WIDTH;

                        for (int zSection = 0; zSection < UADTerrainSampler.CELL_WIDTH; ++zSection) {
                            int z = blockMinZ + zNoise * UADTerrainSampler.CELL_WIDTH + zSection;
                            int zInChunk = z & 15;
                            double zd = (double) zSection / (double) UADTerrainSampler.CELL_WIDTH;

                            for (int ySection = UADTerrainSampler.CELL_HEIGHT - 1; ySection >= 0; --ySection) {
                                int y = terrainMinY + yNoise * UADTerrainSampler.CELL_HEIGHT + ySection;
                                if (y < worldMinY || y >= worldMaxY) {
                                    continue;
                                }

                                int ySectionIndex = chunk.getSectionIndex(y);
                                if (sectionIndex != ySectionIndex) {
                                    sectionIndex = ySectionIndex;
                                    section = chunk.getSection(ySectionIndex);
                                }

                                double yd = (double) ySection / (double) UADTerrainSampler.CELL_HEIGHT;
                                double noiseValue = UADTerrainSampler.densityFromCorners(
                                        c00, c01, c10, c11, c00u, c01u, c10u, c11u, xd, yd, zd);
                                UADStructureTerraformer.ApplyResult terraform = terraformer.apply(noiseValue, x, y, z);
                                noiseValue = terraform.density();

                                Holder<Biome> biome = getNoiseBiome(randomState, x, y, z);

                                BlockState state;
                                if (terraform.forceAir()) {
                                    // BEARD_BOX cavities must stay dry below sea level (otherwise terrainBlock fills water).
                                    state = Blocks.AIR.defaultBlockState();
                                } else {
                                    state = UADTerrainBlocks.terrainBlock(
                                            noiseValue, biome, x, y, z, this.seaLevel, genSettings,
                                            pos -> getNoiseBiome(randomState, pos.getX(), pos.getY(), pos.getZ())
                                    );
                                }

                                if (!state.isAir()) {
                                    section.setBlockState(xInChunk, y & 15, zInChunk, state, false);
                                    oceanFloor.update(xInChunk, y, zInChunk, state);
                                    worldSurface.update(xInChunk, y, zInChunk, state);
                                    if (!state.getFluidState().isEmpty()) {
                                        mutable.set(x, y, z);
                                        chunk.markPosForPostprocessing(mutable);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            double[][] swap = columns[0];
            columns[0] = columns[1];
            columns[1] = swap;
        }

        return chunk;
    }

    private Holder<Biome> getNoiseBiome(RandomState randomState, int x, int y, int z) {
        Long2ObjectOpenHashMap<Holder<Biome>> cache = biomeCache.get();
        int quartX = QuartPos.fromBlock(x);
        int quartY = QuartPos.fromBlock(y);
        int quartZ = QuartPos.fromBlock(z);
        long key = BlockPos.asLong(quartX, quartY, quartZ);
        Holder<Biome> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        if (cache.size() > BIOME_CACHE_LIMIT) {
            cache.clear();
        }
        Holder<Biome> biome = this.biomeSource.getNoiseBiome(quartX, quartY, quartZ, randomState.sampler());
        cache.put(key, biome);
        return biome;
    }

    @Override
    protected OptionalInt iterateNoiseColumn(
            LevelHeightAccessor level,
            RandomState randomState,
            int x,
            int z,
            @Nullable MutableObject<NoiseColumn> columnOut,
            @Nullable Predicate<BlockState> predicate
    ) {
        UADTerrainSampler terrainSampler = sampler(randomState);
        NoiseGeneratorSettings genSettings = this.generatorSettings().value();
        int minY = level.getMinBuildHeight();
        int height = level.getHeight();

        BlockState[] states = null;
        if (columnOut != null) {
            states = new BlockState[height];
            columnOut.setValue(new NoiseColumn(minY, states));
        }

        int noiseX = Math.floorDiv(x, UADTerrainSampler.CELL_WIDTH);
        int noiseZ = Math.floorDiv(z, UADTerrainSampler.CELL_WIDTH);
        int localX = Math.floorMod(x, UADTerrainSampler.CELL_WIDTH);
        int localZ = Math.floorMod(z, UADTerrainSampler.CELL_WIDTH);
        double xd = (double) localX / (double) UADTerrainSampler.CELL_WIDTH;
        double zd = (double) localZ / (double) UADTerrainSampler.CELL_WIDTH;

        double[] c00 = new double[UADTerrainSampler.noiseSizeY() + 1];
        double[] c01 = new double[UADTerrainSampler.noiseSizeY() + 1];
        double[] c10 = new double[UADTerrainSampler.noiseSizeY() + 1];
        double[] c11 = new double[UADTerrainSampler.noiseSizeY() + 1];
        terrainSampler.fillNoiseColumn(c00, noiseX, noiseZ);
        terrainSampler.fillNoiseColumn(c01, noiseX, noiseZ + 1);
        terrainSampler.fillNoiseColumn(c10, noiseX + 1, noiseZ);
        terrainSampler.fillNoiseColumn(c11, noiseX + 1, noiseZ + 1);

        int terrainMinY = UADTerrainSampler.minY();

        for (int y = minY + height - 1; y >= minY; --y) {
            BlockState state;
            if (y < terrainMinY || y > UADTerrainSampler.MAX_TERRAIN_Y) {
                state = Blocks.AIR.defaultBlockState();
            } else {
                int relativeY = y - terrainMinY;
                int yNoise = relativeY / UADTerrainSampler.CELL_HEIGHT;
                int ySection = relativeY % UADTerrainSampler.CELL_HEIGHT;
                double yd = (double) ySection / (double) UADTerrainSampler.CELL_HEIGHT;
                double noiseValue = UADTerrainSampler.densityFromCorners(
                        c00[yNoise], c01[yNoise], c10[yNoise], c11[yNoise],
                        c00[yNoise + 1], c01[yNoise + 1], c10[yNoise + 1], c11[yNoise + 1],
                        xd, yd, zd
                );
                Holder<Biome> biome = getNoiseBiome(randomState, x, y, z);
                state = UADTerrainBlocks.terrainBlock(
                        noiseValue, biome, x, y, z, this.seaLevel, genSettings,
                        pos -> getNoiseBiome(randomState, pos.getX(), pos.getY(), pos.getZ())
                );
            }

            if (states != null) {
                states[y - minY] = state;
            }
            if (predicate != null && predicate.test(state)) {
                return OptionalInt.of(y + 1);
            }
        }

        return OptionalInt.of(minY);
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
        chunk.getOrCreateNoiseChunk(access -> makeNoiseChunk(access, structureManager, Blender.of(region), randomState));
        super.buildSurface(region, structureManager, randomState, chunk);
    }
}
