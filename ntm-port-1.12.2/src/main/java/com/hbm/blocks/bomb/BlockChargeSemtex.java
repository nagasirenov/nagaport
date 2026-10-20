package com.hbm.blocks.bomb;

import java.util.List;

import com.hbm.config.GeneralConfig;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.particle.helper.ExplosionCreator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockChargeSemtex extends BlockChargeBase {

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {
		
		if(!world.isRemote) {
			safe = true;
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			safe = false;
			
			ExplosionVNT xnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 10F);
			xnt.setBlockAllocator(new BlockAllocatorStandard(32));
			xnt.setBlockProcessor(new BlockProcessorStandard()
					.setAllDrop()
					.setFortune(3));
			xnt.explode();
			ExplosionCreator.composeEffectSmall(world, x + 0.5, y + 1, z + 0.5);
			
			return BombReturnCode.DETONATED;
		}
		
		return BombReturnCode.UNDEFINED;
	}

	@Override
	public int getRenderType() {
		return BlockChargeC4.renderID;
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);
		list.add(TextFormatting.BLUE + "Will drop all blocks.");
		list.add(TextFormatting.BLUE + "Does not do damage.");
		list.add(TextFormatting.BLUE + "");
		list.add(TextFormatting.LIGHT_PURPLE + "Fortune III");
	}
	
}
