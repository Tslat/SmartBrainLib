package net.tslat.smartbrainlib.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.internal.SmartBrain;
import net.tslat.smartbrainlib.api.internal.SmartBrainProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// LivingEntity class injections for root `SmartBrainLib` boilerplate functionality
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /// Inject automatic brain creation for [SmartBrainOwner]s
    @SuppressWarnings("unchecked")
    @Inject(method = "makeBrain", at = @At("HEAD"), cancellable = true)
    public <BO extends LivingEntity & SmartBrainOwner<BO>> void sbl$makeSmartBrain(Brain.Packed packedBrain, CallbackInfoReturnable<Brain<? extends LivingEntity>> cir) {
        if ((LivingEntity)(Object)this instanceof SmartBrainOwner<?> smartBrainOwner)
            cir.setReturnValue(new SmartBrainProvider<>(((BO)smartBrainOwner).getBrainBuilder()).makeBrain((BO)smartBrainOwner, Brain.Packed.EMPTY));
    }

    /// Inject automatic brain ticking
    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;serverAiStep()V"))
    public void sbl$InjectBrainTick(LivingEntity self, Operation<Void> original) {
        original.call(self);

        if (self.getBrain() instanceof SmartBrain smartBrain && !(self instanceof Mob) && self.level() instanceof ServerLevel level)
            smartBrain.tick(level, self);
    }
}
