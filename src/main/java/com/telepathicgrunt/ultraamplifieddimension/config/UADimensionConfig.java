package com.telepathicgrunt.ultraamplifieddimension.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class UADimensionConfig {
	public static final ForgeConfigSpec GENERAL_SPEC;

	public static ForgeConfigSpec.BooleanValue heavyFog;
	public static ForgeConfigSpec.IntValue cloudHeight;
	public static ForgeConfigSpec.BooleanValue netherLighting;
	public static ForgeConfigSpec.ConfigValue<String> skyType;
	public static ForgeConfigSpec.BooleanValue forceExitToOverworld;
	public static ForgeConfigSpec.BooleanValue allowNetherPortal;

	public static ForgeConfigSpec.IntValue biomeSize;
	public static ForgeConfigSpec.DoubleValue subBiomeRate;
	public static ForgeConfigSpec.DoubleValue mutatedBiomeRate;

	public static ForgeConfigSpec.BooleanValue enableUadDimension;
	public static ForgeConfigSpec.BooleanValue setUadAsDefaultDimension;
	public static ForgeConfigSpec.BooleanValue overrideVanillaOverworld;

	static {
		ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
		setupConfig(configBuilder);
		GENERAL_SPEC = configBuilder.build();
	}

	private static void setupConfig(ForgeConfigSpec.Builder builder) {
		builder.comment("Appearance and portal options").push("General Dimension Options");

		heavyFog = builder
				.comment("heavy fog (not distance fog)")
				.translation("ultraamplified.config.dimension.heavyfog")
				.define("heavyFog", false);

		cloudHeight = builder
				.comment("cloud height (default 245)")
				.translation("ultraamplified.config.dimension.cloudheight")
				.defineInRange("cloudHeight", 245, -64, 512);

		netherLighting = builder
				.comment("nether-style lighting")
				.translation("ultraamplified.config.dimension.netherlighting")
				.define("netherLighting", false);

		skyType = builder
				.comment("sky type: NORMAL, END, or NONE")
				.translation("ultraamplified.config.dimension.skytype")
				.define("skyType", "NORMAL");

		allowNetherPortal = builder
				.comment("allow nether portals inside UAD")
				.translation("ultraamplified.config.dimension.allownetherportal")
				.define("allowNetherPortal", false);

		forceExitToOverworld = builder
				.comment("amplified portal always exits to overworld")
				.translation("ultraamplified.config.dimension.forceexittooverworld")
				.define("forceExitToOverworld", false);

		builder.pop();

		builder.comment("biome scale").push("Biome Size");

		biomeSize = builder
				.comment("biome size 1-20 (default 20; closer to 1.16.5). new chunks only")
				.translation("ultraamplified.config.biome.biomesize")
				.defineInRange("biomeSize", 20, 1, 20);

		subBiomeRate = builder
				.comment("sub-biome chance (default 0.44)")
				.translation("ultraamplified.config.biome.subbiomerate")
				.defineInRange("subBiomeRate", 0.44D, 0.0D, 1.0D);

		mutatedBiomeRate = builder
				.comment("mutated biome chance (default 0.42)")
				.translation("ultraamplified.config.biome.mutatedbiomerate")
				.defineInRange("mutatedBiomeRate", 0.42D, 0.0D, 1.0D);

		builder.pop();

		builder.comment("overworld integration").push("World Integration");

		enableUadDimension = builder
				.comment("enable extra UAD dimension (default true). false disables portal travel to it")
				.translation("ultraamplified.config.world.enableuaddimension")
				.define("enableUadDimension", true);

		setUadAsDefaultDimension = builder
				.comment("default world type uses UAD overworld; vanilla kept as original_overworld; portals stay. ignored if overrideVanillaOverworld")
				.translation("ultraamplified.config.world.setuadasdefaultdimension")
				.define("setUadAsDefaultDimension", false);

		overrideVanillaOverworld = builder
				.comment("replace overworld generation with UAD; disables amplified portals. overrides setUadAsDefaultDimension")
				.translation("ultraamplified.config.world.overridevanillaoverworld")
				.define("overrideVanillaOverworld", false);

		builder.pop();
	}
}
