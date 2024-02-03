package com.direwolf20.buildinggadgets2.common.network.newpackets.handler;

import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.SendCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.UpdateTemplateManagerPayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class PacketUpdateTemplateManager {
    public static final PacketUpdateTemplateManager INSTANCE = new PacketUpdateTemplateManager();

    public static PacketUpdateTemplateManager get() {
        return INSTANCE;
    }

    public void handle(final UpdateTemplateManagerPayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            var sender = context.player();
            if (sender.isEmpty()) {
                return;
            }

            var player = sender.get();
            AbstractContainerMenu container = player.containerMenu;
            if (container == null || !(container instanceof TemplateManagerContainer))
                return;

            ItemStack gadgetStack = container.getSlot(0).getItem();
            ItemStack templateStack = container.getSlot(1).getItem();
            if (payload.mode() == 0) { //Save
                if (templateStack.isEmpty()) { //Save the templateName to the Gadget if theres no paper in the slot
                    GadgetNBT.setTemplateName(gadgetStack, payload.templateName());
                    return;
                }

                if (templateStack.is(Items.PAPER)) {
                    container.setItem(1, container.getStateId(), new ItemStack(Registration.Template.get()));
                    templateStack = container.getSlot(1).getItem();
                }

                GadgetNBT.setTemplateName(templateStack, payload.templateName());

                if (gadgetStack.isEmpty())
                    return;

                copyData((ServerPlayer) player, gadgetStack, templateStack);

            } else if (payload.mode() == 1) { //Load
                if (templateStack.isEmpty() || gadgetStack.isEmpty())
                    return;

                copyData((ServerPlayer) player, templateStack, gadgetStack);
                GadgetNBT.setTemplateName(gadgetStack, GadgetNBT.getTemplateName(templateStack)); //Set gadget template name to templatestack name
            }
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
