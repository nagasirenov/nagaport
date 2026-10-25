package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemMS extends Item {

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag)
	{
		list.add("Lost but not forgotten");
	}

        @Override
    public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
        net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
        int side = facing != null ? facing.getIndex() : 0;
        return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
    }

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int i, float a, float b, float c)
    {
    	if(!world.isRemote) {
    		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.ntm_dirt) {

				com.hbm.world.WorldBlockDestruction.destroy(world, x, y, z, false);

    	    	Random rand = new Random();
    	    	List<ItemStack> list = new ArrayList<ItemStack>();

    	    	list.add(new ItemStack(ModItems.ingot_u238m2, 1, 1));
    	    	list.add(new ItemStack(ModItems.ingot_u238m2, 1, 2));
    	    	list.add(new ItemStack(ModItems.ingot_u238m2, 1, 3));

    	    	for(ItemStack sta : list) {
    	            float f = rand.nextFloat() * 0.8F + 0.1F;
    	            float f1 = rand.nextFloat() * 0.8F + 0.1F;
    	            float f2 = rand.nextFloat() * 0.8F + 0.1F;
    	            EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, sta);

    	            float f3 = 0.05F;
    	            entityitem.motionX = (float)rand.nextGaussian() * f3;
    	            entityitem.motionY = (float)rand.nextGaussian() * f3 + 0.2F;
    	            entityitem.motionZ = (float)rand.nextGaussian() * f3;
    	            if(!world.isRemote)
    	            	world.spawnEntity(entityitem);
    	    	}
    			return true;
    		}
    	}

        return false;
    }

}
