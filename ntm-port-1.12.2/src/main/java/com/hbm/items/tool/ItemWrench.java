package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityPipelineBase;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemWrench extends ItemSword {

	public ItemWrench(ToolMaterial mat) {
		super(mat);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {

		if(!player.isSneaking()) {
			
			Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
			
			if(b instanceof BlockDummyable) {
				int[] core = ((BlockDummyable)b).findCore(world, x, y, z);
				
				if(core != null) {
					x = core[0];
					y = core[1];
					z = core[2];
				}
			}
			
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(te != null && te instanceof TileEntityPipelineBase) {

				if(stack.getTagCompound() == null) {
					stack.setTagCompound(new NBTTagCompound());

					stack.getTagCompound().setInteger("x", x);
					stack.getTagCompound().setInteger("y", y);
					stack.getTagCompound().setInteger("z", z);

					if(!world.isRemote) {
						player.sendMessage(new TextComponentString("Pipe start"));
					}
				} else if(!world.isRemote) {

					int x1 = stack.getTagCompound().getInteger("x");
					int y1 = stack.getTagCompound().getInteger("y");
					int z1 = stack.getTagCompound().getInteger("z");

					if(world.getTileEntity(new net.minecraft.util.math.BlockPos(x1, y1, z1)) instanceof TileEntityPipelineBase) {

						TileEntityPipelineBase first = (TileEntityPipelineBase) world.getTileEntity(new net.minecraft.util.math.BlockPos(x1, y1, z1));
						TileEntityPipelineBase second = ((TileEntityPipelineBase) te);
						
						switch (TileEntityPipelineBase.canConnect(first, second)) {
							case 0:
								first.addConnection(x, y, z);
								second.addConnection(x1, y1, z1);
								player.sendMessage(new TextComponentString("Pipe end"));
								break;
							case 1: player.sendMessage(new TextComponentString("Pipe error - Pipes are not the same type")); break;
							case 2: player.sendMessage(new TextComponentString("Pipe error - Cannot connect to the same pipe anchor")); break;
							case 3: player.sendMessage(new TextComponentString("Pipe error - Pipe anchor is too far away")); break;
							case 4: player.sendMessage(new TextComponentString("Pipe error - Pipe anchor fluid types do not match")); break;
						}
						
						stack.setTagCompound(null);

					} else {

						player.sendMessage(new TextComponentString("Pipe error"));
						stack.setTagCompound(null);
					}
				}

				player.swingItem();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase entityPlayer) {
		World world = entity.world;

		Vec3d vec = entityPlayer.getLookVec();

		double dX = vec.x * 0.5;
		double dY = vec.y * 0.5;
		double dZ = vec.z * 0.5;

		entity.motionX += dX;
		entity.motionY += dY;
		entity.motionZ += dZ;
		world.playSoundAtEntity(entity, "random.anvil_land", 3.0F, 0.75F);
		
		return false;
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if(itemstack.getTagCompound() != null) {
			list.add("Pipe start x: " + itemstack.getTagCompound().getInteger("x"));
			list.add("Pipe start y: " + itemstack.getTagCompound().getInteger("y"));
			list.add("Pipe start z: " + itemstack.getTagCompound().getInteger("z"));
		} else {
			list.add("Right-click anchor to connect");
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inhand) {

		if(world.isRemote) {
			if(stack.getTagCompound() != null) {
				Vec3d vec = new Vec3d(
						entity.posX - stack.getTagCompound().getInteger("x"),
						entity.posY - stack.getTagCompound().getInteger("y"),
						entity.posZ - stack.getTagCompound().getInteger("z"));
				
				MainRegistry.proxy.displayTooltip(stack.getDisplayName() + ": " + ((int) vec.length()) + "m", MainRegistry.proxy.ID_WRENCH);
			}
		}
	}
}
