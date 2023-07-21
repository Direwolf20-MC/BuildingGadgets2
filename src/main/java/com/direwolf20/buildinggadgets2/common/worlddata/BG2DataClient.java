package com.direwolf20.buildinggadgets2.common.worlddata;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BG2DataClient {
    private static final HashMap<UUID, ArrayList<StatePos>> copyPasteLookup = new HashMap<>();
    private static final HashMap<UUID, UUID> copyPasteCache = new HashMap<>();

    public static void updateLookupFromNBT(UUID gadgetUUID, UUID copyUUID, ArrayList<StatePos> list) {
        if (copyPasteLookup.containsKey(gadgetUUID))
            copyPasteLookup.remove(gadgetUUID);
        copyPasteLookup.put(gadgetUUID, list);

        if (copyPasteCache.containsKey(gadgetUUID))
            copyPasteCache.remove(gadgetUUID);
        copyPasteCache.put(gadgetUUID, copyUUID);
    }

    public static ArrayList<StatePos> getLookupFromUUID(UUID gadgetUUID) {
        return copyPasteLookup.get(gadgetUUID);
    }

    public static UUID getCopyUUID(UUID gadgetUUID) {
        return copyPasteCache.get(gadgetUUID);
    }
}
