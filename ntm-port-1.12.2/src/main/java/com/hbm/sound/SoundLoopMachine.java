package com.hbm.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class SoundLoopMachine extends ResourceLoopSound implements ITickableSound {
	boolean donePlaying = false;
	TileEntity te;

	public SoundLoopMachine(ResourceLocation path, TileEntity te) {
		super(path);
		this.repeat = true;
		this.volume = 1;
		this.pitch = 1;
		this.xPosF = te.getPos().getX();
		this.yPosF = te.getPos().getY();
		this.zPosF = te.getPos().getZ();
		this.repeatDelay = 0;
		this.te = te;
	}

	@Override
	public void update() {
		if(te == null || (te != null && te.isInvalid()))
			donePlaying = true;
	}

	@Override
	public boolean isDonePlaying() {
		return this.donePlaying;
	}
	
	public void setVolume(float f) {
		volume = f;
	}
	
	public void setPitch(float f) {
		pitch = f;
	}
	
	public void stop() {
		donePlaying = true;
	}
}
