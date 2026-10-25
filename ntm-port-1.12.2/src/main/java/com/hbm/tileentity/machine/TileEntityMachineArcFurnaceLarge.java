package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineArcFurnaceLarge;
import com.hbm.inventory.gui.GUIMachineArcFurnaceLarge;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.inventory.recipes.ArcFurnaceRecipes.ArcFurnaceRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemArcElectrode;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.Vec3NT;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineArcFurnaceLarge extends TileEntityMachineBase implements IEnergyReceiverMK2, IControlReceiver, IGUIProvider, IUpgradeInfoProvider {

	public long power;
	public static final long maxPower = 2_500_000;
	public boolean liquidMode = false;
	public float progress;
	public boolean isProgressing;
	public boolean hasMaterial;
	public int delay;
	public int upgrade;

	public float lid;
	public float prevLid;
	public int approachNum;
	public float syncLid;

	private AudioWrapper audioLid;
	private AudioWrapper audioProgress;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT();

	public byte[] electrodes = new byte[3];
	public static final byte ELECTRODE_NONE = 0;
	public static final byte ELECTRODE_FRESH = 1;
	public static final byte ELECTRODE_USED = 2;
	public static final byte ELECTRODE_DEPLETED = 3;

	public int getMaxInputSize() {
		return upgrade == 0 ? 1 : upgrade == 1 ? 4 : upgrade == 2 ? 8 : 16;
	}

	public static final int maxLiquid = MaterialShapes.BLOCK.q(128);
	public List<MaterialStack> liquids = new ArrayList();

	public TileEntityMachineArcFurnaceLarge() {
		super(30);
	}

	@Override
	public String getDefaultName() {
		return "container.machineArcFurnaceLarge";
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		super.setInventorySlotContents(i, stack);

		if(stack != null && stack.getItem() instanceof ItemMachineUpgrade && i == 4) {
			this.world.playSound(null, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, com.hbm.main.ModSoundEvents.resolve("hbm:item.upgradePlug"), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Override
	public void update() {

		upgradeManager.checkSlots(this, slots, 4, 4);
		this.upgrade = upgradeManager.getLevel(UpgradeType.SPEED);

		if(!this.world.isRemote) {

			this.power = Library.chargeTEFromItems(slots, 3, power, maxPower);
			this.isProgressing = false;

			for(DirPos pos : getConPos()) this.trySubscribe(this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());

			if(lid > 0) loadIngredients();

			if(power > 0) {

				boolean ingredients = this.hasIngredients();
				boolean electrodes = this.hasElectrodes();

				int consumption = (int) (1_000 * Math.pow(5, upgrade));

				if(ingredients && electrodes && delay <= 0 && this.liquids.isEmpty()) {
					if(lid > 0) {
						lid -= 1F / (60F / (upgrade * 0.5 + 1));
						if(lid < 0) lid = 0;
						this.progress = 0;
					} else {

						if(power >= consumption) {
							int duration = 400 / (upgrade * 2 + 1);
							this.progress += 1F / duration;
							this.isProgressing = true;
							this.power -= consumption;
							if(this.progress >= 1F) {
								this.process();
								this.progress = 0;
								this.markDirty();
								this.delay = (int) (120 / (upgrade * 0.5 + 1));
								PollutionHandler.incrementPollution(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), PollutionType.SOOT, 10F);
							}
						}
					}
				} else {
					if(this.delay > 0) delay--;
					this.progress = 0;
					if(lid < 1) {
						lid += 1F / (60F / (upgrade * 0.5 + 1));
						if(lid > 1) lid = 1;
					}
				}

				hasMaterial = ingredients;
			}

			this.decideElectrodeState();

			if(!hasMaterial) hasMaterial = this.hasIngredients();

			if(!this.liquids.isEmpty() && this.lid > 0F) {

				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);

				Vec3NT impact = new Vec3NT(0, 0, 0);
				MaterialStack didPour = CrucibleUtil.pourFullStack(this.world, this.pos.getX() + 0.5D + dir.offsetX * 2.875D, this.pos.getY() + 1.25D, this.pos.getZ() + 0.5D + dir.offsetZ * 2.875D, 6, true, this.liquids, MaterialShapes.INGOT.q(1), impact);

				if(didPour != null) {
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "foundry");
					data.setInteger("color", didPour.material.moltenColor);
					data.setByte("dir", (byte) dir.ordinal());
					data.setFloat("off", 0.625F);
					data.setFloat("base", 0.625F);
					data.setFloat("len", Math.max(1F, this.pos.getY() + 1 - (float) (Math.ceil(impact.y) - 0.875)));
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, this.pos.getX() + 0.5D + dir.offsetX * 2.875D, this.pos.getY() + 1, this.pos.getZ() + 0.5D + dir.offsetZ * 2.875D), new TargetPoint(this.world.provider.getDimension(), this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 50));
				}
			}

			this.liquids.removeIf(o -> o.amount <= 0);

			this.networkPackNT(150);
		} else {

			this.prevLid = this.lid;

			if(this.approachNum > 0) {
				this.lid = this.lid + ((this.syncLid - this.lid) / (float) this.approachNum);
				--this.approachNum;
			} else {
				this.lid = this.syncLid;
			}

			if(this.lid != this.prevLid) {
				if(this.audioLid == null || !this.audioLid.isPlaying()) {
					this.audioLid = MainRegistry.proxy.getLoopedSound("hbm:door.wgh_start", this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.getVolume(0.75F), 15F, 1.0F, 5);
					this.audioLid.startSound();
				}
				this.audioLid.keepAlive();
			} else {
				if(this.audioLid != null) {
					this.audioLid.stopSound();
					this.audioLid = null;
				}
			}

			if((lid == 1 || lid == 0) && lid != prevLid && !(this.prevLid == 0 && this.lid == 1)) {
				MainRegistry.proxy.playSoundClient(this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:door.wgh_stop", this.getVolume(1), 1F);
			}

			if(this.isProgressing) {
				if(this.audioProgress == null || !this.audioProgress.isPlaying()) {
					this.audioProgress = MainRegistry.proxy.getLoopedSound("hbm:block.electricHum", this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.getVolume(1.5F), 15F, 0.75F, 5);
					this.audioProgress.startSound();
				}
				this.audioProgress.updatePitch(0.75F);
				this.audioProgress.keepAlive();
			} else {
				if(this.audioProgress != null) {
					this.audioProgress.stopSound();
					this.audioProgress = null;
				}
			}

			if(this.lid != this.prevLid && this.lid > this.prevLid && !(this.prevLid == 0 && this.lid == 1) && MainRegistry.proxy.me().getDistance(this.pos.getX() + 0.5, this.pos.getY() + 4, this.pos.getZ() + 0.5) < 50) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "tower");
				data.setFloat("lift", 0.01F);
				data.setFloat("base", 0.5F);
				data.setFloat("max", 2F);
				data.setInteger("life", 70 + this.world.rand.nextInt(30));
				data.setDouble("posX", this.pos.getX() + 0.5 + this.world.rand.nextGaussian() * 0.5);
				data.setDouble("posZ", this.pos.getZ() + 0.5 + this.world.rand.nextGaussian() * 0.5);
				data.setDouble("posY", this.pos.getY() + 4);
				data.setBoolean("noWind", true);
				data.setFloat("alphaMod", prevLid / lid);
				data.setInteger("color", 0x000000);
				data.setFloat("strafe", 0.05F);
				for(int i = 0; i < 3; i++) MainRegistry.proxy.effectNT(data);
			}

			if(this.lid != this.prevLid && this.lid < this.prevLid && this.lid > 0.5F && this.hasMaterial && MainRegistry.proxy.me().getDistance(this.pos.getX() + 0.5, this.pos.getY() + 4, this.pos.getZ() + 0.5) < 50) {

				if(this.world.rand.nextInt(5) == 0) {
					NBTTagCompound flame = new NBTTagCompound();
					flame.setString("type", "rbmkflame");
					flame.setDouble("posX", this.pos.getX() + 0.5 + this.world.rand.nextGaussian() * 0.5);
					flame.setDouble("posZ", this.pos.getZ() + 0.5 + this.world.rand.nextGaussian() * 0.5);
					flame.setDouble("posY", this.pos.getY() + 2.75);
					flame.setInteger("maxAge", 50);
					for(int i = 0; i < 2; i++) MainRegistry.proxy.effectNT(flame);
				}
			}
		}
	}

	/** Moves items from the input queue to the main grid */
	public void loadIngredients() {

		boolean markDirty = false;

		for(int q /* queue */ = 25; q < 30; q++) {
			if(slots[q] == null) continue;
			ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(slots[q], this.liquidMode);
			if(recipe == null) continue;
			int max = this.getMaxInputSize();
			int recipeMax = this.liquidMode ? max : slots[q].getMaxStackSize() / recipe.solidOutput.getCount();
			max = Math.min(max, recipeMax);

			// add to existing stacks
			for(int i /* ingredient */ = 5; i < 25; i++) {
				if(slots[i] == null) continue;
				if(!slots[q].isItemEqual(slots[i]) || !ItemStack.areItemStackTagsEqual(slots[q], slots[i])) continue;
				int toMove = BobMathUtil.min(slots[i].getMaxStackSize() - slots[i].getCount(), slots[q].getCount(), max - slots[i].getCount());
				if(toMove > 0) {
					this.decrStackSize(q, toMove);
					slots[i].grow(toMove);
					markDirty = true;
				}
				if(slots[q] == null) break;
			}

			// add to empty slot
			if(slots[q] != null) for(int i /* ingredient */ = 5; i < 25; i++) {
				if(slots[i] != null) continue;
				int toMove = Math.min(max, slots[q].getCount());
				slots[i] = slots[q].copy();
				slots[i].setCount(toMove);
				this.decrStackSize(q, toMove);
				markDirty = true;
				if(slots[q] == null) break;
			}
		}

		if(markDirty) this.markDirty();
	}

	public void decideElectrodeState() {
		for(int i = 0; i < 3; i++) {

			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.arc_electrode_burnt) { this.electrodes[i] = this.ELECTRODE_DEPLETED; continue; }
				if(slots[i].getItem() == ModItems.arc_electrode) {
					if(this.isProgressing || ItemArcElectrode.getDurability(slots[i]) > 0) this.electrodes[i] = this.ELECTRODE_USED;
					else this.electrodes[i] = this.ELECTRODE_FRESH;
					continue;
				}
			}
			this.electrodes[i] = this.ELECTRODE_NONE;
		}
	}

	public void process() {

		for(int i = 5; i < 25; i++) {
			if(slots[i] == null) continue;
			ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(slots[i], this.liquidMode);
			if(recipe == null) continue;

			if(!liquidMode && recipe.solidOutput != null) {
				int amount = slots[i].getCount();
				slots[i] = recipe.solidOutput.copy();
				slots[i].setCount(slots[i].getCount() * (amount));
			}

			if(liquidMode && recipe.fluidOutput != null) {

				while(slots[i] != null && slots[i].getCount() > 0) {
					int liquid = this.getStackAmount(liquids);
					int toAdd = this.getStackAmount(recipe.fluidOutput);

					if(liquid + toAdd <= this.maxLiquid) {
						this.decrStackSize(i, 1);
						for(MaterialStack stack : recipe.fluidOutput) {
							this.addToStack(stack);
						}
					} else {
						break;
					}
				}
			}
		}

		for(int i = 0; i < 3; i++) {
			if(ItemArcElectrode.damage(slots[i])) {
				slots[i] = new ItemStack(ModItems.arc_electrode_burnt, 1, slots[i].getItemDamage());
			}
		}
	}

	public boolean hasIngredients() {

		for(int i = 5; i < 25; i++) {
			if(slots[i] == null) continue;
			ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(slots[i], this.liquidMode);
			if(recipe == null) continue;
			if(liquidMode && recipe.fluidOutput != null) return true;
			if(!liquidMode && recipe.solidOutput != null) return true;
		}

		return false;
	}

	public boolean hasElectrodes() {
		for(int i = 0; i < 3; i++) {
			if(slots[i] == null || slots[i].getItem() != ModItems.arc_electrode) return false;
		}
		return true;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] {
				0, 1, 2,
				5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
				25, 26, 27, 28, 29};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		if(slot < 3) return stack.getItem() == ModItems.arc_electrode;
		if(slot >= 25) {
			ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(stack, this.liquidMode);
			if(recipe == null) return false;
			return true;
		}
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot < 3) return stack.getItem() == ModItems.arc_electrode;
		if(slot > 4) {
			ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(stack, this.liquidMode);
			if(recipe == null) return false;
			if(liquidMode) {
				return recipe.fluidOutput != null;
			} else {
				return recipe.solidOutput != null;
			}
		}
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		if(slot < 3) return lid >= 1 && stack.getItem() != ModItems.arc_electrode;
		if(slot > 4 && slot < 25) return lid > 0 && ArcFurnaceRecipes.getOutput(stack, this.liquidMode) == null;
		if(slot >= 25) return ArcFurnaceRecipes.getOutput(stack, this.liquidMode) == null;
		return false;
	}

	public void addToStack(MaterialStack matStack) {

		for(MaterialStack mat : liquids) {
			if(mat.material == matStack.material) {
				mat.amount += matStack.amount;
				return;
			}
		}

		liquids.add(matStack.copy());
	}

	//Returns what is unused, or null if used up
	public ItemStack distributeInput(ItemStack is, boolean modulate) {
		if(is.getCount() == 0) return null;
		ItemStack split;

		//Slots 0,1,2
		if(is.getItem() == ModItems.arc_electrode) {
			for(int i = 0; i < 3; i++) {
				if(slots[i] == null || slots[i].isEmpty()) {
					split = is.splitStack(1);
					if(modulate) this.setInventorySlotContents(i, split);
				}
				if (is.getCount() == 0) return null;
			}
			//Don't tell me you're gonna add an arc furnace recipe smelting electrodes
			return is;
		}

		//Slots 5-24
		ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(is, this.liquidMode);
		if(recipe != null) {
			int maxStackSize = this.liquidMode ? 64 : recipe.solidOutput.getMaxStackSize() / recipe.solidOutput.getCount();
			maxStackSize = Math.min(maxStackSize, Math.min(is.getMaxStackSize(), getMaxInputSize()));

			//Scan
			for(int i = 5; i < 25; i++){
				if(slots[i] == null || slots[i].isEmpty()) {
					if(is.getCount() > maxStackSize) {
						split = is.splitStack(maxStackSize);
						if(modulate) slots[i] = split;
					} else {
						if(modulate) slots[i] = is;
						return null;
					}
				} else if(ItemStackUtil.areStacksCompatible(is, slots[i]) && slots[i].getCount() < maxStackSize) {
					if(is.getCount() > maxStackSize - slots[i].getCount()) {
						is.splitStack(maxStackSize - slots[i].getCount());
						if(modulate) slots[i].setCount(maxStackSize);
					} else {
						if(modulate) slots[i].grow(is.getCount());
						return null;
					}
				}
			}
		}
		return is;
	}

	//Returns requested ItemStack
	public ItemStack collectRequested(ItemStack is, boolean modulate) {
		int req = is.getCount();
		if(req == 0) return null;

		//Slots 0,1,2
		if(is.getItem() != ModItems.arc_electrode) {
			for(int i = 0; i < 3; i++) {
				if(slots[i] == null || slots[i].isEmpty()) continue;
				if(ItemStackUtil.areStacksCompatible(is, slots[i])) {
					if(req > slots[i].getCount()) {
						req -= slots[i].getCount();
						if(modulate) slots[i] = null;
					} else if(req < slots[i].getCount()) {
						if(modulate) slots[i].shrink(req);
						return is;
					} else {
						if(modulate) slots[i] = null;
						return is;
					}
				}
			}
		}

		//Slots 5-24
		if(ArcFurnaceRecipes.getOutput(is, this.liquidMode) == null) {
			for(int i = 5; i < 25; i++) {
				if(slots[i] == null || slots[i].isEmpty()) continue;
				if(ItemStackUtil.areStacksCompatible(is, slots[i])) {
					if(req > slots[i].getCount()) {
						req -= slots[i].getCount();
						if(modulate) slots[i] = null;
					} else if(req < slots[i].getCount()) {
						if(modulate) slots[i].shrink(req);
						return is;
					} else {
						if(modulate) slots[i] = null;
						return is;
					}
				}
			}
		}

		is.shrink(req);
		if(is.getCount() == 0) return null;
		return is;
	}

	//Return ItemStack in slot, null if unavailable
	public ItemStack getAvailableItemFromSlot(int slot) {
		if(slots[slot] == null || slots[slot].isEmpty()) return null;
		if(slot < 3 && slots[slot].getItem() == ModItems.arc_electrode) return null;
		else if(slot > 4 && ArcFurnaceRecipes.getOutput(slots[slot], this.liquidMode) != null) return null;
		else if(slot == 3 || slot == 4) return null;
		else return slots[slot];
	}

	public static int getStackAmount(List<MaterialStack> stack) {
		int amount = 0;
		for(MaterialStack mat : stack) amount += mat.amount;
		return amount;
	}

	public static int getStackAmount(MaterialStack[] stack) {
		int amount = 0;
		for(MaterialStack mat : stack) amount += mat.amount;
		return amount;
	}

	protected DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(this.pos.getX() + dir.offsetX * 3 + rot.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 3 + rot.offsetZ, dir),
				new DirPos(this.pos.getX() + dir.offsetX * 3 - rot.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 3 - rot.offsetZ, dir),
				new DirPos(this.pos.getX() + rot.offsetX * 3 + dir.offsetX, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 3 + dir.offsetZ, rot),
				new DirPos(this.pos.getX() + rot.offsetX * 3 - dir.offsetX, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 3 - dir.offsetZ, rot),
				new DirPos(this.pos.getX() - rot.offsetX * 3 + dir.offsetX, this.pos.getY(), this.pos.getZ() - rot.offsetZ * 3 + dir.offsetZ, rot.getOpposite()),
				new DirPos(this.pos.getX() - rot.offsetX * 3 - dir.offsetX, this.pos.getY(), this.pos.getZ() - rot.offsetZ * 3 - dir.offsetZ, rot.getOpposite())
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeFloat(progress);
		buf.writeFloat(lid);
		buf.writeBoolean(isProgressing);
		buf.writeBoolean(liquidMode);
		buf.writeBoolean(hasMaterial);

		for(int i = 0; i < 3; i++) buf.writeByte(electrodes[i]);

		buf.writeShort(liquids.size());

		for(MaterialStack mat : liquids) {
			buf.writeInt(mat.material.id);
			buf.writeInt(mat.amount);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.progress = buf.readFloat();
		this.syncLid = buf.readFloat();
		this.isProgressing = buf.readBoolean();
		this.liquidMode = buf.readBoolean();
		this.hasMaterial = buf.readBoolean();

		for(int i = 0; i < 3; i++) electrodes[i] = buf.readByte();

		int mats = buf.readShort();

		this.liquids.clear();
		for(int i = 0; i < mats; i++) {
			liquids.add(new MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt()));
		}

		if(syncLid != 0 && syncLid != 1) this.approachNum = 2;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.liquidMode = nbt.getBoolean("liquidMode");
		this.progress = nbt.getFloat("progress");
		this.lid = nbt.getFloat("lid");
		this.delay = nbt.getInteger("delay");

		int count = nbt.getShort("count");
		liquids.clear();

		for(int i = 0; i < count; i++) {
			liquids.add(new MaterialStack(Mats.matById.get(nbt.getInteger("m" + i)), nbt.getInteger("a" + i)));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setBoolean("liquidMode", liquidMode);
		nbt.setFloat("progress", progress);
		nbt.setFloat("lid", lid);
		nbt.setInteger("delay", delay);

		int count = liquids.size();
		nbt.setShort("count", (short) count);
		for(int i = 0; i < count; i++) {
			MaterialStack mat = liquids.get(i);
			nbt.setInteger("m" + i, mat.material.id);
			nbt.setInteger("a" + i, mat.amount);
		}
		return nbt;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 3,
					this.pos.getY(),
					this.pos.getZ() - 3,
					this.pos.getX() + 4,
					this.pos.getY() + 6,
					this.pos.getZ() + 4
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineArcFurnaceLarge(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineArcFurnaceLarge(player.inventory, this);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUsableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.getBoolean("liquid")) {
			this.liquidMode = !this.liquidMode;
			this.markDirty();
		}
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_arc_furnace));
		if(type == UpgradeType.SPEED) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (100 - 100 / (level * 2 + 1)) + "%"));
			info.add(TextFormatting.RED + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "+" + ((int) Math.pow(5, level) * 100 - 100) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		return upgrades;
	}

}
