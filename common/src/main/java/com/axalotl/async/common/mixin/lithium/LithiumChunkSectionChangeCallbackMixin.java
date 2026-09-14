package com.axalotl.async.common.mixin.lithium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.lithium.common.block.BlockListeningSection;
import net.caffeinemc.mods.lithium.common.tracking.block.SectionedBlockChangeTracker;
import net.caffeinemc.mods.lithium.common.tracking.block.ChunkSectionChangeCallback;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ChunkSectionChangeCallback.class, remap = false)
public class LithiumChunkSectionChangeCallbackMixin {

    @WrapMethod(method = "onBlockChange")
    private synchronized void onBlockChange(BlockListeningSection section, Operation<Void> original) {
        original.call(section);
    }

    @WrapMethod(method = "addTracker")
    private synchronized void addTracker(SectionedBlockChangeTracker tracker, Operation<Void> original) {
        original.call(tracker);
    }

    @WrapMethod(method = "removeTracker")
    private synchronized void removeTracker(SectionedBlockChangeTracker tracker, Operation<Void> original) {
        original.call(tracker);
    }

    @WrapMethod(method = "onChunkSectionInvalidated")
    private synchronized void onChunkSectionInvalidated(SectionPos sectionPos, Operation<Void> original) {
        original.call(sectionPos);
    }
}