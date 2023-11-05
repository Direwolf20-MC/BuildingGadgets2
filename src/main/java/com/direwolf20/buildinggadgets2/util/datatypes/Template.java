package com.direwolf20.buildinggadgets2.util.datatypes;

import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.ItemStackKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
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
            if (entry.getKey().getStack().isEmpty()) continue;
            requiredItems.put(entry.getKey().item.getCreatorModId(entry.getKey().getStack()) + ":" + entry.getKey().item.toString(), entry.getValue());
        }
    }

    public void replaceBlocks(BlockState sourceState, BlockState targetState) {
        if (statePosArrayList == null || statePosArrayList.equals("")) return;
        ArrayList<StatePos> statePosList = new ArrayList<>();

        try {
            CompoundTag deserializedNBT = TagParser.parseTag(this.statePosArrayList);
            statePosList = BG2Data.statePosListFromNBTMapArray(deserializedNBT);
        } catch (Exception e) {
            return;
        }

        if (statePosList.isEmpty()) return;

        for (StatePos statePos : statePosList) {
            if (statePos.state.equals(sourceState))
                statePos.state = targetState;
        }
        this.statePosArrayList = BG2Data.statePosListToNBTMapArray(statePosList).toString();
    }

    public Template(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String compactJSON = gson.toJson(JsonParser.parseString(json));
            Template temp = gson.fromJson(compactJSON, Template.class);
            if (temp.statePosArrayList == null) { //if this data wasn't in the JSON, it might be from the old template format, lets try!
                TemplateJsonRepresentation temp2 = gson.fromJson(compactJSON, TemplateJsonRepresentation.class);
                byte[] bytes = Base64.getDecoder().decode(temp2.body);
                CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
                BlockPos startPos = new BlockPos(temp2.header.get("bounding_box").getAsJsonObject().get("min_x").getAsInt(), temp2.header.get("bounding_box").getAsJsonObject().get("min_y").getAsInt(), temp2.header.get("bounding_box").getAsJsonObject().get("min_z").getAsInt());
                BlockPos endPos = new BlockPos(temp2.header.get("bounding_box").getAsJsonObject().get("max_x").getAsInt(), temp2.header.get("bounding_box").getAsJsonObject().get("max_y").getAsInt(), temp2.header.get("bounding_box").getAsJsonObject().get("max_z").getAsInt());
                this.name = ""; //Todo
                this.statePosArrayList = BG2Data.statePosListToNBTMapArray(TemplateJsonRepresentation.deserialize(nbt, startPos, endPos)).toString();
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
}
