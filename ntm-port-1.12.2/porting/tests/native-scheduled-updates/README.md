# Ordinary native scheduled block updates

Exact base: `bdb4c897d5f554458d23c6efab036b056ee20de0` (CP83).

## Production scope

37 ordinary five-argument `World.scheduleBlockUpdate(x, y, z, block, delay)` calls in 21 files become native `World.scheduleUpdate(new BlockPos(x, y, z), block, delay)`. All original receiver, coordinate, block and delay expressions remain in their original Java evaluation order. The position is immutable and built from the original integer expressions, not a reused mutable position or an already supplied callback state. The 18 preexisting native calls are untouched.

No helper/shim, timing clamp, priority change, new world/chunk check, callback rewrite, branch rewrite, RNG draw, queue mutation or source exclusion is added. The production diff contains exactly 37 changed lines. Existing CRLF is preserved in BlockDynamicSlag. Use `git -c core.whitespace=cr-at-eol diff --check` rather than treating preserved CRLF as newly introduced whitespace.

The two press calls deliberately preserve repeated `this.pos.getX/Y/Z` reads and the later `this.world` read inside `WorldBlockState.read`. Replacing these with `this.pos` or one cached world would not have the same argument evaluation. Existing delay1-then-delay2 pairs in SoyuzLauncher and MachineITER remain separate original calls; the native queue keeps the first existing same-position/block entry, rather than inventing a second callback.

This only migrates scheduling expressions. Many enclosing block/tile classes still have unrelated unresolved methods. In particular, BlockGasBase's old callback and protected immediate-flag write remain unchanged and unresolved; this patch does not pretend those full owners are executable.

## API and original-engine audit

The supplied Forge 1614 `conf/methods.csv` and 1.7.10 SRG establish:

- `WorldServer.a(int,int,int,Block,int)` = `func_147464_a` = ordinary `scheduleBlockUpdate`.
- `WorldServer.a(int,int,int,Block,int,int)` = `func_147454_a` = `scheduleBlockUpdateWithPriority`.
- `WorldServer.b(int,int,int,Block,int,int)` = `func_147446_b`, a distinct restoration path.

Actual original bytecode makes the five-argument method forward to the priority-aware ordinary path with literal priority0. Actual native `WorldServer.scheduleUpdate` likewise forwards to `updateBlockTick` with literal priority0. The ordinary paths apply the immediate-mode logic and loaded-position check, compute non-air due time as world total time plus the unchanged signed delay, and deduplicate an existing equal position/block entry. Original equality does not include scheduled time or priority. The Forge 1614 scheduling patch adds comments, not an active persistent-chunk alteration to that ordinary method.

Native `scheduleBlockUpdate(BlockPos,Block,int,int)` is NOT the matching ordinary overload: it is the restoration path and bypasses both the ordinary loaded-chunk and immediate-mode gates. It would silently enqueue unloaded work and bypass immediate callbacks. This patch never uses it.

## Accepted native engine boundaries

These are native platform integration, not claimed 1.7.10-wide timing parity:

- Original normal scheduling checks a zero-radius area and rejects Y outside [0,256). Native normal scheduling uses `isBlockLoaded`, which checks X/Z chunk membership without that Y guard. The source's integer coordinate expressions are not clamped or changed to compensate. Native out-of-height queue insertion is explicitly tested.
- Original immediate requiring-update blocks check radius8. Native Forge reduces the radius to0 for a persistent chunk; this is explicitly tested, not recreated in production.
- Native scheduling obtains material through the block's default state rather than the old direct material getter. Normal non-air delay/priority and air default-time behavior are checked. This is not a certification of every NTM state mapping.
- Native due-tick processing on an unloaded position attempts an ordinary delay0 reschedule; because its chunk is still absent, the ordinary gate rejects it. The actual combined path is executed, not replaced with a successful mock rescheduler.
- Native `WorldServer.isChunkLoaded` delegates to the actual `ChunkProviderServer.chunkExists`, which only consults the loaded map. Unlike `getLoadedChunk`, it does not clear `Chunk.unloadQueued`. No eager provider lookup is inserted.

No reflection or direct queue access is present in production. No attempt is made to emulate a 1.7.10 engine with 1.12 queues.

## Evidence and limits

- Exact forward/inverse verification of all 21 complete source files against Git CP83; 132 per-site target/delay/coordinate and unrelated-edit mutation rejections. `verify.py.reverse_source(relative, text)` is available for later checkpoint history-chain integration.
- Whole-source JavacTask parse/analyze only: 4,065 units, zero parse errors, no code generation. Errors fall from12,169 to12,132, exactly37 old signature diagnostics removed and zero errors added. Every changed call attributes to genuine `World.scheduleUpdate(BlockPos,Block,int)`; all18 previous native scheduling calls remain unchanged. This is not a successful full production compile.
- 1,028 native assertions pass. Every one of the37 patched call expressions is copied verbatim into a Block-derived fixture, compiled against genuine Forge1.12.2, and run for loaded/unloaded chunks and deterministic RNG seeds. Coordinate expressions, block identity, delay, priority0 and RNG remainder are checked. `tickRate` and `getDelay` are explicitly instrumented domain collaborators, not claims to execute every production block's behavior. The actual production WorldBlockState helper is compiled for the two live-block press reads.
- A special press fixture mutates the world field during the first position getter. It verifies original outer-receiver capture, XYZXYZ getter order, subsequent live-block lookup in the new world, and scheduling into the originally captured world. Null-receiver fixtures prove delay effects still occur before invocation throws.
- Real native `scheduleUpdate`, `updateBlockTick`, `tickUpdates`, area/block loading checks and `ChunkProviderServer.chunkExists` execute unchanged. Empty scheduler collections and an actual native provider are initialized in a WorldServer fixture allocated without full server startup. Queue inspection is read-only; all tick insertions use actual native public scheduling methods. Actual Chunk objects are allocated only as provider membership entries, not simulated terrain. Live state lookup and persistent-chunk membership are controlled collaborators.
- Checks cover due/future work, priority ordering, deduplication, skipped replacement/air owners, immediate requiring/non-requiring branches, loaded/unloaded areas, forced-chunk radius, signed extreme delays, air entries and the misleading same-name restoration method. No fake Minecraft/Forge class or copied scheduler implementation is compiled.

The checked run is recorded in `verified-run.log`, `verified-summary.json` and narrow textual `engine-evidence.txt`. No proprietary JAR/class files are included. Full-world initialization, chunk generation, save/load lifecycle, complete owner execution and gameplay timing are outside this bounded result.

## Offline reproduction

    python3 porting/tests/native-scheduled-updates/run.py JAVA_HOME CLASSPATH_FILE LEGACY_CLIENT_JAR FORGE_1614_USERDEV_JAR SRG_1710_ZIP OUTPUT_DIRECTORY

The runner requires existing local Java8, genuine Forge1.12.2 classpath, original client, Forge1614 userdev and 1.7.10 SRG inputs. It performs no networking/installations, Gradle rebuild, pushes or binary copying. Run on the sealed CP83 base plus this independent production patch. `--attribution-only` is an optional diagnostic-only mode. Output includes hashes of original inputs and exact javap excerpts for the audited engine boundaries.
