package com.direwolf20.buildinggadgets2.setup;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
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

import static com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api.MOD_ID;
import static com.direwolf20.buildinggadgets2.client.particles.ModParticles.PARTICLE_TYPES;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUND_REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, BuildingGadgets2Api.MOD_ID);
    public static final Supplier<SoundEvent> BEEP = SOUND_REGISTRY.register("beep", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "beep")));

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
    public static final DeferredHolder<Block, RenderBlock> RENDER_BLOCK = BLOCKS.register("render_block", RenderBlock::new);
    public static final DeferredHolder<Block, TemplateManager> TEMPLATE_MANAGER = BLOCKS.register("template_manager", TemplateManager::new);
    public static final DeferredHolder<Item, BlockItem> TEMPLATE_MANAGER_BLOCK_ITEM = ITEMS.register("template_manager", () -> new BlockItem(TEMPLATE_MANAGER.get(), new Item.Properties()));

    // BlockEntities (Not TileEntities - Honest)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RenderBlockBE>> RENDER_BLOCK_BLOCK_ENTITY = BLOCK_ENTITIES.register("renderblock", () -> BlockEntityType.Builder.of(RenderBlockBE::new, RENDER_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TemplateManagerBE>> TEMPLATE_MANGER_BLOCK_ENTITY = BLOCK_ENTITIES.register("templatemanager", () -> BlockEntityType.Builder.of(TemplateManagerBE::new, TEMPLATE_MANAGER.get()).build(null));

    // Items
    public static final DeferredHolder<Item, GadgetBuilding> BUILDING_GADGET = ITEMS.register("gadget_building", GadgetBuilding::new);
    public static final DeferredHolder<Item, GadgetExchanger> EXCHANGING_GADGET = ITEMS.register("gadget_exchanging", GadgetExchanger::new);
    public static final DeferredHolder<Item, GadgetCopyPaste> COPY_PASTE_GADGET = ITEMS.register("gadget_copy_paste", GadgetCopyPaste::new);
    public static final DeferredHolder<Item, GadgetCutPaste> CUT_PASTE_GADGET = ITEMS.register("gadget_cut_paste", GadgetCutPaste::new);
    public static final DeferredHolder<Item, GadgetDestruction> DESTRUCTION_GADGET = ITEMS.register("gadget_destruction", GadgetDestruction::new);
    public static final DeferredHolder<Item, TemplateItem> TEMPLATE = ITEMS.register("template", TemplateItem::new);
    public static final DeferredHolder<Item, Redprint> REDPRINT = ITEMS.register("redprint", Redprint::new);

    // Containers
    public static final DeferredHolder<MenuType<?>, MenuType<TemplateManagerContainer>> TEMPLATE_MANAGER_CONTAINER = CONTAINERS.register("templatemanager",
            () -> IMenuTypeExtension.create(TemplateManagerContainer::new));
}
