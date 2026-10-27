package com.hbm.blocks.generic;

import java.io.IOException;
import net.minecraft.util.math.MathHelper;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.hbm.blocks.IBlockSideRotation;
import com.hbm.blocks.SideRotationModelState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.i18n.I18nUtil;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.NBTStructure.JigsawConnection;
import com.hbm.world.gen.nbt.SpawnCondition;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.JigsawPool;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

/**
 * You're familiar with Billy Mitchell, World Video Game Champion? He could probably do it.
 * So I gotta find a way to harness his power. And I think I've found a way.
 *
 * THAT'S RIGHT, WE'RE GONNA CHEAT.
 *
 * NBTStructures have the inherent flaws of the vanilla structure system: Structures are composed
 * before terrain gen even kicks in, placement order of components are arbitrary and certain
 * connected parts will fall apart due to unexpected variance in the terrain. Not good.
 * The solution: Simply delay generation of parts using a tile entity that checks if the chunks
 * in front of it are loaded, and then places a random part from the chosen pool. When this happens,
 * the player is usually still far far away so they'll be none the wiser. Chunk load checks help
 * prevent forced chunk loading and all the lag that comes with that.
 *
 * The system is named after tandem shaped charges: Make a hole with the first charge, then deliver
 * the actual payload.
 *
 * @author hbm, Mellow
 */
public class BlockWandTandem extends BlockContainer implements IBlockSideRotation, INBTBlockTransformable, IGUIProvider, ILookOverlay {

	private static final ResourceLocation blockTexture = new ResourceLocation(RefStrings.MODID, "blocks/wand_tandem");
	private static final ResourceLocation iconTop = new ResourceLocation(RefStrings.MODID, "blocks/wand_tandem_top");
	private static final ResourceLocation iconSide = new ResourceLocation(RefStrings.MODID, "blocks/wand_tandem_side");
	private static final ResourceLocation iconBack = new ResourceLocation(RefStrings.MODID, "blocks/wand_tandem_back");
	private static final Collection<ResourceLocation> MODEL_TEXTURES = Collections.unmodifiableList(Arrays.asList(blockTexture, iconTop, iconSide, iconBack));

