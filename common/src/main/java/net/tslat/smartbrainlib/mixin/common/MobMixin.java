package net.tslat.smartbrainlib.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.tslat.smartbrainlib.api.internal.SmartBrain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// Mob class injections for root SBL boilerplate functionality
@Mixin(Mob.class)
public class MobMixin {
    /// Inject automatic brain ticking
    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapOperation(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V"))
    public void sbl$InjectMobBrainTick(Mob self, ServerLevel level, Operation<Void> original) {
        original.call(self, level);

        if (self.getBrain() instanceof SmartBrain smartBrain)
            smartBrain.tick(level, self);
    }
}
