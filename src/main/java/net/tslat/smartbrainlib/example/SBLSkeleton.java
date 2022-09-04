package net.tslat.smartbrainlib.example;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.BowAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.AvoidSun;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.EscapeSun;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;

import java.util.List;

/**
 * Example Skeleton using the SBL brain system
 */
public class SBLSkeleton extends Skeleton implements SmartBrainOwner<SBLSkeleton> {
	public SBLSkeleton(EntityType<? extends SBLSkeleton> entityType, Level level) {
		super(entityType, level);
	}


	@Override
	protected final void registerGoals() {}
	// Let's make sure we're definitely not using any goals
	@Override
	public final void reassessWeaponGoal() {}

	@Override
	protected Brain.Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	public List<ExtendedSensor<SBLSkeleton>> getSensors() {
		return ObjectArrayList.of(
				new NearbyPlayersSensor<>(), 							// Keep track of nearby players
				new NearbyLivingEntitySensor<SBLSkeleton>()
						.setPredicate((target, entity) ->
								target instanceof Player ||
								target instanceof IronGolem ||
								target instanceof Wolf ||
								(target instanceof Turtle turtle && turtle.isBaby() && !turtle.isInWater())));
	}																	// Keep track of nearby entities the Skeleton is interested in

	@Override
	public BrainActivityGroup<SBLSkeleton> getCoreTasks() {
		return BrainActivityGroup.coreTasks(
				new AvoidSun<>(),																							// Keep pathfinder avoiding the sun
				new EscapeSun<>().cooldownFor(entity -> 20),													// Escape the sun
				new AvoidEntity<>().avoiding(entity -> entity instanceof Wolf),												// Run away from wolves
				new LookAtTargetSink(40, 300), 														// Look at the look target
				new StrafeTarget<>().stopStrafingWhen(SBLSkeleton::isHoldingBow).startCondition(SBLSkeleton::isHoldingBow),	// Strafe around target
				new MoveToWalkTarget<>());																					// Move to the current walk target
	}

	@Override
	public BrainActivityGroup<SBLSkeleton> getIdleTasks() {
		return BrainActivityGroup.idleTasks(
				new FirstApplicableBehaviour<SBLSkeleton>( 				// Run only one of the below behaviours, trying each one in order. Include explicit generic typing because javac is silly
						new TargetOrRetaliate<>(),						// Set the attack target
						new SetPlayerLookTarget<>(),					// Set the look target to a nearby player if available
						new SetRandomLookTarget<>()), 					// Set the look target to a random nearby location
				new OneRandomBehaviour<>( 								// Run only one of the below behaviours, picked at random
						new SetRandomWalkTarget<>().speedModifier(1), 				// Set the walk target to a nearby random pathable location
						new Idle<>().runFor(entity -> entity.getRandom().nextInt(30, 60)))); // Don't walk anywhere
	}

	@Override
	public BrainActivityGroup<SBLSkeleton> getFightTasks() {
		return BrainActivityGroup.fightTasks(
				new StopAttackingIfTargetInvalid<>(target -> !target.isAlive() || target instanceof Player && ((Player)target).isCreative()), 	 // Invalidate the attack target if it's no longer applicable
				new FirstApplicableBehaviour<>( 																							  	 // Run only one of the below behaviours, trying each one in order
						new BowAttack<SBLSkeleton>(20).startCondition(SBLSkeleton::isHoldingBow),	 												 // Fire a bow, if holding one
						new AnimatableMeleeAttack<>(0).whenStarting(entity -> setAggressive(true)).whenStarting(entity -> setAggressive(false)))// Melee attack
		);
	}

	@Override
	protected void customServerAiStep() {
		tickBrain(this);
	}

	// Easy predicate to save on redundant code
	private static boolean isHoldingBow(LivingEntity livingEntity) {
		return livingEntity.isHolding(stack -> stack.getItem() instanceof BowItem);
	}
}
