package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.inventory.gui.GUICrateTungsten;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IDysonConverter;

import api.hbm.block.ILaserable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityCrateTungsten extends TileEntityCrateBase implements ILaserable, IDysonConverter, net.minecraft.util.ITickable {

	@Override
	public boolean isEmpty() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
			if(this.getStackInSlot(i) != null && !this.getStackInSlot(i).isEmpty()) return false;
		}
		return true;
	}

	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }

	@Override
	public void clear() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
		this.setInventorySlotContents(i, null);
		}
	}

	private int heatTimer;

	public TileEntityCrateTungsten() {
		super(27);
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.crateTungsten";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {
			if(heatTimer > 0)
				heatTimer--;

			if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()) ) != 1 && heatTimer > 0)
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), 1, 3);

			if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()) ) != 0 && heatTimer <= 0)
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), 0, 3);
		}

		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()) ) == 1) {
			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, this.pos.getX() + this.world.rand.nextDouble(), this.pos.getY() + 1.1, this.pos.getZ() + this.world.rand.nextDouble(), 0.0, 0.0, 0.0);
			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, this.pos.getX() + this.world.rand.nextDouble(), this.pos.getY() + 1.1, this.pos.getZ() + this.world.rand.nextDouble(), 0.0, 0.0, 0.0);

			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, this.pos.getX() - 0.1, this.pos.getY() + this.world.rand.nextDouble(), this.pos.getZ() + this.world.rand.nextDouble(), 0.0, 0.0, 0.0);
			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, this.pos.getX() - 0.1, this.pos.getY() + this.world.rand.nextDouble(), this.pos.getZ() + this.world.rand.nextDouble(), 0.0, 0.0, 0.0);

			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, this.pos.getX() + 1.1, this.pos.getY() + this.world.rand.nextDouble(), this.pos.getZ() + this.world.rand.nextDouble(), 0.0, 0.0, 0.0);
			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, this.pos.getX() + 1.1, this.pos.getY() + this.world.rand.nextDouble(), this.pos.getZ() + this.world.rand.nextDouble(), 0.0, 0.0, 0.0);

			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, this.pos.getX() + this.world.rand.nextDouble(), this.pos.getY() + this.world.rand.nextDouble(), this.pos.getZ() - 0.1, 0.0, 0.0, 0.0);
			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, this.pos.getX() + this.world.rand.nextDouble(), this.pos.getY() + this.world.rand.nextDouble(), this.pos.getZ() - 0.1, 0.0, 0.0, 0.0);

			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, this.pos.getX() + this.world.rand.nextDouble(), this.pos.getY() + this.world.rand.nextDouble(), this.pos.getZ() + 1.1, 0.0, 0.0, 0.0);
			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, this.pos.getX() + this.world.rand.nextDouble(), this.pos.getY() + this.world.rand.nextDouble(), this.pos.getZ() + 1.1, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		if(this.isLocked())
			return false;

		if(itemStack.getItem() == ModItems.billet_polonium)
			return false;

		if(itemStack.getItem() == ModItems.crucible && itemStack.getItemDamage() > 0)
			return false;

		if(FurnaceRecipes.instance().getSmeltingResult(itemStack) == null)
			return true;

		return false;
	}

	@Override
	public void addEnergy(World world, int x, int y, int z, long energy, ForgeDirection dir) {
		heatTimer = 5;

		for(int i = 0; i < slots.length; i++) {

			if(slots[i] == null)
				continue;

			ItemStack result = FurnaceRecipes.instance().getSmeltingResult(slots[i]);

			if(slots[i].getItem() == ModItems.billet_polonium && energy > 10000000)
				result = new ItemStack(ModItems.billet_yharonite);

			if(slots[i].getItem() == ModItems.crucible && slots[i].getItemDamage() > 0 && energy > 10000000)
				result = new ItemStack(ModItems.crucible, 1, 0);

			int size = slots[i].getCount();

			if(result != null && result.getCount() * size <= result.getMaxStackSize()) {
				slots[i] = result.copy();
				slots[i].setCount(slots[i].getCount() * (size));
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCrateTungsten(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICrateTungsten(player.inventory, this);
	}

	// Dyson laser support
	@Override
	public boolean provideEnergy(int x, int y, int z, long energy) {
		addEnergy(this.world, x, y, z, energy / 5000, ForgeDirection.NORTH);
		return true;
	}

	@Override
	public long maximumEnergy() {
		return Long.MAX_VALUE;
	}

}
