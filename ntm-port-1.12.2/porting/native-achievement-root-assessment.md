# Native achievement-root assessment, Forge 1.12.2-14.23.5.2768

Read-only assessment. No production edits, builds, downloads, source exclusions, or compatibility types were made. Original source is /workspace/shared/nagaport/ntm-original-pristine/src/main/java. Port source is its ntm-port-1.12.2 sibling. API and bytecode evidence comes from the port's .gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar, inspected with the local Java 8 javap. Source line numbers below refer to pristine source unless stated otherwise.

## Decision summary

Use native advancements for visible completion and native statistics for actual numeric counters. Do not mechanically replace Achievement with StatBasic: that loses dependency enforcement, UI icons/tree/special frames, notifications and achievement-style completion. Do not merely replace triggerAchievement with grantCriterion: 1.12.2 grantCriterion does not inspect parent completion. Preserve award-event predicates and recipients at existing call sites and explicitly check the original gameplay prerequisite separately from the visual parent.

A complete native port is not a declaration-only change. It spans all award sites, crafting dispatch, Bobmazon authorization and icons, gneiss's first-time XP gate, and legacy vanilla awards. The smallest coherent system is data-defined native advancements with one impossible criterion each, ResourceLocation identifiers resolved through the server AdvancementManager, and a mod-owned server grant/query utility. This is ordinary mod gameplay logic using native APIs, not an emulated net.minecraft.stats.Achievement class. No fake API types should be introduced.

Decisions needed: native auto-layout versus recreation of the exact old achievement page; the treatment of the existing broken rotConsum declaration; and whether original-world/player-save migration is a deliverable. The vanilla iron/fish/inventory-open semantics need explicit preservation, not a silent substitution with broader modern awards.

## Verified native API and semantics

