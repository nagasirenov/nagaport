package com.hbm.entity.cart;

import com.hbm.items.tool.ItemModMinecart.EnumCartBase;
import com.hbm.render.entity.item.RenderNeoCart;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;


public abstract class EntityMinecartNTM extends EntityMinecart {
	
	private static final DataParameter<Integer> CART_BASE = EntityDataManager.createKey(EntityMinecartNTM.class, DataSerializers.VARINT);

	public EntityMinecartNTM(World p_i1712_1_) {
		super(p_i1712_1_);
	}

	public EntityMinecartNTM(World world, double x, double y, double z, EnumCartBase type) {
		super(world, x, y, z);
		this.setBase(type);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(CART_BASE, new Integer(0)); //EnumCartBase
	}
	
	public void setBase(EnumCartBase type) {
		this.dataManager.set(CART_BASE, type.ordinal());
	}
	
	public EnumCartBase getBase() {
		return EnumCartBase.values()[this.dataManager.get(CART_BASE)];
	}

	public int getMinecartType() {
		return -1;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entity) {
		return entity.getEntityBoundingBox();
	}

	public AxisAlignedBB getBoundingBox() {
		return this.getEntityBoundingBox();
	}

	@Override
	public void killMinecart(DamageSource p_94095_1_) {
		this.setDead();
		ItemStack itemstack = getCartItem();

		if(this.getCartItem() != null) {
			itemstack.setStackDisplayName(this.getCartItem().getDisplayName());
		}

		this.entityDropItem(itemstack, 0.0F);
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("base", this.dataManager.get(CART_BASE));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.dataManager.set(CART_BASE, nbt.getInteger("base"));
	}
	
	@SideOnly(Side.CLIENT)
	public void renderSpecialContent(RenderNeoCart renderer) { }
}
