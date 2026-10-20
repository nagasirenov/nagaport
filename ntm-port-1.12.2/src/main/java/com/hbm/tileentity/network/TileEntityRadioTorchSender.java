package com.hbm.tileentity.network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.ForgeDirection;

public class TileEntityRadioTorchSender extends TileEntityRadioTorchBase {

	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
			BlockPos sourcePos = this.pos.add(dir.offsetX, dir.offsetY, dir.offsetZ);
			int input = this.world.getRedstonePower(sourcePos, dir.getOpposite().toEnumFacing());
			
			IBlockState sourceState = this.world.getBlockState(sourcePos);
			
			if(sourceState.hasComparatorInputOverride()) {
				input = sourceState.getComparatorInputOverride(this.world, sourcePos);
			}
			
			boolean shouldSend = this.polling;
			
			if(input != this.lastState) {
				this.markDirty();
				this.lastState = input;
				// Native notification may request the update packet immediately.
				IBlockState state = this.world.getBlockState(this.pos);
				this.world.notifyBlockUpdate(this.pos, state, state, 2);
				shouldSend = true;
			}

			if(shouldSend && !this.channel.isEmpty()) {
				RTTYSystem.broadcast(this.world, this.channel, this.customMap ? this.mapping[input] : (input + ""));
			}
		}
		
		super.update();
	}
}
