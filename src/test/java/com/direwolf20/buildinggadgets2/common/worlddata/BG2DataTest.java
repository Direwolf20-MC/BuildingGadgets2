package com.direwolf20.buildinggadgets2.common.worlddata;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.google.common.collect.HashBiMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BG2DataTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static StatePos cell(int x, BlockState state) {
        return new StatePos(state, new BlockPos(x, 0, 0));
    }

    private static Map<BlockPos, BlockState> cells(List<StatePos> list) {
        Map<BlockPos, BlockState> cells = new HashMap<>();
        list.forEach(entry -> cells.put(entry.pos, entry.state));
        return cells;
    }

    private static ArrayList<StatePos> roundTrip(ArrayList<StatePos> list) {
        return BG2Data.statePosListFromNBTMapArray(BG2Data.statePosListToNBTMapArray(list));
    }

    @Test
    void sparseCopyIncludesAirRatherThanOverrunningOrEncodingMinusOne() {
        ArrayList<StatePos> list = new ArrayList<>(List.of(
                cell(0, Blocks.STONE.defaultBlockState()), cell(2, Blocks.DIAMOND_BLOCK.defaultBlockState())));
        CompoundTag tag = BG2Data.statePosListToNBTMapArray(list);
        assertEquals(3, tag.getIntArray("statelist").orElseThrow().length);
        assertTrue(java.util.Arrays.stream(tag.getIntArray("statelist").orElseThrow()).allMatch(index -> index >= 0));
        var restored = BG2Data.statePosListFromNBTMapArray(tag);
        assertEquals(Blocks.STONE.defaultBlockState(), restored.getFirst().state);
        assertEquals(Blocks.AIR.defaultBlockState(), restored.get(1).state);
        assertEquals(Blocks.DIAMOND_BLOCK.defaultBlockState(), restored.getLast().state);
        assertEquals(2, list.size());
    }

    @Test
    void incidentSizedSparseCopySurvivesTheOld14133Boundary() {
        ArrayList<StatePos> list = new ArrayList<>();
        for (int x = 0; x < 14134; x++) {
            if (x != 777) list.add(cell(x, Blocks.STONE.defaultBlockState()));
        }
        assertEquals(14133, list.size());
        var restored = roundTrip(list);
        assertEquals(14134, restored.size());
        assertTrue(restored.get(777).state.isAir());
        assertTrue(restored.get(778).state.is(Blocks.STONE));
    }

    @Test
    void denseFormatAndPaletteOrderStayUnchanged() {
        ArrayList<StatePos> list = new ArrayList<>(List.of(
                cell(0, Blocks.STONE.defaultBlockState()), cell(1, Blocks.GOLD_BLOCK.defaultBlockState()),
                cell(2, Blocks.STONE.defaultBlockState())));
        CompoundTag expected = new CompoundTag();
        expected.put("startpos", BG2Data.writeBlockPos(BlockPos.ZERO));
        expected.put("endpos", BG2Data.writeBlockPos(new BlockPos(2, 0, 0)));
        expected.put("blockstatemap", StatePos.getBlockStateNBT(StatePos.getBlockStateMap(list)));
        expected.putIntArray("statelist", new int[]{0, 1, 0});
        assertEquals(expected, BG2Data.statePosListToNBTMapArray(list));
    }

    @Test
    void duplicateCoordinatesUseNewestRecord() {
        var list = new ArrayList<>(List.of(cell(0, Blocks.STONE.defaultBlockState()),
                cell(2, Blocks.STONE.defaultBlockState()), cell(0, Blocks.GOLD_BLOCK.defaultBlockState())));
        assertEquals(Blocks.GOLD_BLOCK.defaultBlockState(), cells(roundTrip(list)).get(BlockPos.ZERO));
    }

    @Test
    void reversedEndpointsAndNegativeCoordinatesPreserveProperties() {
        BlockState state = Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, true)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
        var list = new ArrayList<>(List.of(cell(-3, state), cell(-2, Blocks.STONE.defaultBlockState())));
        Collections.reverse(list);
        assertEquals(cells(list), cells(roundTrip(list)));
    }

    @Test
    void unorderedOutliersExpandBoundsInsteadOfDisappearing() {
        var list = new ArrayList<>(List.of(cell(0, Blocks.STONE.defaultBlockState()),
                cell(-2, Blocks.EMERALD_BLOCK.defaultBlockState()), cell(1, Blocks.STONE.defaultBlockState())));
        assertEquals(Blocks.EMERALD_BLOCK.defaultBlockState(), cells(roundTrip(list)).get(new BlockPos(-2, 0, 0)));
    }

    @Test
    void emptyAndNullKeepExistingContract() {
        assertTrue(BG2Data.statePosListToNBTMapArray(null).isEmpty());
        assertTrue(BG2Data.statePosListToNBTMapArray(new ArrayList<>()).isEmpty());
    }

    @Test
    void volumeChecksRejectOverflowAndExcessBeforeAllocation() {
        assertEquals(16777216, BG2Data.checkedPackedVolume(BlockPos.ZERO, new BlockPos(255, 255, 255)));
        assertThrows(IllegalArgumentException.class,
                () -> BG2Data.checkedPackedVolume(BlockPos.ZERO, new BlockPos(256, 255, 255)));
        assertThrows(IllegalArgumentException.class,
                () -> BG2Data.checkedPackedVolume(new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
                        new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)));
        assertThrows(IllegalArgumentException.class,
                () -> BG2Data.checkedPackedVolume(new BlockPos(2, 0, 0), BlockPos.ZERO));
    }

    @Test
    void savedDataCodecReopensSparseCopyAndPreservesUndoAndNames() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var list = new ArrayList<>(List.of(cell(0, Blocks.STONE.defaultBlockState()), cell(2, Blocks.GOLD_BLOCK.defaultBlockState())));
        var copy = new HashMap<UUID, ArrayList<StatePos>>();
        var undo = new HashMap<UUID, ArrayList<StatePos>>();
        copy.put(id, list);
        undo.put(id, new ArrayList<>(list));
        var names = HashBiMap.<UUID, String>create();
        names.put(id, "Regression template");
        var data = new BG2Data(undo, copy, new HashMap<>(), names);
        var encoded = BG2Data.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        var restored = BG2Data.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        assertEquals(3, restored.getCopyPasteList(id, false).size());
        assertEquals(cells(list), cells(restored.peekUndoList(id)));
        assertEquals(names, restored.getRedprintLookup());
    }

    @Test
    void seededSparseSelectionsConserveAllRecordedStates() {
        Random random = new Random(617315);
        for (int trial = 0; trial < 30; trial++) {
            ArrayList<StatePos> list = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(BlockPos.ZERO, new BlockPos(6, 3, 4))) {
                if (pos.equals(BlockPos.ZERO) || pos.equals(new BlockPos(6, 3, 4)) || random.nextBoolean()) {
                    list.add(new StatePos((random.nextBoolean() ? Blocks.STONE : Blocks.AIR).defaultBlockState(), pos.immutable()));
                }
            }
            var before = cells(list);
            var after = cells(roundTrip(list));
            assertTrue(after.entrySet().containsAll(before.entrySet()));
            assertTrue(after.entrySet().stream().allMatch(e -> before.containsKey(e.getKey()) || e.getValue().isAir()));
        }
    }
}
