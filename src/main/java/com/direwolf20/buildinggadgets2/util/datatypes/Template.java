package com.direwolf20.buildinggadgets2.util.datatypes;

import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.datagen.BG2BlockTags;
import com.direwolf20.buildinggadgets2.util.ItemStackKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class Template {
    public String name;
    public String statePosArrayList;
    public Map<String, Integer> requiredItems = new Object2IntOpenHashMap<>();

    public Template(String name, ArrayList<StatePos> statePosArrayList) {
        this.name = name;
        this.statePosArrayList = BG2Data.statePosListToNBTMapArray(statePosArrayList).toString();
        Map<ItemStackKey, Integer> requiredItemsTemp = StatePos.getItemList(statePosArrayList);
        for (Map.Entry<ItemStackKey, Integer> entry : requiredItemsTemp.entrySet()) {
            requiredItems.put(entry.getKey().item.toString(), entry.getValue());
        }
    }

    public Template(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String compactJSON = gson.toJson(JsonParser.parseString(json));
            Template temp = gson.fromJson(compactJSON, Template.class);
            if (statePosArrayList == null) { //if this data wasn't in the JSON, it might be from the old template format, lets try!
                TemplateJsonRepresentation temp2 = gson.fromJson(compactJSON, TemplateJsonRepresentation.class);
                byte[] bytes = Base64.getDecoder().decode(temp2.body);
                CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
                BlockPos startPos = new BlockPos(temp2.header.get("bounding_box").getAsJsonObject().get("min_x").getAsInt(), temp2.header.get("bounding_box").getAsJsonObject().get("min_y").getAsInt(), temp2.header.get("bounding_box").getAsJsonObject().get("min_z").getAsInt());
                BlockPos endPos = new BlockPos(temp2.header.get("bounding_box").getAsJsonObject().get("max_x").getAsInt(), temp2.header.get("bounding_box").getAsJsonObject().get("max_y").getAsInt(), temp2.header.get("bounding_box").getAsJsonObject().get("max_z").getAsInt());
                ArrayList<StatePos> statePosList = TemplateJsonRepresentation.deserialize(nbt, startPos, endPos);
                this.name = ""; //Todo
                this.statePosArrayList = BG2Data.statePosListToNBTMapArray(statePosList).toString();
            } else {
                this.name = temp.name;
                this.statePosArrayList = temp.statePosArrayList;
            }
        } catch (Exception e) {
            this.name = "";
            this.statePosArrayList = "";
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("buildinggadgets2.screen.invalidjson"), true);
        }

    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    private static final class TemplateJsonRepresentation {
        public static final int B1_BYTE_MASK = 0xFF;
        public static final int B2_BYTE_MASK = 0xFF_FF;
        public static final int B3_BYTE_MASK = 0xFF_FF_FF;

        public final JsonObject header;
        public final String body;

        private TemplateJsonRepresentation(JsonObject header, String body) {
            this.header = header;
            this.body = body;
        }

        public static ArrayList<StatePos> deserialize(CompoundTag nbt, BlockPos startPos, BlockPos endPos) {
            ArrayList<StatePos> statePosList = new ArrayList<>();
            ListTag posList = nbt.getList("pos", Tag.TAG_LONG);
            ListTag stateList = nbt.getList("data", Tag.TAG_COMPOUND);
            HashMap<BlockPos, BlockState> tempMap = new HashMap<>();

            for (Tag inbt : posList) {
                LongTag longNBT = (LongTag) inbt;
                BlockPos pos = posFromLong(longNBT.getAsLong());
                int stateID = readStateId(longNBT.getAsLong());
                BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), stateList.getCompound(stateID).getCompound("state"));
                tempMap.put(pos, blockState);
            }

            AABB area = new AABB(startPos, endPos);
            BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
                BlockState blockState = tempMap.getOrDefault(pos, Blocks.AIR.defaultBlockState());
                if (blockState.isAir()) {
                    statePosList.add(new StatePos(blockState, pos));
                    return;
                }

                if (blockState.is(BG2BlockTags.BG2DENY)) {
                    statePosList.add(new StatePos(Blocks.AIR.defaultBlockState(), pos));
                    return;
                }
                if (blockState.getBlock().defaultDestroyTime() < 0) {
                    statePosList.add(new StatePos(Blocks.AIR.defaultBlockState(), pos));
                    return;
                }
                if (!blockState.getFluidState().isEmpty() && !blockState.getFluidState().isSource()) {
                    statePosList.add(new StatePos(Blocks.AIR.defaultBlockState(), pos));
                    return;
                }

                statePosList.add(new StatePos(blockState, pos));
            });

            return statePosList;
        }

        public static BlockPos posFromLong(long serialized) {
            int x = (int) ((serialized >> 24) & B2_BYTE_MASK);
            int y = (int) ((serialized >> 16) & B1_BYTE_MASK);
            int z = (int) (serialized & B2_BYTE_MASK);
            return new BlockPos(x, y, z);
        }

        public static int readStateId(long serialized) {
            return (int) ((serialized >> 40) & B3_BYTE_MASK);
        }
    }
}
