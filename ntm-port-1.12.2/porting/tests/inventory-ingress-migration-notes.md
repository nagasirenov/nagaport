# Independent inventory ingress and removal boundary

Scope: the 35 DIRECT setter files listed in inventory-ingress-source-manifest.json. MachineBase, SatDock and PistonInserter are excluded and require separate integration coverage. DiFurnaceRTG owns a direct setter but inherits removal from MachineBase. The other 34 files now declare native removeStackFromSlot rather than getStackInSlotOnClosing. Independent getters are unchanged. Four portable-inventory decrement bodies change only their existing full-removal setter clear argument from null to EMPTY; their legacy count API and other behavior are unchanged.

Setters normalize null or native-empty input to nullable storage before any count access or storage. Existing nonempty identity, clamp limit, callback count and callback ordering remain. Only touched setter count APIs became native. Removals retain original clear/callback behavior, return the same live nonempty object, and expose EMPTY for absence or native-empty results. Portable item inventories still clear through their setter, preserving markDirty; that native clear passes EMPTY. No compatibility shim or runtime inventory-type whitelist was introduced.

Read-only legacy removal call audit found three generic proxy/PWR calls, which belong to the coordinated boundary work, and seven container-local calls in ContainerAnvil, ContainerArmorTable, ContainerBook, ContainerLemegeton and ContainerWeaponTable. Those latter receivers are vanilla/local crafting inventories, not these owned declarations. No typed calls to the 34 renamed owned methods were found. This is not a claim that the unrelated containers compile.

## Source preservation and composition

The frozen manifest records complete before/after SHA256 plus exact old/new methods for each file. check-inventory-ingress-source.py exposes reverse_source(relative, text): unknown paths pass through; owned paths reject any migrated whole-file drift, reverse exact unique slices, and validate the original whole-file hash. It needs no local Git objects or temporary snapshots and preserves CRLF bytes through UTF-8 byte reads. Integrators must compose this reversal before callback -> naming -> sided -> earlier historical reversals. Other simultaneous boundary changes need their own frozen reversal first where relevant. Existing historical manifests must not be rehashed. The helper itself does not automatically modify earlier checkers.

## Verification

Direct native run: PASS 828 checks across 35 setters and 34 removals, plus 35 whole-file reversal checks, 105 deliberate drift rejections and exact clear-only source checks for four portable-inventory decrement callers. Those four decrement bodies are not compiled in the native method fixture because they retain unrelated legacy count API debt. Tests compile exact extracted production methods with actual Minecraft 1.12.2 ItemStack and registry bootstrap; controlled storage and callback counters isolate method behavior. Cases include null, EMPTY, zero/negative inputs, unclamped/clamped live identity, neighboring-slot preservation, setter notifications, removal identity/count, nullable clear and item-inventory removal dirty callbacks.

These are NOT complete production inventory/entity classes, proxy integration, full mod compilation, client/server gameplay, save roundtrip or Windows runtime acceptance. The controlled legacy foundry notification collaborator checks unchanged callback count, not native World method migration.

Run with an existing JDK8 and native Minecraft/Forge classpath:

    python3 porting/tests/check-inventory-ingress-source.py
    python3 porting/tests/inventory-ingress-native-test.py --java-home /path/to/jdk8 --classpath "$NATIVE_CLASSPATH"

On Windows use the installed Python executable and native semicolon-separated classpath. The runner selects .exe where required, joins paths with os.pathsep, writes UTF-8, and uses a temporary output directory. It does not invoke Gradle, fetch dependencies, or access Git objects.

Prepared, NOT executed: porting/inventory-ingress-probe.init.gradle provides testPortInventoryIngress with -PportPython=yourPython support; it uses the existing compileJava classpath without source-set changes. JDK tools are selected from java.home or its JDK parent if Gradle reports the embedded JRE.
