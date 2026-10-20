package com.hbm.tileentity.machine;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.util.CrucibleUtil;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;

public class TileEntityFoundryOutlet extends TileEntityFoundryBase{

	public NTMMaterial filter = null;
	public NTMMaterial lastFilter = null;
	/* inverts filter behavior, will let everything but the filter material pass */
	public boolean invertFilter = false;
	/** inverts redstone behavior, i.e. when TRUE, the outlet will be blocked by default and only open with redstone */
	public boolean invertRedstone = false;
	public boolean lastClosed = false;

	/** if TRUE, prevents all fluids from flowing through the outlet and renders a small barrier */
	public boolean isClosed() {
		return invertRedstone ^ this.world.isBlockPowered(this.pos);
	}

	@Override
	public void update() {
		super.update();

		if(this.world.isRemote) {
			boolean isClosed = isClosed();
			if(this.lastClosed != isClosed || this.filter != this.lastFilter) {
				this.lastFilter = this.filter;
				this.lastClosed = isClosed;
				this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
			}
		}
	}

	@Override public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return stack; }

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {

		if(filter != null && (filter != stack.material ^ invertFilter)) return false;
		if(isClosed()) return false;
		if(side != ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite()) return false;

		Vec3d start = new Vec3d(x + 0.5, y - 0.125, z + 0.5);
		Vec3d end = new Vec3d(x + 0.5, y + 0.125 - 4, z + 0.5);

		RayTraceResult[] mop = new RayTraceResult[1];
		ICrucibleAcceptor acc = CrucibleUtil.getPouringTarget(world, start, end, mop);

		if(acc == null) {
			return false;
		}

		return acc.canAcceptPartialPour(world, mop[0].getBlockPos().getX(), mop[0].getBlockPos().getY(), mop[0].getBlockPos().getZ(), mop[0].hitVec.x, mop[0].hitVec.y, mop[0].hitVec.z, ForgeDirection.UP, stack);
	}

	@Override
	public MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {

		Vec3d start = new Vec3d(x + 0.5, y - 0.125, z + 0.5);
		Vec3d end = new Vec3d(x + 0.5, y + 0.125 - 4, z + 0.5);

		RayTraceResult[] mop = new RayTraceResult[1];
		ICrucibleAcceptor acc = CrucibleUtil.getPouringTarget(world, start, end, mop);

		if(acc == null)
			return stack;

		MaterialStack didPour = acc.pour(world, mop[0].getBlockPos().getX(), mop[0].getBlockPos().getY(), mop[0].getBlockPos().getZ(), mop[0].hitVec.x, mop[0].hitVec.y, mop[0].hitVec.z, ForgeDirection.UP, stack);


		if(stack != null) {

			ForgeDirection dir = side.getOpposite();
			double hitY = mop[0].getBlockPos().getY() + 1;

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "foundry");
			data.setInteger("color", stack.material.moltenColor);
			data.setByte("dir", (byte) dir.ordinal());
			data.setFloat("off", 0.375F);
			data.setFloat("base", 0F);
			data.setFloat("len", Math.max(1F, this.pos.getY() - (float) (Math.ceil(hitY) - 0.875)));
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, this.pos.getX() + 0.5D - dir.offsetX * 0.125, this.pos.getY() + 0.125, this.pos.getZ() + 0.5D - dir.offsetZ * 0.125), new TargetPoint(this.world.provider.getDimension(), this.pos.getX() + 0.5, this.pos.getY(), this.pos.getZ() + 0.5, 50));

		}

		return didPour;
	}

	@Override
	public int getCapacity() {
		return 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.invertRedstone = nbt.getBoolean("invert");
		this.invertFilter = nbt.getBoolean("invertFilter");
		this.filter = Mats.matById.get((int) nbt.getShort("filter"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("invert", this.invertRedstone);
		nbt.setBoolean("invertFilter", this.invertFilter);
		nbt.setShort("filter", this.filter == null ? -1 : (short) this.filter.id);
		return nbt;
	}

	@Override
	public NBTTagCompound getSettings(World world, int x, int y, int z) {
		NBTTagCompound nbt = new NBTTagCompound();

		nbt.setBoolean("invert", this.invertRedstone);
		nbt.setBoolean("invertFilter", this.invertFilter);
		if(filter != null){
			nbt.setIntArray("matFilter", new int[]{ filter.id });
		}

		return nbt;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {

		if(nbt.hasKey("invert"))   this.invertRedstone = nbt.getBoolean("invert");
		if(nbt.hasKey("invertFilter")) this.invertFilter = nbt.getBoolean("invertFilter");
		if(nbt.hasKey("matFilter")) {
			int[] ids = nbt.getIntArray("matFilter");
			if(ids.length > 0 && index < ids.length)
				this.filter = Mats.matById.get(ids[index]);
		}

	}

	@Override
	public String[] infoForDisplay(World world, int x, int y, int z) {
		ArrayList<String> info = new ArrayList<>();
		info.add("copytool.invertRedstone");
		info.add("copytool.invertFilter");
		if (filter != null) info.add(filter.getUnlocalizedName());
		return info.toArray(new String[0]);
	}
}
