package com.axalotl.async.common.mixin.entity.movement;

import com.axalotl.async.common.utils.LithiumMovementTrackingLock;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Mixin(EntitySection.class)
public class EntitySectionMixin<T extends EntityAccess> {

    @Shadow
    @Final
    private ClassInstanceMultiMap<T> storage;

    @Shadow
    private volatile Visibility chunkStatus;

    @WrapMethod(method = "add")
    private void add(EntityAccess entity, Operation<Void> original) {
        synchronized (this) {
            original.call(entity);
        }
    }

    @WrapMethod(method = "remove")
    private boolean remove(EntityAccess entity, Operation<Boolean> original) {
        synchronized (this) {
            return original.call(entity);
        }
    }

    @WrapMethod(method = "updateChunkStatus")
    private Visibility updateChunkStatus(Visibility chunkStatus, Operation<Visibility> original) {
        synchronized (LithiumMovementTrackingLock.LOCK) {
            synchronized (this) {
                return original.call(chunkStatus);
            }
        }
    }

    @WrapMethod(method = "getEntities()Ljava/util/stream/Stream;")
    private Stream<T> getEntities(Operation<Stream<T>> original) {
        return snapshot().stream();
    }

    @WrapMethod(method = "getEntities(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;")
    private AbortableIterationConsumer.Continuation getEntities(AABB bb, AbortableIterationConsumer<T> entities, Operation<AbortableIterationConsumer.Continuation> original) {
        // The consumer runs arbitrary game code which may lock entities, so it must never run while
        // this section's monitor is held: a concurrent Entity#setRemoved holds the entity monitor and
        // then takes the section monitor to unlink itself, which would deadlock.
        for (T entity : snapshot()) {
            if (entity.getBoundingBox().intersects(bb) && entities.accept(entity).shouldAbort()) {
                return AbortableIterationConsumer.Continuation.ABORT;
            }
        }
        return AbortableIterationConsumer.Continuation.CONTINUE;
    }

    @WrapMethod(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;")
    private <U extends T> AbortableIterationConsumer.Continuation getEntities(EntityTypeTest<T, U> type, AABB bb, AbortableIterationConsumer<? super U> consumer, Operation<AbortableIterationConsumer.Continuation> original) {
        List<T> candidates = snapshot(type.getBaseClass());
        if (candidates.isEmpty()) {
            return AbortableIterationConsumer.Continuation.CONTINUE;
        }
        for (T entity : candidates) {
            U casted = type.tryCast(entity);
            if (casted != null && entity.getBoundingBox().intersects(bb) && consumer.accept(casted).shouldAbort()) {
                return AbortableIterationConsumer.Continuation.ABORT;
            }
        }
        return AbortableIterationConsumer.Continuation.CONTINUE;
    }

    /**
     * Copies this section's contents while holding its monitor, so that iteration and any callback
     * invoked for an entity happen without holding it.
     */
    @Unique
    private List<T> snapshot() {
        synchronized (this) {
            List<T> copy = new ArrayList<>(storage.size());
            for (T entity : storage) {
                if (entity != null) {
                    copy.add(entity);
                }
            }
            return copy;
        }
    }

    @Unique
    private List<T> snapshot(Class<? extends T> baseClass) {
        synchronized (this) {
            Collection<? extends T> found = storage.find(baseClass);
            if (found.isEmpty()) {
                return List.of();
            }
            List<T> copy = new ArrayList<>(found.size());
            for (T entity : found) {
                if (entity != null) {
                    copy.add(entity);
                }
            }
            return copy;
        }
    }
}
