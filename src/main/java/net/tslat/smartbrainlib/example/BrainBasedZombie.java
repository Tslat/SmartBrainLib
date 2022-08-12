package net.tslat.smartbrainlib.example;

import com.mojang.serialization.Dynamic;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.core.BrainActivityGroup;
import net.tslat.smartbrainlib.core.SmartBrainHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrainBasedZombie extends Zombie implements SmartBrainOwner<BrainBasedZombie> {
	private SmartBrainHandler<BrainBasedZombie> brainHandler = null;

	public BrainBasedZombie(EntityType<? extends BrainBasedZombie> entityType, Level level) {
		super(entityType, level);
	}

	// Let's make sure we're definitely not registering any goals
	@Override
	protected void registerGoals() {}

	@Override
	public Brain.Provider<BrainBasedZombie> brainProvider() {
		return this.brainHandler.getProvider();
	}

	@Override
	public Brain<BrainBasedZombie> getBrain() {
		return this.brainHandler.getBrain();
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> codecLoader) {
		if (this.brainHandler == null)
			this.brainHandler = new SmartBrainHandler<>(this);

		return this.brainHandler.makeBrain(codecLoader);
	}

	@Override
	public List<SensorType<? extends Sensor<? super BrainBasedZombie>>> getSensors() {
		return List.of();
	}

	@Override
	public BrainActivityGroup<BrainBasedZombie> getCoreTasks() {
		return SmartBrainOwner.super.getCoreTasks();
	}

	@Override
	public BrainActivityGroup<BrainBasedZombie> getIdleTasks() {
		return SmartBrainOwner.super.getIdleTasks();
	}

	@Override
	public BrainActivityGroup<BrainBasedZombie> getFightTasks() {
		return SmartBrainOwner.super.getFightTasks();
	}

	@Override
	public Map<Activity, BrainActivityGroup<BrainBasedZombie>> getAdditionalTasks() {
		return SmartBrainOwner.super.getAdditionalTasks();
	}

	@Override
	public Set<Activity> getAlwaysRunningActivities() {
		return SmartBrainOwner.super.getAlwaysRunningActivities();
	}

	@Override
	public Activity getDefaultActivity() {
		return SmartBrainOwner.super.getDefaultActivity();
	}

	@Override
	public List<Activity> getActivityPriorities() {
		return SmartBrainOwner.super.getActivityPriorities();
	}

	@Override
	public void handleAdditionalBrainSetup(Brain<BrainBasedZombie> brain) {
		SmartBrainOwner.super.handleAdditionalBrainSetup(brain);
	}
}
