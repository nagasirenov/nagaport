package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.handler.CompatHandler;
import net.minecraftforge.fml.common.Optional;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import org.lwjgl.input.Keyboard;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BobMathUtil;
import net.minecraft.util.math.BlockPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.redstoneoverradio.IRORValueProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public class MachineCapacitor extends BlockContainer implements ILookOverlay, IPersistentInfoProvider, ITooltipProvider {

	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
	@Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
	@Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta); }
	@Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
	@Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
	@Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
	@Override public boolean isOpaqueCube(IBlockState state) { return false; }
	@Override public boolean isFullCube(IBlockState state) { return false; }
	@Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) { return BlockFaceShape.UNDEFINED; }


	protected long power;
	String name;

	public MachineCapacitor(Material mat, long power, String name) {
		super(mat);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
		this.power = power;
		this.name = name;
	}

	public String getModelMaterial() { return name; }

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float fX, float fY, float fZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getStateFromMeta(side.getIndex());
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCapacitor(this.power);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(!(te instanceof TileEntityCapacitor))
			return;

		TileEntityCapacitor battery = (TileEntityCapacitor) te;
		List<String> text = new ArrayList();
		text.add(BobMathUtil.getShortNumber(battery.getPower()) + " / " + BobMathUtil.getShortNumber(battery.getMaxPower()) + "HE");

		double percent = (double) battery.getPower() / (double) battery.getMaxPower();
		int charge = (int) Math.floor(percent * 10_000D);
		int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int)(0xFF * percent) << 8);
		text.add("&[" + color + "&]" + (charge / 100D) + "%");
		text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + "+" + BobMathUtil.getShortNumber(battery.powerReceived) + "HE/t");
		text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + "-" + BobMathUtil.getShortNumber(battery.powerSent) + "HE/t");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.YELLOW + "" + BobMathUtil.getShortNumber(persistentTag.getLong("power")) + "/" + BobMathUtil.getShortNumber(persistentTag.getLong("maxPower")) + "HE");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.GOLD + "Stores up to "+ BobMathUtil.getShortNumber(this.power) + "HE");
		list.add(TextFormatting.GOLD + "Charge speed: "+ BobMathUtil.getShortNumber(this.power / 200) + "HE");
		list.add(TextFormatting.GOLD + "Discharge speed: "+ BobMathUtil.getShortNumber(this.power / 600) + "HE");

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			for(String s : I18nUtil.resolveKeyArray("tile.capacitor.desc")) list.add(TextFormatting.YELLOW + s);
		} else {
			list.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC +"Hold <" +
					TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "LSHIFT" +
					TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "> to display more info");
		}
		if(this == ModBlocks.capacitor_complex) {
			list.add("TaCdSa236-7 N-Boosted Anti Mass core surrounded by");
			list.add("Flashlead antimatter lattice in a BF Stabilization Matrix");
			list.add("subjected to the Ferric Osmiridium-Lutece");
			list.add("ψ(x,t)=Aeiℏ(px−Et) Wavefunction.");
		}

	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if(world instanceof World) drops.addAll(IPersistentNBT.getDrops((World) world, pos.getX(), pos.getY(), pos.getZ(), this));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
		IPersistentNBT.restoreData(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			this.dropBlockAsItem(world, pos, state, 0);
			harvesters.set(null);
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack tool) {
		player.addStat(StatList.getBlockStats(this), 1);
		player.addExhaustion(0.025F);
	}
	@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
	public static class TileEntityCapacitor extends TileEntityLoadedBase implements IEnergyProviderMK2, IEnergyReceiverMK2, IPersistentNBT, IRORValueProvider, SimpleComponent, CompatHandler.OCComponent, net.minecraft.util.ITickable {

		/** Placement metadata does not change the stored energy tile's identity. */
		@Override
		public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
			return oldState.getBlock() != newState.getBlock();
		}

		public long power;
		protected long maxPower;
		public long powerReceived;
		public long powerSent;
		public long lastPowerReceived;
		public long lastPowerSent;

		public TileEntityCapacitor() { }

		public TileEntityCapacitor(long maxPower) {
			this.maxPower = maxPower;
		}

		@Override
		public void update() {

			if(!this.world.isRemote) {

				ForgeDirection opp = ForgeDirection.getOrientation(this.getBlockMetadata());
				ForgeDirection dir = opp.getOpposite();

				BlockPos pos = new BlockPos(this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ);

				boolean didStep = false;
				ForgeDirection last = null;

				while(this.world.getBlockState(pos).getBlock() == ModBlocks.capacitor_bus) {
					ForgeDirection current = ForgeDirection.getOrientation(this.world.getBlockState(pos).getBlock().getMetaFromState(this.world.getBlockState(pos)));
					if(!didStep) last = current;
					didStep = true;

					if(last != current) {
						pos = null;
						break;
					}

					pos = pos.add(current.offsetX, current.offsetY, current.offsetZ);
				}

				if(pos != null && last != null) {
					this.tryUnsubscribe(this.world, pos.getX(), pos.getY(), pos.getZ());
					this.tryProvide(this.world, pos.getX(), pos.getY(), pos.getZ(), last);
				}

				this.trySubscribe(this.world, this.pos.getX() + opp.offsetX, this.pos.getY() + opp.offsetY, this.pos.getZ() + opp.offsetZ, opp);

				networkPackNT(15);

				this.lastPowerSent = powerSent;
				this.lastPowerReceived = powerReceived;
				this.powerSent = 0;
				this.powerReceived = 0;
			}
		}

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeLong(power);
			buf.writeLong(maxPower);
			buf.writeLong(powerReceived);
			buf.writeLong(powerSent);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			power = buf.readLong();
			maxPower = buf.readLong();
			powerReceived = buf.readLong();
			powerSent = buf.readLong();
		}

		@Override
		public long transferPower(long power) {
			if(power + this.getPower() <= this.getMaxPower()) {
				this.setPower(power + this.getPower());
				this.powerReceived += power;
				return 0;
			}
			long capacity = this.getMaxPower() - this.getPower();
			long overshoot = power - capacity;
			this.powerReceived += (this.getMaxPower() - this.getPower());
			this.setPower(this.getMaxPower());
			return overshoot;
		}

		@Override
		public void usePower(long power) {
			this.powerSent += Math.min(this.getPower(), power);
			this.setPower(this.getPower() - power);
		}

		@Override
		public long getPower() {
			return power;
		}

		@Override
		public long getMaxPower() {
			return maxPower;
		}

		@Override public long getProviderSpeed() {
			return this.getMaxPower() / 300;
		}

		@Override public long getReceiverSpeed() {
			return this.getMaxPower() / 100;
		}

		@Override
		public ConnectionPriority getPriority() {
			return ConnectionPriority.LOW;
		}

		@Override
		public void setPower(long power) {
			this.power = power;
		}

		@Override
		public boolean canConnect(ForgeDirection dir) {
			return dir == ForgeDirection.getOrientation(this.getBlockMetadata());
		}

		@Override
		public void writeNBT(NBTTagCompound nbt) {
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("power", power);
			data.setLong("maxPower", maxPower);
			nbt.setTag(NBT_PERSISTENT_KEY, data);
		}

		@Override
		public void readNBT(NBTTagCompound nbt) {
			NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
			this.power = data.getLong("power");
			this.maxPower = data.getLong("maxPower");
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.power = nbt.getLong("power");
			this.maxPower = nbt.getLong("maxPower");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setLong("power", power);
			nbt.setLong("maxPower", maxPower);
			return nbt;
		}

		@Override
		public String[] getFunctionInfo() {
			return new String[] {
					PREFIX_VALUE + "fill",
					PREFIX_VALUE + "fillpercent",
			};
		}

		@Override
		public String provideRORValue(String name) {
			if((PREFIX_VALUE + "fill").equals(name))		return "" + this.power;
			if((PREFIX_VALUE + "fillpercent").equals(name))	return "" + this.power * 100 / this.maxPower;
			return null;
		}

		// opencomputer
		@Override
		@Optional.Method(modid = "OpenComputers")
		public String getComponentName() {
			return "capacitor";
		}

		@Callback(direct = true)
		@Optional.Method(modid = "OpenComputers")
		public Object[] getEnergy(Context context, Arguments args) {
			return new Object[] {power};
		}

		@Callback(direct = true)
		@Optional.Method(modid = "OpenComputers")
		public Object[] getMaxEnergy(Context context, Arguments args) {
			return new Object[] {maxPower};
		}

		@Callback(direct = true)
		@Optional.Method(modid = "OpenComputers")
		public Object[] getEnergySent(Context context, Arguments args) {
			return new Object[] {lastPowerReceived};
		}

		@Callback(direct = true)
		@Optional.Method(modid = "OpenComputers")
		public Object[] getEnergyReceived(Context context, Arguments args) { return new Object[] {lastPowerSent}; }

		@Callback(direct = true)
		@Optional.Method(modid = "OpenComputers")
		public Object[] getInfo(Context context, Arguments args) {
			return new Object[] {power, maxPower, lastPowerReceived, lastPowerSent};
		}

		@Override
		@Optional.Method(modid = "OpenComputers")
		public String[] methods() {
			return new String[] {
				"getEnergy",
				"getMaxEnergy",
				"getEnergySent",
				"getEnergyReceived",
				"getInfo"
			};
		}
		@Override
		@Optional.Method(modid = "OpenComputers")
		public Object[] invoke(String method, Context context, Arguments args) throws Exception {
			switch(method) {
				case ("getEnergy"):
					return getEnergy(context, args);
				case ("getMaxEnergy"):
					return getMaxEnergy(context, args);
				case ("getEnergySent"):
					return getEnergySent(context, args);
				case ("getEnergyReceived"):
					return getEnergyReceived(context, args);
				case ("getInfo"):
					return getEnergyReceived(context, args);
			}
			throw new NoSuchMethodException();
		}
	}
}
