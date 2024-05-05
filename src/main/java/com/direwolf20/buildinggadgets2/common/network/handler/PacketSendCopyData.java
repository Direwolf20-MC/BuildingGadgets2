package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.network.data.SendCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.UUID;

public class PacketSendCopyData {
    public static final PacketSendCopyData INSTANCE = new PacketSendCopyData();

    public static PacketSendCopyData get() {
        return INSTANCE;
    }

    public void handle(final SendCopyDataPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            UUID gadgetUUID = payload.gadgetUUID();
            UUID copyUUID = payload.copyUUID();
            ArrayList<StatePos> statePosList = BG2Data.statePosListFromNBTMapArray(payload.tag());
            BG2DataClient.updateLookupFromNBT(gadgetUUID, copyUUID, statePosList);
        });
    }
}
