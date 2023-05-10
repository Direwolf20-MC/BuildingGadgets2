package com.direwolf20.buildinggadgets2.setup;

import com.direwolf20.buildinggadgets2.common.items.GadgetBuilding;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.direwolf20.buildinggadgets2.common.BuildingGadgets2.MODID;

public class Registration {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    //public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    //public static final RegistryObject<CardClearRecipe.Serializer> CARD_CLEAR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("cardclear", CardClearRecipe.Serializer::new);
    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
        //PARTICLE_TYPES.register(bus);
        //RECIPE_SERIALIZERS.register(bus);
    }

    // Some common properties for our blocks and items
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);

    //Blocks
    //public static final RegistryObject<Block> LaserConnector = BLOCKS.register("laser_connector", LaserConnector::new);
    //public static final RegistryObject<Item> LaserConnector_ITEM = fromBlock(LaserConnector);

    //BlockEntities (Not TileEntities - Honest)
    //public static final RegistryObject<BlockEntityType<LaserNodeBE>> LaserNode_BE = BLOCK_ENTITIES.register("lasernode", () -> BlockEntityType.Builder.of(LaserNodeBE::new, LaserNode.get()).build(null));
    //public static final RegistryObject<BlockEntityType<LaserConnectorBE>> LaserConnector_BE = BLOCK_ENTITIES.register("laserconnector", () -> BlockEntityType.Builder.of(LaserConnectorBE::new, LaserConnector.get()).build(null));

    //Items
    public static final RegistryObject<Item> Building_Gadget = ITEMS.register("gadget_building", GadgetBuilding::new);

    //Containers
    //public static final RegistryObject<MenuType<LaserNodeContainer>> LaserNode_Container = CONTAINERS.register("lasernode",
    //        () -> IForgeMenuType.create((windowId, inv, data) -> new LaserNodeContainer(windowId, inv, inv.player, data)));

    // Conveniance function: Take a RegistryObject<Block> and make a corresponding RegistryObject<Item> from it
    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    }
}
