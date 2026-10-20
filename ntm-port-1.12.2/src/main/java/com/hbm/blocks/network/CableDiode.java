package com.hbm.blocks.network;

import api.hbm.block.IToolable;
import api.hbm.energymk2.IEnergyConnectorBlock;
import api.hbm.energymk2.IEnergyConnectorMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.*;

public class CableDiode extends BlockContainer implements IEnergyConnectorBlock, ILookOverlay, IToolable, ITooltipProvider {

	public CableDiode(Material mat) {
		super(mat);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
	}

    public static boolean isNativeModelOwner(net.minecraft.block.Block block) {
        return block != null && block.getClass() == CableDiode.class
                && new ResourceLocation("hbm", "tile.cable_diode").equals(block.getRegistryName());
    }
    @Override public boolean hasCustomBreakingProgress(IBlockState state) { return isNativeModelOwner(this); }
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    @Override protected BlockStateContainer createBlockState() { return new ExtendedBlockState(this, new IProperty<?>[] {METADATA}, new IUnlistedProperty<?>[] {ConnectionState.CONNECTIONS}); }
    @Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(METADATA, metadata & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        // The original renderer mixed AO-lit standard cubes with pre-shaded OBJ arms.
        // Native passes separate those lighting contracts without changing geometry.
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT_MIPPED;
    }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) { return BlockFaceShape.UNDEFINED; }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ((IExtendedBlockState)state).withProperty(ConnectionState.CONNECTIONS, ConnectionState.energy(world, pos));
    }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) { return true; }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        world.setBlockState(pos, getStateFromMeta(EnumFacing.getDirectionFromEntityLiving(pos, player).getIndex()), 2);
    }

	@Override
	public boolean canConnect(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
		return true;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		TileEntityDiode te = (TileEntityDiode)world.getTileEntity(new BlockPos(x, y, z));

		if(world.isRemote)
			return true;

		if(tool == ToolType.SCREWDRIVER) {
			if(te.level < 11)
				te.level++;
			te.markDirty();
			BlockPos pos = new BlockPos(x, y, z);
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			return true;
		}

		if(tool == ToolType.HAND_DRILL) {
			if(te.level > 1)
				te.level--;
			te.markDirty();
			BlockPos pos = new BlockPos(x, y, z);
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			return true;
		}

		if(tool == ToolType.DEFUSER) {
			int p = te.priority.ordinal() + 1;
			if(p > 4) p = 0;
			te.priority = ConnectionPriority.values()[p];
			te.markDirty();
			BlockPos pos = new BlockPos(x, y, z);
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			return true;
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.GOLD + "Limits throughput and restricts flow direction");
		list.add(TextFormatting.YELLOW + "Use screwdriver to increase throughput");
		list.add(TextFormatting.YELLOW + "Use hand drill to decrease throughput");
		list.add(TextFormatting.YELLOW + "Use defuser to change network priority");
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(!(te instanceof TileEntityDiode))
			return;

		TileEntityDiode diode = (TileEntityDiode) te;

		List<String> text = new ArrayList();
		text.add("Max.: " + BobMathUtil.getShortNumber(diode.getMaxPower()) + "HE/t");
		text.add("Priority: " + diode.priority.name());

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDiode();
	}

	public static class TileEntityDiode extends TileEntityLoadedBase implements IEnergyReceiverMK2, net.minecraft.util.ITickable {
        @Override public boolean canRenderBreaking() {
            return getClass() == TileEntityDiode.class && getWorld() != null && isNativeModelOwner(getWorld().getBlockState(getPos()).getBlock());
        }


		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			level = nbt.getInteger("level");
			priority = ConnectionPriority.values()[nbt.getByte("p")];
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("level", level);
			nbt.setByte("p", (byte) this.priority.ordinal());
			return nbt;
		}

		@Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }

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

		int level = 1;

		private ForgeDirection getDir() {
			return ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
		}

		@Override
		public void update() {

			if(!this.world.isRemote) {
				for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

					if(dir == getDir())
						continue;

					this.trySubscribe(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);
				}

				pulses = 0;
				this.setPower(0); //tick is over, reset our allowed transfe
			}
		}

		@Override
		public boolean canConnect(ForgeDirection dir) {
			return dir != getDir();
		}

		/** Used as an intra-tick tracker for how much energy has been transmitted, resets to 0 each tick and maxes out based on transfer */
		private long power;
		private boolean recursionBrake = false;
		private int pulses = 0;
		public ConnectionPriority priority = ConnectionPriority.NORMAL;

		@Override
		public long transferPower(long power) {

			if(recursionBrake)
				return power;

			pulses++;
			if(this.getPower() >= this.getMaxPower() || pulses > 10) return power; //if we have already maxed out transfer or max pulses, abort

			recursionBrake = true;

			ForgeDirection dir = getDir();
			PowerNode node = Nodespace.getNode(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ);
			TileEntity te = Compat.getTileStandard(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ);

			if(node != null && !node.expired && node.hasValidNet() && te instanceof IEnergyConnectorMK2 && ((IEnergyConnectorMK2) te).canConnect(dir.getOpposite())) {
				long toTransfer = Math.min(power, this.getReceiverSpeed());
				long remainder = node.net.sendPowerDiode(toTransfer);
				long transferred = (toTransfer - remainder);
				this.power += transferred;
				power -= transferred;

			} else if(te instanceof IEnergyReceiverMK2 && te != this) {
				IEnergyReceiverMK2 rec = (IEnergyReceiverMK2) te;
				if(rec.canConnect(dir.getOpposite())) {
					long toTransfer = Math.min(power, rec.getReceiverSpeed());
					long remainder = rec.transferPower(toTransfer);
					power -= (toTransfer - remainder);
					recursionBrake = false;
					return power;
				}
			}

			recursionBrake = false;
			return power;
		}

		@Override
		public long getReceiverSpeed() {
			return this.getMaxPower() - this.getPower();
		}

		@Override
		public long getMaxPower() {
			return (long) Math.pow(10, level);
		}

		@Override
		public long getPower() {
			return Math.min(power, this.getMaxPower());
		}

		@Override
		public void setPower(long power) {
			this.power = power;
		}

		@Override
		public ConnectionPriority getPriority() {
			return this.priority;
		}
	}
}
