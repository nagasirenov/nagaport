package com.hbm.blocks;

import com.hbm.lib.RefStrings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBase extends Block {

	@SideOnly(Side.CLIENT)
	protected IIcon blockIcon;

	private boolean beaconable = false;
	private boolean canSpawn = true;
	private String textureName;

	public BlockBase() {
		super(Material.rock);
	}

	public BlockBase(Material material) {
		super(material);
	}

	/** Legacy NTM naming helper; the registry name retains the full legacy unlocalized path. */
	public BlockBase setBlockName(String name) {
		this.setUnlocalizedName(name);
		this.setRegistryName(new ResourceLocation(RefStrings.MODID, this.getUnlocalizedName()));
		this.textureName = RefStrings.MODID + ":" + name;
		return this;
	}

	/** Legacy texture path used by NTM's texture-atlas callbacks. */
	public String getTextureName() {
		return textureName;
	}

	/** Registers the normal block texture for legacy renderers and subclasses. */
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		ResourceLocation name = getRegistryName();
		if(name == null) throw new IllegalStateException("Block must be registered before its icon callback: " + this);
		blockIcon = register.registerIcon(name.toString());
	}

	/** Legacy side/metadata icon lookup used by the 1.12 baked-model bridge. */
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return blockIcon;
	}

	/**
	 * Daisychainable setter for making the block a beacon base block
	 * @return
	 */
	public BlockBase setBeaconable() {
		this.beaconable = true;
		return this;
	}

	public BlockBase noMobSpawn() {
		this.canSpawn = false;
		return this;
	}

	@Override
	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
		return this.canSpawn ? super.canCreatureSpawn(state, world, pos, type) : false;
	}

	@Override
	public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beaconPos) {
		return this.beaconable;
	}

	/**
	 * Sets the block to air and drops it
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public void dismantle(World world, int x, int y, int z) {

		BlockPos pos = new BlockPos(x, y, z);
		world.setBlockToAir(pos);

		ItemStack itemstack = new ItemStack(this, 1);
		float f = world.rand.nextFloat() * 0.6F + 0.2F;
		float f1 = world.rand.nextFloat() * 0.2F;
		float f2 = world.rand.nextFloat() * 0.6F + 0.2F;

		EntityItem entityitem = new EntityItem(world, x + f, y + f1 + 1, z + f2, itemstack);

		float f3 = 0.05F;
		entityitem.motionX = (float) world.rand.nextGaussian() * f3;
		entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 0.2F;
		entityitem.motionZ = (float) world.rand.nextGaussian() * f3;

		if(!world.isRemote)
			world.spawnEntity(entityitem);
	}
}
