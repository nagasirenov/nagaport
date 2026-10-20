package com.hbm.blocks.generic;

import com.hbm.world.gen.util.LogicBlockActions;
import com.hbm.world.gen.util.LogicBlockConditions;
import com.hbm.world.gen.util.LogicBlockInteractions;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

import java.util.function.Consumer;
import java.util.function.Function;

public class LogicBlock extends BlockContainer {

	public LogicBlock() {
		super(Material.ROCK);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new LogicBlock.TileEntityLogicBlock();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if(tile instanceof LogicBlock.TileEntityLogicBlock){
			LogicBlock.TileEntityLogicBlock logicBlock = (LogicBlock.TileEntityLogicBlock) tile;
			if(logicBlock.disguise != null){
				return logicBlock.disguise.getIcon(side, logicBlock.disguiseMeta);
			}
		}

		return super.getIcon(world, x, y, z, side);
	}

	@Override
	public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
		TileEntity te = worldIn.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		if(te instanceof LogicBlock.TileEntityLogicBlock && ((LogicBlock.TileEntityLogicBlock) te).interaction != null) {
			((LogicBlock.TileEntityLogicBlock) te).interaction.accept(new Object[]{worldIn, te, x, y, z, player, side, subX, subY, subZ});
			return true;
		}

		return super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
	}

	public static class TileEntityLogicBlock extends TileEntity implements net.minecraft.util.ITickable {

		//phase is incremented per condition check, timer counts since last condition check by default
		public int phase = 0;
		public int timer = 0;

		public Block disguise;
		public int disguiseMeta;

		/**Actions always get called before conditions, use the phase and timer variables in order to control behavior via conditions*/
		public String conditionID = "PLAYER_CUBE_5";
		public String actionID = "FODDER_WAVE";
		/**Interactions are called on right click, and passes on the parameters of the right click to consumer*/
		public String interactionID;

		public Function<LogicBlock.TileEntityLogicBlock, Boolean> condition;
		public Consumer<LogicBlock.TileEntityLogicBlock> action;
		/**Consists of world instance, TileEntity instance, three ints for coordinates, one int for block side, and player instance, in that order **/
		public Consumer<Object[]> interaction;

		public EntityPlayer player;

		public ForgeDirection direction = ForgeDirection.UNKNOWN;
		@Override
		public void update() {

			if(!this.world.isRemote) {
				if(action == null){
					action = LogicBlockActions.actions.get(actionID);
				}
				if(condition == null){
					condition = LogicBlockConditions.conditions.get(conditionID);
				}
				if(interaction == null && interactionID != null){
					interaction = LogicBlockInteractions.interactions.get(interactionID);
				}

				if(action == null || condition == null){
					{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX(); int nativePlacementY = this.pos.getY(); int nativePlacementZ = this.pos.getZ(); net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
					return;
				}
				action.accept(this);
				if(condition.apply(this)) {
					phase++;
					timer = 0;
				} else {
					timer++;
				}
			}
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("phase", phase);

			nbt.setString("actionID", actionID);
			nbt.setString("conditionID", conditionID);
			if(interactionID != null)
				nbt.setString("interactionID", interactionID);

			nbt.setInteger("direction", direction.ordinal());
			if(disguise != null){
				nbt.setInteger("disguiseMeta", disguiseMeta);
				nbt.setString("disguise", GameRegistry.findUniqueIdentifierFor(disguise).toString());
			}
			return nbt;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.phase = nbt.getInteger("phase");

			this.actionID = nbt.getString("actionID");
			this.conditionID = nbt.getString("conditionID");
			if(nbt.hasKey("interactionID")) this.interactionID = nbt.getString("interactionID");

			this.direction = ForgeDirection.getOrientation(nbt.getInteger("direction"));

			if(nbt.hasKey("disguise")){
				disguiseMeta = nbt.getInteger("disguiseMeta");
				disguise = Block.getBlockFromName(nbt.getString("disguise"));
			}
		}

		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}
	}

}
