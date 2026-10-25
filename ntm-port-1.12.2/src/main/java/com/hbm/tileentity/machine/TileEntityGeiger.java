package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.handler.CompatHandler;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.util.CompatEnergyControl;
import com.hbm.util.ContaminationUtil;

import api.hbm.redstoneoverradio.IRORValueProvider;
import api.hbm.tile.IInfoProviderEC;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityGeiger extends TileEntity implements SimpleComponent, IInfoProviderEC, CompatHandler.OCComponent, IRORValueProvider, net.minecraft.util.ITickable {

	int timer = 0;
	float ticker = 0;

	@Override
	public void update() {

		timer++;

		if(timer == 10) {
			timer = 0;
			ticker = check();

			// To update the adjacent comparators
			this.world.notifyNeighborsOfStateChange(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()), this.getBlockType(), true);
		}

		if(timer % 5 == 0) {

			if(ticker > 0) {
				if(this.world.rand.nextFloat() > ticker) return;
				List<Integer> list = new ArrayList<Integer>();

				if(ticker < 1) list.add(0);
				if(ticker < 5) list.add(0);
				if(ticker < 10) list.add(1);
				if(ticker > 5 && ticker < 15) list.add(2);
				if(ticker > 10 && ticker < 20) list.add(3);
				if(ticker > 15 && ticker < 25) list.add(4);
				if(ticker > 20 && ticker < 30) list.add(5);
				if(ticker > 25) list.add(6);

				int r = list.get(this.world.rand.nextInt(list.size()));

				if(r > 0) this.world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), com.hbm.main.ModSoundEvents.resolve("hbm:item.geiger" + r), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);

			} else if(this.world.rand.nextInt(50) == 0) {
				this.world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), com.hbm.main.ModSoundEvents.resolve("hbm:item.geiger"+ (1 + this.world.rand.nextInt(1))), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}

	}

	public float check() {
		return ChunkRadiationManager.proxy.getRadiation(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_geiger";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getRads(Context context, Arguments args) {
		return new Object[] {check()};
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		int rads = (int) Math.ceil(ticker);
		String chunkPrefix = ContaminationUtil.getPreffixFromRad(rads);
		data.setString(CompatEnergyControl.S_CHUNKRAD, chunkPrefix + rads + " RAD/s");

	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + 1, this.pos.getZ() + 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_VALUE + "rad",
		};
	}

	@Override
	public String provideRORValue(String name) {
		if((PREFIX_VALUE + "rad").equals(name))	return "" + (int) Math.ceil(ticker);
		return null;
	}
}
