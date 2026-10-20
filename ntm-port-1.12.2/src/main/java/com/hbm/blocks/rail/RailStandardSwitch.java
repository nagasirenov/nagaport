package com.hbm.blocks.rail;


import com.hbm.blocks.BlockDummyable;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class RailStandardSwitch extends BlockRailWaypointSystem implements IRenderBlock {


	public RailStandardSwitch() {
		super(Material.IRON);
		
		RailDef main = new RailDef("main");
		RailDef side = new RailDef("side");
		railDefs.add(main);
		railDefs.add(side);

		main.nodes.add(new Vec3d(-8.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(-7.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(6.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(7.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(8.5, 0.1875, 0.5));

		side.nodes.add(new Vec3d(-8.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-7.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-6.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-5.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-4.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-3.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-2.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-1.5, 0.1875, 4.5));
		side.nodes.add(new Vec3d(-0.5, 0.1875, 4.25));
		side.nodes.add(new Vec3d(0.5, 0.1875, 3.9375));
		side.nodes.add(new Vec3d(1.5, 0.1875, 3.375));
		side.nodes.add(new Vec3d(2.5, 0.1875, 2.4625));
		side.nodes.add(new Vec3d(3.5, 0.1875, 1.75));
		side.nodes.add(new Vec3d(4.5, 0.1875, 1.1875));
		side.nodes.add(new Vec3d(5.5, 0.1875, 0.875));
		side.nodes.add(new Vec3d(6.5, 0.1875, 0.625));
		side.nodes.add(new Vec3d(7.5, 0.1875, 0.5));
		side.nodes.add(new Vec3d(8.5, 0.1875, 0.5));
	}
	

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRailSwitch();
	}


	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 7, 7, 1, 0};
	}

	@Override
	public int getOffset() {
		return 7;
	}

	@Override
	public TrackGauge getGauge(World world, int x, int y, int z) {
		return TrackGauge.STANDARD;
	}

	@Override
	public boolean canCross(World world, int x, int y, int z, Vec3d from, Vec3d to, RailDef def) {
		TileEntityRailSwitch tile = (TileEntityRailSwitch) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		if(tile == null) return true;
		
		ForgeDirection dir = ForgeDirection.getOrientation(tile.getBlockMetadata() - 10);

		if(dir == Library.POS_X) if(from.x < to.x) return true;
		if(dir == Library.NEG_X) if(from.x > to.x) return true;
		if(dir == Library.POS_Z) if(from.z < to.z) return true;
		if(dir == Library.NEG_Z) if(from.z > to.z) return true;
		
		if(dir == Library.POS_X) if(to.x < x + 0.5 + 7) return true;
		if(dir == Library.NEG_X) if(to.x > x + 0.5 - 7) return true;
		if(dir == Library.POS_Z) if(to.z < z + 0.5 + 7) return true;
		if(dir == Library.NEG_Z) if(to.z > z + 0.5 - 7) return true;
		
		if(tile.isSwitched) {
			if("side".equals(def.name)) return true;
		} else {
			if("main".equals(def.name)) return true;
		}
		
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos at, net.minecraft.block.state.IBlockState state, EntityPlayer player, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing side, float hitX, float hitY, float hitZ) {
		int x = at.getX(), y = at.getY(), z = at.getZ();
		
		if(world.isRemote) return true;
		if(player.isSneaking()) return false;
		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.train) return false;
		
		int[] pos = this.findCore(world, x, y, z);
		
		if(pos != null) {
			TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
			
			if(tile instanceof TileEntityRailSwitch) {
				TileEntityRailSwitch sw = (TileEntityRailSwitch) tile;
				sw.isSwitched = !sw.isSwitched;
				sw.markDirty();
				net.minecraft.util.math.BlockPos core = new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]);
				world.notifyBlockUpdate(core, world.getBlockState(core), world.getBlockState(core), 3);
			}
		}
		
		return true;
	}
	
	@Override
	public Vec3d snapAndMove(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info) {
		return super.snapAndMove(world, x, y, z, trainX, trainY, trainZ, motionX, motionY, motionZ, speed, info);
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(!super.checkRequirement(world, x, y, z, dir, o)) return false;
		
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		dir = dir.getOpposite();

		int dX = dir.offsetX;
		int dZ = dir.offsetZ;
		int rX = rot.offsetX;
		int rZ = rot.offsetZ;

		for(int i = 0; i < 4; i++) if(!world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * (2 + i) + rX * 2, y, z + dZ * (2 + i) + rZ * 2)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * (2 + i) + rX * 2, y, z + dZ * (2 + i) + rZ * 2))) return false;
		for(int i = 0; i < 2; i++) if(!world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * (4 + i) + rX * 3, y, z + dZ * (4 + i) + rZ * 3)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * (4 + i) + rX * 3, y, z + dZ * (4 + i) + rZ * 3))) return false;
		if(!world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 5 + rX * 4, y, z + dZ * 5 + rZ * 4)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 5 + rX * 4, y, z + dZ * 5 + rZ * 4))) return false;
		for(int j = 0; j < 2; j++) for(int i = 0; i < 2; i++) if(!world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * (6 + j) + rX * (3 + i), y, z + dZ * (6 + j) + rZ * (3 + i))).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * (6 + j) + rX * (3 + i), y, z + dZ * (6 + j) + rZ * (3 + i)))) return false;
		if(!world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 7 + rX * 5, y, z + dZ * 7 + rZ * 5)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 7 + rX * 5, y, z + dZ * 7 + rZ * 5))) return false;
		for(int j = 0; j < 7; j++) for(int i = 0; i < 2; i++) if(!world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * (8 + j) + rX * (4 + i), y, z + dZ * (8 + j) + rZ * (4 + i))).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * (8 + j) + rX * (4 + i), y, z + dZ * (8 + j) + rZ * (4 + i)))) return false;
		
		return true;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		
		BlockDummyable.safeRem = true;
		
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		dir = dir.getOpposite();

		int dX = dir.offsetX;
		int dZ = dir.offsetZ;
		int rX = rot.offsetX;
		int rZ = rot.offsetZ;

		for(int i = 0; i < 4; i++) world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * (2 + i) + rX * 2, y, z + dZ * (2 + i) + rZ * 2), this.getStateFromMeta(rot.ordinal()), 3);
		for(int i = 0; i < 2; i++) world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * (4 + i) + rX * 3, y, z + dZ * (4 + i) + rZ * 3), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 5 + rX * 4, y, z + dZ * 5 + rZ * 4), this.getStateFromMeta(rot.ordinal()), 3);
		for(int j = 0; j < 2; j++) for(int i = 0; i < 2; i++) world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * (6 + j) + rX * (3 + i), y, z + dZ * (6 + j) + rZ * (3 + i)), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 7 + rX * 5, y, z + dZ * 7 + rZ * 5), this.getStateFromMeta(rot.ordinal()), 3);
		for(int j = 0; j < 7; j++) for(int i = 0; i < 2; i++) world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * (8 + j) + rX * (4 + i), y, z + dZ * (8 + j) + rZ * (4 + i)), this.getStateFromMeta(dir.ordinal()), 3);
		
		BlockDummyable.safeRem = false;
	}


	
	public static class TileEntityRailSwitch extends TileEntity {
		public boolean isSwitched = false;
		

		@Override
		public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }

		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
		}
		
		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
			this.world.markBlockRangeForRenderUpdate(this.getPos(), this.getPos());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.isSwitched = nbt.getBoolean("isSwitched");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setBoolean("isSwitched", this.isSwitched);
			return nbt;
		}
	}
}
