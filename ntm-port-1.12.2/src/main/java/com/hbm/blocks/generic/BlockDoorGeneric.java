package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.atmosphere.IBlockSealable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.interfaces.IBomb;
import com.hbm.tileentity.DoorDecl;
import com.hbm.tileentity.TileEntityDoorGeneric;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.Rotation;

import api.hbm.block.IToolable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockDoorGeneric extends BlockDummyable implements IBomb, IBlockSealable, IToolable {

	public DoorDecl type;

	public BlockDoorGeneric(Material materialIn, DoorDecl type){
		super(materialIn);
		this.type = type;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		if(meta >= 12) return new TileEntityDoorGeneric();
		return null;
	}

	@Override
	public int[] getDimensions(){
		return type.getDimensions();
	}

	@Override
	public int getOffset(){
		return type.getBlockOffset();
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {
		int[] pos1 = findCore(world, x, y, z);
		if(pos1 == null) return BombReturnCode.ERROR_INCOMPATIBLE;
		TileEntityDoorGeneric door = (TileEntityDoorGeneric) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos1[0], pos1[1], pos1[2]));
		if(door != null) {
			DoorDecl decl = door.getDoorType();
			if(!decl.remoteControllable()) return BombReturnCode.ERROR_INCOMPATIBLE;
			if(door.tryToggle(null)) {
				return BombReturnCode.TRIGGERED;
			}
		}

		return BombReturnCode.ERROR_INCOMPATIBLE;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer playerIn, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(!world.isRemote && !playerIn.isSneaking()) {
			int[] pos1 = findCore(world, x, y, z);
			if(pos1 == null) return false;
			TileEntityDoorGeneric door = (TileEntityDoorGeneric) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos1[0], pos1[1], pos1[2]));

			if(door != null) {
				return door.tryToggle(playerIn);
			}
		}
		return !playerIn.isSneaking();
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER || !player.isSneaking()) return false;

		int[] pos1 = findCore(world, x, y, z);
		if(pos1 == null) return false;
		TileEntityDoorGeneric door = (TileEntityDoorGeneric) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos1[0], pos1[1], pos1[2]));

		if(door == null || !door.getDoorType().hasSkins()) return false;
		if(world.isRemote) return true;
		door.cycleSkinIndex();
		return true;
	}

	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		int meta = com.hbm.world.WorldBlockState.readMetadata(world, x, y, z);
		boolean open = hasExtra(meta) || (te instanceof TileEntityDoorGeneric && ((TileEntityDoorGeneric)te).shouldUseBB);
		return type.isLadder(open);
	}

	@Override
	public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB entityBox, List collidingBoxes, Entity entityIn) {
		AxisAlignedBB box = getBoundingBox(worldIn, x, y, z, true);
		box = new AxisAlignedBB(
				Math.min(box.minX, box.maxX), Math.min(box.minY, box.maxY), Math.min(box.minZ, box.maxZ),
				Math.max(box.minX, box.maxX), Math.max(box.minY, box.maxY), Math.max(box.minZ, box.maxZ));

		if(box.minY == y && box.maxY == y) return;
		if(box.minX == box.maxX && box.minY == box.maxY && box.minZ == box.maxZ) return;

		if(box != null && box.intersects(entityBox)) {
			collidingBoxes.add(box);
		}

		//if(hasExtra(com.hbm.world.WorldBlockState.readMetadata(worldIn, x, y, z))) //transition hatch only worked with this, but fire door doesn't
		//	return;
		//super.addCollisionBoxesToList(worldIn, x, y, z, entityBox, collidingBoxes, entityIn);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		AxisAlignedBB aabb = this.getBoundingBox(world, x, y, z, true);
		if(aabb.minX == aabb.maxX && aabb.minY == aabb.maxY && aabb.minZ == aabb.maxZ) return null;
		return aabb;
	}

	// Enables clicking through the open door
	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, net.minecraft.util.math.BlockPos pos, Vec3d startVec, Vec3d endVec) {
		AxisAlignedBB box = getBoundingBox(world, pos.getX(), pos.getY(), pos.getZ(), false);
		box = new AxisAlignedBB(
			Math.min(box.minX, box.maxX), Math.min(box.minY, box.maxY), Math.min(box.minZ, box.maxZ),
			Math.max(box.minX, box.maxX), Math.max(box.minY, box.maxY), Math.max(box.minZ, box.maxZ)
		);

		RayTraceResult intercept = box.calculateIntercept(startVec, endVec);
		if(intercept != null) {
			return new RayTraceResult(intercept.hitVec, intercept.sideHit, pos);
		}
		return null;
	}

	public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) { //btw the method name is the exact opposite of that it's doing, check net.minecraft.pathfinding.PathNavigate#512
		return hasExtra(com.hbm.world.WorldBlockState.readMetadata(world, x, y, z)); //if it's open
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block blockIn){

		if(!world.isRemote){

			int[] corePos = findCore(world, x, y, z);
			if(corePos != null){
				TileEntity core = world.getTileEntity(new net.minecraft.util.math.BlockPos(corePos[0], corePos[1], corePos[2]));
				if(core instanceof TileEntityDoorGeneric){
					TileEntityDoorGeneric door = (TileEntityDoorGeneric)core;
					door.updateRedstonePower(x, y, z);
				}
			}
		}
		super.onNeighborBlockChange( world, x, y, z, blockIn);
	}

	@Override
	public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return getBoundingBox((net.minecraft.world.World) world, x, y, z, false);
	}

	public AxisAlignedBB getBoundingBox(World world, int x, int y, int z, boolean forCollision) {
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		int[] core = this.findCore(world, x, y, z);
		boolean open = hasExtra(meta) || (te instanceof TileEntityDoorGeneric && ((TileEntityDoorGeneric)te).state != 0);
		if(core == null){
			return new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
		}
		TileEntity te2 = world.getTileEntity(new net.minecraft.util.math.BlockPos(core[0], core[1], core[2]));
		ForgeDirection dir = ForgeDirection.getOrientation(te2.getBlockMetadata() - BlockDummyable.offset);
		BlockPos pos = new BlockPos(x - core[0], y - core[1], z - core[2]).rotate(Rotation.getBlockRotation(dir).add(Rotation.COUNTERCLOCKWISE_90));
		AxisAlignedBB box = type.getBlockBound(pos.getX(), pos.getY(), pos.getZ(), open, forCollision);

		switch(te2.getBlockMetadata() - offset){
		case 2: return new AxisAlignedBB(x + 1 - box.minX, y + box.minY, z + 1 - box.minZ, x + 1 - box.maxX, y + box.maxY, z + 1 - box.maxZ);
		case 4: return new AxisAlignedBB(x + 1 - box.minZ, y + box.minY, z + box.minX, x + 1 - box.maxZ, y + box.maxY, z + box.maxX);
		case 3: return new AxisAlignedBB(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ);
		case 5: return new AxisAlignedBB(x + box.minZ, y + box.minY, z + 1 - box.maxX, x + box.maxZ, y + box.maxY, z + 1 - box.minX);
		}
		return new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
	}

	@Override
	public boolean isSealed(World world, int x, int y, int z) {
		return !getBlocksMovement(world, x, y, z);
	}


	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), x, y, z, dir)) return false;

		if(type.getExtraDimensions() != null) for(int[] dims : type.getExtraDimensions()) {
			if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, dims, x, y, z, dir)) return false;
		}

		return true;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), this, dir);

		if(type.getExtraDimensions() != null) for(int[] dims : type.getExtraDimensions()) {
			MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, dims, this, dir);
		}
	}
}
