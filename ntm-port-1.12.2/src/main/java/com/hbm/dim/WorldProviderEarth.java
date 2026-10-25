package com.hbm.dim;

public class WorldProviderEarth extends WorldProviderCelestial {

	@Override
	public net.minecraft.world.DimensionType getDimensionType() {
		return net.minecraftforge.common.DimensionManager.getProviderType(this.getDimension());
	}

	@Override
	public boolean hasLife() {
		return true;
	}

	@Override
	public boolean canRespawnHere() {
		return true;
	}
	
	@Override
	public void updateWeather() {
		super.updateWeather();
	}

}
