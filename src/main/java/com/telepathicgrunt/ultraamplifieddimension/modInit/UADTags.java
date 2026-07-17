package com.telepathicgrunt.ultraamplifieddimension.modInit;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class UADTags {
    public static final TagKey<Item> PORTAL_ACTIVATION_ITEMS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "portal_activation_items"));
    public static final TagKey<Block> PORTAL_CORNER_BLOCKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "portal_corner_blocks"));
    public static final TagKey<Block> PORTAL_NON_CORNER_BLOCKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "portal_non_corner_blocks"));
    public static final TagKey<Block> PORTAL_CENTER_BLOCKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(UltraAmplifiedDimension.MODID, "portal_center_blocks"));

    private UADTags() {
    }
}
