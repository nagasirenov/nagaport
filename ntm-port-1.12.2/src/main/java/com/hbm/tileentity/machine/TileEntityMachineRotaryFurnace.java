package com.hbm.tileentity.machine;

import java.io.IOException;
import java.util.Random;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.container.ContainerMachineRotaryFurnace;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineRotaryFurnace;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes.RotaryFurnaceRecipe;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.module.ModuleBurnTime;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.*;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.Vec3NT;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluid.IFluidStandardTransceiver;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineRotaryFurnace extends TileEntityMachinePolluting implements IFluidStandardTransceiver, IGUIProvider, IFluidCopiable, IConditionalInvAccess, IConfigurableMachine {

	public FluidTank[] tanks;
	public boolean isProgressing;
	public float progress;
	public int burnTime;
	public double burnHeat = 1D;
	public int maxBurnTime;
	public int steamUsed = 0;
	public boolean isVenting;
	public MaterialStack output;
	public static final int maxOutput = MaterialShapes.BLOCK.q(16);

	private boolean canBreathe;

	public int anim;
	public int lastAnim;

	/**Given this has no heat, the heat mod instead affects the progress per fuel **/
	public static ModuleBurnTime burnModule = new ModuleBurnTime()
		.setCokeTimeMod(1.25)
		.setRocketTimeMod(1.5)
		.setSolidTimeMod(1.5)
		.setBalefireTimeMod(1.5)

		.setSolidHeatMod(1.5)
		.setRocketHeatMod(3)
		.setBalefireHeatMod(10);

	public TileEntityMachineRotaryFurnace() {
		super(5, 50);
		tanks = new FluidTank[3];
		tanks[0] = new FluidTank(Fluids.NONE, 16_000);
		tanks[1] = new FluidTank(Fluids.STEAM, 12_000);
		tanks[2] = new FluidTank(Fluids.SPENTSTEAM, 120);
	}

	@Override
	public String getDefaultName() {
		return "container.machineRotaryFurnace";
	}

	@Override
	public void update() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		if(!this.world.isRemote) {

			tanks[0].setType(3, slots);

			for(DirPos pos : getSteamPos()) {
				this.trySubscribe(tanks[1].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				if(tanks[2].getFill() > 0) this.sendFluid(tanks[2], this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}
			if(tanks[0].getTankType() != Fluids.NONE) for(DirPos pos : getFluidPos()) {
				this.trySubscribe(tanks[0].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}

			if(smoke.getFill() > 0) this.sendFluid(smoke, this.world, this.pos.getX() + rot.offsetX, this.pos.getY() + 5, this.pos.getZ() + rot.offsetZ, Library.POS_Y);

			if(this.output != null) {

				int prev = this.output.amount;
				Vec3NT impact = new Vec3NT(0, 0, 0);
				MaterialStack leftover = CrucibleUtil.pourSingleStack(this.world, this.pos.getX() + 0.5D + rot.offsetX * 2.875D, this.pos.getY() + 1.25D, this.pos.getZ() + 0.5D + rot.offsetZ * 2.875D, 6, true, this.output, MaterialShapes.INGOT.q(1), impact);
				this.output = leftover;

				if(prev != this.output.amount) {
					this.output = leftover;
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "foundry");
					data.setInteger("color", leftover.material.moltenColor);
					data.setByte("dir", (byte) rot.ordinal());
					data.setFloat("off", 0.625F);
					data.setFloat("base", 0.625F);
					data.setFloat("len", Math.max(1F, this.pos.getY() + 1 - (float) (Math.ceil(impact.y) - 1.125)));
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, this.pos.getX() + 0.5D + rot.offsetX * 2.875D, this.pos.getY() + 0.75, this.pos.getZ() + 0.5D + rot.offsetZ * 2.875D), new TargetPoint(this.world.provider.getDimension(), this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 50));
				}

				if(output.amount <= 0) this.output = null;
			}

			RotaryFurnaceRecipe recipe = RotaryFurnaceRecipes.getRecipe(slots[0], slots[1], slots[2]);
			this.isProgressing = false;
			this.canBreathe = false;

			if(recipe != null) {

				if(this.burnTime <= 0 && slots[4] != null && TileEntityFurnace.isItemFuel(slots[4])) {
					this.burnHeat = burnModule.getMod(slots[4], burnModule.getModHeat());
					this.maxBurnTime = this.burnTime = burnModule.getBurnTime(slots[4]) / 2;
					this.decrStackSize(4, 1);
					this.markChanged();
				}

				if(this.canProcess(recipe)) {
					float speed = Math.max((float) burnHeat, 1);
					this.progress += speed / recipe.duration;

					speed =  (float)(13 * Math.log10(speed) + 1);
					tanks[1].setFill((int) (tanks[1].getFill() - recipe.steam * speed));
					steamUsed += recipe.steam * speed;
					this.isProgressing = true;

					if(this.progress >= 1F) {
						this.progress -= 1F;
						this.consumeItems(recipe);

						if(this.output == null) {
							this.output = recipe.output.copy();
						} else {
							this.output.amount += recipe.output.amount;
						}
						this.markDirty();
					}

					if(this.burnTime > 0) {
						this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND / 10F);
						this.burnTime--;
					}

				} else {
					this.progress = 0;
				}

				if(this.steamUsed >= 100) {
					int steamReturn = this.steamUsed / 100;
					int canReturn = tanks[2].getMaxFill() - tanks[2].getFill();
					int doesReturn = Math.min(steamReturn, canReturn);
					this.steamUsed -= doesReturn * 100;
					tanks[2].setFill(tanks[2].getFill() + doesReturn);
				}

			} else {
				this.progress = 0;
			}

			this.isVenting = false;

			this.networkPackNT(50);

		} else {

			if(this.burnTime > 0 && MainRegistry.proxy.me().getDistance(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 25) {
				Random rand = this.world.rand;
				this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, this.pos.getX() + 0.5 + dir.offsetX * 0.5 + rot.offsetX + rand.nextGaussian() * 0.25, this.pos.getY() + 0.375, this.pos.getZ() + 0.5 + dir.offsetZ * 0.5 + rot.offsetZ + rand.nextGaussian() * 0.25, 0, 0, 0);
			}

			if(isVenting && this.world.getTotalWorldTime() % 2 == 0) {

				NBTTagCompound fx = new NBTTagCompound();
				fx.setString("type", "tower");
				fx.setFloat("lift", 10F);
				fx.setFloat("base", 0.25F);
				fx.setFloat("max", 2.5F);
				fx.setInteger("life", 100 + this.world.rand.nextInt(20));
				fx.setInteger("color",0x202020);
				fx.setDouble("posX", this.pos.getX() + 0.5 + rot.offsetX);
				fx.setDouble("posY", this.pos.getY() + 5);
				fx.setDouble("posZ", this.pos.getZ() + 0.5 + rot.offsetZ);
				MainRegistry.proxy.effectNT(fx);
			}
			this.lastAnim = this.anim;
			if(this.isProgressing) {
				this.anim += (int) Math.max(burnModule.getMod(slots[4], burnModule.getModHeat()), 1);
			}
		}
	}

	@Override public void serialize(ByteBuf buf) {
		super.serialize(buf);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
		tanks[2].serialize(buf);
		buf.writeBoolean(isVenting);
		buf.writeBoolean(isProgressing);
		buf.writeFloat(progress);
		buf.writeInt(burnTime);
		buf.writeInt(maxBurnTime);

		if(this.output != null) {
			buf.writeBoolean(true);
			buf.writeInt(this.output.material.id);
			buf.writeInt(this.output.amount);
		} else {
			buf.writeBoolean(false);
		}
	}

	@Override public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
		tanks[2].deserialize(buf);
		isVenting = buf.readBoolean();
		isProgressing = buf.readBoolean();
		progress = buf.readFloat();
		burnTime = buf.readInt();
		maxBurnTime = buf.readInt();

		if(buf.readBoolean()) {
			this.output = new MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt());
		} else {
			this.output = null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tanks[0].readFromNBT(nbt, "t0");
		this.tanks[1].readFromNBT(nbt, "t1");
		this.tanks[2].readFromNBT(nbt, "t2");
		this.progress = nbt.getFloat("prog");
		this.burnTime = nbt.getInteger("burn");
		this.burnHeat = nbt.getDouble("heat");
		this.maxBurnTime = nbt.getInteger("maxBurn");
		if (nbt.hasKey("outType")) {
			NTMMaterial mat = Mats.matById.get(nbt.getInteger("outType"));
			this.output = new MaterialStack(mat, nbt.getInteger("outAmount"));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.tanks[0].writeToNBT(nbt, "t0");
		this.tanks[1].writeToNBT(nbt, "t1");
		this.tanks[2].writeToNBT(nbt, "t2");
		nbt.setFloat("prog", progress);
		nbt.setInteger("burn", burnTime);
		nbt.setDouble("heat", burnHeat);
		nbt.setInteger("maxBurn", maxBurnTime);
		if (this.output != null) {
			nbt.setInteger("outType", this.output.material.id);
			nbt.setInteger("outAmount", this.output.amount);
		}
		return nbt;
	}

	public DirPos[] getSteamPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		return new DirPos[] {
				new DirPos(this.pos.getX() - dir.offsetX * 2 - rot.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2 - rot.offsetZ * 2, dir.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX * 2 - rot.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2 - rot.offsetZ, dir.getOpposite())
		};
	}

	public DirPos[] getFluidPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		return new DirPos[] {
				new DirPos(this.pos.getX() + dir.offsetX + rot.offsetX * 3, this.pos.getY(), this.pos.getZ() + dir.offsetZ + rot.offsetZ * 3, rot),
				new DirPos(this.pos.getX() - dir.offsetX + rot.offsetX * 3, this.pos.getY(), this.pos.getZ() - dir.offsetZ + rot.offsetZ * 3, rot)
		};
	}

	public boolean canProcess(RotaryFurnaceRecipe recipe) {

		if(this.burnTime <= 0) return false;

		if(recipe.fluid != null) {
			if(this.tanks[0].getTankType() != recipe.fluid.type) return false;
			if(this.tanks[0].getFill() < recipe.fluid.fill) return false;
		}

		float speed = Math.max((float) burnHeat, 1);

		if(tanks[1].getFill() < recipe.steam * speed) return false;
		if(tanks[2].getMaxFill() - tanks[2].getFill() < recipe.steam * speed / 100) return false;
		if(this.steamUsed > 100) return false;

		if(this.output != null) {
			if(this.output.material != recipe.output.material) return false;
			if(this.output.amount + recipe.output.amount > maxOutput) return false;
		}

		canBreathe = breatheAir(1);

		return canBreathe;
	}

	public void consumeItems(RotaryFurnaceRecipe recipe) {

		for(AStack aStack : recipe.ingredients) {

			for(int i = 0; i < 3; i++) {
				ItemStack stack = slots[i];
				if(aStack.matchesRecipe(stack, true) && stack.getCount() >= aStack.stacksize) {
					this.decrStackSize(i, aStack.stacksize);
					break;
				}
			}
		}

		if(recipe.fluid != null) {
			this.tanks[0].setFill(tanks[0].getFill() - recipe.fluid.fill);
		}
	}

	@Override
	public void pollute(PollutionType type, float amount) {
		FluidTank tank = type == PollutionType.SOOT ? smoke : type == PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;

		int fluidAmount = (int) Math.ceil(amount * 100);
		tank.setFill(tank.getFill() + fluidAmount);

		if(tank.getFill() > tank.getMaxFill()) {
			int overflow = tank.getFill() - tank.getMaxFill();
			tank.setFill(tank.getMaxFill());
			PollutionHandler.incrementPollution(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), type, overflow / 100F);
			this.isVenting = true;
		}
	}

	@Override public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return new int[0]; }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot < 3 || slot == 4; }
	@Override public boolean canExtractItem(int slot, ItemStack  stack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return false; }

	@Override public boolean isItemValidForSlot(int x, int y, int z, int slot, ItemStack stack) { return slot < 3 || slot == 4; }
	@Override public boolean canExtractItem(int x, int y, int z, int slot, ItemStack stack, int side) { return false; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 2,
					this.pos.getY(),
					this.pos.getZ() - 2,
					this.pos.getX() + 3,
					this.pos.getY() + 5,
					this.pos.getZ() + 3
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
	public int[] getAccessibleSlotsFromSide(int x, int y, int z, int side) {
		BlockPos pos = new BlockPos(x, y, z);
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		BlockPos core = new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ());

		//Red
		if(side == dir.getOpposite().ordinal() && pos.equals(core.clone().offset(dir, -1).offset(rot, -2))) return new int[] {0};
		//Yellow
		if(side == dir.getOpposite().ordinal() && pos.equals(core.clone().offset(dir, -1).offset(rot, -1))) return new int[] {1};
		//Green
		if(side == dir.getOpposite().ordinal() && pos.equals(core.clone().offset(dir, -1))) return new int[] {2};
		//Fuel
		if(side == dir.ordinal() && pos.equals(core.clone().offset(dir, 1).offset(rot, -1))) return new int[] {4};

		return new int[] { };
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tanks[0], tanks[1], tanks[2], smoke}; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[2], smoke}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0], tanks[1]}; }

	@Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachineRotaryFurnace(player.inventory, this); }
	@Override public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachineRotaryFurnace(player.inventory, this); }

	@Override
	public String getConfigName() {
		return "rotaryfurnace";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		if(obj.has("burnModule")) {
			burnModule.readIfPresent(obj.get("M:burnModule").getAsJsonObject());
		}
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("M:burnModule").beginObject();
		burnModule.writeConfig(writer);
		writer.endObject();
	}
}
