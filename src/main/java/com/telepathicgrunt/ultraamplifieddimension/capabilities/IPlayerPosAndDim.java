package com.telepathicgrunt.ultraamplifieddimension.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IPlayerPosAndDim {
    void setNonUADim(ResourceKey<Level> incomingDim);
    ResourceKey<Level> getNonUADim();

    void setNonUAPos(Vec3 incomingPos);
    Vec3 getNonUAPos();

    void setNonUAPitch(float incomingPitch);
    float getNonUAPitch();

    void setNonUAYaw(float incomingYaw);
    float getNonUAYaw();

    void setUAPos(Vec3 incomingPos);
    Vec3 getUAPos();

    void setUAPitch(float incomingPitch);
    float getUAPitch();

    void setUAYaw(float incomingYaw);
    float getUAYaw();

    CompoundTag saveNBTData();
    void loadNBTData(CompoundTag nbtTag);
}
