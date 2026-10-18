package com.hbm.items.tool;

import net.minecraft.util.math.RayTraceResult;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

public class BigSword extends ItemSword {

	public World worldObj;

	public double posX;
	public double posY;
	public double posZ;

	public BigSword(ToolMaterial p_i45356_1_) {
		super(p_i45356_1_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer Entityplayer) {

		return itemStack;
	}

	/*public void onUsingTick(ItemStack itemStack, EntityPlayer Entityplayer, int count) {
		World world = Entityplayer.worldObj;

		Vec3d look = Entityplayer.getLookVec();
		Random rand = new Random();
		RayTraceResult Coord = Library.rayTrace(Entityplayer, 300, 1);
		// EntityLightningBolt Lightning = new EntityLightningBolt(world, 1, 1,
		// 1);
		// Lightning.setPosition(Coord.getBlockPos().getX(),Coord.getBlockPos().getY(),Coord.getBlockPos().getZ());
		EntityPlasmaBeam plasma = new EntityPlasmaBeam(world, Entityplayer);
		plasma.setPosition(Coord.getBlockPos().getX() + (rand.nextGaussian() * 0.25D), Coord.getBlockPos().getY() + 1,
				Coord.getBlockPos().getZ() + (rand.nextGaussian() * 0.25D));
		if (!Entityplayer.worldObj.isRemote) {
			// world.spawnEntityInWorld(Lightning);
			world.setBlock(Coord.getBlockPos().getX(), Coord.getBlockPos().getY(), Coord.getBlockPos().getZ(), Blocks.ice);
			world.spawnEntityInWorld(plasma);
		}
		System.out.println(Coord.getBlockPos().getX() + ", " + Coord.getBlockPos().getY() + ", " + Coord.getBlockPos().getZ());
		Entityplayer.addPotionEffect(new PotionEffect(6, 20, 5));
		itemStack.damageItem(25, Entityplayer);
	}*/
}
