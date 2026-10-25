package com.hbm.dim;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class CelestialBodyWorldSavedData extends WorldSavedData {

	private static final String DATA_NAME = "CelestialBodyData";

	public CelestialBodyWorldSavedData(String name) {
		super(name);
	}

	private WorldProviderCelestial provider;
	private long localTime;

	private boolean requiresLoad;
	private NBTTagCompound nbt;
	
	public static CelestialBodyWorldSavedData get(WorldProviderCelestial provider) {
		if(provider == null || provider.getWorldObj() == null) return null;
		MapStorage storage = provider.getWorldObj().getMapStorage();
		if(storage == null) return null;

		CelestialBodyWorldSavedData result = (CelestialBodyWorldSavedData) storage.getOrLoadData(CelestialBodyWorldSavedData.class, DATA_NAME);
		
		if(result == null) {
			result = new CelestialBodyWorldSavedData(DATA_NAME);
			storage.setData(DATA_NAME, result);
		}

		// readFromNBT is called before the result is returned and a WorldProvider is made available, so we call this here
		if(result.requiresLoad) {
			result.requiresLoad = false;
			provider.readFromNBT(result.nbt);
		}

		result.provider = provider;
		
		return result;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		localTime = nbt.getLong("time");
		this.nbt = nbt;
		requiresLoad = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("time", localTime);
		provider.writeToNBT(nbt);
		return nbt;
	}

	public long getLocalTime() {
		return localTime;
	}

	public void setLocalTime(long time) {
		localTime = time;
		markDirty();
	}
	
}
