package com.hbm.blocks;

import com.hbm.lib.RefStrings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
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
		super(Material.ROCK);
	}

	public BlockBase(Material material) {
		super(material);
	}

	/** Legacy NTM naming helper; the registry name retains the full legacy unlocalized path. */
	public BlockBase setBlockName(String name) {
		this.setTranslationKey(name);
		this.setRegistryName(new ResourceLocation(RefStrings.MODID, this.getTranslationKey()));
		this.textureName = RefStrings.MODID + ":" + name;
		return this;
	}

	/** Public NTM construction hook for the native, protected Block sound setter. */
	@Override
	public BlockBase setSoundType(SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	@Override
	public BlockBase setTranslationKey(String key) {
		super.setTranslationKey(key);
		return this;
	}

	@Override
	public BlockBase setCreativeTab(net.minecraft.creativetab.CreativeTabs tab) {
		super.setCreativeTab(tab);
		return this;
	}

	@Override
	public BlockBase setHardness(float hardness) {
		super.setHardness(hardness);
		return this;
	}

	@Override
	public BlockBase setResistance(float resistance) {
		super.setResistance(resistance);
		return this;
	}

	@Override
	public BlockBase setLightLevel(float lightLevel) {
		super.setLightLevel(lightLevel);
		return this;
	}

	@Override
	public BlockBase setLightOpacity(int opacity) {
		super.setLightOpacity(opacity);
		return this;
	}

	@Override
	public BlockBase setBlockUnbreakable() {
		super.setBlockUnbreakable();
		return this;
	}

	/** Original explicit texture assignment, consumed by the native atlas adapter. */
	public BlockBase setBlockTextureName(String texture) {
		this.textureName = texture;
		return this;
	}

	/** Legacy texture path used by NTM's texture-atlas callbacks. */
	public String getTextureName() {
		return textureName;
	}

	/** Registers the normal block texture for legacy renderers and subclasses. */
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		String texture = getTextureName();
		if(texture == null) {
			ResourceLocation name = getRegistryName();
			if(name == null) throw new IllegalStateException("Block must be registered before its icon callback: " + this);
			texture = name.toString();
		} else {
			// 1.7 used a block-only atlas rooted at textures/blocks. The native
			// shared atlas is rooted at textures, so preserve the original file.
			ResourceLocation legacy = new ResourceLocation(texture);
			texture = new ResourceLocation(legacy.getNamespace(), "blocks/" + legacy.getPath()).toString();
		}
		blockIcon = register.registerIcon(texture);
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

	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
		return this.canSpawn ? super.canCreatureSpawn(state, world, pos, type) : false;
	}

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
