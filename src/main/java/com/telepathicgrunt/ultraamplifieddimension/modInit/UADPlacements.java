package com.telepathicgrunt.ultraamplifieddimension.modInit;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.world.decorators.LedgeSurfacePlacer;
import com.telepathicgrunt.ultraamplifieddimension.world.decorators.NonAirSurfaceLedgePlacer;
import com.telepathicgrunt.ultraamplifieddimension.world.decorators.OffsetPlacer;
import com.telepathicgrunt.ultraamplifieddimension.world.decorators.RangeValidationPlacer;
import com.telepathicgrunt.ultraamplifieddimension.world.decorators.WaterIceSurfacePlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class UADPlacements {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, UltraAmplifiedDimension.MODID);

    public static final RegistryObject<PlacementModifierType<LedgeSurfacePlacer>> LEDGE_SURFACE_PLACER =
            PLACEMENT_MODIFIERS.register("ledge_surface_placer", () -> () -> LedgeSurfacePlacer.CODEC);
    public static final RegistryObject<PlacementModifierType<OffsetPlacer>> Y_OFFSET_PLACER =
            PLACEMENT_MODIFIERS.register("y_offset_placer", () -> () -> OffsetPlacer.CODEC);
    public static final RegistryObject<PlacementModifierType<RangeValidationPlacer>> RANGE_VALIDATION_PLACER =
            PLACEMENT_MODIFIERS.register("range_validation_placer", () -> () -> RangeValidationPlacer.CODEC);
    public static final RegistryObject<PlacementModifierType<WaterIceSurfacePlacer>> WATER_ICE_SURFACE_PLACER =
            PLACEMENT_MODIFIERS.register("water_ice_surface_placer", () -> () -> WaterIceSurfacePlacer.CODEC);
    public static final RegistryObject<PlacementModifierType<NonAirSurfaceLedgePlacer>> NON_AIR_SURFACE_LEDGE_PLACER =
            PLACEMENT_MODIFIERS.register("non_air_surface_ledge_placer", () -> () -> NonAirSurfaceLedgePlacer.CODEC);
}
