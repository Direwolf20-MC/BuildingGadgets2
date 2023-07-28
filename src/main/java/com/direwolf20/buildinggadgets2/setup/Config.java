package com.direwolf20.buildinggadgets2.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {
    public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_POWER = "power";
    public static final String SUBCATEGORY_BUILDINGGADGET = "building_gadget";
    public static final String SUBCATEGORY_EXCHANGINGGADGET = "exchanging_gadget";
    public static final String SUBCATEGORY_CUTPASTEGADGET = "cutpaste_gadget";
    public static final String SUBCATEGORY_COPYPASTEGADGET = "copypaste_gadget";
    public static final String SUBCATEGORY_DESTRUCTIONGADGET = "destruction_gadget";

    public static ForgeConfigSpec.IntValue BUILDINGGADGET_MAXPOWER;
    public static ForgeConfigSpec.IntValue BUILDINGGADGET_COST;
    public static ForgeConfigSpec.IntValue EXCHANGINGGADGET_MAXPOWER;
    public static ForgeConfigSpec.IntValue EXCHANGINGGADGET_COST;
    public static ForgeConfigSpec.IntValue CUTPASTEGADGET_MAXPOWER;
    public static ForgeConfigSpec.IntValue CUTPASTEGADGET_COST;
    public static ForgeConfigSpec.IntValue COPYPASTEGADGET_MAXPOWER;
    public static ForgeConfigSpec.IntValue COPYPASTEGADGET_COST;
    public static ForgeConfigSpec.IntValue DESTRUCTIONGADGET_MAXPOWER;
    public static ForgeConfigSpec.IntValue DESTRUCTIONGADGET_COST;

    public static ForgeConfigSpec.IntValue RAYTRACE_RANGE;

    public static void register() {
        //registerServerConfigs();
        registerCommonConfigs();
        //registerClientConfigs();
    }

    private static void registerClientConfigs() {
        //PowergenConfig.registerClientConfig(CLIENT_BUILDER);
        //ManaConfig.registerClientConfig(CLIENT_BUILDER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
    }

    private static void registerCommonConfigs() {
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        generalConfig();
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Power settings").push(CATEGORY_POWER);
        powerConfig();
        COMMON_BUILDER.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_BUILDER.build());
    }

    private static void registerServerConfigs() {
        //GeneratorConfig.registerServerConfig(SERVER_BUILDER);
        //PowergenConfig.registerServerConfig(SERVER_BUILDER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_BUILDER.build());
    }

    private static void generalConfig() {
        RAYTRACE_RANGE = COMMON_BUILDER.comment("Maximum distance you can build at")
                .defineInRange("rayTraceRange", 32, 1, 64);
    }

    private static void powerConfig() {
        COMMON_BUILDER.comment("Building Gadget").push(SUBCATEGORY_BUILDINGGADGET);
        BUILDINGGADGET_MAXPOWER = COMMON_BUILDER.comment("Maximum power for the Building Gadget")
                .defineInRange("maxPower", 500000, 0, Integer.MAX_VALUE);
        BUILDINGGADGET_COST = COMMON_BUILDER.comment("Base cost per block placed")
                .defineInRange("baseCost", 50, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Exchanging Gadget").push(SUBCATEGORY_EXCHANGINGGADGET);
        EXCHANGINGGADGET_MAXPOWER = COMMON_BUILDER.comment("Maximum power for the Exchanging Gadget")
                .defineInRange("maxPower", 500000, 0, Integer.MAX_VALUE);
        EXCHANGINGGADGET_COST = COMMON_BUILDER.comment("Base cost per block exchanged")
                .defineInRange("baseCost", 100, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Cut Paste Gadget").push(SUBCATEGORY_CUTPASTEGADGET);
        CUTPASTEGADGET_MAXPOWER = COMMON_BUILDER.comment("Maximum power for the Cut and Paste Gadget")
                .defineInRange("maxPower", 5000000, 0, Integer.MAX_VALUE);
        CUTPASTEGADGET_COST = COMMON_BUILDER.comment("Base cost per block Cut (Paste is free)")
                .defineInRange("baseCost", 200, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Copy Paste Gadget").push(SUBCATEGORY_COPYPASTEGADGET);
        COPYPASTEGADGET_MAXPOWER = COMMON_BUILDER.comment("Maximum power for the Copy and Paste Gadget")
                .defineInRange("maxPower", 1000000, 0, Integer.MAX_VALUE);
        COPYPASTEGADGET_COST = COMMON_BUILDER.comment("Base cost per block Paste (Copy is Free)")
                .defineInRange("baseCost", 50, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Destruction Gadget").push(SUBCATEGORY_DESTRUCTIONGADGET);
        DESTRUCTIONGADGET_MAXPOWER = COMMON_BUILDER.comment("Maximum power for the Destruction Gadget")
                .defineInRange("maxPower", 1000000, 0, Integer.MAX_VALUE);
        DESTRUCTIONGADGET_COST = COMMON_BUILDER.comment("Base cost per block Destroyed")
                .defineInRange("baseCost", 200, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();
    }

    private static void buildingGadgetConfig() {

    }

    private static void clientConfig() {

    }

    private static void serverConfig() {

    }

}
