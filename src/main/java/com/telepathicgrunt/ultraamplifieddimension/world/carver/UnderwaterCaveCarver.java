package com.telepathicgrunt.ultraamplifieddimension.world.carver;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class UnderwaterCaveCarver extends CaveWorldCarver {
    private static final Set<Block> CARVABLE_BLOCKS = createCarvableBlocks();

    public UnderwaterCaveCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    protected boolean carveBlock(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor, CarvingMask carvingMask, BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos pos2, BlockPos.MutableBlockPos pos3, Aquifer aquifer, MutableBoolean isSurface) {
        return carveUnderwaterBlock(context, config, biomeAccessor, chunk, pos.getX(), pos.getY(), pos.getZ());
    }

    private static boolean carveUnderwaterBlock(CarvingContext context, CaveCarverConfiguration config, Function<BlockPos, Holder<Biome>> biomeAccessor, ChunkAccess chunk, int x, int y, int z) {
        int minHeight = config.lavaLevel.resolveY(context);
        if (y >= minHeight) {
            return false;
        }

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(x, y, z);
        BlockState lavaBlock;

        if (y < 11) {
            Holder<Biome> biomeHolder = biomeAccessor.apply(mutableBlockPos);
            Biome biome = biomeHolder.value();
            String biomeIDString = biomeHolder.unwrapKey()
                    .map(key -> key.location().toString())
                    .orElse("");
            lavaBlock = GeneralUtils.carverLavaReplacement(biomeIDString, biome);
        } else {
            lavaBlock = Blocks.LAVA.defaultBlockState();
        }

        BlockState blockstate = chunk.getBlockState(mutableBlockPos);
        if (!CARVABLE_BLOCKS.contains(blockstate.getBlock())) {
            return false;
        }

        if (y == 10) {
            RandomSource random = RandomSource.create(chunk.getPos().toLong() ^ mutableBlockPos.asLong());
            if (random.nextFloat() < 0.25F && !lavaBlock.is(Blocks.OBSIDIAN)) {
                chunk.setBlockState(mutableBlockPos, Blocks.MAGMA_BLOCK.defaultBlockState(), false);
                chunk.markPosForPostprocessing(mutableBlockPos);
            } else {
                chunk.setBlockState(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), false);
            }

            return true;
        }

        if (y < 10) {
            chunk.setBlockState(mutableBlockPos, lavaBlock, false);
            return false;
        }

        chunk.setBlockState(mutableBlockPos, Blocks.WATER.defaultBlockState(), false);
        return true;
    }

    private static Set<Block> createCarvableBlocks() {
        Set<Block> blocks = new HashSet<>();
        blocks.add(Blocks.STONE);
        blocks.add(Blocks.GRANITE);
        blocks.add(Blocks.DIORITE);
        blocks.add(Blocks.ANDESITE);
        blocks.add(Blocks.DEEPSLATE);
        blocks.add(Blocks.SAND);
        blocks.add(Blocks.GRAVEL);
        blocks.add(Blocks.WATER);
        blocks.add(Blocks.LAVA);
        blocks.add(Blocks.OBSIDIAN);
        blocks.add(Blocks.AIR);
        blocks.add(Blocks.CAVE_AIR);
        return blocks;
    }
}
