package net.tslat.smartbrainlib.example;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
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
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.AvoidSun;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.EscapeSun;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
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

	// Add our sensors - these handle passively detecting and remembering nearby environmental factors
	@Override
	public List<? extends ExtendedSensor<? extends SBLSkeleton>> getSensors() {
		return ObjectArrayList.of(
				new NearbyPlayersSensor<>(), // Keep track of nearby players
				new NearbyLivingEntitySensor<SBLSkeleton>() // Keep track of nearby entities the Skeleton is interested in
						.setPredicate((target, entity) ->
								target instanceof Player ||
										target instanceof IronGolem ||
										target instanceof Wolf ||
										(target instanceof Turtle turtle && turtle.isBaby() && !turtle.isInWater())));
	}

	// Add our core tasks - this group runs every tick regardless of any other activities the skeleton may be running
	@Override
	public BrainActivityGroup<? extends SBLSkeleton> getCoreTasks() {
		return BrainActivityGroup.coreTasks(
				new AvoidSun<>(), // Keep pathfinder avoiding the sun
				new EscapeSun<>() // Escape the sun
						.cooldownFor(entity -> 20),
				new AvoidEntity<>() // Run away from wolves
						.avoiding(entity -> entity instanceof Wolf),
				new LookAtTarget<>() // Look at the look target
						.runFor(entity -> entity.getRandom().nextIntBetweenInclusive(40, 300)),
				new StrafeTarget<>()	// Strafe around target
						.stopStrafingWhen(entity -> !isHoldingBow(entity))
						.startCondition(SBLSkeleton::isHoldingBow),
				new MoveToWalkTarget<>()); // Move to the current walk target
	}

	// Add our idle tasks - this group runs automatically if no other activites are running (such as fighting)
	@Override
	public BrainActivityGroup<? extends SBLSkeleton> getIdleTasks() {
		return BrainActivityGroup.idleTasks(
				new FirstApplicableBehaviour<SBLSkeleton>( // Run only one of the below behaviours, trying each one in order. Include explicit generic typing because javac is silly
						new TargetOrRetaliate<>(), // Set the attack target
						new SetPlayerLookTarget<>(), // Set the look target to a nearby player if available
						new SetRandomLookTarget<>()), // Set the look target to a random nearby location
				new OneRandomBehaviour<>( // Run only one of the below behaviours, picked at random
						new SetRandomWalkTarget<>() // Set the walk target to a nearby random pathable location
								.speedModifier(1),
						new Idle<>() // Don't do anything for a bit
								.runFor(entity -> entity.getRandom().nextInt(30, 60))));
	}

	// Add our fight tasks - this group only runs when the skeleton has a target to attack, as dictated by TargetOrRetaliate
	@Override
	public BrainActivityGroup<? extends SBLSkeleton> getFightTasks() {
		return BrainActivityGroup.fightTasks(
				new InvalidateAttackTarget<>(), // Invalidate the attack target if it's no longer applicable
				new SetWalkTargetToAttackTarget<>() // Run at the target if not holding a bow
						.startCondition(entity -> !isHoldingBow(entity) && (!entity.level().isDay() || (entity.isOnFire() && entity.level().canSeeSky(entity.blockPosition())))),
				new FirstApplicableBehaviour<>( // Run only one of the below behaviours, trying each one in order
						new BowAttack<>(20) // Fire a bow, if holding one
								.startCondition(SBLSkeleton::isHoldingBow),
						new AnimatableMeleeAttack<>(0) // Melee attack
								.whenStarting(entity -> setAggressive(true))
								.whenStopping(entity -> setAggressive(false)))
		);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		tickBrain(this);
	}

	// Easy predicate to save on redundant code
	private static boolean isHoldingBow(LivingEntity livingEntity) {
		return livingEntity.isHolding(stack -> stack.getItem() instanceof BowItem);
	}
}