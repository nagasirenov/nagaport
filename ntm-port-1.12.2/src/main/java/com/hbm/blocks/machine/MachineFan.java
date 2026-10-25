package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.handler.atmosphere.IBlockSealable;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.ChatBuilder;

import api.hbm.block.IBlowable;
import api.hbm.block.IToolable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class MachineFan extends BlockContainer implements IToolable, ITooltipProvider, IBlockSealable {

	public MachineFan() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFan();
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int l = net.minecraft.util.EnumFacing.getDirectionFromEntityLiving(new net.minecraft.util.math.BlockPos(x, y, z), player).getIndex();
		com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, l, 2);
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		int meta = com.hbm.world.WorldBlockState.readMetadata(world, pos.getX(), pos.getY(), pos.getZ());

		if(side == EnumFacing.UP || side == EnumFacing.DOWN) return meta != 0 && meta != 1;
		if(side == EnumFacing.NORTH || side == EnumFacing.SOUTH) return meta != 2 && meta != 3;
		if(side == EnumFacing.EAST || side == EnumFacing.WEST) return meta != 4 && meta != 5;

		return false;
	}

	public static class TileEntityFan extends TileEntityLoadedBase implements net.minecraft.util.ITickable {

		public float spin;
		public float prevSpin;

		private boolean hasAtmosphere;
		public boolean falloff = true;

		@Override
		public void update() {

			this.prevSpin = this.spin;

			if(this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()))) {
				if(!this.world.isRemote) {
					CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
					hasAtmosphere = atmosphere != null && atmosphere.getPressure() > 0.01D;
				}

				if(hasAtmosphere) {
					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());

					int range = 10;
					int effRange = 0;
					double push = 0.1;

					for(int i = 1; i <= range; i++) {
						net.minecraft.block.state.IBlockState nativeCubeState_block = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + dir.offsetX * i, this.pos.getY() + dir.offsetY * i, this.pos.getZ() + dir.offsetZ * i);
						Block block = nativeCubeState_block.getBlock();
						boolean blowable = block instanceof IBlowable;

						if(block.isNormalCube(nativeCubeState_block) || blowable) {
							if(!this.world.isRemote && blowable)
								((IBlowable) block).applyFan(this.world, this.pos.getX() + dir.offsetX * i, this.pos.getY() + dir.offsetY * i, this.pos.getZ() + dir.offsetZ * i, dir, i);

							break;
						}

						effRange = i;
					}

					int x = dir.offsetX * effRange;
					int y = dir.offsetY * effRange;
					int z = dir.offsetZ * effRange;

					List<Entity> affected = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.pos.getX() + 0.5 + Math.min(x, 0), this.pos.getY() + 0.5 + Math.min(y, 0), this.pos.getZ() + 0.5 + Math.min(z, 0), this.pos.getX() + 0.5 + Math.max(x, 0), this.pos.getY() + 0.5 + Math.max(y, 0), this.pos.getZ() + 0.5 + Math.max(z, 0)).expand(0.5, 0.5, 0.5));

					for(Entity e : affected) {

						double coeff = push;

						if(falloff) {
							double dist = e.getDistance(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5);
							coeff *= 1.5 * (1 - dist / range / 2);
						}

						e.motionX += dir.offsetX * coeff;
						e.motionY += dir.offsetY * coeff;
						e.motionZ += dir.offsetZ * coeff;
					}

					if(this.world.isRemote && this.world.rand.nextInt(30) == 0) {
						double speed = 0.2;
						this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.CLOUD, this.pos.getX() + 0.5 + dir.offsetX * 0.5, this.pos.getY() + 0.5 + dir.offsetY * 0.5, this.pos.getZ() + 0.5 + dir.offsetZ * 0.5, dir.offsetX * speed, dir.offsetY * speed, dir.offsetZ * speed);
					}
				}

				this.spin += 30;
			}

			if(this.spin >= 360) {
				this.prevSpin -= 360;
				this.spin -= 360;
			}

			if(!this.world.isRemote) {
				networkPackNT(150);
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public double getMaxRenderDistanceSquared() {
			return 65536.0D;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.falloff = nbt.getBoolean("falloff");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setBoolean("falloff", falloff);
			return nbt;
		}

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeBoolean(falloff);
			buf.writeBoolean(hasAtmosphere);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			falloff = buf.readBoolean();
			hasAtmosphere = buf.readBoolean();
		}
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool == ToolType.SCREWDRIVER) {
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

			if(meta == 0) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 1, 3);
			if(meta == 1) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 0, 3);
			if(meta == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 3);
			if(meta == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 3);
			if(meta == 4) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 3);
			if(meta == 5) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 3);

			return true;
		}

		if(tool == ToolType.HAND_DRILL) {
			TileEntityFan tile = (TileEntityFan) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(tile != null) {
				tile.falloff = !tile.falloff;
				tile.markDirty();

				if(!world.isRemote) {
					PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("").nextTranslation(this.getTranslationKey() + (tile.falloff ? ".falloffOn" : ".falloffOff")).color(TextFormatting.GOLD).flush(), MainRegistry.proxy.ID_FAN_MODE), (EntityPlayerMP) player);

					com.hbm.sound.VanillaWorldSoundEffects.play(world, x + 0.5, y + 0.5, z + 0.5, "random.click", 0.5F, 0.5F);
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}

	@Override
	public boolean isSealed(World world, int x, int y, int z) {
		return false;
	}
}
