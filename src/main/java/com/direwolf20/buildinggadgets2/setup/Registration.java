package com.direwolf20.buildinggadgets2.setup;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.blocks.TemplateManager;
import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.items.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.direwolf20.buildinggadgets2.BuildingGadgets2.MODID;
import static com.direwolf20.buildinggadgets2.client.particles.ModParticles.PARTICLE_TYPES;

public class Registration {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MODID);
    private static final DeferredRegister<SoundEvent> SOUND_REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, BuildingGadgets2.MODID);
    public static final Supplier<SoundEvent> BEEP = SOUND_REGISTRY.register("beep", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, "beep")));

    public static void init(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        CONTAINERS.register(eventBus);
        SOUND_REGISTRY.register(eventBus);
        PARTICLE_TYPES.register(eventBus);
        BG2DataComponents.genSettingToggles();
        BG2DataComponents.genSettingValues();
        BG2DataComponents.COMPONENTS.register(eventBus);
    }

    //Blocks
    public static final DeferredHolder<Block, RenderBlock> RenderBlock = BLOCKS.registerBlock(
            "render_block",
            RenderBlock::new,
            () -> BlockBehaviour.Properties.of().strength(20f).dynamicShape().noOcclusion());
    public static final DeferredHolder<Block, TemplateManager> TemplateManager = BLOCKS.registerBlock(
            "template_manager",
            TemplateManager::new,
            () -> BlockBehaviour.Properties.of().strength(2f));
    public static final DeferredHolder<Item, BlockItem> TemplateManager_ITEM = ITEMS.registerItem(
            "template_manager",
            props -> new BlockItem(TemplateManager.get(), props),
            Item.Properties::new);

    //BlockEntities (Not TileEntities - Honest)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RenderBlockBE>> RenderBlock_BE = BLOCK_ENTITIES.register("renderblock", () -> new BlockEntityType<>(RenderBlockBE::new, RenderBlock.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TemplateManagerBE>> TemplateManager_BE = BLOCK_ENTITIES.register("templatemanager", () -> new BlockEntityType<>(TemplateManagerBE::new, TemplateManager.get()));
    //public static final RegistryObject<BlockEntityType<LaserConnectorBE>> LaserConnector_BE = BLOCK_ENTITIES.register("laserconnector", () -> BlockEntityType.Builder.of(LaserConnectorBE::new, LaserConnector.get()).build(null));

    //Items
    public static final DeferredHolder<Item, GadgetBuilding> Building_Gadget = ITEMS.registerItem(
            "gadget_building",
            GadgetBuilding::new,
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, GadgetExchanger> Exchanging_Gadget = ITEMS.registerItem(
            "gadget_exchanging",
            GadgetExchanger::new,
            () -> new Item.Properties().stacksTo(1).enchantable(3));
    public static final DeferredHolder<Item, GadgetCopyPaste> CopyPaste_Gadget = ITEMS.registerItem(
            "gadget_copy_paste",
            GadgetCopyPaste::new,
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, GadgetCutPaste> CutPaste_Gadget = ITEMS.registerItem(
            "gadget_cut_paste",
            GadgetCutPaste::new,
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, GadgetDestruction> Destruction_Gadget = ITEMS.registerItem(
            "gadget_destruction",
            GadgetDestruction::new,
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, TemplateItem> Template = ITEMS.registerItem(
            "template",
            TemplateItem::new,
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, Redprint> Redprint = ITEMS.registerItem(
            "redprint",
            Redprint::new,
            () -> new Item.Properties().stacksTo(1));

    //Containers
    public static final DeferredHolder<MenuType<?>, MenuType<TemplateManagerContainer>> TemplateManager_Container = CONTAINERS.register("templatemanager",
            () -> IMenuTypeExtension.create(TemplateManagerContainer::new));
}
