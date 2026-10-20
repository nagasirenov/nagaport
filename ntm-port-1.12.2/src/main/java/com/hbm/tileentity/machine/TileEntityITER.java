package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerITER;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.gui.GUIITER;
import com.hbm.inventory.recipes.BreederRecipes;
import com.hbm.inventory.recipes.BreederRecipes.BreederRecipe;
import com.hbm.inventory.recipes.FusionRecipesLegacy;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemFusionShield;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.CompatEnergyControl;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import api.hbm.tile.IInfoProviderEC;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@Deprecated
public class TileEntityITER extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiver, IGUIProvider, IInfoProviderEC, IFluidCopiable, IRORValueProvider, IRORInteractive {

	public long power;
	public static final long maxPower = 10000000;
	public static final int powerReq = 100000;
	public FluidTank[] tanks;
	public FluidTank plasma;

	public int progress;
	public static final int duration = 100;
	public long totalRuntime;

	@SideOnly(Side.CLIENT)
	public int blanket;

	public float rotor;
	public float lastRotor;
	public boolean isOn;

	private float rotorSpeed = 0F;

	private AudioWrapper audio;

	public TileEntityITER() {
		super(6);
		tanks = new FluidTank[4];
		tanks[0] = new FluidTank(Fluids.WATER, 1280000);
		tanks[1] = new FluidTank(Fluids.ULTRAHOTSTEAM, 128000);
		tanks[2] = new FluidTank(Fluids.COOLANT, 16_000);
		tanks[3] = new FluidTank(Fluids.COOLANT_HOT, 16_000);
		plasma = new FluidTank(Fluids.PLASMA_DT, 16000);

	}

	@Override
	public String getDefaultName() {
		return "container.machineITER";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			this.updateConnections();
			power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			if(tanks[2].getTankType().hasTrait(FT_Heatable.class)) {
				FT_Heatable trait = tanks[2].getTankType().getTrait(FT_Heatable.class);
				HeatingStep step = trait.getFirstStep();
				tanks[3].setTankType(step.typeProduced);
			}
			else {
				tanks[2].setTankType(Fluids.NONE);
				tanks[3].setTankType(Fluids.NONE);
			}
			/// START Processing part ///

			if(!isOn) {
				plasma.setFill(0);	//jettison plasma if the thing is turned off
			}

			//explode either if there's plasma that is too hot or if the reactor is turned on but the magnets have no power
			if(plasma.getFill() > 0 && (this.plasma.getTankType().temperature >= this.getShield() || (this.isOn && this.power < this.powerReq || tanks[2].getFill() == 0 || tanks[3].getFill() == tanks[3].getMaxFill()))) {
				this.explode();
			}

			if(isOn && power >= powerReq) {
				power -= powerReq;

				if(plasma.getFill() > 0) {
					this.totalRuntime++;
					int delay = FusionRecipesLegacy.getByproductDelay(plasma.getTankType());
					if(delay > 0 && totalRuntime % delay == 0) produceByproduct();
				}

				if(plasma.getFill() > 0 && this.getShield() != 0) {

					ItemFusionShield.setShieldDamage(slots[3], ItemFusionShield.getShieldDamage(slots[3]) + 1);

					if(ItemFusionShield.getShieldDamage(slots[3]) > ((ItemFusionShield)slots[3].getItem()).maxDamage) {
						slots[3] = null;
						com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.shutdown", 5F, 1F);
						this.isOn = false;
						this.markDirty();
					}
				}

				int prod = FusionRecipesLegacy.getSteamProduction(plasma.getTankType());
				int lod = FusionRecipesLegacy.getCoolant(plasma.getTankType());

				for(int i = 0; i < 20; i++) {

					if(plasma.getFill() > 0) {

						if(tanks[0].getFill() >= prod * 10) {
							tanks[0].setFill(tanks[0].getFill() - prod * 10);
							tanks[1].setFill(tanks[1].getFill() + prod);

						}
						if(tanks[1].getFill() > tanks[1].getMaxFill()) {
							tanks[1].setFill(tanks[1].getMaxFill());
							tanks[0].setFill(tanks[0].getMaxFill()); //this should stop it from eating fluids when buffers are full
						}

						if(tanks[2].getFill() >= lod) {
							int coolantToDrain = (int) (Math.min(tanks[3].getMaxFill(), tanks[2].getFill()));
							coolantToDrain = Math.min(lod, tanks[1].getMaxFill() - tanks[1].getFill());
							tanks[2].setFill(tanks[2].getFill() - coolantToDrain);
							tanks[3].setFill(tanks[3].getFill() + coolantToDrain);

						}
						if(tanks[3].getFill() > tanks[3].getMaxFill()) {
							tanks[3].setFill(tanks[3].getMaxFill());
							tanks[2].setFill(tanks[2].getMaxFill());
						}

						plasma.setFill(plasma.getFill() - 1);
					}
				}
			}

			doBreederStuff();

			/// END Processing part ///

			/// START Notif packets ///

			for(DirPos pos : getConPos()) {
				if(tanks[1].getFill() > 0) {
					this.sendFluid(tanks[1], this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}
				if(tanks[3].getFill() > 0) {
					this.sendFluid(tanks[3], this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}
			}

			this.networkPackNT(250);
			/// END Notif packets ///

		} else {

			this.lastRotor = this.rotor;
			this.rotor += this.rotorSpeed;

			if(this.rotor >= 360) {
				this.rotor -= 360;
				this.lastRotor -= 360;
			}

			if(this.isOn && this.power >= powerReq) {
				this.rotorSpeed = Math.max(0F, Math.min(15F, this.rotorSpeed + 0.05F));

				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound("hbm:block.fusionReactorRunning", this.pos.getX(), this.pos.getY(), this.pos.getZ(), 1.0F, 30F, 1.0F);
					audio.startSound();
				}

				float rotorSpeed = this.rotorSpeed / 15F;
				audio.updateVolume(getVolume(0.5f * rotorSpeed));
				audio.updatePitch(0.25F + 0.75F * rotorSpeed);
			} else {
				this.rotorSpeed = Math.max(0F, Math.min(15F, this.rotorSpeed - 0.1F));

				if(audio != null) {
					if(this.rotorSpeed > 0) {
						float rotorSpeed = this.rotorSpeed / 15F;
						audio.updateVolume(getVolume(0.5f * rotorSpeed));
						audio.updatePitch(0.25F + 0.75F * rotorSpeed);
					} else {
						audio.stopSound();
						audio = null;
					}
				}
			}
		}
	}

