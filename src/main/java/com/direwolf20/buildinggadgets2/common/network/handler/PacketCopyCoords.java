package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.network.data.CopyCoordsPayload;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketCopyCoords {
    public static final PacketCopyCoords INSTANCE = new PacketCopyCoords();

    public static PacketCopyCoords get() {
        return INSTANCE;
    }

    public void handle(final CopyCoordsPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            GadgetNBT.setCopyStartPos(gadgetStack, payload.startPos());
            GadgetNBT.setCopyEndPos(gadgetStack, payload.endPos());

            if (gadgetStack.getItem() instanceof GadgetCopyPaste gadgetCopyPaste) {
                BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadgetStack);
                ItemActionContext itemContext = new ItemActionContext(lookingAt.getBlockPos(), lookingAt, player, player.level(), InteractionHand.MAIN_HAND, gadgetStack);
                gadgetCopyPaste.buildAndStore(itemContext, gadgetStack);
            }
        });
    }
}
