package com.telepathicgrunt.ultraamplifieddimension.client;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class UltraAmplifiedDimensionClient {
    private UltraAmplifiedDimensionClient() {
    }

    public static void subscribeClientEvents(IEventBus modEventBus) {
        modEventBus.addListener(UltraAmplifiedDimensionClient::onClientSetup);
        modEventBus.addListener(UltraAmplifiedDimensionClient::onRegisterDimensionEffects);
        modEventBus.addListener(BlockColorManager::onBlockColorsInit);
        modEventBus.addListener(BlockColorManager::onItemColorsInit);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // Render types are declared in block model JSONs (render_type) for 1.20.1+.
        event.enqueueWork(() -> {
        });
    }

    private static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "sky_property"), new UADDimensionSpecialEffects());
    }
}
