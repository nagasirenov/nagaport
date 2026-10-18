package com.hbm.blocks.generic;

import static com.hbm.lib.ForgeDirection.EAST;
import static com.hbm.lib.ForgeDirection.NORTH;
import static com.hbm.lib.ForgeDirection.SOUTH;
import static com.hbm.lib.ForgeDirection.WEST;

import com.hbm.lib.RefStrings;

import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockChain extends Block implements INBTBlockTransformable {

	@SideOnly(Side.CLIENT)
	private IIcon iconEnd;

	public BlockChain(Material mat) {
		super(mat);
	}

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public static int renderID = RenderingRegistry.getNextAvailableRenderId();

    public int getRenderType() {
        return renderID;
    }

    @Override
    public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
        return true;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconEnd = iconRegister.registerIcon(RefStrings.MODID + ":chain_end");
		super.registerBlockIcons(iconRegister);
	}

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {

        if(com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y - 1, z, ForgeDirection.UP, false) || (world.getBlock(x, y - 1, z) == this && world.getBlockMetadata(x, y, z) == world.getBlockMetadata(x, y - 1, z)))
    		return this.blockIcon;

    	return this.iconEnd;
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
    	func_149797_b(world.getBlockMetadata(x, y, z));

        if(!(com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y - 1, z, ForgeDirection.UP, false) || (world.getBlock(x, y - 1, z) == this && world.getBlockMetadata(x, y, z) == world.getBlockMetadata(x, y - 1, z))))
    		this.minY = 0.25;
    }

    public void func_149797_b(int meta) {

        float f = 0.125F;

    	if(meta == 0) {

        	this.minX = 3 * f;
        	this.minY = 0;
        	this.minZ = 3 * f;
        	this.maxX = 5 * f;
        	this.maxY = 1;
        	this.maxZ = 5 * f;
    	}

        if (meta == 2)
        {
            this.setBlockBounds(3 * f, 0.0F, 1.0F - f, 5 * f, 1.0F, 1.0F);
        }

        if (meta == 3)
        {
            this.setBlockBounds(3 * f, 0.0F, 0.0F, 5 * f, 1.0F, f);
        }

        if (meta == 4)
        {
            this.setBlockBounds(1.0F - f, 0.0F, 3 * f, 1.0F, 1.0F, 5 * f);
        }

        if (meta == 5)
        {
            this.setBlockBounds(0.0F, 0.0F, 3 * f, f, 1.0F, 5 * f);
        }
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    public boolean canPlaceBlockAt(World world, int x, int y, int z) {

        if(com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y + 1, z, ForgeDirection.DOWN) || world.getBlock(x, y + 1, z) == this)
    		return true;

        return com.hbm.lib.FacingCompat.isWorldSideSolid(world, x - 1, y, z, EAST ) ||
                com.hbm.lib.FacingCompat.isWorldSideSolid(world, x + 1, y, z, WEST ) ||
                com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z - 1, SOUTH) ||
                com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z + 1, NORTH);
    }

    public int onBlockPlaced(World world, int x, int y, int z, int side, float p_149660_6_, float p_149660_7_, float p_149660_8_, int meta)
    {
        int j1 = meta;

        if(side == 2 && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z + 1, NORTH))
            j1 = 2;

        if(side == 3 && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z - 1, SOUTH))
            j1 = 3;

        if(side == 4 && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x + 1, y, z, WEST))
            j1 = 4;

        if(side == 5 && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x - 1, y, z, EAST))
            j1 = 5;

        if(j1 == 0) {

        	if(world.getBlock(x, y + 1, z) == this)
        		return world.getBlockMetadata(x, y + 1, z);

            if(com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y + 1, z, ForgeDirection.DOWN))
        		return 0;
        }

        if(j1 == 0) {
	        if(com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z + 1, NORTH))
	            j1 = 2;

	        if(com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z - 1, SOUTH))
	            j1 = 3;

	        if(com.hbm.lib.FacingCompat.isWorldSideSolid(world, x + 1, y, z, WEST))
	            j1 = 4;

	        if(com.hbm.lib.FacingCompat.isWorldSideSolid(world, x - 1, y, z, EAST))
	            j1 = 5;
        }

        return j1;
    }

    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {

        int l = world.getBlockMetadata(x, y, z);
        boolean flag = false;

        if(world.getBlock(x, y + 1, z) == this && world.getBlockMetadata(x, y, z) == world.getBlockMetadata(x, y + 1, z)) {
            super.onNeighborBlockChange(world, x, y, z, block);
            return;
        }

        if(com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y + 1, z, ForgeDirection.DOWN) && world.getBlockMetadata(x, y, z) == 0) {
            super.onNeighborBlockChange(world, x, y, z, block);
            return;
        }

        if (l == 2 && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z + 1, NORTH))
            flag = true;

        if (l == 3 && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z - 1, SOUTH))
            flag = true;

        if (l == 4 && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x + 1, y, z, WEST))
            flag = true;

        if (l == 5 && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x - 1, y, z, EAST))
            flag = true;

        if (!flag)
            world.func_147480_a(x, y, z, true);

        super.onNeighborBlockChange(world, x, y, z, block);
    }

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		if (coordBaseMode == 0) return meta;
		if (meta == 0) return meta;
		if (meta == 1) return meta;
		return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
	}
}
