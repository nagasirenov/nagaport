package com.hbm.items.tool;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemMatch extends Item {


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_, int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_)
    {
        if (p_77648_3_.isRemote)
        {
            return true;
        }
        else
        {
            if (p_77648_7_ == 0)
            {
                --p_77648_5_;
            }

            if (p_77648_7_ == 1)
            {
                ++p_77648_5_;
            }

            if (p_77648_7_ == 2)
            {
                --p_77648_6_;
            }

            if (p_77648_7_ == 3)
            {
                ++p_77648_6_;
            }

            if (p_77648_7_ == 4)
            {
                --p_77648_4_;
            }

            if (p_77648_7_ == 5)
            {
                ++p_77648_4_;
            }

            if (!p_77648_2_.canPlayerEdit(new net.minecraft.util.math.BlockPos(p_77648_4_, p_77648_5_, p_77648_6_), net.minecraft.util.EnumFacing.byIndex(p_77648_7_), p_77648_1_))
            {
                return false;
            }
            else
            {
                if (com.hbm.world.WorldBlockState.read(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_).getBlock().getDefaultState().getMaterial() == Material.AIR)
                {
                    com.hbm.sound.VanillaWorldSoundEffects.play(p_77648_3_, p_77648_4_ + 0.5D, p_77648_5_ + 0.5D, p_77648_6_ + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
                    com.hbm.world.WorldBlockState.write(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_, Blocks.FIRE.getDefaultState(), 3);
                }

                if (!p_77648_2_.capabilities.isCreativeMode)
                {
                    p_77648_1_.shrink(1);
                }

                return true;
            }
        }
    }

}
