package net.tslat.smartbrainlib.example;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.core.SmartBrainProvider;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;

import java.util.List;

public class BrainBasedZombie extends Zombie implements SmartBrainOwner<BrainBasedZombie> {
	public BrainBasedZombie(EntityType<? extends BrainBasedZombie> entityType, Level level) {
		super(entityType, level);
	}

	// Let's make sure we're definitely not registering any goals
	@Override
	protected void registerGoals() {}

	// Return the SmartBrainProvider, so we get a SmartBrain
	@Override
	protected Brain.Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	public List<ExtendedSensor<BrainBasedZombie>> getSensors() {
		return List.of();
	}

	// Tick the brain so it can update its tasks and sensors
	@Override
	protected void customServerAiStep() {
		tickBrain(this);
	}
}
