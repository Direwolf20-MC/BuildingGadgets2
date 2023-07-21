package com.direwolf20.buildinggadgets2.util.datatypes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class TagPos {
    public CompoundTag tag;
    public BlockPos pos;

    public TagPos(CompoundTag tag, BlockPos pos) {
        this.tag = tag;
        this.pos = pos;
    }

    public TagPos(CompoundTag compoundTag) {
        if (!compoundTag.contains("blocktag") || !compoundTag.contains("blockpos")) {
            this.tag = null;
            this.pos = null;
        }
        this.tag = compoundTag.getCompound("tedata");
        this.pos = NbtUtils.readBlockPos(compoundTag.getCompound("blockpos"));
    }

    public CompoundTag getTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("tedata", tag);
        compoundTag.put("blockpos", NbtUtils.writeBlockPos(pos));
        return compoundTag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TagPos) {
            return ((TagPos) obj).tag.equals(this.tag) && ((TagPos) obj).pos.equals(this.pos);
        }
        return false;
    }
}
