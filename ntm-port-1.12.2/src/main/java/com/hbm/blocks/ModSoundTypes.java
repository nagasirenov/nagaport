package com.hbm.blocks;

import com.hbm.main.MainRegistry;
import net.minecraft.block.SoundType;

public class ModSoundTypes {
	/** Original 1.7 metal type used stone samples and a 1.5 base pitch. */
	public static final ModSoundType legacyMetal = new ModSoundType(SoundType.STONE.getPlaceSound(),
			SoundType.STONE.getBreakSound(), SoundType.STONE.getStepSound(), 1F, 1.5F);
	
	public static final ModSoundType grate = ModSoundType.customStep(SoundType.STONE, "hbm:step.metalBlock", 0.5F, 1.0F);
	// 1.7 soundTypeMetal used stone samples with a higher default pitch. The
	// custom factory overrides that pitch; retaining native METAL would change its event.
	public static final ModSoundType pipe = ModSoundType.customDig(SoundType.STONE, "hbm:block.pipePlaced", 0.85F, 0.85F).enveloped(MainRegistry.instance.rand).pitchFunction((in, rand, type) -> {
		if(type == ModSoundType.SubType.BREAK) in -= 0.15F;
		return in + rand.nextFloat() * 0.2F;
	});
	public static final ModSoundType mork = ModSoundType.customBreak(SoundType.STONE, "hbm:step.morkite", 0.85F, 0.85F).enveloped(MainRegistry.instance.rand).pitchFunction((in, rand, type) -> {
		if(type == ModSoundType.SubType.BREAK) in -= 0.15F;
		return in + rand.nextFloat() * 0.2F;
	});
}
