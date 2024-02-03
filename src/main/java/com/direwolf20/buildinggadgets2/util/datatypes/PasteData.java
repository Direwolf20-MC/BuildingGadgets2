package com.direwolf20.buildinggadgets2.util.datatypes;

import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.SendPasteBatchesPayload;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.PacketSendPasteBatches;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class PasteData {
    private final int totalChunks;
    private final Map<Integer, FriendlyByteBuf> receivedChunks = new HashMap<>();

    public PasteData(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    public boolean isComplete() {
        return receivedChunks.size() == totalChunks;
    }

    public void addChunk(int position, FriendlyByteBuf chunk) {
        receivedChunks.put(position, chunk);
    }

    public FriendlyByteBuf assembleData() {
        FriendlyByteBuf fullData = new FriendlyByteBuf(Unpooled.buffer());
        for (int i = 0; i < totalChunks; i++) {
            fullData.writeBytes(receivedChunks.get(i));
        }
        return fullData;
    }

    public static List<FriendlyByteBuf> splitBuffer(FriendlyByteBuf buffer, int chunkSize) {
        List<FriendlyByteBuf> chunks = new ArrayList<>();

        while (buffer.readableBytes() > 0) {
            int size = Math.min(buffer.readableBytes(), chunkSize);
            FriendlyByteBuf chunkBuffer = new FriendlyByteBuf(Unpooled.buffer(size)); // Allocate buffer with the required size
            buffer.readBytes(chunkBuffer, size);
            chunks.add(chunkBuffer);
        }

        return chunks;
    }

    public static void sendCompoundTag(CompoundTag tag) {
        FriendlyByteBuf fullBuffer = new FriendlyByteBuf(Unpooled.buffer());
        fullBuffer.writeNbt(tag);
        List<FriendlyByteBuf> chunks = splitBuffer(fullBuffer, 30000); // Assuming 30,000 bytes as chunk size

        UUID copyUUID = UUID.randomUUID();
        for (int i = 0; i < chunks.size(); i++) {
            PacketDistributor.SERVER.noArg().send(new SendPasteBatchesPayload(copyUUID, chunks.size(), i, chunks.get(i)));
        }
    }
}
