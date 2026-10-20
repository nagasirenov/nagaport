# Verified legacy achievement gates and native migration constraints

Evidence-only follow-up to `native-achievement-root-assessment.md`, verified 2026-10-19. No production implementation, builds, downloads, installs, or save migration were performed for this verification. The UI grouping decision remains parked. Original-world/player-save migration is not authorized or assumed in scope.

## Findings that resolve previous uncertainty

- Legacy achievement parents are gameplay gates, including for declarations using `initIndependentStat()`. A child event before its direct parent is unlocked is discarded. There is no queue, replay, or later inventory-based backfill.
- `rotConsum` is null in NTM, but passing it to the original player achievement trigger is a no-op, not a crash. Preserve the no-op; do not create a new award or predicate.
- Genuine Forge 1614 `FakePlayer.addStat` is empty. Standard Forge fake players consequently never earn these achievements. Forge 2768's native `PlayerAdvancements.grantCriterion` rejection of `FakePlayer` preserves that behavior. Do not attribute this guarantee to arbitrary third-party fake-player subclasses that override the method differently.
- The vanished vanilla dependencies require seven native legacy definitions, not just iron/fish replacements: `openInventory`, `mineWood`, `buildWorkBench`, `buildPickaxe`, `buildFurnace`, `acquireIron`, `cookFish`.
- Original achievement storage accumulates integers; completion is only `value > 0`. Native one-criterion advancement completion alone does not preserve repeated integer totals. NTM's own achievement readers use boolean completion, not those integer totals.

## Reproducible evidence and hashes

Disassembled with `/workspace/shared/nagaport-toolchain/java8/bin/javap -c -p -classpath <jar> <class>`. Names below were matched against the actual Forge userdev `conf/packaged.srg`, not guessed from decompiler names.

SHA-256:

