package com.hbm.saveddata.satellites;

import com.hbm.entity.projectile.EntityTom;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.SatelliteSavedData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class SatelliteHorizons extends Satellite {

	boolean used = false;

	public SatelliteHorizons() {
		this.satIface = Interfaces.SAT_COORD;
	}

	public void onOrbit(World world, double x, double y, double z) {

		for(Object p : world.playerEntities)
			com.hbm.advancement.NtmAdvancements.award(((EntityPlayer)p), MainRegistry.horizonsStart);
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("used", used);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		used = nbt.getBoolean("used");
	}

	public void onCoordAction(World world, EntityPlayer player, int x, int y, int z) {

		if(used)
			return;

		used = true;
		SatelliteSavedData.getData(world, x, z).markDirty();


		EntityTom tom = new EntityTom(world);
		tom.setPosition(x + 0.5, 600, z + 0.5);

		IChunkProvider provider = world.getChunkProvider();
		provider.loadChunk(x >> 4, z >> 4);

		world.spawnEntityInWorld(tom);
		for(Object p : world.playerEntities)
			com.hbm.advancement.NtmAdvancements.award(((EntityPlayer)p), MainRegistry.horizonsEnd);

		//not necessary but JUST to make sure
		if(!world.isRemote) {

			world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString(TextFormatting.RED + "Horizons has been activated."));
		}
	}

	@Override
	protected float[] getColor() {
		return new float[] { 0.0F, 0.0F, 0.0F, 0.0F };
	}

}
