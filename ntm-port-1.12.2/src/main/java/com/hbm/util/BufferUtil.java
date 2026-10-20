package com.hbm.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class BufferUtil {

	private static final Charset CHARSET = StandardCharsets.UTF_8;

	// Writes a string to a byte buffer by encoding the length and raw bytes
	public static void writeString(ByteBuf buf, String value) {
		if(value == null) {
			buf.writeInt(-1);
			return;
		}
		
		final byte[] bytes = value.getBytes(CHARSET);
		buf.writeInt(bytes.length);
		buf.writeBytes(bytes);
	}

	// Reads a string from a byte buffer via the written length and raw bytes
	public static String readString(ByteBuf buf) {
		final int count = buf.readInt();
		if(count < 0) return null;

		final byte[] bytes = new byte[count];
		buf.readBytes(bytes);

		return new String(bytes, CHARSET);
	}

	/**
	 * Writes an integer array to a buffer.
	 */
	public static void writeIntArray(ByteBuf buf, int[] array) {
		buf.writeInt(array.length);
		for (int value : array) {
			buf.writeInt(value);
		}
	}

	/**
	 * Reads an integer array from a buffer.
	 */
	public static int[] readIntArray(ByteBuf buf) {
		int length = buf.readInt();

		int[] array = new int[length];

		for (int i = 0; i < length; i++) {
			array[i] = buf.readInt();
		}

		return array;
	}

	/**
	 * Writes a vector to a buffer.
	 */
	public static void writeVec3(ByteBuf buf, Vec3d vector) {
		buf.writeBoolean(vector != null);
		if(vector == null) return;
		buf.writeDouble(vector.x);
		buf.writeDouble(vector.y);
		buf.writeDouble(vector.z);
	}

	/**
	 * Reads a vector from a buffer.
	 */
	public static Vec3d readVec3(ByteBuf buf) {
		boolean vectorExists = buf.readBoolean();
		if(!vectorExists) {
			return null;
		}
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();

		return new Vec3d(x, y, z);
	}

	/**
	 * Writes a NBTTagCompound to a buffer.
	 */
	public static void writeNBT(ByteBuf buf, NBTTagCompound compound) {
		if(compound != null) {
			byte[] nbtData = new byte[0];
			try {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				CompressedStreamTools.writeCompressed(compound, output);
				nbtData = output.toByteArray();
			} catch(IOException e) {
				e.printStackTrace();
			}
			buf.writeShort((short) nbtData.length);
			buf.writeBytes(nbtData);
		} else
			buf.writeShort(-1);
	}

	/**
	 * Reads a NBTTagCompound from a buffer.
	 */
	public static NBTTagCompound readNBT(ByteBuf buf) {
		short nbtLength = buf.readShort();

		if (nbtLength == -1) // check if no compound was even given.
			return new NBTTagCompound();
		byte[] tags = new byte[nbtLength];
		buf.readBytes(tags);
		try {
			// readCompressed uses an unlimited tracker in 1.12; retain the old 2 MiB limit.
			try(DataInputStream input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(tags))))) {
				return CompressedStreamTools.read(input, new NBTSizeTracker(2097152L));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return new NBTTagCompound();
	}

	/**
	 * Writes the ItemStack to the buffer.
	 */
	public static void writeItemStack(ByteBuf buf, ItemStack item) {
		// Native empty stacks have no publicly readable legacy item/count identity.
		if (item == null || item.isEmpty())
			buf.writeShort(-1);
		else {
			buf.writeShort(Item.getIdFromItem(item.getItem()));
			buf.writeByte(item.getCount());
			buf.writeShort(item.getItemDamage());
			NBTTagCompound nbtTagCompound = null;

			if (item.getItem().isDamageable() || item.getItem().getShareTag())
				nbtTagCompound = item.getTagCompound();

			writeNBT(buf, nbtTagCompound);
		}
	}

	/**
	 * Reads an ItemStack from a buffer.
	 */
	public static ItemStack readItemStack(ByteBuf buf) {
		ItemStack item = null;
		short id = buf.readShort();

		if (id >= 0) {
			byte quantity = buf.readByte();
			short meta = buf.readShort();
			item = new ItemStack(Item.getItemById(id), quantity, meta);
			item.setTagCompound(readNBT(buf));
		}
		return item;
	}
}