	public BlockWandTandem() {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().withProperty(SideRotationModelState.METADATA, 0));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityWandTandem();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		int l = determinePlacementOrientation(pos, player);
		world.setBlockState(pos, getStateFromMeta(l), 2);
	}

	/** 1.7 piston placement in native feet-relative coordinates; deliberately not EnumFacing.getDirectionFromEntityLiving. */
	private static int determinePlacementOrientation(BlockPos pos, EntityLivingBase player) {
		if(MathHelper.abs((float) player.posX - pos.getX()) < 2.0F && MathHelper.abs((float) player.posZ - pos.getZ()) < 2.0F) {
			// The old posY - yOffset is the native entity feet position.
			double height = player.posY + 1.82D;
			if(height - pos.getY() > 2.0D) return 1;
			if(pos.getY() - height > 0.0D) return 0;
		}
		int yaw = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		return yaw == 0 ? 2 : (yaw == 1 ? 5 : (yaw == 2 ? 3 : (yaw == 3 ? 4 : 0)));
	}


	@Override
	public ResourceLocation getModelTexture(int side, int meta) {
		if(side == meta) return blockTexture;
		if(IBlockSideRotation.isOpposite(side, meta)) return iconBack;
		if(side <= 1) return iconTop;
		if(side > 3 && meta <= 1) return iconTop;
		return iconSide;
	}

	@Override
	public int getRotationFromSide(IBlockAccess world, int x, int y, int z, int side) {
		if(side == 0) return IBlockSideRotation.topToBottom(getRotationFromSide(world, x, y, z, 1));

		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
		if(side == meta || IBlockSideRotation.isOpposite(side, meta)) return 0;

		// downwards facing has no changes, upwards flips anything not handled already
		if(meta == 0) return 0;
		if(meta == 1) return 3;

		// top (and bottom) is rotated fairly normally
		if(side == 1) {
			switch(meta) {
			case 2: return 3;
			case 3: return 0;
			case 4: return 1;
			case 5: return 2;
			}
		}

		// you know what I aint explaining further, it's a fucking mess here
		if(meta == 2) return side == 4 ? 2 : 1;
		if(meta == 3) return side == 4 ? 1 : 2;
		if(meta == 4) return side == 2 ? 1 : 2;
		if(meta == 5) return side == 2 ? 2 : 1;

		return 0;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public Collection<ResourceLocation> getModelTextures() {
		return MODEL_TEXTURES;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return SideRotationModelState.create(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(SideRotationModelState.METADATA, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(SideRotationModelState.METADATA);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return SideRotationModelState.extend(state, world, pos);
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);

		if(!(te instanceof TileEntityWandTandem)) return false;

		TileEntityWandTandem jigsaw = (TileEntityWandTandem) te;

		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == Items.PAPER) {
			TileEntityWandTandem.copyMode = true;
			if(!player.getHeldItem(hand).hasTagCompound()) {
				player.getHeldItem(hand).setTagCompound(new NBTTagCompound());
				jigsaw.writeToNBT(player.getHeldItem(hand).getTagCompound());
			} else {
				jigsaw.readFromNBT(player.getHeldItem(hand).getTagCompound());
				jigsaw.markDirty();
			}
			TileEntityWandTandem.copyMode = false;
			return true;
		}

		if(!player.isSneaking()) {
			Block block = ModBlocks.getBlockFromStack(player.getHeldItem(hand));
			if(block == ModBlocks.wand_air) block = Blocks.AIR;

			if(block != null && !ModBlocks.isStructureBlock(block, false)) {
				jigsaw.replaceBlock = block;
				jigsaw.replaceMeta = player.getHeldItem(hand).getItemDamage();
				jigsaw.markDirty();

				return true;
			}

			if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.wand_s) return false;

			if(world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());

			return true;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GuiWandTandem((TileEntityWandTandem) world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(!(te instanceof TileEntityWandTandem)) return;
		TileEntityWandTandem jigsaw = (TileEntityWandTandem) te;

		List<String> text = new ArrayList<String>();

		text.add(TextFormatting.GRAY + "Target pool: " + TextFormatting.RESET + jigsaw.pool);
		text.add(TextFormatting.GRAY + "Target name: " + TextFormatting.RESET + jigsaw.target);
		text.add(TextFormatting.GRAY + "Turns into: " + TextFormatting.RESET + jigsaw.replaceBlock.getRegistryName().toString());
		text.add(TextFormatting.GRAY + "   with meta: " + TextFormatting.RESET + jigsaw.replaceMeta);
		text.add(TextFormatting.GRAY + "Joint type: " + TextFormatting.RESET + (jigsaw.isRollable ? "Rollable" : "Aligned"));

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}


	public static class TileEntityWandTandem extends TileEntityLoadedBase implements IControlReceiver, net.minecraft.util.ITickable {

		@Override
		public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
			return oldState.getBlock() != newState.getBlock();
		}


		public static boolean copyMode = false;

		private String pool = "default";
		private String target = "default";
		private Block replaceBlock = Blocks.AIR;
		private int replaceMeta = 0;
		private boolean isRollable = true; // sets joint type, rollable joints can be placed in any orientation for vertical jigsaw connections

		private boolean isArmed = false;
		private SpawnCondition structure;

		@Override
		public void update() {
			if(this.isInvalid()) return;
			if(!(this.world.getBlockState(this.pos).getBlock() instanceof BlockWandTandem)) {
				this.invalidate();
				if(this.hasWorld()) {
					this.world.removeTileEntity(this.pos);
				}
				return;
			}
			if(!this.world.isRemote) {
				tryGenerate();
				networkPackNT(15);
			}
		}

		private void tryGenerate() {
			if(!this.isArmed || target == null || target.isEmpty() || pool == null || pool.isEmpty()) return;

			JigsawPool pool = structure.getPool(this.pool);
			if(pool == null) return;

			JigsawPiece nextPiece = pool.get(this.world.rand);
			if(nextPiece == null) return;

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());

			List<JigsawConnection> connectionPool = nextPiece.structure.getConnectionPool(dir, target);
			if(connectionPool == null) return;

			JigsawConnection toConnection = connectionPool.get(this.world.rand.nextInt(connectionPool.size()));
			int nextCoordBase = directionOffsetToCoordBase(dir.getOpposite(), toConnection.dir);

			BlockPos pos = new BlockPos(this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ);

			// offset the starting point to the connecting point
			int ox = nextPiece.structure.rotateX(toConnection.pos.x, toConnection.pos.z, nextCoordBase);
			int oy = toConnection.pos.y;
			int oz = nextPiece.structure.rotateZ(toConnection.pos.x, toConnection.pos.z, nextCoordBase);

			nextPiece.structure.build(this.world, nextPiece, pos.getX() - ox, pos.getY() - oy, pos.getZ() - oz, nextCoordBase, structure.name);

			this.world.setBlockState(this.pos, replaceBlock.getStateFromMeta(replaceMeta), 2);
		}

		private int directionOffsetToCoordBase(ForgeDirection from, ForgeDirection to) {
			for(int i = 0; i < 4; i++) {
				if(from == to) return i % 4;
				from = from.getRotation(ForgeDirection.DOWN);
			}
			return 0;
		}

		@Override
		public void serialize(ByteBuf buf) {
			BufferUtil.writeString(buf, pool);
			BufferUtil.writeString(buf, target);
			buf.writeInt(Block.getIdFromBlock(replaceBlock));
			buf.writeInt(replaceMeta);
			buf.writeBoolean(isRollable);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			pool = BufferUtil.readString(buf);
			target = BufferUtil.readString(buf);
			replaceBlock = Block.getBlockById(buf.readInt());
			replaceMeta = buf.readInt();
			isRollable = buf.readBoolean();
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			if(!copyMode) {
				super.writeToNBT(nbt);
				nbt.setInteger("direction", this.getBlockMetadata());
				if(isArmed) {
					nbt.setBoolean("isArmed", isArmed);
					nbt.setString("structure", structure.name);
				}
			}

			nbt.setString("pool", pool);
			nbt.setString("target", target);
			nbt.setString("block", replaceBlock.getRegistryName().toString());
			nbt.setInteger("meta", replaceMeta);
			nbt.setBoolean("roll", isRollable);
			return nbt;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			if(!copyMode) {
				super.readFromNBT(nbt);
				isArmed = nbt.getBoolean("isArmed");
				structure = NBTStructure.getStructure(nbt.getString("structure"));
			}

			pool = nbt.getString("pool");
			target = nbt.getString("target");
			replaceBlock = Block.getBlockFromName(nbt.getString("block"));
			replaceMeta = nbt.getInteger("meta");
			isRollable = nbt.getBoolean("roll");
		}

		@Override
		public boolean hasPermission(EntityPlayer player) {
			return true;
		}

		@Override
		public void receiveControl(NBTTagCompound nbt) {
			readFromNBT(nbt);
			markDirty();
		}

		public void arm(SpawnCondition structure) {
			isArmed = true;
			this.structure = structure;
		}

	}

	public static class GuiWandTandem extends GuiScreen {

		private final TileEntityWandTandem jigsaw;

		private GuiTextField textPool;
		private GuiTextField textTarget;

		private GuiButton jointToggle;

		public GuiWandTandem(TileEntityWandTandem jigsaw) {
			this.jigsaw = jigsaw;
		}

		@Override
		public void initGui() {
			Keyboard.enableRepeatEvents(true);

			textPool = new GuiTextField(0, fontRenderer, this.width / 2 - 150, 50, 300, 20);
			textPool.setText(jigsaw.pool);

			textTarget = new GuiTextField(1, fontRenderer, this.width / 2 + 10, 100, 140, 20);
			textTarget.setText(jigsaw.target);

			jointToggle = new GuiButton(0, this.width / 2 + 60, 150, 90, 20, jigsaw.isRollable ? "Rollable" : "Aligned");
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			drawDefaultBackground();

			drawString(fontRenderer, "Target pool:", this.width / 2 - 150, 37, 0xA0A0A0);
			textPool.drawTextBox();

			drawString(fontRenderer, "Target name:", this.width / 2 + 10, 87, 0xA0A0A0);
			textTarget.drawTextBox();

			drawString(fontRenderer, "Joint type:", this.width / 2 + 60, 137, 0xA0A0A0);
			jointToggle.drawButton(mc, mouseX, mouseY, partialTicks);

			super.drawScreen(mouseX, mouseY, partialTicks);
		}

		@Override
		public void onGuiClosed() {
			Keyboard.enableRepeatEvents(false);

			NBTTagCompound data = new NBTTagCompound();
			jigsaw.writeToNBT(data);

			data.setString("pool", textPool.getText());
			data.setString("target", textTarget.getText());
			data.setBoolean("roll", jointToggle.displayString == "Rollable");

			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, jigsaw.getPos().getX(), jigsaw.getPos().getY(), jigsaw.getPos().getZ()));
		}

		@Override
		protected void keyTyped(char typedChar, int keyCode) throws IOException {
			super.keyTyped(typedChar, keyCode);
			textPool.textboxKeyTyped(typedChar, keyCode);
			textTarget.textboxKeyTyped(typedChar, keyCode);
		}

		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			textPool.mouseClicked(mouseX, mouseY, mouseButton);
			textTarget.mouseClicked(mouseX, mouseY, mouseButton);

			if(jointToggle.mousePressed(mc, mouseX, mouseY)) {
				jointToggle.displayString = jointToggle.displayString == "Rollable" ? "Aligned" : "Rollable";
			}
		}

		@Override public boolean doesGuiPauseGame() { return false; }
	}
}
