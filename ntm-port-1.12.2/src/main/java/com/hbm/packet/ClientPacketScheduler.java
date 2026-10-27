package com.hbm.packet;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientPacketScheduler {

	private ClientPacketScheduler() {
	}

	public static void schedule(Runnable task) {
		Minecraft.getMinecraft().addScheduledTask(task);
	}
}
