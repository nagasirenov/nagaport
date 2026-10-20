# Remaining chunk-coordinate consumers (2026-10-19)

Bounded migration from checkpoint `a1b1930`: 13 production consumers now use the real
`net.minecraft.util.math.ChunkPos`, `x/z`, `Chunk.getPos()`, `ChunkPos.asLong`, and
`getXStart/getZStart`. No substitute Minecraft class or compatibility adapter added.
The 16-block chunk shifts, 64-block pollution cells, station-cell arithmetic,
subchunk hashes, explosion ordering, and coordinate NBT names/types are unchanged.

## Station hashes verified, not assumed

The official Mojang version manifest's Minecraft 1.7.10 server binary is:
https://launcher.mojang.com/v1/objects/952438ac4e01b4d115c5fc38f891710c4941df29/server.jar

SHA-1: `952438ac4e01b4d115c5fc38f891710c4941df29`, size 9,605,030 bytes.
Its obfuscated chunk-coordinate class `agu` has the two int constructor/fields,
packed-coordinate static method `a(int,int)`, center methods `a()/b()`, and
`hashCode()`. `javap -c` confirms its hash arithmetic is identical to mapped
1.12.2 `ChunkPos.hashCode()`: `(1664525*x + 1013904223) ^
(1664525*(z ^ -559038737) + 1013904223)`, with Java int overflow.
The packed long also uses identical low-X/high-Z masked 32-bit halves.
Logs: `continuation-2026-10-19/chunkpos-{legacy,target}-api.log` and
`chunkpos-legacy-ids.log`. This permits retaining direct native hash calls for
station display/lookup, rather than introducing an unnecessary domain helper.
Six actual legacy-instance ID outputs are pinned in the native probe, including
negative and int-limit coordinates. An optional isolated URLClassLoader oracle
compares actual legacy instances with target instances (the old jar is not a
production dependency and is not distributed with this patch).

## Reproduce

Use Java 8 and the project's configured Gradle/toolchain:

```
bash gradlew --offline --no-daemon --console=plain --init-script porting/chunkpos-probe.init.gradle testPortChunkPos
# Optional stronger oracle: add -PlegacyMinecraftServerJar=/path/to/verified/1.7.10/server.jar
python3 porting/tests/check-chunkpos-migration.py
```

The portable init task takes the actual `compileJava.classpath`; it does not
change production source sets, compiler diagnostics, or source exclusions.
The native test compiles the complete production `SubChunkKey` and uses real
ChunkPos/BlockPos/NBT classes. Checks cover signed boundary conversion, packing,
hashes, independent-instance map/set lookup, mutable subchunk lookup keys and
height discrimination, and a native pollution coordinate NBT-format round trip.
The NBT test is a format contract, not execution of the full PollutionHandler.
When checkpoint `a1b1930` is available, the separate static guard compares all
13 complete sources to it, allowing only enumerated API substitutions. Without
that local checkpoint (for example, in a transferred patch/archive), it verifies
newline-normalized SHA-256 fingerprints of the reviewed sources and explicitly
reports snapshot verification rather than a fresh baseline comparison. An optional
first argument selects another available git reference; an unavailable explicit
reference fails. These checks guard production NBT methods, coordinate formulas,
RNG order, and station identifier formatting against changes outside this slice.

## Limits

Only SubChunkKey is isolated-compiled as a complete production class here.
Other consumers still have unrelated legacy APIs (World/TileEntity/ItemStack,
MathHelper, provider internals, etc.). WorldUtil's native packed-key replacement
does not complete its separate ChunkProviderServer migration. Old names remaining
in CommandDebugChunkLoad and TestEventTester are commented-out code. Full build,
gameplay, station/pollution save/restart, and multiplayer are not established by
this probe. Parent task owns the common full compile and integration diagnostics.
