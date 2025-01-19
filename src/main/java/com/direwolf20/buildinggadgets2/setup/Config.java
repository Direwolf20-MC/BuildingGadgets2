package com.direwolf20.buildinggadgets2.setup;


import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_POWER = "power";
    public static final String CATEGORY_MAXSIZE = "maxsize";
    public static final String SUBCATEGORY_BUILDINGGADGET = "building_gadget";
    public static final String SUBCATEGORY_EXCHANGINGGADGET = "exchanging_gadget";
    public static final String SUBCATEGORY_CUTPASTEGADGET = "cutpaste_gadget";
    public static final String SUBCATEGORY_COPYPASTEGADGET = "copypaste_gadget";
    public static final String SUBCATEGORY_DESTRUCTIONGADGET = "destruction_gadget";

    public static ModConfigSpec.IntValue BUILDINGGADGET_MAXPOWER;
    public static ModConfigSpec.IntValue BUILDINGGADGET_COST;

    public static ModConfigSpec.IntValue EXCHANGINGGADGET_MAXPOWER;
    public static ModConfigSpec.IntValue EXCHANGINGGADGET_COST;

    public static ModConfigSpec.IntValue CUTPASTEGADGET_MAXPOWER;
    public static ModConfigSpec.IntValue CUTPASTEGADGET_COST;
    public static ModConfigSpec.IntValue CUTPASTEGADGET_MAXSIZE;

    public static ModConfigSpec.IntValue COPYPASTEGADGET_MAXPOWER;
    public static ModConfigSpec.IntValue COPYPASTEGADGET_COST;
    public static ModConfigSpec.IntValue COPYPASTEGADGET_MAXSIZE;

    public static ModConfigSpec.IntValue DESTRUCTIONGADGET_MAXPOWER;
    public static ModConfigSpec.IntValue DESTRUCTIONGADGET_COST;

    public static ModConfigSpec.IntValue RAYTRACE_RANGE;

    public static void register(ModContainer container) {
        //registerServerConfigs();
        registerCommonConfigs(container);
        //registerClientConfigs();
    }

    private static void registerClientConfigs(ModContainer container) {
        //PowergenConfig.registerClientConfig(CLIENT_BUILDER);
        //ManaConfig.registerClientConfig(CLIENT_BUILDER);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
    }

    private static void registerCommonConfigs(ModContainer container) {
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        generalConfig();
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Power settings").push(CATEGORY_POWER);
        powerConfig();
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Max Size settings").push(CATEGORY_MAXSIZE);
        maxSizeConfig();
        COMMON_BUILDER.pop();

        container.registerConfig(ModConfig.Type.COMMON, COMMON_BUILDER.build());
    }

    private static void registerServerConfigs(ModContainer container) {
        //GeneratorConfig.registerServerConfig(SERVER_BUILDER);
        //PowergenConfig.registerServerConfig(SERVER_BUILDER);
        container.registerConfig(ModConfig.Type.SERVER, SERVER_BUILDER.build());
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
        CUTPASTEGADGET_COST = COMMON_BUILDER.comment("Base cost per block - Checked during CUT, Charged during PASTE")
                .defineInRange("baseCost", 50, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Copy Paste Gadget").push(SUBCATEGORY_COPYPASTEGADGET);
        COPYPASTEGADGET_MAXPOWER = COMMON_BUILDER.comment("Maximum power for the Copy and Paste Gadget")
                .defineInRange("maxPower", 1000000, 0, Integer.MAX_VALUE);
        COPYPASTEGADGET_COST = COMMON_BUILDER.comment("Base cost per block Paste (Copy is Free)")
                .defineInRange("baseCost", 50, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Destruction Gadget").push(SUBCATEGORY_DESTRUCTIONGADGET);
        DESTRUCTIONGADGET_MAXPOWER = COMMON_BUILDER.comment("Maximum power for the Destruction Gadget")
                .defineInRange("maxPower", 2000000, 0, Integer.MAX_VALUE);
        DESTRUCTIONGADGET_COST = COMMON_BUILDER.comment("Base cost per block Destroyed")
                .defineInRange("baseCost", 50, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();
    }

    private static void maxSizeConfig() {
        COMMON_BUILDER.comment("Cut Paste Gadget").push(SUBCATEGORY_CUTPASTEGADGET);
        CUTPASTEGADGET_MAXSIZE = COMMON_BUILDER.comment("Maximum size for the Cut and Paste Gadget")
                .defineInRange("maxSize", 100000, 10000, 1000000);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Copy Paste Gadget").push(SUBCATEGORY_COPYPASTEGADGET);
        COPYPASTEGADGET_MAXSIZE = COMMON_BUILDER.comment("Maximum size for the Copy and Paste Gadget")
                .defineInRange("maxSize", 100000, 10000, 1000000);
        COMMON_BUILDER.pop();
    }

    private static void buildingGadgetConfig() {

    }

    private static void clientConfig() {

    }

    private static void serverConfig() {

    }

}
