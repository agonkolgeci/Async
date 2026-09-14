package com.axalotl.async.common.utils;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

// Kept outside the mixin package: Mixin forbids loading classes that live in a mixin package.
public final class LastChunkSlot {
    public Object owner;
    public long pos;
    public ChunkStatus status;
    public ChunkAccess chunk;
    public int tick = Integer.MIN_VALUE;
}
