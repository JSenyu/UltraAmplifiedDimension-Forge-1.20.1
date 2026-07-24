package com.telepathicgrunt.ultraamplifieddimension.modInit;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.world.processors.CeilingVinePostProcessor;
import com.telepathicgrunt.ultraamplifieddimension.world.processors.ClearInvalidBlockEntityNbtProcessor;
import com.telepathicgrunt.ultraamplifieddimension.world.processors.RemoveFloatingBlocksProcessor;
import com.telepathicgrunt.ultraamplifieddimension.world.processors.ReplaceAirOnlyProcessor;
import com.telepathicgrunt.ultraamplifieddimension.world.processors.ReplaceLiquidOnlyProcessor;
import com.telepathicgrunt.ultraamplifieddimension.world.processors.SpawnerRandomizingProcessor;
import com.telepathicgrunt.ultraamplifieddimension.world.processors.WallVinePostProcessor;
import com.telepathicgrunt.ultraamplifieddimension.world.processors.WaterloggingFixProcessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class UADProcessors {

    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, UltraAmplifiedDimension.MODID);

    public static final RegistryObject<StructureProcessorType<WaterloggingFixProcessor>> WATER_FIX_PROCESSOR =
            STRUCTURE_PROCESSORS.register("water_fix_processor", () -> () -> WaterloggingFixProcessor.CODEC);

    public static final RegistryObject<StructureProcessorType<ReplaceAirOnlyProcessor>> REPLACE_AIR_ONLY_PROCESSOR =
            STRUCTURE_PROCESSORS.register("replace_air_only_processor", () -> () -> ReplaceAirOnlyProcessor.CODEC);

    public static final RegistryObject<StructureProcessorType<ReplaceLiquidOnlyProcessor>> REPLACE_LIQUIDS_ONLY_PROCESSOR =
            STRUCTURE_PROCESSORS.register("replace_liquids_only_processor", () -> () -> ReplaceLiquidOnlyProcessor.CODEC);

    public static final RegistryObject<StructureProcessorType<RemoveFloatingBlocksProcessor>> REMOVE_FLOATING_BLOCKS_PROCESSOR =
            STRUCTURE_PROCESSORS.register("remove_floating_blocks_processor", () -> () -> RemoveFloatingBlocksProcessor.CODEC);

    public static final RegistryObject<StructureProcessorType<SpawnerRandomizingProcessor>> SPAWNER_RANDOMIZING_PROCESSOR =
            STRUCTURE_PROCESSORS.register("spawner_randomizing_processor", () -> () -> SpawnerRandomizingProcessor.CODEC);

    public static final RegistryObject<StructureProcessorType<WallVinePostProcessor>> WALL_VINE_POST_PROCESSOR =
            STRUCTURE_PROCESSORS.register("wall_vine_post_processor", () -> () -> WallVinePostProcessor.CODEC);

    public static final RegistryObject<StructureProcessorType<CeilingVinePostProcessor>> CEILING_VINE_POST_PROCESSOR =
            STRUCTURE_PROCESSORS.register("ceiling_vine_post_processor", () -> () -> CeilingVinePostProcessor.CODEC);

    public static final RegistryObject<StructureProcessorType<ClearInvalidBlockEntityNbtProcessor>> CLEAR_INVALID_BLOCK_ENTITY_NBT_PROCESSOR =
            STRUCTURE_PROCESSORS.register("clear_invalid_block_entity_nbt_processor", () -> () -> ClearInvalidBlockEntityNbtProcessor.CODEC);
}
