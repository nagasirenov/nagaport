package com.hbm.tileentity.deco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.TrappedBrick.Trap;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.items.ModItems;

import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityTrappedBrick extends TileEntity implements net.minecraft.util.ITickable {

	AxisAlignedBB detector = null;
	ForgeDirection dir = ForgeDirection.UNKNOWN;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(detector == null) {
				setDetector();
			}

			List players = this.world.getEntitiesWithinAABB(EntityPlayer.class, detector);

			if(!players.isEmpty())
				trigger();
		}
	}

	@SuppressWarnings("incomplete-switch")
	private void trigger() {

		Trap trap = Trap.get(this.getBlockMetadata());

		switch(trap) {
		case FALLING_ROCKS:
			for(int x = 0; x < 3; x++) {
				for(int z = 0; z < 3; z++) {
					EntityRubble rubble = new EntityRubble(this.world, this.pos.getX() - 0.5 + x, this.pos.getY() - 0.5, this.pos.getZ() - 0.5 + z);
					rubble.setMetaBasedOnBlock(ModBlocks.reinforced_stone, 0);
					this.world.spawnEntity(rubble);
				}
			}
			break;
		case ARROW:
			EntityArrow arrow = new net.minecraft.entity.projectile.EntityTippedArrow(this.world);
			arrow.setPosition(this.pos.getX() + 0.5 + dir.offsetX, this.pos.getY() + 0.5, this.pos.getZ() + 0.5 + dir.offsetZ);
			arrow.motionX = dir.offsetX;
			arrow.motionZ = dir.offsetZ;
			this.world.spawnEntity(arrow);
			break;
		case FLAMING_ARROW:
			EntityArrow farrow = new net.minecraft.entity.projectile.EntityTippedArrow(this.world);
			farrow.setPosition(this.pos.getX() + 0.5 + dir.offsetX, this.pos.getY() + 0.5, this.pos.getZ() + 0.5 + dir.offsetZ);
			farrow.motionX = dir.offsetX;
			farrow.motionZ = dir.offsetZ;
			farrow.setFire(60);
			this.world.spawnEntity(farrow);
			break;
		case PILLAR:
			for(int i = 0; i < 3; i++)
				com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, this.pos.getX(), this.pos.getY() - 1 - i, this.pos.getZ(), ModBlocks.concrete_pillar);
			break;
		case POISON_DART:
			//TBI
			break;
		case ZOMBIE:
			EntityZombie zombie = new EntityZombie(this.world);
			zombie.setPosition(this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5);
			switch(this.world.rand.nextInt(3)) {
			case 0: zombie.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.chernobylsign)); break;
			case 1: zombie.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.cobalt_sword)); break;
			case 2: zombie.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.cmb_hoe)); break;
			}
			zombie.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 1.0F);
			this.world.spawnEntity(zombie);
			break;
		case SPIDERS:
			for(int i = 0; i < 3; i++) {
				EntityCaveSpider spider = new EntityCaveSpider(this.world);
				spider.setPosition(this.pos.getX() + 0.5, this.pos.getY() - 1, this.pos.getZ() + 0.5);
				this.world.spawnEntity(spider);
			}
			break;
		}

		com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, "random.click", 0.3F, 0.6F);
		{ net.minecraft.world.World nativeRegisteredWriteWorld = this.world; int nativeRegisteredWriteX = this.pos.getX(); int nativeRegisteredWriteY = this.pos.getY(); int nativeRegisteredWriteZ = this.pos.getZ(); net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.brick_jungle; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
	}

	@SuppressWarnings("incomplete-switch")
	private void setDetector() {

		Trap trap = Trap.get(this.getBlockMetadata());

		switch(trap) {
		case FALLING_ROCKS:
			detector = new AxisAlignedBB(this.pos.getX() - 1, this.pos.getY() - 3, this.pos.getZ() - 1, this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() + 2);
			return;

		case PILLAR:
			detector = new AxisAlignedBB(this.pos.getX() + 0.2, this.pos.getY() - 3, this.pos.getZ() + 0.2, this.pos.getX() + 0.8, this.pos.getY(), this.pos.getZ() + 0.8);
			return;

		case ARROW:
		case FLAMING_ARROW:
		case POISON_DART:
			setDetectorDirectional();
			return;

		case ZOMBIE:
			detector = new AxisAlignedBB(this.pos.getX() - 1, this.pos.getY() + 1, this.pos.getZ() - 1, this.pos.getX() + 2, this.pos.getY() + 2, this.pos.getZ() + 2);
			return;

		case SPIDERS:
			detector = new AxisAlignedBB(this.pos.getX() - 1, this.pos.getY() - 3, this.pos.getZ() - 1, this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() + 2);
			return;
		}

		detector = new AxisAlignedBB(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + 1, this.pos.getZ() + 1);
	}

	private void setDetectorDirectional() {

		List<ForgeDirection> dirs = new ArrayList() {{
			add(ForgeDirection.NORTH);
			add(ForgeDirection.SOUTH);
			add(ForgeDirection.EAST);
			add(ForgeDirection.WEST);
		}};

		Collections.shuffle(dirs);

		for(ForgeDirection dir : dirs) {

			if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ).getBlock() == Blocks.AIR) {

				double minX = this.pos.getX() + 0.4;
				double minY = this.pos.getY() + 0.4;
				double minZ = this.pos.getZ() + 0.4;
				double maxX = this.pos.getX() + 0.6;
				double maxY = this.pos.getY() + 0.6;
				double maxZ = this.pos.getZ() + 0.6;

				if(dir.offsetX > 0)
					maxX += 3;
				else if(dir.offsetX < 0)
					minX -= 3;

				if(dir.offsetZ > 0)
					maxZ += 3;
				else if(dir.offsetZ < 0)
					minZ -= 3;

				detector = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
				this.dir = dir;
				return;
			}
		}

		detector = new AxisAlignedBB(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + 1, this.pos.getZ() + 1);
	}
}
