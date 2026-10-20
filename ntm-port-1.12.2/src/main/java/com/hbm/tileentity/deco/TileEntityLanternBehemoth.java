package com.hbm.tileentity.deco;

import java.util.ArrayList;
import java.util.List;

import com.hbm.entity.missile.EntityBobmazon;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemCircuit.EnumCircuitType;
import com.hbm.items.special.ItemKitCustom;
import com.hbm.tileentity.IRepairable;

import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityLanternBehemoth extends TileEntityLoadedBase implements IRepairable, net.minecraft.util.ITickable {

	public boolean isBroken = false;
	public int comTimer = -1;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(comTimer == 360) com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.hornNearSingle", 10F, 1F);
			if(comTimer == 280) com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.hornFarSingle", 10000F, 1F);
			if(comTimer == 220) com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.hornNearDual", 10F, 1F);
			if(comTimer == 100) com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.hornFarDual", 10000F, 1F);

			if(comTimer == 0) {
				List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.pos.getX() - 10, this.pos.getY() - 10, this.pos.getZ() - 10, this.pos.getX() + 11, this.pos.getY() + 11, this.pos.getZ() + 11));
				EntityPlayer first = players.isEmpty() ? null : players.get(0);
				boolean bonus = first == null ? false : (HbmPlayerProps.getData(first).reputation >= 10);
				EntityBobmazon shuttle = new EntityBobmazon(this.world);
				shuttle.posX = this.pos.getX() + 0.5 + this.world.rand.nextGaussian() * 10;
				shuttle.posY = 300;
				shuttle.posZ = this.pos.getZ() + 0.5 + this.world.rand.nextGaussian() * 10;
				ItemStack payload = ItemKitCustom.create("Supplies", null, 0xffffff, 0x008000,
						DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC, 4 + this.world.rand.nextInt(4)),
						DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED, 4 + this.world.rand.nextInt(2)),
						bonus ? new ItemStack(ModItems.gem_alexandrite) : new ItemStack(Items.DIAMOND, 6 + this.world.rand.nextInt(6)),
						new ItemStack(Blocks.RED_FLOWER));
				shuttle.payload = payload;

				this.world.spawnEntityInWorld(shuttle);
			}

			if(comTimer >= 0) {
				comTimer--;
			}

			networkPackNT(250);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.pos.getX() - 50, this.pos.getY() - 50, this.pos.getZ() - 50, this.pos.getX() + 51, this.pos.getY() + 51, this.pos.getZ() + 51));
		for(EntityPlayer player : players) {
			HbmPlayerProps props = HbmPlayerProps.getData(player);
			if(props.reputation > -25) props.reputation--;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(this.isBroken);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.isBroken = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		isBroken = nbt.getBoolean("isBroken");
		comTimer = nbt.getInteger("comTimer");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("isBroken", isBroken);
		nbt.setInteger("comTimer", comTimer);
		return nbt;
	}

	@Override
	public boolean isDamaged() {
		return isBroken;
	}

	List<AStack> repair = new ArrayList();
	@Override
	public List<AStack> getRepairMaterials() {

		if(!repair.isEmpty())
			return repair;

		repair.add(new OreDictStack(OreDictManager.STEEL.plate(), 2));
		repair.add(new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC));
		return repair;
	}

	@Override
	public void repair() {
		this.isBroken = false;
		this.comTimer = 400;
		this.markDirty();
	}

	@Override public void tryExtinguish(World world, int x, int y, int z, EnumExtinguishType type) { }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX(),
					this.pos.getY(),
					this.pos.getZ(),
					this.pos.getX() + 1,
					this.pos.getY() + 6,
					this.pos.getZ() + 1
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
