package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.EnumMap;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.ClientConfig;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemRebarPlacer;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.main.ServerProxy;
import com.hbm.render.block.ISBRHUniversal;
import com.hbm.render.block.nativeblock.UniversalCuboidGeometry;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.PersistentRegistryRef;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.RebarNetwork;
import com.hbm.uninos.networkproviders.RebarNetworkProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;
import com.hbm.util.InventoryUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluidmk2.IFluidReceiverMK2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockRebar extends BlockContainer implements ISBRHUniversal {

	public BlockRebar() {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().withProperty(UniversalBlockState.META, 0));
	}

	@Override protected BlockStateContainer createBlockState() { return UniversalBlockState.create(this); }
	@Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(UniversalBlockState.META, meta & 15); }
	@Override public int getMetaFromState(IBlockState state) { return UniversalBlockState.metadata(state); }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRebar();
	}

	@Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
	@Override public boolean isOpaqueCube(IBlockState state) { return false; }
	@Override public boolean isFullCube(IBlockState state) { return false; }
	@Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, net.minecraft.util.math.BlockPos pos, EnumFacing face) { return BlockFaceShape.UNDEFINED; }

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, net.minecraft.util.math.BlockPos pos, Block block, net.minecraft.util.math.BlockPos fromPos) {
		TileEntity tile = world.getTileEntity(pos);
		if(!(tile instanceof TileEntityRebar)) return;

		TileEntityRebar rebar = (TileEntityRebar) tile;
		rebar.hasConnection = false;

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity neighbor = Compat.getTileStandard(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);
			if(neighbor instanceof TileEntityPipeBaseNT) {
				rebar.hasConnection = true;
				return;
			}
		}
	}

	public static class TileEntityRebar extends TileEntityLoadedBase implements IFluidReceiverMK2, IBufPacketReceiver, net.minecraft.util.ITickable {

		public Block concrete;
		public int concreteMeta;
		public int progress;
		public int prevProgress;
		protected RebarNode node;
		public boolean hasConnection = false;
		private NBTTagCompound legacyRef;
		
		public TileEntityRebar setup(Block b, int m) {
			this.concrete = b;
			this.concreteMeta = m;
			return this;
		}

		@Override
		public boolean shouldRefresh(World world, net.minecraft.util.math.BlockPos pos, IBlockState oldState, IBlockState newState) {
			return oldState.getBlock() != newState.getBlock();
		}

		@Override
		public void update() {

			long time = this.world.getTotalWorldTime();

			if(!this.world.isRemote) {

				if(prevProgress != progress) {
					this.world.markChunkDirty(this.pos, this);
					prevProgress = progress;
				}
				
				if(this.progress >= 1_000) {
					if(concrete != null && ItemRebarPlacer.isValidConk(Item.getItemFromBlock(concrete), concreteMeta)) {
						this.world.setBlockState(this.pos, concrete.getStateFromMeta(concreteMeta), 3);
					} else {
						this.world.setBlockState(this.pos, ModBlocks.concrete_rebar.getDefaultState());
					}
					return;
				}
				
				if(time % 60 == 0) {
					for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
						this.trySubscribe(Fluids.CONCRETE, this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);
					}
				}

				if(this.node == null || this.node.expired) {

					this.node = (RebarNode) UniNodespace.getNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), RebarNetworkProvider.THE_PROVIDER);

					if(this.node == null || this.node.expired) {
						this.node = this.createNode();
						UniNodespace.createNode(this.world, this.node);
					}
				}
				
				this.networkPackNT(100);
			}
		}
		
		@Override
		public void invalidate() {
			super.invalidate();

			if(!this.world.isRemote) {
				if(this.node != null) {
					UniNodespace.destroyNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), RebarNetworkProvider.THE_PROVIDER);
				}
			}
		}

		@Override public void serialize(ByteBuf buf) { buf.writeInt(progress); }
		@Override public void deserialize(ByteBuf buf) { this.progress = buf.readInt(); }

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.progress = nbt.getInteger("progress");
			this.hasConnection = nbt.getBoolean("hasConnection");
			
			this.concrete = PersistentRegistryRef.readBlock(nbt, "block");
			this.concreteMeta = nbt.getInteger("meta");
			this.legacyRef = PersistentRegistryRef.legacyOnly(nbt, "block", "meta");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("progress", this.progress);
			nbt.setBoolean("hasConnection", this.hasConnection);
			
			if(this.concrete != null) {
				PersistentRegistryRef.writeBlock(nbt, "block", this.concrete);
				nbt.setInteger("meta", this.concreteMeta);
			} else {
				PersistentRegistryRef.preserveLegacy(this.legacyRef, nbt, "block", "meta");
			}
			return nbt;
		}
		
		public RebarNode createNode() {
			TileEntity tile = (TileEntity) this;
			return new RebarNode(RebarNetworkProvider.THE_PROVIDER, new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(
					new DirPos(tile.getPos().getX() + 1, tile.getPos().getY(), tile.getPos().getZ(), Library.POS_X),
					new DirPos(tile.getPos().getX() - 1, tile.getPos().getY(), tile.getPos().getZ(), Library.NEG_X),
					new DirPos(tile.getPos().getX(), tile.getPos().getY() + 1, tile.getPos().getZ(), Library.POS_Y),
					new DirPos(tile.getPos().getX(), tile.getPos().getY() - 1, tile.getPos().getZ(), Library.NEG_Y),
					new DirPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ() + 1, Library.POS_Z),
					new DirPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ() - 1, Library.NEG_Z)
					);
		}

		@Override
		public FluidTank[] getAllTanks() {
			FluidTank tank = new FluidTank(Fluids.CONCRETE, 1_000);
			tank.setFill(progress);
			return new FluidTank[] {tank};
		}

		@Override
		public long transferFluid(FluidType type, int pressure, long amount) {
			if(type != Fluids.CONCRETE) return amount;
			if(this.node == null || this.node.expired || !this.node.hasValidNet()) return amount;
			
			List<TileEntityRebar> lowestLinks = new ArrayList();
			int lowestY = 256;
			int progress = 0;
			int capacity = 0;
			
			for(Object o : this.node.net.links) {
				RebarNode node = (RebarNode) o;
				int y = node.positions[0].getY(); //rebar can only have one pos, there's no multiblock rebar
				
				if(y < lowestY) {
					lowestY = y;
					progress = 0;
					capacity = 0;
					lowestLinks.clear();
				}
				
				if(y == lowestY) {
					TileEntity tile = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(node.positions[0].getX(), y, node.positions[0].getZ()));
					if(!(tile instanceof TileEntityRebar)) continue;
					
					TileEntityRebar rebar = (TileEntityRebar) tile;
					
					progress += rebar.progress;
					capacity += 1_000;
					lowestLinks.add(rebar);
				}
			}
			
			if(capacity > 0 && !lowestLinks.isEmpty()) {
				int maxSpeed = 50;
				int maxAccept = (int) BobMathUtil.min(capacity - progress, amount, maxSpeed * lowestLinks.size());
				int target = Math.min((progress + maxAccept) / lowestLinks.size(), 1_000);
				
				for(TileEntityRebar rebar : lowestLinks) {
					if(rebar.progress >= target) continue;
					int delta = target - rebar.progress;
					if(delta > amount) continue;
					
					rebar.progress += delta;
					amount -= delta;
				}
			}
			
			return amount;
		}

		@Override
		public long getDemand(FluidType type, int pressure) {
			return 10_000;
		}
	}
	
	public static class RebarNode extends GenNode<RebarNetwork> {

		public RebarNode(INetworkProvider<RebarNetwork> provider, BlockPos... positions) {
			super(provider, positions);
		}
		
		@Override
		public RebarNode setConnections(DirPos... connections) {
			super.setConnections(connections);
			return this;
		}
	}

	@Override
	public List<AxisAlignedBB> getRenderBoxes(IBlockState state, boolean inventory) {
		List<AxisAlignedBB> boxes = new ArrayList<>();
		double o = 0.25D;
		double min = inventory ? 0D : -0.001D;
		double max = inventory ? 1D : 1.001D;
		if(!inventory && ClientConfig.RENDER_REBAR_SIMPLE.get()) {
			boxes.add(new AxisAlignedBB(0.4375D, min, 0.4375D, 0.5625D, max, 0.5625D));
			boxes.add(new AxisAlignedBB(min, 0.4375D, 0.4375D, max, 0.5625D, 0.5625D));
			boxes.add(new AxisAlignedBB(0.4375D, 0.4375D, min, 0.5625D, 0.5625D, max));
		} else {
			boxes.add(new AxisAlignedBB(0.4375D - o, min, 0.4375D - o, 0.5625D - o, max, 0.5625D - o));
			boxes.add(new AxisAlignedBB(0.4375D - o, min, 0.4375D + o, 0.5625D - o, max, 0.5625D + o));
			boxes.add(new AxisAlignedBB(0.4375D + o, min, 0.4375D - o, 0.5625D + o, max, 0.5625D - o));
			boxes.add(new AxisAlignedBB(0.4375D + o, min, 0.4375D + o, 0.5625D + o, max, 0.5625D + o));

			boxes.add(new AxisAlignedBB(min, 0.4375D - o, 0.4375D - o, max, 0.5625D - o, 0.5625D - o));
			boxes.add(new AxisAlignedBB(min, 0.4375D - o, 0.4375D + o, max, 0.5625D - o, 0.5625D + o));
			boxes.add(new AxisAlignedBB(min, 0.4375D + o, 0.4375D - o, max, 0.5625D + o, 0.5625D - o));
			boxes.add(new AxisAlignedBB(min, 0.4375D + o, 0.4375D + o, max, 0.5625D + o, 0.5625D + o));

			boxes.add(new AxisAlignedBB(0.4375D - o, 0.4375D - o, min, 0.5625D - o, 0.5625D - o, max));
			boxes.add(new AxisAlignedBB(0.4375D - o, 0.4375D + o, min, 0.5625D - o, 0.5625D + o, max));
			boxes.add(new AxisAlignedBB(0.4375D + o, 0.4375D - o, min, 0.5625D + o, 0.5625D - o, max));
			boxes.add(new AxisAlignedBB(0.4375D + o, 0.4375D + o, min, 0.5625D + o, 0.5625D + o, max));
		}
		
		return boxes;
	}

	@SideOnly(Side.CLIENT)
	private static IBakedModel concreteModel(TextureAtlasSprite sprite, int progress) {
		List<BakedQuad> quads = UniversalCuboidGeometry.bake(Collections.singletonList(
				new AxisAlignedBB(0, 0, 0, 1, progress / 1000D, 1)), sprite, false, 0);
		EnumMap<EnumFacing, List<BakedQuad>> faces = new EnumMap<>(EnumFacing.class);
		for(EnumFacing face : EnumFacing.values()) faces.put(face, Collections.emptyList());
		return new SimpleBakedModel(quads, faces, true, true, sprite, ItemCameraTransforms.DEFAULT, ItemOverrideList.NONE);
	}

	// from ModEventHandlerClient.onRenderWorldLastEvent
	@SideOnly(Side.CLIENT)
	public static void renderRebar(List tiles, float interp) {
		
		List<TileEntityRebar> rebars = new ArrayList();
		for(Object o : tiles) {
			if(!(o instanceof TileEntityRebar)) continue;
			TileEntityRebar rebar = (TileEntityRebar) o;
			if(rebar.progress > 0) rebars.add(rebar);
			if(rebars.size() >= ClientConfig.RENDER_REBAR_LIMIT.get()) break;
		}

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		World world = mc.world;

		double dx = player.prevPosX + (player.posX - player.prevPosX) * interp;
		double dy = player.prevPosY + (player.posY - player.prevPosY) * interp;
		double dz = player.prevPosZ + (player.posZ - player.prevPosZ) * interp;
		
		if(!rebars.isEmpty()) {
			
			GL11.glPushMatrix();
			GL11.glShadeModel(GL11.GL_SMOOTH);
			//RenderHelper.enableStandardItemLighting();

			EntityRenderer entityRenderer = mc.entityRenderer;
			entityRenderer.enableLightmap();

			TextureAtlasSprite concrete = mc.getTextureMapBlocks().getAtlasSprite(RefStrings.MODID + ":blocks/concrete_liquid");
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buffer = tess.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

			for(TileEntityRebar rebar : rebars) {
				buffer.setTranslation(-dx, -dy, -dz);
				mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world,
						concreteModel(concrete, rebar.progress), ModBlocks.rebar.getDefaultState(), rebar.getPos(), buffer, false);
			}

			tess.draw();
			buffer.setTranslation(0, 0, 0);
			entityRenderer.disableLightmap();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glPopMatrix();
		}
		
		ItemStack held = player.getHeldItemMainhand();
		if(!held.isEmpty() && held.getItem() == ModItems.rebar_placer && held.hasTagCompound() &&
				held.getTagCompound().hasKey("pos") && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {

			int[] pos = held.getTagCompound().getIntArray("pos");
			RayTraceResult mop = mc.objectMouseOver;
			net.minecraft.util.math.BlockPos adjacent = mop.getBlockPos().offset(mop.sideHit);
			int iX = adjacent.getX();
			int iY = adjacent.getY();
			int iZ = adjacent.getZ();

			double minX = Math.min(pos[0], iX) + 0.125;
			double maxX = Math.max(pos[0], iX) + 0.875;
			double minY = Math.min(pos[1], iY) + 0.125;
			double maxY = Math.max(pos[1], iY) + 0.875;
			double minZ = Math.min(pos[2], iZ) + 0.125;
			double maxZ = Math.max(pos[2], iZ) + 0.875;
			
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor3f(1F, 1F, 1F);
			
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buffer = tess.getBuffer();
			buffer.setTranslation(-dx, -dy, -dz);
			// UV0 is unused with texturing disabled, but native UV1 lightmap indexing requires it.
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.BLOCK);
			appendSelectionBox(buffer, minX, minY, minZ, maxX, maxY, maxZ);
			tess.draw();
			buffer.setTranslation(0, 0, 0);

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
			
			int rebarLeft = InventoryUtil.countAStackMatches(player, new ComparableStack(ModBlocks.rebar), true);
			int rebarRequired = (Math.max(pos[0], iX) - Math.min(pos[0], iX) + 1) * (Math.max(pos[1], iY) - Math.min(pos[1], iY) + 1) * (Math.max(pos[2], iZ) - Math.min(pos[2], iZ) + 1);
			MainRegistry.proxy.displayTooltip((rebarRequired > rebarLeft ? TextFormatting.RED : TextFormatting.GREEN) + (rebarLeft + " / " + rebarRequired), 1_000, ServerProxy.ID_CABLE);
		}
	}
	@SideOnly(Side.CLIENT)
	private static void appendSelectionBox(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			// top
			buffer.pos(minX, maxY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(minX, maxY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			
			buffer.pos(minX, maxY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(maxX, maxY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			
			buffer.pos(maxX, maxY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(maxX, maxY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();

			buffer.pos(maxX, maxY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(minX, maxY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			
			// bottom
			buffer.pos(minX, minY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(minX, minY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			
			buffer.pos(minX, minY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(maxX, minY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			
			buffer.pos(maxX, minY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(maxX, minY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();

			buffer.pos(maxX, minY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(minX, minY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();

			// sides
			buffer.pos(minX, minY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(minX, maxY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();

			buffer.pos(maxX, minY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(maxX, maxY, minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();

			buffer.pos(maxX, minY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(maxX, maxY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();

			buffer.pos(minX, minY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			buffer.pos(minX, maxY, maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(0, 240).endVertex();
			
	}

}
