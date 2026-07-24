package com.telepathicgrunt.ultraamplifieddimension.dimension.terrain;

import com.telepathicgrunt.ultraamplifieddimension.config.UADimensionConfig;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import java.util.stream.IntStream;

/**
 * Column noise for UAD terrain fill. Sampling constants are fixed here;
 * datapack noise_router density does not drive this sampler.
 *
 * When {@code generateBelowZero} is enabled, Y&lt;0 is biased toward solid deepslate mass
 * with cheese/noodle-style cave carving (dry air), instead of huge flooded voids.
 */
public final class UADTerrainSampler {
    public static final int CELL_WIDTH = 2;
    public static final int CELL_HEIGHT = 4;
    /** Noise cells when terrain starts at Y=0 (covers Y 0..243). */
    public static final int NOISE_SIZE_Y = 61;
    /** Noise cells when generateBelowZero extends terrain to Y=-64 (covers Y -64..243). */
    public static final int NOISE_SIZE_Y_BELOW_ZERO = 77;
    public static final int NOISE_SIZE_XZ = 16 / CELL_WIDTH;
    public static final int MIN_Y = 0;
    public static final int MIN_Y_BELOW_ZERO = -64;
    public static final int MAX_TERRAIN_Y = NOISE_SIZE_Y * CELL_HEIGHT - 1; // 243

    private static final double XZ_SCALE = 2.0D;
    private static final double Y_SCALE = 420.0D;
    private static final double XZ_FACTOR = 684.0D;
    private static final double Y_FACTOR = 68400.0D;
    private static final double DENSITY_FACTOR = 0.95D;
    private static final double DENSITY_OFFSET = -0.46875D;
    private static final double TOP_SLIDE_TARGET = -10.0D;
    private static final double TOP_SLIDE_SIZE = 3.0D;
    private static final double TOP_SLIDE_OFFSET = 0.0D;
    private static final double BOTTOM_SLIDE_TARGET = 10.0D;
    private static final double BOTTOM_SLIDE_SIZE = 1.0D;
    private static final double BOTTOM_SLIDE_OFFSET = 4.0D;

    // Fixed biome depth/scale (0.0 / 0.0 + offsets used by old UAD).
    private static final double DENSITY_BIAS;
    private static final double DENSITY_SCALE;

    static {
        float depthWeight = 1.0F + 0.4F * 2.0F;
        float scaleWeight = 1.0F + 0.3F * 12.0F;
        double d16 = depthWeight * 0.5F - 0.125F;
        double d18 = scaleWeight * 0.9F + 0.1F;
        DENSITY_BIAS = d16 * 0.265625D;
        DENSITY_SCALE = 96.0D / d18;
    }

    private final PerlinNoise minLimitNoise;
    private final PerlinNoise maxLimitNoise;
    private final PerlinNoise mainNoise;
    private final PerlinNoise depthNoise;
    private final PerlinNoise cheeseCaveNoise;
    private final PerlinNoise noodleCaveNoise;

    public UADTerrainSampler(RandomSource random) {
        this.minLimitNoise = PerlinNoise.createLegacyForBlendedNoise(random, IntStream.rangeClosed(-15, 0));
        this.maxLimitNoise = PerlinNoise.createLegacyForBlendedNoise(random, IntStream.rangeClosed(-15, 0));
        this.mainNoise = PerlinNoise.createLegacyForBlendedNoise(random, IntStream.rangeClosed(-7, 0));
        this.depthNoise = PerlinNoise.createLegacyForBlendedNoise(random, IntStream.rangeClosed(-15, 0));
        this.cheeseCaveNoise = PerlinNoise.create(random.fork(), IntStream.rangeClosed(-2, 0));
        this.noodleCaveNoise = PerlinNoise.create(random.fork(), IntStream.rangeClosed(-2, 0));
    }

    public static boolean generateBelowZero() {
        try {
            return UADimensionConfig.generateBelowZero.get();
        } catch (IllegalStateException | NullPointerException ignored) {
            return false;
        }
    }

    /** Terrain floor Y used by the sampler / chunk generator. */
    public static int minY() {
        return generateBelowZero() ? MIN_Y_BELOW_ZERO : MIN_Y;
    }

