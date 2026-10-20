package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ChatBuilder;
import com.hbm.util.fauxpointtwelve.BlockPos;

import api.hbm.energymk2.IEnergyConductorMK2;
import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import api.hbm.energymk2.PowerNetMK2;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemPowerNetTool extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b instanceof BlockDummyable) {
			int[] pos = ((BlockDummyable) b).findCore(world, x, y, z);

			if(pos != null) {
				x = pos[0];
				y = pos[1];
				z = pos[2];
			}
		}

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(world.isRemote)
			return true;

		if((te instanceof IEnergyConductorMK2)) {
			PowerNode node = Nodespace.getNode(world, x, y, z);

			if(node != null && node.hasValidNet()) {

				PowerNetMK2 net = node.net;
				String id = Integer.toHexString(net.hashCode());
				player.sendMessage(ChatBuilder.start("Start of diagnostic for network " + id).color(TextFormatting.GOLD).flush());
				player.sendMessage(ChatBuilder.start("Links: " + net.links.size()).color(TextFormatting.YELLOW).flush());
				player.sendMessage(ChatBuilder.start("Providers: " + net.providerEntries.size()).color(TextFormatting.YELLOW).flush());
				player.sendMessage(ChatBuilder.start("Receivers: " + net.receiverEntries.size()).color(TextFormatting.YELLOW).flush());
				player.sendMessage(ChatBuilder.start("End of diagnostic for network " + id).color(TextFormatting.GOLD).flush());

				for(PowerNode link : net.links) {

					for(BlockPos pos : link.positions) {
						NBTTagCompound data = new NBTTagCompound();
						data.setString("type", "debug");
						data.setInteger("color", 0xffff00);
						data.setFloat("scale", 0.5F);
						data.setString("text", id);
						PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), radius));
					}
				}

			} else {
				player.sendMessage(ChatBuilder.start("Error: No network found!").color(TextFormatting.RED).flush());
			}

			return true;
		}

		return false;
	}

	private static final int radius = 20;

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add(TextFormatting.RED + "Right-click cable to analyze the power net.");
		list.add(TextFormatting.RED + "Links (cables, poles, etc.) are YELLOW");
		list.add(TextFormatting.RED + "Subscribers (any receiver) are BLUE");
		list.add(TextFormatting.RED + "Links with mismatching network info (BUGGED!) are RED");
		list.add(TextFormatting.RED + "Displays stats such as link and subscriber count");
		list.add(TextFormatting.RED + "Proxies are connection points for multiblock links (e.g. 4 for substations)");
		list.add(TextFormatting.RED + "Particles only spawn in a " + radius + " block radius!");
	}
}
