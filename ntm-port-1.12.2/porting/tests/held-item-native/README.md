# Nullable NTM main-hand boundary

Base: CP54 `0732d318302d807ce8579878a0cdde6b18bbba86`. This finite patch replaces 406 compiler-attributed zero-argument `getHeldItem()` invocations in 136 production files. It adds one 22-line NTM-owned boundary, `NativeHeldItem.getMainHandNullable(EntityLivingBase)`, rather than a platform shim. Existing callers deliberately retain their nullable stack contracts, repeated getter calls, identity comparisons, branch order and downstream APIs. No explicit-hand invocation changes, and offhand equipment never participates.

The helper invokes real, virtual native `getHeldItemMainhand()` exactly once and translates only `ItemStack.EMPTY` identity to null. Any other object, including a noncanonical zero/negative-count or AIR stack and its attached tag, remains identical. Custom native main-hand and equipment-slot overrides are honored. Custom null results remain null. A null entity still throws. Class initialization introduces no static state.

Important engine boundary: original zero-count `ItemStack.getItem()` retains its underlying item, whereas native `getItem()` exposes AIR. This patch does not and cannot restore that ItemStack contract; it specifically avoids making things worse by dropping noncanonical stack identity, NBT or aliases through `isEmpty()`. This is not universal legacy stack or full caller gameplay equivalence.

## Owner and override closure

`HeldItemScanner` parses and attributes every production Java source against the genuine mapped native dependencies, despite the expected unrelated incomplete-port errors. Each selected receiver has an actual declared superclass chain reaching EntityLivingBase. Actual selected static receiver types are EntityLivingBase, EntityLiving, EntityPlayer, EntityPlayerMP, AbstractClientPlayer and EntityPlayerSP. Unresolved/error/type-variable/non-entity/static-type receivers, zero-argument or applicable-varargs old-name shadows, explicit-super invocations, Unicode-escaped calls and potential `com` package-name captures are rejected. Explicit native hand calls are untouched. The 12 unresolved `Minecraft.thePlayer` calls from CP53 became genuine EntityPlayerSP receivers after CP54; all 406 are now attributed, not text-selected.

AST traversal audits declarations named getHeldItem, getHeldItemMainhand, getItemStackFromSlot, getEquipmentInSlot, getCurrentItemOrArmor, getLastActiveItems and setCurrentItemOrArmor. There are no such declarations in current NTM production. A separate pinned original-tree audit screens all 3,840 pristine Java files and parses all 181 name-bearing/Unicode candidate units: there are likewise no declarations. Thus no NTM old getter override is silently bypassed. Existing FBI, undead-soldier, dummy and RAD-beast equipment setters are calls, not overrides; those old setter calls remain unported and no whole-NPC behavior is claimed.

The actual original SRG maps EntityLivingBase/EntityLiving/EntityPlayer `func_70694_bm` to sv/sw/yz `be`. Original EntityLiving reads its equipment array at index zero directly; original EntityPlayer delegates to InventoryPlayer.getCurrentItem. Neither goes through the old armor/equipment getter hooks. Native EntityLivingBase.getHeldItemMainhand delegates virtually to getItemStackFromSlot(MAINHAND); native player routes to inventory.getCurrentItem, while native mob reads hand slot zero. Native custom overrides execute in the runtime tests; an actual original-engine subclass override also executes, with exact returned identity and one invocation.

## Executable proof and explicit limits

- 77 actual original/native runtime checks cover player/mob empty and equipped stacks, NBT aliases, canonical and noncanonical emptiness, negative counts, AIR stacks, mainhand/offhand separation, invalid hotbar indices, custom slot and main-hand overrides, null entities/results, repeated-call behavior and short-circuiting.
- Two complete unchanged production methods are extracted, compiled and run: Library.checkForHeld and ItemBlockStorageCrate.InventoryCrate.isUseableByPlayer. Their original equivalents execute on real original-engine player/item objects through exact SRG name mapping.
- The precise ItemCrucible.onEquip durability/held predicate and BlockTransporterRocket.onBlockActivated held/linker predicate execute against both actual engines. The latter binds the ModItems.transporter_linker item reference as an explicit native Item argument; it does not invent a ModItems or platform implementation. These are predicate boundaries only: animation, sound, standardOpenBehavior and the enclosing full weapon/machine methods are NOT executed or claimed complete.
- Unsafe only allocates genuine engine instances or explicit test subclasses without a full server/client constructor graph; genuine inventory/equipment getters are then invoked. This is not a game launch, world lifecycle or network test. No platform class stubs or source exclusions are used.
- 23 adversarial javac fixtures cover owner identity, local/inherited/interface/static/varargs shadows, explicit native hands, generics, super, casts and Unicode.
- 136 exact forward/inverse source manifests and 408 source mutations protect original UTF-8/CRLF bytes and unrelated edits. The helper source is SHA256-pinned.
- Full reconstructed-source attribution must select precisely the 406 original spans. Full post-edit attribution must resolve precisely the 406 new spans to the actual static NTM method with EntityLivingBase parameter and native ItemStack return type. Bytecode separately proves one native virtual getter and no offhand or isEmpty path.

Reproduce, without downloads or a full Gradle build:

    python3 porting/tests/held-item-native/run.py --java-home JAVA8 --classpath-file NATIVE_CLASSPATH_FILE --legacy-client ORIGINAL_CLIENT_JAR --legacy-srg ORIGINAL_SRG_ZIP --verify-attribution
    python3 porting/tests/held-item-native/audit_original.py --java-home JAVA8 --classpath-file NATIVE_CLASSPATH_FILE

The original audit uses read-only git objects from the pinned base, so it requires that commit and its pristine source tree. The runtime creates a temporary original-JAR test container omitting signature metadata solely so test subclasses can share the original default package; all original class bytes remain unchanged. No Minecraft binaries are redistributed.

Observed attribution diagnostics before/after: 26,162 / 25,787. They are expected incomplete-port diagnostics, not a successful build, distinct bug counts, a completion percentage or a claim that 406 diagnostics disappeared. Complete mod compile, whole NPC/machine/weapon owners, gameplay, saves and launches remain gates outside this finite proof.

Integration must preserve other workers' exact fragments. These isolated whole-file hashes are not permission to overwrite concurrent approved edits. No main-tree edits, full Gradle runs, downloads, exclusions or pushes were performed by this worker.
