package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.turret.TurretBase;
import com.hbm.lib.Library;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;

public class ItemTurretControl extends Item {

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean bool) {
		
		if(entity instanceof EntityPlayer) {
			
			EntityPlayer player = (EntityPlayer) entity;

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && stack != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).equals(stack)) {
				
				if(stack.hasTagCompound()) {
					int x = stack.getTagCompound().getInteger("xCoord");
					int y = stack.getTagCompound().getInteger("yCoord");
					int z = stack.getTagCompound().getInteger("zCoord");

					TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

					if(te != null && te instanceof TileEntityTurretBaseNT) {
						TileEntityTurretBaseNT turret = (TileEntityTurretBaseNT) te;
						
						RayTraceResult pos = Library.rayTrace(player, 200, 1, true, true, false);
						
						if(pos == null)
							pos = Library.rayTrace(player, 200, 1);
						
						if(pos != null) { 
							
							Vec3d vecOrigin = new Vec3d(player.posX, player.posY + player.eyeHeight - player.getYOffset(), player.posZ);
							Vec3d vecDestination = new Vec3d(pos.getBlockPos().getX(), pos.getBlockPos().getY(), pos.getBlockPos().getZ());
							
							//List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.addCoord(pos.getBlockPos().getX(), pos.getBlockPos().getY(), pos.getBlockPos().getZ()).expand(1.0, 1.0, 1.0));
							List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().grow(200, 200, 200));
							
							for(Entity e : list) {

								if (e.canBeCollidedWith() && e != player) {
									
									RayTraceResult mop = e.getEntityBoundingBox().grow(0.2, 0.2, 0.2).calculateIntercept(vecOrigin, vecDestination);
									
									if(mop != null) {
										
										pos = new RayTraceResult(e, mop.hitVec);
									}
								}
							}
							
							if(pos.typeOfHit == RayTraceResult.Type.ENTITY) {
								turret.target = pos.entityHit;
								turret.turnTowards(turret.getEntityPos(pos.entityHit));
								
							} else {
								turret.turnTowards(new Vec3d(pos.getBlockPos().getX() + 0.5, pos.getBlockPos().getY() + 0.5, pos.getBlockPos().getZ() + 0.5));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if(itemstack.getTagCompound() != null) {
			list.add("Linked to:");
			list.add("X: " + String.valueOf(itemstack.getTagCompound().getInteger("xCoord")));
			list.add("Y: " + String.valueOf(itemstack.getTagCompound().getInteger("yCoord")));
			list.add("Z: " + String.valueOf(itemstack.getTagCompound().getInteger("zCoord")));
		} else {
			list.add("Please select a turret.");
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int fx, float fy, float fz, float f0) {
		
		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() instanceof TurretBase) {
			if(stack.getTagCompound() == null) {
				stack.setTagCompound(new NBTTagCompound());
			}
			
			stack.getTagCompound().setInteger("xCoord", x);
			stack.getTagCompound().setInteger("yCoord", y);
			stack.getTagCompound().setInteger("zCoord", z);
			
			if(!world.isRemote) {
				player.sendMessage(new TextComponentString("Turret Linked!"));
			}

			world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);
			return true;
		}
		
		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() instanceof BlockDummyable) {
			
			int[] pos = ((BlockDummyable)com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock()).findCore(world, x, y, z);
			
			if(pos == null)
				return false;

			int ix = pos[0];
			int iy = pos[1];
			int iz = pos[2];
			
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(ix, iy, iz));
			
			if(te instanceof TileEntityTurretBaseNT) {
				
				if(stack.getTagCompound() == null) {
					stack.setTagCompound(new NBTTagCompound());
				}
				
				stack.getTagCompound().setInteger("xCoord", ix);
				stack.getTagCompound().setInteger("yCoord", iy);
				stack.getTagCompound().setInteger("zCoord", iz);
				
				if(!world.isRemote) {
					player.sendMessage(new TextComponentString("Turret Linked!"));
				}

				world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);
				return true;
			}
		}

		return false;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
		return EnumAction.bow;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int i) {

		int j = this.getMaxItemUseDuration(stack) - i;
		ArrowLooseEvent event = new ArrowLooseEvent(player, stack, j);
		MinecraftForge.EVENT_BUS.post(event);

		j = event.getCharge();

		if(stack.hasTagCompound()) {
			/// ///
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack p_77626_1_) {
		return 72000;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_) {
		ArrowNockEvent event = new ArrowNockEvent(p_77659_3_, p_77659_1_);
		MinecraftForge.EVENT_BUS.post(event);
		{
			p_77659_3_.setItemInUse(p_77659_1_, this.getMaxItemUseDuration(p_77659_1_));
		}

		return p_77659_1_;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		World world = player.world;

		if(stack.hasTagCompound()) {
			
			int x = stack.getTagCompound().getInteger("xCoord");
			int y = stack.getTagCompound().getInteger("yCoord");
			int z = stack.getTagCompound().getInteger("zCoord");

			if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() instanceof TurretBase) {

				/// ///
			}

			if(world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z)) instanceof TileEntityTurretBaseNT) {
				
				TileEntityTurretBaseNT turret = (TileEntityTurretBaseNT) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
				
				turret.manualSetup();
				if(!world.isRemote)
					turret.updateFiringTick();
			}
		}
	}
}