    /** Vertical noise cell count for the active minY setting. */
    public static int noiseSizeY() {
        return generateBelowZero() ? NOISE_SIZE_Y_BELOW_ZERO : NOISE_SIZE_Y;
    }

    /** Trilinear lerp of eight noise-cell corners, then density shaping. */
    public static double densityFromCorners(
            double c00, double c01, double c10, double c11,
            double c00u, double c01u, double c10u, double c11u,
            double xd, double yd, double zd
    ) {
        double d9 = Mth.lerp(yd, c00, c00u);
        double d10 = Mth.lerp(yd, c10, c10u);
        double d11 = Mth.lerp(yd, c01, c01u);
        double d12 = Mth.lerp(yd, c11, c11u);
        double d14 = Mth.lerp(xd, d9, d10);
        double d15 = Mth.lerp(xd, d11, d12);
        return shapeDensity(Mth.lerp(zd, d14, d15));
    }

    public static double shapeDensity(double raw) {
        double noiseValue = Mth.clamp(raw / 200.0D, -1.0D, 1.0D);
        return noiseValue / 2.0D - noiseValue * noiseValue * noiseValue / 24.0D;
    }

    public void fillNoiseColumn(double[] noiseColumn, int noiseX, int noiseZ) {
        int sizeY = noiseSizeY();
        int terrainMinY = minY();
        boolean belowZero = generateBelowZero();
        double horizontalScale = 684.412D * XZ_SCALE;
        double verticalScale = 684.412D * Y_SCALE;
        double horizontalStretch = horizontalScale / XZ_FACTOR;
        double verticalStretch = verticalScale / Y_FACTOR;
        double randomDensity = randomDensityOffset(noiseX, noiseZ);

        for (int noiseY = 0; noiseY <= sizeY; ++noiseY) {
            double sample = sampleNoise(noiseX, noiseY, noiseZ, horizontalScale, verticalScale, horizontalStretch, verticalStretch);
            double d8 = 1.0D - (double) noiseY * 2.0D / (double) sizeY + randomDensity;
            double d9 = d8 * DENSITY_FACTOR + DENSITY_OFFSET;
            double d10 = (d9 + DENSITY_BIAS) * DENSITY_SCALE;
            if (d10 > 0.0D) {
                sample = sample + d10 * 4.0D;
            } else {
                sample = sample + d10;
            }

            if (TOP_SLIDE_SIZE > 0.0D) {
                double t = ((double) (sizeY - noiseY) - TOP_SLIDE_OFFSET) / TOP_SLIDE_SIZE;
                sample = Mth.clampedLerp(TOP_SLIDE_TARGET, sample, t);
            }
            if (BOTTOM_SLIDE_SIZE > 0.0D) {
                double t = ((double) noiseY - BOTTOM_SLIDE_OFFSET) / BOTTOM_SLIDE_SIZE;
                sample = Mth.clampedLerp(BOTTOM_SLIDE_TARGET, sample, t);
            }

            if (belowZero) {
                sample = applyBelowZeroCaveDensity(sample, noiseX, noiseY, noiseZ, terrainMinY);
            }

            noiseColumn[noiseY] = sample;
        }
    }

    /**
     * Below Y=0: densify into a deepslate-like mass (vanilla deep layer), then carve
     * cheese/noodle caves similar to 1.18+ noise caves. Cavities stay dry (see terrainBlock).
     */
    private double applyBelowZeroCaveDensity(double sample, int noiseX, int noiseY, int noiseZ, int terrainMinY) {
        int approxY = terrainMinY + noiseY * CELL_HEIGHT;
        if (approxY >= 8) {
            return sample;
        }

        // Near-solid deepslate mass like vanilla below Y=0.
        double depthFactor = Mth.clamp((8.0D - approxY) / 72.0D, 0.0D, 1.0D);
        sample = Mth.lerp(depthFactor, sample, sample + 120.0D);
        if (approxY < 0) {
            sample = Math.max(sample, 40.0D);
        }

        if (approxY >= 0) {
            return sample;
        }

        // Cheese caves (large hollow pockets) — scaled closer to vanilla cheese frequency.
        double cx = noiseX * 0.35D;
        double cy = noiseY * 0.55D;
        double cz = noiseZ * 0.35D;
        double cheese = this.cheeseCaveNoise.getValue(cx, cy, cz);
        double cheeseThreshold = 0.14D + (1.0D - depthFactor) * 0.03D;
        if (Math.abs(cheese) < cheeseThreshold) {
            sample = -100.0D;
        }

        // Noodle caves (tunnels).
        double nx = noiseX * 0.75D + 40.0D;
        double ny = noiseY * 0.95D;
        double nz = noiseZ * 0.75D - 40.0D;
        double noodleA = this.noodleCaveNoise.getValue(nx, ny, nz);
        double noodleB = this.noodleCaveNoise.getValue(nx + 5.2D, ny + 1.3D, nz - 3.7D);
        double noodle = Math.sqrt(noodleA * noodleA + noodleB * noodleB);
        if (noodle < 0.10D) {
            sample = -110.0D;
        }

        // Keep a solid band above bedrock so caves cannot open into the floor.
        if (approxY <= terrainMinY + 6) {
            sample = Math.max(sample, 80.0D);
        }

        return sample;
    }

