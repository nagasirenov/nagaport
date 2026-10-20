package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockPlushie.PlushieType;
import com.hbm.blocks.generic.BlockPlushie.TileEntityPlushie;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineEPress;
import com.hbm.inventory.gui.GUIMachineEPress;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.CompatEnergyControl;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.tile.IInfoProviderEC;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineEPress extends TileEntityMachineBase implements IEnergyReceiverMK2, IGUIProvider, IUpgradeInfoProvider, IInfoProviderEC {

	private final com.hbm.tileentity.LegacyPressMetadataUpgrade legacyMetadataUpgrade =
			new com.hbm.tileentity.LegacyPressMetadataUpgrade();

	public void withLegacyPromotion(net.minecraft.block.Block owner, World world,
			net.minecraft.util.math.BlockPos pos, Runnable promotion) {
		legacyMetadataUpgrade.run(this, owner, world, pos, promotion);
	}

	@Override
	public boolean shouldRefresh(World world, net.minecraft.util.math.BlockPos pos,
			net.minecraft.block.state.IBlockState before, net.minecraft.block.state.IBlockState after) {
		if (legacyMetadataUpgrade.consume(this, world, pos, before, after)) return false;
		return super.shouldRefresh(world, pos, before, after);
	}

	public long power = 0;
	public final static long maxPower = 50000;

	public int press;
	public double renderPress;
	public double lastPress;
	private int syncPress;
	private int turnProgress;
	public final static int maxPress = 200;
	boolean isRetracting = false;
	private int delay;

	public ItemStack syncStack;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT();

	public TileEntityMachineEPress() {
		super(5);
	}

	@Override
	public String getDefaultName() {
		return "container.epress";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			// Triggers the legacy monoblock fix
			if (com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()) ) < 12) {
				this.world.scheduleUpdate(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()), com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()).getBlock(), 1);
			}

			this.updateConnections();
			power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			boolean canProcess = this.canProcess();

			if((canProcess || this.isRetracting || this.delay > 0) && power >= 100) {

				power -= 100;

				if(delay <= 0) {

					upgradeManager.checkSlots(this, slots, 4, 4);
					int speed = 1 + upgradeManager.getLevel(UpgradeType.SPEED);

					int stampSpeed = this.isRetracting ? 20 : 45;
					stampSpeed *= (1D + (double) speed / 4D);

					if(this.isRetracting) {
						this.press -= stampSpeed;

						if(this.press <= 0) {
							this.isRetracting = false;
							this.delay = 5 - speed + 1;
						}
					} else if(canProcess) {
						this.press += stampSpeed;

						if(this.press >= maxPress) {
							String squish = "hbm:block.pressOperate";
							TileEntity tile = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ()));
							if(tile instanceof TileEntityPlushie) {
								TileEntityPlushie plushie = (TileEntityPlushie) tile;
								squish = "hbm:block.squeakyPain";

								if(plushie.type == PlushieType.HUNDUN) {
									com.hbm.world.WorldBlockDestruction.destroy(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), false);
									ExplosionVNT vnt = new ExplosionVNT(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, 5);
									vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 50));
									vnt.setPlayerProcessor(new PlayerProcessorStandard());
									vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
									vnt.explode();
								}
							}
							this.world.playSoundEffect(this.pos.getX(), this.pos.getY(), this.pos.getZ(), squish, getVolume(1.5F), 1.0F);
							ItemStack output = PressRecipes.getOutput(slots[2], slots[1]);
							if(slots[3] == null) {
								slots[3] = output.copy();
							} else {
								slots[3].stackSize += output.stackSize;
							}
							this.decrStackSize(2, 1);

							if(slots[1].getMaxDamage() != 0) {
								slots[1].setItemDamage(slots[1].getItemDamage() + 1);
								if(slots[1].getItemDamage() >= slots[1].getMaxDamage()) {
									slots[1] = null;
								}
							}

							this.isRetracting = true;
							this.delay = 5 - speed + 1;

							this.markDirty();
						}
					} else if(this.press > 0){
						this.isRetracting = true;
					}
				} else {
					delay--;
				}
			}

			this.networkPackNT(50);

		} else {

			// approach-based interpolation, GO!
			this.lastPress = this.renderPress;

			if(this.turnProgress > 0) {
				this.renderPress = this.renderPress + ((this.syncPress - this.renderPress) / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.renderPress = this.syncPress;
			}

			TileEntity te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ()));
			if(te instanceof TileEntityPlushie) {
				TileEntityPlushie abuseTarget = (TileEntityPlushie) te;
				abuseTarget.miseryFactor = MathHelper.clamp(((float)syncPress - 100.0F) / 100.0F, (float) (0), (float) (1));
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(press);
		if (slots[2] == null)
			buf.writeShort(-1); // indicate that the NBT doesn't actually exist to avoid null pointer errors.
		else
			BufferUtil.writeNBT(buf, slots[2].getTagCompound());
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.syncPress = buf.readInt();

		NBTTagCompound stack = BufferUtil.readNBT(buf);
		this.syncStack = ItemStack.loadItemStackFromNBT(stack);

		this.turnProgress = 2;
	}

	public boolean canProcess() {
		if(power < 100) return false;
		if(slots[1] == null || slots[2] == null) return false;

		ItemStack output = PressRecipes.getOutput(slots[2], slots[1]);

		if(output == null) return false;

		if(slots[3] == null) return true;
		if(slots[3].stackSize + output.stackSize <= slots[3].getMaxStackSize() && slots[3].getItem() == output.getItem() && slots[3].getItemDamage() == output.getItemDamage()) return true;
		return false;
	}

	private void updateConnections() {

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			this.trySubscribe(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {

		if(stack.getItem() instanceof ItemStamp)
			return i == 1;

		return i == 2;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 1, 2, 3 };
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i == 3;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		press = nbt.getInteger("press");
		power = nbt.getInteger("power");
		isRetracting = nbt.getBoolean("ret");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("press", press);
		nbt.setLong("power", power);
		nbt.setBoolean("ret", isRetracting);
		return nbt;
	}

	@Override
	public void setPower(long i) {
		power = i;

	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	AxisAlignedBB aabb;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(aabb != null)
			return aabb;

		aabb = AxisAlignedBB.getBoundingBox(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + 3, this.pos.getZ() + 1);
		return aabb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineEPress(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineEPress(player.inventory, this);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_epress));
		if(type == UpgradeType.SPEED) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (50 * level / 3) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		return upgrades;
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setInteger(CompatEnergyControl.I_PROGRESS, this.press);
	}
}
