package com.hbm.items.tool;

import java.util.List;

import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IBomb.BombReturnCode;
import com.hbm.interfaces.IHoldableWeapon;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.ChatBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemLaserDetonator extends Item implements IHoldableWeapon {

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add("Aim & click to detonate!");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		
		RayTraceResult pos = Library.rayTrace(player, 500, 1);
		int x = pos.getBlockPos().getX();
		int y = pos.getBlockPos().getY();
		int z = pos.getBlockPos().getZ();

		if(!world.isRemote) {
			if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() instanceof IBomb) {
				BombReturnCode ret = ((IBomb) com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock()).explode(world, x, y, z);

				if(GeneralConfig.enableExtendedLogging)
					MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");
				
				world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);
				PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("").nextTranslation(ret.getUnlocalizedMessage()).color(ret.wasSuccessful() ? TextFormatting.YELLOW : TextFormatting.RED).flush(), MainRegistry.proxy.ID_DETONATOR), (EntityPlayerMP) player);
				
			} else {
				world.playSoundAtEntity(player, "hbm:item.techBoop", 1.0F, 1.0F);
				PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("").nextTranslation(BombReturnCode.ERROR_NO_BOMB.getUnlocalizedMessage()).color(TextFormatting.RED).flush(), MainRegistry.proxy.ID_DETONATOR), (EntityPlayerMP) player);
			}
		} else {
			
			Vec3d vec = new Vec3d(x + 0.5 - player.posX, y + 0.5 - player.posY, z + 0.5 - player.posZ);
			double len = Math.min(vec.length(), 15D);
			vec = vec.normalize();
			
			for(int i = 0; i < len; i++) {
				double rand = world.rand.nextDouble() * len + 3;
				world.spawnParticle("reddust", player.posX + vec.x * rand, player.posY + vec.y * rand, player.posZ + vec.z * rand, 0, 0, 0);
			}
		}

		return stack;
	}

	@Override
	public Crosshair getCrosshair() {
		return Crosshair.L_ARROWS;
	}
}
