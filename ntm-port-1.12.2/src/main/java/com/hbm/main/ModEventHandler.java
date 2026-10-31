package com.hbm.main;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.Vec3d;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.Level;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.blocks.IStepTickReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockAshes;
import com.hbm.blocks.machine.BlockBeamBase;
import com.hbm.config.GeneralConfig;
import com.hbm.config.MobConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.config.ServerConfig;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.CelestialTeleporter;
import com.hbm.dim.DimensionFishingLoot;
import com.hbm.dim.WorldGeneratorCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.WorldProviderEarth;
import com.hbm.dim.WorldTypeTeleport;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Invasion;
import com.hbm.dim.trait.CBT_Lights;
import com.hbm.dim.trait.CelestialBodyTrait;
import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.entity.missile.EntityRideableRocket.RocketState;
import com.hbm.entity.mob.EntityCreeperTainted;
import com.hbm.entity.mob.EntityCyberCrab;
import com.hbm.entity.mob.EntityUFO;
import com.hbm.entity.mob.siege.EntitySiegeCraft;
import com.hbm.entity.mob.siege.EntitySiegeUFO;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityBurningFOEQ;
import com.hbm.entity.train.EntityRailCarBase;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.BobmazonOfferFactory;
import com.hbm.handler.BossSpawnHandler;
import com.hbm.handler.EntityEffectHandler;
import com.hbm.handler.HTTPHandler;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.type.HazardTypeNeutron;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.IEquipReceiver;
import com.hbm.items.ModItems;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.IAttackHandler;
import com.hbm.items.armor.IDamageHandler;
import com.hbm.items.armor.ItemArmorMod;
import com.hbm.items.armor.ItemModDefuser;
import com.hbm.items.armor.ItemModRevive;
import com.hbm.items.armor.ItemModShackles;
import com.hbm.items.tool.ItemGuideBook.BookType;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.toclient.PermaSyncPacket;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.packet.toclient.SerializableRecipePacket;
import com.hbm.particle.helper.BlackPowderCreator;
import com.hbm.potion.HbmPotion;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.tileentity.network.RequestNetwork;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.*;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.world.PlanetGen;
import com.hbm.world.generator.TimedGenerator;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockFire;
import net.minecraft.block.IGrowable;
import net.minecraft.command.CommandGameRule;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import com.hbm.lib.ForgeDirection;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

@Spaghetti("fuck")
public class ModEventHandler {

	private static Random rand = new Random();

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

