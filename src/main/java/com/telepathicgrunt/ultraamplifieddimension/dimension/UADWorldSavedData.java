package com.telepathicgrunt.ultraamplifieddimension.dimension;

import com.mojang.datafixers.util.Pair;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.blocks.AmplifiedPortalBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UADWorldSavedData extends SavedData {
    public static final String DATA_KEY = UltraAmplifiedDimension.MODID + "_delayed_teleportation";

    private List<TeleportEntry> teleportingEntities = new ArrayList<>();
    private List<SpawnParticles> particles = new ArrayList<>();

    public static UADWorldSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(UADWorldSavedData::load, UADWorldSavedData::new, DATA_KEY);
    }

    public static void tick(ServerLevel level) {
        MinecraftServer server = level.getServer();
        UADWorldSavedData data = get(level);

        List<TeleportEntry> entityList = data.teleportingEntities;
        data.teleportingEntities = new ArrayList<>();

        List<SpawnParticles> particleList = data.particles;
        data.particles = new ArrayList<>();

        for (SpawnParticles entry : particleList) {
            ServerLevel targetWorld = server.getLevel(entry.targetWorld);
            if (targetWorld != null) {
                AmplifiedPortalBlock.createLotsOfParticles(targetWorld, entry.targetVec, targetWorld.random);
            }
        }

        for (TeleportEntry entry : entityList) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.playerUUID);
            ServerLevel targetWorld = server.getLevel(entry.targetWorld);
            if (player != null && targetWorld != null && player.level() == level) {
                ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());
                targetWorld.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, playerChunkPos, 1, player.getId());

                player.fallDistance = 0;
                player.teleportTo(
                        targetWorld,
                        entry.targetVec.x,
                        entry.targetVec.y + 0.2D,
                        entry.targetVec.z,
                        entry.targetLook.getFirst(),
                        entry.targetLook.getSecond()
                );

                data.addParticle(entry.targetWorld, new Vec3(entry.targetVec.x - 0.5D, entry.targetVec.y, entry.targetVec.z - 0.5D));
            }
        }
    }

    public void addPlayer(Player player, ResourceKey<Level> destination, Vec3 targetVec, Pair<Float, Float> targetLook) {
        this.teleportingEntities.add(new TeleportEntry(player.getUUID(), destination, targetVec, targetLook));
    }

    public void addParticle(ResourceKey<Level> destination, Vec3 targetVec) {
        this.particles.add(new SpawnParticles(destination, targetVec));
    }

    public static UADWorldSavedData load(CompoundTag tag) {
        return new UADWorldSavedData();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    private record TeleportEntry(UUID playerUUID, ResourceKey<Level> targetWorld, Vec3 targetVec, Pair<Float, Float> targetLook) {
    }

    private record SpawnParticles(ResourceKey<Level> targetWorld, Vec3 targetVec) {
    }
}
