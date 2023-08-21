package com.direwolf20.buildinggadgets2.common.worlddata;

import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.network.packets.PacketRequestCopyData;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BG2DataClient {
    private static final HashMap<UUID, ArrayList<StatePos>> copyPasteLookup = new HashMap<>();
    private static final HashMap<UUID, UUID> copyPasteCache = new HashMap<>();
    public static boolean awaitingUpdate = false;
    public static int updateTimer = 0;

    public static void updateLookupFromNBT(UUID gadgetUUID, UUID copyUUID, ArrayList<StatePos> list) {
        if (copyPasteLookup.containsKey(gadgetUUID))
            copyPasteLookup.remove(gadgetUUID);
        copyPasteLookup.put(gadgetUUID, list); //Store GadgetUUID -> list of blocks

        if (copyPasteCache.containsKey(gadgetUUID))
            copyPasteCache.remove(gadgetUUID);
        copyPasteCache.put(gadgetUUID, copyUUID); //Store GadgetUUID -> copyUUID (Where copyUUID is a unique id set each time we copy a new set of blocks)

        //Reset the update timers
        updateTimer = 0;
        awaitingUpdate = false;
    }

    public static boolean isClientUpToDate(ItemStack gadget) {
        UUID gadgetUUID = GadgetNBT.getUUID(gadget);
        if (!GadgetNBT.hasCopyUUID(gadget))
            return false; //If the gadget hasn't copied anything yet, lets just bail out now!
        UUID copyUUID = GadgetNBT.getCopyUUID(gadget); //Get the unique identifier of the copy from the gadget
        UUID copyPasteUUIDCache = copyPasteCache.get(gadgetUUID); //Get the CopyUUID of this gadget thats cached here
        if (copyPasteUUIDCache != null && copyPasteUUIDCache.equals(copyUUID)) //If the Cache'd UUID of the copy/paste matches whats on the item, we don't need to rebuild the render
            return true; //No need to rebuild cache because its up to date!
        //This classes data is not up to date - request it from server
        if (awaitingUpdate && updateTimer < 100) { //If we already requested an update from the server, don't try again for a few seconds
            updateTimer++;
            return false; //Since we're still awaiting an update, we're not good yet!
        }
        //Actually request the update from the server
        PacketHandler.sendToServer(new PacketRequestCopyData(gadgetUUID, copyUUID));
        awaitingUpdate = true;
        updateTimer = 0;
        return false;
    }


    public static ArrayList<StatePos> getLookupFromUUID(UUID gadgetUUID) {
        return copyPasteLookup.get(gadgetUUID);
    }

    public static UUID getCopyUUID(UUID gadgetUUID) {
        return copyPasteCache.get(gadgetUUID);
    }
}
