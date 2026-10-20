package com.hbm.tileentity.machine;

import static com.hbm.inventory.OreDictManager.KEY_COBBLESTONE;
import static com.hbm.inventory.OreDictManager.KEY_SAND;
import static com.hbm.inventory.OreDictManager.KEY_STONE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOreFluid;
import com.hbm.dim.SolarSystem;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.container.ContainerMiningLaser;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMiningLaser;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.InventoryUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardSender;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineMiningLaser extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardSender, IGUIProvider, IUpgradeInfoProvider {

	public long power;
	public int age = 0;
	public static final long maxPower = 100000000;
	public static final int consumption = 10000;
	public FluidTank tank;

	public boolean isOn;
	private boolean redstonePowered;
	public int targetX;
	public int targetY;
	public int targetZ;
	public int lastTargetX;
	public int lastTargetY;
	public int lastTargetZ;
	public boolean beam;
	boolean lock = false;
	double breakProgress;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT();

	public TileEntityMachineMiningLaser() {

		//slot 0: battery
		//slots 1 - 8: upgrades
		//slots 9 - 29: output
		super(30);
		tank = new FluidTank(Fluids.OIL, 64_000);
	}

	@Override
	public String getDefaultName() {
		return "container.miningLaser";
	}

	private double clientBreakProgress;

	private DirPos[] getConPos() {
		return new DirPos[] {
			new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ(), Library.POS_X),
			new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ(), Library.NEG_X),
			new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
			new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z),
		};
	}

	private boolean isMultiblockRedstonePowered() {
		for (DirPos conPos : getConPos()) {
			BlockPos pos = conPos.offset(conPos.getDir().getOpposite());
			if(this.world.isBlockIndirectlyGettingPowered(pos.getX(), pos.getY(), pos.getZ())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			this.updateConnections();

			for (DirPos pos : getConPos()) {
				this.sendFluid(tank, this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}

			power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			//reset progress if the position changes
			if(lastTargetX != targetX ||
					lastTargetY != targetY ||
					lastTargetZ != targetZ)
				breakProgress = 0;

			//set last positions for interpolation and the like
			lastTargetX = targetX;
			lastTargetY = targetY;
			lastTargetZ = targetZ;

			boolean prevRedstone = this.redstonePowered;
			this.redstonePowered = this.isMultiblockRedstonePowered();

			if(prevRedstone != this.redstonePowered) {
				this.markDirty();
			}

			boolean shouldRun = this.isOn && !this.redstonePowered;

			if(shouldRun) {

				upgradeManager.checkSlots(this, slots, 1, 8);
				int cycles = 1 + upgradeManager.getLevel(UpgradeType.OVERDRIVE);
				int speed = 1 + upgradeManager.getLevel(UpgradeType.SPEED);
				int range = 1 + upgradeManager.getLevel(UpgradeType.EFFECT) * 2;
				int fortune = upgradeManager.getLevel(UpgradeType.FORTUNE);
				int consumption = this.consumption
						- (this.consumption * upgradeManager.getLevel(UpgradeType.POWER) / 16)
						+ (this.consumption * upgradeManager.getLevel(UpgradeType.SPEED) / 16);

				for(int i = 0; i < cycles; i++) {

					if(power < consumption) {
						beam = false;
						break;
					}

					power -= consumption;

					if(targetY <= 0)
						targetY = this.pos.getY() - 2;

					scan(range);


					Block block = com.hbm.world.WorldBlockState.read(this.world, targetX, targetY, targetZ).getBlock();

					if(block.getDefaultState().getMaterial().isLiquid()) {
						com.hbm.world.WorldAirRemoval.remove(this.world, targetX, targetY, targetZ);
						buildDam();
						continue;
					}

					if(beam && canBreak(block, targetX, targetY, targetZ)) {

						breakProgress += getBreakSpeed(speed);
						clientBreakProgress = Math.min(breakProgress, 1);

						if(breakProgress < 1) {
							this.world.destroyBlockInWorldPartially(-1, targetX, targetY, targetZ, (int) Math.floor(breakProgress * 10));
						} else {
							breakBlock(fortune);
							buildDam();
						}
					}
				}
			} else {
				targetY = this.pos.getY() - 2;
				beam = false;
			}

			for (DirPos pos : getConPos()) {
				this.tryFillContainer(pos.getX(), pos.getY(), pos.getZ());
			}

			this.networkPackNT(250);
		}
	}

	private void updateConnections() {
		this.trySubscribe(this.world, this.pos.getX(), this.pos.getY() + 2, this.pos.getZ(), ForgeDirection.UP);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		buf.writeInt(this.lastTargetX);
		buf.writeInt(this.lastTargetY);
		buf.writeInt(this.lastTargetZ);
		buf.writeInt(this.targetX);
		buf.writeInt(this.targetY);
		buf.writeInt(this.targetZ);
		buf.writeBoolean(this.beam);
		buf.writeBoolean(this.isOn);
		buf.writeDouble(this.clientBreakProgress);
		tank.serialize(buf);
		buf.writeBoolean(this.redstonePowered);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.lastTargetX = buf.readInt();
		this.lastTargetY = buf.readInt();
		this.lastTargetZ = buf.readInt();
		this.targetX = buf.readInt();
		this.targetY = buf.readInt();
		this.targetZ = buf.readInt();
		this.beam = buf.readBoolean();
		this.isOn = buf.readBoolean();
		this.breakProgress = buf.readDouble();
		tank.deserialize(buf);
		this.redstonePowered = buf.readBoolean();
	}

	private void buildDam() {

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if(com.hbm.world.WorldBlockState.read(this.world, targetX + dir.offsetX, targetY + dir.offsetY, targetZ + dir.offsetZ).getBlock().getDefaultState().getMaterial().isLiquid()) this.world.setBlock(targetX + dir.offsetX, targetY + dir.offsetY, targetZ + dir.offsetZ, ModBlocks.barricade);
		}
	}

	private void tryFillContainer(int x, int y, int z) {

		Block b = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();
		if(b != Blocks.CHEST && b != Blocks.TRAPPED_CHEST && b != ModBlocks.crate_iron && b != ModBlocks.crate_desh &&
				b != ModBlocks.crate_steel && b != ModBlocks.safe && b != Blocks.HOPPER)
			return;

		IInventory inventory = (IInventory)this.world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		if(inventory == null)
			return;

		for(int i = 9; i <= 29; i++) {

			if(slots[i] != null) {
				int prev = slots[i].stackSize;
				slots[i] = InventoryUtil.tryAddItemToInventory(inventory, 0, inventory.getSizeInventory() - 1, slots[i]);

				if(slots[i] == null || slots[i].stackSize < prev)
					return;
			}
		}
	}

	private void breakBlock(int fortune) {

		Block b = com.hbm.world.WorldBlockState.read(this.world, targetX, targetY, targetZ).getBlock();
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, targetX, targetY, targetZ) );
		boolean normal = true;
		boolean doesBreak = true;

		if(b == Blocks.LIT_REDSTONE_ORE)
			b = Blocks.REDSTONE_ORE;

		ItemStack stack = new ItemStack(b, 1, meta);

		if(stack != null && stack.getItem() != null) {
			if(hasCrystallizer()) {

				CrystallizerRecipe result = CrystallizerRecipes.getOutput(stack, Fluids.PEROXIDE);
				if(result == null) result = CrystallizerRecipes.getOutput(stack, Fluids.SULFURIC_ACID);

				if(result != null) {
					this.world.spawnEntity(new EntityItem(this.world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, result.output.copy()));
					normal = false;
				}

			} else if(hasCentrifuge()) {

				ItemStack[] result = CentrifugeRecipes.getOutput(stack);
				if(result != null) {
					for(ItemStack sta : result) {

						if(sta != null) {
							this.world.spawnEntity(new EntityItem(this.world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, sta.copy()));
							normal = false;
						}
					}
				}

			} else if(hasShredder()) {

				ItemStack result = ShredderRecipes.getShredderResult(stack);
				if(result != null && result.getItem() != ModItems.scrap) {
					this.world.spawnEntity(new EntityItem(this.world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, result.copy()));
					normal = false;
				}

			} else if(hasSmelter()) {

				ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(stack);
				if(result != null) {
					this.world.spawnEntity(new EntityItem(this.world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, result.copy()));
					normal = false;
				}
			}
		}

		if(doesBreak) {
			if(normal) b.dropBlockAsItem(this.world, targetX, targetY, targetZ, meta, fortune);
			this.world.func_147480_a(targetX, targetY, targetZ, false);
		}

		suckDrops();

		if(doesScream()) {
			com.hbm.sound.WorldSoundEffects.play(this.world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, "hbm:block.screm", 2000.0F, 1.0F);
		}

		breakProgress = 0;
	}

	private static final List<AStack> bad = Arrays.asList(new AStack[] {
		new ComparableStack(Blocks.DIRT),
		new OreDictStack(KEY_STONE),
		new OreDictStack(KEY_COBBLESTONE),
		new OreDictStack(KEY_SAND),
		new ComparableStack(Blocks.SANDSTONE),
		new ComparableStack(Blocks.GRAVEL),
		new ComparableStack(ModBlocks.basalt),
		new ComparableStack(ModBlocks.stone_gneiss),
		new ComparableStack(Items.FLINT),
		new ComparableStack(Items.SNOWBALL),
		new ComparableStack(Items.WHEAT_SEEDS),
		new ComparableStack(ModBlocks.tektite),
	});

	private boolean isBad(ItemStack stack) {
		for(AStack mj : bad) {
			if(mj.matchesRecipe(stack, true)) return true;
		}

		return false;
	}

	//hahahahahahahaha he said "suck"
	private void suckDrops() {

		int rangeHor = 3;
		int rangeVer = 1;
		boolean nullifier = hasNullifier();

		List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(
				targetX + 0.5 - rangeHor,
				targetY + 0.5 - rangeVer,
				targetZ + 0.5 - rangeHor,
				targetX + 0.5 + rangeHor,
				targetY + 0.5 + rangeVer,
				targetZ + 0.5 + rangeHor
				));

		for(EntityItem entityItem : items) {

			if(entityItem.isDead) continue;

			if(nullifier && isBad(entityItem.getEntityItem())) {
				entityItem.setDead();
				entityItem.delayBeforeCanPickup = 60;
				continue;
			}

			Item item = entityItem.getEntityItem().getItem();
			if(item instanceof ItemBlock) {
				Block block = ((ItemBlock) item).field_150939_a;

				if(block instanceof BlockOreFluid) {
					int meta = entityItem.getEntityItem().getItemDamage();

					BlockOreFluid oreFluid = (BlockOreFluid) block;

					int toFill = oreFluid.getBlockFluidAmount(meta);

					if(toFill > 0) {
						tank.setTankType(oreFluid.getPrimaryFluid(meta));
						tank.setFill(Math.min(tank.getFill() + toFill, tank.getMaxFill()));
					}

					entityItem.setDead();
					entityItem.delayBeforeCanPickup = 60;
					continue;
				}
			}

			ItemStack stack = InventoryUtil.tryAddItemToInventory(slots, 9, 29, entityItem.getEntityItem().copy());

			if(stack == null) {
				entityItem.setDead();
				entityItem.delayBeforeCanPickup = 60;
			} else {
				entityItem.setEntityItemStack(stack.copy()); //copy is not necessary but i'm paranoid due to the kerfuffle of the old drill
			}
		}

		List<EntityLivingBase> mobs = this.world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(
				targetX + 0.5 - 1,
				targetY + 0.5 - 1,
				targetZ + 0.5 - 1,
				targetX + 0.5 + 1,
				targetY + 0.5 + 1,
				targetZ + 0.5 + 1
				));

		for(EntityLivingBase mob : mobs) {
			mob.setFire(5);
		}
	}

	public double getBreakSpeed(int speed) {

		float hardness = com.hbm.world.WorldBlockState.read(this.world, targetX, targetY, targetZ).getBlock().getBlockHardness(this.world, targetX, targetY, targetZ) * 15 / speed;

		if(hardness == 0)
			return 1;

		return 1 / hardness;
	}

	public void scan(int range) {

		for(int x = -range; x <= range; x++) {
			for(int z = -range; z <= range; z++) {

				if(com.hbm.world.WorldBlockState.read(this.world, x + this.pos.getX(), targetY, z + this.pos.getZ()).getBlock().getDefaultState().getMaterial().isLiquid()) {
					continue;
				}

				if(canBreak(com.hbm.world.WorldBlockState.read(this.world, x + this.pos.getX(), targetY, z + this.pos.getZ()).getBlock(), x + this.pos.getX(), targetY, z + this.pos.getZ())) {
					targetX = x + this.pos.getX();
					targetZ = z + this.pos.getZ();
					beam = true;
					return;
				}
			}
		}

		beam = false;
		targetY--;
	}

	private boolean canBreak(Block block, int x, int y, int z) {
		return !block.isAir(this.world, x, y, z) && block.getDefaultState().getMaterial() != ModBlocks.materialGas && block.getBlockHardness(this.world, x, y, z) >= 0 && !block.getDefaultState().getMaterial().isLiquid() && block != Blocks.BEDROCK;
	}

	public int getRange() {
		int range = 1;
		for(int i = 1; i < 9; i++) {
			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.upgrade_effect_1) range += 2;
				else if(slots[i].getItem() == ModItems.upgrade_effect_2) range += 4;
				else if(slots[i].getItem() == ModItems.upgrade_effect_3) range += 6;
			}
		}
		return Math.min(range, 25);
	}

	public boolean hasNullifier() {
		for(int i = 1; i < 9; i++) {
			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.upgrade_nullifier) return true;
			}
		}
		return false;
	}

	public boolean hasSmelter() {
		for(int i = 1; i < 9; i++) {
			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.upgrade_smelter) return true;
			}
		}
		return false;
	}

	public boolean hasShredder() {
		for(int i = 1; i < 9; i++) {
			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.upgrade_shredder) return true;
			}
		}
		return false;
	}

	public boolean hasCentrifuge() {
		for(int i = 1; i < 9; i++) {
			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.upgrade_centrifuge) return true;
			}
		}
		return false;
	}

	public boolean hasCrystallizer() {
		for(int i = 1; i < 9; i++) {
			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.upgrade_crystallizer) return true;
			}
		}
		return false;
	}

	public boolean doesScream() {
		for(int i = 1; i < 9; i++) {
			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.upgrade_screm) return true;
			}
		}
		return false;
	}

	public int getConsumption() {
		return this.consumption;
	}

	public int getWidth() {

		return 1 + getRange() * 2;
	}

	public int getPowerScaled(int i) {
		return (int)((power * i) / maxPower);
	}

	public int getProgressScaled(int i) {
		return (int) (breakProgress * i);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i >= 9 && i <= 29;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int slot = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

		int[] slots = new int[21];

		for(int i = 0; i < 21; i++) {
			slots[i] = i + 9;
		}

		return slots;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		super.setInventorySlotContents(i, stack);

		if(stack != null && i >= 1 && i <= 8 && stack.getItem() instanceof ItemMachineUpgrade)
			this.world.playSoundEffect(this.pos.getX() + 0.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5, "hbm:item.upgradePlug", 1.0F, 1.0F);
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

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		tank.readFromNBT(nbt, "oil");
		power = nbt.getLong("power");
		isOn = nbt.getBoolean("isOn");
		redstonePowered = false;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		tank.writeToNBT(nbt, "oil");
		nbt.setLong("power", power);
		nbt.setBoolean("isOn", isOn);
		return nbt;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMiningLaser(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMiningLaser(player.inventory, this);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE || type == UpgradeType.EFFECT || type == UpgradeType.FORTUNE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_mining_laser));
		if(type == UpgradeType.SPEED) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (100 - 100 / (level + 1)) + "%"));
			info.add(TextFormatting.RED + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "+" + (100 * level / 16) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "-" + (100 * level / 16) + "%"));
		}
		if(type == UpgradeType.EFFECT) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_RANGE, "+" + (2 * level) + "m"));
		}
		if(type == UpgradeType.FORTUNE) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_FORTUNE, "+" + level));
		}
		if(type == UpgradeType.OVERDRIVE) {
			info.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_GRAY) + "YES");
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 12);
		upgrades.put(UpgradeType.POWER, 12);
		upgrades.put(UpgradeType.EFFECT, 12);
		upgrades.put(UpgradeType.FORTUNE, 3);
		upgrades.put(UpgradeType.OVERDRIVE, 9);
		return upgrades;
	}
}
