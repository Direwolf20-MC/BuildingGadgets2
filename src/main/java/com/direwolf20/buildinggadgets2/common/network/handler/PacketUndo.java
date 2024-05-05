package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.data.UndoPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketUndo {
    public static final PacketUndo INSTANCE = new PacketUndo();

    public static PacketUndo get() {
        return INSTANCE;
    }

    public void handle(final UndoPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            if (gadgetStack.getItem() instanceof BaseGadget actualGadget) {
                actualGadget.undo(context.player().level(), context.player(), gadgetStack);
            }
        });
    }
}
