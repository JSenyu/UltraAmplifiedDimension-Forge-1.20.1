package com.telepathicgrunt.ultraamplifieddimension.modInit;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.blocks.AmplifiedPortalBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.BigCactusBodyBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.BigCactusCornerBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.BigCactusMainBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.CoarseGlowdirtBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.GlowdirtBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.GlowgrassBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.GlowmyceliumBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.GlowpodzolBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.GlowsandBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.GlowstoneOreBlock;
import com.telepathicgrunt.ultraamplifieddimension.blocks.RedGlowsandBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class UADBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, UltraAmplifiedDimension.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, UltraAmplifiedDimension.MODID);

    public static final RegistryObject<Block> AMPLIFIED_PORTAL = createBlock("amplified_portal", AmplifiedPortalBlock::new, true);
    public static final RegistryObject<Block> GLOWSTONE_ORE = createBlock("glowstone_ore", GlowstoneOreBlock::new, true);
    public static final RegistryObject<Block> COARSE_GLOWDIRT = createBlock("coarse_glowdirt", CoarseGlowdirtBlock::new, true);
    public static final RegistryObject<Block> GLOWDIRT = createBlock("glowdirt", GlowdirtBlock::new, true);
    public static final RegistryObject<Block> GLOWGRASS_BLOCK = createBlock("glowgrass_block", GlowgrassBlock::new, true);
    public static final RegistryObject<Block> GLOWMYCELIUM = createBlock("glowmycelium", GlowmyceliumBlock::new, true);
    public static final RegistryObject<Block> GLOWPODZOL = createBlock("glowpodzol", GlowpodzolBlock::new, true);
    public static final RegistryObject<Block> GLOWSAND = createBlock("glowsand", GlowsandBlock::new, true);
    public static final RegistryObject<Block> RED_GLOWSAND = createBlock("red_glowsand", RedGlowsandBlock::new, true);
    public static final RegistryObject<Block> BIG_CACTUS_BODY_BLOCK = createBlock("big_cactus_body_block", BigCactusBodyBlock::new, true);
    public static final RegistryObject<Block> BIG_CACTUS_CORNER_BLOCK = createBlock("big_cactus_corner_block", BigCactusCornerBlock::new, true);
    public static final RegistryObject<Block> BIG_CACTUS_MAIN_BLOCK = createBlock("big_cactus_main_block", BigCactusMainBlock::new, true);

    public static <B extends Block> RegistryObject<B> createBlock(String name, Supplier<B> block, boolean hasItem) {
        RegistryObject<B> blockHolder = BLOCKS.register(name, block);
        if (hasItem) {
            ITEMS.register(name, () -> new BlockItem(blockHolder.get(), new Item.Properties()));
        }
        return blockHolder;
    }
}
