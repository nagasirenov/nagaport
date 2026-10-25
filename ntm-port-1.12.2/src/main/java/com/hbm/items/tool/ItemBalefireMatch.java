package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBalefireMatch extends Item {

    public ItemBalefireMatch() {
        this.maxStackSize = 1;
        this.setMaxDamage(256);
        this.setCreativeTab(CreativeTabs.TOOLS);
    }

        @Override
    public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
        net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
        int side = facing != null ? facing.getIndex() : 0;
        return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
    }

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {

        if (side == 0)
            --y;

        if (side == 1)
            ++y;

        if (side == 2)
            --z;

        if (side == 3)
            ++z;

        if (side == 4)
            --x;

        if (side == 5)
            ++x;

        if (!player.canPlayerEdit(new net.minecraft.util.math.BlockPos(x, y, z), net.minecraft.util.EnumFacing.byIndex(side), stack)) {
            return false;
        } else {

            if (world.isAirBlock(new net.minecraft.util.math.BlockPos(x, y, z))) {
                com.hbm.sound.VanillaWorldSoundEffects.play(world, (double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
                com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.balefire);
            }

            stack.damageItem(1, player);
            return true;
        }
    }
}
