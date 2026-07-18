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

public class RangeValidationPlacer extends PlacementModifier {
    public static final Codec<RangeValidationPlacer> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
                    Codec.INT.fieldOf("max_Y").forGetter((config) -> config.maxY),
                    Codec.INT.fieldOf("min_Y").forGetter((config) -> config.minY))
            .apply(builder, RangeValidationPlacer::new));

    public final int maxY;
    public final int minY;

    public RangeValidationPlacer(int maxY, int minY) {
        this.maxY = maxY;
        this.minY = minY;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        if (pos.getY() <= this.maxY && pos.getY() > this.minY) {
            return Stream.of(pos);
        }

        return Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return UADPlacements.RANGE_VALIDATION_PLACER.get();
    }
}
