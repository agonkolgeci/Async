package com.axalotl.async.common.mixin.lithium;

import com.axalotl.async.common.utils.LithiumBlockChangeTrackerLock;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.lithium.common.block.BlockListeningSection;
import net.caffeinemc.mods.lithium.common.tracking.block.ChunkSectionChangeCallback;
import net.caffeinemc.mods.lithium.common.tracking.block.SectionedBlockChangeTracker;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ChunkSectionChangeCallback.class, remap = false)
public class LithiumChunkSectionChangeCallbackMixin {

    @WrapMethod(method = "onBlockChange")
    private void onBlockChange(BlockListeningSection section, Operation<Void> original) {
        synchronized (LithiumBlockChangeTrackerLock.LOCK) {
            original.call(section);
        }
    }

    @WrapMethod(method = "addTracker")
    private void addTracker(SectionedBlockChangeTracker tracker, Operation<Void> original) {
        synchronized (LithiumBlockChangeTrackerLock.LOCK) {
            original.call(tracker);
        }
    }

    @WrapMethod(method = "removeTracker")
    private void removeTracker(SectionedBlockChangeTracker tracker, Operation<Void> original) {
        synchronized (LithiumBlockChangeTrackerLock.LOCK) {
            original.call(tracker);
        }
    }

    @WrapMethod(method = "onChunkSectionInvalidated")
    private void onChunkSectionInvalidated(SectionPos sectionPos, Operation<Void> original) {
        synchronized (LithiumBlockChangeTrackerLock.LOCK) {
            original.call(sectionPos);
        }
    }
}