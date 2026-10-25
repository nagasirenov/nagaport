package com.hbm.tileentity.machine;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.tileentity.network.RTTYSystem.RTTYChannel;
import com.hbm.util.BufferUtil;
import com.hbm.util.NoteBuilder;
import com.hbm.util.NoteBuilder.Instrument;
import com.hbm.util.NoteBuilder.Note;
import com.hbm.util.NoteBuilder.Octave;
import com.hbm.util.Tuple.Triplet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityRadioRec extends TileEntityLoadedBase implements IControlReceiver, net.minecraft.util.ITickable {

	public String channel = "";
	public boolean isOn = false;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.isOn && !this.channel.isEmpty()) {
				RTTYChannel chan = RTTYSystem.listen(this.world, this.channel);

				if(chan != null && chan.timeStamp == this.world.getTotalWorldTime() - 1) {
					Triplet<Instrument, Note, Octave>[] notes = NoteBuilder.translate(chan.signal + "");

					for(Triplet<Instrument, Note, Octave> note : notes) {
						Instrument i = note.getX();
						Note n = note.getY();
						Octave o = note.getZ();

						int noteId = n.ordinal() + o.ordinal() * 12;
						String s = "harp";

						if(i == Instrument.BASSDRUM) s = "bd";
						if(i == Instrument.SNARE) s = "snare";
						if(i == Instrument.CLICKS)  s = "hat";
						if(i == Instrument.BASSGUITAR)  s = "bassattack";

						float f = (float)Math.pow(2.0D, (double)(noteId - 12) / 12.0D);
						this.world.playSound(null, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, com.hbm.main.ModSoundEvents.resolve("note." + s), net.minecraft.util.SoundCategory.BLOCKS, 3.0F, f);
					}
				}
			}

			networkPackNT(15);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		BufferUtil.writeString(buf, this.channel);
		buf.writeBoolean(this.isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.channel = BufferUtil.readString(buf);
		this.isOn = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		channel = nbt.getString("channel");
		isOn = nbt.getBoolean("isOn");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setString("channel", channel);
		nbt.setBoolean("isOn", isOn);
		return nbt;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return player.getDistance(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) < 16D;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("channel")) this.channel = data.getString("channel");
		if(data.hasKey("isOn")) this.isOn = data.getBoolean("isOn");

		this.markDirty();
	}
}
