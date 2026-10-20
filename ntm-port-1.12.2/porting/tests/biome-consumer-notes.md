# Remaining biome consumers: native 1.12.2 migration

Baseline: 24dab0cd, original NTM behavior only. No CE code or new generation.

## Exact scope

Seven production files: NTMWorldGenerator, MapGenNTMFeatures, HbmWorld,
BiomeCave, ArcticVault, WorldInAJar, and RenderLandmine. BiomeGenNoMansLand,
BiomeDecoratorNoMansLand, MainRegistry, ModBiomes, celestial definitions,
configured IDs, registry names, providers, and decorators are unchanged.
The commented-out No Man's Land activation remains commented out.

Changes replace old biome types/constants with Biome/BiomeMesa/Biomes,
BiomeDictionary.isBiomeOfType with hasType, and properties with native getters.
Raw temperature remains getDefaultTemperature(), not altitude-adjusted
getTemperature(BlockPos). ArcticVault intentionally retains the latter behavior.
rootHeight becomes getBaseHeight(), including the unchanged oil-rig -1.5F cutoff.
canSpawnLightningBolt becomes canRain, whose pinned native implementation excludes
snowy biomes and otherwise uses the rain-enabled flag.

Provider lookups remain provider lookups; world lookups remain world lookups.
This matters when chunk biome data differs from the provider after crater mutation.
The structure exclusion list still compares exactly five vanilla biome identities,
not all biomes carrying OCEAN or RIVER dictionary tags. RenderLandmine preserves
snow-before-desert texture precedence. All RNG draws, thresholds, control flow,
spawn weights, chunk arithmetic, and original generation decisions are unchanged.

WorldInAJar implements only the native biome accessor signature in this batch.
Other legacy IBlockAccess methods remain outside scope. BiomeCave's legacy event,
world/block operations are separable and intentionally not rewritten. The seven
whole consumer classes are NOT claimed to compile; substantial unrelated legacy
API dependencies remain. Parent owns the sole post-batch full compile.

## Tests and evidence

`check-biome-consumers.py` uses the enumerated exact edit manifest and SHA-256
snapshots for all seven complete files. Forward and reverse application must match
byte-for-byte, and deliberate source drift must be rejected. Before snapshots were
verified against `git show 24dab0cd:ntm-port-1.12.2/<path>` when captured.
`reverse_source(relative, text)` is exported for parent historical snapshot bridges;
it returns unchanged text outside these seven exact relative paths and rejects drift
inside them. Do not blanket-normalize unrelated later changes through this bridge.

`probe-biome-consumers.py` extracts actual production methods and expressions, then
compiles/runs against the real pinned Forge 1.12.2-14.23.5.2768 mapped jar and its
resolved dependency classpath. No substitute Minecraft/Forge classes are used.
The fixture subclasses native Biome/World/WorldProvider/BiomeProviderSingle and
implements native IChunkProvider to exercise real World and Chunk biome routing.
Test-only biomes are registered in the isolated JVM's actual Forge registry before
dictionary operations. No production registry code or saves are modified.

Coverage:
- Full-source parity protects the complete seven-file algorithms.
- Actual isInvalidBiome/isFlatBiome and cave classifier methods, plus all 32 actual
  canSpawn lambdas, compile natively; every lambda executes across 150 property
  combinations. The individual 32 lambda outputs are smoke coverage, not 32
  independently reimplemented golden algorithms.
- Flat/cave/desert-texture thresholds have explicit expected assertions; oil-rig
  base-height predicate is checked below, at, and above -1.5F.
- Exact vanilla exclusion identities versus custom OCEAN dictionary membership.
- Rain/snow-disabled behavior and raw versus altitude-sensitive temperature.
- Native World + Chunk loaded-biome override versus provider biome, unloaded
  fallback, negative chunk-center lookup coordinates, native TileEntity lookup,
  and WorldInAJar's constant plains accessor.

Run without Gradle using Java 8 and the saved real classpath:

    python3 porting/tests/probe-biome-consumers.py --java-home <JDK8> --classpath <native classpath>

Optional Gradle integration is `porting/biome-consumer-probe.init.gradle`, task
`testPortBiomeConsumers`. This task does not assert whole consumer compilation.
No Gradle task was run by this batch's worker.

Verified log: `porting/continuation-2026-10-19/biome-consumer-native-probe.log`.

## Still-required acceptance

Whole-project compile; runnable client/dedicated-server loading; real structure and
cave generation; mine texture rendering; crater/provider interaction during actual
world population; save/reload biome consistency. Existing celestial/orbit/crater
registration and optional extended IDs need their own runtime acceptance. This
consumer-only batch neither changes IDs nor establishes cross-version save safety.
