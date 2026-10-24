package com.direwolf20.buildinggadgets2.util.datatypes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

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
        // TODO(port): save format changed — "blockpos" is now stored as a packed long instead of via NbtUtils.writeBlockPos. See note in StatePos.java.
        this.tag = compoundTag.getCompoundOrEmpty("tedata");
        this.pos = BlockPos.of(compoundTag.getLongOr("blockpos", 0L));
    }

    public CompoundTag getTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("tedata", tag);
        compoundTag.putLong("blockpos", pos.asLong());
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
