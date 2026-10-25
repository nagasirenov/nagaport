package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.FloodlightBeam.TileEntityFloodlightBeam;
import com.hbm.util.Compat;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import api.hbm.block.IToolable;
import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class Floodlight extends BlockContainer implements IToolable, INBTBlockTransformable {

	public Floodlight(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFloodlight();
	}

	@Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}
	@Override public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) { return false; }
	@Override public boolean isFullCube(net.minecraft.block.state.IBlockState state) { return false; }

	//only method that respects sides, called first for orientation
	public int onBlockPlaced(World world, int x, int y, int z, int side, float fX, float fY, float fZ, int meta) {
		return side;
	}

	//only method with player param, called second for variable rotation
	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		setAngle(world, x, y, z, player, true);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		setAngle(world, x, y, z, player, false);
		return true;
	}

	public void setAngle(World world, int x, int y, int z, EntityLivingBase player, boolean updateMeta) {

		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		float rotation = player.rotationPitch;

		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile instanceof TileEntityFloodlight) {
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 6;
			TileEntityFloodlight floodlight = (TileEntityFloodlight) tile;

			if(meta == 0 || meta == 1) {
				if(i == 0 || i == 2) if(updateMeta) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta + 6, 3);
				if(meta == 1) if(i == 0 || i == 1) rotation = 180F - rotation;
				if(meta == 0) if(i == 0 || i == 3) rotation = 180F - rotation;
			}

			floodlight.rotation = -Math.round(rotation / 5F) * 5F;
			if(floodlight.isOn) floodlight.destroyLights();
			tile.markDirty();
		}
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		if(meta < 6) {
			switch(coordBaseMode) {
				case 1: // West
					switch(meta) {
						case 2: return 5;
						case 3: return 4;
						case 4: return 2;
						case 5: return 3;
					}
					break;
				case 2: // North
					switch(meta) {
						case 2: return 3;
						case 3: return 2;
						case 4: return 5;
						case 5: return 4;
					}
					break;
				case 3: // East
					switch(meta) {
						case 2: return 4;
						case 3: return 5;
						case 4: return 3;
						case 5: return 2;
					}
					break;
			}
		}

		// Also rotate the upper bits that store additional state (6-11)
		if(meta >= 6) {
			return transformMeta(meta - 6, coordBaseMode) + 6;
		}

		return meta;
	}

	@Override
	public Block transformBlock(Block block) {
		return block; // No block transformation needed
	}

	public static class TileEntityFloodlight extends TileEntity implements IEnergyReceiverMK2, net.minecraft.util.ITickable {

		public float rotation;
		protected BlockPos[] lightPos = new BlockPos[15];
		public static final long maxPower = 5_000;
		public long power;

		public int delay;
		public boolean isOn;

		@Override
		public void update() {

			if(!this.world.isRemote) {

				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() % 6).getOpposite();
				this.trySubscribe(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);

				if(delay > 0) {
					delay --;
					return;
				}

				if(power >= 100) {
					power -= 100;

					if(!isOn) {
						this.isOn = true;
						this.castLights();
						this.world.markChunkDirty(this.pos, this);
						com.hbm.world.WorldBlockState.markBlockForUpdate(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());

					} else {

						long timer = this.world.getTotalWorldTime();
						if(timer % 5 == 0) {
							timer = timer / 5;
							this.castLight((int) Math.abs(timer % this.lightPos.length));
						}
					}

				} else {
					if(isOn) {
						this.isOn = false;
						this.delay = 60;
						this.destroyLights();
						this.world.markChunkDirty(this.pos, this);
						com.hbm.world.WorldBlockState.markBlockForUpdate(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
					}
				}
			}
		}

		private void castLight(int index) {
			BlockPos newPos = this.getRayEndpoint(index);
			BlockPos oldPos = this.lightPos[index];
			this.lightPos[index] = null;

			if(newPos == null || !newPos.equals(oldPos)) { //if the new end point is null or not equal to the previous, delete the previous spot
				if(oldPos != null) {
					TileEntity tile = Compat.getTileStandard(this.world, oldPos.getX(), oldPos.getY(), oldPos.getZ());
					if(tile instanceof TileEntityFloodlightBeam) {
						TileEntityFloodlightBeam beam = (TileEntityFloodlightBeam) tile;
						if(beam.cache == this) {
							com.hbm.world.WorldBlockState.write(this.world, oldPos.getX(), oldPos.getY(), oldPos.getZ(), Blocks.AIR.getStateFromMeta(0), 2);
						}
					}
				}
			}

			if(newPos == null) return;

			if(com.hbm.world.WorldBlockState.read(this.world, newPos.getX(), newPos.getY(), newPos.getZ()).getBlock() == Blocks.AIR) {
				com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, newPos.getX(), newPos.getY(), newPos.getZ(), ModBlocks.floodlight_beam, 0, 2);
				TileEntity tile = Compat.getTileStandard(this.world, newPos.getX(), newPos.getY(), newPos.getZ());
				if(tile instanceof TileEntityFloodlightBeam) ((TileEntityFloodlightBeam) tile).setSource(this, newPos.getX(), newPos.getY(), newPos.getZ(), index);
				this.lightPos[index] = newPos;
			}

			if(com.hbm.world.WorldBlockState.read(this.world, newPos.getX(), newPos.getY(), newPos.getZ()).getBlock() == ModBlocks.floodlight_beam) {
				this.lightPos[index] = newPos;
			}
		}

		public BlockPos getRayEndpoint(int index) {

			if(index < 0 || index >= lightPos.length) return null;

			int meta = this.getBlockMetadata();
			com.hbm.util.Vec3NT dir = new com.hbm.util.Vec3NT(1, 0, 0);

			float[] angles = getVariation(index);

			float rotation = this.rotation;
			if(meta == 1 || meta == 7) rotation = 180 - rotation;
			if(meta == 6) rotation = 180 - rotation;

			dir.rotateAroundZRad((float) (rotation / 180D * Math.PI) + angles[0]);
			if(meta == 6) dir.rotateAroundYRad((float) (Math.PI / 2D));
			if(meta == 7) dir.rotateAroundYRad((float) (Math.PI / 2D));
			if(meta == 2) dir.rotateAroundYRad((float) (Math.PI / 2D));
			if(meta == 3) dir.rotateAroundYRad((float) -(Math.PI / 2D));
			if(meta == 4) dir.rotateAroundYRad((float) (Math.PI));
			dir.rotateAroundYRad(angles[1]);

			for(int i = 1; i < 64; i++) {
				int iX = (int) Math.floor(this.pos.getX() + 0.5 + dir.x * i);
				int iY = (int) Math.floor(this.pos.getY() + 0.5 + dir.y * i);
				int iZ = (int) Math.floor(this.pos.getZ() + 0.5 + dir.z * i);

				if(iX == this.pos.getX() && iY == this.pos.getY() && iZ == this.pos.getZ()) continue;

				Block block = com.hbm.world.WorldBlockState.read(this.world, iX, iY, iZ).getBlock();
				if(com.hbm.world.WorldBlockState.read(this.world, iX, iY, iZ).getLightOpacity() < 127) continue;

				int fX = (int) Math.floor(this.pos.getX() + 0.5 + dir.x * (i - 1));
				int fY = (int) Math.floor(this.pos.getY() + 0.5 + dir.y * (i - 1));
				int fZ = (int) Math.floor(this.pos.getZ() + 0.5 + dir.z * (i - 1));

				if(i > 1) return new BlockPos(fX, fY, fZ);
			}

			return null;
		}

		private void castLights() {
			for(int i = 0; i < this.lightPos.length; i++) this.castLight(i);
		}

		private void destroyLight(int index) {
			BlockPos pos = lightPos[index];
			if(pos != null) {
				if(pos != null && com.hbm.world.WorldBlockState.read(this.world, pos.getX(), pos.getY(), pos.getZ()).getBlock() == ModBlocks.floodlight_beam) {
					com.hbm.world.WorldBlockState.write(this.world, pos.getX(), pos.getY(), pos.getZ(), Blocks.AIR.getStateFromMeta(0), 2);
				}
			}
		}

		private void destroyLights() {
			for(int i = 0; i < this.lightPos.length; i++) destroyLight(i);
		}

		private float[] getVariation(int index) {
			return new float[] {
					(((index / 3) - 2) * 7.5F) / 180F * (float) Math.PI,
					(((index % 3) - 1) * 15F) / 180F * (float) Math.PI
				};
		}

		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.rotation = nbt.getFloat("rotation");
			this.power = nbt.getLong("power");
			this.isOn = nbt.getBoolean("isOn");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setFloat("rotation", rotation);
			nbt.setLong("power", power);
			nbt.setBoolean("isOn", isOn);
			return nbt;
		}

		@Override public long getPower() { return power; }
		@Override public void setPower(long power) { this.power = power; }
		@Override public long getMaxPower() { return maxPower; }

		private boolean isLoaded = true;
		@Override public boolean isLoaded() { return isLoaded; }
		@Override public void onChunkUnload() { this.isLoaded = false; }

		AxisAlignedBB bb = null;

		@Override
		public AxisAlignedBB getRenderBoundingBox() {

			if(bb == null) {
				bb = new AxisAlignedBB(
						this.pos.getX() - 1,
						this.pos.getY() - 1,
						this.pos.getZ() - 1,
						this.pos.getX() + 2,
						this.pos.getY() + 2,
						this.pos.getZ() + 2
						);
			}

			return bb;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public double getMaxRenderDistanceSquared() {
			return 65536.0D;
		}
	}
}
