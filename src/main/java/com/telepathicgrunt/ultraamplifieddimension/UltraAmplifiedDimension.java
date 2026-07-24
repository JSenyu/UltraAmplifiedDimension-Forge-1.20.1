package com.telepathicgrunt.ultraamplifieddimension;

import com.telepathicgrunt.ultraamplifieddimension.capabilities.CapabilityPlayerPosAndDim;
import com.telepathicgrunt.ultraamplifieddimension.client.UltraAmplifiedDimensionClient;
import com.telepathicgrunt.ultraamplifieddimension.config.UADimensionConfig;
import com.telepathicgrunt.ultraamplifieddimension.dimension.AmplifiedPortalCreation;
import com.telepathicgrunt.ultraamplifieddimension.dimension.OverworldIntegration;
import com.telepathicgrunt.ultraamplifieddimension.dimension.UADChunkGenerator;
import com.telepathicgrunt.ultraamplifieddimension.dimension.UADDimension;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADBlocks;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADCarvers;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADCreativeTabs;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADFeatures;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADHeightProviders;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADPlacements;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADProcessors;
import com.telepathicgrunt.ultraamplifieddimension.modInit.UADTreeDecoratorTypes;
import com.telepathicgrunt.ultraamplifieddimension.utils.BiomeSetsHelper;
import com.telepathicgrunt.ultraamplifieddimension.world.carver.CaveCavityCarver;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
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
        UADFeatures.FEATURES.register(modEventBus);
        UADProcessors.STRUCTURE_PROCESSORS.register(modEventBus);
        UADTreeDecoratorTypes.TREE_DECORATOR_TYPES.register(modEventBus);
        UADPlacements.PLACEMENT_MODIFIERS.register(modEventBus);
        UADHeightProviders.HEIGHT_PROVIDERS.register(modEventBus);
        UADCarvers.WORLD_CARVERS.register(modEventBus);

        forgeBus.addListener(UADDimension::levelTick);
        forgeBus.addListener(AmplifiedPortalCreation::portalCreationRightClick);
        forgeBus.addListener(this::onServerAboutToStart);
        forgeBus.addListener(this::onServerStarted);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            UltraAmplifiedDimensionClient.subscribeClientEvents(modEventBus);
        }

        context.registerConfig(ModConfig.Type.COMMON, UADimensionConfig.GENERAL_SPEC, "ultra_amplified_dimension.toml");
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(BiomeSetsHelper::generateBiomeSets);
    }

    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        long seed = event.getServer().getWorldData().worldGenOptions().seed();
        CaveCavityCarver.setSeed(BiomeManager.obfuscateSeed(seed));
    }

    private void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        ChunkGenerator overworldGen = overworld != null ? overworld.getChunkSource().getGenerator() : null;
        boolean uadOverworld = overworldGen instanceof UADChunkGenerator;
        boolean uadDimLoaded = server.getLevel(UADDimension.UAD_WORLD_KEY) != null;
        boolean originalOwLoaded = server.getLevel(OverworldIntegration.ORIGINAL_OVERWORLD_KEY) != null;

        LOGGER.info(
                "UAD server status: biomeSize={}, generateBelowZero={}, enableUadDimension={}, setUadAsDefaultDimension={}, overrideVanillaOverworld={}, portalsEnabled={}, overworldGenerator={}, uadOverworld={}, uadDimensionLoaded={}, originalOverworldLoaded={}",
                UADimensionConfig.biomeSize.get(),
                UADimensionConfig.generateBelowZero.get(),
                UADimensionConfig.enableUadDimension.get(),
                UADimensionConfig.setUadAsDefaultDimension.get(),
                UADimensionConfig.overrideVanillaOverworld.get(),
                OverworldIntegration.portalsEnabled(),
                overworldGen != null ? overworldGen.getClass().getSimpleName() : "null",
                uadOverworld,
                uadDimLoaded,
                originalOwLoaded
        );
    }
}

