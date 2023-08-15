package com.direwolf20.buildinggadgets2.util.datatypes;

import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.ItemStackKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
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
            this.name = temp.name;
            this.statePosArrayList = temp.statePosArrayList;
        } catch (Exception e) {
            this.name = "";
            this.statePosArrayList = "";
        }

    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
