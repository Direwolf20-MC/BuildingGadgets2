package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.network.data.SendCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.network.data.SendPastePayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class PacketSendPaste {
    public static final PacketSendPaste INSTANCE = new PacketSendPaste();

    public static PacketSendPaste get() {
        return INSTANCE;
    }

    public void handle(final SendPastePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            AbstractContainerMenu container = player.containerMenu;
            if (container == null || !(container instanceof TemplateManagerContainer))
                return;

            ItemStack templateStack = container.getSlot(1).getItem();

            if (templateStack.isEmpty())
                return;

            if (templateStack.is(Items.PAPER)) {
                container.setItem(1, container.getStateId(), new ItemStack(Registration.TEMPLATE.get()));
                templateStack = container.getSlot(1).getItem();
            }

            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(player.level().getServer()).overworld());

            ArrayList<StatePos> buildList = BG2Data.statePosListFromNBTMapArray(payload.tag());
            bg2Data.addToCopyPaste(GadgetNBT.getUUID(templateStack), buildList);
            GadgetNBT.setCopyUUID(templateStack, payload.copyUUID());

            //Update the client - Yes - even though this came from the client!! This is to make sure the server sanity checked the blocks list
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(GadgetNBT.getUUID(templateStack), false);
            ((ServerPlayer) player).connection.send(new SendCopyDataPayload(GadgetNBT.getUUID(templateStack), GadgetNBT.getCopyUUID(templateStack), tag));
        });
    }

    public static void copyData(ServerPlayer sender, ItemStack sourceStack, ItemStack targetStack) {
        UUID sourceUUID = GadgetNBT.getUUID(sourceStack);
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(sender.level().getServer()).overworld());
        ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(sourceUUID, false);
        UUID targetUUID = GadgetNBT.getUUID(targetStack);
        GadgetNBT.setCopyUUID(targetStack); //This UUID will be used to determine if the copy/paste we are rendering from the cache is old or not.
        bg2Data.addToCopyPaste(targetUUID, buildList);

        //Ensure client has the updated values for both objects
        CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(sourceUUID, false);
        sender.connection.send(new SendCopyDataPayload(sourceUUID, GadgetNBT.getCopyUUID(sourceStack), tag));

        tag = bg2Data.getCopyPasteListAsNBTMap(targetUUID, false);
        sender.connection.send(new SendCopyDataPayload(targetUUID, GadgetNBT.getCopyUUID(targetStack), tag));
    }
}
