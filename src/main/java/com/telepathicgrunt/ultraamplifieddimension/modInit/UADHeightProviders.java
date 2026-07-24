package com.telepathicgrunt.ultraamplifieddimension.modInit;

import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.world.placement.ConfigAncientCityHeight;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class UADHeightProviders {
    public static final DeferredRegister<HeightProviderType<?>> HEIGHT_PROVIDERS =
            DeferredRegister.create(Registries.HEIGHT_PROVIDER_TYPE, UltraAmplifiedDimension.MODID);

    public static final RegistryObject<HeightProviderType<ConfigAncientCityHeight>> CONFIG_ANCIENT_CITY =
            HEIGHT_PROVIDERS.register("config_ancient_city", () -> () -> ConfigAncientCityHeight.CODEC);
}
