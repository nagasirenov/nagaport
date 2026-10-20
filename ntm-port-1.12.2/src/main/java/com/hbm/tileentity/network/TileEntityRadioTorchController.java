package com.hbm.tileentity.network;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.RTTYSystem.RTTYChannel;
import com.hbm.util.BufferUtil;
import com.hbm.util.Compat;

import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.RORFunctionException;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import com.hbm.lib.ForgeDirection;

public class TileEntityRadioTorchController extends TileEntityLoadedBase implements IControlReceiver, net.minecraft.util.ITickable {

    @Override public boolean canRenderBreaking() {
        return getWorld() != null && com.hbm.blocks.network.RadioTorchBase.isNativeBreakingOwner(this, getWorld().getBlockState(getPos()));
    }


	public String channel = "";
	public String prev;
	public boolean polling = false;

	@Override
	public void update() {

		if(!this.world.isRemote) {
			
			if(channel != null && !channel.isEmpty()) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
				
				TileEntity tile = Compat.getTileStandard(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ);
				
				if(tile instanceof IRORInteractive) {
					IRORInteractive ror = (IRORInteractive) tile;
					
					RTTYChannel chan = RTTYSystem.listen(this.world, channel);
					if(chan != null) {
						String rec = "" + chan.signal;
						if("selfdestruct".equals(rec)) {
							this.world.destroyBlock(this.pos, false);
							ExplosionVNT vnt = new ExplosionVNT(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, 5, null);
							vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 50).setupPiercing(5F, 0.5F));
							vnt.setPlayerProcessor(new PlayerProcessorStandard());
							vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
							vnt.explode();
							return;
						}
						if(this.polling || !rec.equals(prev)) {
							try {
								if(rec != null && !rec.isEmpty()) ror.runRORFunction(IRORInteractive.PREFIX_FUNCTION + IRORInteractive.getCommand(rec), IRORInteractive.getParams(rec));
							} catch(RORFunctionException ex) { }
							prev = rec;
						}
					}
				}
			}

			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(this.polling);
		BufferUtil.writeString(buf, channel);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.polling = buf.readBoolean();
		channel = BufferUtil.readString(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.polling = nbt.getBoolean("p");
		channel = nbt.getString("c");
		this.prev = nbt.getString("prev");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("p", polling);
		nbt.setString("c", channel);
		if(prev != null) nbt.setString("prev", prev);
		return nbt;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("p")) this.polling = data.getBoolean("p");
		if(data.hasKey("c")) channel = data.getString("c");
		
		this.markDirty();
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return player.getDistance(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) < 16D;
	}
}
