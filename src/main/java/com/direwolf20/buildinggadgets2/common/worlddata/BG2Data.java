package com.direwolf20.buildinggadgets2.common.worlddata;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.util.VecHelpers;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BG2Data extends SavedData {
    private static final String NAME = "buildinggadgets2";

    private static final Codec<StatePos> STATEPOS_CODEC = CompoundTag.CODEC.xmap(StatePos::new, StatePos::getTag);
    private static final Codec<TagPos> TAGPOS_CODEC = CompoundTag.CODEC.xmap(TagPos::new, TagPos::getTag);

    private static final Codec<ArrayList<StatePos>> FLAT_STATEPOS_LIST_CODEC =
            STATEPOS_CODEC.listOf().xmap(ArrayList::new, l -> l);

    private static final Codec<ArrayList<StatePos>> PACKED_STATEPOS_LIST_CODEC =
            CompoundTag.CODEC.xmap(BG2Data::statePosListFromNBTMapArray, BG2Data::statePosListToNBTMapArray);

    private static final Codec<ArrayList<TagPos>> TAGPOS_LIST_CODEC =
            TAGPOS_CODEC.listOf().xmap(ArrayList::new, l -> l);

    private static final Codec<HashMap<UUID, ArrayList<StatePos>>> UNDO_MAP_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, FLAT_STATEPOS_LIST_CODEC)
                    .xmap(HashMap::new, m -> m);

    private static final Codec<HashMap<UUID, ArrayList<StatePos>>> COPY_MAP_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, PACKED_STATEPOS_LIST_CODEC)
                    .xmap(HashMap::new, m -> m);

    private static final Codec<HashMap<UUID, ArrayList<TagPos>>> TE_MAP_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, TAGPOS_LIST_CODEC)
                    .xmap(HashMap::new, m -> m);

    private static final Codec<BiMap<UUID, String>> REDPRINT_MAP_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING)
                    .xmap(m -> {
                        BiMap<UUID, String> bi = HashBiMap.create();
                        bi.putAll(m);
                        return bi;
                    }, bi -> bi);

    public static final Codec<BG2Data> CODEC = RecordCodecBuilder.create(i -> i.group(
            UNDO_MAP_CODEC.optionalFieldOf("undolist", new HashMap<>()).forGetter(d -> d.undoList),
            COPY_MAP_CODEC.optionalFieldOf("copypaste", new HashMap<>()).forGetter(d -> d.copyPasteLookup),
            TE_MAP_CODEC.optionalFieldOf("temaptag", new HashMap<>()).forGetter(d -> d.teMap),
            REDPRINT_MAP_CODEC.optionalFieldOf("redprinttag", HashBiMap.create()).forGetter(d -> d.redprintLookup)
    ).apply(i, BG2Data::new));

    public static final SavedDataType<BG2Data> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, NAME),
            () -> new BG2Data(new HashMap<>(), new HashMap<>(), new HashMap<>(), HashBiMap.create()),
            CODEC
    );

    private final HashMap<UUID, ArrayList<StatePos>> undoList; //GadgetUUID -> UndoList StatePosData
    private final HashMap<UUID, ArrayList<StatePos>> copyPasteLookup; //GadgetUUID -> StatePosData
    private final HashMap<UUID, ArrayList<TagPos>> teMap; //GadgetUUID -> Tile Entity Data
    private final BiMap<UUID, String> redprintLookup; //A list of RedPrint names to UUIDs

    public BG2Data(HashMap<UUID, ArrayList<StatePos>> undoList, HashMap<UUID, ArrayList<StatePos>> copyPasteLookup, HashMap<UUID, ArrayList<TagPos>> teMap, BiMap<UUID, String> redprintLookup) {
        // Defensive copies — the Codec optionalFieldOf defaults are shared across decode calls,
        // and this SavedData mutates these fields in place throughout its lifetime.
        this.undoList = new HashMap<>(undoList);
        this.copyPasteLookup = new HashMap<>(copyPasteLookup);
        this.teMap = new HashMap<>(teMap);
        this.redprintLookup = HashBiMap.create(redprintLookup);
    }

    public boolean addToRedprints(UUID uuid, String name) {
        if (redprintLookup.containsKey(uuid)) return false;
        if (redprintLookup.containsValue(name)) return false;
        redprintLookup.put(uuid, name);
        return true;
    }

    public boolean removeFromRedprints(String name) {
        if (redprintLookup.containsValue(name)) {
            UUID uuid = redprintLookup.inverse().get(name);
            redprintLookup.remove(uuid);
            copyPasteLookup.remove(uuid);
            teMap.remove(uuid);
            return true;
        }
        return false;
    }

    public UUID getRedprintUUIDfromName(String name) {
        if (redprintLookup.containsValue(name))
            return redprintLookup.inverse().get(name);
        return null;
    }

    public BiMap<UUID, String> getRedprintLookup() {
        return redprintLookup;
    }

    public boolean containsUndoList(UUID uuid) {
        return undoList.containsKey(uuid);
    }

    public void addToUndoList(UUID uuid, ArrayList<StatePos> list, Level level) {
        undoList.put(uuid, list);
        this.setDirty();
    }

    public void removeFromUndoList(UUID uuid) {
        undoList.remove(uuid);
        this.setDirty();
    }

    public void addToCopyPaste(UUID uuid, ArrayList<StatePos> list) {
        copyPasteLookup.put(uuid, list);
        this.setDirty();
    }

    public void addToTEMap(UUID uuid, ArrayList<TagPos> list) {
        teMap.put(uuid, list);
        this.setDirty();
    }

    public ArrayList<StatePos> getCopyPasteList(UUID uuid, boolean remove) {
        ArrayList<StatePos> returnList = copyPasteLookup.get(uuid);
        if (remove) {
            returnList = copyPasteLookup.remove(uuid);
            this.setDirty();
        }
        return returnList;
    }

    public CompoundTag getCopyPasteListAsNBTMap(UUID uuid, boolean remove) {
        return statePosListToNBTMapArray(getCopyPasteList(uuid, remove));
    }

    public ArrayList<StatePos> peekUndoList(UUID uuid) {
        ArrayList<StatePos> posList = undoList.get(uuid);
        this.setDirty();
        return posList;
    }

    public ArrayList<StatePos> popUndoList(UUID uuid) {
        ArrayList<StatePos> posList = undoList.remove(uuid);
        this.setDirty();
        return posList;
    }

    public ArrayList<TagPos> peekTEMap(UUID uuid) {
        ArrayList<TagPos> tagList = teMap.get(uuid);
        return tagList;
    }

    public ArrayList<TagPos> getTEMap(UUID uuid) {
        ArrayList<TagPos> tagList = teMap.remove(uuid);
        this.setDirty();
        return tagList;
    }

    public static CompoundTag statePosListToNBTMapArray(ArrayList<StatePos> list) {
        CompoundTag tag = new CompoundTag();
        if (list == null || list.isEmpty()) return tag;
        ArrayList<BlockState> blockStateMap = StatePos.getBlockStateMap(list);
        ListTag blockStateMapList = StatePos.getBlockStateNBT(blockStateMap);
        int[] blocklist = new int[list.size()];
        final int[] counter = {0};
        BlockPos start = list.get(0).pos;
        BlockPos end = list.get(list.size() - 1).pos;
        AABB aabb = VecHelpers.aabbFromBlockPos(start, end);

        Map<BlockPos, BlockState> blockStateByPos = list.stream()
                .collect(Collectors.toMap(e -> e.pos, e -> e.state));

        BlockPos.betweenClosedStream(aabb).map(BlockPos::immutable).forEach(pos -> {
            BlockState blockState = blockStateByPos.get(pos);
            blocklist[counter[0]++] = blockStateMap.indexOf(blockState);
        });
        tag.put("startpos", writeBlockPos(start));
        tag.put("endpos", writeBlockPos(end));
        tag.put("blockstatemap", blockStateMapList);
        tag.putIntArray("statelist", blocklist); //Todo - Short Array?
        return tag;
    }

    /**
     * Something Changed in NBTUtils.ReadBlockPos that broke templates, so i made the below as a workaround
     */
    public static BlockPos readBlockPos(CompoundTag compoundTag, String pKey) {
        if (!compoundTag.contains(pKey)) return BlockPos.ZERO;
        CompoundTag tag = compoundTag.getCompoundOrEmpty(pKey);
        return new BlockPos(tag.getIntOr("X", 0), tag.getIntOr("Y", 0), tag.getIntOr("Z", 0));
    }

    public static CompoundTag writeBlockPos(BlockPos pPos) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putInt("X", pPos.getX());
        compoundtag.putInt("Y", pPos.getY());
        compoundtag.putInt("Z", pPos.getZ());
        return compoundtag;
    }

    public static ArrayList<StatePos> statePosListFromNBTMapArray(CompoundTag tag) {
        ArrayList<StatePos> statePosList = new ArrayList<>();
        if (!tag.contains("blockstatemap") || !tag.contains("statelist")) return statePosList;
        ArrayList<BlockState> blockStateMap = StatePos.getBlockStateMapFromNBT(tag.getListOrEmpty("blockstatemap"));
        BlockPos start = readBlockPos(tag, "startpos");
        BlockPos end = readBlockPos(tag, "endpos");
        AABB aabb = VecHelpers.aabbFromBlockPos(start, end);
        int[] blocklist = tag.getIntArray("statelist").orElse(new int[0]);
        final int[] counter = {0};
        BlockPos.betweenClosedStream(aabb).map(BlockPos::immutable).forEach(pos -> {
            int blockStateLookup = blocklist[counter[0]++];
            BlockState blockState = blockStateMap.get(blockStateLookup);
            statePosList.add(new StatePos(blockState, pos));
        });
        return statePosList;
    }

    public static BG2Data get(ServerLevel world) {
        BG2Data bg2Data = world.getDataStorage().computeIfAbsent(TYPE);
        bg2Data.setDirty();
        return bg2Data;
    }
}
