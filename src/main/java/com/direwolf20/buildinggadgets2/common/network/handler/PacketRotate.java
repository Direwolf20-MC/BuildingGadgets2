package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import com.direwolf20.buildinggadgets2.common.network.data.RotatePayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class PacketRotate {
    public static final PacketRotate INSTANCE = new PacketRotate();

    public static PacketRotate get() {
        return INSTANCE;
    }

    public void handle(final RotatePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            UUID gadgetUUID = GadgetNBT.getUUID(gadgetStack);

            if (gadgetStack.getItem() instanceof GadgetCutPaste) {
                if (ServerTickHandler.gadgetWorking(gadgetUUID)) {
                    context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.cutinprogress"), true);
                    return; //If the gadget is mid cut, don't sync data
                }
            }

            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(context.player().level().getServer()).overworld());

            ArrayList<StatePos> currentPosList = bg2Data.getCopyPasteList(gadgetUUID, false);
            ArrayList<TagPos> tagListMutable = bg2Data.peekTEMap(gadgetUUID);

            ArrayList<StatePos> newPosList = StatePos.rotate90Degrees(currentPosList, tagListMutable);

            bg2Data.addToCopyPaste(gadgetUUID, newPosList);
            GadgetNBT.setCopyUUID(gadgetStack);

            //Handle TE Data - do nothing if null or empty
            if (tagListMutable == null || tagListMutable.isEmpty()) return;
            bg2Data.addToTEMap(gadgetUUID, tagListMutable);
        });
    }
}
