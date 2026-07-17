package com.telepathicgrunt.ultraamplifieddimension.blocks;

import com.mojang.datafixers.util.Pair;
import com.telepathicgrunt.ultraamplifieddimension.capabilities.CapabilityPlayerPosAndDim;
import com.telepathicgrunt.ultraamplifieddimension.capabilities.IPlayerPosAndDim;
import com.telepathicgrunt.ultraamplifieddimension.config.UADimensionConfig;
import com.telepathicgrunt.ultraamplifieddimension.dimension.AmplifiedPortalCreation;
import com.telepathicgrunt.ultraamplifieddimension.dimension.UADDimension;
import com.telepathicgrunt.ultraamplifieddimension.dimension.UADWorldSavedData;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AmplifiedPortalBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public AmplifiedPortalBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(5.0F, 3600000.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 15)
                .noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide
                && !player.isPassenger()
                && !player.isVehicle()
                && player.canChangeDimensions()
                && !player.isShiftKeyDown()
                && player.getUsedItemHand() == hand) {

            MinecraftServer server = player.getServer();
            if (server == null) {
                return InteractionResult.FAIL;
            }

            IPlayerPosAndDim cap = player.getCapability(CapabilityPlayerPosAndDim.PAST_POS_AND_DIM)
                    .orElseThrow(IllegalStateException::new);

            ResourceKey<Level> destinationKey;
            float pitch;
            float yaw;
            boolean enteringUA;

            if (player.level().dimension().equals(UADDimension.UAD_WORLD_KEY)) {
                destinationKey = UADimensionConfig.forceExitToOverworld.get() || cap.getNonUADim() == null
                        ? Level.OVERWORLD
                        : cap.getNonUADim();
                pitch = cap.getNonUAPitch();
                yaw = cap.getNonUAYaw();
                enteringUA = false;
                cap.setUAPos(player.position());
                cap.setUAPitch(player.getXRot());
                cap.setUAYaw(player.getYRot());
            } else {
                destinationKey = UADDimension.UAD_WORLD_KEY;
                pitch = cap.getUAPitch();
                yaw = cap.getUAYaw();
                enteringUA = true;
                cap.setNonUAPos(player.position());
                cap.setNonUADim(player.level().dimension());
                cap.setNonUAPitch(player.getXRot());
                cap.setNonUAYaw(player.getYRot());
            }

            ServerLevel destinationWorld = server.getLevel(destinationKey);
            if (destinationWorld == null) {
                destinationKey = Level.OVERWORLD;
                destinationWorld = server.getLevel(destinationKey);
            }
            if (destinationWorld == null) {
                return InteractionResult.FAIL;
            }

            if (destinationKey.equals(UADDimension.UAD_WORLD_KEY)
                    && !AmplifiedPortalCreation.checkForGeneratedPortal(destinationWorld)) {
                AmplifiedPortalCreation.generatePortal(destinationWorld);
            }

            Vec3 playerVec3Pos;
            if (enteringUA && cap.getUAPos() == null) {
                BlockPos worldOriginBlockPos = new BlockPos(10, destinationWorld.getMinBuildHeight(), 8);
                int portalY = destinationWorld.getMaxBuildHeight() - 1;
                while (portalY >= destinationWorld.getMinBuildHeight()) {
                    if (destinationWorld.getBlockState(worldOriginBlockPos.atY(portalY)).is(UADBlocks.AMPLIFIED_PORTAL.get())) {
                        break;
                    }
                    portalY--;
                }

                if (portalY < destinationWorld.getMinBuildHeight()) {
                    playerVec3Pos = Vec3.atBottomCenterOf(destinationWorld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldOriginBlockPos)).add(0, 0.5D, 0);
                } else {
                    worldOriginBlockPos = worldOriginBlockPos.atY(portalY - 1);
                    boolean validSpaceFound = false;
                    outer:
                    for (int x = -2; x < 3; x++) {
                        for (int z = -2; z < 3; z++) {
                            if (x == -2 || x == 2 || z == -2 || z == 2) {
                                BlockPos check = worldOriginBlockPos.offset(x, 0, z);
                                if (destinationWorld.getBlockState(check).isAir()
                                        && destinationWorld.getBlockState(check.above()).isAir()) {
                                    worldOriginBlockPos = check;
                                    validSpaceFound = true;
                                    break outer;
                                }
                            }
                        }
                    }
                    if (!validSpaceFound) {
                        worldOriginBlockPos = destinationWorld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(10, 0, 8));
                    }
                    playerVec3Pos = Vec3.atBottomCenterOf(worldOriginBlockPos).add(0, -0.3D, 0);
                }
            } else if (enteringUA) {
                playerVec3Pos = cap.getUAPos();
            } else if (cap.getNonUAPos() == null || UADimensionConfig.forceExitToOverworld.get()) {
                BlockPos spawnPos = destinationWorld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, destinationWorld.getSharedSpawnPos());
                while (!destinationWorld.getBlockState(spawnPos).isAir() && spawnPos.getY() < destinationWorld.getMaxBuildHeight() - 2) {
                    spawnPos = spawnPos.above();
                }
                destinationWorld.setBlockAndUpdate(spawnPos, UADBlocks.AMPLIFIED_PORTAL.get().defaultBlockState());
                playerVec3Pos = Vec3.atBottomCenterOf(spawnPos).add(0, 0.5D, 0);
            } else {
                playerVec3Pos = cap.getNonUAPos();
            }

            if (player.isSleeping()) {
                player.stopSleeping();
            }

            UADWorldSavedData.get((ServerLevel) level).addPlayer(player, destinationKey, playerVec3Pos, Pair.of(yaw, pitch));
            createLotsOfParticles((ServerLevel) level, player.position(), level.random);
            return InteractionResult.SUCCESS;
        }

        return super.use(state, level, position, player, hand, hit);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (player != null && player.isCreative()) {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }

        if (level.dimension().equals(UADDimension.UAD_WORLD_KEY) && pos.getX() == 8 && pos.getZ() == 8) {
            BlockPos.MutableBlockPos highest = new BlockPos.MutableBlockPos(pos.getX(), level.getMaxBuildHeight() - 1, pos.getZ());
            while (highest.getY() >= level.getMinBuildHeight()) {
                if (level.getBlockState(highest).is(UADBlocks.AMPLIFIED_PORTAL.get())) {
                    break;
                }
                highest.move(0, -1, 0);
            }
            if (highest.getY() == pos.getY()) {
                return false;
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            createLotsOfParticles(serverLevel, Vec3.atLowerCornerOf(pos), level.random);
        }
    }

    public static void createLotsOfParticles(ServerLevel level, Vec3 position, RandomSource random) {
        double xPos = position.x + 0.5D;
        double yPos = position.y + 0.5D;
        double zPos = position.z + 0.5D;
        double xOffset = (random.nextFloat() - 0.4D) * 0.8D;
        double zOffset = (random.nextFloat() - 0.4D) * 0.8D;
        level.sendParticles(ParticleTypes.FLAME, xPos, yPos, zPos, 50, xOffset, 0, zOffset, random.nextFloat() * 0.1D + 0.05D);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double d0 = pos.getX() + (random.nextFloat() * 3 - 1);
        double d1 = pos.getY() + (random.nextFloat() * 3 - 1);
        double d2 = pos.getZ() + (random.nextFloat() * 3 - 1);
        level.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }
}