- Advancement(ResourceLocation, Advancement parent, DisplayInfo, AdvancementRewards, Map<String,Criterion>, String[][] requirements).
- DisplayInfo(ItemStack, ITextComponent title, ITextComponent description, ResourceLocation background, FrameType, boolean toast, boolean announce, boolean hidden); getIcon(), setPosition(float,float).
- EntityPlayerMP.getAdvancements(); PlayerAdvancements.getProgress(Advancement).isDone(); grantCriterion(Advancement,String); revokeCriterion; save/reload.
- AdvancementManager.getAdvancement(ResourceLocation), getAdvancements(), reload(). ForgeHooks.loadAdvancements loads mod assets during manager reload. Use assets/hbm/advancements/*.json in this Minecraft version, not the later data directory format. Resolve current manager objects after reload; do not keep stale Advancement objects indefinitely.
- grantCriterion bytecode has no gameplay parent gate. It awards configured rewards and announcements on first completion. Forge 2768 explicitly rejects FakePlayer in grantCriterion. Assess that divergence for automation rather than treating all EntityPlayerMP as equivalent.
- AdvancementManager.reload calls AdvancementTreeNode.layout on displayed roots after loading. Standard JSON does not preserve the original x/y grid. DisplayInfo.setPosition exists, but custom positioning needs a reliable post-load/reload synchronization strategy, not guessed JSON keys.
- StatBasic(String,ITextComponent), StatBase.registerStat(), EntityPlayerMP.getStatFile().readStat(StatBase), EntityPlayer.addStat(StatBase,int) exist. initIndependentStat returns StatBase. StatBasic has no achievement parent/icon/frame. Retain stat.ntmLegendary, stat.ntmMines and stat.ntmBullets as native statistics.
- ClientAdvancementManager supports a single listener; do not casually install a permanent listener that disrupts the native screen. Bobmazon currently reads completion server-side and its client rendering only needs the requirement icon, so dedicated client completion tracking is not demonstrated necessary by these sources.

## Preservation rules and traps

1. Preserve prerequisites as runtime award gates. A child event before its prerequisite should not be silently queued or retroactively granted unless original 1.7 semantics are verified to do that. initIndependentStat on these declarations is not proof that the parent is cosmetic. The old engine jar was not present in the inspected project/toolchain, so validate original engine increment semantics before implementing exact replay behavior or numeric achievement-count mirroring.
2. Keep original event ownership, radii, conditions and ordering. In particular achGoFish goes to the player victim, not the shooter; reactor/boss/nuclear awards often go to nearby/all players rather than the killer; radPoison is in the 200 <= radiation < 400 branch, not every level >=200; digamma stages fire sequentially at >0, >=2, >=10.
3. Bobmazon gates: NONE=openInventory; STEEL=achBlastFurnace; ASSEMBLY=achAssembly; CHEMICS=achChemplant; OIL=achDesh; NUCLEAR=achTechnetium; HIDDEN=bobHidden. Packet authorization is (req != null AND completed(req)) OR creative. Do not make NONE universally true without an explicit decision. Keep creative bypass and all costs unchanged. Rendering uses requirement.achievement.theItemStack, so migrate to an explicit native ItemStack icon or resolved Advancement DisplayInfo icon.
4. First gneiss break: server EntityPlayerMP only, achStratum incomplete, then grant and set block-drop XP to 500. Subsequent breaks do not get this bonus. Preserve across reconnect, death/respawn and server restart. Do not add advancement XP rewards as well, which doubles it.
5. Crafting map matches ComparableStack.makeSingular and includes 35 entries. Retain metadata matching and original call sites (craft/smelt events, output slots, shift-click machine containers, anvil packet). Do not replace with possession/inventory_changed criteria: receiving/trading an item is not crafting it.
6. Do not add new achievement rewards. Use AdvancementRewards.EMPTY/default no-reward data. Special frame mapping to native challenge is visual and should not introduce challenge reward XP.
7. Several declarations have no awards in the pristine source. Preserve them as dormant definitions; do not invent unlock predicates.
8. rotConsum has a public field and egg_balefire pickup trigger but its constructor is commented out. It is NOT one of the 63 active awards. Flag the pre-existing bug explicitly; neither silently remove the call nor invent a new registered award.

## Legacy vanilla award differences requiring care

SlotSmelting gives acquireIron for Items.iron_ingot and cookFish for Items.cooked_fished after the smelt event, independently of its XP math. Preserve those two effects. Forge 2768 assets contain minecraft:story/smelt_iron, but its criterion is inventory_changed containing an iron ingot, not smelting; its parent is story/upgrade_tools. Thus mapping to that advancement broadens award eligibility and does not preserve old prerequisites. There is no corresponding standalone fish cooking advancement in the inspected built-in advancement paths. balanced_diet is food consumption and is not a substitute. openInventory also has no equivalent: story/root requires possessing a crafting table, not opening inventory.

Exact preservation therefore needs explicit native legacy award definitions for iron and fish (plus their relevant old prerequisite chain and event tracking), and an explicit inventory-open completion signal for Bobmazon NONE. This expands beyond three replacement calls because old prerequisite awards belonged to vanilla 1.7. Alternatives (modern smelt_iron plus a custom fish award, or always-true NONE) are gameplay changes requiring approval. Do not claim parity before choosing and testing this strategy. A StatBasic with the old ID alone only preserves a counter, not the vanished vanilla achievement UI/parent events.

## UI differences and scope decision

The old single Nuclear Tech page contains 21 unrelated root nodes and hand-placed coordinates. Default native Advancement roots create separate tabs. A single Nuclear Tech tab needs a synthetic displayed root and visual parent links for all old roots; that synthetic root must be excluded from gameplay prerequisite gating and unlocked without extra rewards. Old 42 dependency links remain separate gameplay data. Native auto-layout, visibility, title/description formatting, special frames, notifications, and root tab structure differ. Preserving exact old GUI appearance requires a new mod GUI (not a shim class), so approval is needed if exact appearance is mandatory. Avoid silently marking bobHidden hidden simply because of its variable name: the original declaration does not set a hidden flag.

## Save migration is a separate requirement

Fresh-world native persistence follows PlayerAdvancements; it does not automatically read 1.7 achievement entries from player stats JSON. Keep an explicit old achievement ID -> native ResourceLocation table, preserving capitalization in old IDs but using lowercase native paths. If existing player saves must retain progress, implement a one-time, backed-up, idempotent migration at the appropriate player/server lifecycle, with validation of player UUID/world provenance, old completion values, all parent cases, and no duplicate XP/items/chat. Keep numeric legacy counts separately if required. Never reset old data, assume copying a world across versions is safe, or infer migration permission from a source-only port request. Login/restart persistence tests on new 1.12 saves are required even if old-world migration is out of scope.

## Testable implementation plan

1. Freeze a generated inventory of definitions (below), all 42 parent pairs, 35 crafting map entries and award call sites. Resolve rotConsum and vanilla preservation/UI/save scope decisions before behavior-changing edits.
2. Add native advancement resources using original names/descriptions/icons (including item metadata), one impossible criterion per mod award, no rewards; maintain a separate parent-gate table and old-ID map. Use deterministic lowercase paths, not default locale case conversion.
3. Replace mod declarations/consumers with ResourceLocation and native grant/query functions. Preserve all existing guards and delivery recipients. Update Bobmazon server checks and icons and gneiss gate in the same coherent slice.
4. Preserve 3 ordinary stats and native item/block craft/use statistics. BossSpawnHandler's acidizer gate is crafting/placing machine_crystallizer, NOT achievement completion; migrate array access to StatList.getCraftStats/getObjectUseStats and writeStat to readStat without replacing it by achAcidizer.
5. Implement the approved vanilla legacy strategy including inventory-open; verify iron and cooked-fish custom smelting and normal vanilla paths, prerequisite state, metadata variants, shift-click, and exactly-once notification behavior. Keep furnace XP and random smelting extras unchanged.
6. Static checks: no net.minecraft.stats.Achievement/AchievementList/AchievementPage, triggerAchievement, hasAchievementUnlocked or theItemStack remain; exactly 63 active original definitions plus only explicitly approved added legacy/synthetic nodes; no source exclusions/stubs.
7. Runtime tests: every parent edge with child-before-parent and parent-before-child; every crafting map entry and negative possession-only case; two players at inside/outside original radii; Bobmazon 7 requirements and creative bypass; first/repeated gneiss 500 XP; radiation boundary values 199/200/399/400/999/1000 and digamma 0/>0/2/10; client versus dedicated server; fake-player discrepancy; no duplicate awards under repeated tick events.
8. Restart/relogin/death, advancement reload, multiple worlds and save migration fixtures if approved. Validate tab count, icons/meta, translations, special frames and unreachable definitions visually. Build then run these tests only after the coherent code slice exists; successful compilation alone is not behavior validation.

## Exact definition inventory

Each original declaration below preserves ID, name key, coordinates, icon, parent and special flag.

```java
achSacrifice = new Achievement("achievement.sacrifice", "sacrifice", -3, 1, ModItems.burnt_bark, null).initIndependentStat().setSpecial().registerStat();
achImpossible = new Achievement("achievement.impossible", "impossible", 18, 10, ModItems.nothing, null).initIndependentStat().setSpecial().registerStat();
achTOB = new Achievement("achievement.tasteofblood", "tasteofblood", 3, 10, new ItemStack(ModItems.fluid_icon, 1, Fluids.ASCHRAB.getID()), null).initIndependentStat().setSpecial().registerStat();
achGoFish = new Achievement("achievement.goFish", "goFish", 5, 10, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.GOFISH), null).initIndependentStat().setSpecial().registerStat();
achPotato = new Achievement("achievement.potato", "potato", -2, -2, ModItems.battery_potatos, null).initIndependentStat().setSpecial().registerStat();
achC20_5 = new Achievement("achievement.c20_5", "c20_5", 3, 6, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.QUESTIONMARK), null).initIndependentStat().setSpecial().registerStat();
achFiend = new Achievement("achievement.fiend", "fiend", -6, 8, ModItems.shimmer_sledge, null).initIndependentStat().setSpecial().registerStat();
achFiend2 = new Achievement("achievement.fiend2", "fiend2", -4, 9, ModItems.shimmer_axe, null).initIndependentStat().setSpecial().registerStat();
achStratum = new Achievement("achievement.stratum", "stratum", -4, -2, new ItemStack(ModBlocks.stone_gneiss), null).initIndependentStat().setSpecial().registerStat();
achOmega12 = new Achievement("achievement.omega12", "omega12", 17, -1, ModItems.particle_digamma, null).initIndependentStat().setSpecial().registerStat();
achNo9 = new Achievement("achievement.no9", "no9", -8, 12, ModItems.no9, null).initIndependentStat().registerStat();
achSlimeball = new Achievement("achievement.slimeball", "slimeball", -10, 6, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.ACID), null).initIndependentStat().registerStat();
achSulfuric = new Achievement("achievement.sulfuric", "sulfuric", -10, 8, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.BALLS), achSlimeball).initIndependentStat().setSpecial().registerStat();
achInferno = new Achievement("achievement.inferno", "inferno", -8, 10, ModItems.canister_napalm, null).initIndependentStat().setSpecial().registerStat();
achRedRoom = new Achievement("achievement.redRoom", "redRoom", -10, 10, ModItems.key_red, null).initIndependentStat().setSpecial().registerStat();
bobHidden = new Achievement("achievement.hidden", "hidden", 15, -4, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.QUESTIONMARK), null).initIndependentStat().registerStat();
horizonsStart = new Achievement("achievement.horizonsStart", "horizonsStart", -5, 4, ModItems.sat_gerald, null).initIndependentStat().registerStat();
horizonsEnd = new Achievement("achievement.horizonsEnd", "horizonsEnd", -3, 4, ModItems.sat_gerald, horizonsStart).initIndependentStat().registerStat();
horizonsBonus = new Achievement("achievement.horizonsBonus", "horizonsBonus", -1, 4, ModItems.sat_gerald, horizonsEnd).initIndependentStat().registerStat().setSpecial();
bossCreeper = new Achievement("achievement.bossCreeper", "bossCreeper", -7, 1, ModItems.coin_creeper, null).initIndependentStat().registerStat();
bossMeltdown = new Achievement("achievement.bossMeltdown", "bossMeltdown", -8, 3, ModItems.coin_radiation, bossCreeper).initIndependentStat().registerStat();
bossMaskman = new Achievement("achievement.bossMaskman", "bossMaskman", -8, -1, ModItems.coin_maskman, bossCreeper).initIndependentStat().registerStat();
bossWorm = new Achievement("achievement.bossWorm", "bossWorm", -8, -3, ModItems.coin_worm, bossMaskman).initIndependentStat().registerStat().setSpecial();
bossUFO = new Achievement("achievement.bossUFO", "bossUFO", -6, -3, ModItems.coin_ufo, bossWorm).initIndependentStat().registerStat().setSpecial();
achRadPoison = new Achievement("achievement.radPoison", "radPoison", -2, 6, ModItems.geiger_counter, null).initIndependentStat().registerStat();
achRadDeath = new Achievement("achievement.radDeath", "radDeath", 0, 6, Items.skull, achRadPoison).initIndependentStat().registerStat().setSpecial();
achSomeWounds = new Achievement("achievement.someWounds", "someWounds", -2, 10, ModItems.injector_knife, null).initIndependentStat().registerStat();
digammaSee = new Achievement("achievement.digammaSee", "digammaSee", -1, 8, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMASEE), null).initIndependentStat().registerStat();
digammaFeel = new Achievement("achievement.digammaFeel", "digammaFeel", 1, 8, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMAFEEL), digammaSee).initIndependentStat().registerStat();
digammaKnow = new Achievement("achievement.digammaKnow", "digammaKnow", 3, 8, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMAKNOW), digammaFeel).initIndependentStat().registerStat().setSpecial();
digammaKauaiMoho = new Achievement("achievement.digammaKauaiMoho", "digammaKauaiMoho", 5, 8, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMAKAUAIMOHO), digammaKnow).initIndependentStat().registerStat().setSpecial();
digammaUpOnTop = new Achievement("achievement.digammaUpOnTop", "digammaUpOnTop", 7, 8, DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMAUPONTOP), digammaKauaiMoho).initIndependentStat().registerStat().setSpecial();
achBurnerPress = new Achievement("achievement.burnerPress", "burnerPress", 0, 0, new ItemStack(ModBlocks.machine_press), null).initIndependentStat().registerStat();
achBlastFurnace = new Achievement("achievement.blastFurnace", "blastFurnace", 1, 3, new ItemStack(ModBlocks.machine_difurnace_off), achBurnerPress).initIndependentStat().registerStat();
achAssembly = new Achievement("achievement.assembly", "assembly", 3, -1, new ItemStack(ModBlocks.machine_assembly_machine), achBurnerPress).initIndependentStat().registerStat();
achSelenium = new Achievement("achievement.selenium", "selenium", 3, 2, ModItems.ingot_starmetal, achBurnerPress).initIndependentStat().setSpecial().registerStat();
achChemplant = new Achievement("achievement.chemplant", "chemplant", 6, -1, new ItemStack(ModBlocks.machine_chemical_plant), achAssembly).initIndependentStat().registerStat();
achConcrete	= new Achievement("achievement.concrete", "concrete", 6, -4, new ItemStack(ModBlocks.concrete), achChemplant).initIndependentStat().registerStat();
achPolymer = new Achievement("achievement.polymer", "polymer", 9, -1, ModItems.ingot_polymer, achChemplant).initIndependentStat().registerStat();
achDesh = new Achievement("achievement.desh", "desh", 9, 2, ModItems.ingot_desh, achChemplant).initIndependentStat().registerStat();
achTantalum = new Achievement("achievement.tantalum", "tantalum", 7, 3, ModItems.gem_tantalium, achChemplant).initIndependentStat().setSpecial().registerStat();
achGasCent = new Achievement("achievement.gasCent", "gasCent", 13, 2, ModItems.ingot_uranium_fuel, achDesh).initIndependentStat().registerStat();
achCentrifuge = new Achievement("achievement.centrifuge", "centrifuge", 12, -2, new ItemStack(ModBlocks.machine_centrifuge), achPolymer).initIndependentStat().registerStat();
achDriveFail = new Achievement("achievement.driveFail", "driveFail", 8, -3, new ItemStack(ModItems.full_drive), achPolymer).initIndependentStat().setSpecial().registerStat();
achFOEQ = new Achievement("achievement.FOEQ", "FOEQ", 5, 5, ModItems.sat_foeq, achDesh).initIndependentStat().setSpecial().registerStat();
achSoyuz = new Achievement("achievement.soyuz", "soyuz", 7, 6, Items.baked_potato, achDesh).initIndependentStat().setSpecial().registerStat();
achSpace = new Achievement("achievement.space", "space", 9, 7, ModItems.missile_soyuz, achDesh).initIndependentStat().setSpecial().registerStat();
achSchrab = new Achievement("achievement.schrab", "schrab", 11, 3, ModItems.ingot_schrabidium, achDesh).initIndependentStat().registerStat();
achAcidizer = new Achievement("achievement.acidizer", "acidizer", 11, 5, new ItemStack(ModBlocks.machine_crystallizer), achDesh).initIndependentStat().registerStat();
achRadium = new Achievement("achievement.radium", "radium", 13, -4, ModItems.coffee_radium, achCentrifuge).initIndependentStat().setSpecial().registerStat();
achTechnetium = new Achievement("achievement.technetium", "technetium", 15, -2, ModItems.ingot_tcalloy, achCentrifuge).initIndependentStat().registerStat();
achZIRNOXBoom = new Achievement("achievement.ZIRNOXBoom", "ZIRNOXBoom", 14, -1, ModItems.debris_element, achCentrifuge).initIndependentStat().setSpecial().registerStat();
achChicagoPile = new Achievement("achievement.chicagoPile", "chicagoPile", 13, 0, ModItems.pile_rod_plutonium, achCentrifuge).initIndependentStat().registerStat();
achSILEX = new Achievement("achievement.SILEX", "SILEX", 12, 7, new ItemStack(ModBlocks.machine_silex), achAcidizer).initIndependentStat().registerStat();
achWatz = new Achievement("achievement.watz", "watz", 14, 3, ModItems.watz_pellet, achSchrab).initIndependentStat().registerStat();
achWatzBoom = new Achievement("achievement.watzBoom", "watzBoom", 14, 5, ModItems.bucket_mud, achWatz).initIndependentStat().setSpecial().registerStat();
achRBMK = new Achievement("achievement.RBMK", "RBMK", 9, -5, ModItems.rbmk_fuel_ueu, achConcrete).initIndependentStat().registerStat();
achRBMKBoom = new Achievement("achievement.RBMKBoom", "RBMKBoom", 9, -7, ModItems.debris_fuel, achRBMK).initIndependentStat().setSpecial().registerStat();
achBismuth = new Achievement("achievement.bismuth", "bismuth", 11, -6, ModItems.ingot_bismuth, achRBMK).initIndependentStat().registerStat();
achBreeding = new Achievement("achievement.breeding", "breeding", 7, -6, ModItems.ingot_am_mix, achRBMK).initIndependentStat().setSpecial().registerStat();
achFusion = new Achievement("achievement.fusion", "fusion", 13, -7, new ItemStack(ModBlocks.iter), achBismuth).initIndependentStat().setSpecial().registerStat();
achRedBalloons = new Achievement("achievement.redBalloons", "redBalloons", 11, 0, ModItems.missile_nuclear, achPolymer).initIndependentStat().setSpecial().registerStat();
achManhattan = new Achievement("achievement.manhattan", "manhattan", 11, -4, new ItemStack(ModBlocks.nuke_boy), achPolymer).initIndependentStat().setSpecial().registerStat();
```

Counts: 63 active declarations, 64 public Achievement fields, 21 null-parent roots, 42 non-null-parent edges, 34 setSpecial declarations.
No external MainRegistry references found for these active definitions: achSacrifice, achImpossible, achTOB, achC20_5, horizonsBonus, digammaUpOnTop.
There are 40 textual triggerAchievement call sites in 30 source files, including the generic crafting dispatcher and the rotConsum call.

## Exact crafting mappings

```java
craftingAchievements.put(new ComparableStack(ModItems.piston_selenium), MainRegistry.achSelenium);
craftingAchievements.put(new ComparableStack(ModItems.gun_b92), MainRegistry.achSelenium);
craftingAchievements.put(new ComparableStack(ModItems.battery_potatos), MainRegistry.achPotato);
craftingAchievements.put(new ComparableStack(ModBlocks.machine_press), MainRegistry.achBurnerPress);
craftingAchievements.put(new ComparableStack(ModItems.rbmk_fuel_empty), MainRegistry.achRBMK);
craftingAchievements.put(new ComparableStack(ModBlocks.machine_chemical_plant), MainRegistry.achChemplant);
craftingAchievements.put(new ComparableStack(ModBlocks.concrete_smooth), MainRegistry.achConcrete);
craftingAchievements.put(new ComparableStack(ModBlocks.concrete_asbestos), MainRegistry.achConcrete);
craftingAchievements.put(new ComparableStack(ModItems.ingot_polymer), MainRegistry.achPolymer);
craftingAchievements.put(new ComparableStack(ModItems.ingot_desh), MainRegistry.achDesh);
craftingAchievements.put(new ComparableStack(ModItems.gem_tantalium), MainRegistry.achTantalum);
craftingAchievements.put(new ComparableStack(ModBlocks.machine_gascent), MainRegistry.achGasCent);
craftingAchievements.put(new ComparableStack(ModBlocks.machine_centrifuge), MainRegistry.achCentrifuge);
craftingAchievements.put(new ComparableStack(ModItems.ingot_schrabidium), MainRegistry.achSchrab);
craftingAchievements.put(new ComparableStack(ModItems.nugget_schrabidium), MainRegistry.achSchrab);
craftingAchievements.put(new ComparableStack(ModBlocks.machine_crystallizer), MainRegistry.achAcidizer);
craftingAchievements.put(new ComparableStack(ModBlocks.machine_silex), MainRegistry.achSILEX);
craftingAchievements.put(new ComparableStack(ModItems.nugget_technetium), MainRegistry.achTechnetium);
craftingAchievements.put(new ComparableStack(ModBlocks.struct_watz_core), MainRegistry.achWatz);
craftingAchievements.put(new ComparableStack(ModItems.nugget_bismuth), MainRegistry.achBismuth);
craftingAchievements.put(new ComparableStack(ModItems.nugget_am241), MainRegistry.achBreeding);
craftingAchievements.put(new ComparableStack(ModItems.nugget_am242), MainRegistry.achBreeding);
craftingAchievements.put(new ComparableStack(ModItems.missile_nuclear), MainRegistry.achRedBalloons);
craftingAchievements.put(new ComparableStack(ModItems.missile_nuclear_cluster), MainRegistry.achRedBalloons);
craftingAchievements.put(new ComparableStack(ModItems.missile_doomsday), MainRegistry.achRedBalloons);
craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_10_nuclear), MainRegistry.achRedBalloons);
craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_10_nuclear_large), MainRegistry.achRedBalloons);
craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_15_nuclear), MainRegistry.achRedBalloons);
craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_15_nuclear_shark), MainRegistry.achRedBalloons);
craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_15_boxcar), MainRegistry.achRedBalloons);
craftingAchievements.put(new ComparableStack(ModBlocks.struct_torus_core), MainRegistry.achFusion);
craftingAchievements.put(new ComparableStack(ModBlocks.machine_difurnace_off), MainRegistry.achBlastFurnace);
craftingAchievements.put(new ComparableStack(ModBlocks.machine_assembly_machine), MainRegistry.achAssembly);
craftingAchievements.put(new ComparableStack(ModItems.billet_pu_mix), MainRegistry.achChicagoPile);
craftingAchievements.put(new ComparableStack(ModItems.particle_digamma), MainRegistry.achOmega12);
```

## All achievement calls and local predicate context

The source files remain authoritative for enclosing method guards; snippets below show each original award and its immediate condition/recipient.

### com/hbm/main/ModEventHandler.java:389

```java
377: 		if(event.entity.getUniqueID().toString().equals(ShadyUtil.HbMinecraft) || event.entity.getCommandSenderName().equals("HbMinecraft")) {
378: 			event.entity.dropItem(ModItems.book_of_, 1);
379: 		}
380:
381: 		if(event.entity.getUniqueID().toString().equals(ShadyUtil.MellowRPG8)) {
382: 			event.entity.entityDropItem(new ItemStack(ModBlocks.block_meteor, 1 + rand.nextInt(10)), 0.0F);
383: 		}
384:
385: 		if(event.entity instanceof EntityCreeperTainted && event.source == ModDamageSource.boxcar) {
386:
387: 			for(Object o : event.entity.worldObj.getEntitiesWithinAABB(EntityPlayer.class, event.entity.boundingBox.expand(50, 50, 50))) {
388: 				EntityPlayer player = (EntityPlayer)o;
389: 				player.triggerAchievement(MainRegistry.bobHidden);
390: 			}
391: 		}
```

### com/hbm/main/ModEventHandler.java:1681

```java
1669: 		if(!e.player.worldObj.isRemote && e.smelting.getItem() == ModItems.ingot_uranium && e.player.getRNG().nextInt(64) == 0) {
1670:
1671: 			if(!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.quartz_plutonium)))
1672: 				e.player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.quartz_plutonium), false);
1673: 			else
1674: 				e.player.inventoryContainer.detectAndSendChanges();
1675: 		}
1676: 	}
1677:
1678: 	@SubscribeEvent
1679: 	public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
1680: 		if(event.pickedUp.getEntityItem().getItem() == Items.slime_ball)
1681: 			event.player.triggerAchievement(MainRegistry.achSlimeball);
1682: 		if(event.pickedUp.getEntityItem().getItem() == ModItems.egg_balefire)
1683: 			event.player.triggerAchievement(MainRegistry.rotConsum);
```

### com/hbm/main/ModEventHandler.java:1683

```java
1671: 			if(!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.quartz_plutonium)))
1672: 				e.player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.quartz_plutonium), false);
1673: 			else
1674: 				e.player.inventoryContainer.detectAndSendChanges();
1675: 		}
1676: 	}
1677:
1678: 	@SubscribeEvent
1679: 	public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
1680: 		if(event.pickedUp.getEntityItem().getItem() == Items.slime_ball)
1681: 			event.player.triggerAchievement(MainRegistry.achSlimeball);
1682: 		if(event.pickedUp.getEntityItem().getItem() == ModItems.egg_balefire)
1683: 			event.player.triggerAchievement(MainRegistry.rotConsum);
1684: 	}
1685:
```

### com/hbm/main/ModEventHandler.java:1695

```java
1683: 			event.player.triggerAchievement(MainRegistry.rotConsum);
1684: 	}
1685:
1686: 	@SubscribeEvent
1687: 	public void onBlockBreak(BreakEvent event) {
1688:
1689: 		EntityPlayer player = event.getPlayer();
1690:
1691: 		if(!(player instanceof EntityPlayerMP))
1692: 			return;
1693:
1694: 		if(event.block == ModBlocks.stone_gneiss && !((EntityPlayerMP) player).func_147099_x().hasAchievementUnlocked(MainRegistry.achStratum)) {
1695: 			event.getPlayer().triggerAchievement(MainRegistry.achStratum);
1696: 			event.setExpToDrop(500);
1697: 		}
```

### com/hbm/handler/EntityEffectHandler.java:268

```java
256:
257: 		/// EFFECTS ///
258: 		if(eRad >= 1000) {
259:
260: 			entity.attackEntityFrom(ModDamageSource.radiation, 1000F);
261: 			HbmLivingProps.setRadiation(entity, 0);
262:
263: 			if(entity.getHealth() > 0) {
264: 				entity.setHealth(0);
265: 				entity.onDeath(ModDamageSource.radiation);
266: 			}
267:
268: 			if(entity instanceof EntityPlayer) ((EntityPlayer) entity).triggerAchievement(MainRegistry.achRadDeath);
269:
270: 		} else if(eRad >= 800) {
```

### com/hbm/handler/EntityEffectHandler.java:292

```java
280: 			if(world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(Potion.weakness.id, 10 * 20, 2));
281: 			if(world.rand.nextInt(500) == 0) entity.addPotionEffect(new PotionEffect(Potion.poison.id, 3 * 20, 1));
282:
283: 		} else if(eRad >= 400) {
284: 			if(world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 5 * 30, 0));
285: 			if(world.rand.nextInt(500) == 0) entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 5 * 20, 0));
286: 			if(world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(Potion.weakness.id, 5 * 20, 1));
287:
288: 		} else if(eRad >= 200) {
289: 			if(world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 5 * 20, 0));
290: 			if(world.rand.nextInt(500) == 0) entity.addPotionEffect(new PotionEffect(Potion.weakness.id, 5 * 20, 0));
291:
292: 			if(entity instanceof EntityPlayer) ((EntityPlayer) entity).triggerAchievement(MainRegistry.achRadPoison);
293: 		}
294: 	}
```

### com/hbm/util/AchievementHandler.java:60

```java
48: 		craftingAchievements.put(new ComparableStack(ModBlocks.struct_torus_core), MainRegistry.achFusion);
49: 		craftingAchievements.put(new ComparableStack(ModBlocks.machine_difurnace_off), MainRegistry.achBlastFurnace);
50: 		craftingAchievements.put(new ComparableStack(ModBlocks.machine_assembly_machine), MainRegistry.achAssembly);
51: 		craftingAchievements.put(new ComparableStack(ModItems.billet_pu_mix), MainRegistry.achChicagoPile);
52: 		craftingAchievements.put(new ComparableStack(ModItems.particle_digamma), MainRegistry.achOmega12);
53: 	}
54:
55: 	public static void fire(EntityPlayer player, ItemStack stack) {
56: 		if(player.worldObj.isRemote) return;
57: 		ComparableStack comp = new ComparableStack(stack).makeSingular();
58: 		Achievement achievement = craftingAchievements.get(comp);
59: 		if(achievement != null) {
60: 			player.triggerAchievement(achievement);
61: 		}
62: 	}
```

### com/hbm/extprop/HbmLivingProps.java:210

```java
198: 			data.setString("type", "sweat");
199: 			data.setInteger("count", 50);
200: 			data.setInteger("block", Block.getIdFromBlock(Blocks.soul_sand));
201: 			data.setInteger("entity", entity.getEntityId());
202: 			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 50));
203: 		}
204:
205: 		if(entity instanceof EntityPlayer) {
206:
207: 			float di = getData(entity).digamma;
208:
209: 			if(di > 0F)
210: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaSee);
211: 			if(di >= 2F)
212: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaFeel);
```

### com/hbm/extprop/HbmLivingProps.java:212

```java
200: 			data.setInteger("block", Block.getIdFromBlock(Blocks.soul_sand));
201: 			data.setInteger("entity", entity.getEntityId());
202: 			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 50));
203: 		}
204:
205: 		if(entity instanceof EntityPlayer) {
206:
207: 			float di = getData(entity).digamma;
208:
209: 			if(di > 0F)
210: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaSee);
211: 			if(di >= 2F)
212: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaFeel);
213: 			if(di >= 10F)
214: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaKnow);
```

### com/hbm/extprop/HbmLivingProps.java:214

```java
202: 			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 50));
203: 		}
204:
205: 		if(entity instanceof EntityPlayer) {
206:
207: 			float di = getData(entity).digamma;
208:
209: 			if(di > 0F)
210: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaSee);
211: 			if(di >= 2F)
212: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaFeel);
213: 			if(di >= 10F)
214: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaKnow);
215: 		}
216: 	}
```

### com/hbm/items/food/ItemEnergy.java:204

```java
192: 			}
193: 			if(this == ModItems.coffee) {
194: 				player.heal(10);
195: 				player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 60 * 20, 2));
196: 				this.setContainerItem(ModItems.cmug_empty);
197: 				this.container = ModItems.cmug_empty;
198: 				System.out.println(this.container);
199: 			}
200: 			if(this == ModItems.coffee_radium) {
201: 				player.heal(10);
202: 				player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 60 * 20, 2));
203: 				HbmLivingProps.incrementRadiation(player, 500F);
204: 				player.triggerAchievement(MainRegistry.achRadium);
205: 			}
206:
```

### com/hbm/items/armor/ItemModKnife.java:81

```java
69:
70: 				if(entity instanceof EntityPlayerMP) {
71: 					NBTTagCompound data = new NBTTagCompound();
72: 					data.setString("type", "properJolt");
73:
74: 					if(entity.getMaxHealth() > 2F) {
75: 						data.setInteger("time", 10000 + entity.getRNG().nextInt(10000));
76: 						data.setInteger("maxTime", 10000);
77: 					} else {
78: 						data.setInteger("time", 0);
79: 						data.setInteger("maxTime", 0);
80:
81: 						((EntityPlayer)entity).triggerAchievement(MainRegistry.achSomeWounds);
82: 					}
83: 					PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(data, 0, 0, 0), (EntityPlayerMP)entity);
```

### com/hbm/items/tool/WeaponSpecial.java:238

```java
226: 		}
227: 		if(this == ModItems.wrench_flipped) {
228: 			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", -0.1, 1));
229: 		}
230: 		return multimap;
231: 	}
232:
233: 	@Override
234: 	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean b) {
235:
236: 		if(entity instanceof EntityPlayer) {
237: 			if(ArmorUtil.checkForFiend((EntityPlayer) entity)) {
238: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.achFiend);
239: 			} else if(ArmorUtil.checkForFiend2((EntityPlayer) entity)) {
240: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.achFiend2);
```

### com/hbm/items/tool/WeaponSpecial.java:240

```java
228: 			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", -0.1, 1));
229: 		}
230: 		return multimap;
231: 	}
232:
233: 	@Override
234: 	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean b) {
235:
236: 		if(entity instanceof EntityPlayer) {
237: 			if(ArmorUtil.checkForFiend((EntityPlayer) entity)) {
238: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.achFiend);
239: 			} else if(ArmorUtil.checkForFiend2((EntityPlayer) entity)) {
240: 				((EntityPlayer) entity).triggerAchievement(MainRegistry.achFiend2);
241: 			}
242: 		}
```

### com/hbm/items/tool/ItemBoltgun.java:65

```java
53: 			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
54: 				ItemStack slot = player.inventory.getStackInSlot(i);
55:
56: 				if(slot != null) {
57: 					if(slot.getItem() == bolt.getItem() && slot.getItemDamage() == bolt.getItemDamage()) {
58: 						if(!world.isRemote) {
59: 							world.playSoundAtEntity(entity, "hbm:item.boltgun", 1.0F, 1.0F);
60: 							player.inventory.decrStackSize(i, 1);
61: 							player.inventoryContainer.detectAndSendChanges();
62: 							EntityDamageUtil.attackEntityFromIgnoreIFrame(entity, DamageSource.causePlayerDamage(player).setDamageBypassesArmor(), 10F);
63:
64: 							if(!entity.isEntityAlive() && entity instanceof EntityPlayer) {
65: 								((EntityPlayer) entity).triggerAchievement(MainRegistry.achGoFish);
66: 							}
67:
```

### com/hbm/items/special/ItemCigarette.java:55

```java
43: 	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
44: 		stack.stackSize--;
45:
46: 		if(!world.isRemote) {
47:
48: 			if(this == ModItems.cigarette) {
49: 				HbmLivingProps.incrementBlackLung(player, 2000);
50: 				HbmLivingProps.incrementAsbestos(player, 2000);
51: 				HbmLivingProps.incrementRadiation(player, 100F);
52:
53: 				ItemStack helmet = player.getEquipmentInSlot(4);
54: 				if(helmet != null && helmet.getItem() == ModItems.no9) {
55: 					player.triggerAchievement(MainRegistry.achNo9);
56: 				}
57: 			}
```

### com/hbm/entity/missile/EntityRideableRocket.java:209

```java
197: 						targetWorld = DimensionManager.getWorld(targetDimensionId);
198: 					}
199: 					if(targetWorld != null) {
200: 						Satellite.orbit(targetWorld, Satellite.getIDFromItem(rocket.capsule.part), satFreq, posX, posY, posZ);
201: 					}
202: 				} else if(rocket.capsule.part == ModItems.rp_station_core_20) {
203: 					// We mark the station as travellable, but we don't actually add the station until the player travels to it
204: 					OrbitalStation.addStation(x, z, CelestialBody.getBody(worldObj));
205:
206: 					if(thrower != null && thrower instanceof EntityPlayer) {
207: 						EntityPlayer player = (EntityPlayer) thrower;
208: 						if(!player.capabilities.isCreativeMode && !ItemVOTVdrive.wasCopied(navDrive)) {
209: 							player.triggerAchievement(MainRegistry.achDriveFail);
210: 						}
211: 					}
```

### com/hbm/entity/missile/EntitySoyuz.java:64

```java
52: 			List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(posX - 5, posY - 15, posZ - 5, posX + 5, posY, posZ + 5));
53:
54: 			for(Entity e : list) {
55: 				e.setFire(15);
56: 				e.attackEntityFrom(ModDamageSource.exhaust, 100.0F);
57:
58: 				if(e instanceof EntityPlayer) {
59: 					if(!memed) {
60: 						memed = true;
61: 						worldObj.playSoundEffect(posX, posY, posZ, "hbm:alarm.soyuzed", 100, 1.0F);
62: 					}
63:
64: 					((EntityPlayer)e).triggerAchievement(MainRegistry.achSoyuz);
65: 				}
66: 			}
```

### com/hbm/entity/missile/EntitySoyuz.java:107

```java
95:
96: 	private void deployPayload() {
97:
98: 		if(mode == 0 && payload != null) {
99:
100: 			if(payload[0] != null) {
101:
102: 				ItemStack load = payload[0];
103:
104: 				if(load.getItem() == ModItems.flame_pony) {
105: 					ExplosionLarge.spawnTracers(worldObj, posX, posY, posZ, 25);
106: 					for(Object p : worldObj.playerEntities)
107: 						((EntityPlayer)p).triggerAchievement(MainRegistry.achSpace);
108: 				}
109:
```

### com/hbm/entity/missile/EntitySoyuz.java:112

```java
100: 			if(payload[0] != null) {
101:
102: 				ItemStack load = payload[0];
103:
104: 				if(load.getItem() == ModItems.flame_pony) {
105: 					ExplosionLarge.spawnTracers(worldObj, posX, posY, posZ, 25);
106: 					for(Object p : worldObj.playerEntities)
107: 						((EntityPlayer)p).triggerAchievement(MainRegistry.achSpace);
108: 				}
109:
110: 				if(load.getItem() == ModItems.sat_foeq) {
111: 					for(Object p : worldObj.playerEntities)
112: 						((EntityPlayer)p).triggerAchievement(MainRegistry.achFOEQ);
113: 				}
114:
```

### com/hbm/entity/effect/EntitySpear.java:72

```java
60: 				ExAttrib at = Vec3.createVectorHelper(ix - posX, 0, iz - posZ).lengthVector() < 20 ? ExAttrib.DIGAMMA_CIRCUIT : ExAttrib.DIGAMMA;
61:
62: 				new ExplosionNT(worldObj, this, ix, iy, iz, 7.5F)
63: 				.addAttrib(ExAttrib.NOHURT)
64: 				.addAttrib(ExAttrib.NOPARTICLE)
65: 				.addAttrib(ExAttrib.NODROP)
66: 				.addAttrib(ExAttrib.NOSOUND)
67: 				.addAttrib(at).explode();
68:
69: 				for(Object obj : worldObj.playerEntities) {
70: 					EntityPlayer player = (EntityPlayer) obj;
71: 					ContaminationUtil.contaminate(player, HazardType.DIGAMMA, ContaminationType.DIGAMMA, 0.05F);
72: 					player.triggerAchievement(MainRegistry.digammaKauaiMoho);
73: 				}
74: 			}
```

### com/hbm/entity/logic/EntityNukeExplosionMK5.java:63

```java
51: 	@Override
52: 	public void onUpdate() {
53:
54: 		if(strength == 0) {
55: 			this.clearChunkLoader();
56: 			this.setDead();
57: 			return;
58: 		}
59:
60: 		if(!worldObj.isRemote) loadChunk((int) Math.floor(posX / 16D), (int) Math.floor(posZ / 16D));
61:
62: 		for(Object player : this.worldObj.playerEntities) {
63: 			((EntityPlayer)player).triggerAchievement(MainRegistry.achManhattan);
64: 		}
65:
```

### com/hbm/entity/logic/EntityNukeExplosionMK3.java:129

```java
117: 		super(p_i1582_1_);
118: 	}
119:
120:     @Override
121: 	public void onUpdate() {
122:         super.onUpdate();
123:
124: 		if(!worldObj.isRemote) loadChunk((int) Math.floor(posX / 16D), (int) Math.floor(posZ / 16D));
125:
126:         if(!this.did)
127:         {
128:         	for(Object player : this.worldObj.playerEntities)
129:     			((EntityPlayer)player).triggerAchievement(MainRegistry.achManhattan);
130:
131:     		if(GeneralConfig.enableExtendedLogging && !worldObj.isRemote)
```

### com/hbm/entity/mob/EntityMaskMan.java:102

```java
90: 			if(!worldObj.isRemote)
91: 				worldObj.createExplosion(this, posX, posY + 4, posZ, 2.5F, true);
92: 		}
93: 	}
94:
95: 	@Override
96: 	public void onDeath(DamageSource p_70645_1_) {
97: 		super.onDeath(p_70645_1_);
98:
99: 		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(50, 50, 50));
100:
101: 		for(EntityPlayer player : players) {
102: 			player.triggerAchievement(MainRegistry.bossMaskman);
103: 		}
104: 	}
```

### com/hbm/entity/mob/EntityUFO.java:309

```java
297:
298: 		if(this.deathTime == -10) {
299: 			worldObj.playSoundAtEntity(this, "hbm:entity.chopperDamage", 10.0F, 1.0F);
300: 		}
301:
302: 		if(this.deathTime == 19 && !worldObj.isRemote) {
303: 			worldObj.newExplosion(this, posX, posY, posZ, 10F, true, true);
304: 			ExplosionNukeSmall.explode(worldObj, posX, posY, posZ, ExplosionNukeSmall.PARAMS_MEDIUM);
305:
306: 			List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(200, 200, 200));
307:
308: 			for(EntityPlayer player : players) {
309: 				player.triggerAchievement(MainRegistry.bossUFO);
310: 				player.inventory.addItemStackToInventory(new ItemStack(ModItems.coin_ufo));
311: 			}
```

### com/hbm/entity/mob/EntityCreeperNuclear.java:81

```java
69:
70: 		if(rand.nextInt(3) == 0)
71: 			this.dropItem(ModItems.coin_creeper, 1);
72: 	}
73:
74: 	@Override
75: 	public void onDeath(DamageSource p_70645_1_) {
76: 		super.onDeath(p_70645_1_);
77:
78: 		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(50, 50, 50));
79:
80: 		for(EntityPlayer player : players) {
81: 			player.triggerAchievement(MainRegistry.bossCreeper);
82: 		}
83:
```

### com/hbm/entity/mob/EntityRADBeast.java:178

```java
166: 	protected Item getDropItem() {
167: 		return ModItems.rod_zirnox_uranium_fuel_depleted;
168: 	}
169:
170: 	@Override
171: 	public void onDeath(DamageSource p_70645_1_) {
172: 		super.onDeath(p_70645_1_);
173:
174: 		if(this.getMaxHealth() > 150) {
175: 			List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(50, 50, 50));
176:
177: 			for(EntityPlayer player : players) {
178: 				player.triggerAchievement(MainRegistry.bossMeltdown);
179: 			}
180: 		}
```

### com/hbm/entity/mob/botprime/EntityBOTPrimeHead.java:143

```java
131: 		float f3 = MathHelper.sqrt_double(dx * dx + dz * dz);
132: 		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(dx, dz) * 180.0D / Math.PI);
133: 		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(dy, f3) * 180.0D / Math.PI);
134: 	}
135:
136:     @Override
137: 	public void onDeath(DamageSource p_70645_1_) {
138: 		super.onDeath(p_70645_1_);
139:
140: 		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(200, 200, 200));
141:
142: 		for(EntityPlayer player : players) {
143: 			player.triggerAchievement(MainRegistry.bossWorm);
144: 			player.inventory.addItemStackToInventory(new ItemStack(ModItems.coin_worm));
145: 		}
```

### com/hbm/saveddata/satellites/SatelliteHorizons.java:26

```java
14:
15: public class SatelliteHorizons extends Satellite {
16:
17: 	boolean used = false;
18:
19: 	public SatelliteHorizons() {
20: 		this.satIface = Interfaces.SAT_COORD;
21: 	}
22:
23: 	public void onOrbit(World world, double x, double y, double z) {
24:
25: 		for(Object p : world.playerEntities)
26: 			((EntityPlayer)p).triggerAchievement(MainRegistry.horizonsStart);
27: 	}
28:
```

### com/hbm/saveddata/satellites/SatelliteHorizons.java:54

```java
42: 		used = true;
43: 		SatelliteSavedData.getData(world, x, z).markDirty();
44:
45:
46: 		EntityTom tom = new EntityTom(world);
47: 		tom.setPosition(x + 0.5, 600, z + 0.5);
48:
49: 		IChunkProvider provider = world.getChunkProvider();
50: 		provider.loadChunk(x >> 4, z >> 4);
51:
52: 		world.spawnEntityInWorld(tom);
53: 		for(Object p : world.playerEntities)
54: 			((EntityPlayer)p).triggerAchievement(MainRegistry.horizonsEnd);
55:
56: 		//not necessary but JUST to make sure
```

### com/hbm/saveddata/satellites/SatelliteRelay.java:17

```java
5: import net.minecraft.entity.player.EntityPlayer;
6: import net.minecraft.world.World;
7:
8: public class SatelliteRelay extends Satellite {
9:
10: 	public SatelliteRelay() {
11: 		this.satIface = Interfaces.NONE;
12: 	}
13:
14: 	public void onOrbit(World world, double x, double y, double z) {
15:
16: 		for(Object p : world.playerEntities)
17: 			((EntityPlayer)p).triggerAchievement(MainRegistry.achFOEQ);
18: 	}
19:
```

### com/hbm/tileentity/machine/TileEntityReactorZirnox.java:391

```java
379: 		int[] dimensions = {1, 0, 2, 2, 2, 2,};
380: 		worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, ModBlocks.zirnox_destroyed, this.getBlockMetadata(), 3);
381: 		MultiblockHandlerXR.fillSpace(worldObj, this.xCoord, this.yCoord, this.zCoord, dimensions, ModBlocks.zirnox_destroyed, ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset));
382: 		worldObj.playSoundEffect(xCoord, yCoord + 2, zCoord, "hbm:block.rbmk_explosion", 10.0F, 1.0F);
383: 		worldObj.createExplosion(null, this.xCoord, this.yCoord + 3, this.zCoord, 12.0F, true);
384: 		zirnoxDebris();
385: 		ExplosionNukeGeneric.waste(worldObj, this.xCoord, this.yCoord, this.zCoord, 35);
386:
387: 		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class,
388: 				AxisAlignedBB.getBoundingBox(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5).expand(100, 100, 100));
389:
390: 		for(EntityPlayer player : players) {
391: 			player.triggerAchievement(MainRegistry.achZIRNOXBoom);
392: 		}
393:
```

### com/hbm/tileentity/machine/TileEntityWatz.java:525

```java
513: 			setBrokenColumn(1, ModBlocks.watz_end, 1, j, 3);
514: 			setBrokenColumn(1, ModBlocks.watz_end, 1, -3, j);
515: 			setBrokenColumn(1, ModBlocks.watz_end, 1, j, -3);
516: 		}
517: 		setBrokenColumn(1, ModBlocks.watz_end, 1, 2, 2);
518: 		setBrokenColumn(1, ModBlocks.watz_end, 1, 2, -2);
519: 		setBrokenColumn(1, ModBlocks.watz_end, 1, -2, 2);
520: 		setBrokenColumn(1, ModBlocks.watz_end, 1, -2, -2);
521:
522: 		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5).expand(50, 50, 50));
523:
524: 		for(EntityPlayer player : players) {
525: 			player.triggerAchievement(MainRegistry.achWatzBoom);
526: 		}
527: 	}
```

### com/hbm/tileentity/machine/rbmk/TileEntityRBMKBase.java:556

```java
544: 		NBTTagCompound data = new NBTTagCompound();
545: 		data.setString("type", "rbmkmush");
546: 		data.setFloat("scale", smallDim);
547: 		PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, avgX + 0.5, yCoord + 1, avgZ + 0.5), new TargetPoint(worldObj.provider.dimensionId,avgX + 0.5, yCoord + 1, avgZ + 0.5, 250));
548: 		MainRegistry.proxy.effectNT(data);
549:
550: 		worldObj.playSoundEffect(avgX + 0.5, yCoord + 1, avgZ + 0.5, "hbm:block.rbmk_explosion", 50.0F, 1.0F);
551:
552: 		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class,
553: 				AxisAlignedBB.getBoundingBox(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5).expand(50, 50, 50));
554:
555: 		for(EntityPlayer player : players) {
556: 			player.triggerAchievement(MainRegistry.achRBMKBoom);
557: 		}
558:
```

### com/hbm/tileentity/machine/albion/TileEntityPADetector.java:159

```java
147:
148: 				if(recipe.output2 != null) {
149: 					if(slots[4] == null) {
150: 						slots[4] = recipe.output2.copy();
151: 					} else {
152: 						slots[4].stackSize += recipe.output2.stackSize;
153: 					}
154: 				}
155: 			}
156:
157: 			if((recipe.output1 != null && recipe.output1.getItem() == ModItems.particle_digamma) || (recipe.output2 != null && recipe.output2.getItem() == ModItems.particle_digamma)) {
158: 				List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5).expand(100, 50, 100));
159: 				for(EntityPlayer player : players) player.triggerAchievement(MainRegistry.achOmega12);
160: 			}
161:
```

### com/hbm/blocks/generic/BlockRedBrickKeyhole.java:91

```java
79: 			if((player.getHeldItem().getItem() == ModItems.key_red || cracked) && side != 0 && side != 1) {
80: 				if(cracked) player.getHeldItem().stackSize--;
81: 				if(world.isRemote) return true;
82: 				ForgeDirection dir = ForgeDirection.getOrientation(side);
83: 				generateRoom(world, x - dir.offsetX * 4, y - 2, z - dir.offsetZ * 4, dir);
84: 				int b = 0;
85: 				if(side == 2) b = 1;
86: 				if(side == 5) b = 2;
87: 				if(side == 3) b = 3;
88: 				if(side == 4) b = 0;
89: 				ItemModDoor.placeDoorBlock(world, x, y - 1, z, b, ModBlocks.door_red);
90: 				world.playSoundAtEntity(player, "hbm:block.lockOpen", 1.0F, 1.0F);
91: 				player.triggerAchievement(MainRegistry.achRedRoom);
92: 				return true;
93: 			}
```

### com/hbm/blocks/generic/BlockKeyhole.java:69

```java
57: 			if((player.getHeldItem().getItem() == ModItems.key_red || cracked) && side != 0 && side != 1) {
58: 				if(cracked) player.getHeldItem().stackSize--;
59: 				if(world.isRemote) return true;
60: 				ForgeDirection dir = ForgeDirection.getOrientation(side);
61: 				generateRoom(world, x - dir.offsetX * 4, y - 2, z - dir.offsetZ * 4);
62: 				int b = 0;
63: 				if(side == 2) b = 1;
64: 				if(side == 5) b = 2;
65: 				if(side == 3) b = 3;
66: 				if(side == 4) b = 0;
67: 				ItemModDoor.placeDoorBlock(world, x, y - 1, z, b, ModBlocks.door_red);
68: 				world.playSoundAtEntity(player, "hbm:block.lockOpen", 1.0F, 1.0F);
69: 				player.triggerAchievement(MainRegistry.achRedRoom);
70: 				return true;
71: 			}
```

### com/hbm/blocks/fluid/GenericFluidBlock.java:84

```java
72:
73: 					entity.motionX = 0;
74: 					entity.motionY = 0;
75: 					entity.motionZ = 0;
76:
77: 					if(entity.ticksExisted % 20 == 0 && !world.isRemote) {
78: 						entity.attackEntityFrom(damageSource, damage * 0.1F);
79:
80: 						if(this == ModBlocks.sulfuric_acid_block && entity.isDead && ((EntityItem)entity).getEntityItem().getItem() == Items.slime_ball) {
81: 							List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, entity.boundingBox.expand(10, 10, 10));
82:
83: 							for(EntityPlayer player : players)
84: 								player.triggerAchievement(MainRegistry.achSulfuric);
85: 						}
86: 					}
```

### com/hbm/blocks/machine/MachineFluidTank.java:157

```java
145: 		if(tank.lastExplosion == explosion) return;
146: 		tank.lastExplosion = explosion;
147:
148: 		if(!tank.hasExploded) {
149: 			tank.explode();
150:
151: 			if(explosion.exploder != null && explosion.exploder instanceof EntityBombletZeta) {
152: 				if(tank.tank.getTankType().getTrait(FT_Flammable.class) == null) return;
153:
154: 				List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
155: 						AxisAlignedBB.getBoundingBox(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).expand(100, 100, 100));
156:
157: 				for(EntityPlayer p : players) p.triggerAchievement(MainRegistry.achInferno);
158: 			}
159: 		} else {
```

### com/hbm/blocks/machine/MachineRefinery.java:129

```java
117:
118: 		TileEntityMachineRefinery refinery = (TileEntityMachineRefinery) core;
119: 		if(refinery.lastExplosion == explosion) return;
120: 		refinery.lastExplosion = explosion;
121:
122: 		if(!refinery.hasExploded) {
123: 			refinery.explode(world, x, y, z);
124:
125: 			if(explosion.exploder != null && explosion.exploder instanceof EntityBombletZeta) {
126: 				List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
127: 						AxisAlignedBB.getBoundingBox(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).expand(100, 100, 100));
128:
129: 				for(EntityPlayer p : players) p.triggerAchievement(MainRegistry.achInferno);
130: 			}
131: 		} else {
```

## All completion readers, vanilla awards and crafting dispatch entry points

- com/hbm/main/ModEventHandler.java:1654: AchievementHandler.fire(e.player, e.crafting);
- com/hbm/main/ModEventHandler.java:1659: AchievementHandler.fire(e.player, e.smelting);
- com/hbm/main/ModEventHandler.java:1694: if(event.block == ModBlocks.stone_gneiss && !((EntityPlayerMP) player).func_147099_x().hasAchievementUnlocked(MainRegistry.achStratum)) {
- com/hbm/handler/BossSpawnHandler.java:68: boolean acidizerStat = !GeneralConfig.enableStatReRegistering || (statCraft != null && playerMP.func_147099_x().writeStat(statCraft) > 0)|| (statPlace != null && playerMP.func_147099_x().writeStat(statPlace) > 0);
- com/hbm/inventory/SlotSmelting.java:82: this.thePlayer.addStat(AchievementList.acquireIron, 1);
- com/hbm/inventory/SlotSmelting.java:86: this.thePlayer.addStat(AchievementList.cookFish, 1);
- com/hbm/inventory/SlotCraftingOutput.java:26: public static void checkAchievements(EntityPlayer player, ItemStack stack) {
- com/hbm/inventory/SlotCraftingOutput.java:27: AchievementHandler.fire(player, stack);
- com/hbm/inventory/SlotCraftingOutput.java:53: checkAchievements(this.player, stack);
- com/hbm/packet/toserver/AnvilCraftPacket.java:64: AchievementHandler.fire(p, recipe.output.get(0).stack);
- com/hbm/packet/toserver/ItemBobmazonPacket.java:73: if(req != null && p.func_147099_x().hasAchievementUnlocked(req) || p.capabilities.isCreativeMode) {
- com/hbm/inventory/container/ContainerMachineChemicalPlant.java:52: SlotCraftingOutput.checkAchievements(player, slotStack);
- com/hbm/inventory/container/ContainerMachineChemicalFactory.java:49: SlotCraftingOutput.checkAchievements(player, slotStack);
- com/hbm/inventory/container/ContainerMachinePUREX.java:46: SlotCraftingOutput.checkAchievements(player, slotStack);
- com/hbm/inventory/container/ContainerMachineAssemblyFactory.java:49: SlotCraftingOutput.checkAchievements(player, slotStack);
- com/hbm/inventory/container/ContainerCrystallizer.java:58: SlotCraftingOutput.checkAchievements(player, stack);
- com/hbm/inventory/container/ContainerMachineAssemblyMachine.java:45: SlotCraftingOutput.checkAchievements(player, slotStack);
- com/hbm/inventory/container/ContainerMachinePrecAss.java:45: SlotCraftingOutput.checkAchievements(player, slotStack);
- com/hbm/inventory/container/ContainerMachineAnnihilator.java:45: SlotCraftingOutput.checkAchievements(player, slotStack);
- com/hbm/inventory/gui/GUIScreenBobmazon.java:273: gui.itemRender.renderItemAndEffectIntoGUI(gui.fontRendererObj, gui.mc.getTextureManager(), requirement.achievement.theItemStack, x + 1, y + 1);
- com/hbm/inventory/gui/GUIScreenBobmazon.java:284: NONE(AchievementList.openInventory),
- com/hbm/inventory/gui/GUIScreenBobmazon.java:298: return player.func_147099_x().hasAchievementUnlocked(achievement);

## Explicit dependency edge list

- achSlimeball -> achSulfuric
- horizonsStart -> horizonsEnd
- horizonsEnd -> horizonsBonus
- bossCreeper -> bossMeltdown
- bossCreeper -> bossMaskman
- bossMaskman -> bossWorm
- bossWorm -> bossUFO
- achRadPoison -> achRadDeath
- digammaSee -> digammaFeel
- digammaFeel -> digammaKnow
- digammaKnow -> digammaKauaiMoho
- digammaKauaiMoho -> digammaUpOnTop
- achBurnerPress -> achBlastFurnace
- achBurnerPress -> achAssembly
- achBurnerPress -> achSelenium
- achAssembly -> achChemplant
- achChemplant -> achConcrete
- achChemplant -> achPolymer
- achChemplant -> achDesh
- achChemplant -> achTantalum
- achDesh -> achGasCent
- achPolymer -> achCentrifuge
- achPolymer -> achDriveFail
- achDesh -> achFOEQ
- achDesh -> achSoyuz
- achDesh -> achSpace
- achDesh -> achSchrab
- achDesh -> achAcidizer
- achCentrifuge -> achRadium
- achCentrifuge -> achTechnetium
- achCentrifuge -> achZIRNOXBoom
- achCentrifuge -> achChicagoPile
- achAcidizer -> achSILEX
- achSchrab -> achWatz
- achWatz -> achWatzBoom
- achConcrete -> achRBMK
- achRBMK -> achRBMKBoom
- achRBMK -> achBismuth
- achRBMK -> achBreeding
- achBismuth -> achFusion
- achPolymer -> achRedBalloons
- achPolymer -> achManhattan

Crafting map distinct award targets: 24. Both pristine and current port have identical 40 triggerAchievement call expressions across 30 files at assessment time.
