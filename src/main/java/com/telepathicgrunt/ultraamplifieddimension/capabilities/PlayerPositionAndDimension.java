package com.telepathicgrunt.ultraamplifieddimension.capabilities;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.dimension.UADDimension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PlayerPositionAndDimension implements IPlayerPosAndDim {
    public ResourceKey<Level> nonUADimension = Level.OVERWORLD;
    public Vec3 nonUABlockPos = null;
    public float nonUAPitch = 0;
    public float nonUAYaw = 0;
    public Vec3 UABlockPos = null;
    public float UAPitch = 3.75F;
    public float UAYaw = 90F;

    @Override
    public void setNonUADim(ResourceKey<Level> incomingDim) {
        if (incomingDim == UADDimension.UAD_WORLD_KEY) {
            nonUADimension = Level.OVERWORLD;
            UltraAmplifiedDimension.LOGGER.error("Tried to set NonUADimension to UA dimension.");
        } else {
            nonUADimension = incomingDim;
        }
    }

    @Override
    public ResourceKey<Level> getNonUADim() {
        return nonUADimension;
    }

    @Override
    public void setNonUAPitch(float incomingPitch) {
        nonUAPitch = incomingPitch;
    }

    @Override
    public float getNonUAPitch() {
        return nonUAPitch;
    }

    @Override
    public void setNonUAYaw(float incomingYaw) {
        nonUAYaw = incomingYaw;
    }

    @Override
    public float getNonUAYaw() {
        return nonUAYaw;
    }

    @Override
    public void setNonUAPos(Vec3 incomingPos) {
        nonUABlockPos = incomingPos;
    }

    @Override
    public Vec3 getNonUAPos() {
        return nonUABlockPos;
    }

    @Override
    public void setUAPos(Vec3 incomingPos) {
        UABlockPos = incomingPos;
    }

    @Override
    public Vec3 getUAPos() {
        return UABlockPos;
    }

    @Override
    public void setUAPitch(float incomingPitch) {
        UAPitch = incomingPitch;
    }

    @Override
    public float getUAPitch() {
        return UAPitch;
    }

    @Override
    public void setUAYaw(float incomingYaw) {
        UAYaw = incomingYaw;
    }

    @Override
    public float getUAYaw() {
        return UAYaw;
    }

    @Override
    public CompoundTag saveNBTData() {
        CompoundTag data = new CompoundTag();
        if (getNonUAPos() != null) {
            data.putDouble("NonUA_X", getNonUAPos().x);
            data.putDouble("NonUA_Y", getNonUAPos().y);
            data.putDouble("NonUA_Z", getNonUAPos().z);
        }
        data.putFloat("NonUAPitch", nonUAPitch);
        data.putFloat("NonUAYaw", nonUAYaw);
        if (getUAPos() != null) {
            data.putDouble("UA_X", getUAPos().x);
            data.putDouble("UA_Y", getUAPos().y);
            data.putDouble("UA_Z", getUAPos().z);
        }
        data.putFloat("UAPitch", UAPitch);
        data.putFloat("UAYaw", UAYaw);
        if (getNonUADim() != null) {
            data.putString("NonUADimensionNamespace", getNonUADim().location().getNamespace());
            data.putString("NonUADimensionPath", getNonUADim().location().getPath());
        }
        return data;
    }

    @Override
    public void loadNBTData(CompoundTag nbtTag) {
        ResourceLocation dimId = ResourceLocation.fromNamespaceAndPath(
                nbtTag.contains("NonUADimensionNamespace") ? nbtTag.getString("NonUADimensionNamespace") : "minecraft",
                nbtTag.contains("NonUADimensionPath") ? nbtTag.getString("NonUADimensionPath") : "overworld"
        );
        ResourceKey<Level> storedDimension = ResourceKey.create(Registries.DIMENSION, dimId);

        Vec3 storedPositionNonUA = null;
        if (nbtTag.contains("NonUA_X") && nbtTag.contains("NonUA_Y") && nbtTag.contains("NonUA_Z")) {
            storedPositionNonUA = new Vec3(nbtTag.getDouble("NonUA_X"), nbtTag.getDouble("NonUA_Y"), nbtTag.getDouble("NonUA_Z"));
        }
        Vec3 storePositionUA = null;
        if (nbtTag.contains("UA_X") && nbtTag.contains("UA_Y") && nbtTag.contains("UA_Z")) {
            storePositionUA = new Vec3(nbtTag.getDouble("UA_X"), nbtTag.getDouble("UA_Y"), nbtTag.getDouble("UA_Z"));
        }

        setNonUADim(storedDimension);
        setNonUAPitch(nbtTag.getFloat("NonUAPitch"));
        setNonUAYaw(nbtTag.getFloat("NonUAYaw"));
        setNonUAPos(storedPositionNonUA);
        setUAPitch(nbtTag.getFloat("UAPitch"));
        setUAYaw(nbtTag.getFloat("UAYaw"));
        setUAPos(storePositionUA);
    }
}
