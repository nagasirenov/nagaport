package com.hbm.items.tool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.ClientConfig;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.handler.ability.AvailableAbilities;
import com.hbm.handler.ability.IBaseAbility;
import com.hbm.handler.ability.IToolAreaAbility;
import com.hbm.handler.ability.IToolHarvestAbility;
import com.hbm.handler.ability.ToolPreset;
import com.hbm.interfaces.IItemHUD;
import com.hbm.inventory.gui.GUIScreenToolAbility;
import com.hbm.items.IItemControlReceiver;
import com.hbm.items.IKeybindReceiver;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.Tuple.Pair;

import api.hbm.item.IDepthRockTool;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IShearable;

public class ItemToolAbility extends ItemTool implements IDepthRockTool, IGUIProvider, IItemControlReceiver, IKeybindReceiver, IItemHUD {

	protected boolean isShears = false;
	protected EnumToolType toolType;
	protected EnumRarity rarity = EnumRarity.COMMON;
	//was there a reason for this to be private?
	protected float damage;
	private final float toolEfficiency;
	protected double movement;
	protected AvailableAbilities availableAbilities = new AvailableAbilities().addToolAbilities();
	protected boolean rockBreaker = false;

	public static enum EnumToolType {

		PICKAXE(
				Sets.newHashSet(new Material[] { Material.IRON, Material.ANVIL, Material.ROCK, Material.GLASS }),
				Sets.newHashSet(new Block[] { Blocks.COBBLESTONE, Blocks.DOUBLE_STONE_SLAB, Blocks.STONE_SLAB, Blocks.STONE, Blocks.SANDSTONE, Blocks.MOSSY_COBBLESTONE, Blocks.IRON_ORE, Blocks.IRON_BLOCK, Blocks.COAL_ORE, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.DIAMOND_ORE, Blocks.DIAMOND_BLOCK, Blocks.ICE, Blocks.NETHERRACK, Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK, Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL, Blocks.ACTIVATOR_RAIL })
		),
		AXE(
				Sets.newHashSet(new Material[] { Material.WOOD, Material.PLANTS, Material.VINE }),
				Sets.newHashSet(new Block[] { Blocks.PLANKS, Blocks.BOOKSHELF, Blocks.LOG, Blocks.LOG2, Blocks.CHEST, Blocks.PUMPKIN, Blocks.LIT_PUMPKIN })
		),
		SHOVEL(
				Sets.newHashSet(new Material[] { Material.CLAY, Material.SAND, Material.GROUND, Material.SNOW, Material.CRAFTED_SNOW }),
				Sets.newHashSet(new Block[] { Blocks.GRASS, Blocks.DIRT, Blocks.SAND, Blocks.GRAVEL, Blocks.SNOW_LAYER, Blocks.SNOW, Blocks.CLAY, Blocks.FARMLAND, Blocks.SOUL_SAND, Blocks.MYCELIUM })
		),
		MINER(
				Sets.newHashSet(new Material[] { Material.GRASS, Material.IRON, Material.ANVIL, Material.GLASS, Material.ROCK, Material.CLAY, Material.SAND, Material.GROUND, Material.SNOW, Material.CRAFTED_SNOW })
		);

		private EnumToolType(Set<Material> materials) {
			this.materials = materials;
		}

		private EnumToolType(Set<Material> materials, Set<Block> blocks) {
			this.materials = materials;
			this.blocks = blocks;
		}

		public Set<Material> materials = new HashSet();
		public Set<Block> blocks = new HashSet();
	}

	public ItemToolAbility setShears() {
		this.isShears = true;
		return this;
	}

	public ItemToolAbility(float damage, double movement, ToolMaterial material, EnumToolType type) {
		super(material, type.blocks);
		this.damage = damage;
		this.toolEfficiency = material.getEfficiency();
		this.movement = movement;
		this.toolType = type;

		// hacky workaround, might be good to rethink this entire system
		if(type == EnumToolType.MINER) {
			this.setHarvestLevel("pickaxe", material.getHarvestLevel());
			this.setHarvestLevel("shovel", material.getHarvestLevel());
		} else {
			this.setHarvestLevel(type.toString().toLowerCase(Locale.US), material.getHarvestLevel());
		}
	}

	public ItemToolAbility addAbility(IBaseAbility ability, int level) {
		this.availableAbilities.addAbility(ability, level);

		return this;
	}

	public ItemToolAbility setDepthRockBreaker() {
		this.rockBreaker = true;
		return this;
	}

