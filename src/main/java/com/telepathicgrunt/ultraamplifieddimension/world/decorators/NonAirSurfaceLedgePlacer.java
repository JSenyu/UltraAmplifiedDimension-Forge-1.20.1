package com.telepathicgrunt.ultraamplifieddimension.world.decorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADPlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class NonAirSurfaceLedgePlacer extends PlacementModifier {
    public static final Codec<NonAirSurfaceLedgePlacer> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("column_passes").orElse(0).forGetter((config) -> config.columnCount),
            Codec.floatRange(0, 1).fieldOf("valid_spot_chance").orElse(1F).forGetter((config) -> config.validSpotChance))
            .apply(builder, NonAirSurfaceLedgePlacer::new));

    public final int columnCount;
    public final float validSpotChance;

    public NonAirSurfaceLedgePlacer(int columnCount, float validSpotChance) {
        this.columnCount = columnCount;
        this.validSpotChance = validSpotChance;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        List<BlockPos> list = new ArrayList<>();

        for (int count = 0; count < this.columnCount; ++count) {
            int x = random.nextInt(16) + pos.getX();
            int z = random.nextInt(16) + pos.getZ();
            int heightMapY = context.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            mutable.set(x, heightMapY, z);

            BlockState prevBlockState = context.getBlockState(mutable.above());
            int bottomYLimit = context.generator().getSeaLevel();

            while (mutable.getY() >= bottomYLimit) {
                BlockState currentBlockState = context.getBlockState(mutable);

                if (!currentBlockState.is(BlockTags.LEAVES) &&
                        !currentBlockState.is(BlockTags.LOGS) &&
                        !currentBlockState.is(Blocks.BEDROCK) &&
                        !currentBlockState.is(Blocks.CACTUS) &&
                        !currentBlockState.isAir() &&
                        prevBlockState.isAir()) {
                    if (random.nextFloat() < this.validSpotChance) {
                        list.add(mutable.immutable());
                    }

                    mutable.set(
                            random.nextInt(16) + pos.getX(),
                            mutable.getY(),
                            random.nextInt(16) + pos.getZ());
                }

                prevBlockState = currentBlockState;
                mutable.move(Direction.DOWN);
            }
        }

        return list.stream();
    }

    @Override
    public PlacementModifierType<?> type() {
        return UADPlacements.NON_AIR_SURFACE_LEDGE_PLACER.get();
    }
}
