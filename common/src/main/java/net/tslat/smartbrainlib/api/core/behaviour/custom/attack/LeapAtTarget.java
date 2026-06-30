package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.function.*;

/// SmartBrainLib equivalent of [net.minecraft.world.entity.ai.goal.LeapAtTargetGoal]<br/>
/// Launches the entity at the current attack target and attacks after a pre-determined delay
///
/// @see AnimatableMeleeAttack
/// @see #canAttack(BiPredicate)
/// @param <BO> The brain owner entity
public class LeapAtTarget<BO extends Mob> extends AnimatableMeleeAttack<BO> {
    protected ToFloatBiFunction<BO, LivingEntity> verticalJumpStrength = (_, _) -> 0.3f;
    protected ToFloatBiFunction<BO, LivingEntity> jumpStrength = (_, _) -> 0.4f;
    protected ToFloatBiFunction<BO, LivingEntity> moveSpeedContribution = (_, _) -> 0.2f;
    protected ToFloatBiFunction<BO, LivingEntity> leapRange = (_, _) -> 8f;

    public LeapAtTarget(int delayTicks) {
        super(delayTicks);
        
        canAttack((entity, target) -> target.isAlive() && BrainUtil.canSee(entity, target) && entity.closerThan(target, this.leapRange.applyAsFloat(entity, target)));
    }
    
    public LeapAtTarget(ToIntFunction<BO> delayTicks) {
        super(delayTicks);
        
        canAttack((entity, target) -> target.isAlive() && BrainUtil.canSee(entity, target) && entity.closerThan(target, this.leapRange.applyAsFloat(entity, target)));
    }

    /// Set how far away (in blocks) the entity can be to leap
    public LeapAtTarget<BO> leapRange(float range) {
        return leapRange((_, _) -> range);
    }
    
    /// Set a function to determine how far away (in blocks) the entity can be to leap
    public LeapAtTarget<BO> leapRange(ToFloatBiFunction<BO, LivingEntity> range) {
        this.leapRange = range;

        return this;
    }

    /// Set the jump strength factor for the leap<br/>
    /// The actual jump strength will vary depending on the distance to jump
    public LeapAtTarget<BO> jumpStrength(float strength) {
        return jumpStrength((_, _) -> strength);
    }
    
    /// Set a function to determine the jump strength factor for the leap<br/>
    /// The actual jump strength will vary depending on the distance to jump
    public LeapAtTarget<BO> jumpStrength(ToFloatBiFunction<BO, LivingEntity> function) {
        this.jumpStrength = function;

        return this;
    }

    /// Set the additional vertical velocity added when leaping<br/>
    /// This value is not normally scaled by the leap distance
    public LeapAtTarget<BO> verticalJumpStrength(float strength) {
        return verticalJumpStrength((_, _) -> strength);
    }
    
    /// Set a function to determine the additional vertical velocity added when leaping<br/>
    /// This value is not normally scaled by the leap distance
    public LeapAtTarget<BO> verticalJumpStrength(ToFloatBiFunction<BO, LivingEntity> function) {
        this.verticalJumpStrength = function;

        return this;
    }
    
    /// Set the amount that the entity's existing velocity contributes to the jump's velocity<br/>
    /// The returned value here acts as a percentage of the entity's existing velocity
    ///
    /// `0.2f = 20% of the existing velocity added to the jump`
    public LeapAtTarget<BO> moveSpeedContribution(float ratio) {
        return moveSpeedContribution((_, _) -> ratio);
    }
    
    /// Set a function to determine the amount that the entity's existing velocity contributes to the jump's velocity<br/>
    /// The returned value here acts as a percentage of the entity's existing velocity.
    ///
    /// `0.2f = 20% of the existing velocity added to the jump`
    public LeapAtTarget<BO> moveSpeedContribution(ToFloatBiFunction<BO, LivingEntity> function) {
        this.moveSpeedContribution = function;

        return this;
    }
    
    //<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
    /// Set the number of ticks between attacks
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> attackInterval(int ticks) {
        return (LeapAtTarget<BO>)super.attackInterval(ticks);
    }
    
    /// Set the function that determines the time (in ticks) between attacks
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> attackInterval(ToIntBiFunction<BO, @Nullable LivingEntity> supplier) {
        return (LeapAtTarget<BO>)super.attackInterval(supplier);
    }
    
    /// Set a custom predicate for whether a target is valid to attack or not
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> canAttack(BiPredicate<BO, LivingEntity> predicate) {
        return (LeapAtTarget<BO>)super.canAttack(predicate);
    }
    
    /// Set an additional callback to run when the delayed activation is called
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> whenActivating(Consumer<BO> callback) {
        return (LeapAtTarget<BO>)super.whenActivating(callback);
    }
    
    /// Set a callback for when the behaviour successfully begins
    ///
    /// This is called immediately prior to [#start(LivingEntity)]
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> whenStarting(Consumer<BO> callback) {
        return (LeapAtTarget<BO>)super.whenStarting(callback);
    }
    
    /// Set a callback for when the behaviour stops
    ///
    /// This is called immediately prior to [#stop(LivingEntity)]
    ///
    /// Note that the behaviour stopping does not necessarily mean it was successful
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> whenStopping(Consumer<BO> callback) {
        return (LeapAtTarget<BO>)super.whenStopping(callback);
    }
    
    /// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> runFor(int ticks) {
        return (LeapAtTarget<BO>)super.runFor(ticks);
    }
    
    /// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> runFor(int minTicks, int maxTicks) {
        return (LeapAtTarget<BO>)super.runFor(minTicks, maxTicks);
    }
    
    /// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
        return (LeapAtTarget<BO>)super.runFor(timeProvider);
    }
    
    /// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> noTimeout() {
        return (LeapAtTarget<BO>)super.noTimeout();
    }
    
    /// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> cooldownFor(int ticks) {
        return (LeapAtTarget<BO>)super.cooldownFor(ticks);
    }
    
    /// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> cooldownFor(int minTicks, int maxTicks) {
        return (LeapAtTarget<BO>)super.cooldownFor(minTicks, maxTicks);
    }
    
    /// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
        return (LeapAtTarget<BO>)super.cooldownFor(timeProvider);
    }
    
    /// Set an additional condition for the behaviour to be able to start
    ///
    /// Prevents this behaviour starting unless this predicate returns true.
    ///
    /// @param predicate The condition for starting
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> startCondition(Predicate<BO> predicate) {
        return (LeapAtTarget<BO>)super.startCondition(predicate);
    }
    
    /// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
    @ApiStatus.NonExtendable
    @Override
    public LeapAtTarget<BO> stopIf(Predicate<BO> predicate) {
        return (LeapAtTarget<BO>)super.stopIf(predicate);
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
    /// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
        this.target = BrainUtil.getTargetOfEntity(entity);

        return this.target != null && entity.onGround() && this.validTarget.test(entity, this.target);
    }
    
    /// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	/// 
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method  
    @MustBeInvokedByOverriders
    @Override
    protected void start(BO entity) {
        super.start(entity);
        
        //noinspection DataFlowIssue
        Vec3 velocity = new Vec3(this.target.getX() - entity.getX(), 0, this.target.getZ() - entity.getZ());

        if (velocity.lengthSqr() > Mth.EPSILON)
            velocity = velocity.normalize().scale(this.jumpStrength.applyAsFloat(entity, this.target)).add(entity.getDeltaMovement().scale(this.moveSpeedContribution.applyAsFloat(entity, this.target)));

        entity.setDeltaMovement(velocity.x, this.verticalJumpStrength.applyAsFloat(entity, this.target), velocity.z);
    }
    //</editor-fold>
}
