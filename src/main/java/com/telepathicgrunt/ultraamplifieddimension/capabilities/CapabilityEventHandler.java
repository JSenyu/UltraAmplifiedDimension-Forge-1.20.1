package com.telepathicgrunt.ultraamplifieddimension.capabilities;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltraAmplifiedDimension.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEventHandler {
    public static final ResourceLocation PLAYER_PAST_POS_AND_DIM =
            ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "player_past_pos_and_dim");

    @SubscribeEvent
    public static void onAttachCapabilitiesToEntities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(PLAYER_PAST_POS_AND_DIM, new PastPosAndDimProvider());
        }
    }
}