    private double randomDensityOffset(int noiseX, int noiseZ) {
        double d0 = depthNoise.getValue(noiseX * 200, 10.0D, noiseZ * 200, 1.0D, 0.0D, true);
        if (d0 < 0.0D) {
            d0 *= 3.0D;
        }
        double d2 = d0 * 24.575625D - 2.0D;
        return d2 < 0.0D ? d2 * 0.009486607142857142D : Math.min(d2, 1.0D) * 0.006640625D;
    }

    private double sampleNoise(int x, int y, int z, double horizontalScale, double verticalScale,
                               double horizontalStretch, double verticalStretch) {
        double frequency = 1.0;
        double interpolationValue = 0.0;

        for (int octave = 0; octave < 8; octave++) {
            double scaledVerticalScale = verticalStretch * frequency;
            double scaledY = y * scaledVerticalScale;
            ImprovedNoise sampler = mainNoise.getOctaveNoise(octave);
            if (sampler != null) {
                interpolationValue += sampleOctave(sampler,
                        PerlinNoise.wrap(x * horizontalStretch * frequency),
                        PerlinNoise.wrap(scaledY),
                        PerlinNoise.wrap(z * horizontalStretch * frequency),
                        scaledVerticalScale, scaledY, frequency);
            }
            frequency /= 2.0;
        }

        double clampedInterpolation = (interpolationValue / 10.0 + 1.0) / 2.0;

        if (clampedInterpolation >= 1) {
            return sampleLimitNoise(maxLimitNoise, x, y, z, horizontalScale, verticalScale) / 512.0;
        } else if (clampedInterpolation <= 0) {
            return sampleLimitNoise(minLimitNoise, x, y, z, horizontalScale, verticalScale) / 512.0;
        } else {
            double lowerNoise = sampleLimitNoise(minLimitNoise, x, y, z, horizontalScale, verticalScale);
            double upperNoise = sampleLimitNoise(maxLimitNoise, x, y, z, horizontalScale, verticalScale);
            return Mth.lerp(clampedInterpolation, lowerNoise / 512.0, upperNoise / 512.0);
        }
    }

    private static double sampleLimitNoise(PerlinNoise noise, int x, int y, int z,
                                           double horizontalScale, double verticalScale) {
        double frequency = 1.0;
        double result = 0.0;
        for (int octave = 0; octave < 16; octave++) {
            double scaledVerticalScale = verticalScale * frequency;
            double scaledY = y * scaledVerticalScale;
            ImprovedNoise sampler = noise.getOctaveNoise(octave);
            if (sampler != null) {
                result += sampleOctave(sampler,
                        PerlinNoise.wrap(x * horizontalScale * frequency),
                        PerlinNoise.wrap(scaledY),
                        PerlinNoise.wrap(z * horizontalScale * frequency),
                        scaledVerticalScale, scaledY, frequency);
            }
            frequency /= 2.0;
        }
        return result;
    }

    private static double sampleOctave(ImprovedNoise sampler, double x, double y, double z,
                                       double scaledVerticalScale, double scaledY, double frequency) {
        return sampler.noise(x, y, z, scaledVerticalScale, scaledY) / frequency;
    }
}