	// <insert obvious Rarity joke here>
	public ItemToolAbility setRarity(EnumRarity rarity) {
		this.rarity = rarity;
		return this;
	}

	public EnumRarity getRarity(ItemStack stack) {
		return this.rarity != EnumRarity.COMMON ? this.rarity : super.getRarity(stack);
	}

	public boolean hitEntity(ItemStack stack, EntityLivingBase victim, EntityLivingBase attacker) {

		if(!attacker.world.isRemote && attacker instanceof EntityPlayer && canOperate(stack)) {

			this.availableAbilities.getWeaponAbilities().forEach((ability, level) -> {
				ability.onHit(level, attacker.world, (EntityPlayer) attacker, victim, this);
			});
		}

		stack.damageItem(1, attacker);

		return true;
	}

	// Should be safe, considering the AoE ability does a similar trick already.
	// If not, wrap this in a ThreadLocal or something...
	public static int dropX, dropY, dropZ;

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {

		World world = player.world;
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		IBlockState state = com.hbm.world.WorldBlockState.read(world, x, y, z);
		Block block = state.getBlock();

		/*
		 * The original implementation of this always returned FALSE which uses the vanilla block break code.
		 * This one now returns TRUE when an ability applies and instead relies on breakExtraBlock, which has the minor
		 * issue of only running on the sever, while the client uses the vanilla implementation. breakExtraBlock was only
		 * meant to be used for AoE or vein miner and not for the block that's being mined, hence break EXTRA block.
		 * The consequence was that the server would fail to break keyholes since breakExtraBlock is supposed to exclude
		 * them, while the client happily removes the block, causing a desync.
		 *
		 * Since keyholes aren't processable and exempt from silk touch anyway, we just default to the vanilla implementation in every case.
		 */
		if(block == ModBlocks.stone_keyhole || block == ModBlocks.stone_keyhole_meta) return false;

		if(!world.isRemote && (canHarvestBlockWithStack(state, stack) || canShearBlock(block, stack, world, pos)) && canOperate(stack)) {
			Configuration config = getConfiguration(stack);
			ToolPreset preset = config.getActivePreset();

			dropX = x;
			dropY = y;
			dropZ = z;

			preset.harvestAbility.preHarvestAll(preset.harvestAbilityLevel, world, player);

			boolean skipRef = preset.areaAbility.onDig(preset.areaAbilityLevel, world, x, y, z, player, this);

			if(!skipRef) {
				breakExtraBlock(world, x, y, z, player, x, y, z);
			}

			preset.harvestAbility.postHarvestAll(preset.harvestAbilityLevel, world, player);

			return true;
		}

		return false;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase player) {
		return super.onBlockDestroyed(stack, world, state, pos, player);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state) {

		if(!canOperate(stack))
			return 1;

		if(toolType == null)
			return super.getDestroySpeed(stack, state);

		if(toolType.blocks.contains(state.getBlock()) || toolType.materials.contains(state.getMaterial()))
			return this.toolEfficiency;

		return super.getDestroySpeed(stack, state);
	}

	public boolean canOperate(ItemStack stack) {
		return true;
	}
	private boolean canHarvestBlockWithStack(IBlockState state, ItemStack stack) {

		if(!canOperate(stack))
			return false;

		if(this.getConfiguration(stack).getActivePreset().harvestAbility == IToolHarvestAbility.SILK)
			return true;

		return getDestroySpeed(stack, state) > 1;
	}

	@Override
	public boolean canBreakRock(World world, EntityPlayer player, ItemStack tool, Block block, int x, int y, int z) {
		return canOperate(tool) && this.rockBreaker;
	}

	public boolean canShearBlock(Block block, ItemStack stack, World world, BlockPos pos) {
		return this.isShears(stack) && block instanceof IShearable && ((IShearable) block).isShearable(stack, world, pos);
	}

	public boolean isShears(ItemStack stack) {
		return this.isShears;
	}

