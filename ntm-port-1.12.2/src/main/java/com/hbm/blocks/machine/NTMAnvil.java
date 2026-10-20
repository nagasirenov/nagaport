package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.hbm.blocks.BlockFallingNT;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.container.ContainerAnvil;
import com.hbm.inventory.gui.GUIAnvil;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Native block callbacks. The real anvil GUI/container collaborators remain separate migration work. */
public class NTMAnvil extends BlockFallingNT implements ITooltipProvider, IGUIProvider {
    public static final int TIER_IRON = 1, TIER_STEEL = 2, TIER_OIL = 3, TIER_NUCLEAR = 4,
            TIER_RBMK = 5, TIER_FUSION = 6, TIER_PARTICLE = 7, TIER_GERALD = 8;
    public final int tier;
    public static final HashMap<Integer, List<NTMAnvil>> tierMap = new HashMap<>();
    private static final AxisAlignedBB EAST_WEST = new AxisAlignedBB(0, 0, .25, 1, .75, .75);
    private static final AxisAlignedBB NORTH_SOUTH = new AxisAlignedBB(.25, 0, 0, .75, .75, 1);
    public NTMAnvil(Material material, int tier) {
        super(material);
        setSoundType(SoundType.ANVIL);
        setHardness(5F);
        setResistance(100F);
        this.tier = tier;
        tierMap.computeIfAbsent(tier, key -> new ArrayList<>()).add(this);
    }
    public static List<ItemStack> getAnvilsFromTier(int tier) {
        List<ItemStack> result = new ArrayList<>();
        List<NTMAnvil> anvils = tierMap.get(tier);
        if(anvils != null) for(NTMAnvil anvil : anvils) result.add(new ItemStack(anvil));
        return result;
    }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ) {
        if(world.isRemote) return true;
        if(player.isSneaking()) return false;
        player.openGui(MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        int direction = MathHelper.floor(placer.rotationYaw * 4F / 360F + .5D) & 3;
        world.setBlockState(pos, state.withProperty(META, new int[] {3, 4, 2, 5}[direction]), 2);
    }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        int meta = getMetaFromState(state);
        if(meta == 2 || meta == 3) return EAST_WEST;
        if(meta == 4 || meta == 5) return NORTH_SOUTH;
        // Legacy invalid metadata inherited mutable bounds; use the initial full-cube bounds deterministically.
        return FULL_BLOCK_AABB;
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }
    @Override public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extended) {
        list.add(TextFormatting.GOLD + "Tier " + tier + " Anvil");
    }
    @Override public Container provideContainer(int id, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerAnvil(player.inventory, ((NTMAnvil)world.getBlockState(new BlockPos(x, y, z)).getBlock()).tier);
    }
    @Override @SideOnly(Side.CLIENT)
    public Object provideGUI(int id, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIAnvil(player.inventory, ((NTMAnvil)world.getBlockState(new BlockPos(x, y, z)).getBlock()).tier);
    }
}
