package com.axalotl.async.common.mixin.utils;

import com.axalotl.async.common.ParallelProcessor;
import com.axalotl.async.common.config.AsyncConfig;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {

    /*
     * The exit portal lookup reads POIs, may load destination chunks and may create a portal.
     * None of this is thread-safe, and holding a lock around it deadlocked the server: a worker
     * kept the lock while waiting for the main thread to load a chunk, while the main thread
     * waited for the lock. Workers now hand the whole lookup to the main thread, which runs it
     * serially like vanilla, so the entity still travels and no lock is needed.
     */
    @WrapMethod(method = "getExitPortal")
    private TeleportTransition async_getExitPortal(
            ServerLevel level, Entity entity, BlockPos pos, BlockPos exitPos,
            boolean isNether, WorldBorder worldBorder, Operation<TeleportTransition> original
    ) {
        if (AsyncConfig.disabled || !ParallelProcessor.isServerExecutionThread()) {
            return original.call(level, entity, pos, exitPos, isNether, worldBorder);
        }
        return ParallelProcessor.callOnMainThread(() -> original.call(level, entity, pos, exitPos, isNether, worldBorder));
    }
}
