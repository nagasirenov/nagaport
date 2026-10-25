package com.hbm.entity.projectile;

import com.hbm.lib.ModDamageSource;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.ParticleBurstPacket;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityRubble extends EntityThrowableNT {

	private static final DataParameter<Integer> BLOCK_ID = EntityDataManager.createKey(EntityRubble.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> BLOCK_META = EntityDataManager.createKey(EntityRubble.class, DataSerializers.VARINT);


    public EntityRubble(World world)
    {
        super(world);
    }

    @Override
	public void entityInit() {
        this.dataManager.register(BLOCK_ID, 0);
        this.dataManager.register(BLOCK_META, 0);
    }

    public EntityRubble(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
	protected void onImpact(RayTraceResult mop)
    {
        if (mop.entityHit != null)
        {
            byte b0 = 15;

            mop.entityHit.attackEntityFrom(ModDamageSource.rubble, b0);
        }

        if(this.ticksExisted > 2) {
        	this.setDead();
        	
    		this.world.playSound(null, this.posX, this.posY, this.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:block.debris"), net.minecraft.util.SoundCategory.PLAYERS, 1.5F, 1.0F);
            //worldObj.playEvent(2001, new net.minecraft.util.math.BlockPos((int)posX, (int)posY, (int)posZ), this.dataManager.get(BLOCK_ID) + (this.dataManager.get(BLOCK_META) << 12));
    		
    		if(!this.world.isRemote)
			PacketDispatcher.wrapper.sendToAllAround(new ParticleBurstPacket((int)Math.floor(posX), (int)posY, (int)Math.floor(posZ), this.dataManager.get(BLOCK_ID), this.dataManager.get(BLOCK_META)), new TargetPoint(this.world.provider.getDimension(), posX, posY, posZ, 50));
        }
    }

    @Override
    protected float getAirDrag() {
        return 1F;
    }

    public void setMetaBasedOnBlock(Block b, int i) {

		this.dataManager.set(BLOCK_ID, Block.getIdFromBlock(b));
		this.dataManager.set(BLOCK_META, i);
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.dataManager.set(BLOCK_ID, nbt.getInteger("block"));
		this.dataManager.set(BLOCK_META, nbt.getInteger("meta"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("block", this.dataManager.get(BLOCK_ID));
		nbt.setInteger("meta", this.dataManager.get(BLOCK_META));
	}

	public int getBlockId() { return this.dataManager.get(BLOCK_ID); }
	public int getBlockMetadata() { return this.dataManager.get(BLOCK_META); }
}
