package com.direwolf20.buildinggadgets2.setup;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.blocks.TemplateManager;
import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.items.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.direwolf20.buildinggadgets2.BuildingGadgets2.MODID;
import static com.direwolf20.buildinggadgets2.client.particles.ModParticles.PARTICLE_TYPES;

public class Registration {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static final DeferredRegister<SoundEvent> SOUND_REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BuildingGadgets2.MODID);
    public static final RegistryObject<SoundEvent> BEEP = SOUND_REGISTRY.register("beep", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BuildingGadgets2.MODID, "beep")));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
        SOUND_REGISTRY.register(bus);
        PARTICLE_TYPES.register(bus);
    }

    //Blocks
    public static final RegistryObject<Block> RenderBlock = BLOCKS.register("render_block", RenderBlock::new);
    public static final RegistryObject<Block> TemplateManager = BLOCKS.register("template_manager", TemplateManager::new);
    public static final RegistryObject<Item> TemplateManager_ITEM = ITEMS.register("template_manager", () -> new BlockItem(TemplateManager.get(), new Item.Properties()));

    //BlockEntities (Not TileEntities - Honest)
    public static final RegistryObject<BlockEntityType<RenderBlockBE>> RenderBlock_BE = BLOCK_ENTITIES.register("renderblock", () -> BlockEntityType.Builder.of(RenderBlockBE::new, RenderBlock.get()).build(null));
    public static final RegistryObject<BlockEntityType<TemplateManagerBE>> TemplateManager_BE = BLOCK_ENTITIES.register("templatemanager", () -> BlockEntityType.Builder.of(TemplateManagerBE::new, TemplateManager.get()).build(null));
    //public static final RegistryObject<BlockEntityType<LaserConnectorBE>> LaserConnector_BE = BLOCK_ENTITIES.register("laserconnector", () -> BlockEntityType.Builder.of(LaserConnectorBE::new, LaserConnector.get()).build(null));

    //Items
    public static final RegistryObject<Item> Building_Gadget = ITEMS.register("gadget_building", GadgetBuilding::new);
    public static final RegistryObject<Item> Exchanging_Gadget = ITEMS.register("gadget_exchanging", GadgetExchanger::new);
    public static final RegistryObject<Item> CopyPaste_Gadget = ITEMS.register("gadget_copy_paste", GadgetCopyPaste::new);
    public static final RegistryObject<Item> CutPaste_Gadget = ITEMS.register("gadget_cut_paste", GadgetCutPaste::new);
    public static final RegistryObject<Item> Destruction_Gadget = ITEMS.register("gadget_destruction", GadgetDestruction::new);
    public static final RegistryObject<Item> Template = ITEMS.register("template", TemplateItem::new);

    //Containers
    public static final RegistryObject<MenuType<TemplateManagerContainer>> TemplateManager_Container = CONTAINERS.register("templatemanager",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TemplateManagerContainer(windowId, inv, data)));
}
