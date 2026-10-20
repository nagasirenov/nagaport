package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.ReactorResearch;
import com.hbm.handler.CompatHandler;
import com.hbm.inventory.container.ContainerMachineReactorBreeding;
import com.hbm.inventory.gui.GUIMachineReactorBreeding;
import com.hbm.inventory.recipes.BreederRecipes;
import com.hbm.inventory.recipes.BreederRecipes.BreederRecipe;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.CompatEnergyControl;

import api.hbm.tile.IInfoProviderEC;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityMachineReactorBreeding extends TileEntityMachineBase implements SimpleComponent, IGUIProvider, IInfoProviderEC, CompatHandler.OCComponent {

	public int flux;
	public float progress;
	
	private static final int[] slots_io = new int[] { 0, 1 };

	public TileEntityMachineReactorBreeding() {
		super(2);
	}
	
	@Override
	public String getDefaultName() {
		return "container.reactorBreeding";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {
			this.flux = 0;
			getInteractions();
			
			if(canProcess()) {
				
				progress += 0.0025F * (this.flux / BreederRecipes.getOutput(slots[0]).flux);
				
				if(this.progress >= 1.0F) {
					this.progress = 0F;
					this.processItem();
					this.markDirty();
				}
			} else {
				progress = 0.0F;
			}

			this.networkPackNT(20);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(flux);
		buf.writeFloat(progress);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.flux = buf.readInt();
		this.progress = buf.readFloat();
	}
	
	public void getInteractions() {
		
		for(byte d = 2; d < 6; d++) {
			ForgeDirection dir = ForgeDirection.getOrientation(d);
			Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ).getBlock();
			
			if(b == ModBlocks.reactor_research) {

				int[] pos = ((ReactorResearch) ModBlocks.reactor_research).findCore(this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ);

				if(pos != null) {
					TileEntity tile = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

					if(tile instanceof TileEntityReactorResearch) {
						TileEntityReactorResearch reactor = (TileEntityReactorResearch) tile;
						this.flux += reactor.totalFlux;
					}
				}
			}
		}
	}

	public boolean canProcess() {
		
		if(slots[0] == null)
			return false;
		
		BreederRecipe recipe = BreederRecipes.getOutput(slots[0]);
		
		if(recipe == null)
			return false;
		
		if(this.flux < recipe.flux)
			return false;

		if(slots[1] == null)
			return true;

		if(!slots[1].isItemEqual(recipe.output))
			return false;

		if(slots[1].stackSize < slots[1].getMaxStackSize())
			return true;
		else
			return false;
	}

	private void processItem() {
		
		if(canProcess()) {
			
			BreederRecipe rec = BreederRecipes.getOutput(slots[0]);
			
			if(rec == null)
				return;
			
			ItemStack itemStack = rec.output;

			if(slots[1] == null) {
				slots[1] = itemStack.copy();
			} else if(slots[1].isItemEqual(itemStack)) {
				slots[1].stackSize += itemStack.stackSize;
			}

			slots[0].stackSize--;
				
			if(slots[0].stackSize <= 0) {
				slots[0] = null;
			}
		}
	}

	

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return slots_io;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i == 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i == 1;
	}

	public int getProgressScaled(int i) {
		return (int) (this.progress * i);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		flux = nbt.getInteger("flux");
		progress = nbt.getFloat("progress");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setInteger("flux", flux);
		nbt.setFloat("progress", progress);
		return nbt;
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX(),
					this.pos.getY(),
					this.pos.getZ(),
					this.pos.getX() + 1,
					this.pos.getY() + 3,
					this.pos.getZ() + 1
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
	
	// do some opencomputer stuff
	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "breeding_reactor";
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getFlux(Context context, Arguments args) {
		return new Object[] {flux};
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getProgress(Context context, Arguments args) {
		return new Object[] {progress};
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {flux, progress};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineReactorBreeding(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineReactorBreeding(player.inventory, this);
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setInteger(CompatEnergyControl.I_FLUX, flux);
	}
}
