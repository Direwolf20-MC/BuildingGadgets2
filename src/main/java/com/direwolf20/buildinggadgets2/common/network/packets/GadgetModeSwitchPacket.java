package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetModes;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * TODO: remove me, this is a tmp
 */
public class GadgetModeSwitchPacket {
    boolean rotate;
    ResourceLocation modeId;

    public GadgetModeSwitchPacket(ResourceLocation modeId, boolean rotate) {
        this.modeId = modeId;
    }

    public static GadgetModeSwitchPacket decode(FriendlyByteBuf buf) {
        return new GadgetModeSwitchPacket(buf.readResourceLocation(), buf.readBoolean());
    }

    public static void encode(GadgetModeSwitchPacket message, FriendlyByteBuf buf) {
        buf.writeResourceLocation(message.modeId);
        buf.writeBoolean(message.rotate);
    }

    public static void handle(GadgetModeSwitchPacket message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof BaseGadget actualGadget)) {
                return;
            }

            if (message.rotate) {
                var mode = actualGadget.rotateModes(gadget);
                sender.displayClientMessage(Component.literal("Mode changed to ").append(Component.translatable("buildinggadgets2.modes." + mode.getPath())), true);
                return;
            }

            ResourceLocation modeId = message.modeId;
            ImmutableSortedSet<BaseMode> modesForGadget = GadgetModes.INSTANCE.getModesForGadget(actualGadget.gadgetTarget());

            var modeToUse = modesForGadget
                    .stream()
                    .filter(e -> e.getId().equals(modeId))
                    .findFirst()
                    .orElse(modesForGadget.first());

            GadgetNBT.setMode(gadget, modeToUse);
            sender.displayClientMessage(Component.literal("Mode changed to: ").append(Component.translatable(modeToUse.i18n())), true);
        });

        context.get().setPacketHandled(true);
    }
}
