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
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.direwolf20.buildinggadgets2.BuildingGadgets2.MODID;
import static com.direwolf20.buildinggadgets2.client.particles.ModParticles.PARTICLE_TYPES;

public class Registration {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MODID);
    private static final DeferredRegister<SoundEvent> SOUND_REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, BuildingGadgets2.MODID);
    public static final Supplier<SoundEvent> BEEP = SOUND_REGISTRY.register("beep", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2.MODID, "beep")));

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
    public static final DeferredHolder<Block, RenderBlock> RenderBlock = BLOCKS.register("render_block", RenderBlock::new);
    public static final DeferredHolder<Block, TemplateManager> TemplateManager = BLOCKS.register("template_manager", TemplateManager::new);
    public static final DeferredHolder<Item, BlockItem> TemplateManager_ITEM = ITEMS.register("template_manager", () -> new BlockItem(TemplateManager.get(), new Item.Properties()));

    //BlockEntities (Not TileEntities - Honest)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RenderBlockBE>> RenderBlock_BE = BLOCK_ENTITIES.register("renderblock", () -> BlockEntityType.Builder.of(RenderBlockBE::new, RenderBlock.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TemplateManagerBE>> TemplateManager_BE = BLOCK_ENTITIES.register("templatemanager", () -> BlockEntityType.Builder.of(TemplateManagerBE::new, TemplateManager.get()).build(null));
    //public static final RegistryObject<BlockEntityType<LaserConnectorBE>> LaserConnector_BE = BLOCK_ENTITIES.register("laserconnector", () -> BlockEntityType.Builder.of(LaserConnectorBE::new, LaserConnector.get()).build(null));

    //Items
    public static final DeferredHolder<Item, GadgetBuilding> Building_Gadget = ITEMS.register("gadget_building", GadgetBuilding::new);
    public static final DeferredHolder<Item, GadgetExchanger> Exchanging_Gadget = ITEMS.register("gadget_exchanging", GadgetExchanger::new);
    public static final DeferredHolder<Item, GadgetCopyPaste> CopyPaste_Gadget = ITEMS.register("gadget_copy_paste", GadgetCopyPaste::new);
    public static final DeferredHolder<Item, GadgetCutPaste> CutPaste_Gadget = ITEMS.register("gadget_cut_paste", GadgetCutPaste::new);
    public static final DeferredHolder<Item, GadgetDestruction> Destruction_Gadget = ITEMS.register("gadget_destruction", GadgetDestruction::new);
    public static final DeferredHolder<Item, TemplateItem> Template = ITEMS.register("template", TemplateItem::new);
    public static final DeferredHolder<Item, Redprint> Redprint = ITEMS.register("redprint", Redprint::new);

    //Containers
    public static final DeferredHolder<MenuType<?>, MenuType<TemplateManagerContainer>> TemplateManager_Container = CONTAINERS.register("templatemanager",
            () -> IMenuTypeExtension.create(TemplateManagerContainer::new));
}
