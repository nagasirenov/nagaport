package com.hbm.main;

import com.hbm.lib.RefStrings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class ModSoundEvents {

	private static final Map<ResourceLocation, SoundEvent> SOUNDS = new LinkedHashMap<>();
	private static final Set<ResourceLocation> UNRESOLVED_WARNED = ConcurrentHashMap.newKeySet();

	/**
	 * Explicit registered silence for a genuinely unresolved legacy name. It is
	 * deliberately not a random real sound; callers still get a valid SoundEvent
	 * and the missing identity is logged instead of creating an unregistered
	 * runtime event.
	 */
	public static final SoundEvent MISSING = create("missing");

	public static final SoundEvent SLICER = create("entity.slicer");
	public static final SoundEvent BLOCK_OPENC = create("block.openc");
	public static final SoundEvent BLOCK_CLOSEC = create("block.closec");
	public static final SoundEvent STEP_METAL_BLOCK = create("step.metalBlock");
	public static final SoundEvent BLOCK_PIPE_PLACED = create("block.pipePlaced");
	public static final SoundEvent STEP_MORKITE = create("step.morkite");
	public static final SoundEvent ITEM_BATTERY = create("item.battery");
	public static final SoundEvent ITEM_UPGRADE_PLUG = create("item.upgradePlug");
	public static final SoundEvent BLOCK_CRATE_BREAK = create("block.crateBreak");
	public static final SoundEvent RECORD_LAMBDA_CORE = create("music.recordLambdaCore");
	public static final SoundEvent RECORD_SECTOR_SWEEP = create("music.recordSectorSweep");
	public static final SoundEvent RECORD_VORTAL_COMBAT = create("music.recordVortalCombat");
	public static final SoundEvent RECORD_TRANSMISSION = create("music.transmission");
	public static final SoundEvent RECORD_GOD_SPEED = create("music.recordGodSpeed");
	public static final SoundEvent RECORD_GOOP = create("music.recordGoop");
	public static final SoundEvent RECORD_ETHEREAL = create("music.recordEthereal");

	public static final SoundEvent WEAPON_EXPLOSION_LARGE_NEAR = create("weapon.explosionLargeNear");
	public static final SoundEvent WEAPON_EXPLOSION_LARGE_FAR = create("weapon.explosionLargeFar");
	public static final SoundEvent WEAPON_CASING_SMALL = create("weapon.casing.small");
	public static final SoundEvent WEAPON_CASING_MEDIUM = create("weapon.casing.medium");
	public static final SoundEvent WEAPON_CASING_LARGE = create("weapon.casing.large");
	public static final SoundEvent WEAPON_CASING_SHELL = create("weapon.casing.shell");

	public static final SoundEvent WEAPON_EXPLOSION_SMALL_NEAR = create("weapon.explosionSmallNear");
	public static final SoundEvent WEAPON_EXPLOSION_SMALL_FAR = create("weapon.explosionSmallFar");

	// Only compiler-proven coordinate effects; existing approved paths stay separate.
	private static final Map<ResourceLocation, SoundEvent> WORLD_EFFECTS = new LinkedHashMap<>();
	private static final Map<ResourceLocation, SoundCategory> WORLD_CATEGORIES = new LinkedHashMap<>();
	static {
		worldEffect("alarm.gambit", SoundCategory.RECORDS);
		worldEffect("alarm.trainHorn", SoundCategory.RECORDS);
		worldEffect("block.chungusLever", SoundCategory.BLOCKS);
		worldEffect("block.crateClose", SoundCategory.BLOCKS);
		worldEffect("block.crateOpen", SoundCategory.BLOCKS);
		worldEffect("block.damage", SoundCategory.BLOCKS);
		worldEffect("block.hatchImpact", SoundCategory.BLOCKS);
		worldEffect("block.screm", SoundCategory.BLOCKS);
		worldEffect("block.shutdown", SoundCategory.BLOCKS);
		worldEffect("block.storageClose", SoundCategory.BLOCKS);
		worldEffect("block.storageOpen", SoundCategory.BLOCKS);
		worldEffect("entity.bombWhistle", SoundCategory.PLAYERS);
		worldEffect("entity.oldExplosion", SoundCategory.AMBIENT);
		worldEffect("entity.planeCrash", SoundCategory.HOSTILE);
		worldEffect("entity.planeShotDown", SoundCategory.HOSTILE);
		worldEffect("entity.ufoBlast", SoundCategory.HOSTILE);
		worldEffect("item.pinBreak", SoundCategory.PLAYERS);
		worldEffect("item.techBleep", SoundCategory.PLAYERS);
		worldEffect("item.unpack", SoundCategory.PLAYERS);
		worldEffect("player.cough", SoundCategory.PLAYERS);
		worldEffect("player.vomit", SoundCategory.PLAYERS);
		worldEffect("turret.jeremy_fire", SoundCategory.BLOCKS);
		worldEffect("turret.jeremy_reload", SoundCategory.BLOCKS);
		worldEffect("turret.mortarWhistle", SoundCategory.BLOCKS);
		worldEffect("weapon.boat", SoundCategory.PLAYERS);
		worldEffect("weapon.cDeploy", SoundCategory.PLAYERS);
		worldEffect("weapon.chainsaw", SoundCategory.PLAYERS);
		worldEffect("weapon.dFlash", SoundCategory.PLAYERS);
		worldEffect("weapon.explosionMedium", SoundCategory.BLOCKS);
		worldEffect("weapon.explosionTiny", SoundCategory.BLOCKS);
		worldEffect("weapon.fire.disintegration", SoundCategory.PLAYERS);
		worldEffect("weapon.fire.tauRelease", SoundCategory.PLAYERS);
		worldEffect("weapon.flamethrowerShoot", SoundCategory.PLAYERS);
		worldEffect("weapon.fstbmbPing", SoundCategory.PLAYERS);
		worldEffect("weapon.immolatorShoot", SoundCategory.PLAYERS);
		worldEffect("weapon.kapeng", SoundCategory.PLAYERS);
		worldEffect("weapon.laserBang", SoundCategory.PLAYERS);
		worldEffect("weapon.missileTakeOff", SoundCategory.PLAYERS);
		worldEffect("weapon.mukeExplosion", SoundCategory.BLOCKS);
		worldEffect("weapon.reload.tubeFwoomp", SoundCategory.PLAYERS);
		worldEffect("weapon.robin_explosion", SoundCategory.PLAYERS);
		worldEffect("weapon.stingerLockOn", SoundCategory.PLAYERS);
		worldEffect("weapon.switchmode1", SoundCategory.PLAYERS);
		worldEffect("weapon.switchmode2", SoundCategory.PLAYERS);
		worldEffect("weapon.trainImpact", SoundCategory.PLAYERS);
		// Additional compiler-attributed literal coordinate effects with original bundled resources.
		worldEffect("alarm.chime", SoundCategory.RECORDS);
		worldEffect("alarm.hatch", SoundCategory.RECORDS);
		worldEffect("alarm.soyuzed", SoundCategory.RECORDS);
		worldEffect("block.boilerGroan", SoundCategory.BLOCKS);
		worldEffect("block.hatchOpen", SoundCategory.BLOCKS);
		worldEffect("block.hornFarDual", SoundCategory.BLOCKS);
		worldEffect("block.hornFarSingle", SoundCategory.BLOCKS);
		worldEffect("block.hornNearDual", SoundCategory.BLOCKS);
		worldEffect("block.hornNearSingle", SoundCategory.BLOCKS);
		worldEffect("block.missileAssembly2", SoundCategory.BLOCKS);
		worldEffect("block.pressOperate", SoundCategory.BLOCKS);
		worldEffect("block.rbmk_explosion", SoundCategory.BLOCKS);
		worldEffect("block.reactorStart", SoundCategory.BLOCKS);
		worldEffect("block.reactorStop", SoundCategory.BLOCKS);
		worldEffect("block.sonarPing", SoundCategory.BLOCKS);
		worldEffect("block.steamEngineOperate", SoundCategory.BLOCKS);
		worldEffect("block.turbinegasShutdown", SoundCategory.BLOCKS);
		worldEffect("block.turbinegasStartup", SoundCategory.BLOCKS);
		worldEffect("block.vaultScrapeNew", SoundCategory.BLOCKS);
		worldEffect("block.vaultThudNew", SoundCategory.BLOCKS);
		worldEffect("block.warnOverspeed", SoundCategory.BLOCKS);
		worldEffect("door.garage_stop", SoundCategory.BLOCKS);
		worldEffect("door.wgh_stop", SoundCategory.BLOCKS);
		worldEffect("entity.soyuzTakeoff", SoundCategory.PLAYERS);
		worldEffect("item.techBoop", SoundCategory.PLAYERS);
		worldEffect("misc.htrfstart", SoundCategory.BLOCKS);
		worldEffect("misc.htrstart", SoundCategory.BLOCKS);
		worldEffect("misc.htrstop", SoundCategory.BLOCKS);
		worldEffect("misc.lpwstart", SoundCategory.BLOCKS);
		worldEffect("misc.lpwstop", SoundCategory.BLOCKS);
		worldEffect("misc.spincharge", SoundCategory.BLOCKS);
		worldEffect("misc.spinshot", SoundCategory.BLOCKS);
		worldEffect("player.oil", SoundCategory.PLAYERS);
		worldEffect("turret.chekhov_fire", SoundCategory.BLOCKS);
		worldEffect("turret.howard_fire", SoundCategory.BLOCKS);
		worldEffect("turret.howard_reload", SoundCategory.BLOCKS);
		worldEffect("turret.richard_fire", SoundCategory.BLOCKS);
		worldEffect("turret.sentry_fire", SoundCategory.BLOCKS);
		worldEffect("weapon.fstbmbStart", SoundCategory.PLAYERS);
		worldEffect("weapon.rocketFlame", SoundCategory.BLOCKS);
		worldEffect("weapon.sparkShoot", SoundCategory.PLAYERS);
		worldEffect("weapon.tauShoot", SoundCategory.PLAYERS);
		worldEffect("block.squeakyToy", SoundCategory.BLOCKS);
		worldEffect("player.groan", SoundCategory.PLAYERS);
		worldEffect("player.gulp", SoundCategory.PLAYERS);
		worldEffect("weapon.coilgunReload", SoundCategory.PLAYERS);
		worldEffect("weapon.fire.shredderCycle", SoundCategory.PLAYERS);
		worldEffect("weapon.fire.smack", SoundCategory.PLAYERS);
		worldEffect("weapon.fire.tau", SoundCategory.PLAYERS);
		worldEffect("weapon.foley.gunWhack", SoundCategory.PLAYERS);
		worldEffect("weapon.glClose", SoundCategory.PLAYERS);
		worldEffect("weapon.glOpen", SoundCategory.PLAYERS);
		worldEffect("weapon.glReload", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.boltClose", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.boltOpen", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.dryFireClick", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.fatmanFull", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.impact", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.insertCanister", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.insertRocket", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.leverCock", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.magInsert", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.magRemove", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.magSmallInsert", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.magSmallRemove", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.openLatch", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.pistolCock", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.pressureValve", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.revolverClose", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.revolverCock", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.revolverSpin", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.rifleCock", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.screw", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.shotgunCock", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.shotgunCockClose", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.shotgunCockOpen", SoundCategory.PLAYERS);
		worldEffect("weapon.reload.shotgunReload", SoundCategory.PLAYERS);

	}

	private static void worldEffect(String path, SoundCategory category) {
		SoundEvent sound = create(path);
		// Do not call SoundEvent.getSoundName(): it is a dev-name method that is
		// absent from the SRG runtime class. The registration key is authoritative.
		ResourceLocation name = new ResourceLocation(RefStrings.MODID, path);
		WORLD_EFFECTS.put(name, sound);
		WORLD_CATEGORIES.put(name, category);
	}

	public static boolean isWorldEffect(ResourceLocation name) { return WORLD_EFFECTS.containsKey(name); }

	public static SoundEvent worldEffect(ResourceLocation name) {
		SoundEvent sound = WORLD_EFFECTS.get(name);
		if(sound == null) throw new IllegalArgumentException("Unsupported world sound effect: " + name);
		return sound;
	}

	public static SoundCategory worldEffectCategory(ResourceLocation name) {
		worldEffect(name); // Reject unknown names rather than guessing MASTER.
		return WORLD_CATEGORIES.get(name);
	}

	private static SoundEvent create(String path) {
		// Native ResourceLocation normalizes the original sound-event keys to lowercase,
		// just as SoundHandler does while loading sounds.json.
		ResourceLocation name = new ResourceLocation(RefStrings.MODID, path);
		SoundEvent existing = SOUNDS.get(name);
		if(existing != null) return existing;
		SoundEvent sound = new SoundEvent(name).setRegistryName(name);
		SOUNDS.put(name, sound);
		return sound;
	}

	private static SoundEvent resolveVanilla(String name) {
		switch(name) {
			case "random.fizz": return SoundEvents.BLOCK_FIRE_EXTINGUISH;
			case "random.explode": return SoundEvents.ENTITY_GENERIC_EXPLODE;
			case "random.click": return SoundEvents.BLOCK_LEVER_CLICK;
			case "random.anvil_land": return SoundEvents.BLOCK_ANVIL_LAND;
			case "random.anvil_use": return SoundEvents.BLOCK_ANVIL_USE;
			case "random.anvil_break": return SoundEvents.BLOCK_ANVIL_DESTROY;
			case "random.bow": return SoundEvents.ENTITY_ARROW_SHOOT;
			case "random.bowhit": return SoundEvents.ENTITY_ARROW_HIT;
			case "random.break": return SoundEvents.ENTITY_ITEM_BREAK;
			case "random.burp": return SoundEvents.ENTITY_PLAYER_BURP;
			case "random.chestclosed": return SoundEvents.BLOCK_CHEST_CLOSE;
			case "random.chestopen": return SoundEvents.BLOCK_CHEST_OPEN;
			case "random.door_open": return SoundEvents.BLOCK_WOODEN_DOOR_OPEN;
			case "random.door_close": return SoundEvents.BLOCK_WOODEN_DOOR_CLOSE;
			case "random.drink": return SoundEvents.ENTITY_GENERIC_DRINK;
			case "random.eat": return SoundEvents.ENTITY_GENERIC_EAT;
			case "random.glass": return SoundEvents.BLOCK_GLASS_BREAK;
			case "random.levelup": return SoundEvents.ENTITY_PLAYER_LEVELUP;
			case "random.orb": return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
			case "random.pop": return SoundEvents.ENTITY_ITEM_PICKUP;
			case "random.splash": return SoundEvents.ENTITY_GENERIC_SPLASH;
			case "random.successful_hit": return SoundEvents.ENTITY_ARROW_HIT_PLAYER;
			case "fire.fire": return SoundEvents.BLOCK_FIRE_AMBIENT;
			case "fire.ignite": return SoundEvents.ITEM_FLINTANDSTEEL_USE;
			case "liquid.lava": return SoundEvents.BLOCK_LAVA_AMBIENT;
			case "liquid.lavapop": return SoundEvents.BLOCK_LAVA_POP;
			case "liquid.water": return SoundEvents.BLOCK_WATER_AMBIENT;
			case "liquid.splash": return SoundEvents.ENTITY_GENERIC_SPLASH;
			case "liquid.swim": return SoundEvents.ENTITY_GENERIC_SWIM;
			case "game.neutral.swim.splash": return SoundEvents.ENTITY_GENERIC_SPLASH;
			case "game.tnt.primed": return SoundEvents.ENTITY_TNT_PRIMED;
			case "ambient.weather.thunder": return SoundEvents.ENTITY_LIGHTNING_THUNDER;
			case "ambient.weather.rain": return SoundEvents.WEATHER_RAIN;
			case "ambient.cave.cave": return SoundEvents.AMBIENT_CAVE;
			case "mob.zombie.woodbreak": return SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD;
			case "mob.zombie.wood": return SoundEvents.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD;
			case "mob.zombie.metal": return SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR;
			case "mob.endermen.portal": return SoundEvents.ENTITY_ENDERMEN_TELEPORT;
			case "mob.blaze.hit": return SoundEvents.ENTITY_BLAZE_HURT;
			case "mob.blaze.breathe": return SoundEvents.ENTITY_BLAZE_AMBIENT;
			case "mob.ghast.fireball": return SoundEvents.ENTITY_GHAST_SHOOT;
			case "mob.ghast.scream": return SoundEvents.ENTITY_GHAST_SCREAM;
			case "mob.creeper.primed": return SoundEvents.ENTITY_CREEPER_PRIMED;
			case "mob.bat.takeoff": return SoundEvents.ENTITY_BAT_TAKEOFF;
			case "mob.irongolem.hit": return SoundEvents.ENTITY_IRONGOLEM_HURT;
			case "mob.irongolem.death": return SoundEvents.ENTITY_IRONGOLEM_DEATH;
			case "tile.piston.out": return SoundEvents.BLOCK_PISTON_EXTEND;
			case "tile.piston.in": return SoundEvents.BLOCK_PISTON_CONTRACT;
			case "minecart.base": return SoundEvents.ENTITY_MINECART_RIDING;
			case "minecart.inside": return SoundEvents.ENTITY_MINECART_RIDING;
			case "note.harp": return SoundEvents.BLOCK_NOTE_HARP;
			case "note.bd": return SoundEvents.BLOCK_NOTE_BASEDRUM;
			case "note.snare": return SoundEvents.BLOCK_NOTE_SNARE;
			case "note.hat": return SoundEvents.BLOCK_NOTE_HAT;
			case "note.bassattack": return SoundEvents.BLOCK_NOTE_BASS;
			case "note.pling": return SoundEvents.BLOCK_NOTE_PLING;
			case "note.bell": return SoundEvents.BLOCK_NOTE_BELL;
			case "note.flute": return SoundEvents.BLOCK_NOTE_FLUTE;
			case "note.chime": return SoundEvents.BLOCK_NOTE_CHIME;
			case "note.guitar": return SoundEvents.BLOCK_NOTE_GUITAR;
			case "note.xylophone": return SoundEvents.BLOCK_NOTE_XYLOPHONE;
			case "step.stone": return SoundEvents.BLOCK_STONE_STEP;
			case "step.wood": return SoundEvents.BLOCK_WOOD_STEP;
			case "step.gravel": return SoundEvents.BLOCK_GRAVEL_STEP;
			case "step.grass": return SoundEvents.BLOCK_GRASS_STEP;
			case "step.metal": return SoundEvents.BLOCK_METAL_STEP;
			case "step.sand": return SoundEvents.BLOCK_SAND_STEP;
			case "step.snow": return SoundEvents.BLOCK_SNOW_STEP;
			case "step.ladder": return SoundEvents.BLOCK_LADDER_STEP;
			case "step.cloth": return SoundEvents.BLOCK_CLOTH_STEP;
			case "dig.stone": return SoundEvents.BLOCK_STONE_HIT;
			case "dig.wood": return SoundEvents.BLOCK_WOOD_HIT;
			case "dig.gravel": return SoundEvents.BLOCK_GRAVEL_HIT;
			case "dig.grass": return SoundEvents.BLOCK_GRASS_HIT;
			case "dig.metal": return SoundEvents.BLOCK_METAL_HIT;
			case "dig.sand": return SoundEvents.BLOCK_SAND_HIT;
			case "dig.snow": return SoundEvents.BLOCK_SNOW_HIT;
			case "dig.glass": return SoundEvents.BLOCK_GLASS_HIT;
			case "dig.cloth": return SoundEvents.BLOCK_CLOTH_HIT;
			default: return null;
		}
	}

	/** Resolve sound strings with legacy vanilla translation and defensive fallbacks. */
	public static SoundEvent resolve(String name) {
		SoundEvent vanilla = resolveVanilla(name);
		if(vanilla != null) return vanilla;

		ResourceLocation location = new ResourceLocation(name);
		// Blocks are constructed in preInit, before the native sound registry event.
		// Keep the exact object that registerSounds will register later.
		SoundEvent sound = SOUNDS.get(location);
		if(sound == null && !name.contains(":")) {
			sound = SOUNDS.get(new ResourceLocation(RefStrings.MODID, name));
		}
		if(sound == null) sound = SoundEvent.REGISTRY.getObject(location);
		if(sound == null && !name.contains(":")) {
			sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(RefStrings.MODID, name));
		}
		if(sound == null) {
			if(UNRESOLVED_WARNED.add(location)) {
				MainRegistry.logger.warn("Unresolved legacy sound event '{}'; using the registered silent placeholder", name);
			}
			sound = MISSING;
		}
		return sound;
	}

	private ModSoundEvents() { }

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for(SoundEvent sound : SOUNDS.values()) {
            ResourceLocation name = sound.getRegistryName();
            if(event.getRegistry().containsKey(name)) {
                if(event.getRegistry().getValue(name) != sound)
                    throw new IllegalStateException("Conflicting sound event registration: " + name);
                continue;
            }
            event.getRegistry().register(sound);
        }
	}
}
