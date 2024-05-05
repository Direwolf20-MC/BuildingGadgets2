package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import com.direwolf20.buildinggadgets2.common.network.data.CutPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketCut {
    public static final PacketCut INSTANCE = new PacketCut();

    public static PacketCut get() {
        return INSTANCE;
    }

    public void handle(final CutPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            if (gadgetStack.getItem() instanceof GadgetCutPaste gadgetCopyPaste) {
                gadgetCopyPaste.cutAndStore(context.player(), gadgetStack);
            }
        });
    }
}
