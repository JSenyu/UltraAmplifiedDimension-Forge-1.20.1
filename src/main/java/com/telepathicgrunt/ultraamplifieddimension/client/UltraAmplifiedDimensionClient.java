package com.telepathicgrunt.ultraamplifieddimension.client;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class UltraAmplifiedDimensionClient {
    private UltraAmplifiedDimensionClient() {
    }

    public static void subscribeClientEvents(IEventBus modEventBus) {
        modEventBus.addListener(UltraAmplifiedDimensionClient::onRegisterDimensionEffects);
        modEventBus.addListener(BlockColorManager::onBlockColorsInit);
        modEventBus.addListener(BlockColorManager::onItemColorsInit);
    }

    private static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "sky_property"), new UADDimensionSpecialEffects());
    }
}
