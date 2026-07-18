package com.telepathicgrunt.ultraamplifieddimension.world.decorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADPlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class OffsetPlacer extends PlacementModifier {
    public static final Codec<OffsetPlacer> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Codec.INT.fieldOf("yoffset").orElse(0).forGetter((config) -> config.yOffset),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("yspread").orElse(0).forGetter((config) -> config.ySpread))
            .apply(builder, OffsetPlacer::new));

    public final int yOffset;
    public final int ySpread;

    public OffsetPlacer(int yOffset, int ySpread) {
        this.yOffset = yOffset;
        this.ySpread = ySpread;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        if (this.ySpread > 0) {
            return Stream.of(pos.above(this.yOffset + random.nextInt(this.ySpread * 2) - this.ySpread));
        }

        return Stream.of(pos.above(this.yOffset));
    }

    @Override
    public PlacementModifierType<?> type() {
        return UADPlacements.Y_OFFSET_PLACER.get();
    }
}
