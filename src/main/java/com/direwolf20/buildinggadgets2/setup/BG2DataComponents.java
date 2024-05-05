package com.direwolf20.buildinggadgets2.setup;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BG2DataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.createDataComponents(BuildingGadgets2.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> BOUND_GLOBAL_POS = COMPONENTS.register("bound_global_pos", () -> DataComponentType.<GlobalPos>builder().persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> ANCHOR_POS = COMPONENTS.register("anchor_pos", () -> DataComponentType.<BlockPos>builder().persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> RENDER_TYPE = COMPONENTS.register("render_type", () -> DataComponentType.<Byte>builder().persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ANCHOR_SIDE = COMPONENTS.register("anchor_side", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<BlockPos>>> ANCHOR_LIST = COMPONENTS.register("anchor_list", () -> DataComponentType.<List<BlockPos>>builder().persistent(BlockPos.CODEC.listOf()).networkSynchronized(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list())).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> COPY_START_POS = COMPONENTS.register("copy_start_pos", () -> DataComponentType.<BlockPos>builder().persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> COPY_END_POS = COMPONENTS.register("copy_end_pos", () -> DataComponentType.<BlockPos>builder().persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> RELATIVE_PASTE = COMPONENTS.register("relative_paste", () -> DataComponentType.<BlockPos>builder().persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> GADGET_UUID = COMPONENTS.register("gadget_uuid", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> COPY_UUID = COMPONENTS.register("copy_uuid", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockState>> GADGET_BLOCKSTATE = COMPONENTS.register("gadget_blockstate", () -> DataComponentType.<BlockState>builder().persistent(BlockState.CODEC).networkSynchronized(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY)).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<UUID>>> UNDO_LIST = COMPONENTS.register("undo_list", () -> DataComponentType.<List<UUID>>builder().persistent(UUIDUtil.CODEC.listOf()).networkSynchronized(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list())).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GADGET_RANGE = COMPONENTS.register("gadget_range", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> TEMPLATE_NAME = COMPONENTS.register("template_name", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> GADGET_MODE = COMPONENTS.register("gadget_mode", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FORGE_ENERGY = COMPONENTS.register("forge_energy", () -> DataComponentType.<Integer>builder().persistent(Codec.INT.orElse(0)).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    public static final Map<GadgetNBT.ToggleableSettings, DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>>> SETTING_TOGGLES = new HashMap<>();
    public static final Map<GadgetNBT.IntSettings, DeferredHolder<DataComponentType<?>, DataComponentType<Integer>>> SETTING_VALUES = new HashMap<>();

    public static void genSettingToggles() {
        for (GadgetNBT.ToggleableSettings toggleableSetting : GadgetNBT.ToggleableSettings.values()) {
            DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SETTING_TOGGLE = COMPONENTS.register(toggleableSetting.getName() + "_toggle", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL.orElse(false)).networkSynchronized(ByteBufCodecs.BOOL).build());
            SETTING_TOGGLES.put(toggleableSetting, SETTING_TOGGLE);
        }
    }

    public static void genSettingValues() {
        for (GadgetNBT.IntSettings intSetting : GadgetNBT.IntSettings.values()) {
            DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SETTING_VALUE = COMPONENTS.register(intSetting.getName() + "_value", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
            SETTING_VALUES.put(intSetting, SETTING_VALUE);
        }
    }

    private static @NotNull <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, final Codec<T> codec) {
        return register(name, codec, null);
    }

    private static @NotNull <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, final Codec<T> codec, @Nullable final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        if (streamCodec == null) {
            return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).build());
        } else {
            return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build());
        }
    }
}
