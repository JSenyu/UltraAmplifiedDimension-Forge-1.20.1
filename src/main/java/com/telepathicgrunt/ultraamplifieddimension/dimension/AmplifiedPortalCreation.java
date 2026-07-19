package com.telepathicgrunt.ultraamplifieddimension.dimension;

import com.telepathicgrunt.ultraamplifieddimension.modInit.UADBlocks;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADTags;
import com.telepathicgrunt.ultraamplifieddimension.world.features.AmplifiedPortalFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

public final class AmplifiedPortalCreation {
    private AmplifiedPortalCreation() {
    }

    public static void portalCreationRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        if (player.isShiftKeyDown()) {
            return;
        }
        if (!OverworldIntegration.portalsEnabled()) {
            return;
        }
        if (!event.getItemStack().is(UADTags.PORTAL_ACTIVATION_ITEMS)) {
            return;
        }
        if (!isValid(level, event.getPos())) {
            return;
        }

        // Deny flint-and-steel fire placement so it cannot fight portal activation.
        event.setCanceled(true);
        event.setUseItem(Event.Result.DENY);
        event.setUseBlock(Event.Result.DENY);

        if (level.isClientSide()) {
            player.swing(event.getHand());
            return;
        }

        if (trySpawnPortal(level, event.getPos())) {
            player.swing(event.getHand(), true);
        }
    }

    public static boolean checkForGeneratedPortal(LevelAccessor worldUA) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(8, worldUA.getMaxBuildHeight(), 8);
        worldUA.getChunk(pos);
        while (pos.getY() >= worldUA.getMinBuildHeight()) {
            if (worldUA.getBlockState(pos).is(UADBlocks.AMPLIFIED_PORTAL.get())) {
                return true;
            }
            pos.move(0, -1, 0);
        }
        return false;
    }

    public static void generatePortal(ServerLevel worldUA) {
        AmplifiedPortalFrame amplifiedPortalFrame = new AmplifiedPortalFrame();
        BlockPos pos = new BlockPos(8, worldUA.getMaxBuildHeight(), 8);
        worldUA.getChunk(pos);
        pos = worldUA.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        if (pos.getY() > worldUA.getMaxBuildHeight() - 4) {
            pos = pos.below(3);
        } else if (pos.getY() < worldUA.getMinBuildHeight() + 6) {
            pos = new BlockPos(pos.getX(), worldUA.getMinBuildHeight() + 6, pos.getZ());
        }
        amplifiedPortalFrame.generate(worldUA, pos);
    }

    public static boolean isValid(LevelAccessor world, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x * z) == 1) {
                    if (!world.getBlockState(pos.offset(x, -1, z)).is(UADTags.PORTAL_CORNER_BLOCKS)) {
                        return false;
                    }
                } else {
                    BlockState currentFloor = world.getBlockState(pos.offset(x, -1, z));
                    if (!(currentFloor.is(UADTags.PORTAL_NON_CORNER_BLOCKS)
                            && (!currentFloor.hasProperty(SlabBlock.TYPE) || currentFloor.getValue(SlabBlock.TYPE) == SlabType.BOTTOM))) {
                        return false;
                    }
                }
            }
        }

        if (!world.getBlockState(pos).is(UADTags.PORTAL_CENTER_BLOCKS)) {
            return false;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x * z) == 1) {
                    if (!world.getBlockState(pos.offset(x, 1, z)).is(UADTags.PORTAL_CORNER_BLOCKS)) {
                        return false;
                    }
                } else {
                    BlockState currentCeiling = world.getBlockState(pos.offset(x, 1, z));
                    if (!(currentCeiling.is(UADTags.PORTAL_NON_CORNER_BLOCKS)
                            && (!currentCeiling.hasProperty(SlabBlock.TYPE) || currentCeiling.getValue(SlabBlock.TYPE) == SlabType.TOP))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean trySpawnPortal(LevelAccessor world, BlockPos pos) {
        if (isValid(world, pos)) {
            world.setBlock(pos, UADBlocks.AMPLIFIED_PORTAL.get().defaultBlockState(), 18);
            return true;
        }
        return false;
    }
}
