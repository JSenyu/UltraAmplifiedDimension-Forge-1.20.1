package com.telepathicgrunt.ultraamplifieddimension.modInit;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.world.carver.CaveCavityCarver;
import com.telepathicgrunt.ultraamplifieddimension.world.carver.RavineCarver;
import com.telepathicgrunt.ultraamplifieddimension.world.carver.SuperLongRavineCarver;
import com.telepathicgrunt.ultraamplifieddimension.world.carver.UnderwaterCaveCarver;
import com.telepathicgrunt.ultraamplifieddimension.world.carver.configs.CaveConfig;
import com.telepathicgrunt.ultraamplifieddimension.world.carver.configs.RavineConfig;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class UADCarvers {
    public static final DeferredRegister<WorldCarver<?>> WORLD_CARVERS =
            DeferredRegister.create(Registries.CARVER, UltraAmplifiedDimension.MODID);

    public static final RegistryObject<WorldCarver<RavineConfig>> RAVINE_CARVER =
            WORLD_CARVERS.register("ravine", () -> new RavineCarver(RavineConfig.CODEC));
    public static final RegistryObject<WorldCarver<RavineConfig>> LONG_RAVINE_CARVER =
            WORLD_CARVERS.register("long_ravine", () -> new SuperLongRavineCarver(RavineConfig.CODEC));
    public static final RegistryObject<WorldCarver<CaveConfig>> CAVE_CAVITY_CARVER =
            WORLD_CARVERS.register("cave_cavity", () -> new CaveCavityCarver(CaveConfig.CODEC));
    public static final RegistryObject<WorldCarver<CaveCarverConfiguration>> UNDERWATER_CAVE_CARVER =
            WORLD_CARVERS.register("underwater_cave", () -> new UnderwaterCaveCarver(CaveCarverConfiguration.CODEC));
}
