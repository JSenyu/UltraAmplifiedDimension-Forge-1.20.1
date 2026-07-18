package com.telepathicgrunt.ultraamplifieddimension.world.decorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADPlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WaterIceSurfacePlacer extends PlacementModifier {
    public static final Codec<WaterIceSurfacePlacer> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("column_passes").orElse(0).forGetter((config) -> config.columnCount),
            Codec.floatRange(0, 1).fieldOf("valid_spot_chance").orElse(1F).forGetter((config) -> config.validSpotChance),
            Codec.BOOL.fieldOf("skip_top_ledge").orElse(false).forGetter((config) -> config.skipTopLedge),
            Codec.BOOL.fieldOf("include_ice_placement").orElse(false).forGetter((config) -> config.includeIcePlacement))
            .apply(builder, WaterIceSurfacePlacer::new));

    public final int columnCount;
    public final float validSpotChance;
    public final boolean skipTopLedge;
    public final boolean includeIcePlacement;

    public WaterIceSurfacePlacer(int columnCount, float validSpotChance, boolean skipTopLedge, boolean includeIcePlacement) {
        this.columnCount = columnCount;
        this.validSpotChance = validSpotChance;
        this.skipTopLedge = skipTopLedge;
        this.includeIcePlacement = includeIcePlacement;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        List<BlockPos> list = new ArrayList<>();
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
            mutable.set(x, heightMapY, z);

            BlockState prevBlockState = context.getBlockState(mutable.above());
            int bottomYLimit = context.generator().getSeaLevel();

            while (mutable.getY() >= bottomYLimit - 20) {
                BlockState currentBlockState = context.getBlockState(mutable);

                if (isLiquidOrIce(currentBlockState) && prevBlockState.isAir()) {
                    int topY = heightCache[localIndex];
                    skippedTopLedge = (this.skipTopLedge && mutable.getY() == topY - 1);

                    if (!skippedTopLedge) {
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
        return UADPlacements.WATER_ICE_SURFACE_PLACER.get();
    }

    private boolean isLiquidOrIce(BlockState state) {
        return state.getFluidState().is(FluidTags.WATER) || (this.includeIcePlacement && state.is(BlockTags.ICE));
    }
}
