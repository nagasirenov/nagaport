package com.hbm.main;

import com.hbm.lib.RefStrings;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class ModSoundEvents {

	public static final SoundEvent SLICER = new SoundEvent(
			new ResourceLocation(RefStrings.MODID, "entity.slicer"))
				.setRegistryName(RefStrings.MODID, "entity.slicer");

	private ModSoundEvents() { }

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		event.getRegistry().register(SLICER);
	}
}
