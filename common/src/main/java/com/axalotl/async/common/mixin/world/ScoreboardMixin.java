package com.axalotl.async.common.mixin.world;

import com.axalotl.async.api.utils.ConcurrentCollections;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {

    @Shadow
    private final Map<String, Map<Objective, Score>> playerScores = ConcurrentCollections.newHashMap();

    @WrapMethod(method = "getPlayersTeam")
    private PlayerTeam getPlayersTeam(String scoreHolder, Operation<PlayerTeam> original) {
        synchronized (this) {
            return original.call(scoreHolder);
        }
    }

    @WrapMethod(method = "addPlayerToTeam")
    private boolean addPlayerToTeam(String scoreHolder, PlayerTeam playerTeam, Operation<Boolean> original) {
        synchronized (this) {
            return original.call(scoreHolder, playerTeam);
        }
    }

    @WrapMethod(method = "removePlayerFromTeam(Ljava/lang/String;)Z")
    private boolean removePlayerFromTeam(String scoreHolder, Operation<Boolean> original) {
        synchronized (this) {
            return original.call(scoreHolder);
        }
    }

    @WrapMethod(method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V")
    private void removePlayerFromTeam(String scoreHolder, PlayerTeam playerTeam, Operation<Void> original) {
        synchronized (this) {
            original.call(scoreHolder, playerTeam);
        }
    }

    @WrapMethod(method = "addPlayerTeam")
    private PlayerTeam addPlayerTeam(String name, Operation<PlayerTeam> original) {
        synchronized (this) {
            return original.call(name);
        }
    }

    @WrapMethod(method = "removePlayerTeam")
    private void removePlayerTeam(PlayerTeam playerTeam, Operation<Void> original) {
        synchronized (this) {
            original.call(playerTeam);
        }
    }
}