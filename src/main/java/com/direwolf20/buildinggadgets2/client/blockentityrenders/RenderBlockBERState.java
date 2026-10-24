package com.direwolf20.buildinggadgets2.client.blockentityrenders;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class RenderBlockBERState extends BlockEntityRenderState {
    public byte renderType;
    public BlockState renderBlock;
    public float scale;
    public ClientLevel level;
    public boolean shrinking;
    public int[] tintLayers = new int[]{-1};
    /** Per-face adjacency cull flags for renderFade — true means skip that face. */
    public boolean[] cullFaces = new boolean[Direction.values().length];
}
