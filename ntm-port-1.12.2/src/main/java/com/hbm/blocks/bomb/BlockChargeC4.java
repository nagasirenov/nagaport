package com.hbm.blocks.bomb;

import java.util.List;

import com.hbm.main.MainRegistry;
import com.hbm.config.GeneralConfig;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.particle.helper.ExplosionCreator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockChargeC4 extends BlockChargeBase {
	/** Retained for legacy charge aliases; no Forge block render ID is allocated. */
	@Deprecated
	public static final int renderID = -1;

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {

		if(!world.isRemote) {
			safe = true;
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			safe = false;

			ExplosionVNT xnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 15F);
			xnt.setBlockAllocator(new BlockAllocatorStandard(32));
			xnt.setBlockProcessor(new BlockProcessorStandard().setNoDrop());
			xnt.setEntityProcessor(new EntityProcessorStandard());
			xnt.setPlayerProcessor(new PlayerProcessorStandard());
			xnt.explode();
			ExplosionCreator.composeEffectSmall(world, x + 0.5, y + 1, z + 0.5);

			return BombReturnCode.DETONATED;
		}

		return BombReturnCode.UNDEFINED;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);
		list.add(TextFormatting.BLUE + "Does not drop blocks.");
	}

}
