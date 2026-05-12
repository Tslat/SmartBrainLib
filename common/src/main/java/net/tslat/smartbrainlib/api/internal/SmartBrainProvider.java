package net.tslat.smartbrainlib.api.internal;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.tslat.smartbrainlib.api.SmartBrainBuilder;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.WeakReference;
import java.util.List;

/// `SmartBrainLib` extension of the vanilla [Brain.Provider]
///
/// Functionally this is just an implementation detail and has little to no outward use, however `SmartBrainLib` implements it for compatibility purposes
public class SmartBrainProvider<BO extends LivingEntity & SmartBrainOwner<BO>> extends Brain.Provider<BO> {
	protected final WeakReference<SmartBrainBuilder<BO>> builder;

	/// Create a new instanceof [SmartBrainProvider] for the given [SmartBrainBuilder] instance
	///
	/// @see #create(LivingEntity)
	@ApiStatus.Internal
	public SmartBrainProvider(SmartBrainBuilder<BO> builder) {
		super(ImmutableList.of(), List.of(), _ -> List.of());

		this.builder = new WeakReference<>(builder);
	}

	/// Create a new [SmartBrainProvider] instance for the given [Entity]
	///
	/// This method should **<u>NOT</u>** be used to store a static reference.<br/>
	/// If you intend to store this in a field for re-use for your entity, store it as an instance field instead
	public static <BO extends LivingEntity & SmartBrainOwner<BO>> SmartBrainProvider<BO> create(BO entity) {
		return new SmartBrainProvider<>(entity.getBrainBuilder());
	}

	/// Create a [SmartBrain] instance from this provider
	public SmartBrain<BO> makeBrain(BO entity) {
		return makeBrain(entity, Brain.Packed.EMPTY);
	}

	/// Create a [SmartBrain] instance from this provider
	@ApiStatus.Internal
	@Override
	public SmartBrain<BO> makeBrain(BO entity, Brain.Packed packedBrain) {
		final SmartBrainBuilder<BO> builder = this.builder.get();

		if (builder == null)
			return entity.getBrainBuilder().makeBrain(entity, packedBrain);

		return builder.makeBrain(entity, packedBrain);
	}
}
