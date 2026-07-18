package com.telepathicgrunt.ultraamplifieddimension.world.decorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADPlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
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

public class LedgeSurfacePlacer extends PlacementModifier {
    public static final Codec<LedgeSurfacePlacer> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("column_passes").orElse(0).forGetter((config) -> config.columnCount),
            Codec.floatRange(0, 1).fieldOf("valid_spot_chance").orElse(1F).forGetter((config) -> config.validSpotChance),
            Codec.BOOL.fieldOf("skip_top_ledge").orElse(false).forGetter((config) -> config.skipTopLedge),
            Codec.BOOL.fieldOf("underside_only").orElse(false).forGetter((config) -> config.undersideOnly),
            Codec.BOOL.fieldOf("water_pos_only").orElse(false).forGetter((config) -> config.waterPosOnly))
            .apply(builder, LedgeSurfacePlacer::new));

    public final int columnCount;
    public final float validSpotChance;
    public final boolean skipTopLedge;
    public final boolean undersideOnly;
    public final boolean waterPosOnly;

    public LedgeSurfacePlacer(int columnCount, float validSpotChance, boolean skipTopLedge, boolean undersideOnly, boolean waterPosOnly) {
        this.columnCount = columnCount;
        this.validSpotChance = validSpotChance;
        this.skipTopLedge = skipTopLedge;
        this.undersideOnly = undersideOnly;
        this.waterPosOnly = waterPosOnly;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        List<BlockPos> list = new ArrayList<>();
        // Cache heightmap per column so skipTopLedge doesn't re-query every Y step
        int[] heightCache = new int[16 * 16];
        boolean[] heightCached = new boolean[16 * 16];

        for (int count = 0; count < this.columnCount; ++count) {
            int x = random.nextInt(16) + pos.getX();
            int z = random.nextInt(16) + pos.getZ();
            int localIndex = (x & 15) * 16 + (z & 15);
            int heightMapY = heightCached[localIndex]
                    ? heightCache[localIndex]
                    : context.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            heightCache[localIndex] = heightMapY;
            heightCached[localIndex] = true;
            boolean skippedTopLedge;
            boolean isValidWaterPos;
            mutable.set(x, heightMapY, z);

            BlockState prevBlockState = context.getBlockState(mutable.above());
            int bottomYLimit = this.waterPosOnly ? 11 : context.generator().getSeaLevel();

            while (mutable.getY() >= bottomYLimit) {
                BlockState currentBlockState = context.getBlockState(mutable);

                if (!currentBlockState.is(BlockTags.LEAVES) &&
                        !currentBlockState.is(BlockTags.LOGS) &&
                        !currentBlockState.is(Blocks.BEDROCK) &&
                        !currentBlockState.is(Blocks.CACTUS) &&
                        ((!this.undersideOnly && !notSolidSpace(currentBlockState) && notSolidSpace(prevBlockState)) ||
                                (this.undersideOnly && notSolidSpace(currentBlockState) && !notSolidSpace(prevBlockState)))) {
                    int topY = heightCached[localIndex] ? heightCache[localIndex] : heightMapY;
                    skippedTopLedge = !this.undersideOnly && (this.skipTopLedge && mutable.getY() == topY - 1);
                    isValidWaterPos = (this.undersideOnly ? currentBlockState.getFluidState().is(FluidTags.WATER) : prevBlockState.getFluidState().is(FluidTags.WATER));

                    if (((this.waterPosOnly && isValidWaterPos) ||
                            (!this.waterPosOnly && !isValidWaterPos)) &&
                            !skippedTopLedge) {
                        if (random.nextFloat() < this.validSpotChance) {
                            list.add(mutable.immutable());
                        }

                        x = random.nextInt(16) + pos.getX();
                        z = random.nextInt(16) + pos.getZ();
                        localIndex = (x & 15) * 16 + (z & 15);
                        if (!heightCached[localIndex]) {
                            heightCache[localIndex] = context.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
                            heightCached[localIndex] = true;
                        }
                        mutable.set(x, mutable.getY(), z);
                    }
                }

                prevBlockState = currentBlockState;
                mutable.move(Direction.DOWN);
            }
        }

        return list.stream();
    }

    @Override
    public PlacementModifierType<?> type() {
        return UADPlacements.LEDGE_SURFACE_PLACER.get();
    }

    private static boolean notSolidSpace(BlockState state) {
        return state.isAir() || !(state.getFluidState().isEmpty() || state.isSolid());
    }
}
