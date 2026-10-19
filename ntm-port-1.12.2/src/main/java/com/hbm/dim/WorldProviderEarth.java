package com.hbm.dim;

public class WorldProviderEarth extends WorldProviderCelestial {

	@Override
	public String getDimensionName() {
		return "Overworld";
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
