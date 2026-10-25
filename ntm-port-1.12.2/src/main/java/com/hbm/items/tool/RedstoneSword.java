package com.hbm.items.tool;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

public class RedstoneSword extends ItemSword {

	//Pridenauer you damn bastard.

	public RedstoneSword(ToolMaterial p_i45356_1_) {
		super(p_i45356_1_);
	}

	@Override
	@SideOnly(Side.CLIENT)
    public boolean isFull3D()
    {
        return true;
    }


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_)
    {
        if (p_77648_7_ == 0)
        {
            --y;
        }

        if (p_77648_7_ == 1)
        {
            ++y;
        }

        if (p_77648_7_ == 2)
        {
            --z;
        }

        if (p_77648_7_ == 3)
        {
            ++z;
        }

        if (p_77648_7_ == 4)
        {
            --x;
        }

        if (p_77648_7_ == 5)
        {
            ++x;
        }

        if (!player.canPlayerEdit(new net.minecraft.util.math.BlockPos(x, y, z), net.minecraft.util.EnumFacing.byIndex(p_77648_7_), itemStack))
        {
            return false;
        }
        else
        {
            if (world.isAirBlock(new net.minecraft.util.math.BlockPos(x, y, z)))
            {
                com.hbm.sound.VanillaWorldSoundEffects.play(world, x + 0.5D, y + 0.5D, z + 0.5D, "random.break", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
                com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, Blocks.REDSTONE_WIRE);
            }

            itemStack.damageItem(14, player);
            return true;
        }
    }
}
