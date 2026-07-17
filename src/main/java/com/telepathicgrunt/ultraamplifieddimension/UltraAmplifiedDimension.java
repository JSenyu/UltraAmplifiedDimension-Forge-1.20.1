package com.telepathicgrunt.ultraamplifieddimension;

import com.telepathicgrunt.ultraamplifieddimension.capabilities.CapabilityPlayerPosAndDim;
import com.telepathicgrunt.ultraamplifieddimension.client.UltraAmplifiedDimensionClient;
import com.telepathicgrunt.ultraamplifieddimension.config.UADimensionConfig;
import com.telepathicgrunt.ultraamplifieddimension.dimension.AmplifiedPortalCreation;
import com.telepathicgrunt.ultraamplifieddimension.dimension.UADDimension;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADBlocks;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADCreativeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UltraAmplifiedDimension.MODID)
public class UltraAmplifiedDimension {
    public static final String MODID = "ultra_amplified_dimension";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public UltraAmplifiedDimension(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        modEventBus.addListener(this::setup);
        CapabilityPlayerPosAndDim.register(modEventBus);

        UADBlocks.BLOCKS.register(modEventBus);
        UADBlocks.ITEMS.register(modEventBus);
        UADCreativeTabs.CREATIVE_TABS.register(modEventBus);
        UADDimension.CHUNK_GENERATORS.register(modEventBus);
        UADDimension.BIOME_SOURCES.register(modEventBus);

        forgeBus.addListener(UADDimension::levelTick);
        forgeBus.addListener(AmplifiedPortalCreation::portalCreationRightClick);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            UltraAmplifiedDimensionClient.subscribeClientEvents(modEventBus);
        }

        context.registerConfig(ModConfig.Type.COMMON, UADimensionConfig.GENERAL_SPEC, "ultra_amplified_dimension.toml");
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(UADDimension::setupDimension);
    }
}
