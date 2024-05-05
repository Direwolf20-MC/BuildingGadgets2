package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.data.RangeChangePayload;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketRangeChange {
    public static final PacketRangeChange INSTANCE = new PacketRangeChange();

    public static PacketRangeChange get() {
        return INSTANCE;
    }

    public void handle(final RangeChangePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            GadgetNBT.setToolRange(gadgetStack, payload.range());
            context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.range_set", payload.range()), true);
        });
    }
}
