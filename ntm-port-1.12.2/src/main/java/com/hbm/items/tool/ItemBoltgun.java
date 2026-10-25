package com.hbm.items.tool;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.material.Mats;
import com.hbm.items.IAnimatedItem;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.anim.AnimationEnums.ToolAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.EntityDamageUtil;

import api.hbm.block.IToolable;
import api.hbm.block.IToolable.ToolType;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class ItemBoltgun extends Item implements IAnimatedItem<ToolAnimation> {

	public ItemBoltgun() {
		this.setMaxStackSize(1);
		this.setCreativeTab(MainRegistry.controlTab);

		ToolType.BOLT.register(new ItemStack(this));
	}

	@Override
	public Item setTranslationKey(String unlocalizedName) {
		super.setTranslationKey(unlocalizedName);
		com.hbm.render.item.NativeItemTextureRegistry.texture(this, RefStrings.MODID + ":"+ unlocalizedName);
		return this;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {

		World world = player.world;
		if(!entity.isEntityAlive()) return false;

		ItemStack[] bolts = new ItemStack[] { new ItemStack(ModItems.bolt_spike), Mats.MAT_STEEL.make(ModItems.bolt), Mats.MAT_TUNGSTEN.make(ModItems.bolt), Mats.MAT_DURA.make(ModItems.bolt)};

		for(ItemStack bolt : bolts) {
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack slot = player.inventory.getStackInSlot(i);

				if(slot != null) {
					if(slot.getItem() == bolt.getItem() && slot.getItemDamage() == bolt.getItemDamage()) {
						if(!world.isRemote) {
							world.playSound(null, entity.posX, entity.posY, entity.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.boltgun"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
							player.inventory.decrStackSize(i, 1);
							player.inventoryContainer.detectAndSendChanges();
							EntityDamageUtil.attackEntityFromIgnoreIFrame(entity, DamageSource.causePlayerDamage(player).setDamageBypassesArmor(), 10F);

							if(!entity.isEntityAlive() && entity instanceof EntityPlayer) {
								com.hbm.advancement.NtmAdvancements.award(((EntityPlayer) entity), MainRegistry.achGoFish);
							}

							NBTTagCompound data = new NBTTagCompound();
							data.setString("type", "vanillaExt");
							data.setString("mode", "largeexplode");
							data.setFloat("size", 1F);
							data.setByte("count", (byte)1);
							PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, entity.posX, entity.posY + entity.height / 2 - entity.getYOffset(), entity.posZ), new TargetPoint(world.provider.getDimension(), entity.posX, entity.posY, entity.posZ, 50));

							playAnimation(player, ToolAnimation.SWING);
						}

						return true;
					}
				}
			}
		}

		return false;
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b instanceof IToolable && ((IToolable)b).onScrew(world, player, x, y, z, side, fX, fY, fZ, ToolType.BOLT)) {

			if(!world.isRemote) {

				world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.boltgun"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				player.inventoryContainer.detectAndSendChanges();
				ForgeDirection dir = ForgeDirection.getOrientation(side);
				double off = 0.25;

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "largeexplode");
				data.setFloat("size", 1F);
				data.setByte("count", (byte)1);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x + fX + dir.offsetX * off, y + fY + dir.offsetY * off, z + fZ + dir.offsetZ * off), new TargetPoint(world.provider.getDimension(), x, y, z, 50));

				playAnimation(player, ToolAnimation.SWING);
			}

			return false;
		}

		return false;
	}

	@Override
	public Class<ToolAnimation> getEnum() {
		return ToolAnimation.class;
	}

	@Override
	public BusAnimation getAnimation(ToolAnimation type, ItemStack stack) {
		return new BusAnimation()
				.addBus("RECOIL", new BusAnimationSequence()
						.addPos(1, 0, 1, 50)
						.addPos(0, 0, 1, 100));
	}

	@Override
	public boolean shouldPlayerModelAim(ItemStack stack) {
		return false;
	}

}