		if(!event.player.world.isRemote) {

			if(GeneralConfig.enableMOTD) {
				event.player.sendMessage(new TextComponentString("Loaded world with JamesH2 & Mellow's NTM: Space " + RefStrings.VERSION + " (1.12.2 port)!"));

				if(HTTPHandler.newVersion) {
					event.player.sendMessage(
							new TextComponentString("New version " + HTTPHandler.versionNumber + " is available! Click ")
							.setStyle(new Style().setColor(TextFormatting.YELLOW))
							.appendSibling(new TextComponentString("[here]")
									.setStyle(new Style()
										.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT/releases"))
										.setUnderlined(true)
										.setColor(TextFormatting.RED)
									)
								)
							.appendSibling(new TextComponentString(" to download!").setStyle(new Style().setColor(TextFormatting.YELLOW)))
							);
				}
			}

			if(MobConfig.enableDucks && event.player instanceof EntityPlayerMP && !event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("hasDucked"))
				PacketDispatcher.wrapper.sendTo(new PlayerInformPacket("Press O to Duck!", ServerProxy.ID_DUCK, 30_000), (EntityPlayerMP) event.player);


			if(GeneralConfig.enableGuideBook) {
				HbmPlayerProps props = HbmPlayerProps.getData(event.player);

				if(!props.hasReceivedBook) {
					event.player.inventory.addItemStackToInventory(new ItemStack(ModItems.book_guide, 1, BookType.STARTER.ordinal()));
					event.player.inventoryContainer.detectAndSendChanges();
					props.hasReceivedBook = true;
				}
			}


			if(event.player.world.getWorldInfo().getTerrainType() instanceof WorldTypeTeleport) {
				HbmPlayerProps props = HbmPlayerProps.getData(event.player);

				if(!props.hasWarped) {
					WorldTypeTeleport teleport = (WorldTypeTeleport) event.player.world.getWorldInfo().getTerrainType();
					teleport.onPlayerJoin(event.player);
					props.hasWarped = true;
				}
			}

			if(GeneralConfig.enableServerRecipeSync && FMLCommonHandler.instance().getSide() == Side.SERVER && event.player instanceof EntityPlayerMP) {
				File recDir = new File(MainRegistry.configDir.getAbsolutePath() + File.separatorChar + "hbmRecipes");

				MainRegistry.logger.info("Sending recipes to client!");

				boolean hasSent = false;

				for(SerializableRecipe recipe : SerializableRecipe.recipeHandlers) {
					File recFile = new File(recDir.getAbsolutePath() + File.separatorChar + recipe.getFileName());
					if(recFile.exists() && recFile.isFile()) {
						MainRegistry.logger.info("Sending recipe file: " + recFile.getName());
						PacketDispatcher.wrapper.sendTo(new SerializableRecipePacket(recFile), (EntityPlayerMP) event.player);
						hasSent = true;
					}
				}

				if(hasSent) {
					PacketDispatcher.wrapper.sendTo(new SerializableRecipePacket(true), (EntityPlayerMP) event.player);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClient(ClientDisconnectionFromServerEvent event) {
		SerializableRecipe.clearReceivedRecipes();
	}

	@SubscribeEvent
	public void onEntityMount(net.minecraftforge.event.entity.EntityMountEvent event) {
		if(event.isDismounting() && event.getEntityBeingMounted() instanceof EntityRideableRocket) {
			EntityRideableRocket rocket = (EntityRideableRocket) event.getEntityBeingMounted();
			if(!rocket.canExitCapsule() && (rocket.forceExitTimer < 60 || rocket.getState() == EntityRideableRocket.RocketState.TRANSFER)) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {

		EntityPlayer player = event.player;

		if((player.getUniqueID().toString().equals(ShadyUtil.Dr_Nostalgia) || player.getDisplayName().equals("Dr_Nostalgia")) && !player.world.isRemote) {

			if(!player.inventory.hasItemStack(new ItemStack(ModItems.hat)))
				player.inventory.addItemStackToInventory(new ItemStack(ModItems.hat));

			if(!player.inventory.hasItemStack(new ItemStack(ModItems.beta)))
				player.inventory.addItemStackToInventory(new ItemStack(ModItems.beta));
		}
	}

	@SubscribeEvent
	public void onEntityConstructing(EntityEvent.EntityConstructing event) {
		// Forge attaches capabilities after this event, not during it.
		if(event.getEntity() instanceof EntityPlayer && event.getEntity() == MainRegistry.proxy.me())
			BlockAshes.ashes = 0;
	}

	@SubscribeEvent
	public void onPlayerChangeDimension(PlayerChangedDimensionEvent event) {
		EntityPlayer player = event.player;
		HbmPlayerProps data = HbmPlayerProps.getData(player);
		data.setKeyPressed(EnumKeybind.JETPACK, false);
		data.setKeyPressed(EnumKeybind.DASH, false);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityDeathFirst(LivingDeathEvent event) {

		for(int i = 1; i < 5; i++) {

			ItemStack stack = com.hbm.util.LivingEquipmentRead.getEquipmentNullable(event.getEntityLiving(), i);

			if(stack != null && stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {

				ItemStack revive = ArmorModHandler.pryMods(stack)[ArmorModHandler.extra];

				if(revive != null) {

					//Classic revive
					if(revive.getItem() instanceof ItemModRevive) {
						revive.setItemDamage(revive.getItemDamage() + 1);

						if(revive.getItemDamage() >= revive.getMaxDamage()) {
							ArmorModHandler.removeMod(stack, ArmorModHandler.extra);
						} else {
							ArmorModHandler.applyMod(stack, revive);
						}

						event.getEntityLiving().setHealth(event.getEntityLiving().getMaxHealth());
						event.getEntityLiving().addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 60, 99));
						event.setCanceled(true);
						return;
					}

					//Shackles
					if(revive.getItem() instanceof ItemModShackles && HbmLivingProps.getRadiation(event.getEntityLiving()) < 1000F) {

						revive.setItemDamage(revive.getItemDamage() + 1);

						int dmg = revive.getItemDamage();
						ArmorModHandler.applyMod(stack, revive);

						event.getEntityLiving().setHealth(event.getEntityLiving().getMaxHealth());
						HbmLivingProps.incrementRadiation(event.getEntityLiving(), dmg * dmg);
						event.setCanceled(true);
						return;
					}
				}
			}
		}

	}

	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event) {

		HbmLivingProps.setRadiation(event.getEntityLiving(), 0);

		if(event.getEntity().world.isRemote)
			return;

		if(GeneralConfig.enableCataclysm) {
			EntityBurningFOEQ foeq = new EntityBurningFOEQ(event.getEntity().world);
			foeq.setPositionAndRotation(event.getEntity().posX, 500, event.getEntity().posZ, 0.0F, 0.0F);
			event.getEntity().world.spawnEntity(foeq);
		}

		if(event.getEntity().getUniqueID().toString().equals(ShadyUtil.HbMinecraft) || event.getEntity().getName().equals("HbMinecraft")) {
			event.getEntity().dropItem(ModItems.book_of_, 1);
		}

		if(event.getEntity().getUniqueID().toString().equals(ShadyUtil.MellowRPG8)) {
			event.getEntity().entityDropItem(new ItemStack(ModBlocks.block_meteor, 1 + rand.nextInt(10)), 0.0F);
		}

		if(event.getEntity() instanceof EntityCreeperTainted && event.getSource() == ModDamageSource.boxcar) {

			for(Object o : event.getEntity().world.getEntitiesWithinAABB(EntityPlayer.class, event.getEntity().getEntityBoundingBox().grow(50, 50, 50))) {
				EntityPlayer player = (EntityPlayer)o;
				com.hbm.advancement.NtmAdvancements.award(player, MainRegistry.bobHidden);
			}
		}
		CelestialBody body = CelestialBody.getBody(event.getEntity().world);

		CBT_Invasion alien = body.getTrait(CBT_Invasion.class);
		if(alien != null) {
			if(event.getEntity() instanceof EntitySiegeUFO) {

				alien.kills++;

				body.modifyTraits(alien);
			}else if (event.getEntity() instanceof EntitySiegeCraft) {
				alien.kills += 3;

				body.modifyTraits(alien);
			}

			if(alien.wave >= 4 && event.getEntity() instanceof EntityUFO) {
				HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits = body.getTraits(event.getEntity().world);

				currentTraits.remove(CBT_Invasion.class);

			        for (Object obj : event.getEntity().world.playerEntities) {
			            if (obj instanceof EntityPlayer) {
			                EntityPlayer player = (EntityPlayer) obj;
			                player.sendMessage(
			                    new TextComponentString("The Invasion Is Over!")
			                        .setStyle(new Style().setColor(TextFormatting.YELLOW))
			                );
			            }
			        }


				body.setTraits(event.getEntity().world, currentTraits);


			}
		}


		if(!event.getEntityLiving().world.isRemote) {

			if(event.getSource()==ModDamageSource.eve)
			{
				for(int i = -1; i < 2; i++) {
					for(int j = -1; j < 2; j++) {
						for(int k = -1; k < 2; k++) {
							if(com.hbm.world.WorldBlockState.read(event.getEntityLiving().world, (int)event.getEntityLiving().posX+i, (int)event.getEntityLiving().posY+j, (int)event.getEntityLiving().posZ+k).getBlock()==Blocks.AIR)
							{
								if(ModBlocks.flesh_block.canPlaceBlockAt(event.getEntityLiving().world, new net.minecraft.util.math.BlockPos((int)event.getEntityLiving().posX+i, (int)event.getEntityLiving().posY+j, (int)event.getEntityLiving().posZ+k)))
								{
									com.hbm.world.WorldBlockState.writeLegacyBlock(event.getEntityLiving().world, (int)event.getEntityLiving().posX+i, (int)event.getEntityLiving().posY+j, (int)event.getEntityLiving().posZ+k, ModBlocks.flesh_block);
								}
							}
						}
					}
				}
			}

			if(event.getSource() instanceof EntityDamageSource && ((EntityDamageSource)event.getSource()).getTrueSource() instanceof EntityPlayer
					 && !(((EntityDamageSource)event.getSource()).getTrueSource() instanceof FakePlayer)) {

				if(event.getEntityLiving() instanceof EntitySpider && event.getEntityLiving().getRNG().nextInt(500) == 0) {
					event.getEntityLiving().dropItem(ModItems.spider_milk, 1);
				}

				if(event.getEntityLiving() instanceof EntityCaveSpider && event.getEntityLiving().getRNG().nextInt(100) == 0) {
					event.getEntityLiving().dropItem(ModItems.serum, 1);
				}

				if(event.getEntityLiving() instanceof EntityAnimal && event.getEntityLiving().getRNG().nextInt(500) == 0) {
					event.getEntityLiving().dropItem(ModItems.bandaid, 1);
				}

				if(event.getEntityLiving() instanceof IMob) {
					if(event.getEntityLiving().getRNG().nextInt(1000) == 0) event.getEntityLiving().dropItem(ModItems.heart_piece, 1);
					if(event.getEntityLiving().getRNG().nextInt(250) == 0) event.getEntityLiving().dropItem(ModItems.key_red_cracked, 1);
					if(event.getEntityLiving().getRNG().nextInt(250) == 0) event.getEntityLiving().dropItem(ModItems.launch_code_piece, 1);
				}

				if(event.getEntityLiving() instanceof EntityCyberCrab && event.getEntityLiving().getRNG().nextInt(500) == 0) {
					event.getEntityLiving().dropItem(ModItems.wd40, 1);
				}

				if(event.getEntityLiving() instanceof EntityVillager&& event.getEntityLiving().getRNG().nextInt(1) == 0) {
					event.getEntityLiving().dropItem(ModItems.flesh, 5);
			}
		}
	}
}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityDeathLast(LivingDeathEvent event) {

		EntityLivingBase entity = event.getEntityLiving();

		if(EntityDamageUtil.wasAttackedByV1(event.getSource())) {

			NBTTagCompound vdat = new NBTTagCompound();
			vdat.setString("type", "giblets");
			vdat.setInteger("ent", entity.getEntityId());
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(vdat, entity.posX, entity.posY + entity.height * 0.5, entity.posZ), new TargetPoint(entity.dimension, entity.posX, entity.posY + entity.height * 0.5, entity.posZ, 150));

			com.hbm.sound.VanillaWorldSoundEffects.play(entity.world, entity.posX, entity.posY, entity.posZ, "mob.zombie.woodbreak", 2.0F, 0.95F + entity.world.rand.nextFloat() * 0.2F);

			EntityPlayer attacker = (EntityPlayer) ((EntityDamageSource)event.getSource()).getTrueSource();

			if(attacker.getDistanceSq(entity) < 100) {
				attacker.heal(entity.getMaxHealth() * 0.25F);
			}
		}

		if(event.getEntityLiving() instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) event.getEntityLiving();

			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {

				ItemStack stack = player.inventory.getStackInSlot(i);

				if(stack != null && stack.getItem() == ModItems.detonator_deadman) {

					if(stack.getTagCompound() != null) {

						int x = stack.getTagCompound().getInteger("x");
						int y = stack.getTagCompound().getInteger("y");
						int z = stack.getTagCompound().getInteger("z");

						if(!player.world.isRemote && com.hbm.world.WorldBlockState.read(player.world, x, y, z).getBlock() instanceof IBomb) {

							((IBomb) com.hbm.world.WorldBlockState.read(player.world, x, y, z).getBlock()).explode(player.world, x, y, z);

							if(GeneralConfig.enableExtendedLogging)
								MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by dead man's switch from " + player.getDisplayName() + "!");
						}

						player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void decorateMob(LivingSpawnEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		World world = event.getWorld();

		if(!MobConfig.enableMobGear || entity.isChild() || world.isRemote) return;

		Map<Integer, List<WeightedRandomObject>> slotPools = new HashMap<>();

		float soot = PollutionHandler.getPollution(entity.world, MathHelper.floor(event.getX()), MathHelper.floor(event.getY()), MathHelper.floor(event.getZ()), PollutionType.SOOT); //uhfgfg

		if(entity instanceof EntityZombie) {
			if(world.rand.nextFloat() < 0.005F && soot > 2) { // full hazmat zombine
				MobUtil.equipFullSet(entity, ModItems.hazmat_helmet, ModItems.hazmat_plate, ModItems.hazmat_legs, ModItems.hazmat_boots);
				return;
			}
			slotPools = MobUtil.slotPoolCommon;

		} else if(entity instanceof EntitySkeleton) {
			slotPools = MobUtil.slotPoolRanged;
			ItemStack bowReplacement = getSkelegun(soot, world.rand);
			slotPools.put(0, createSlotPool(50, bowReplacement != null ? new Object[][]{{bowReplacement, 1}} : new Object[][]{}));
		}

		MobUtil.assignItemsToEntity(entity, slotPools, rand);
	}

	private List<WeightedRandomObject> createSlotPool(int nullWeight, Object[][] items) {
		List<WeightedRandomObject> pool = new ArrayList<>();
		pool.add(new WeightedRandomObject(null, nullWeight));
		for (Object[] item : items) {
			Object obj = item[0];
			int weight = (int) item[1];

			if (obj instanceof Item) {
				pool.add(new WeightedRandomObject(new ItemStack((Item) obj), weight));
			} else if (obj instanceof ItemStack) {		//lol just make it pass ItemStack aswell
				pool.add(new WeightedRandomObject(obj, weight));
			}
		}
		return pool;
	}

	private static ItemStack getSkelegun(float soot, Random rand) {
		if (!MobConfig.enableMobWeapons) return null;
		if (rand.nextDouble() > Math.log(soot) * 0.25) return null;

		ArrayList<WeightedRandomObject> pool = new ArrayList<>();

		if(soot < 0.3){
			pool.add(new WeightedRandomObject(new ItemStack(ModItems.gun_pepperbox), 5));
			pool.add(new WeightedRandomObject(null, 20));
		} else if(soot > 0.3 && soot < 1) {
			pool.addAll(MobUtil.slotPoolGuns.get(0.3));
		} else if (soot < 3) {
			pool.addAll(MobUtil.slotPoolGuns.get(1D));
		} else if (soot < 5) {
			pool.addAll(MobUtil.slotPoolGuns.get(3D));
		} else {
			pool.addAll(MobUtil.slotPoolGuns.get(5D));
		}

		WeightedRandomObject selected = (WeightedRandomObject) WeightedRandom.getRandomItem(rand, pool);

		return selected.asStack();
	}

	@SubscribeEvent
	public void addAITasks(EntityJoinWorldEvent event) {
		if(event.getWorld().isRemote || !(event.getEntity() instanceof EntityLiving)) return;

		EntityLiving living = (EntityLiving) event.getEntity();
		ItemStack held = com.hbm.util.NativeHeldItem.getMainHandNullable(living);

		if(held != null && held.getItem() instanceof ItemGunBaseNT) {
			MobUtil.addFireTask(living);
		}
	}

	@SubscribeEvent
	public void onItemToss(ItemTossEvent event) {

		ItemStack yeet = event.getEntityItem().getItem();

		if(yeet.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(yeet)) {

			ItemStack[] mods = ArmorModHandler.pryMods(yeet);
			ItemStack cladding = mods[ArmorModHandler.cladding];

			if(cladding != null && cladding.getItem() == ModItems.cladding_obsidian) {
				ReflectionHelper.setPrivateValue(Entity.class, event.getEntityItem(), true, "field_149119_a", "field_83001_bt", "field_149500_a", "invulnerable");
			}
		}

		if(yeet.getItem() == ModItems.bismuth_tool) {
			ReflectionHelper.setPrivateValue(Entity.class, event.getEntityItem(), true, "field_149119_a", "field_83001_bt", "field_149500_a", "invulnerable");
		}
	}

	@SubscribeEvent
	public void onBlockPlaced(PlaceEvent event) {
		if(event.getWorld().isRemote) return;
		BlockPos pos = event.getPos();
		Block block = event.getPlacedBlock().getBlock();
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		boolean placeCancelled = ChunkAtmosphereManager.proxy.runEffectsOnBlock(event.getWorld(), block, x, y, z);
		if(placeCancelled) return;

		if(block instanceof IGrowable) {
			ChunkAtmosphereManager.proxy.trackPlant(event.getWorld(), x, y, z);
		}

		if(SpaceConfig.allowNetherPortals && event.getWorld().provider.getDimension() > 1 && block instanceof BlockFire) {
			Blocks.PORTAL.trySpawnPortal(event.getWorld(), pos);
		}

		// sneaky sneaky space furnace
		if(block == Blocks.FURNACE) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(event.getWorld(), x, y, z, ModBlocks.furnace, 0, 2);
			ModBlocks.furnace.onBlockPlacedBy(event.getWorld(), pos, event.getPlacedBlock(), event.getPlayer(), event.getItemInHand());
		}

		if(y >= event.getWorld().provider.getHorizon()) {
			if(block.getLightValue(event.getPlacedBlock(), event.getWorld(), pos) > 10) {
				CelestialBody body = CelestialBody.getBody(event.getWorld());
				CBT_Lights lights = body.getTrait(CBT_Lights.class);

				if(lights == null) lights = new CBT_Lights();
				lights.addLight(block, x, y, z);

				body.modifyTraits(lights);
			}
		}
	}

	@SubscribeEvent
	public void onBucketUse(FillBucketEvent event) {
		if(event.getWorld().isRemote) return;
		if(event.getTarget().typeOfHit != RayTraceResult.Type.BLOCK) return;
		if(!(event.getWorld().provider instanceof WorldProviderCelestial) && !(event.getWorld().provider instanceof WorldProviderOrbit)) return;

		if(event.getEmptyBucket() != null && event.getEmptyBucket().getItem() == Items.WATER_BUCKET) {
			net.minecraft.util.EnumFacing side = event.getTarget().sideHit;
			CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(event.getWorld(), event.getTarget().getBlockPos().getX() + side.getXOffset(), event.getTarget().getBlockPos().getY() + side.getYOffset(), event.getTarget().getBlockPos().getZ() + side.getZOffset());
			boolean hasLiquidPressure = ChunkAtmosphereManager.proxy.hasLiquidPressure(atmosphere);

			if(Loader.isModLoaded(Compat.MOD_COFH)) {
				if(!hasLiquidPressure) {
					event.setCanceled(true);
				}
			} else {
				if(hasLiquidPressure) {
					ReflectionHelper.setPrivateValue(WorldProvider.class, event.getWorld().provider, false, "field_76575_d", "doesWaterVaporize");
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingDrop(LivingDropsEvent event) {

		if(!event.getEntityLiving().world.isRemote) {
			boolean contaminated = HbmLivingProps.getContagion(event.getEntityLiving()) > 0;

			if(contaminated) {

				for(EntityItem item : event.getDrops()) {
					ItemStack stack = item.getItem();

					if(!stack.hasTagCompound()) {
						stack.setTagCompound(new NBTTagCompound());
					}

					stack.getTagCompound().setBoolean("ntmContagion", true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {

		if(event.getEntityLiving() instanceof EntityCreeper && event.getEntityLiving().getEntityData().getBoolean("hfr_defused")) {
			ItemModDefuser.defuse((EntityCreeper) event.getEntityLiving(), null, false);
		}

		if(!event.getEntity().world.isRemote && event.getEntityLiving().isPotionActive(HbmPotion.slippery)) {
			if (event.getEntityLiving().onGround) {
				double slipperiness = 0.6;
				double inertia = 0.1;
				boolean isMoving = event.getEntityLiving().moveForward != 0.0 || event.getEntityLiving().moveStrafing != 0.0;

				double angle = Math.atan2(event.getEntityLiving().motionZ, event.getEntityLiving().motionX);

				double targetXMotion = Math.cos(angle) * slipperiness;
				double targetZMotion = Math.sin(angle) * slipperiness;

				double diffX = targetXMotion - event.getEntityLiving().motionX;
				double diffZ = targetZMotion - event.getEntityLiving().motionZ;

				event.getEntityLiving().motionX += diffX * inertia; //god weeps
				event.getEntityLiving().motionZ += diffZ * inertia;

				if (!isMoving) {
					event.getEntityLiving().motionX *= (1.0 - 0.1);

					double totalVelocity = Math.sqrt(event.getEntityLiving().motionX * event.getEntityLiving().motionX + event.getEntityLiving().motionZ * event.getEntityLiving().motionZ);
					double smoothingAmount = totalVelocity * 0.02;
						event.getEntityLiving().motionX -= event.getEntityLiving().motionX / totalVelocity * smoothingAmount;
						event.getEntityLiving().motionZ -= event.getEntityLiving().motionZ / totalVelocity * smoothingAmount;
				}
			}
		}

		boolean isFlying = event.getEntity() instanceof EntityPlayer ? ((EntityPlayer) event.getEntity()).capabilities.isFlying : false;

		if(!isFlying) {
			if(event.getEntity() instanceof EntityFlying) {
				return;
			}
			float gravity = CelestialBody.getGravity(event.getEntityLiving());

			if(gravity == 0) {
				event.getEntityLiving().motionY /= 0.98F;
				event.getEntityLiving().motionY += (AstronomyUtil.STANDARD_GRAVITY / 20F);

				if(event.getEntityLiving() instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) event.getEntityLiving();
					if(player.isSneaking()) event.getEntityLiving().motionY -= 0.01F;
					if(ReflectionHelper.getPrivateValue(EntityLivingBase.class, player, "field_70703_bu", "isJumping")) event.getEntityLiving().motionY += 0.01F;
				} else if(event.getEntity() instanceof EntityChicken) {
					event.getEntityLiving().motionY = 0;
				}

				event.getEntityLiving().motionY *= 0.91F;
			} else if(!event.getEntityLiving().isInWater() && event.getEntityLiving().ticksExisted > 20 && (gravity < 1.5F || gravity > 1.7F)) {
				// If gravity is basically the same as normal, do nothing
				// Also do nothing in water, or if we've been alive less than a second (so we don't glitch into the ground)

				// Minimum gravity to prevent floating bug
				if(gravity < 0.2F) gravity = 0.2F;

				// Undo falling, and add our intended falling speed
				// On high gravity planets, only apply falling speed when descending, so we can still jump up single blocks
				if((gravity < 1.5F || event.getEntityLiving().motionY < 0) && !(event.getEntity() instanceof EntityChicken)) {
					event.getEntityLiving().motionY /= 0.98F;
					event.getEntityLiving().motionY += (AstronomyUtil.STANDARD_GRAVITY / 20F);
					event.getEntityLiving().motionY -= (gravity / 20F);
					event.getEntityLiving().motionY *= 0.98F;
				}
			}
		}

		NBTTagCompound entityData = event.getEntityLiving().getEntityData();
		boolean hasPreviousArmor = entityData.hasKey("hbmPreviousArmor", 9);
		ItemStack[] prevArmor = hasPreviousArmor ? new ItemStack[5] : null;
		if(hasPreviousArmor) {
			NBTTagList previousArmor = entityData.getTagList("hbmPreviousArmor", 10);
			for(int n = 0; n < previousArmor.tagCount(); n++) {
				NBTTagCompound previousStack = previousArmor.getCompoundTagAt(n);
				int slot = previousStack.getByte("Slot");
				if(slot >= 1 && slot < 5) prevArmor[slot] = new ItemStack(previousStack);
			}
		}

		if(event.getEntityLiving() instanceof EntityPlayerMP) {
			ItemStack previousMainHand = entityData.hasKey("hbmPreviousMainHand") ? new ItemStack(entityData.getCompoundTag("hbmPreviousMainHand")) : ItemStack.EMPTY;
			ItemStack currentMainHand = com.hbm.util.NativeHeldItem.getMainHandNullable(event.getEntityLiving());

			if((previousMainHand.isEmpty() || currentMainHand == null || previousMainHand.getItem() != currentMainHand.getItem())
					&& currentMainHand != null && currentMainHand.getItem() instanceof IEquipReceiver) {
				((IEquipReceiver)currentMainHand.getItem()).onEquip((EntityPlayer) event.getEntityLiving(), currentMainHand);
			}
			if(currentMainHand != null && !currentMainHand.isEmpty()) entityData.setTag("hbmPreviousMainHand", currentMainHand.writeToNBT(new NBTTagCompound()));
			else entityData.removeTag("hbmPreviousMainHand");
		}

		NBTTagList currentArmor = new NBTTagList();
		for(int i = 1; i < 5; i++) {
			ItemStack armor = com.hbm.util.LivingEquipmentRead.getEquipmentNullable(event.getEntityLiving(), i);
			if(armor != null && !armor.isEmpty()) {
				NBTTagCompound stackTag = armor.writeToNBT(new NBTTagCompound());
				stackTag.setByte("Slot", (byte) i);
				currentArmor.appendTag(stackTag);
			}
		}
		entityData.setTag("hbmPreviousArmor", currentArmor);

		for(int i = 1; i < 5; i++) {

			ItemStack prev = prevArmor != null ? prevArmor[i] : null;
			ItemStack armor = com.hbm.util.LivingEquipmentRead.getEquipmentNullable(event.getEntityLiving(), i);

			boolean prevEmpty = prev == null || prev.isEmpty();
			boolean armorEmpty = armor == null || armor.isEmpty();
			boolean sameArmor = (prevEmpty && armorEmpty) || (!prevEmpty && !armorEmpty && ItemStack.areItemStacksEqual(prev, armor));
			boolean reapply = prevArmor != null && !sameArmor;

			if(reapply) {

				if(!prevEmpty && ArmorModHandler.hasMods(prev)) {

					for(ItemStack mod : ArmorModHandler.pryMods(prev)) {

						if(mod != null && mod.getItem() instanceof ItemArmorMod) {

							Multimap map = ((ItemArmorMod)mod.getItem()).getModifiers(prev);

							if(map != null)
								event.getEntityLiving().getAttributeMap().removeAttributeModifiers(map);
						}
					}
				}
			}

			if(!armorEmpty && ArmorModHandler.hasMods(armor)) {

				for(ItemStack mod : ArmorModHandler.pryMods(armor)) {

					if(mod != null && mod.getItem() instanceof ItemArmorMod) {
						((ItemArmorMod)mod.getItem()).modUpdate(event.getEntityLiving(), armor);
						HazardSystem.applyHazards(mod, event.getEntityLiving());

						if(reapply) {

							Multimap map = ((ItemArmorMod)mod.getItem()).getModifiers(armor);

							if(map != null)
								event.getEntityLiving().getAttributeMap().applyAttributeModifiers(map);
						}
					}
				}
			}
		}

		EntityEffectHandler.onUpdate(event.getEntityLiving());

		if(!event.getEntity().world.isRemote && !(event.getEntityLiving() instanceof EntityPlayer)) {
			HazardSystem.updateLivingInventory(event.getEntityLiving());
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLoad(WorldEvent.Load event) {
		BobmazonOfferFactory.init();

		updateWaterOpacity(event.getWorld());

		if(!(event.getWorld().provider instanceof WorldProviderEarth)) {
			PlanetGen.overrideOverworldProvider();
		}
	}

	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event) {
		NeutronNodeWorld.streamWorlds.remove(event.getWorld());
	}

	public static boolean didSit = false;
	public static Field reference = null;

	@SubscribeEvent
	public void worldTick(WorldTickEvent event) {

		if(event.world != null && !event.world.isRemote) {

			if(reference != null) {
				for(Object player : event.world.playerEntities) {
					if(((EntityPlayer) player).getRidingEntity() != null && event.world.getTotalWorldTime() % (1 * 60 * 20) == 0) {
						((EntityPlayer) player).dismountRidingEntity();
						didSit = true;
					}
				}
				/*if(didSit && event.world.getTotalWorldTime() % (1 * 20 * 20) == 0) {
					try { reference.setFloat(null, (float) (rand.nextGaussian() * 0.1 + Math.PI)); } catch(Throwable e) { }
				}*/
			}

			List loadedEntityList = new ArrayList();
			loadedEntityList.addAll(event.world.loadedEntityList); // ConcurrentModificationException my balls

			int tickrate = Math.max(1, ServerConfig.ITEM_HAZARD_DROP_TICKRATE.get());

			for(Object e : loadedEntityList) {

				if(e instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) e;

					// handle dismount events, or our players will splat upon leaving tall rockets
					if(player.getRidingEntity() != null && player.getRidingEntity() instanceof EntityRideableRocket && player.isSneaking()) {
						EntityRideableRocket rocket = (EntityRideableRocket) player.getRidingEntity();

						if(player.isSneaking()) {
							// Prevent leaving a rocket in motion, for safety
							if(rocket.canExitCapsule() || (rocket.forceExitTimer >= 60 && rocket.getState() != EntityRideableRocket.RocketState.TRANSFER)) {
								boolean inOrbit = event.world.provider instanceof WorldProviderOrbit;
								Entity ridingEntity = player.getRidingEntity();
								float prevHeight = ridingEntity.height;

								ridingEntity.height = inOrbit ? ridingEntity.height + 1.0F : 1.0F;
								player.dismountRidingEntity();
								if(!inOrbit) player.setPositionAndUpdate(player.posX + 2, player.posY, player.posZ);
								ridingEntity.height = prevHeight;
							} else {
								rocket.forceExitTimer++;
							}

							player.setSneaking(false);
						} else {
							rocket.forceExitTimer = 0;
						}
					}
				}

				if(event.phase == Phase.END && event.world.getTotalWorldTime() % tickrate == 0) {
					if(e instanceof EntityItem) {
						EntityItem item = (EntityItem) e;
						HazardSystem.updateDroppedItem(item);
					}
				}
			}

			if(event.phase == Phase.END) {
				EntityRailCarBase.updateMotion(event.world);

				CelestialTeleporter.runQueuedTeleport();

				// Once per second, run atmospheric chemistry
				if(event.world.getTotalWorldTime() % 20 == 0) {
					CelestialBody.updateChemistry(event.world);
				}
			}

			// Tick our per celestial body timer
			if(event.phase == Phase.START && event.world.provider instanceof WorldProviderCelestial && event.world.provider.getDimension() != 0) {
				if(event.world.getGameRules().getBoolean("doDaylightCycle")) {
					event.world.provider.setWorldTime(event.world.provider.getWorldTime() + 1L);
				}
			}

		}

		if(event.phase == Phase.START) {
			BossSpawnHandler.rollTheDice(event.world);
			TimedGenerator.automaton(event.world, 100);

			updateWaterOpacity(event.world);
		}

		if(event.phase == Phase.START && event.world.provider.getDimension() == SpaceConfig.orbitDimension) {
			for(Object o : event.world.loadedEntityList) {
				if(o instanceof EntityItem) {
					EntityItem item = (EntityItem) o;
					item.motionX *= 0.9D;
					item.motionY = 0.03999999910593033D; // when entity gravity is applied, this becomes exactly 0
					item.motionZ *= 0.9D;
				}
			}
		}
		//if(event.phase == Phase.END) {
			//CelestialBody body = CelestialBody.getBody(event.world);
			//CBT_Invasion alien = body.getTrait(CBT_Invasion.class);

			//if(alien == null) return;
			//alien.SpawnAttempt(event.world);

			//body.modifyTraits(alien);
		//}

	}

	private void updateWaterOpacity(World world) {
		// Per world water opacity!
		int waterOpacity = 3;
		if(world.provider instanceof WorldProviderCelestial) {
			waterOpacity = ((WorldProviderCelestial) world.provider).getWaterOpacity();
		}

		Blocks.WATER.setLightOpacity(waterOpacity);
		Blocks.FLOWING_WATER.setLightOpacity(waterOpacity);
	}

	@SubscribeEvent
	public void onGenerateOre(GenerateMinable event) {
		if(event.getWorld().provider instanceof WorldProviderCelestial && event.getWorld().provider.getDimension() != 0) {
			WorldGeneratorCelestial.onGenerateOre(event);
		}
	}

	@SubscribeEvent
	public void onEntityAttacked(LivingAttackEvent event) {

		EntityLivingBase e = event.getEntityLiving();

		if(e instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) e;

			if(ArmorUtil.checkArmor(player, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
				HbmPlayerProps.plink(player, "random.break", 0.5F, 1.0F + e.getRNG().nextFloat() * 0.5F);
				event.setCanceled(true);
			}

			if(com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2) != null && com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2).getItem() instanceof ArmorFSB)
				((ArmorFSB)com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2).getItem()).handleAttack(event);

			for(ItemStack stack : player.inventory.armorInventory) {
				if (stack == null || stack.isEmpty()) continue;
				if(stack.getItem() instanceof IAttackHandler) {
					((IAttackHandler)stack.getItem()).handleAttack(event, stack);
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamaged(LivingHurtEvent event) {

		EntityLivingBase e = event.getEntityLiving();

		if(e instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) e;

			HbmPlayerProps props = HbmPlayerProps.getData(player);
			if(props.shield > 0 |props.nitanHealth > 0) {
				float reduce = Math.min(props.shield+props.nitanHealth, event.getAmount());
				props.shield -= reduce;
				props.nitanHealth -= reduce;
				event.setAmount((float) (event.getAmount() - (reduce)));
			}
			props.lastDamage = player.ticksExisted;
		}

		if(HbmLivingProps.getContagion(e) > 0 && event.getAmount() < 100)
			event.setAmount((float) (event.getAmount() * (2F)));

		/// V1 ///
		if(EntityDamageUtil.wasAttackedByV1(event.getSource())) {
			EntityPlayer attacker = (EntityPlayer) ((EntityDamageSource)event.getSource()).getTrueSource();

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "vanillaburst");
			data.setInteger("count", (int)Math.min(e.getMaxHealth() / 2F, 250));
			data.setDouble("motion", 0.1D);
			data.setString("mode", "blockdust");
			data.setInteger("block", Block.getIdFromBlock(Blocks.REDSTONE_BLOCK));
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, e.posX, e.posY + e.height * 0.5, e.posZ), new TargetPoint(e.dimension, e.posX, e.posY, e.posZ, 50));

			if(attacker.getDistanceSq(e) < 25) {
				attacker.heal(event.getAmount() * 0.5F);
			}
		}

		/// ARMOR MODS ///
		for(int i = 1; i < 5; i++) {

			ItemStack armor = com.hbm.util.LivingEquipmentRead.getEquipmentNullable(e, i);

			if(armor != null && ArmorModHandler.hasMods(armor)) {

				for(ItemStack mod : ArmorModHandler.pryMods(armor)) {

					if(mod != null && mod.getItem() instanceof ItemArmorMod) {
						((ItemArmorMod)mod.getItem()).modDamage(event, armor);
					}
				}
			}
		}

		if(e instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) e;

			/// FSB ARMOR ///
			if(com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2) != null && com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2).getItem() instanceof ArmorFSB)
				((ArmorFSB)com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2).getItem()).handleHurt(event);


			for(ItemStack stack : player.inventory.armorInventory) {
				if (stack == null || stack.isEmpty()) continue;
				if(stack.getItem() instanceof IDamageHandler) {
					((IDamageHandler)stack.getItem()).handleDamage(event, stack);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerFall(PlayerFlyableFallEvent event) {

		EntityPlayer e = event.getEntityPlayer();

		if(com.hbm.util.PlayerInventoryRead.get(e.inventory.armorInventory, 2) != null && com.hbm.util.PlayerInventoryRead.get(e.inventory.armorInventory, 2).getItem() instanceof ArmorFSB)
			((ArmorFSB)com.hbm.util.PlayerInventoryRead.get(e.inventory.armorInventory, 2).getItem()).handleFall(e, event.getDistance());
	}

	// only for the ballistic gauntlet! contains dangerous conditional returns!
	@SubscribeEvent
	public void onPlayerPunch(AttackEntityEvent event) {

		EntityPlayer player = event.getEntityPlayer();
		ItemStack chestplate = com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2);

		if(!player.world.isRemote && chestplate != null && ArmorModHandler.hasMods(chestplate)) {

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND).containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) return;

			ItemStack[] mods = ArmorModHandler.pryMods(chestplate);
			ItemStack servo = mods[ArmorModHandler.servos];

			if(servo != null && servo.getItem() == ModItems.ballistic_gauntlet) {

				BulletConfig firedConfig = null;
				BulletConfig[] gauntletConfigs = new BulletConfig[] {XFactory12ga.g12_bp, XFactory12ga.g12_bp_magnum, XFactory12ga.g12_bp_slug, XFactory12ga.g12, XFactory12ga.g12_slug, XFactory12ga.g12_flechette, XFactory12ga.g12_magnum, XFactory12ga.g12_explosive, XFactory12ga.g12_phosphorus};

				for(BulletConfig config : gauntletConfigs) {

					if(InventoryUtil.doesPlayerHaveAStack(player, config.ammo, true, true)) {
						firedConfig = config;
						break;
					}
				}

				if(firedConfig != null) {
					int bullets = firedConfig.projectilesMin;

					if(firedConfig.projectilesMax > firedConfig.projectilesMin) {
						bullets += player.getRNG().nextInt(firedConfig.projectilesMax - firedConfig.projectilesMin);
					}

					for(int i = 0; i < bullets; i++) {
						EntityBulletBaseMK4 mk4 = new EntityBulletBaseMK4(player, firedConfig, 15F, 0F, -0.1875, -0.0625, 0.5);
						player.world.spawnEntity(mk4);
						if(i == 0 && firedConfig.blackPowder) BlackPowderCreator.composeEffect(player.world, mk4.posX, mk4.posY, mk4.posZ, mk4.motionX, mk4.motionY, mk4.motionZ, 10, 0.25F, 0.5F, 10, 0.25F);
					}

					player.playSound(ModSoundEvents.resolve("hbm:weapon.shotgunShoot"), 1.0F, 1.0F);
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityJump(LivingJumpEvent event) {

		EntityLivingBase e = event.getEntityLiving();

		if(e instanceof EntityPlayer && com.hbm.util.PlayerInventoryRead.get(((EntityPlayer)e).inventory.armorInventory, 2) != null && com.hbm.util.PlayerInventoryRead.get(((EntityPlayer)e).inventory.armorInventory, 2).getItem() instanceof ArmorFSB)
			((ArmorFSB)com.hbm.util.PlayerInventoryRead.get(((EntityPlayer)e).inventory.armorInventory, 2).getItem()).handleJump((EntityPlayer)e);
	}

	@SubscribeEvent
	public void onEntityFall(LivingFallEvent event) {

		EntityLivingBase e = event.getEntityLiving();

		float gravity = CelestialBody.getGravity(e);


		// Reduce fall damage on low gravity bodies
		if(gravity < 0.3F) {
			event.setDistance(0);
		} else if(gravity < 1.5F) {
			event.setDistance((float) (event.getDistance() * (gravity / AstronomyUtil.STANDARD_GRAVITY)));
		}

		if(e instanceof EntityPlayer && com.hbm.util.PlayerInventoryRead.get(((EntityPlayer)e).inventory.armorInventory, 2) != null && com.hbm.util.PlayerInventoryRead.get(((EntityPlayer)e).inventory.armorInventory, 2).getItem() instanceof ArmorFSB)
			((ArmorFSB)com.hbm.util.PlayerInventoryRead.get(((EntityPlayer)e).inventory.armorInventory, 2).getItem()).handleFall((EntityPlayer)e, event.getDistance());
	}

	//this exists!?
	@SubscribeEvent
	public void onUseHoe(UseHoeEvent event) {
		World world = event.getWorld();
		int x = event.getPos().getX();
		int y = event.getPos().getY();
		int z = event.getPos().getZ();

		Block block = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(block == ModBlocks.rubber_grass || block == ModBlocks.rubber_silt) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.rubber_farmland);
			event.getCurrent().damageItem(1, event.getEntityPlayer());
			event.setResult(Result.ALLOW);
		}
	}

	private static final UUID fopSpeed = UUID.fromString("e5a8c95d-c7a0-4ecf-8126-76fb8c949389");

	@SubscribeEvent
	public void onWingFlop(TickEvent.PlayerTickEvent event) {

		EntityPlayer player = event.player;

		if(event.phase == TickEvent.Phase.START) {

			if(com.hbm.util.LivingEquipmentRead.getArmorNullable(player, 2) == null && !player.onGround) {

				if(player.getUniqueID().toString().equals(ShadyUtil.Barnaby99_x) || player.getDisplayName().equals("pheo7")) {

					ArmorUtil.resetFlightTime(player);
					HbmPlayerProps props = HbmPlayerProps.getData(player);

					if(props.isJetpackActive()) {

						if(player.motionY < 0.4D)
							player.motionY += 0.1D;

						Vec3d look = player.getLookVec();

						if(new Vec3d(player.motionX, player.motionY, player.motionZ).length() < 2) {
							player.motionX += look.x * 0.2;
							player.motionY += look.y * 0.2;
							player.motionZ += look.z * 0.2;

							if(look.y > 0)
								player.fallDistance = 0;
						}
					} else if(props.enableBackpack && !player.isSneaking()) {
						if(player.motionY < -0.2) player.motionY += 0.075D;
						if(player.fallDistance > 0) player.fallDistance = 0;
					}
				}

				boolean isBob = player.getUniqueID().toString().equals(ShadyUtil.HbMinecraft) || player.getDisplayName().equals("HbMinecraft");
				boolean isOther = player.getUniqueID().toString().equals(ShadyUtil.the_NCR) || player.getDisplayName().equals("the_NCR");

				if(isBob || isOther) {

					ArmorUtil.resetFlightTime(player);

					if(player.fallDistance > 0)
						player.fallDistance = 0;

					if(player.motionY < -0.4D)
						player.motionY = -0.4D;

					HbmPlayerProps props = HbmPlayerProps.getData(player);

					if(isBob || player.getFoodStats().getFoodLevel() > 6) {

						if(props.isJetpackActive()) {

							double cap = (isBob ? 0.8D : 0.4D);

							if(player.motionY < cap)
								player.motionY += 0.15D;
							else
								player.motionY = cap + 0.15D;

							if(isOther) {
								if(player.getFoodStats().getSaturationLevel() > 0F)
									player.addExhaustion(4F); //burn up saturation so that super-saturating foods have no effect
								else
									player.addExhaustion(0.2F); //4:1 -> 0.05 hunger per tick or 1 per second
							}

						} else if(props.enableBackpack && !player.isSneaking()) {

							if(player.motionY < -1)
								player.motionY += 0.4D;
							else if(player.motionY < -0.1)
								player.motionY += 0.2D;
							else if(player.motionY < 0)
								player.motionY = 0;

							if(isOther && !player.onGround) {
								if(player.getFoodStats().getSaturationLevel() > 0F)
									player.addExhaustion(4F);
								else
									player.addExhaustion(0.04F);
							}

						} else if(!props.enableBackpack && player.isSneaking()) {

							if(player.motionY < -0.08) {

								double mo = player.motionY * (isBob ? -0.6 : -0.4);
								player.motionY += mo;

				Vec3d vec = player.getLookVec();
				vec = vec.scale(mo);

								player.motionX += vec.x;
								player.motionY += vec.y;
								player.motionZ += vec.z;
							}
						}
					}

					Vec3d orig = player.getLookVec();
					Vec3d look = new Vec3d(orig.x, 0, orig.z).normalize();
					double mod = props.enableBackpack ? (isBob ? 0.5D : 0.25D) : 0.125D;

					if(player.moveForward != 0) {
						player.motionX += look.x * 0.35 * player.moveForward * mod;
						player.motionZ += look.z * 0.35 * player.moveForward * mod;
					}

					if(player.moveStrafing != 0) {
						look = look.rotateYaw((float) Math.PI * 0.5F);
						player.motionX += look.x * 0.15 * player.moveStrafing * mod;
						player.motionZ += look.z * 0.15 * player.moveStrafing * mod;
					}
				}
			}

			if(player.getUniqueID().toString().equals(ShadyUtil.LePeeperSauvage) ||	player.getDisplayName().equals("LePeeperSauvage")) {

				Multimap multimap = HashMultimap.create();
				multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(fopSpeed, "FOP SPEED", 0.5, 1));
				player.getAttributeMap().removeAttributeModifiers(multimap);

				if(player.isSprinting()) {
					player.getAttributeMap().applyAttributeModifiers(multimap);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {

		EntityPlayer player = event.player;
		if(player.posY > 300 && player.posY <1000) {
			Vec3d vec = new Vec3d(3 * rand.nextDouble(), 0, 0);
			CBT_Atmosphere thatmosphere = CelestialBody.getTrait(player.world, CBT_Atmosphere.class);

			if(thatmosphere != null && thatmosphere.getPressure() > 0.05 && !player.isRiding()) {
				if(Math.abs(player.motionX) > 1 || Math.abs(player.motionY) > 1 || Math.abs(player.motionZ) > 1) {
					ParticleUtil.spawnGasFlame(player.world, player.posX - 1 + vec.x, player.posY + vec.y, player.posZ + vec.z, 0, 0, 0);
				}
			}
		}

		if(player.isPotionActive(HbmPotion.slippery) && !player.capabilities.isFlying) {
			if (player.onGround) {
				double slipperiness = 0.6;
				double inertia = 0.1;
				boolean isMoving = player.moveForward != 0.0 || player.moveStrafing != 0.0;
				// double playerMotion = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);

				double angle = Math.atan2(player.motionZ, player.motionX);

				double targetXMotion = Math.cos(angle) * slipperiness;
				double targetZMotion = Math.sin(angle) * slipperiness;

				double diffX = targetXMotion - player.motionX;
				double diffZ = targetZMotion - player.motionZ;

				player.motionX += diffX * inertia; //god weeps
				player.motionZ += diffZ * inertia;

				if (!isMoving) {
					player.motionX *= (1.0 - 0.1);

					double totalVelocity = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
					double smoothingAmount = totalVelocity * 0.02;
					player.motionX -= player.motionX / totalVelocity * smoothingAmount;
					player.motionZ -= player.motionZ / totalVelocity * smoothingAmount;
				}
			}
		}

		if(com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2) != null && com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2).getItem() instanceof ArmorFSB)
			((ArmorFSB)com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2).getItem()).handleTick(event);

		if(player.ticksExisted == 100 || player.ticksExisted == 200)
			CraftingManager.crumple();

		if(event.phase == TickEvent.Phase.START) {
			int x = MathHelper.floor(player.posX);
			int y = MathHelper.floor(player.posY - player.getYOffset() - 0.01);
			int z = MathHelper.floor(player.posZ);
			Block b = com.hbm.world.WorldBlockState.read(player.world, x, y, z).getBlock();

			if(b instanceof IStepTickReceiver && !player.capabilities.isFlying) {
				IStepTickReceiver step = (IStepTickReceiver) b;
				step.onPlayerStep(player.world, x, y, z, player);
			}
		}

		if(!player.world.isRemote && event.phase == TickEvent.Phase.START) {
			// Check for players attempting to cross over to another orbital grid
			if(player.world.provider instanceof WorldProviderOrbit && !(player.getRidingEntity() instanceof EntityRideableRocket)) {
				double rx = Math.abs(player.posX) % OrbitalStation.STATION_SIZE;
				double rz = Math.abs(player.posZ) % OrbitalStation.STATION_SIZE;

				int minBuffer = OrbitalStation.BUFFER_SIZE;
				int maxBuffer = OrbitalStation.STATION_SIZE - minBuffer;

				int minWarning = OrbitalStation.BUFFER_SIZE + OrbitalStation.WARNING_SIZE;
				int maxWarning = OrbitalStation.STATION_SIZE - minWarning;

				if(player instanceof EntityPlayerMP && (rx < minWarning || rx > maxWarning || rz < minWarning || rz > maxWarning)) {
					PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("").nextTranslation("info.orbitfall").color(TextFormatting.RED).flush(), ServerProxy.ID_GAS_HAZARD, 3000), (EntityPlayerMP) player);
				}

				if(rx < minBuffer || rx > maxBuffer || rz < minBuffer || rz > maxBuffer) {
					OrbitalStation station = OrbitalStation.getStationFromPosition((int)player.posX, (int)player.posZ);
					CelestialTeleporter.teleport(player, station.orbiting.dimensionId, rand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance, 800, rand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance, false);
				}
			}

			// keep Nether teleports localized
			// this effectively turns the Nether into a shared pocket dimension, but disallows using it to travel between celestial bodies
			boolean inPortal = ReflectionHelper.getPrivateValue(Entity.class, player, "field_71087_bX", "inPortal");
			if(inPortal) {
				MinecraftServer minecraftserver = ((WorldServer)player.world).getMinecraftServer();
				int maxTime = player.getMaxInPortalTime();
				int portalCounter = ReflectionHelper.getPrivateValue(Entity.class, player, "field_82153_h", "portalCounter");
				if(minecraftserver.getAllowNether() && player.getRidingEntity() == null && portalCounter + 1 >= maxTime) {
					ReflectionHelper.setPrivateValue(Entity.class, player, maxTime, "field_82153_h", "portalCounter");
					player.timeUntilPortal = player.getPortalCooldown();

					HbmPlayerProps props = HbmPlayerProps.getData(player);
					int targetDimension = -1;
					if(player.world.provider.getDimension() == -1) {
						targetDimension = props.lastDimension;
					} else {
						props.lastDimension = player.world.provider.getDimension();
					}

					if(player instanceof EntityPlayerMP) ((EntityPlayerMP)player).changeDimension(targetDimension);
					ReflectionHelper.setPrivateValue(Entity.class, player, false, "field_71087_bX", "inPortal");
				}
			}

			/// GHOST FIX START ///

			if(!Float.isFinite(player.getHealth()) || !Float.isFinite(player.getAbsorptionAmount())) {
				player.sendMessage(new TextComponentString("Your health has been restored!"));
				player.playSound(ModSoundEvents.resolve("hbm:item.syringe"), 1.0F, 1.0F);
				player.setHealth(player.getMaxHealth());
				player.setAbsorptionAmount(0);
			}

			/// GHOST FIX END ///

			/// BETA HEALTH START ///
			if(player.inventory.hasItemStack(new ItemStack(ModItems.beta))) {

				if(player.getFoodStats().getFoodLevel() > 10) {
					player.heal(player.getFoodStats().getFoodLevel() - 10);
				}

				if(player.getFoodStats().getFoodLevel() != 10) {

					// Why can't you be normal??
					try {
						Field food = ReflectionHelper.findField(FoodStats.class, "field_75127_a", "foodLevel");
						food.setInt(player.getFoodStats(), 10);
					} catch(Exception e) { }
				}
			}
			/// BETA HEALTH END ///

			/// PU RADIATION START ///

			if(player.getUniqueID().toString().equals(ShadyUtil.Pu_238)) {

				List<EntityLivingBase> entities = player.world.getEntitiesWithinAABB(EntityLivingBase.class, player.getEntityBoundingBox().grow(3, 3, 3));

				for(EntityLivingBase e : entities) {

					if(e != player) {
						e.addPotionEffect(new PotionEffect(HbmPotion.radiation, 300, 2));
					}
				}

			}

			/// PU RADIATION END ///

			for(int i = 0; i < player.inventory.mainInventory.size(); i++) {
				ItemStack stack2 = player.inventory.getStackInSlot(i);

				//oh yeah remind me...
				if(stack2 != null) {
					if(stack2.hasTagCompound() && HazardSystem.getHazardLevelFromStack(stack2, HazardRegistry.RADIATION) == 0) {
						float activation = stack2.getTagCompound().getFloat(HazardTypeNeutron.NEUTRON_KEY);
						ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.CREATIVE, activation / 20);
					}
				}
			}
			/// NEW ITEM SYS START ///
			HazardSystem.updatePlayerInventory(player);
			/// NEW ITEM SYS END ///

			/// SYNC START ///
			if(!player.world.isRemote && player instanceof EntityPlayerMP) PacketDispatcher.wrapper.sendTo(new PermaSyncPacket((EntityPlayerMP) player), (EntityPlayerMP) player);
			/// SYNC END ///
		}

		if(player.world.isRemote && event.phase == event.phase.START && !player.isInvisible() && !player.isSneaking()) {

			if(player.getUniqueID().toString().equals(ShadyUtil.Pu_238)) {

				com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(3 * rand.nextDouble(), 0, 0);
				vec.rotateAroundZRad((float) (rand.nextDouble() * Math.PI));
				vec.rotateAroundYRad((float) (rand.nextDouble() * Math.PI * 2));
				player.world.spawnParticle(net.minecraft.util.EnumParticleTypes.TOWN_AURA, player.posX + vec.x, player.posY + 1 + vec.y, player.posZ + vec.z, 0.0, 0.0, 0.0);
			}
			if(player.getUniqueID().toString().equals(ShadyUtil.DUODEC_)) {

				com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(3 * rand.nextDouble(), 0, 0);

				vec.rotateAroundZRad((float) (rand.nextDouble() * Math.PI));
				vec.rotateAroundYRad((float) (rand.nextDouble() * Math.PI * 2));

				//player.worldObj.spawnParticle("magicCrit", player.posX + vec.x, player.posY + 1 + vec.y, player.posZ + vec.z, 0.0, 0.0, 0.0);
				ParticleUtil.spawnTuneFlame(player.world, player.posX + vec.x, player.posY + 1 + vec.y, player.posZ + vec.z);
				ParticleUtil.spawnJesusFlame(player.world, player.posX + vec.x, player.posY + 1 + vec.y, player.posZ + vec.z);

			}

		}

		/*if(!player.worldObj.isRemote && event.phase == TickEvent.Phase.END && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemGunBaseNT && player instanceof EntityPlayerMP) {
			HeldItemNBTPacket packet = new HeldItemNBTPacket(player.getHeldItem());
			PacketDispatcher.wrapper.sendTo(packet, (EntityPlayerMP) player);
		}*/
	}

	@SubscribeEvent
	public void preventOrganicSpawn(DecorateBiomeEvent.Decorate event) {
		// In space, no one can hear you shroom
		if(!(event.getWorld().provider instanceof WorldProviderCelestial)) return;

		WorldProviderCelestial celestial = (WorldProviderCelestial) event.getWorld().provider;
		if(celestial.hasLife()) return; // Except on Laythe

		switch(event.getType()) {
		case BIG_SHROOM:
		case CACTUS:
		case DEAD_BUSH:
		case LILYPAD:
		case FLOWERS:
		case GRASS:
		case PUMPKIN:
		case REED:
		case SHROOM:
		case TREE:
			event.setResult(Result.DENY);
		default:
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {

		if(event.phase == Phase.START) {
				for(CelestialBody body : CelestialBody.getAllBodies()) {
					List<CelestialBodyTrait> traits = new ArrayList<>(body.getTraits().values());
					for (CelestialBodyTrait trait : traits) {
						trait.update(false, body);

					}
				}

			// do other shit I guess?
			RTTYSystem.updateBroadcastQueue();
			// Logistics drone network
			RequestNetwork.updateEntries();
			// Radar entry handling
			TileEntityMachineRadarNT.updateSystem();
			// Networks! All of them!
			UniNodespace.updateNodespace();
			// Dyson Swarms
			CelestialBody.updateSwarms();
		}


		// There is an issue here somewhere...
		// I cannot, for the life of me, figure out why a single certain bug happens.
		// Every 20-30 or so ticks, players will receive wrong/outdated/weird information in packets
		// I have tried everything to see if I can get this to stop, but it just doesn't seem to work.

		// ^ Update ^ - I figured it out, when the packets were being made for some machines they were being created inside the thread,
		// meaning sometimes the machine would change data *after* the packet was supposed to be sent, meaning incorrect data was being sent.
		// This has since been fixed.

		if(event.phase == Phase.END) {
			// As ByteBufs are added to the queue in `com.hbm.packet.toclient.PacketThreading`, they are processed by the packet thread.
			// This waits until the thread is finished, which most of the time will be instantly since it has plenty of time to process in parallel to everything else.
			PacketThreading.waitUntilThreadFinished();

			NetworkHandler.flush(); // Flush ALL network packets.
		}
	}

	@SubscribeEvent
	public void commandEvent(CommandEvent event) {
		ICommand command = event.getCommand();
		ICommandSender sender = event.getSender();
		if(command instanceof CommandGameRule) {
			MinecraftServer server = sender.getServer();
			if(command.checkPermission(server, sender)) {
				try {
					command.execute(server, sender, event.getParameters());
					RBMKDials.refresh(sender.getEntityWorld()); // Refresh RBMK gamerules.
					event.setCanceled(true);
				} catch(Throwable t) {
					event.setException(t);
				}
			}
		}
	}

	@SubscribeEvent
	public void enteringChunk(EnteringChunk evt) {

		/*if(evt.entity instanceof EntityMissileBaseNT) {
			((EntityMissileBaseNT) evt.entity).loadNeighboringChunks(evt.newChunkX, evt.newChunkZ);
		}

		if(evt.entity instanceof EntityMissileCustom) {
			((EntityMissileCustom) evt.entity).loadNeighboringChunks(evt.newChunkX, evt.newChunkZ);
		}*/
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {

		//test for automatic in-world block replacement

		/*for(int x = 0; x < 16; x++) for(int y = 0; y < 255; y++) for(int z = 0; z < 16; z++) {
			if(event.getChunk().getBlockState(x, y, z).getBlock() instanceof MachineArcFurnace) {
				event.getChunk().func_150807_a(x, y, z, Blocks.AIR, 0);
			}
		}*/

		for(int x = 0; x < 16; x++) for(int y = 0; y < 255; y++) for(int z = 0; z < 16; z++) {
			if(event.getChunk().getBlockState(x, y, z).getBlock() == ModBlocks.absorber) {
				event.getChunk().setBlockState(new BlockPos(x, y, z), ModBlocks.rad_absorber.getStateFromMeta(0));
			}
			else if(event.getChunk().getBlockState(x, y, z).getBlock() == ModBlocks.absorber_red) {
				event.getChunk().setBlockState(new BlockPos(x, y, z), ModBlocks.rad_absorber.getStateFromMeta(1));
			}
			else if(event.getChunk().getBlockState(x, y, z).getBlock() == ModBlocks.absorber_green) {
				event.getChunk().setBlockState(new BlockPos(x, y, z), ModBlocks.rad_absorber.getStateFromMeta(2));
			}
			else if(event.getChunk().getBlockState(x, y, z).getBlock() == ModBlocks.absorber_pink) {
				event.getChunk().setBlockState(new BlockPos(x, y, z), ModBlocks.rad_absorber.getStateFromMeta(3));
			}
		}
	}


	@SubscribeEvent
	public void itemCrafted(PlayerEvent.ItemCraftedEvent e) {
		AchievementHandler.fire(e.player, e.crafting);
	}

	@SubscribeEvent
	public void itemSmelted(PlayerEvent.ItemSmeltedEvent e) {
		AchievementHandler.fire(e.player, e.smelting);

		if(!e.player.world.isRemote && e.smelting.getItem() == Items.IRON_INGOT && e.player.getRNG().nextInt(64) == 0) {

			if(!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.lodestone)))
				e.player.dropItem(new ItemStack(ModItems.lodestone), false);
			else
				e.player.inventoryContainer.detectAndSendChanges();
		}

		if(!e.player.world.isRemote && e.smelting.getItem() == ModItems.ingot_uranium && e.player.getRNG().nextInt(64) == 0) {

			if(!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.quartz_plutonium)))
				e.player.dropItem(new ItemStack(ModItems.quartz_plutonium), false);
			else
				e.player.inventoryContainer.detectAndSendChanges();
		}
	}

	@SubscribeEvent
	public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
		if(event.pickedUp.getItem().getItem() == Items.SLIME_BALL)
			com.hbm.advancement.NtmAdvancements.award(event.player, MainRegistry.achSlimeball);
		if(event.pickedUp.getItem().getItem() == ModItems.egg_balefire)
			com.hbm.advancement.NtmAdvancements.award(event.player, MainRegistry.rotConsum);
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {

		EntityPlayer player = event.getPlayer();
		BlockPos pos = event.getPos();
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = event.getState().getBlock();

		if(!(player instanceof EntityPlayerMP))
			return;

		if(block == ModBlocks.stone_gneiss && !com.hbm.advancement.NtmAdvancements.has(player, MainRegistry.achStratum)) {
			com.hbm.advancement.NtmAdvancements.award(event.getPlayer(), MainRegistry.achStratum);
			event.setExpToDrop(500);
		}

		if(block == Blocks.COAL_ORE || block == Blocks.COAL_BLOCK || block == ModBlocks.ore_lignite) {

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

				int nx = x + dir.offsetX;
				int ny = y + dir.offsetY;
				int nz = z + dir.offsetZ;

				if(event.getWorld().rand.nextInt(2) == 0 && com.hbm.world.WorldBlockState.read(event.getWorld(), nx, ny, nz).getBlock() == Blocks.AIR)
					com.hbm.world.WorldBlockState.writeLegacyBlock(event.getWorld(), nx, ny, nz, ModBlocks.gas_coal);
			}
		}

		if(y > event.getWorld().provider.getHorizon()) {
			if(block.getLightValue(event.getState(), event.getWorld(), pos) > 10) {
				// Placing blocks onto beams counts as a break, but doesn't reduce light count
				if(!(block instanceof BlockBeamBase)) {
					CelestialBody body = CelestialBody.getBody(event.getWorld());
					CBT_Lights lights = body.getTrait(CBT_Lights.class);

					if(lights == null) lights = new CBT_Lights();
					lights.removeLight(block, x, y, z);

					body.modifyTraits(lights);
				}

			}
		}


		if(RadiationConfig.enablePollution && RadiationConfig.enableLeadFromBlocks) {
			if(!ArmorRegistry.hasProtection(player, 3, HazardClass.PARTICLE_FINE)) {

				float metal = PollutionHandler.getPollution(player.world, x, y, z, PollutionType.HEAVYMETAL);

				if(metal < 5) return;

				if(metal < 10) {
					player.addPotionEffect(new PotionEffect(HbmPotion.lead, 100, 0));
				} else if(metal < 25) {
					player.addPotionEffect(new PotionEffect(HbmPotion.lead, 100, 1));
				} else {
					player.addPotionEffect(new PotionEffect(HbmPotion.lead, 100, 2));
				}
			}
		}
	}

	@SubscribeEvent
	public void onAnvilRepair(AnvilRepairEvent event) {

		// Anvil renaming no longer increments the repair cost
		// Note: Forge has a bug, the names are wrong. Right is output, output is left, left is right
		ItemStack left = event.getItemInput();
		ItemStack right = event.getIngredientInput();
		ItemStack output = event.getItemResult();
		if(left.isEmpty() && !right.isEmpty() && !output.isEmpty()) {
			int oldRepairCost = output.getRepairCost();

			if (oldRepairCost > 0) {
				right.setRepairCost(oldRepairCost);
			} else if (right.hasTagCompound()) {
				NBTTagCompound nbt = right.getTagCompound();
				nbt.removeTag("RepairCost");
				if (nbt.getKeySet().isEmpty()) {
					right.setTagCompound(null);
				}
			}
		}
	}

	@SubscribeEvent
	public void onClickBlock(PlayerInteractEvent.RightClickBlock event) {

		int x = event.getPos().getX();
		int y = event.getPos().getY();
		int z = event.getPos().getZ();
		World world = event.getWorld();

		if(GeneralConfig.enable528ExplosiveEnergistics && !world.isRemote) {
			Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();
			String name = b.getRegistryName() == null ? null : b.getRegistryName().toString();
			if(name != null && name.startsWith("appliedenergistics2")) {
				world.destroyBlock(new BlockPos(x, y, z), false);
				ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 5, null);
				vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 20).setupPiercing(5, 0.2F));
				vnt.setPlayerProcessor(new PlayerProcessorStandard());
				vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
				vnt.explode();
				event.setCanceled(true);
			}
		}

		x = event.getPos().getX();
		y = event.getPos().getZ();
		z = event.getPos().getY();

		if(!world.isRemote && world.getTileEntity(new BlockPos(x, y, z)) instanceof TileEntitySign) {

			TileEntitySign sign = (TileEntitySign)world.getTileEntity(new BlockPos(x, y, z));

			String result = ShadyUtil.smoosh(sign.signText[0].getUnformattedText(), sign.signText[1].getUnformattedText(), sign.signText[2].getUnformattedText(), sign.signText[3].getUnformattedText());

			if(ShadyUtil.hashes.contains(result)) {
				world.destroyBlock(new BlockPos(x, y, z), false);
				EntityItem entityitem = new EntityItem(world, x, y, z, new ItemStack(ModItems.bobmazon_hidden));
				entityitem.setPickupDelay(1);
				world.spawnEntity(entityitem);
				MainRegistry.logger.log(Level.FATAL, "THE HIDDENCAT HAS BEEN OBTAINED " + " x: " + x + " / "	+ " y: " + + y + " / "+ "z: " + + z + " by " + event.getEntityPlayer().getDisplayNameString() + "!");

			}
		}
	}

	// This is really fucky, but ensures we can respawn safely on celestial bodies
	// and prevents beds exploding
	@SubscribeEvent
	public void onTrySleep(PlayerInteractEvent.RightClickBlock event) {
		if(event.getWorld().isRemote) return;
		if(event.getWorld().provider.getDimension() == 0) return;
		if(!(event.getWorld().provider instanceof WorldProviderCelestial) && !(event.getWorld().provider instanceof WorldProviderOrbit)) return;

		if(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof BlockBed) {
			WorldProviderCelestial.attemptingSleep = true;
		}
	}

	// PULL THE LEVER KRONK
	@SubscribeEvent
	public void onPull(PlayerInteractEvent.RightClickBlock event) {
		int x = event.getPos().getX();
		int y = event.getPos().getY();
		int z = event.getPos().getZ();
		World world = event.getWorld();

		if(!world.isRemote && world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.LEVER && GeneralConfig.enableExtendedLogging == true) {
			MainRegistry.logger.log(Level.INFO, "[DET] pulled lever at " + x + " / " + y + " / " + z + " by " + event.getEntityPlayer().getDisplayNameString() + "!");
		}
	}



	@SubscribeEvent
	public void chatEvent(ServerChatEvent event) {

		EntityPlayerMP player = event.getPlayer();
		String message = event.getMessage();

		//boolean conditions for the illiterate, edition 1
		//bellow you can see the header of an if-block. inside the brackets, there is a boolean statement.
		//that means nothing other than its value totaling either 'true' or 'false'
		//examples: 'true' would just mean true
		//'1 > 3' would equal false
		//'i < 10' would equal true if 'i' is smaller than 10, if equal or greater, it will result in false

		//let's start from the back:

		//this part means that the message's first character has to equal a '!': ----------------------------+
		//                                                                                                   |
		//this is a logical AND operator: ----------------------------------------------------------------+  |
		//                                                                                                |  |
		//this is a reference to a field in                                                               |  |
		//Library.java containing a reference UUID: -----------------------------------------+            |  |
		//                                                                                   |            |  |
		//this will compare said UUID with                                                   |            |  |
		//the string representation of the                                                   |            |  |
		//current player's UUID: -----------+                                                |            |  |
		//                                  |                                                |            |  |
		//another AND operator: ---------+  |                                                |            |  |
		//                               |  |                                                |            |  |
		//this is a reference to a       |  |                                                |            |  |
		//boolean called                 |  |                                                |            |  |
		//'enableDebugMode' which is     |  |                                                |            |  |
		//only set once by the mod's     |  |                                                |            |  |
		//config and is disabled by      |  |                                                |            |  |
		//default. "debug" is not a      |  |                                                |            |  |
		//substring of the message, nor  |  |                                                |            |  |
		//something that can be toggled  |  |                                                |            |  |
		//in any other way except for    |  |                                                |            |  |
		//the config file: |             |  |                                                |            |  |
		//                 V             V  V                                                V            V  V
		if(GeneralConfig.enableDebugMode && player.getUniqueID().toString().equals(ShadyUtil.HbMinecraft) && message.startsWith("!")) {

			String[] msg = message.split(" ");

			String m = msg[0].substring(1, msg[0].length()).toLowerCase(Locale.US);

			if("gv".equals(m)) {

				int id = 0;
				int size = 1;
				int meta = 0;

				if(msg.length > 1 && NumberUtils.isNumber(msg[1])) {
					id = (int)(double)NumberUtils.createDouble(msg[1]);
				}

				if(msg.length > 2 && NumberUtils.isNumber(msg[2])) {
					size = (int)(double)NumberUtils.createDouble(msg[2]);
				}

				if(msg.length > 3 && NumberUtils.isNumber(msg[3])) {
					meta = (int)(double)NumberUtils.createDouble(msg[3]);
				}

				Item item = Item.getItemById(id);

				if(item != null && size > 0 && meta >= 0) {
					player.inventory.addItemStackToInventory(new ItemStack(item, size, meta));
				}
			}

			player.inventoryContainer.detectAndSendChanges();
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onFoodEaten(LivingEntityUseItemEvent.Finish event) {

		if(!(event.getEntityLiving() instanceof EntityPlayer)) return;
		ItemStack stack = event.getItem();

		if(!stack.isEmpty() && stack.getItem() instanceof ItemFood) {

			if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("ntmCyanide")) {
				for(int i = 0; i < 10; i++) {
					event.getEntityLiving().attackEntityFrom(rand.nextBoolean() ? ModDamageSource.euthanizedSelf : ModDamageSource.euthanizedSelf2, 1000);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onFishingLootTableLoad(LootTableLoadEvent event) {
		DimensionFishingLoot.onLootTableLoad(event);
	}

	@SubscribeEvent
	public void filterBrokenEntity(EntityJoinWorldEvent event) {

		Entity entity = event.getEntity();
		Entity[] parts = entity.getParts();

		if(parts != null) {

			for(int i = 0; i < parts.length; i++) {
				if(parts[i] == null) {
					MainRegistry.logger.error("Prevented spawning of multipart entity " + entity.getClass().getCanonicalName() + " due to parts being null!");
					event.setCanceled(true);
					return;
				}
			}
		}
	}


}
