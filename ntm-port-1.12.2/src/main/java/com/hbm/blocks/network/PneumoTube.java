package com.hbm.blocks.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.inventory.fluid.trait.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Amat;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous_ART;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.util.Compat;

import api.hbm.block.IToolable;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.*;

/** Native exposed pneumatic tube owner; transport stays in TileEntityPneumoTube. */
public class PneumoTube extends BlockContainer implements IToolable, ITooltipProvider {
	@Override
	public net.minecraft.block.Block setSoundType(net.minecraft.block.SoundType sound) {
		return super.setSoundType(sound);
	}

    public PneumoTube(){super(Material.IRON);}
    @Override protected BlockStateContainer createBlockState(){return new ExtendedBlockState(this,new IProperty<?>[0],new IUnlistedProperty<?>[]{PneumaticModelState.MODEL});}
    @Override public int getMetaFromState(IBlockState state){return 0;}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState();}
    @Override public EnumBlockRenderType getRenderType(IBlockState state){return EnumBlockRenderType.MODEL;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing side){return BlockFaceShape.UNDEFINED;}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new TileEntityPneumoTube();}
    public PneumaticModelState snapshot(IBlockAccess world,BlockPos pos){
        int tubes=0,air=0;
        for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS){
            if(canConnectTo(world,pos.getX(),pos.getY(),pos.getZ(),dir)) tubes|=PneumaticModelState.bit(dir.ordinal());
            if(canConnectToAir(world,pos.getX(),pos.getY(),pos.getZ(),dir)) air|=PneumaticModelState.bit(dir.ordinal());
        }
        TileEntity tile=world.getTileEntity(pos);
        TileEntityPneumoTube tube=tile instanceof TileEntityPneumoTube?(TileEntityPneumoTube)tile:null;
        // The original renderer only rendered air connectors when its tile existed.
        return new PneumaticModelState(tubes,tube==null?0:air,tube==null?6:tube.insertionDir.ordinal(),tube==null?6:tube.ejectionDir.ordinal());
    }
    @Override public IBlockState getExtendedState(IBlockState state,IBlockAccess world,BlockPos pos){return ((IExtendedBlockState)state).withProperty(PneumaticModelState.MODEL,snapshot(world,pos));}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){
        PneumaticModelState model=snapshot(world,pos);int air=0;
        for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) if(canConnectToAir(world,pos.getX(),pos.getY(),pos.getZ(),dir))air|=PneumaticModelState.bit(dir.ordinal());
        return new PneumaticModelState(model.tubes,air,model.insertion,model.ejection).bounds();
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return getBoundingBox(state,world,pos);}
    @Override public void addCollisionBoxToList(IBlockState state,World world,BlockPos pos,AxisAlignedBB query,List<AxisAlignedBB> boxes,Entity entity,boolean actual){
        addCollisionBoxToList(pos,query,boxes,new AxisAlignedBB(.3125,.3125,.3125,.6875,.6875,.6875));
        for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) if(canConnectTo(world,pos.getX(),pos.getY(),pos.getZ(),dir)||canConnectToAir(world,pos.getX(),pos.getY(),pos.getZ(),dir))
            addCollisionBoxToList(pos,query,boxes,PneumaticModelState.arm(EnumFacing.byIndex(dir.ordinal()),.3125,.6875,.6875,1));
        // Insertion/ejection-only collars deliberately do not contribute collision arms.
    }
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		if(!player.getHeldItem(hand).isEmpty() && ToolType.getType(player.getHeldItem(hand)) == ToolType.SCREWDRIVER) return false;
		if(!player.isSneaking()) {
			TileEntity tile = world.getTileEntity(new BlockPos(x,y,z));
			if(tile instanceof TileEntityPneumoTube) {
				TileEntityPneumoTube tube = (TileEntityPneumoTube) tile;
				if(tube.isCompressor()) {
					if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier) {
						if(!world.isRemote) {
							FluidType type = ((IItemFluidIdentifier) player.getHeldItem(hand).getItem()).getType(world, x, y, z, player.getHeldItem(hand));
							boolean canUse = !type.hasTrait(FT_Amat.class) && !type.hasTrait(FT_Corrosive.class) && (type.hasTrait(FT_Gaseous.class) || type.hasTrait(FT_Gaseous_ART.class));

							if(canUse) {
								tube.compair.setTankType(type);
								tube.markDirty();
								player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));
							} else {
								player.sendMessage(new TextComponentString("Invalid gas!").setStyle(new Style().setColor(TextFormatting.RED)));
							}
						}
					} else {
						FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
					}

					return true;
				} else if(tube.isEndpoint()) {
					FMLNetworkHandler.openGui(player, MainRegistry.instance, 1, world, x, y, z);
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(world.isRemote) return true;

		TileEntityPneumoTube tube = (TileEntityPneumoTube) world.getTileEntity(new BlockPos(x,y,z));

		ForgeDirection rot = player.isSneaking() ? tube.ejectionDir : tube.insertionDir;
		ForgeDirection oth = player.isSneaking() ? tube.insertionDir : tube.ejectionDir;

		for(int i = 0; i < 7; i++) {
			rot = ForgeDirection.getOrientation((rot.ordinal() + 1) % 7);
			if(rot == ForgeDirection.UNKNOWN) break; //unknown is always valid, simply disables this part
			if(rot == oth) continue; //skip if both positions collide
			TileEntity tile = Compat.getTileStandard(world, x + rot.offsetX, y + rot.offsetY, z + rot.offsetZ);
			if(tile instanceof TileEntityPneumoTube) continue;
			if(tile instanceof IInventory) break; //valid if connected to an IInventory
		}

		if(player.isSneaking()) tube.ejectionDir = rot; else tube.insertionDir = rot;

		tube.markDirty();
		BlockPos pos=new BlockPos(x,y,z);
        world.notifyBlockUpdate(pos,world.getBlockState(pos),world.getBlockState(pos),3);

		return true;
	}

	public boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
		TileEntity tile = world instanceof World ? Compat.getTileStandard((World) world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) : world.getTileEntity(new BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ));
		return tile instanceof TileEntityPneumoTube;
	}

	public boolean canConnectToAir(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
		FluidType air = Fluids.AIR;
		TileEntity te = world.getTileEntity(new BlockPos(x,y,z));
		if(te instanceof TileEntityPneumoTube) {
			TileEntityPneumoTube tube = (TileEntityPneumoTube) te;
			if(!tube.isCompressor()) return false;
			if(tube.ejectionDir == dir || tube.insertionDir == dir) return false;
			air = tube.compair.getTankType();
		}
		TileEntity tile = world.getTileEntity(new BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ));
		if(tile instanceof TileEntityPneumoTube) return false;
		return Library.canConnectFluid(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir, air);
	}

    @Override public void addInformation(ItemStack stack,EntityPlayer player,List list,boolean ext){addStandardInfo(stack,player,list,ext);}
}
