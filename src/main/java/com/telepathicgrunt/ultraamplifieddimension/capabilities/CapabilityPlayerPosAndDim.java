package com.telepathicgrunt.ultraamplifieddimension.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class CapabilityPlayerPosAndDim {
    public static final Capability<IPlayerPosAndDim> PAST_POS_AND_DIM = CapabilityManager.get(new CapabilityToken<>() {});

    private CapabilityPlayerPosAndDim() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CapabilityPlayerPosAndDim::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPlayerPosAndDim.class);
    }
}