	@Override
	public Multimap getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot) {

		Multimap multimap = HashMultimap.create();
		if(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND) return multimap;
		multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", (double) this.damage, 0));
		multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", movement, 1));
		return multimap;
	}

	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return stack.isItemEnchanted() || !getConfiguration(stack).getActivePreset().isNone();
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		availableAbilities.addInformation(list);

		if(this.rockBreaker) {
			list.add("");
			list.add(TextFormatting.RED + "Can break depth rock!");
		}
	}

	public void breakExtraBlock(World world, int x, int y, int z, EntityPlayer playerEntity, int refX, int refY, int refZ) {

		BlockPos pos = new BlockPos(x, y, z);
		if(world.isAirBlock(pos))
			return;

		if(!(playerEntity instanceof EntityPlayerMP))
			return;

		EntityPlayerMP player = (EntityPlayerMP) playerEntity;
		ItemStack stack = com.hbm.util.NativeHeldItem.getMainHandNullable(player);

		if(stack == null) {
			return;
		}

		IBlockState state = com.hbm.world.WorldBlockState.read(world, x, y, z);
		Block block = state.getBlock();
		int meta = com.hbm.world.WorldBlockState.metadata(state);

		if(!(canHarvestBlockWithStack(state, stack) ||
				canShearBlock(block, stack, world, pos)) ||
				(com.hbm.world.WorldBlockState.getBlockHardness(world, x, y, z) == -1.0F && block.getPlayerRelativeBlockHardness(state, player, world, pos) == 0.0F) ||
				block == ModBlocks.stone_keyhole) return;

		BlockPos refPos = new BlockPos(refX, refY, refZ);
		IBlockState refState = com.hbm.world.WorldBlockState.read(world, refX, refY, refZ);
		Block refBlock = refState.getBlock();
		float refStrength = ForgeHooks.blockStrength(refState, player, world, refPos);
		float strength = ForgeHooks.blockStrength(state, player, world, pos);

		if(
			!ForgeHooks.canHarvestBlock(block, player, world, pos) ||
			refStrength / strength > 10f ||
			refBlock.getPlayerRelativeBlockHardness(refState, player, world, refPos) < 0
		)
			return;

		int event = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos);
		if(event == -1)
			return;

		Configuration config = getConfiguration(stack);
		ToolPreset preset = config.getActivePreset();

		preset.harvestAbility.onHarvestBlock(preset.harvestAbilityLevel, world, x, y, z, player, block, meta);
	}

	/** Assumes a canShearBlock check has passed, will most likely crash otherwise! */
	public static void shearBlock(World world, int x, int y, int z, Block block, EntityPlayer player) {

		ItemStack held = com.hbm.util.NativeHeldItem.getMainHandNullable(player);

		IShearable target = (IShearable) block;
		BlockPos pos = new BlockPos(x, y, z);
		if(target.isShearable(held, player.world, pos)) {
			List<ItemStack> drops = target.onSheared(held, player.world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, held));
			Random rand = new Random();

			for(ItemStack stack : drops) {
				float f = 0.7F;
				double d = (double) (rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
				double d1 = (double) (rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
				double d2 = (double) (rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
				EntityItem entityitem = new EntityItem(player.world, (double) dropX + d, (double) dropY + d1, (double) dropZ + d2, stack);
				entityitem.setPickupDelay(10);
				player.world.spawnEntity(entityitem);
			}

			held.damageItem(1, player);
			player.addStat(StatList.getBlockStats(block), 1);
		}
	}

	// Since it's added by forge, access transformers don't affect it (even wildcards), so we do it the old-fashioned way
	private static Method blockCaptureDrops = ReflectionHelper.findMethod(Block.class, "captureDrops", "captureDrops", boolean.class);

	public static void standardDigPost(World world, int x, int y, int z, EntityPlayerMP player) {

		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = com.hbm.world.WorldBlockState.read(world, x, y, z);
		Block block = state.getBlock();
		int l = com.hbm.world.WorldBlockState.metadata(state);
		TileEntity tile = world.getTileEntity(pos);
		world.playEvent(player, 2001, pos, Block.getStateId(state));
		boolean removedByPlayer = false;

		if(player.capabilities.isCreativeMode) {
			removedByPlayer = removeBlock(world, x, y, z, false, player);
			player.connection.sendPacket(new SPacketBlockChange(world, new BlockPos(x, y, z)));
		} else {
			ItemStack itemstack = player.getHeldItemMainhand();
			boolean canHarvest = block.canHarvestBlock(world, pos, player);

			removedByPlayer = removeBlock(world, x, y, z, canHarvest, player);

			if(!itemstack.isEmpty()) {
				itemstack.onBlockDestroyed(world, state, pos, player);

				if(itemstack.isEmpty()) {
					player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
				}
			}

			if(removedByPlayer && canHarvest) {
				try {

					blockCaptureDrops.invoke(block, true);
					block.harvestBlock(world, player, pos, state, tile, itemstack);
					List<ItemStack> drops = (List)blockCaptureDrops.invoke(block, false);
					for (ItemStack stack : drops) {
						net.minecraft.block.Block.spawnAsEntity(world, new net.minecraft.util.math.BlockPos(dropX, dropY, dropZ), stack);
					}
				} catch (IllegalAccessException e) {
					// Shouldn't be possible with ReflectionHelper
					MainRegistry.logger.error("Failed to capture drops for block " + block, e);
				} catch (InvocationTargetException e) {
					// Might be possible? Not in practice, though
					MainRegistry.logger.error("Failed to capture drops for block " + block, e);
				}
			}
		}

		// Why was this commented out?
		// Drop experience
		// if (!player.capabilities.isCreativeMode && flag && event != null) {
		// 	block.dropXpOnBlockBreak(world, x, y, z, event.getExpToDrop());
		// }
	}

	public static boolean removeBlock(World world, int x, int y, int z, boolean canHarvest, EntityPlayerMP player) {
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = com.hbm.world.WorldBlockState.read(world, x, y, z);
		Block block = state.getBlock();
		block.onBlockHarvested(world, pos, state, player);
		boolean flag = block.removedByPlayer(state, world, pos, player, canHarvest);

		if(flag) {
			block.onPlayerDestroy(world, pos, state);
		}

		return flag;
	}

	public static class Configuration {
		public List<ToolPreset> presets;
		public int currentPreset;

		public Configuration() {
			this.presets = null;
			this.currentPreset = 0;
		}

		public Configuration(List<ToolPreset> presets, int currentPreset) {
			this.presets = presets;
			this.currentPreset = currentPreset;
		}

		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger("ability", currentPreset);

			NBTTagList nbtPresets = new NBTTagList();

			for(ToolPreset preset : presets) {
				NBTTagCompound nbtPreset = new NBTTagCompound();
				preset.writeToNBT(nbtPreset);
				nbtPresets.appendTag(nbtPreset);
			}

			nbt.setTag("abilityPresets", nbtPresets);
		}

		public void readFromNBT(NBTTagCompound nbt) {
			currentPreset = nbt.getInteger("ability");

			NBTTagList nbtPresets = nbt.getTagList("abilityPresets", 10);
			int numPresets = Math.min(nbtPresets.tagCount(), 99);

			presets = new ArrayList<ToolPreset>(numPresets);

			for(int i = 0; i < numPresets; i++) {
				NBTTagCompound nbtPreset = nbtPresets.getCompoundTagAt(i);
				ToolPreset preset = new ToolPreset();
				preset.readFromNBT(nbtPreset);
				presets.add(preset);
			}

			currentPreset = Math.max(0, Math.min(currentPreset, presets.size() - 1));
		}

		public void reset(AvailableAbilities availableAbilities) {
			currentPreset = 0;

			presets = new ArrayList<ToolPreset>(availableAbilities.size());
			presets.add(new ToolPreset());

			availableAbilities.getToolAreaAbilities().forEach((ability, level) -> {
				if (ability == IToolAreaAbility.NONE)
					return;
				presets.add(new ToolPreset(ability, level, IToolHarvestAbility.NONE, 0));
			});

			availableAbilities.getToolHarvestAbilities().forEach((ability, level) -> {
				if (ability == IToolHarvestAbility.NONE)
					return;
				presets.add(new ToolPreset(IToolAreaAbility.NONE, 0, ability, level));
			});

			presets.sort(
				Comparator
					.comparing((ToolPreset p) -> p.harvestAbility)
					.thenComparingInt(p -> p.harvestAbilityLevel)
					.thenComparing(p -> p.areaAbility)
					.thenComparingInt(p -> p.areaAbilityLevel)
			);
		}

		public void restrictTo(AvailableAbilities availableAbilities) {
			for (ToolPreset preset : presets) {
				preset.restrictTo(availableAbilities);
			}
		}

		public ToolPreset getActivePreset() {
			return presets.get(currentPreset);
		}
	}

	public Configuration getConfiguration(ItemStack stack) {
		Configuration config = new Configuration();

		if(stack == null || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("ability") || !stack.getTagCompound().hasKey("abilityPresets")) {
			config.reset(availableAbilities);
			return config;
		}

		config.readFromNBT(stack.getTagCompound());
		config.restrictTo(availableAbilities);
		return config;
	}

	public void setConfiguration(ItemStack stack, Configuration config) {
		if (stack == null) {
			return;
		}

		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}

		config.writeToNBT(stack.getTagCompound());
	}

	@Override
	public void receiveControl(ItemStack stack, NBTTagCompound data) {
		Configuration config = new Configuration();
		config.readFromNBT(data);
		config.restrictTo(availableAbilities);
		setConfiguration(stack, config);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenToolAbility(this.availableAbilities);
	}

	@Override
	public boolean canHandleKeybind(EntityPlayer player, ItemStack stack, EnumKeybind keybind) {
		if(player.world.isRemote) return keybind == EnumKeybind.ABILITY_ALT;
		return keybind == EnumKeybind.ABILITY_CYCLE;
	}

	@Override
	public void handleKeybind(EntityPlayer player, ItemStack stack, EnumKeybind keybind, boolean state) {

		if(keybind == EnumKeybind.ABILITY_CYCLE && state) {

			World world = player.world;
			if(!canOperate(stack)) return;

			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			RayTraceResult mop = EntityDamageUtil.getMouseOver(playerMP, playerMP.interactionManager.getBlockReachDistance());

			if(mop.typeOfHit == RayTraceResult.Type.BLOCK) {
				// this is horrifying and won't always produce the correct results, but it beats not having anything
				// simply put, we check if we are aiming at a block, and if we do, we compare that block's onBlockActivated
				// method to the one from Block.class. If the declaring class doesn't match, it's overridden, and we
				// assume that something is going to happen, which it might not, but either way we cancel the ability switch.
				Block b = world.getBlockState(mop.getBlockPos()).getBlock();
				// IDE
				try {
					Method m0 = ReflectionHelper.findMethod(b.getClass(), "onBlockActivated", "func_180639_a", World.class, BlockPos.class, IBlockState.class, EntityPlayer.class, EnumHand.class, EnumFacing.class, float.class, float.class, float.class);
					Method m1 = ReflectionHelper.findMethod(Block.class, "onBlockActivated", "func_180639_a", World.class, BlockPos.class, IBlockState.class, EntityPlayer.class, EnumHand.class, EnumFacing.class, float.class, float.class, float.class);
					if(m0.getDeclaringClass() != m1.getDeclaringClass()) return;
				} catch(Throwable e) { }
			}

			Configuration config = getConfiguration(stack);
			if(config.presets.size() < 2 || world.isRemote) return;

			if(player.isSneaking()) {
				config.currentPreset = 0;
			} else {
				config.currentPreset = (config.currentPreset + 1) % config.presets.size();
			}

			setConfiguration(stack, config);
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(config.getActivePreset().getMessage(), MainRegistry.proxy.ID_TOOLABILITY), playerMP);
			world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.25F, config.getActivePreset().isNone() ? 0.75F : 1.25F);
		}
	}

	@Override
	public void handleKeybindClient(EntityPlayer player, ItemStack stack, EnumKeybind keybind, boolean state) {
		if(state) player.openGui(MainRegistry.instance, 0, player.world, 0, 0, 0);
	}

	private static final Map<IBaseAbility, Pair<Integer, Integer>> abilityGui = new HashMap<>();

	static {
		abilityGui.put(IToolAreaAbility.RECURSION, new Pair<Integer,Integer>(0, 138));
		abilityGui.put(IToolAreaAbility.HAMMER, new Pair<Integer,Integer>(16, 138));
		abilityGui.put(IToolAreaAbility.HAMMER_FLAT, new Pair<Integer,Integer>(32, 138));
		abilityGui.put(IToolAreaAbility.EXPLOSION, new Pair<Integer,Integer>(48, 138));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUD(Pre event, ElementType type, EntityPlayer player, ItemStack stack) {
		if(type != ElementType.CROSSHAIRS) return;

		Configuration config = getConfiguration(stack);
		ToolPreset preset = config.getActivePreset();
		Pair<Integer, Integer> uv = abilityGui.get(preset.areaAbility);

		if(uv == null) return;

		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		int size = 16;
		int ox = ClientConfig.TOOL_HUD_INDICATOR_X.get();
		int oy = ClientConfig.TOOL_HUD_INDICATOR_Y.get();

		GL11.glPushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(GUIScreenToolAbility.texture);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
		gui.drawTexturedModalRect(event.getResolution().getScaledWidth() / 2 - size - 8 + ox, event.getResolution().getScaledHeight() / 2 + 8 + oy, uv.key, uv.value, size, size);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/gui/icons.png"));
	}

}
