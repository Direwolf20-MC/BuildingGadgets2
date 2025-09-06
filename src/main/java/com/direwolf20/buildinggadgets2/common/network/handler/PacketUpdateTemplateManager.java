package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.network.data.SendCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.network.data.UpdateTemplateManagerPayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static com.direwolf20.buildinggadgets2.util.MiscHelpers.playSound;

public class PacketUpdateTemplateManager {
    public static final PacketUpdateTemplateManager INSTANCE = new PacketUpdateTemplateManager();

    public static PacketUpdateTemplateManager get() {
        return INSTANCE;
    }

    public void handle(final UpdateTemplateManagerPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
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
                    UUID sourceUUID = GadgetNBT.getUUID(gadgetStack);
                    BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(player.level().getServer()).overworld());
                    ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(sourceUUID, false);
                    if (buildList == null || buildList.isEmpty()) {
                        playSound((ServerPlayer) player, Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
                        return;
                    }
                    container.setItem(1, container.getStateId(), new ItemStack(Registration.TEMPLATE.get()));
                    templateStack = container.getSlot(1).getItem();
                }

                if (templateStack.is(Registration.REDPRINT.get())) {
                    if (payload.templateName().isEmpty()) {
                        player.displayClientMessage(Component.translatable("buildinggadgets2.messages.namerequired"), true);
                        playSound((ServerPlayer) player, Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
                        return;
                    }
                }

                GadgetNBT.setTemplateName(templateStack, payload.templateName());

                if (gadgetStack.isEmpty())
                    return;

                copyData((ServerPlayer) player, gadgetStack, templateStack, payload.templateName());
            } else if (payload.mode() == 1) { //Load
                if (templateStack.isEmpty() || gadgetStack.isEmpty()) {
                    playSound((ServerPlayer) player, Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
                    return;
                }
                if (templateStack.is(Registration.REDPRINT.get()) && !gadgetStack.is(Registration.CUT_PASTE_GADGET.get())) {
                    playSound((ServerPlayer) player, Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
                    return; //Redprints can only go onto Cut and Paste gadgets
                }
                if (gadgetStack.is(Registration.CUT_PASTE_GADGET.get()) && !templateStack.is(Registration.REDPRINT.get())) {
                    playSound((ServerPlayer) player, Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
                    return; //Cut and Paste gadgets can only be loaded from Redprints
                }


                copyData((ServerPlayer) player, templateStack, gadgetStack, payload.templateName());
                GadgetNBT.setTemplateName(gadgetStack, GadgetNBT.getTemplateName(templateStack)); //Set gadget template name to templatestack name
            }
        });
    }

    public static void copyData(ServerPlayer sender, ItemStack sourceStack, ItemStack targetStack, String templateName) {
        UUID targetUUID = GadgetNBT.getUUID(targetStack);
        UUID sourceUUID = GadgetNBT.getUUID(sourceStack);
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(sender.level().getServer()).overworld());
        if (targetStack.is(Registration.REDPRINT.get())) {
            if (!bg2Data.addToRedprints(targetUUID, templateName)) {
                sender.displayClientMessage(Component.translatable("buildinggadgets2.messages.namealreadyexists"), true);
                playSound(sender, Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
                return;
            }
        }
        ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(sourceUUID, false);
        if (buildList == null || buildList.isEmpty()) {
            playSound(sender, Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
            return;
        }
        GadgetNBT.setCopyUUID(targetStack); //This UUID will be used to determine if the copy/paste we are rendering from the cache is old or not.
        bg2Data.addToCopyPaste(targetUUID, buildList);

        if (sourceStack.is(Registration.REDPRINT.get()) || targetStack.is(Registration.REDPRINT.get())) { //If we are reading or writing to a redprint, also copy the TEMap Data
            ArrayList<TagPos> teMap = bg2Data.peekTEMap(sourceUUID);
            ArrayList<TagPos> copiedMap = new ArrayList<>(Objects.requireNonNullElseGet(teMap, ArrayList::new)); //Put a blank TEMap there if we don't have one
            bg2Data.addToTEMap(targetUUID, copiedMap);
        }

        if (sourceStack.is(Registration.REDPRINT.get())) {
            sourceStack.shrink(1);
        }

        //Ensure client has the updated values for both objects
        CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(sourceUUID, false);
        sender.connection.send(new SendCopyDataPayload(sourceUUID, GadgetNBT.getCopyUUID(sourceStack), tag));

        tag = bg2Data.getCopyPasteListAsNBTMap(targetUUID, false);
        sender.connection.send(new SendCopyDataPayload(targetUUID, GadgetNBT.getCopyUUID(targetStack), tag));

        playSound(sender, Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(SoundEvents.ENCHANTMENT_TABLE_USE.getLocation().toString()))));
    }
}
