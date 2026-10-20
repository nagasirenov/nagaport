# CP87 finite concrete-mob equipment writes

Baseline: fc2cd2dba34caba0dba74ed288f95107ab4dc979 (sealed CP86).

## Production boundary

Exactly 27 complete invocation expressions change across five existing files:

- EntityFBI: 11 constant-slot setters.
- EntityUndeadSoldier: 9 constant-slot setters.
- EntityRADBeast: 1 constant-slot setter and 1 MAINHAND drop-chance setter.
- TileEntityTrappedBrick: 3 concrete EntityZombie setters and 1 MAINHAND drop-chance setter.
- MobUtil: 1 EntityLiving MAINHAND drop-chance setter only.

Each setter remains a virtual call to native setItemStackToSlot. Its integer constant becomes the corresponding native enum: 0 MAINHAND, 1 FEET, 2 LEGS, 3 CHEST, 4 HEAD. Each drop-chance call remains virtual through setDropChance(MAINHAND, unchanged argument). Receiver and complete RHS source bytes are unchanged. There is no new helper, array access, cast, item copy, count normalization, offhand behavior, probability clamp, sound hook bypass, or modification to equip/loot formulas.

Full source census contains 31 old item setters and 3 old drop-chance setters, not merely the smaller subset previously counted by diagnostic signature. Seven item setters remain exactly unchanged: six generic EntityLivingBase writes (ArmorUtil one; MobUtil five) and EntityDummy's separately obsolete dynamic armorType expression.

## Actual original/Forge/native semantics

The original concrete EntityLiving setter stores directly in its five-element equipment array. The native EntityLiving setter stores directly in its two-hand/four-armor native lists. For the selected original indices, both preserve the supplied object by identity. No selected expression supplies null; every selected RHS is a native ItemStack constructor. The existing quantities, tags, aliases and allocation behavior are not normalized by this migration.

Original and native mob drop-chance setters store the supplied float without clamping. Raw negative zero, infinities and a NaN payload are retained in the differential probe. Original skeleton's server-side slot-0 combat-task callback has a native AbstractSkeleton MAINHAND equivalent. Therefore virtual dispatch must not be replaced by raw list writes.

Vanilla EntityPlayer is NOT the original mod's authoritative setter baseline. Forge1614 corrects vanilla's direct armorInventory[index] assignment to MAINHAND at zero and armorInventory[index - 1] otherwise. The exact pinned patch hunk is retained in forge-player-setter-correction.patch. Neither Forge nor FML patches change EntityLiving's setter/drop method; neither contains an EntitySkeleton patch. Forge's player patch adds no equipment sound.

Native EntityPlayer.setItemStackToSlot does call the protected, overridable playEquipSound before storing. Its default implementation produces no sound for EMPTY, but an override still observes that call. This batch therefore leaves generic player-capable setters and generic null removal alone; it does not paper over that dispatch difference.

## Evidence

- source-manifest.json pins the baseline, all 27 before/after invocations, complete RHS values, receiver types, source positions, full before/after file hashes, and all 7 excluded expressions.
- check-source.py reverses five complete files exactly to CP86 and rejects 37 drift mutations. reverse_source(relative, text, strict=False) is available only for use within a separately pinned combined-source inverse.
- EquipmentWriteScanner.java attributes all 4,066 Java compilation units before and after without emitting mod classes. It selects precisely 24 concrete EntityLiving-lineage constant setters and 3 drop setters; it rejects generic, dynamic, shadowed, static, super, Unicode-escaped and offhand candidates. The complete before/after position sets are bijective. All 27 new calls resolve to real EntityLiving native methods with native EntityEquipmentSlot enum constants. No owned setter/drop/sound-hook declarations occur in that source closure.
- NativeEquipmentWriteTest uses actual original EntityZombie and actual native EntityZombie, InventoryPlayer, EntityPlayerMP, ItemStack, NBT and NonNullList. Native and original instrumented subclasses establish virtual dispatch. The original methods executed here are unmodified by the inspected Forge/FML patches.
- All 27 exact selected old expressions fail against native types. All 27 exact selected new expressions compile and execute. Only NTM ModItems dependency fields are supplied by explicit, same-typed native Item fixtures; they are not claimed to be real initialized NTM content. Each full statement is checked for one virtual call, correct mapped slot, unchanged constructor/drop arguments, stored alias, default constructor payload, and untouched offhand.
- The native test passes 339 assertions: the original 157 audit assertions plus full-statement and additional original count/tag/dispatch checks. The run also passes 20 attributed selector fixtures. See native-probe.log.
- legacy-mapping-evidence.json records the verified original SRG class, field and method descriptors and archive hashes.
- engine-setter-contract.javap.txt retains bounded actual engine bytecode. Its vanilla player snippet must be interpreted together with the Forge correction, never alone.

The full-source attribution diagnostics are expected incomplete-port errors, not a successful mod build. This batch does not compile complete NTM production classes, construct complete NTM entities, run gameplay, validate loot behavior, or establish client/server/save acceptance. No network, push, dependencies or global Minecraft/Forge shim are involved.

## Reproduce

From the module, run:

    python3 porting/tests/equipment-writes-native/check-source.py
    python3 -u porting/tests/equipment-writes-native/run.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt \
      --legacy-client /tmp/nagaport-legacy-client.jar \
      --forge-userdev /tmp/nagaport-forge1614-userdev.jar \
      --attribute

The source guard's standalone mode is for this isolated manifest-defined source view. If integrated alongside sibling source edits, first use a separately pinned combined inverse, then run the test in its isolated inverse-defined view. Never refresh this baseline to hide unrelated changes.
