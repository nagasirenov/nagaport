package com.hbm.blocks.generic;

import com.hbm.blocks.LegacyBlockTextures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockNTMTrapdoor extends BlockTrapDoor {

	public BlockNTMTrapdoor withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

    public BlockNTMTrapdoor(Material material) {
        super(material);
    }

    public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
        int meta = com.hbm.world.WorldBlockState.readMetadata(world, x, y, z);

        // isOpen
        if ((meta & 4) == 0)
            return false;

        net.minecraft.block.state.IBlockState belowState = com.hbm.world.WorldBlockState.read(world, x, y - 1, z);
        return belowState != null && belowState.getBlock().isLadder(belowState, world, new net.minecraft.util.math.BlockPos(x, y - 1, z), entity);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
    	int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        // Make the hitbox in-line with ladders, if relevant
        if (isLadder(world, x, y, z, null)) {
            int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
            float thickness = 0.125F;

            if ((meta & 3) == 0)
                return new AxisAlignedBB(x, y, z + 1F - thickness, x + 1F, y + 1F, z + 1F);

            if ((meta & 3) == 1)
                return new AxisAlignedBB(x, y, z, x + 1F, y + 1F, z + thickness);

            if ((meta & 3) == 2)
                return new AxisAlignedBB(x + 1F - thickness, y, z, x + 1F, y + 1F, z + 1F);

            if ((meta & 3) == 3)
                return new AxisAlignedBB(x, y, z, x + thickness, y + 1F, z + 1F);
        }

        return super.getBoundingBox(com.hbm.world.WorldBlockState.read(world, x, y, z), world, new net.minecraft.util.math.BlockPos(x, y, z));
    }

    @Override
public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
	int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
	int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

        // The original code prevented manual operation of Material.iron trapdoors. This bypasses that behavior
        int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
        com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta ^ 4, 2);
        world.playEvent(player, 1003, new net.minecraft.util.math.BlockPos(x, y, z), 0);
        return true;
    }
}
