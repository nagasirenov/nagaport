package com.hbm.entity.projectile;

import com.hbm.blocks.bomb.NukeCustom;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityFallingNuke extends EntityThrowable {
	private static final DataParameter<Byte> BLOCK_METADATA = EntityDataManager.createKey(EntityFallingNuke.class, DataSerializers.BYTE);

	
	float tnt;
	float nuke;
	float hydro;
	float amat;
	float dirty;
	float schrab;
	float euph;

	public EntityFallingNuke(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
        this.setSize(0.98F, 0.98F);
	}

	public EntityFallingNuke(World p_i1582_1_, float tnt, float nuke, float hydro, float amat, float dirty, float schrab, float euph) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
		
		this.tnt = tnt;
		this.nuke = nuke;
		this.hydro = hydro;
		this.amat = amat;
		this.dirty = dirty;
		this.schrab = schrab;
		this.euph = euph;
        this.prevRotationYaw = this.rotationYaw = 90;
        this.prevRotationPitch = this.rotationPitch = 90;
        
        this.setSize(0.98F, 0.98F);
	}

	public void setBlockMetadata(byte metadata) {
		this.dataManager.set(BLOCK_METADATA, metadata);
	}

	public byte getBlockMetadata() {
		return this.dataManager.get(BLOCK_METADATA);
	}

    protected void entityInit() {
    	this.dataManager.register(BLOCK_METADATA, Byte.valueOf((byte)0));
    }
	
	@Override
	public void onUpdate() {


		this.lastTickPosX = this.prevPosX = posX;
		this.lastTickPosY = this.prevPosY = posY;
		this.lastTickPosZ = this.prevPosZ = posZ;
		this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);
		
		/*this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;*/
		
		this.motionX *= 0.99;
		this.motionZ *= 0.99;
		this.motionY -= 0.05D;
		
		if(motionY < -1)
			motionY = -1;
        
        this.rotation();
        
        if(com.hbm.world.WorldBlockState.read(this.world, (int)Math.floor(this.posX), (int)Math.floor(this.posY), (int)Math.floor(this.posZ)).getBlock() != Blocks.AIR)
        {
    		if(!this.world.isRemote)
    		{
				NukeCustom.explodeCustom(this.world, posX, posY, posZ, tnt, nuke, hydro, amat, dirty, schrab, euph);
	    		this.setDead();
    		}
        }
	}
	
	public void rotation() {

		this.prevRotationPitch = rotationPitch;

		if(rotationPitch > -75)
			this.rotationPitch -= 2;
	}

	@Override
	protected void onImpact(RayTraceResult p_70184_1_) { }

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setFloat("tnt", tnt);
		tag.setFloat("nuke", nuke);
		tag.setFloat("hydro", hydro);
		tag.setFloat("amat", amat);
		tag.setFloat("dirty", dirty);
		tag.setFloat("schrab", schrab);
		tag.setFloat("euph", euph);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		tnt = tag.getFloat("tnt");
		nuke = tag.getFloat("nuke");
		hydro = tag.getFloat("hydro");
		amat = tag.getFloat("amat");
		dirty = tag.getFloat("dirty");
		schrab = tag.getFloat("schrab");
		euph = tag.getFloat("euph");
	}

	@Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 25000;
    }
}
