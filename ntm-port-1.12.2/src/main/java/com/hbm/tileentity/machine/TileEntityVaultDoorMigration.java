package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDoorGeneric;

import net.minecraft.tileentity.TileEntity;
import com.hbm.lib.ForgeDirection;

public class TileEntityVaultDoorMigration extends TileEntity implements net.minecraft.util.ITickable {

	@Override
	public void update() {

		if(!this.world.isRemote) {

			int meta = getBlockMetadata();
			if(meta <= 5) {
				com.hbm.world.WorldBlockState.writeLegacyBlock(getWorld(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), ModBlocks.vault_door, meta + 10, 3);
				((BlockDoorGeneric) ModBlocks.vault_door).fillSpace(getWorld(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), ForgeDirection.getOrientation(meta), 0);
			}
		}
	}
}
