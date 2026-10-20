package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerWatz;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.gui.GUIWatz;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemWatzPellet;
import com.hbm.items.machine.ItemWatzPellet.EnumWatzType;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Compat;
import com.hbm.util.EnumUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.function.Function;

import api.hbm.fluid.IFluidStandardTransceiver;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityWatz extends TileEntityMachineBase implements IFluidStandardTransceiver, IControlReceiver, IGUIProvider, IFluidCopiable {

	public FluidTank[] tanks;
	public FluidTank[] sharedTanks;
	public int heat;
	public double fluxLastBase;		//flux created by the previous passive emission, only used for display
	public double fluxLastReaction;	//flux created by the previous reaction, used for the next reaction
	public double fluxDisplay;
	public boolean isOn;
	
	/* lock types for item IO */
	public boolean isLocked = false;
	public ItemStack[] locks;
	
	public TileEntityWatz() {
		super(24);
		this.locks = new ItemStack[slots.length];
		this.tanks = new FluidTank[3];
		this.tanks[0] = new FluidTank(Fluids.COOLANT, 64_000);
		this.tanks[1] = new FluidTank(Fluids.COOLANT_HOT, 64_000);
		this.tanks[2] = new FluidTank(Fluids.WATZ, 64_000);
		resetSharedTanks();
	}

	@Override
	public String getDefaultName() {
		return "container.watz";
	}
	
	protected void resetSharedTanks() {
		this.sharedTanks = new FluidTank[3];
		this.sharedTanks[0] = new FluidTank(Fluids.COOLANT, 64_000);
		this.sharedTanks[1] = new FluidTank(Fluids.COOLANT_HOT, 64_000);
		this.sharedTanks[2] = new FluidTank(Fluids.WATZ, 64_000);
		this.sharedTanks[0].setFill(tanks[0].getFill());
		this.sharedTanks[1].setFill(tanks[1].getFill());
		this.sharedTanks[2].setFill(tanks[2].getFill());
	}

	@Override
	public void update() {
		
		if(!this.world.isRemote) resetSharedTanks();
		
		if(!this.world.isRemote && !updateLock()) {
			
			boolean turnedOn = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY() + 3, this.pos.getZ()).getBlock() == ModBlocks.watz_pump && this.world.getIndirectPowerLevelTo(this.pos.getX(), this.pos.getY() + 5, this.pos.getZ(), 0) > 0;
			List<TileEntityWatz> segments = new ArrayList();
			segments.add(this);
			this.subscribeToTop();
			
			/* accumulate all segments */
			for(int y = this.pos.getY() - 3; y >= 0; y -= 3) {
				TileEntity tile = Compat.getTileStandard(this.world, this.pos.getX(), y, this.pos.getZ());
				if(tile instanceof TileEntityWatz) {
					segments.add((TileEntityWatz) tile);
				} else {
					break;
				}
			}
			
			/* set up shared tanks */
			FluidTank[] sharedTanks = new FluidTank[3];
			for(int i = 0; i < 3; i++) sharedTanks[i] = new FluidTank(tanks[i].getTankType(), 0);
			
			for(TileEntityWatz segment : segments) {
				segment.setupCoolant();
				for(int i = 0; i < 3; i++) {
					sharedTanks[i].changeTankSize(sharedTanks[i].getMaxFill() + segment.tanks[i].getMaxFill());
					sharedTanks[i].setFill(sharedTanks[i].getFill() + segment.tanks[i].getFill());
				}
			}
			
			//update coolant, bottom to top
			for(int i = segments.size() - 1; i >= 0; i--) {
				TileEntityWatz segment = segments.get(i);
				segment.updateCoolant(sharedTanks);
			}
			
			/* update reaction, top to bottom */
			this.updateReaction(null, sharedTanks, turnedOn);
			for(int i = 1; i < segments.size(); i++) {
				TileEntityWatz segment = segments.get(i);
				TileEntityWatz above = segments.get(i - 1);
				segment.updateReaction(above, sharedTanks, turnedOn);
			}
			
			/* send sync packets (order doesn't matter) */
			for(TileEntityWatz segment : segments) {
				segment.sharedTanks[0] = sharedTanks[0];
				segment.sharedTanks[1] = sharedTanks[1];
				segment.sharedTanks[2] = sharedTanks[2];
				segment.isOn = turnedOn;
				segment.networkPackNT(25);
				segment.heat *= 0.99; //cool 1% per tick
			}
			
			/* re-distribute fluid from shared tanks back into actual tanks, bottom to top */
			for(int i = segments.size() - 1; i >= 0; i--) {
				TileEntityWatz segment = segments.get(i);
				for(int j = 0; j < 3; j++) {
					int min = Math.min(segment.tanks[j].getMaxFill(), sharedTanks[j].getFill());
					sharedTanks[j].setFill(sharedTanks[j].getFill() - min);
					segment.tanks[j].setFill(min);
				}
			}
			
			segments.get(segments.size() - 1).sendOutBottom();
			
			/* explode on mud overflow */
			if(sharedTanks[2].getFill() > 0) {
				for(int x = -3; x <= 3; x++) {
					for(int y = 3; y < 6; y++) {
						for(int z = -3; z <= 3; z++) {
							this.world.setBlock(this.pos.getX() + x, this.pos.getY() + y, this.pos.getZ() + z, Blocks.AIR);
						}
					}
				}
				this.disassemble();
				
				ChunkRadiationManager.proxy.incrementRad(this.world, this.pos.getX(), this.pos.getY() + 1, this.pos.getZ(), 1_000F);
				
				com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 2, this.pos.getZ() + 0.5, "hbm:block.rbmk_explosion", 50.0F, 1.0F);
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "rbmkmush");
				data.setFloat("scale", 5);
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, this.pos.getX() + 0.5, this.pos.getY() + 2, this.pos.getZ() + 0.5), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));
				MainRegistry.proxy.effectNT(data);
				
				return;
			}
		}
	}
	
	/** basic sanity checking, usually wouldn't do anything except when NBT loading borks */
	public void setupCoolant() {
		tanks[0].setTankType(Fluids.COOLANT);
		tanks[1].setTankType(tanks[0].getTankType().getTrait(FT_Heatable.class).getFirstStep().typeProduced);
	}
	
	public void updateCoolant(FluidTank[] tanks) {
		
		double coolingFactor = 0.2D; //20% per tick
		double heatToUse = this.heat * coolingFactor;
		
		FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
		HeatingStep step = trait.getFirstStep();
		
		int heatCycles = (int) (heatToUse / step.heatReq);
		int coolCycles = tanks[0].getFill() / step.amountReq;
		int hotCycles = (tanks[1].getMaxFill() - tanks[1].getFill()) / step.amountProduced;
		
		int cycles = Math.min(heatCycles, Math.min(hotCycles, coolCycles));
		this.heat -= cycles * step.heatReq;
		tanks[0].setFill(tanks[0].getFill() - cycles * step.amountReq);
		tanks[1].setFill(tanks[1].getFill() + cycles * step.amountProduced);
	}

	/** enforces strict top to bottom update order (instead of semi-random based on placement) */
	public void updateReaction(TileEntityWatz above, FluidTank[] tanks, boolean turnedOn) {
		
		if(turnedOn) {
			List<ItemStack> pellets = new ArrayList();
			
			for(int i = 0; i < 24; i++) {
				ItemStack stack = slots[i];
				if(stack != null && stack.getItem() == ModItems.watz_pellet) {
					pellets.add(stack);
				}
			}
			
			double baseFlux = 0D;
			
			/* init base flux */
			for(ItemStack stack : pellets) {
				EnumWatzType type = EnumUtil.grabEnumSafely(EnumWatzType.class, stack.getItemDamage());
				baseFlux += type.passive;
			}
			
			double inputFlux = baseFlux + fluxLastReaction;
			double addedFlux = 0D;
			double addedHeat = 0D;
			
			for(ItemStack stack : pellets) {
				EnumWatzType type = EnumUtil.grabEnumSafely(EnumWatzType.class, stack.getItemDamage());
				Function burnFunc = type.burnFunc;
				Function heatDiv = type.heatDiv;
				
				if(burnFunc != null) {
					double div = heatDiv != null ? heatDiv.effonix(heat) : 1D;
					double burn = burnFunc.effonix(inputFlux) / div;
					ItemWatzPellet.setYield(stack, ItemWatzPellet.getYield(stack) - burn);
					addedFlux += burn;
					addedHeat += type.heatEmission * burn;
					tanks[2].setFill(tanks[2].getFill() + (int) Math.round(type.mudContent * burn));
				}
			}
			
			for(ItemStack stack : pellets) {
				EnumWatzType type = EnumUtil.grabEnumSafely(EnumWatzType.class, stack.getItemDamage());
				Function absorbFunc = type.absorbFunc;
				
				if(absorbFunc != null) {
					double absorb = absorbFunc.effonix(baseFlux + fluxLastReaction);
					addedHeat += absorb;
					ItemWatzPellet.setYield(stack, ItemWatzPellet.getYield(stack) - absorb);
					tanks[2].setFill(tanks[2].getFill() + (int) Math.round(type.mudContent * absorb));
				}
			}
			
			this.heat += addedHeat;
			this.fluxLastBase = baseFlux;
			this.fluxLastReaction = addedFlux;
			
		} else {
			this.fluxLastBase = 0;
			this.fluxLastReaction = 0;
			
		}
		
		for(int i = 0; i < 24; i++) {
			ItemStack stack = slots[i];
			
			/* deplete */
			if(stack != null && stack.getItem() == ModItems.watz_pellet && ItemWatzPellet.getEnrichment(stack) <= 0) {
				slots[i] = new ItemStack(ModItems.watz_pellet_depleted, 1, stack.getItemDamage());
				continue; // depleted pellets may persist for one tick
			}
		}
		
		if(above != null) {
			for(int i = 0; i < 24; i++) {
				ItemStack stackBottom = slots[i];
				ItemStack stackTop = above.slots[i];
				
				/* items fall down if the bottom slot is empty */
				if(stackBottom == null && stackTop != null) {
					slots[i] = stackTop.copy();
					above.decrStackSize(i, stackTop.stackSize);
				}
				
				/* items switch places if the top slot is depleted */
				if(stackBottom != null && stackBottom.getItem() == ModItems.watz_pellet && stackTop != null && stackTop.getItem() == ModItems.watz_pellet_depleted) {
					ItemStack buf = stackTop.copy();
					above.slots[i] = stackBottom.copy();
					slots[i] = buf;
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.heat);
		buf.writeBoolean(isOn);
		buf.writeBoolean(isLocked);
		buf.writeDouble(this.fluxLastReaction + this.fluxLastBase);
		for(FluidTank tank : sharedTanks) {
			tank.serialize(buf);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.heat = buf.readInt();
		this.isOn = buf.readBoolean();
		this.isLocked = buf.readBoolean();
		this.fluxDisplay = buf.readDouble();
		for(FluidTank tank : tanks) {
			tank.deserialize(buf);
		}
	}
	
	/** Prevent manual updates when another segment is above this one */
	public boolean updateLock() {
		return Compat.getTileStandard(this.world, this.pos.getX(), this.pos.getY() + 3, this.pos.getZ()) instanceof TileEntityWatz;
	}
	
	protected void subscribeToTop() {
		this.trySubscribe(tanks[0].getTankType(), this.world, this.pos.getX(), this.pos.getY() + 3, this.pos.getZ(), ForgeDirection.UP);
		this.trySubscribe(tanks[0].getTankType(), this.world, this.pos.getX() + 2, this.pos.getY() + 3, this.pos.getZ(), ForgeDirection.UP);
		this.trySubscribe(tanks[0].getTankType(), this.world, this.pos.getX() - 2, this.pos.getY() + 3, this.pos.getZ(), ForgeDirection.UP);
		this.trySubscribe(tanks[0].getTankType(), this.world, this.pos.getX(), this.pos.getY() + 3, this.pos.getZ() + 2, ForgeDirection.UP);
		this.trySubscribe(tanks[0].getTankType(), this.world, this.pos.getX(), this.pos.getY() + 3, this.pos.getZ() - 2, ForgeDirection.UP);
	}
	
	protected void sendOutBottom() {
		
		for(DirPos pos : getSendingPos()) {
			if(tanks[1].getFill() > 0) this.sendFluid(tanks[1], this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			if(tanks[2].getFill() > 0) this.sendFluid(tanks[2], this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}
	
	protected DirPos[] getSendingPos() {
		return new DirPos[] {
				new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ(), ForgeDirection.DOWN),
				new DirPos(this.pos.getX() + 2, this.pos.getY() - 1, this.pos.getZ(), ForgeDirection.DOWN),
				new DirPos(this.pos.getX() - 2, this.pos.getY() - 1, this.pos.getZ(), ForgeDirection.DOWN),
				new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() + 2, ForgeDirection.DOWN),
				new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() - 2, ForgeDirection.DOWN)
		};
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		NBTTagList list = nbt.getTagList("locks", 10);
		
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length) {
				locks[b0] = NullableInventoryStackNbt.read(nbt1);
			}
		}
		
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
		this.heat = nbt.getInteger("heat");
		this.fluxLastBase = nbt.getDouble("lastFluxB");
		this.fluxLastReaction = nbt.getDouble("lastFluxR");
		
		this.isLocked = nbt.getBoolean("isLocked");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		NBTTagList list = new NBTTagList();
		
		for(int i = 0; i < locks.length; i++) {
			if(locks[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				locks[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("locks", list);
		
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
		nbt.setInteger("heat", this.heat);
		nbt.setDouble("lastFluxB", fluxLastBase);
		nbt.setDouble("lastFluxR", fluxLastReaction);
		
		nbt.setBoolean("isLocked", isLocked);
		return nbt;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUsableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		
		if(data.hasKey("lock")) {
			
			if(this.isLocked) {
				this.locks = new ItemStack[slots.length];
			} else {
				for(int i = 0; i < slots.length; i++) {
					this.locks[i] = slots[i];
				}
			}
			
			this.isLocked = !this.isLocked;
			this.markChanged();
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(stack.getItem() != ModItems.watz_pellet) return false;
		if(!this.isLocked) return true;
		return this.locks[i] != null && this.locks[i].getItem() == stack.getItem() && locks[i].getItemDamage() == stack.getItemDamage();
	}
	
	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return stack.getItem() != ModItems.watz_pellet;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 3,
					this.pos.getY(),
					this.pos.getZ() - 3,
					this.pos.getX() + 4,
					this.pos.getY() + 3,
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
	
	private void disassemble() {

		int count = 20;
		Random rand = this.world.rand;
		for(int i = 0; i < count * 5; i++) {
			EntityShrapnel shrapnel = new EntityShrapnel(this.world);
			shrapnel.posX = this.pos.getX() + 0.5;
			shrapnel.posY = this.pos.getY() + 3;
			shrapnel.posZ = this.pos.getZ() + 0.5;
			shrapnel.motionY = ((rand.nextFloat() * 0.5) + 0.5) * (1 + (count / (15 + rand.nextInt(21)))) + (rand.nextFloat() / 50 * count);
			shrapnel.motionX = rand.nextGaussian() * 1	* (1 + (count / 100));
			shrapnel.motionZ = rand.nextGaussian() * 1	* (1 + (count / 100));
			shrapnel.setWatz(true);
			this.world.spawnEntityInWorld(shrapnel);
		}

		this.world.setBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(), ModBlocks.mud_block);
		this.world.setBlock(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ(), ModBlocks.mud_block);
		this.world.setBlock(this.pos.getX(), this.pos.getY() + 2, this.pos.getZ(), ModBlocks.mud_block);
		
		setBrokenColumn(0, ModBlocks.watz_element, 0, 1, 0);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 2, 0);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 0, 1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 0, 2);
		setBrokenColumn(0, ModBlocks.watz_element, 0, -1, 0);
		setBrokenColumn(0, ModBlocks.watz_element, 0, -2, 0);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 0, -1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 0, -2);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 1, 1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 1, -1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, -1, 1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, -1, -1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, 2, 1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, 2, -1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, 1, 2);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, -1, 2);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, -2, 1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, -2, -1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, 1, -2);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, -1, -2);
		
		for(int j = -1; j < 2; j++) {
			setBrokenColumn(1, ModBlocks.watz_end, 1, 3, j);
			setBrokenColumn(1, ModBlocks.watz_end, 1, j, 3);
			setBrokenColumn(1, ModBlocks.watz_end, 1, -3, j);
			setBrokenColumn(1, ModBlocks.watz_end, 1, j, -3);
		}
		setBrokenColumn(1, ModBlocks.watz_end, 1, 2, 2);
		setBrokenColumn(1, ModBlocks.watz_end, 1, 2, -2);
		setBrokenColumn(1, ModBlocks.watz_end, 1, -2, 2);
		setBrokenColumn(1, ModBlocks.watz_end, 1, -2, -2);
		
		List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5).expand(50, 50, 50));
		
		for(EntityPlayer player : players) {
			com.hbm.advancement.NtmAdvancements.award(player, MainRegistry.achWatzBoom);
		}
	}
	
	private void setBrokenColumn(int minHeight, Block b, int meta, int x, int z) {
		
		int height = minHeight + this.world.rand.nextInt(3 - minHeight);
		
		for(int i = 0; i < 3; i++) {
			
			if(i <= height) {
				this.world.setBlock(this.pos.getX() + x, this.pos.getY() + i, this.pos.getZ() + z, b, meta, 3);
			} else {
				this.world.setBlock(this.pos.getX() + x, this.pos.getY() + i, this.pos.getZ() + z, ModBlocks.mud_block);
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerWatz(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIWatz(player.inventory, this);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[1], tanks[2] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[0] };
	}

	@Override
	public FluidTank getTankToPaste() {
		return null;
	}
}