- `/tmp/nagaport-legacy-client.jar`, official 1.7.10 client: `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
- `/tmp/nagaport-forge1614-userdev.jar`: `6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91`.
- Userdev entry `conf/packaged.srg`: `0ea622782729246038c1e7ad92002a4dcdebc8d8839000e2a8c8e2c9b2b6cc4a`.
- Userdev entry `src/main/java/net/minecraftforge/common/util/FakePlayer.java`: `6dc2c8bbb1ef7be0372340829ffd8272d80aabe6f2248fa0fb6368d1383a9c6d`.
- Userdev entry `forgepatches.zip`: `5cb7f1cd1c4faf0c435548c610c3322c15ae3524cbb4926f272ec057dce83bd4`.
- Userdev entry `fmlpatches.zip`: `cb49b85f38816b364a22d6e8cf4a66b5b612aeb8f50c0af65f05b76eddc8c5a4`.
- Native oracle `.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`: `8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e`.

Key mappings:

| Obfuscated symbol | Mapped class/method |
| --- | --- |
| `yz`, `mw`, `bjk` | `EntityPlayer`, `EntityPlayerMP`, `EntityClientPlayerMP` |
| `yz.a(ph)` | `EntityPlayer.func_71029_a(StatBase)`, triggerAchievement |
| `yz.a(ph,int)`, `mw.a(ph,int)`, `bjk.a(ph,int)` | `func_71064_a(StatBase,int)`, addStat |
| `pq`, `pg`, `ph`, `pb` | `StatFileWriter`, `StatisticsFile`, `StatBase`, `Achievement` |
| `pq.b(yz,ph,int)` | `func_150871_b`, incrementStat |
| `pq.a(pb)` | `func_77443_a`, hasAchievementUnlocked |
| `pq.b(pb)` | `func_77442_b`, canUnlockAchievement |
| `pq.a(ph)` | `func_77444_a`, legacy writeStat/read integer |
| `pq.a(yz,ph,int)`, `pg.a(yz,ph,int)` | `func_150873_a`, set/unlock value |
| `pb.a()` | `func_75966_h`, initIndependentStat |
| `pc`, `xk`, `aax`, `aak` | `AchievementList`, `EntityItem`, `SlotCrafting`, `SlotFurnace` |
| `bao`, `nh`, `is`, `it` | `Minecraft`, `NetHandlerPlayServer`, `C16PacketClientStatus`, its `EnumState` |
| `adn` | `ItemPickaxe` |

## Engine behavior

`pq.b(yz,ph,int)` bytecode offsets 0–18 test `StatBase.isAchievement()` and return if `canUnlockAchievement` is false. Offsets 19–29 read the current integer, add the supplied increment, and write it. `pq.b(pb)` tests only whether the direct parent is null or has value greater than zero. It does not traverse the entire ancestry on every award. The natural event sequence enforces ancestors, but a command/external grant may create a completed direct parent without all ancestors; do not introduce a stricter recursive gate.

`pb.a()` merely sets the independent flag. It does not remove `parentAchievement`, change `isAchievement`, or bypass the above test.

`pg.a(yz,ph,int)` updates stored integer values, marks the stat dirty, and broadcasts achievement chat only on an old value of zero becoming positive, when the server's announcement option permits it. Repeated qualified triggers increase the integer but do not reannounce completion. `hasAchievementUnlocked` is `read integer > 0`.

`yz.a(ph)` delegates to `addStat(stat,1)`. Base `EntityPlayer.addStat` does nothing. Server `mw.a(ph,int)` and client `bjk.a(ph,int)` both return immediately for null. Genuine Forge `FakePlayer` overrides `func_71064_a` with an empty body. The `rotConsum` pickup call therefore has no award effect on the original standard player classes, including fake players.

The Forge patch to `EntityPlayerMP.addStat` posts cancellable `AchievementEvent` after the null guard and before incrementing. No `AchievementEvent` handler is present anywhere in pristine NTM Java source. Native 2768 `AdvancementEvent` is delivered after first completion and is not a cancellable equivalent. Do not invent a compatibility shim or promise preservation of external 1.7 add-ons' event interception. The old MP method also updates matching scoreboard objectives separately after the stat-writer call, even if the writer rejected a parent-gated increment; external legacy scoreboard behavior is not reproduced merely by adding native counters.

## Exact vanished vanilla chain and predicates

Definitions and their direct gameplay parents, from `pc` static initialization:

| Legacy name | Obfuscated / SRG field | Gameplay parent | Original qualifying event |
| --- | --- | --- | --- |
| openInventory | `pc.f` / `field_76004_f` | none | Explicit inventory-key action sends `C16PacketClientStatus(OPEN_INVENTORY_ACHIEVEMENT)`; server receipt triggers it. |
| mineWood | `pc.g` / `field_76005_g` | openInventory | Successful server EntityItem pickup of the item for vanilla `Blocks.log` or `Blocks.log2`; all metadata. |
| buildWorkBench | `pc.h` / `field_76017_h` | mineWood | `SlotCrafting` takes a crafting-table output item. |
| buildPickaxe | `pc.i` / `field_76018_i` | buildWorkBench | `SlotCrafting` takes output whose item is `instanceof ItemPickaxe`, including non-wooden pickaxes and subclasses. |
| buildFurnace | `pc.j` / `field_76015_j` | buildPickaxe | `SlotCrafting` takes the vanilla furnace output item. |
| acquireIron | `pc.k` / `field_76016_k` | buildFurnace | `SlotFurnace` takes output whose item is iron ingot, or the existing NTM `SlotSmelting` equivalent call. |
| cookFish | `pc.p` / `field_76026_p` | buildFurnace | `SlotFurnace` takes output whose item is cooked fish, or the existing NTM `SlotSmelting` equivalent call; all cooked-fish metadata. |

`xk.b_(yz)` awards wood after pickup eligibility/inventory insertion succeeds, not on breaking a log or merely possessing one. Forge's pre-pickup cancellation/ALLOW handling remains upstream. The FML post-pickup event is emitted after these vanilla achievement attempts. The native post-pickup event exposes `getStack()`; use this captured picked-up stack, not the entity's potentially emptied stack.

`aax.b(add)` compares crafting output item identities for workbench/furnace and class identity for pickaxe. The FML `ItemCraftedEvent` is fired just before this method by `SlotCrafting.onPickupFromSlot`. There is no possession criterion and no item-metadata restriction for these three awards. A native listener must retain actual crafting provenance; blindly adding these awards to NTM's general `AchievementHandler.fire` would also award them on NTM smelting/machine dispatch, which was not the old predicate.

`aak.b(add)` emits iron/fish attempts after XP processing and the FML smelted event. NTM `com/hbm/inventory/SlotSmelting.java:79–86` has the same ordering. Preserve both ordinary furnace and NTM smelting outputs without double-counting when a native event listener and an explicit NTM call see the same operation. Generic inventory possession, crafting iron, furnace completion without player extraction, hopper extraction, and eating fish are not these award predicates.

In `bao`, the inventory key's normal branch sends the packet before attempting to display `GuiInventory`; the riding-horse branch only requests the horse inventory and does not send this award packet. Creative mode is not excluded: `GuiInventory` subsequently redirects to the creative inventory. Opening a chest/machine or the horse inventory must not qualify. In 1.12.2 `CPacketClientStatus.State` lacks `OPEN_INVENTORY_ACHIEVEMENT`, so a narrow mod C2S signal is required. Send no arbitrary award ID or player ID: the authenticated packet sender is the award recipient. Schedule server work on its main thread. A generic `GuiOpenEvent(GuiInventory)` is convenient but not exact for programmatic GUI opens/canceled opens; the old trigger was the inventory action, not successful GUI initialization. Record and test any intentionally chosen approximation rather than claiming byte-for-byte parity.

A classfile constant-pool scan of the official client found the relevant award fields only in their declaration, the above award sites, and inventory achievement presentation/stat-sync references. It found no other normal vanilla iron/fish/wood/crafting trigger sites.

## NTM readers and native representation

Pristine-source searches found exactly these achievement-completion readers:

- `com/hbm/main/ModEventHandler.java:1694`: gneiss first-break XP gate checks `hasAchievementUnlocked(achStratum)`.
- `com/hbm/packet/toserver/ItemBobmazonPacket.java:73`: required achievement completion or creative bypass.
- `com/hbm/inventory/gui/GUIScreenBobmazon.java:298`: `Requirement.isAvailable` checks boolean achievement completion.

No NTM achievement integer consumer was found. `BossSpawnHandler.java:68` reads positive ordinary crafting/placing statistics for the crystallizer; those are not achievement counters. Keep this separate gate separate. The three declared ordinary mod stats also remain genuine native statistics.

Proposed native representation:

1. Native advancement definitions for the existing 63 active NTM awards plus the seven required legacy vanilla nodes, each with an impossible criterion and no XP/item/loot reward. Keep exact old-ID to lowercase ResourceLocation mappings.
2. A mod-owned server award function checks null/no-op, real server-player eligibility, and the explicit direct gameplay parent. It rejects premature events immediately with no queue. The native advancement's visual parent is not relied on for gameplay authorization.
3. Completion remains a native `PlayerAdvancements` query. Resolve the current advancement manager's objects rather than caching objects across reloads. Keep original event ownership, guards, and call ordering from the original assessment.
4. If integer preservation is required, register an ordinary per-award native `StatBasic` counter alongside, never instead of, each visible advancement. Increment it for every parent-qualified attempt, including repeat attempts after completion. Do not put the increment behind `grantCriterion` returning true or an already-done early return. Keep fake-player and null rejection ahead of both effects. Retain a documented legacy-ID mapping; explicit existing-world migration remains separate and unauthorized. Such auxiliary counters preserve integer totals under ordinary grant flows, not automatically old command, scoreboard, revoke, or third-party cancellation semantics.
5. Standard Forge native crafting/smelting/post-pickup events are the narrow native integration points to restore vanished vanilla predicates. Check caller provenance and prevent duplicate NTM smelting increments. Keep NTM's existing 35-entry crafting dispatcher unchanged in meaning, including machine/anvil calls and metadata matching.
6. Retain `rotConsum` as an explicitly documented no-op, or remove only its dead call with a preservation explanation. Do not register a 64th original NTM award, add an egg-possession award, or change the pickup's other behavior.

## UI choice versus gameplay choices

One native Nuclear Tech tab is a presentation choice: a synthetic displayed root with no gameplay prerequisite, no reward, and no toast/chat may group all old roots. The 42 original NTM gameplay parent links remain distinct runtime data. Native auto-layout is a cosmetic replacement for old hand-placed coordinates; exact old GUI recreation is a separate UI project. Neither grouping nor auto-layout justifies weakening Bobmazon NONE, skipping the old vanilla chain, changing recipients, replaying early events, or adding rewards. The requested grouping decision is still parked; no synthetic root has been implemented here.

The seventh legacy node is necessary for cookFish, not a new gameplay benefit. Ordinary 1.12 `story/smelt_iron` or `story/root` substitutions and a universally true Bobmazon NONE gate would alter eligibility and remain unapproved gameplay changes.

## Required validation before claiming parity

- Direct-parent-before-child versus child-before-parent for all old edges; no later replay. Include externally granted direct parent without ancestors to avoid adding recursive gating.
- Repeated qualified awards: boolean completion stays done; optional auxiliary counters increase once per original attempt, with no duplicate smelting increments.
- Wood pickup before/after inventory action; all vanilla log metadata; no possession-only or log-break grant; canceled/full-inventory pickup negatives.
- Non-wood ItemPickaxe and subclass crafting; crafting versus receiving/smelting outputs; vanilla/NTM furnace iron and both cooked-fish variants, normal click and shift-click.
- Inventory action in survival/creative and with rebound keyboard/mouse controls; horse/chest/machine negatives; document programmatic/canceled GUI behavior if a GUI-event approximation is chosen.
- Null rotConsum and standard Forge FakePlayer do not mutate completion or counters. Gneiss's existing 500-XP behavior must not accidentally gain advancement reward XP or depend on a grant return value the old code never tested.
- Native manager reload, restart/relogin/respawn persistence, Bobmazon's seven gates plus creative bypass, visual grouping and dormant definitions. No original save migration fixture is required unless separately authorized.
