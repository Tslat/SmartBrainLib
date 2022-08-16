package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.example.SBLSkeleton;

public final class SBLExampleEntities {
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, SmartBrainLib.MOD_ID);

	// Example entities begin ---->
	public static final RegistryObject<EntityType<SBLSkeleton>> SBL_SKELETON = register("sbl_skeleton", SBLSkeleton::new, MobCategory.MONSTER, 0.6F, 1.99F);
	// Example entities end ---->

	private static <T extends LivingEntity> RegistryObject<EntityType<T>> register(String registryName, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
		return ENTITY_TYPES.register(registryName, () -> EntityType.Builder.of(factory, category).sized(width, height).build(registryName));
	}

	public static void init(IEventBus modEventBus) {
		ENTITY_TYPES.register(modEventBus);
		modEventBus.addListener(EventPriority.NORMAL, false, EntityAttributeCreationEvent.class, SBLExampleEntities::registerStats);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(EventPriority.NORMAL, false, EntityRenderersEvent.RegisterRenderers.class, SBLExampleEntityRenderers::registerEntityRenderers));
	}

	private static void registerStats(final EntityAttributeCreationEvent ev) {
		ev.put(SBL_SKELETON.get(), Skeleton.createAttributes().build());
	}
}
