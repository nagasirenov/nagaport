# Bounded scheduled/random block callback boundary

Exact base: 112cf44057e5cd1d1173561e66dbcca97edc9bdb (CP71).

33 production updateTick declarations adopt World, BlockPos, IBlockState, Random and decode coordinate locals at entry. All original body bytes, formulas, RNG calls, scheduling calls, flags, live reads and return paths remain unchanged. The supplied state stays deliberately unused. Empty original callbacks remain empty apart from coordinate decoding; no production placeholder or compatibility API is added. This patch does not repair the still-legacy body APIs or claim complete-owner/game readiness.

## Scope and hierarchy

The compiler-resolved inventory contains 58 old-shaped block tick callbacks. 25 are excluded: existing BlockDummyable native/domain bridge and descendant callbacks; explicit superclass-delegating callbacks; and BlockGasBase/BlockHazard roots whose excluded descendants would otherwise lose their old-shaped super target. These families need separately justified coordinated migration, especially state-sensitive vanilla/Forge superclass behavior. The exact list and reasons are in manifest.json.

The 33 selected methods are actual Block descendants and become actual native overrides. Existing native methods, old-shaped domain overloads, excluded methods and unrelated ItemCustomLore.updateTick remain byte-exact. All source-level explicit calls and their attributed receiver/owner information are retained in the audit. None targets a selected owner in the baseline. TileEntityHydroponic's existing old-shaped call through Block is not fixed or certified here. BlockSellafield is selected independently of excluded BlockHazard: it does not call super, and its original overriding body remains intact.

All selected native ancestor families (Block, BlockContainer, BlockSapling, BlockFarmland, BlockFire) inherit the actual Block.randomTick implementation, independently checked with reflection. No new randomTick override is introduced. Native superclass updateTick methods are never invoked by a newly invented delegation. Descendants and all explicit calls are listed by full compiler attribution in verified-summary.json; unresolved ERROR types are recorded honestly.

Full-source attribution parses 4,065 units without parse errors. Diagnostics fall from 13,966 to 13,937: 29 override errors removed, zero errors added, ignoring line shifts. The four-method difference is expected: MeltedFlesh, BlockLayering and BlockUberConcrete lacked @Override, and BlockSellafield already overrode the old BlockHazard method. These four now override native Block.updateTick too.

## Original and native engine evidence

The provided original client and SRG establish aji.a(World,int,int,int,Random) as Block.func_149674_a. Original WorldServer (mt) invokes that same virtual callback in its random block loop g()/func_147456_g, immediate scheduling branch a(int,int,int,Block,int,int), and queued tick processing a(boolean)/func_72955_a. The actual original class bytecode has all three invocation sites and passes the world's existing RNG rather than a newly created random source.

Actual native WorldServer.updateBlocks invokes Block.randomTick with the selected block position/state and the same World.rand. Actual native Block.randomTick immediately invokes virtual updateTick(world,pos,state,random), performing no RNG draw itself. Native WorldServer.updateBlockTick's immediate branch and tickUpdates invoke updateTick directly. Native World.immediateBlockTick also dispatches updateTick, with its own immediate-update flag lifecycle. These routes are not interchangeable scheduling policies; this patch changes none of them and does not add duplicate callbacks.

The signature adaptation preserves the RNG reference unchanged and adds no draws. The coordinate locals preserve mutable integer parameters, including bodies that reassign x/y/z during their original logic. Original live world reads are not replaced by the supplied snapshot. The engine uses non-null BlockPos; no prior null-position contract existed.

## Native runtime scope

147 assertions execute genuine WorldServer.tickUpdates, inherited Block.randomTick and World.immediateBlockTick. The tests cover future-versus-due entries, queue draining, replacement/air owner skips, unloaded-area rescheduling, final integer coordinates, original client/server branching, domain virtual dispatch, immediate flag lifecycle, and the same RNG identity, draw order and remaining stream across each route.

Two complete exact production callback bodies are projected into native Block-derived test owners: BlockNTMSand and BlockFallout. Their fall/radiation domain collaborators record arguments rather than claiming to simulate the separate sand physics or radiation subsystem. A separately labelled RNG boundary fixture consumes known draws to test engine forwarding, not arbitrary production growth/decay logic. Reflection checks all five selected native ancestor randomTick owners.

The fixture is a genuine WorldServer subclass allocated without server startup; only required scheduler fields are initialized (real WorldInfo, Random, Profiler, NextTickListEntry and collections). Area loading, live block-state lookup and the unloaded reschedule boundary are explicitly controlled. The real tickUpdates algorithm, due-time checks, queue processing and callback virtual dispatch execute unchanged. No fake net.minecraft/net.minecraftforge classes or copied scheduler implementation are compiled. This does not prove chunk-random-selection frequency, long-term world timing, full block owners, chunk simulation, persistence, or gameplay parity.

## Reproduction and source boundary

    python3 porting/tests/native-tick-callbacks/run.py JAVA_HOME CLASSPATH_FILE LEGACY_CLIENT_JAR SRG_ZIP OUTPUT_DIRECTORY

Use Java 8, an existing actual Forge 1.12.2 classpath, the provided original client JAR and 1.7.10 SRG ZIP. No downloads/installations are performed. The script reconstructs selected exact baseline sources from Git and links unchanged sources to avoid duplicating assets, runs before/after attribution, source inverses and mutation tests, native scheduler/runtime tests, and captures javap evidence. It expects the exact sealed base plus this independent patch, not an arbitrary later integration.

verify.py exposes reverse_source(relative,text) for history-chain integration. It verifies 33 exact forward/inverse transformations and rejects 99 mutations. Paths outside the fixed manifest are untouched. The original callback body bytes are invariant; any state substitution, altered RNG expression or unrelated source change fails the source hashes. CRLF source formatting remains preserved. verified-run.log and verified-summary.json report the reproduced result; engine-evidence.txt contains the relevant genuine bytecode methods, not rewritten pseudo-code.
