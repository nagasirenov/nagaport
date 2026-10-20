package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockContainerBase;
import com.hbm.blocks.ITooltipProvider;

import api.hbm.block.IInsertable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class PistonInserter extends BlockContainerBase implements ITooltipProvider {

	public PistonInserter() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPistonInserter();
	}

	@Override
	public void neighborChanged(net.minecraft.block.state.IBlockState nativeState, World world, net.minecraft.util.math.BlockPos nativePos, Block neighbor, net.minecraft.util.math.BlockPos fromPos) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		this.updateState(world, x, y, z);
	}

	protected void updateState(World world, int x, int y, int z) {
		if(!world.isRemote) {
			ForgeDirection dir = ForgeDirection.getOrientation(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ));

			if(com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock().isNormalCube())
				return; //no obstructions allowed!

			boolean flag = checkRedstone(world, x, y, z);
			TileEntityPistonInserter piston = (TileEntityPistonInserter)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(flag && !piston.lastState && piston.extend <= 0)
				piston.isRetracting = false;

			piston.lastState = flag;
		}
	}

	protected boolean checkRedstone(World world, int x, int y, int z) {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if(world.getIndirectPowerOutput(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.ordinal()))
				return true;
		}

		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

		if(side != com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) )) return false;

		if(player.isSneaking()) {
			if(!world.isRemote) {
				TileEntityPistonInserter piston = (TileEntityPistonInserter)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

				if(piston.slot != null && piston.isRetracting) {
					ForgeDirection dir = ForgeDirection.getOrientation(piston.getBlockMetadata());

					EntityItem dust = new EntityItem(world, x + 0.5D + dir.offsetX * 0.75D, y + 0.5D + dir.offsetY * 0.75D, z + 0.5D + dir.offsetZ * 0.75D, piston.slot);
					piston.slot = null;

					dust.motionX = dir.offsetX * 0.25;
					dust.motionY = dir.offsetY * 0.25;
					dust.motionZ = dir.offsetZ * 0.25;
					world.spawnEntity(dust);
				}
			}

			return true;
		} else if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null) {
			if(!world.isRemote) {
				TileEntityPistonInserter piston = (TileEntityPistonInserter)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

				if(piston.slot == null) {
					ItemStack removed = player.inventory.decrStackSize(player.inventory.currentItem, 1);
					piston.slot = removed == null || removed.isEmpty() ? null : removed;
					player.inventoryContainer.detectAndSendChanges();
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int l = BlockPistonBase.determineOrientation(world, x, y, z, player);
		world.setBlockMetadataWithNotify(x, y, z, l, 2);
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		int meta = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
		return meta != side.ordinal() && meta != side.getOpposite().ordinal();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		IInventory tileentityfurnace = (IInventory) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tileentityfurnace != null) {

			ItemStack itemstack = tileentityfurnace.getStackInSlot(0);

			if(itemstack != null && !itemstack.isEmpty()) {
				float f = world.rand.nextFloat() * 0.8F + 0.1F;
				float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
				float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

				while(itemstack.getCount() > 0) {
					int j1 = world.rand.nextInt(21) + 10;

					if(j1 > itemstack.getCount()) {
						j1 = itemstack.getCount();
					}

					// Capture before depletion: native empty stacks hide their item.
					net.minecraft.item.Item dropItem = itemstack.getItem();
					int dropMeta = itemstack.getItemDamage();
					NBTTagCompound dropTag = itemstack.getTagCompound();
					itemstack.shrink(j1);
					EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));

					if(dropTag != null) {
						entityitem.getItem().setTagCompound(dropTag.copy());
					}

					float f3 = 0.05F;
					entityitem.motionX = (float) world.rand.nextGaussian() * f3;
					entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 0.2F;
					entityitem.motionZ = (float) world.rand.nextGaussian() * f3;
					world.spawnEntity(entityitem);
				}
			}

			world.func_147453_f(x, y, z, block);
		}

		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	//						 $%&#$&
	//					   %$&&@$%%#%
	//______        	  $%@--$@@%&$%$
	//		|			  %/   *--$#@&&$$
	//		|			 /	  --__  %$%@$&
	//		|			(----^`---	 $@##%
	//		|			/___\	`-----*#@$
	//		|		   /(()_) / /___\  /__
	//		|		 /	\___//	(()_) //-,|
	//		|	   /____|_  /	\___// )_/
	//		|	   \____/ `^-___|___/ |
	//		|		 	 \/   \____/  /_-^-.
	//		|		     /   _-'     |___.  \_
	//		|			/ _-'		 /    `\ \\___
	//		|			`'\____~~+~^/      _)/    \____
	//		|			   \`----'	|   __/			  _)
	//		|			   /(		/~-'		   ,-' |
	//		|			  / `|		|			  /	   |
	//		|			 /	 (		)			 /     `)
	//		|			/	  `-==-'			 |      |
	//		|		   /	   /|				 |      |
	//		|		  /		  / \				 |      |
	//		|		 /		 /	 |	  			 |      |
	//		|		/		/	  \	  _____,.____|		|
	//		|	   / _	   /	   |<`____, ____,|		|
	//		|	  / / \_  /	  	 _ | <_____/	 |		)
	//		|	/  / ^/,^=-~---~' `z---..._______/	    |
	//		|--'  /	/| |/ .^ ,^\    \				    )
	//		|	  |_|| || |(_(  )   |				   |
	//		|	  \_/`-``-`----'___/_____	 	       |
	//		|___..---'			   _|____`-----..-----'\
	//		|_____________________|	@	|			    )
	// average coding session involving tile entities
	public static class TileEntityPistonInserter extends TileEntityLoadedBase implements IInventory, net.minecraft.util.ITickable {

		public ItemStack slot;

		public int extend; //why don't we just make all these ones serverside? we're never using them on the client anyway
		public static final int maxExtend = 25;
		public boolean isRetracting = true;
		public int delay;

		//prevents funkies from happening with block updates or loading into a server
		private boolean lastState;

		//when a fake animatorcel gives you something so 20fps you gotta hit him with the true interpolation stare
		@SideOnly(Side.CLIENT) public double renderExtend;
		@SideOnly(Side.CLIENT) public double lastExtend;
		@SideOnly(Side.CLIENT) private int syncExtend; //what are these for?
		@SideOnly(Side.CLIENT) private int turnProgress;

		public TileEntityPistonInserter() { }

		@Override
		public void update() {

			if(!this.world.isRemote) {

				if(delay <= 0) {

					if(this.isRetracting && this.extend > 0) {
						this.extend--;
					} else if(!this.isRetracting) {
						this.extend++;

						if(this.extend >= this.maxExtend) {
							com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.pressOperate", 1.0F, 1.5F);

							ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
							Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + dir.offsetX * 2, this.pos.getY() + dir.offsetY * 2, this.pos.getZ() + dir.offsetZ * 2).getBlock();

							if(b instanceof IInsertable && ((IInsertable) b).insertItem(this.world, this.pos.getX() + dir.offsetX * 2, this.pos.getY() + dir.offsetY * 2, this.pos.getZ() + dir.offsetZ * 2, dir, slot)) {
								this.decrStackSize(0, 1);
							}

							this.isRetracting = true;
							this.delay = 5;
						}
					}

				} else {
					delay--;
				}

				networkPackNT(25);

			} else {
				this.lastExtend = this.renderExtend;

				if(this.turnProgress > 0) {
					this.renderExtend += (this.syncExtend - this.renderExtend) / (double) this.turnProgress;
					this.turnProgress--;
				} else {
					this.renderExtend = this.syncExtend;
				}
			}

		}

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeInt(extend);

			buf.writeBoolean(this.slot != null);
			if(this.slot != null) {
				BufferUtil.writeNBT(buf, slot.getTagCompound());
			}
		}

		@Override
		public void deserialize(ByteBuf buf) {
			this.syncExtend = buf.readInt();

			if(buf.readBoolean()) {
				NBTTagCompound stack = BufferUtil.readNBT(buf);
				this.slot = ItemStack.loadItemStackFromNBT(stack);
			} else
				this.slot = null;

			this.turnProgress = 2;
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("extend", extend);
			nbt.setBoolean("retract", isRetracting);
			nbt.setBoolean("state", lastState); //saved so loading into a world doesn't cause issues
			if(this.slot != null) {
				NBTTagCompound stack = new NBTTagCompound();
				slot.writeToNBT(stack);
				nbt.setTag("stack", stack);
			}
			return nbt;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.extend = nbt.getInteger("extend");
			this.isRetracting = nbt.getBoolean("retract");
			this.lastState = nbt.getBoolean("state");
			if(nbt.hasKey("stack")) {
				NBTTagCompound stack = nbt.getCompoundTag("stack");
				this.slot = ItemStack.loadItemStackFromNBT(stack);
			} else {
				this.slot = null;
			}
		}

		@SideOnly(Side.CLIENT)
		private AxisAlignedBB aabb;

		@Override
		public AxisAlignedBB getRenderBoundingBox() {

			if(aabb != null)
				return aabb;

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
			aabb = AxisAlignedBB.getBoundingBox(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + 1, this.pos.getZ() + 1).addCoord(dir.offsetX, dir.offsetY, dir.offsetZ);
			return aabb;
		}

		/* BS inventory stuff */

		@Override public int getSizeInventory() { return 1; }

		@Override public ItemStack getStackInSlot(int slot) { return this.slot; }

		@Override
		public ItemStack decrStackSize(int slot, int amount) {
			if(this.slot != null) {
				if(this.slot.stackSize <= amount) {
					ItemStack stack = this.slot;
					this.slot = null;
					return stack;
				}

				ItemStack stack = this.slot.splitStack(amount);
				if(this.slot.stackSize == 0)
					this.slot = null;

				return stack;
			}

			return null;
		}

		@Override
		public ItemStack removeStackFromSlot(int slot) { return ItemStack.EMPTY; }

		@Override
		public void setInventorySlotContents(int slot, ItemStack stack) {
			if(stack != null && stack.isEmpty()) stack = null;
			this.slot = stack;
			if(stack != null && stack.getCount() > this.getInventoryStackLimit())
				stack.setCount(this.getInventoryStackLimit());
		}

		@Override public String getInventoryName() { return null; }

		@Override public boolean hasCustomInventoryName() { return false; }

		@Override public int getInventoryStackLimit() { return 1; }

		@Override public boolean isUseableByPlayer(EntityPlayer player) { return false; }

		@Override public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) {}

		@Override public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) {}

		@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }

	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
