package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.utils.OpenSimplexNoise;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.BoulderFeatureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class Boulders extends Feature<BoulderFeatureConfig> {

    protected long seed;
    protected static OpenSimplexNoise noiseGen;

    public void setSeed(long seed) {
        if (this.seed != seed || noiseGen == null) {
            noiseGen = new OpenSimplexNoise(seed);
            this.seed = seed;
        }
    }

    public Boulders(Codec<BoulderFeatureConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<BoulderFeatureConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos position = context.origin();
        RandomSource random = context.random();
        BoulderFeatureConfig config = context.config();

        BlockPos.MutableBlockPos blockposMutable = new BlockPos.MutableBlockPos().set(position);
        ChunkAccess cachedChunk = level.getChunk(blockposMutable);

        if (blockposMutable.getY() > ((level.getMaxBuildHeight() - config.maxRadius) - 2)) {
            return false;
        }

        setSeed(level.getSeed());
        int prevHeight = 0;

        for (int stackCount = 0; stackCount < config.boulderStackCount; stackCount++) {
            int maxRadius = config.maxRadius;
            int minRadius = config.minRadius;
            int radiusModifier = (stackCount / (int) Math.max(Math.ceil(config.boulderStackCount / (float) config.maxRadius) + 1, 1));
            maxRadius = Math.max(maxRadius - radiusModifier, 1);
            minRadius = Math.max(minRadius - radiusModifier, 1);
            int startRadius = Math.max(random.nextInt(maxRadius - minRadius + 1) + minRadius, 1);
            int randMax = (int) Math.max(startRadius * 0.7f, 3);
            int randMin = (int) Math.max(startRadius * 0.35f, 1);

            for (int currentCount = 0; currentCount < 3; ++currentCount) {
                int x = Math.max(Math.min(startRadius + (random.nextInt(3) - 1), maxRadius), minRadius);
                int y = Math.max(Math.min(startRadius + (random.nextInt(3) - 1), maxRadius), minRadius);
                int z = Math.max(Math.min(startRadius + (random.nextInt(3) - 1), maxRadius), minRadius);

                float calculatedDistance = (x + y + z) * 0.333F + 0.5F;
                BlockPos from = blockposMutable.offset(-x, -y, -z);
                BlockPos to = blockposMutable.offset(x, y, z);

                for (BlockPos blockpos : BlockPos.betweenClosed(from, to)) {
                    if (blockpos.distSqr(blockposMutable) <= calculatedDistance * calculatedDistance) {
                        double noiseValue = 1;
                        if (startRadius > 2) {
                            noiseValue = noiseGen.eval(blockpos.getX() * 0.035D, blockpos.getY() * 0.0075D, blockpos.getZ() * 0.035D);
                        }
                        if (blockpos.distSqr(blockposMutable) > calculatedDistance * calculatedDistance * 0.65f
                                && noiseValue > -0.3D && noiseValue < 0.3D) {
                            continue;
                        }

                        if (blockpos.getX() >> 4 != cachedChunk.getPos().x || blockpos.getZ() >> 4 != cachedChunk.getPos().z) {
                            cachedChunk = level.getChunk(blockpos);
                        }

                        BlockState boulderBlock = GeneralUtils.getRandomEntry(config.blockAndWeights, random);
                        cachedChunk.setBlockState(blockpos, boulderBlock, false);
                    }
                }

                if (config.boulderStackCount > 1) {
                    blockposMutable.move(
                            random.nextInt(randMax) - randMin,
                            random.nextInt(randMax) - randMin,
                            random.nextInt(randMax) - randMin);
                } else {
                    blockposMutable.move(
                            random.nextInt(startRadius * 2) - startRadius,
                            0,
                            random.nextInt(startRadius * 2) - startRadius);

                    blockposMutable.move(Direction.UP,
                            config.heightmapSpread
                                    ? level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockposMutable.getX(), blockposMutable.getZ())
                                    - random.nextInt(2) - blockposMutable.getY()
                                    : -random.nextInt(2));
                }
            }

            prevHeight += minRadius;

            blockposMutable.set(position).move(
                    random.nextInt(randMax) - randMin,
                    prevHeight,
                    random.nextInt(randMax) - randMin);

            if (blockposMutable.getX() >> 4 != cachedChunk.getPos().x || blockposMutable.getZ() >> 4 != cachedChunk.getPos().z) {
                cachedChunk = level.getChunk(blockposMutable);
            }

            BlockState currentState = cachedChunk.getBlockState(blockposMutable);
            while (!currentState.isAir() && !currentState.is(BlockTags.LEAVES) && !currentState.is(BlockTags.LOGS)) {
                blockposMutable.move(Direction.UP);
                currentState = cachedChunk.getBlockState(blockposMutable);
            }

            if (blockposMutable.getY() > ((level.getMaxBuildHeight() - config.maxRadius) - 2)) {
                return false;
            }
        }

        return true;
    }
}
