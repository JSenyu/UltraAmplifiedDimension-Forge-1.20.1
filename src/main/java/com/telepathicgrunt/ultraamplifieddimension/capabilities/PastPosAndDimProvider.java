package com.telepathicgrunt.ultraamplifieddimension.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PastPosAndDimProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    private final PlayerPositionAndDimension backend = new PlayerPositionAndDimension();
    private final LazyOptional<IPlayerPosAndDim> optional = LazyOptional.of(() -> backend);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CapabilityPlayerPosAndDim.PAST_POS_AND_DIM.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return backend.saveNBTData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.loadNBTData(nbt);
    }
}