	protected List<DirPos> connections;

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(tanks[0].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(tanks[2].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	protected List<DirPos> getConPos() {
		if(connections != null && !connections.isEmpty())
			return connections;

		connections = new ArrayList();

		connections.add(new DirPos(this.pos.getX(), this.pos.getY() + 3, this.pos.getZ(), ForgeDirection.UP));
		connections.add(new DirPos(this.pos.getX(), this.pos.getY() - 3, this.pos.getZ(), ForgeDirection.DOWN));

		Vec3d vec = new Vec3d(5.75, 0, 0);

		for(int i = 0; i < 16; i++) {
			vec = vec.rotateYaw((float) (Math.PI / 8));
			connections.add(new DirPos(this.pos.getX() + (int)vec.x, this.pos.getY() + 3, this.pos.getZ() + (int)vec.z, ForgeDirection.UP));
			connections.add(new DirPos(this.pos.getX() + (int)vec.x, this.pos.getY() - 3, this.pos.getZ() + (int)vec.z, ForgeDirection.DOWN));
		}

		return connections;
	}

	private void explode() {
		this.disassemble();

		if(this.plasma.getTankType() == Fluids.PLASMA_BF) {

			com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:weapon.mukeExplosion", 15.0F, 1.0F);
			ExplosionLarge.spawnShrapnels(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, 50);

			ExplosionNT exp = new ExplosionNT(this.world, null, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, 20F)
					.addAttrib(ExAttrib.BALEFIRE)
					.addAttrib(ExAttrib.NOPARTICLE)
					.addAttrib(ExAttrib.NOSOUND)
					.addAttrib(ExAttrib.NODROP)
					.overrideResolution(64);
			exp.doExplosionA();
			exp.doExplosionB(false);

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "muke");
			data.setBoolean("balefire", true);
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));

		} else {
			Vec3d vec = new Vec3d(5.5, 0, 0);
			vec = vec.rotateYaw(this.world.rand.nextFloat() * (float)Math.PI * 2F);

			this.world.newExplosion(null, this.pos.getX() + 0.5 + vec.x, this.pos.getY() + 0.5 + this.world.rand.nextGaussian() * 1.5D, this.pos.getZ() + 0.5 + vec.z, 2.5F, true, true);
		}

	}

	private void doBreederStuff() {

		if(plasma.getFill() == 0) {
			this.progress = 0;
			return;
		}

		BreederRecipe out = BreederRecipes.getOutput(slots[1]);

		if(slots[1] != null && slots[1].getItem() == ModItems.meteorite_sword_irradiated)
			out = new BreederRecipe(ModItems.meteorite_sword_fused, 1000);

		if(slots[1] != null && slots[1].getItem() == ModItems.meteorite_sword_fused)
			out = new BreederRecipe(ModItems.meteorite_sword_baleful, 4000);

		if(slots[1] != null && slots[1].getItem() == Item.getItemFromBlock(ModBlocks.lattice_log))
			out = new BreederRecipe(ModItems.woodemium_briquette, 4000);

		if(out == null) {
			this.progress = 0;
			return;
		}

		if(slots[2] != null && slots[2].stackSize >= slots[2].getMaxStackSize()) {
			this.progress = 0;
			return;
		}

		int level = FusionRecipesLegacy.getBreedingLevel(plasma.getTankType());

		if(out.flux > level) {
			this.progress = 0;
			return;
		}

		progress++;

		if(progress > this.duration) {

			this.progress = 0;

			if(slots[2] != null) {
				slots[2].stackSize++;
			} else {
				slots[2] = out.output.copy();
			}

			slots[1].stackSize--;

			if(slots[1].stackSize <= 0)
				slots[1] = null;

			this.markDirty();
		}
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return slot == 2 || slot == 4; // only allow removing breeder outputs <- ?????
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 1, 2, 4 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {

		if(i == 1 && BreederRecipes.getOutput(stack) != null)
			return true;

		return false;
	}

	private void produceByproduct() {

		ItemStack by = FusionRecipesLegacy.getByproduct(plasma.getTankType());

		if(by == null)
			return;

		if(slots[4] == null) {
			slots[4] = by;
			return;
		}

		if(slots[4].getItem() == by.getItem() && slots[4].getItemDamage() == by.getItemDamage() && slots[4].stackSize < slots[4].getMaxStackSize()) {
			slots[4].stackSize++;
		}
	}

	public int getShield() {

		if(slots[3] == null || !(slots[3].getItem() instanceof ItemFusionShield))
			return 0;

		return ((ItemFusionShield)slots[3].getItem()).maxTemp;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.isOn);
		buf.writeLong(this.power);
		buf.writeInt(this.progress);
		for(FluidTank tank : tanks) tank.serialize(buf);
		plasma.serialize(buf);
		if(slots[3] == null) {
			buf.writeInt(0);
		} else if(slots[3].getItem() == ModItems.fusion_shield_tungsten) {
			buf.writeInt(1);
		} else if(slots[3].getItem() == ModItems.fusion_shield_desh) {
			buf.writeInt(2);
		} else if(slots[3].getItem() == ModItems.fusion_shield_chlorophyte) {
			buf.writeInt(3);
		} else if(slots[3].getItem() == ModItems.fusion_shield_vaporwave) {
			buf.writeInt(4);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.isOn = buf.readBoolean();
		this.power = buf.readLong();
		this.progress = buf.readInt();
		for(FluidTank tank : tanks) tank.deserialize(buf);
		plasma.deserialize(buf);
		this.blanket = buf.readInt();
	}

	@Override
	public void handleButtonPacket(int value, int meta) {
		if(meta == 0) this.isOn = !this.isOn;
	}

	public long getPowerScaled(long i) { return (power * i) / maxPower; }
	public long getProgressScaled(long i) { return (progress * i) / duration; }
	@Override public void setPower(long i) { this.power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.isOn = nbt.getBoolean("isOn");
		this.totalRuntime = nbt.getLong("totalRuntime");

		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
		tanks[2].readFromNBT(nbt, "coolant");
		tanks[3].readFromNBT(nbt, "hotlant");
		plasma.readFromNBT(nbt, "plasma");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", this.power);
		nbt.setBoolean("isOn", isOn);
		nbt.setLong("totalRuntime", this.totalRuntime);

		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
		tanks[2].writeToNBT(nbt, "coolant");
		tanks[3].writeToNBT(nbt, "hotlant");
		plasma.writeToNBT(nbt, "plasma");
		return nbt;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() + 0.5 - 8,
					this.pos.getY() + 0.5 - 3,
					this.pos.getZ() + 0.5 - 8,
					this.pos.getX() + 0.5 + 8,
					this.pos.getY() + 0.5 + 3,
					this.pos.getZ() + 0.5 + 8
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public void disassemble() {
		com.hbm.world.WorldBlockDestruction.destroy(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), false);
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[1], tanks[3]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0], tanks[2]};
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerITER(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIITER(player.inventory, this);
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setBoolean(CompatEnergyControl.B_ACTIVE, this.isOn && plasma.getFill() > 0);
		int output = FusionRecipesLegacy.getSteamProduction(plasma.getTankType());
		data.setDouble("consumption", output * 10);
		data.setDouble("outputmb", output);
	}

	@Override
	public FluidTank getTankToPaste() {
		return null;
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_VALUE + "durability",
				PREFIX_VALUE + "durabilitypercent",
				PREFIX_FUNCTION + "toggle",
				PREFIX_FUNCTION + "switch" + NAME_SEPARATOR + "on/off",
		};
	}

	@Override
	public String provideRORValue(String name) {
		if((PREFIX_VALUE + "durability").equals(name))			return (slots[3] != null && slots[3].getItem() instanceof ItemFusionShield) ? "" + (((ItemFusionShield) slots[3].getItem()).maxDamage - ItemFusionShield.getShieldDamage(slots[3])) : "0";
		if((PREFIX_VALUE + "durabilitypercent").equals(name))	return (slots[3] != null && slots[3].getItem() instanceof ItemFusionShield) ? "" + ((((ItemFusionShield) slots[3].getItem()).maxDamage - ItemFusionShield.getShieldDamage(slots[3])) * 100 / ((ItemFusionShield) slots[3].getItem()).maxDamage) : "0";
		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		if((PREFIX_FUNCTION + "toggle").equals(name)) {
			this.isOn = !this.isOn;
			this.markChanged();
			return null;
		}


		if((PREFIX_FUNCTION + "switch").equals(name) && params.length > 0) {
			if("on".equals(params[0])) {
				this.isOn = true;
				this.markChanged();
				return null;
			}
			if("off".equals(params[0])) {
				this.isOn = false;
				this.markChanged();
				return null;
			}
		}

		return null;
	}
}
