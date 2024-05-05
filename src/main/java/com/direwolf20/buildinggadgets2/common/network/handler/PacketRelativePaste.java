package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.data.RelativePastePayload;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketRelativePaste {
    public static final PacketRelativePaste INSTANCE = new PacketRelativePaste();

    public static PacketRelativePaste get() {
        return INSTANCE;
    }

    public void handle(final RelativePastePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            GadgetNBT.setRelativePaste(gadgetStack, payload.relativePos());
            context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.relativepaste", payload.relativePos().toShortString()), true);
        });
    }
}
