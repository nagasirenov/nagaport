package com.hbm.sound;

import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

/** Client resource sounds resolve through SoundHandler, not the network SoundEvent registry. */
abstract class ResourceLoopSound extends PositionedSound {

	protected ResourceLoopSound(ResourceLocation location) {
		super(location, SoundCategory.MASTER);
	}

	@Override
	public SoundCategory getCategory() {
		// Query the current metadata, including for a sound created before a reload.
		return SoundCategoryResolver.get(positionedSoundLocation);
	}

	@Override
	public float getVolume() { return sound == null ? volume : super.getVolume(); }

	@Override
	public float getPitch() { return sound == null ? pitch : super.getPitch(); }

	// The wrapper API exposes original caller values. Native PositionedSound getters
	// instead multiply by the selected sample, which is not available before playback.
	final float getUnscaledVolume() { return volume; }
	final float getUnscaledPitch() { return pitch; }
}
