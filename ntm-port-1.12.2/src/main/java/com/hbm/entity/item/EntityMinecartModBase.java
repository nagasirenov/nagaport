package com.hbm.entity.item;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityMinecartMobSpawner;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityMinecart.Type;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IMinecartCollisionHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;


//Bridged class, see parent for original
public abstract class EntityMinecartModBase extends EntityMinecart
{
    private static final DataParameter<Integer> MOTION_X_DIRECTION = EntityDataManager.createKey(EntityMinecartModBase.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> MOTION_Z_DIRECTION = EntityDataManager.createKey(EntityMinecartModBase.class, DataSerializers.VARINT);
    private boolean isInReverse;
    private String entityName;
    /** Minecart rotational logic matrix */
    private static final int[][][] matrix = new int[][][] {{{0, 0, -1}, {0, 0, 1}}, {{ -1, 0, 0}, {1, 0, 0}}, {{ -1, -1, 0}, {1, 0, 0}}, {{ -1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, { -1, 0, 0}}, {{0, 0, -1}, { -1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};
    /** appears to be the progress of the turn */
    private int turnProgress;
    private double minecartX;
    private double minecartY;
    private double minecartZ;
    private double minecartYaw;
    private double minecartPitch;
    @SideOnly(Side.CLIENT)
    private double velocityX;
    @SideOnly(Side.CLIENT)
    private double velocityY;
    @SideOnly(Side.CLIENT)
    private double velocityZ;
    /* Forge: Minecart Compatibility Layer Integration. */
    public static float defaultMaxSpeedAirLateral = 0.4f;
    public static float defaultMaxSpeedAirVertical = -1f;
    public static double defaultDragAir = 0.94999998807907104D;
    protected boolean canUseRail = true;
    protected boolean canBePushed = true;
    private static IMinecartCollisionHandler collisionHandler = null;

    /* Instance versions of the above physics properties */
    //private float currentSpeedRail = getMaxCartSpeedOnRail();
    protected float maxSpeedAirLateral = defaultMaxSpeedAirLateral;
    protected float maxSpeedAirVertical = defaultMaxSpeedAirVertical;
    protected double dragAir = defaultDragAir;

    public EntityMinecartModBase(World p_i1712_1_)
    {
        super(p_i1712_1_);
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.7F);
    }

    /**
     * Creates a new minecart of the specified type in the specified location in the given world. par0World - world to
     * create the minecart in, double par1,par3,par5 represent x,y,z respectively. int par7 specifies the type: 1 for
     * MinecartChest, 2 for MinecartFurnace, 3 for MinecartTNT, 4 for MinecartMobSpawner, 5 for MinecartHopper and 0 for
     * a standard empty minecart
     */
    public static EntityMinecart createMinecart(World p_94090_0_, double p_94090_1_, double p_94090_3_, double p_94090_5_, int p_94090_7_)
    {
        switch (p_94090_7_)
        {
            case 1:
                return new EntityMinecartChest(p_94090_0_, p_94090_1_, p_94090_3_, p_94090_5_);
            case 2:
                return new EntityMinecartFurnace(p_94090_0_, p_94090_1_, p_94090_3_, p_94090_5_);
            case 3:
                return new EntityMinecartTNT(p_94090_0_, p_94090_1_, p_94090_3_, p_94090_5_);
            case 4:
                return new EntityMinecartMobSpawner(p_94090_0_, p_94090_1_, p_94090_3_, p_94090_5_);
            case 5:
                return new EntityMinecartHopper(p_94090_0_, p_94090_1_, p_94090_3_, p_94090_5_);
            case 6:
                return new EntityMinecartCommandBlock(p_94090_0_, p_94090_1_, p_94090_3_, p_94090_5_);
            default:
                return new EntityMinecartEmpty(p_94090_0_, p_94090_1_, p_94090_3_, p_94090_5_);
        }
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    @Override
	protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
	protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(MOTION_X_DIRECTION, 0);
        this.dataManager.register(MOTION_Z_DIRECTION, 0);
    }

    /**
     * Returns a boundingBox used to collide the entity with other entities and blocks. This enables the entity to be
     * pushable on contact, like boats or minecarts.
     */
    @Override
	public AxisAlignedBB getCollisionBox(Entity p_70114_1_)
    {
        if (getCollisionHandler() != null)
        {
            return getCollisionHandler().getCollisionBox(this, p_70114_1_);
        }
        return p_70114_1_.canBePushed() ? p_70114_1_.getEntityBoundingBox() : null;
    }

    /**
     * returns the bounding box for this entity
     */
    @Override
	public AxisAlignedBB getCollisionBoundingBox()
    {
        if (getCollisionHandler() != null)
        {
            return getCollisionHandler().getBoundingBox(this);
        }
        return null;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    @Override
	public boolean canBePushed()
    {
        return canBePushed;
    }

    public EntityMinecartModBase(World p_i1713_1_, double p_i1713_2_, double p_i1713_4_, double p_i1713_6_)
    {
        this(p_i1713_1_);
        this.setPosition(p_i1713_2_, p_i1713_4_, p_i1713_6_);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = p_i1713_2_;
        this.prevPosY = p_i1713_4_;
        this.prevPosZ = p_i1713_6_;
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    @Override
	public double getMountedYOffset()
    {
        return this.height * 0.0D - 0.30000001192092896D;
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
    {
        if (!this.world.isRemote && !this.isDead)
        {
            if (this.isEntityInvulnerable(p_70097_1_))
            {
                return false;
            }
            else
            {
                this.setRollingDirection(-this.getRollingDirection());
                this.setRollingAmplitude(10);
                this.velocityChanged = true;
                this.setDamage(this.getDamage() + p_70097_2_ * 10.0F);
                boolean flag = p_70097_1_.getTrueSource() instanceof EntityPlayer && ((EntityPlayer)p_70097_1_.getTrueSource()).capabilities.isCreativeMode;

                if (flag || this.getDamage() > 40.0F)
                {
                    this.removePassengers();

                    if (flag && !this.hasCustomInventoryName())
                    {
                        this.setDead();
                    }
                    else
                    {
                        this.killMinecart(p_70097_1_);
                    }
                }

                return true;
            }
        }
        else
        {
            return true;
        }
    }

    @Override
	public void killMinecart(DamageSource p_94095_1_)
    {
        this.setDead();
        ItemStack itemstack = new ItemStack(Items.MINECART, 1);

        if (this.entityName != null)
        {
            itemstack.setStackDisplayName(this.entityName);
        }

        this.entityDropItem(itemstack, 0.0F);
    }

    /**
     * Setups the entity to do the hurt animation. Only used by packets in multiplayer.
     */
    @Override
	@SideOnly(Side.CLIENT)
    public void performHurtAnimation()
    {
        this.setRollingDirection(-this.getRollingDirection());
        this.setRollingAmplitude(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    @Override
	public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    /**
     * Will get destroyed next tick.
     */
    @Override
	public void setDead()
    {
        super.setDead();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
	public void onUpdate()
    {
        if (this.getRollingAmplitude() > 0)
        {
            this.setRollingAmplitude(this.getRollingAmplitude() - 1);
        }

        if (this.getDamage() > 0.0F)
        {
            this.setDamage(this.getDamage() - 1.0F);
        }

        if (this.posY < -64.0D)
        {
            this.setDead();
        }

        //
        if(this.motionX < 0)
	        this.dataManager.set(MOTION_X_DIRECTION, -1);
        if(this.motionX > 0)
	        this.dataManager.set(MOTION_X_DIRECTION, 1);
        if(this.motionX == 0 && this.motionZ != 0)
	        this.dataManager.set(MOTION_X_DIRECTION, 0);
        if(this.motionZ < 0)
	        this.dataManager.set(MOTION_Z_DIRECTION, -1);
        if(this.motionZ > 0)
	        this.dataManager.set(MOTION_Z_DIRECTION, 1);
        if(this.motionZ == 0 && this.motionX != 0)
	        this.dataManager.set(MOTION_Z_DIRECTION, 0);
        //

        int i;

        if (!this.world.isRemote && this.world instanceof WorldServer)
        {
            this.world.profiler.startSection("portal");
            MinecraftServer minecraftserver = ((WorldServer)this.world).getMinecraftServer();
            i = this.getMaxInPortalTime();

            if (this.inPortal)
            {
                if (minecraftserver.getAllowNether())
                {
                    if (this.getRidingEntity() == null && this.portalCounter++ >= i)
                    {
                        this.portalCounter = i;
                        this.timeUntilPortal = this.getPortalCooldown();
                        byte b0;

                        if (this.world.provider.getDimension() == -1)
                        {
                            b0 = 0;
                        }
                        else
                        {
                            b0 = -1;
                        }

                        this.changeDimension(b0);
                    }

                    this.inPortal = false;
                }
            }
            else
            {
                if (this.portalCounter > 0)
                {
                    this.portalCounter -= 4;
                }

                if (this.portalCounter < 0)
                {
                    this.portalCounter = 0;
                }
            }

            if (this.timeUntilPortal > 0)
            {
                --this.timeUntilPortal;
            }

            this.world.profiler.endSection();
        }

        if (this.world.isRemote)
        {
            if (this.turnProgress > 0)
            {
                double d6 = this.posX + (this.minecartX - this.posX) / this.turnProgress;
                double d7 = this.posY + (this.minecartY - this.posY) / this.turnProgress;
                double d1 = this.posZ + (this.minecartZ - this.posZ) / this.turnProgress;
                double d3 = MathHelper.wrapDegrees(this.minecartYaw - this.rotationYaw);
                this.rotationYaw = (float)(this.rotationYaw + d3 / this.turnProgress);
                this.rotationPitch = (float)(this.rotationPitch + (this.minecartPitch - this.rotationPitch) / this.turnProgress);
                --this.turnProgress;
                this.setPosition(d6, d7, d1);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            }
            else
            {
                this.setPosition(this.posX, this.posY, this.posZ);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            }
        }
        else
        {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.motionY -= 0.03999999910593033D;
            int l = MathHelper.floor(this.posX);
            i = MathHelper.floor(this.posY);
            int i1 = MathHelper.floor(this.posZ);

            BlockPos railPos = new BlockPos(l, i, i1);
            if (BlockRailBase.isRailBlock(this.world, railPos.down()))
            {
                --i;
            }

            double d0 = 0.4D;
            Block block = com.hbm.world.WorldBlockState.read(this.world, l, i, i1).getBlock();

            if (canUseRail() && BlockRailBase.isRailBlock(com.hbm.world.WorldBlockState.read(this.world, l, i, i1)))
            {
                railPos = new BlockPos(l, i, i1);
                float railMaxSpeed = ((BlockRailBase)block).getRailMaxSpeed(this.world, this, railPos);
                double maxSpeed = Math.min(railMaxSpeed, getCurrentCartSpeedCapOnRail());
                this.moveAlongTrack(railPos, com.hbm.world.WorldBlockState.read(this.world, l, i, i1));

                if (block == Blocks.ACTIVATOR_RAIL)
                {
                    this.onActivatorRailPass(l, i, i1, (com.hbm.world.WorldBlockState.readMetadata(this.world, l, i, i1) & 8) != 0);
                }
            }
            else
            {
                this.func_94088_b(onGround ? d0 : getMaxSpeedAirLateral());
            }

            this.doBlockCollisions();
            this.rotationPitch = 0.0F;
            double d8 = this.prevPosX - this.posX;
            double d4 = this.prevPosZ - this.posZ;

            if (d8 * d8 + d4 * d4 > 0.001D)
            {
                this.rotationYaw = (float)(Math.atan2(d4, d8) * 180.0D / Math.PI);

                if (this.isInReverse)
                {
                    this.rotationYaw += 180.0F;
                }
            }

            double d5 = MathHelper.wrapDegrees(this.rotationYaw - this.prevRotationYaw);

            if (d5 < -170.0D || d5 >= 170.0D)
            {
                this.rotationYaw += 180.0F;
                this.isInReverse = !this.isInReverse;
            }

            this.setRotation(this.rotationYaw, this.rotationPitch);

            AxisAlignedBB box;
            if (getCollisionHandler() != null)
            {
                box = getCollisionHandler().getMinecartCollisionBox(this);
            }
            else
            {
                box = this.getEntityBoundingBox().grow(0.2D, 0.0D, 0.2D);
            }

            List list = this.world.getEntitiesWithinAABBExcludingEntity(this, box);

            if (list != null && !list.isEmpty())
            {
                for (int k = 0; k < list.size(); ++k)
                {
                    Entity entity = (Entity)list.get(k);

                    if (!this.isPassenger(entity) && entity.canBePushed() && entity instanceof EntityMinecart)
                    {
                        entity.applyEntityCollision(this);
                    }
                }
            }

            if (this.isBeingRidden() && this.getPassengers().get(0).isDead)
            {
                this.removePassengers();
            }

            MinecraftForge.EVENT_BUS.post(new MinecartUpdateEvent(this, new BlockPos(l, i, i1)));
        }
    }

    /**
     * Called every tick the minecart is on an activator rail. Args: x, y, z, is the rail receiving power
     */
    @Override
	public void onActivatorRailPass(int p_96095_1_, int p_96095_2_, int p_96095_3_, boolean p_96095_4_) {}

	protected void func_94088_b(double p_94088_1_)
    {
        if (this.motionX < -p_94088_1_)
        {
            this.motionX = -p_94088_1_;
        }

        if (this.motionX > p_94088_1_)
        {
            this.motionX = p_94088_1_;
        }

        if (this.motionZ < -p_94088_1_)
        {
            this.motionZ = -p_94088_1_;
        }

        if (this.motionZ > p_94088_1_)
        {
            this.motionZ = p_94088_1_;
        }

        double moveY = motionY;
        if(getMaxSpeedAirVertical() > 0 && motionY > getMaxSpeedAirVertical())
        {
            moveY = getMaxSpeedAirVertical();
            if(Math.abs(motionX) < 0.3f && Math.abs(motionZ) < 0.3f)
            {
                moveY = 0.15f;
                motionY = moveY;
            }
        }

        if (this.onGround)
        {
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
        }

        this.move(net.minecraft.entity.MoverType.SELF, this.motionX, moveY, this.motionZ);

        if (!this.onGround)
        {
            this.motionX *= getDragAir();
            this.motionY *= getDragAir();
            this.motionZ *= getDragAir();
        }
    }

    @Override
    protected void moveAlongTrack(BlockPos railPos, IBlockState railState)
    {
        int p_145821_1_ = railPos.getX();
        int p_145821_2_ = railPos.getY();
        int p_145821_3_ = railPos.getZ();
        double p_145821_4_ = Math.min(((BlockRailBase)railState.getBlock()).getRailMaxSpeed(this.world, this, railPos), getCurrentCartSpeedCapOnRail());
        double p_145821_6_ = getSlopeAdjustment();
        Block p_145821_8_ = railState.getBlock();
        int p_145821_9_ = ((BlockRailBase)p_145821_8_).getRailDirection(this.world, railPos, railState, this).getMetadata();
        this.fallDistance = 0.0F;
        Vec3d vec3 = this.getPos(this.posX, this.posY, this.posZ);
        this.posY = p_145821_2_;
        boolean flag = false;
        boolean flag1 = false;

        if (p_145821_8_ == Blocks.GOLDEN_RAIL)
        {
            flag = (com.hbm.world.WorldBlockState.readMetadata(this.world, p_145821_1_, p_145821_2_, p_145821_3_) & 8) != 0;
            flag1 = !flag;
        }

        if (p_145821_8_ == Blocks.GOLDEN_RAIL)
        {
            p_145821_9_ &= 7;
        }

        if (p_145821_9_ >= 2 && p_145821_9_ <= 5)
        {
            this.posY = p_145821_2_ + 1;
        }

        if (p_145821_9_ == 2)
        {
            this.motionX -= p_145821_6_;
        }

        if (p_145821_9_ == 3)
        {
            this.motionX += p_145821_6_;
        }

        if (p_145821_9_ == 4)
        {
            this.motionZ += p_145821_6_;
        }

        if (p_145821_9_ == 5)
        {
            this.motionZ -= p_145821_6_;
        }

        int[][] aint = matrix[p_145821_9_];
        double d2 = aint[1][0] - aint[0][0];
        double d3 = aint[1][2] - aint[0][2];
        double d4 = Math.sqrt(d2 * d2 + d3 * d3);
        double d5 = this.motionX * d2 + this.motionZ * d3;

        if (d5 < 0.0D)
        {
            d2 = -d2;
            d3 = -d3;
        }

        double d6 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (d6 > 2.0D)
        {
            d6 = 2.0D;
        }

        this.motionX = d6 * d2 / d4;
        this.motionZ = d6 * d3 / d4;
        double d7;
        double d8;
        double d9;
        double d10;

        Entity rider = this.getControllingPassenger();
        if (rider instanceof EntityLivingBase)
        {
            d7 = ((EntityLivingBase)rider).moveForward;

            if (d7 > 0.0D)
            {
                d8 = -Math.sin(rider.rotationYaw * (float)Math.PI / 180.0F);
                d9 = Math.cos(rider.rotationYaw * (float)Math.PI / 180.0F);
                d10 = this.motionX * this.motionX + this.motionZ * this.motionZ;

                if (d10 < 0.01D)
                {
                    this.motionX += d8 * 0.1D;
                    this.motionZ += d9 * 0.1D;
                    flag1 = false;
                }
            }
        }

        if (flag1 && shouldDoRailFunctions())
        {
            d7 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (d7 < 0.03D)
            {
                this.motionX *= 0.0D;
                this.motionY *= 0.0D;
                this.motionZ *= 0.0D;
            }
            else
            {
                this.motionX *= 0.5D;
                this.motionY *= 0.0D;
                this.motionZ *= 0.5D;
            }
        }

        d7 = 0.0D;
        d8 = p_145821_1_ + 0.5D + aint[0][0] * 0.5D;
        d9 = p_145821_3_ + 0.5D + aint[0][2] * 0.5D;
        d10 = p_145821_1_ + 0.5D + aint[1][0] * 0.5D;
        double d11 = p_145821_3_ + 0.5D + aint[1][2] * 0.5D;
        d2 = d10 - d8;
        d3 = d11 - d9;
        double d12;
        double d13;

        if (d2 == 0.0D)
        {
            this.posX = p_145821_1_ + 0.5D;
            d7 = this.posZ - p_145821_3_;
        }
        else if (d3 == 0.0D)
        {
            this.posZ = p_145821_3_ + 0.5D;
            d7 = this.posX - p_145821_1_;
        }
        else
        {
            d12 = this.posX - d8;
            d13 = this.posZ - d9;
            d7 = (d12 * d2 + d13 * d3) * 2.0D;
        }

        this.posX = d8 + d2 * d7;
        this.posZ = d9 + d3 * d7;
        this.setPosition(this.posX, this.posY + this.getYOffset(), this.posZ);

        moveMinecartOnRail(railPos);

        if (aint[0][1] != 0 && MathHelper.floor(this.posX) - p_145821_1_ == aint[0][0] && MathHelper.floor(this.posZ) - p_145821_3_ == aint[0][2])
        {
            this.setPosition(this.posX, this.posY + aint[0][1], this.posZ);
        }
        else if (aint[1][1] != 0 && MathHelper.floor(this.posX) - p_145821_1_ == aint[1][0] && MathHelper.floor(this.posZ) - p_145821_3_ == aint[1][2])
        {
            this.setPosition(this.posX, this.posY + aint[1][1], this.posZ);
        }

        this.applyDrag();
        Vec3d vec31 = this.getPos(this.posX, this.posY, this.posZ);

        if (vec31 != null && vec3 != null)
        {
            double d14 = (vec3.y - vec31.y) * 0.05D;
            d6 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (d6 > 0.0D)
            {
                this.motionX = this.motionX / d6 * (d6 + d14);
                this.motionZ = this.motionZ / d6 * (d6 + d14);
            }

            this.setPosition(this.posX, vec31.y, this.posZ);
        }

        int j1 = MathHelper.floor(this.posX);
        int i1 = MathHelper.floor(this.posZ);

        if (j1 != p_145821_1_ || i1 != p_145821_3_)
        {
            d6 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.motionX = d6 * (j1 - p_145821_1_);
            this.motionZ = d6 * (i1 - p_145821_3_);
        }

        if(shouldDoRailFunctions())
        {
            ((BlockRailBase)p_145821_8_).onMinecartPass(this.world, this, railPos);
        }

        if (flag && shouldDoRailFunctions())
        {
            double d15 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (d15 > 0.01D)
            {
                double d16 = 0.06D;
                this.motionX += this.motionX / d15 * d16;
                this.motionZ += this.motionZ / d15 * d16;
            }
            else if (p_145821_9_ == 1)
            {
                if (com.hbm.world.WorldBlockState.read(this.world, p_145821_1_ - 1, p_145821_2_, p_145821_3_).isNormalCube())
                {
                    this.motionX = 0.02D;
                }
                else if (com.hbm.world.WorldBlockState.read(this.world, p_145821_1_ + 1, p_145821_2_, p_145821_3_).isNormalCube())
                {
                    this.motionX = -0.02D;
                }
            }
            else if (p_145821_9_ == 0)
            {
                if (com.hbm.world.WorldBlockState.read(this.world, p_145821_1_, p_145821_2_, p_145821_3_ - 1).isNormalCube())
                {
                    this.motionZ = 0.02D;
                }
                else if (com.hbm.world.WorldBlockState.read(this.world, p_145821_1_, p_145821_2_, p_145821_3_ + 1).isNormalCube())
                {
                    this.motionZ = -0.02D;
                }
            }
        }
    }

    @Override
	protected void applyDrag()
    {
        if (this.isBeingRidden())
        {
            this.motionX *= 0.996999979019165D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.996999979019165D;
        }
        else
        {
            this.motionX *= 0.9599999785423279D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.9599999785423279D;
        }
    }

	@Override
	@SideOnly(Side.CLIENT)
    public Vec3d getPosOffset(double p_70495_1_, double p_70495_3_, double p_70495_5_, double p_70495_7_)
    {

    	//!!!DANGER!!!//
        //return null;
    	//!!!DANGER!!!//

        int i = MathHelper.floor(p_70495_1_);
        int j = MathHelper.floor(p_70495_3_);
        int k = MathHelper.floor(p_70495_5_);

        if (BlockRailBase.isRailBlock(this.world, new BlockPos(i, j - 1, k)))
        {
            --j;
        }

        Block block = com.hbm.world.WorldBlockState.read(this.world, i, j, k).getBlock();

        if (!BlockRailBase.isRailBlock(com.hbm.world.WorldBlockState.read(this.world, i, j, k)))
        {
            return null;
        }
        else
        {
            BlockPos railPos = new BlockPos(i, j, k);
            int l = ((BlockRailBase)block).getRailDirection(this.world, railPos, com.hbm.world.WorldBlockState.read(this.world, i, j, k), this).getMetadata();

            p_70495_3_ = j;

            if (l >= 2 && l <= 5)
            {
                p_70495_3_ = j + 1;
            }

            int[][] aint = matrix[l];
            double d4 = aint[1][0] - aint[0][0];
            double d5 = aint[1][2] - aint[0][2];
            double d6 = Math.sqrt(d4 * d4 + d5 * d5);
            d4 /= d6;
            d5 /= d6;
            p_70495_1_ += d4 * p_70495_7_;
            p_70495_5_ += d5 * p_70495_7_;

            if (aint[0][1] != 0 && MathHelper.floor(p_70495_1_) - i == aint[0][0] && MathHelper.floor(p_70495_5_) - k == aint[0][2])
            {
                p_70495_3_ += aint[0][1];
            }
            else if (aint[1][1] != 0 && MathHelper.floor(p_70495_1_) - i == aint[1][0] && MathHelper.floor(p_70495_5_) - k == aint[1][2])
            {
                p_70495_3_ += aint[1][1];
            }

            return this.getPos(p_70495_1_, p_70495_3_, p_70495_5_);
        }
    }

    @Override
    public Vec3d getPos(double p_70489_1_, double p_70489_3_, double p_70489_5_)
    {
        int i = MathHelper.floor(p_70489_1_);
        int j = MathHelper.floor(p_70489_3_);
        int k = MathHelper.floor(p_70489_5_);

        if (BlockRailBase.isRailBlock(this.world, new BlockPos(i, j - 1, k)))
        {
            --j;
        }

        Block block = com.hbm.world.WorldBlockState.read(this.world, i, j, k).getBlock();

        if (BlockRailBase.isRailBlock(com.hbm.world.WorldBlockState.read(this.world, i, j, k)))
        {
            BlockPos railPos = new BlockPos(i, j, k);
            int l = ((BlockRailBase)block).getRailDirection(this.world, railPos, com.hbm.world.WorldBlockState.read(this.world, i, j, k), this).getMetadata();
            p_70489_3_ = j;

            if (l >= 2 && l <= 5)
            {
                p_70489_3_ = j + 1;
            }

            int[][] aint = matrix[l];
            double d3 = 0.0D;
            double d4 = i + 0.5D + aint[0][0] * 0.5D;
            double d5 = j + 0.5D + aint[0][1] * 0.5D;
            double d6 = k + 0.5D + aint[0][2] * 0.5D;
            double d7 = i + 0.5D + aint[1][0] * 0.5D;
            double d8 = j + 0.5D + aint[1][1] * 0.5D;
            double d9 = k + 0.5D + aint[1][2] * 0.5D;
            double d10 = d7 - d4;
            double d11 = (d8 - d5) * 2.0D;
            double d12 = d9 - d6;

            if (d10 == 0.0D)
            {
                p_70489_1_ = i + 0.5D;
                d3 = p_70489_5_ - k;
            }
            else if (d12 == 0.0D)
            {
                p_70489_5_ = k + 0.5D;
                d3 = p_70489_1_ - i;
            }
            else
            {
                double d13 = p_70489_1_ - d4;
                double d14 = p_70489_5_ - d6;
                d3 = (d13 * d10 + d14 * d12) * 2.0D;
            }

            p_70489_1_ = d4 + d10 * d3;
            p_70489_3_ = d5 + d11 * d3;
            p_70489_5_ = d6 + d12 * d3;

            if (d11 < 0.0D)
            {
                ++p_70489_3_;
            }

            if (d11 > 0.0D)
            {
                p_70489_3_ += 0.5D;
            }

            return new Vec3d(p_70489_1_, p_70489_3_, p_70489_5_);
        }
        else
        {
            return null;
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
	protected void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        if (p_70037_1_.getBoolean("CustomDisplayTile"))
        {
            this.func_145819_k(p_70037_1_.getInteger("DisplayTile"));
            this.setDisplayTileData(p_70037_1_.getInteger("DisplayData"));
            this.setDisplayTileOffset(p_70037_1_.getInteger("DisplayOffset"));
        }

        if (p_70037_1_.hasKey("CustomName", 8) && p_70037_1_.getString("CustomName").length() > 0)
        {
            this.entityName = p_70037_1_.getString("CustomName");
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
	protected void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        if (this.hasDisplayTile())
        {
            p_70014_1_.setBoolean("CustomDisplayTile", true);
            p_70014_1_.setInteger("DisplayTile", this.func_145820_n().getDefaultState().getMaterial() == Material.AIR ? 0 : Block.getIdFromBlock(this.func_145820_n()));
            p_70014_1_.setInteger("DisplayData", this.getDisplayTileData());
            p_70014_1_.setInteger("DisplayOffset", this.getDisplayTileOffset());
        }

        if (this.entityName != null && this.entityName.length() > 0)
        {
            p_70014_1_.setString("CustomName", this.entityName);
        }
    }

	@SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    /**
     * Applies a velocity to each of the entities pushing them away from each other. Args: entity
     */
    @Override
	public void applyEntityCollision(Entity p_70108_1_)
    {
        MinecraftForge.EVENT_BUS.post(new MinecartCollisionEvent(this, p_70108_1_));
        if (getCollisionHandler() != null)
        {
            getCollisionHandler().onEntityCollision(this, p_70108_1_);
            return;
        }
        if (!this.world.isRemote)
        {
            if (!this.isPassenger(p_70108_1_))
            {
                if (p_70108_1_ instanceof EntityLivingBase && !(p_70108_1_ instanceof EntityPlayer) && !(p_70108_1_ instanceof EntityIronGolem) && canBeRidden()               && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D && !this.isBeingRidden() && p_70108_1_.getRidingEntity() == null)
                {
                    p_70108_1_.startRiding(this);
                }

                double d0 = p_70108_1_.posX - this.posX;
                double d1 = p_70108_1_.posZ - this.posZ;
                double d2 = d0 * d0 + d1 * d1;

                if (d2 >= 9.999999747378752E-5D)
                {
                    d2 = MathHelper.sqrt(d2);
                    d0 /= d2;
                    d1 /= d2;
                    double d3 = 1.0D / d2;

                    if (d3 > 1.0D)
                    {
                        d3 = 1.0D;
                    }

                    d0 *= d3;
                    d1 *= d3;
                    d0 *= 0.10000000149011612D;
                    d1 *= 0.10000000149011612D;
                    d0 *= 1.0F - this.entityCollisionReduction;
                    d1 *= 1.0F - this.entityCollisionReduction;
                    d0 *= 0.5D;
                    d1 *= 0.5D;

                    if (p_70108_1_ instanceof EntityMinecart)
                    {
                        double d4 = p_70108_1_.posX - this.posX;
                        double d5 = p_70108_1_.posZ - this.posZ;
                        Vec3d vec3 = new Vec3d(d4, 0.0D, d5).normalize();
                        Vec3d vec31 = new Vec3d(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F), 0.0D, MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F)).normalize();
                        double d6 = Math.abs(vec3.dotProduct(vec31));

                        if (d6 < 0.800000011920929D)
                        {
                            return;
                        }

                        double d7 = p_70108_1_.motionX + this.motionX;
                        double d8 = p_70108_1_.motionZ + this.motionZ;

                        if (((EntityMinecart)p_70108_1_).isPoweredCart() && !isPoweredCart())
                        {
                            this.motionX *= 0.20000000298023224D;
                            this.motionZ *= 0.20000000298023224D;
                            this.addVelocity(p_70108_1_.motionX - d0, 0.0D, p_70108_1_.motionZ - d1);
                            p_70108_1_.motionX *= 0.949999988079071D;
                            p_70108_1_.motionZ *= 0.949999988079071D;
                        }
                        else if (((EntityMinecart)p_70108_1_).isPoweredCart() && !isPoweredCart())
                        {
                            p_70108_1_.motionX *= 0.20000000298023224D;
                            p_70108_1_.motionZ *= 0.20000000298023224D;
                            p_70108_1_.addVelocity(this.motionX + d0, 0.0D, this.motionZ + d1);
                            this.motionX *= 0.949999988079071D;
                            this.motionZ *= 0.949999988079071D;
                        }
                        else
                        {
                            d7 /= 2.0D;
                            d8 /= 2.0D;
                            this.motionX *= 0.20000000298023224D;
                            this.motionZ *= 0.20000000298023224D;
                            this.addVelocity(d7 - d0, 0.0D, d8 - d1);
                            p_70108_1_.motionX *= 0.20000000298023224D;
                            p_70108_1_.motionZ *= 0.20000000298023224D;
                            p_70108_1_.addVelocity(d7 + d0, 0.0D, d8 + d1);
                        }
                    }
                    else
                    {
                        this.addVelocity(-d0, 0.0D, -d1);
                        p_70108_1_.addVelocity(d0 / 4.0D, 0.0D, d1 / 4.0D);
                    }
                }
            }
        }
    }

    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
	@SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_, boolean teleport)
    {
        this.minecartX = p_70056_1_;
        this.minecartY = p_70056_3_;
        this.minecartZ = p_70056_5_;
        this.minecartYaw = p_70056_7_;
        this.minecartPitch = p_70056_8_;
        this.turnProgress = p_70056_9_ + 2;
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
    }

    /**
     * Sets the current amount of damage the minecart has taken. Decreases over time. The cart breaks when this is over
     * 40.
     */
    @Override
	public void setDamage(float p_70492_1_)
    {
        super.setDamage(p_70492_1_);
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    @Override
	@SideOnly(Side.CLIENT)
    public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_)
    {
        this.velocityX = this.motionX = p_70016_1_;
        this.velocityY = this.motionY = p_70016_3_;
        this.velocityZ = this.motionZ = p_70016_5_;
    }

    /**
     * Gets the current amount of damage the minecart has taken. Decreases over time. The cart breaks when this is over
     * 40.
     */
    @Override
	public float getDamage()
    {
        return super.getDamage();
    }

    /**
     * Sets the rolling amplitude the cart rolls while being attacked.
     */
    @Override
	public void setRollingAmplitude(int p_70497_1_)
    {
        super.setRollingAmplitude(p_70497_1_);
    }

    /**
     * Gets the rolling amplitude the cart rolls while being attacked.
     */
    @Override
	public int getRollingAmplitude()
    {
        return super.getRollingAmplitude();
    }

    /**
     * Sets the rolling direction the cart rolls while being attacked. Can be 1 or -1.
     */
    @Override
	public void setRollingDirection(int p_70494_1_)
    {
        super.setRollingDirection(p_70494_1_);
    }

    /**
     * Gets the rolling direction the cart rolls while being attacked. Can be 1 or -1.
     */
    @Override
	public int getRollingDirection()
    {
        return super.getRollingDirection();
    }

    public abstract int getMinecartType();

    @Override
    public Type getType() { Type type = Type.getById(getMinecartType()); return type == null ? Type.RIDEABLE : type; }

	public Block func_145820_n()
    {
        if (!this.hasDisplayTile())
        {
            return this.func_145817_o();
        }
        else
        {
            return super.getDisplayTile().getBlock();
        }
    }

	public Block func_145817_o()
    {
        return Blocks.AIR;
    }

    @Override
    public IBlockState getDefaultDisplayTile()
    {
        return this.func_145817_o().getDefaultState();
    }

	public int getDisplayTileData()
    {
        return super.getDisplayTile().getBlock().getMetaFromState(super.getDisplayTile());
    }

	public int getDefaultDisplayTileData()
    {
        return 0;
    }

    @Override
	public int getDisplayTileOffset()
    {
        return super.getDisplayTileOffset();
    }

	public int getDefaultDisplayTileOffset()
    {
        return 6;
    }

	public void func_145819_k(int p_145819_1_)
    {
        Block block = Block.getBlockById(p_145819_1_ & 65535);
        this.setDisplayTile(block.getStateFromMeta(this.getDisplayTileData()));
    }

	public void setDisplayTileData(int p_94092_1_)
    {
        this.setDisplayTile(this.func_145820_n().getStateFromMeta(p_94092_1_));
    }

    @Override
	public void setDisplayTileOffset(int p_94086_1_)
    {
        super.setDisplayTileOffset(p_94086_1_);
    }

    @Override
	public boolean hasDisplayTile()
    {
        return super.hasDisplayTile();
    }

    @Override
	public void setHasDisplayTile(boolean p_94096_1_)
    {
        super.setHasDisplayTile(p_94096_1_);
    }

    /**
     * Sets the minecart's name.
     */
	public void setMinecartName(String p_96094_1_)
    {
        this.entityName = p_96094_1_;
        this.setCustomNameTag(p_96094_1_);
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
	public String getCommandSenderName()
    {
        return this.entityName != null ? this.entityName : super.getName();
    }

    /**
     * Returns if the inventory is named
     */
	public boolean hasCustomName()
    {
        return this.entityName != null;
    }

	public boolean hasCustomInventoryName()
    {
        return hasCustomName();
    }

	public String func_95999_t()
    {
        return this.entityName;
    }
    /* =================================== FORGE START ===========================================*/
    /**
     * Moved to allow overrides.
     * This code handles minecart movement and speed capping when on a rail.
     */
    @Override
    public void moveMinecartOnRail(BlockPos railPos){
        double par4 = Math.min(((BlockRailBase)this.world.getBlockState(railPos).getBlock()).getRailMaxSpeed(this.world, this, railPos), getCurrentCartSpeedCapOnRail());
        double d12 = this.motionX;
        double d13 = this.motionZ;

        if (this.isBeingRidden())
        {
            d12 *= 0.75D;
            d13 *= 0.75D;
        }

        if (d12 < -par4)
        {
            d12 = -par4;
        }

        if (d12 > par4)
        {
            d12 = par4;
        }

        if (d13 < -par4)
        {
            d13 = -par4;
        }

        if (d13 > par4)
        {
            d13 = par4;
        }

        this.move(net.minecraft.entity.MoverType.SELF, d12, 0.0D, d13);
    }

    /**
     * Gets the current global Minecart Collision handler if none
     * is registered, returns null
     * @return The collision handler or null
     */
    public static IMinecartCollisionHandler getCollisionHandler()
    {
        return collisionHandler;
    }

    /**
     * Sets the global Minecart Collision handler, overwrites any
     * that is currently set.
     * @param handler The new handler
     */
    public static void setCollisionHandler(IMinecartCollisionHandler handler)
    {
        collisionHandler = handler;
    }

    /**
     * This function returns an ItemStack that represents this cart.
     * This should be an ItemStack that can be used by the player to place the cart,
     * but is not necessary the item the cart drops when destroyed.
     * @return An ItemStack that can be used to place the cart.
     */
    @Override
	public ItemStack getCartItem() {

        return new ItemStack(Items.MINECART);
    }

    /**
     * Returns true if this cart can currently use rails.
     * This function is mainly used to gracefully detach a minecart from a rail.
     * @return True if the minecart can use rails.
     */
    @Override
	public boolean canUseRail()
    {
        return canUseRail;
    }

    /**
     * Set whether the minecart can use rails.
     * This function is mainly used to gracefully detach a minecart from a rail.
     * @param use Whether the minecart can currently use rails.
     */
    @Override
	public void setCanUseRail(boolean use)
    {
        canUseRail = use;
    }

    /**
     * Return false if this cart should not call onMinecartPass() and should ignore Powered Rails.
     * @return True if this cart should call onMinecartPass().
     */
    @Override
	public boolean shouldDoRailFunctions()
    {
        return true;
    }

    /**
     * Returns true if this cart is self propelled.
     * @return True if powered.
     */
    @Override
	public boolean isPoweredCart()
    {
        return getMinecartType()== 2;
    }

    /**
     * Returns true if this cart can be ridden by an Entity.
     * @return True if this cart can be ridden.
     */
    @Override
	public boolean canBeRidden()
    {
        return false;
    }

    /**
     * Getters/setters for physics variables
     */

    /**
     * Returns the carts max speed when traveling on rails. Carts going faster
     * than 1.1 cause issues with chunk loading. Carts cant traverse slopes or
     * corners at greater than 0.5 - 0.6. This value is compared with the rails
     * max speed and the carts current speed cap to determine the carts current
     * max speed. A normal rail's max speed is 0.4.
     *
     * @return Carts max speed.
     */
    @Override
	public float getMaxCartSpeedOnRail()
    {
        return 1.2f;
    }

    @Override
	public float getMaxSpeedAirLateral()
    {
        return maxSpeedAirLateral;
    }

    @Override
	public void setMaxSpeedAirLateral(float value)
    {
        maxSpeedAirLateral = value;
    }

    @Override
	public float getMaxSpeedAirVertical()
    {
        return maxSpeedAirVertical;
    }

    @Override
	public void setMaxSpeedAirVertical(float value)
    {
        maxSpeedAirVertical = value;
    }

    @Override
	public double getDragAir()
    {
        return dragAir;
    }

    @Override
	public void setDragAir(double value)
    {
        dragAir = value;
    }

    @Override
	public double getSlopeAdjustment()
    {
        return 0.0078125D;
    }
    /* =================================== FORGE END ===========================================*/
}
