package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraftforge.common.Tags;

import java.util.Map;
import java.util.WeakHashMap;

public class EllipsoidPocket extends Feature<OreConfiguration> {

    public EllipsoidPocket(Codec<OreConfiguration> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        OreConfiguration config = context.config();

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos();
        BlockState blockToReplace;
        float angleOfRotation = (float) (Math.PI * random.nextFloat());
        float sinOfAngle = Mth.sin(angleOfRotation);
        float cosOfAngle = Mth.cos(angleOfRotation);
        OreConfiguration.TargetBlockState targetState = config.targetStates.get(0);
        BlockState placementState = targetState.state;
        float size = config.size * 0.5f;
        boolean solidState = placementState.canOcclude();
        ChunkAccess cachedChunk;
        float stretchedFactor = 0.7f;
        if (config.size < 10) {
            stretchedFactor = 1;
        }
        int maxY = (int) (size / 3);
        int minY = -maxY - 1;

        for (int y = minY; y <= maxY; y++) {
            float yModified = y;
            if (y < 0) {
                yModified = y + 0.25f;
            } else if (y > 0) {
                y = (int) (y + 0.5f);
            }

            float percentageOfRadius = 1f - (yModified / size) * (yModified / size) * 3;
            float majorRadiusSq = (size * percentageOfRadius) * (size * percentageOfRadius);
            float minorRadiusSq = (size * stretchedFactor * percentageOfRadius) * (size * stretchedFactor * percentageOfRadius);

            for (int x = (int) -size; x < size; x++) {
                for (int z = (int) -size; z < size; z++) {
                    float majorComp = (x + 0.5f) * cosOfAngle - (z + 0.5f) * sinOfAngle;
                    float minorComp = (x + 0.5f) * sinOfAngle + (z + 0.5f) * cosOfAngle;

                    float result = ((majorComp * majorComp) / (majorRadiusSq * majorRadiusSq))
                            + ((minorComp * minorComp) / (minorRadiusSq * minorRadiusSq));

                    if (result * 100f < 1f && !(x == 0 && z == 0 && y * y >= (size * size))) {
                        blockposMutable.set(position.getX() + x, position.getY() + y, position.getZ() + z);
                        cachedChunk = getCachedChunk(level, blockposMutable);

                        blockToReplace = cachedChunk.getBlockState(blockposMutable);
                        if (targetState.target.test(blockToReplace, random) || blockToReplace.is(Tags.Blocks.ORES)) {
                            if (solidState) {
                                cachedChunk.setBlockState(blockposMutable, placementState, false);
                            } else {
                                boolean touchingLiquid = false;
                                for (Direction direction : Direction.values()) {
                                    if (direction != Direction.DOWN) {
                                        blockposMutable.move(direction);
                                        cachedChunk = getCachedChunk(level, blockposMutable);

                                        if (!cachedChunk.getBlockState(blockposMutable).getFluidState().isEmpty()) {
                                            touchingLiquid = true;
                                            blockposMutable.move(direction.getOpposite());
                                            break;
                                        }

                                        blockposMutable.move(direction.getOpposite());
                                    }
                                }

                                if (!touchingLiquid) {
                                    cachedChunk = getCachedChunk(level, blockposMutable);
                                    cachedChunk.setBlockState(blockposMutable, placementState, false);
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private static final Map<ResourceKey<Level>, Map<Long, ChunkAccess>> CACHED_CHUNKS_ALL_WORLDS = new WeakHashMap<>();

    private ChunkAccess getCachedChunk(WorldGenLevel level, BlockPos blockpos) {
        ResourceKey<Level> worldKey = level.getLevel().dimension();
        Map<Long, ChunkAccess> worldStorage = CACHED_CHUNKS_ALL_WORLDS.computeIfAbsent(worldKey, k -> new WeakHashMap<>());

        if (worldStorage.size() > 9) {
            worldStorage.clear();
        }

        long posLong = (long) (blockpos.getX() >> 4) & 4294967295L | ((long) (blockpos.getZ() >> 4) & 4294967295L) << 32;
        ChunkAccess cachedChunk = worldStorage.get(posLong);
        if (cachedChunk == null) {
            cachedChunk = level.getChunk(blockpos);
            worldStorage.put(posLong, cachedChunk);
        }

        return cachedChunk;
    }
}
