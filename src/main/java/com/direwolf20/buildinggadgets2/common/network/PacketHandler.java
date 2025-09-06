package com.direwolf20.buildinggadgets2.common.network;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.common.network.data.*;
import com.direwolf20.buildinggadgets2.common.network.handler.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {
    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(BuildingGadgets2Api.MOD_ID);

        //Going to Server
        registrar.playToServer(AnchorPayload.TYPE, AnchorPayload.STREAM_CODEC, PacketAnchor.get()::handle);
        registrar.playToServer(CopyCoordsPayload.TYPE, CopyCoordsPayload.STREAM_CODEC, PacketCopyCoords.get()::handle);
        registrar.playToServer(CutPayload.TYPE, CutPayload.STREAM_CODEC, PacketCut.get()::handle);
        registrar.playToServer(DestructionRangesPayload.TYPE, DestructionRangesPayload.STREAM_CODEC, PacketDestructionRanges.get()::handle);
        registrar.playToServer(ModeSwitchPayload.TYPE, ModeSwitchPayload.STREAM_CODEC, PacketModeSwitch.get()::handle);
        registrar.playToServer(RangeChangePayload.TYPE, RangeChangePayload.STREAM_CODEC, PacketRangeChange.get()::handle);
        registrar.playToServer(RelativePastePayload.TYPE, RelativePastePayload.STREAM_CODEC, PacketRelativePaste.get()::handle);
        registrar.playToServer(RequestCopyDataPayload.TYPE, RequestCopyDataPayload.STREAM_CODEC, PacketRequestCopyData.get()::handle);
        registrar.playToServer(RenderChangePayload.TYPE, RenderChangePayload.STREAM_CODEC, PacketRenderChange.get()::handle);
        registrar.playToServer(RotatePayload.TYPE, RotatePayload.STREAM_CODEC, PacketRotate.get()::handle);
        registrar.playToServer(SendCopyDataToServerPayload.TYPE, SendCopyDataToServerPayload.STREAM_CODEC, PacketSendCopyDataToServer.get()::handle);
        registrar.playToServer(SendPastePayload.TYPE, SendPastePayload.STREAM_CODEC, PacketSendPaste.get()::handle);
        registrar.playToServer(ToggleSettingPayload.TYPE, ToggleSettingPayload.STREAM_CODEC, PacketToggleSetting.get()::handle);
        registrar.playToServer(UndoPayload.TYPE, UndoPayload.STREAM_CODEC, PacketUndo.get()::handle);
        registrar.playToServer(UpdateTemplateManagerPayload.TYPE, UpdateTemplateManagerPayload.STREAM_CODEC, PacketUpdateTemplateManager.get()::handle);


        //Going to Client
        registrar.playToClient(SendCopyDataPayload.TYPE, SendCopyDataPayload.STREAM_CODEC, PacketSendCopyData.get()::handle);
    }
}
