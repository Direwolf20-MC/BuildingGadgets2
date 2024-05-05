package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.data.RenderChangePayload;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketRenderChange {
    public static final PacketRenderChange INSTANCE = new PacketRenderChange();

    public static PacketRenderChange get() {
        return INSTANCE;
    }

    public void handle(final RenderChangePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            GadgetNBT.setRenderType(gadgetStack, payload.renderType());
            context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.render_set", Component.translatable(GadgetNBT.getRenderType(gadgetStack).getLang())), true);

        });
    }
}
