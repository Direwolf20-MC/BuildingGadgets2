package com.direwolf20.buildinggadgets2.setup;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.blocks.TemplateManager;
import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.items.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;


import java.util.function.Supplier;

import static com.direwolf20.buildinggadgets2.BuildingGadgets2.MODID;
import static com.direwolf20.buildinggadgets2.client.particles.ModParticles.PARTICLE_TYPES;

public class Registration {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MODID);
    private static final DeferredRegister<SoundEvent> SOUND_REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, BuildingGadgets2.MODID);
    public static final Supplier<SoundEvent> BEEP = SOUND_REGISTRY.register("beep", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BuildingGadgets2.MODID, "beep")));

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
    public static final Supplier<Block> RenderBlock = BLOCKS.register("render_block", RenderBlock::new);
    public static final Supplier<Block> TemplateManager = BLOCKS.register("template_manager", TemplateManager::new);
    public static final Supplier<Item> TemplateManager_ITEM = ITEMS.register("template_manager", () -> new BlockItem(TemplateManager.get(), new Item.Properties()));

    //BlockEntities (Not TileEntities - Honest)
    public static final Supplier<BlockEntityType<RenderBlockBE>> RenderBlock_BE = BLOCK_ENTITIES.register("renderblock", () -> BlockEntityType.Builder.of(RenderBlockBE::new, RenderBlock.get()).build(null));
    public static final Supplier<BlockEntityType<TemplateManagerBE>> TemplateManager_BE = BLOCK_ENTITIES.register("templatemanager", () -> BlockEntityType.Builder.of(TemplateManagerBE::new, TemplateManager.get()).build(null));
    //public static final RegistryObject<BlockEntityType<LaserConnectorBE>> LaserConnector_BE = BLOCK_ENTITIES.register("laserconnector", () -> BlockEntityType.Builder.of(LaserConnectorBE::new, LaserConnector.get()).build(null));

    //Items
    public static final Supplier<Item> Building_Gadget = ITEMS.register("gadget_building", GadgetBuilding::new);
    public static final Supplier<Item> Exchanging_Gadget = ITEMS.register("gadget_exchanging", GadgetExchanger::new);
    public static final Supplier<Item> CopyPaste_Gadget = ITEMS.register("gadget_copy_paste", GadgetCopyPaste::new);
    public static final Supplier<Item> CutPaste_Gadget = ITEMS.register("gadget_cut_paste", GadgetCutPaste::new);
    public static final Supplier<Item> Destruction_Gadget = ITEMS.register("gadget_destruction", GadgetDestruction::new);
    public static final Supplier<Item> Template = ITEMS.register("template", TemplateItem::new);

    //Containers
    public static final Supplier<MenuType<TemplateManagerContainer>> TemplateManager_Container = CONTAINERS.register("templatemanager",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TemplateManagerContainer(windowId, inv, data)));
}
