# Checkpoint 82: native slot callbacks, world writes, plane synchronization and cube textures

Baseline: sealed CP81 242867906a53eb7da03c879f476ced61c1170e14. Production scope is 56 existing Java files, with no new platform emulation classes, source exclusions, dependency changes or gameplay balance edits. The complete historical source guard verifies 56 exact inverses and 168 mutation rejections before the earlier checkpoint chain.

## Bounded native validation

- Slot pickup linkage: 7 legacy overrides, 32 ordinary call edges and 7 super-call edges across 32 files now use native onTake and its ItemStack return contract. Original callback body ordering and early exits remain unchanged. 117 actual native Slot/Container assertions, 24 original/native parent assertions, four behavioral mutants, one negative native compile and 96 source drift controls pass. This is NOT complete table inventory/transfer certification: existing nullable checks versus native EMPTY, Slot.onSlotChange quantity accumulation, upgrade helpers and full transfer methods remain separate unfinished roots. No global inventory normalization was introduced.
- World writes: 36 compiler-attributed vanilla calls across 17 files now provide genuine native states to a small WorldBlockState coordinate boundary. Original [-30000000,30000000) horizontal/[0,256) height admission, argument order, flags and return dispatch remain. 22,797 original/native World/Chunk/storage checks, seven exact conversion bytecode audits, eight behavior mutants and 36 source mutants pass. Fifteen nonrepresentable raw-metadata calls are excluded; custom writes and metadata-only APIs remain unfinished. Native observers, debug-world rejection and conditional lighting are documented platform differences, not claims of identical old engine internals. Placement callbacks, ticks, full lighting, network/save and arbitrary mod interactions are not certified.
- Plane data: native owner-local FLOAT health and BYTE bomber style keys replace 19 legacy watcher accesses across PlaneBase, Bomber, C130 and RenderBomber. 2,382 native assertions in each of two fresh class-loading orders (4,764 total) cover initialization, factories, NBT, full/dirty packets and byte style domain. Original first-server-tick health overwrite is preserved, not silently fixed. Complete constructors use the actual Forge quota-zero ticket path; successful ticket/chunk-loading behavior remains untested. Fifty-one source slices and complete-file inverses define the bounded projection.
- Cube textures: 15 audited all-face bindings across nine exact owners extend the existing native atlas/model path. Sound, assets, gameplay, ItemBlock classes and renderer implementations are untouched. 65,309 native assertions include genuine atlas/GL upload, late item ownership, reloads and original Nether-coal animation timing/texels; the prior depth suite passes 51,935 assertions. These headless tests are not an in-game visual review. Fourteen sound-blocked vanilla falling constructions remain untouched.

Each family directory under porting/tests contains exact provenance, dependency hashes, native harness commands, mutations and scope limitations: slot-take-native, world-block-write-native, native-entity-plane and plain-cube-remainder. Read their READMEs before extending a transform. Historical whitespace/CRLF is intentionally preserved rather than normalized.

## Full compile and reproduction

Fresh offline full compile completed with 12,359 diagnostics, versus 12,471 at CP81 (112 fewer). All 4,065 generated compiler inputs are byte-identical to current source. The exact result is recorded in diagnostics.json, full-compile.log and source-inputs.json. A failed compile does not create a runnable mod. No successful whole-mod build, client/server startup or game-ready claim is made.

From ntm-port-1.12.2:

    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/tests/check-thermal-family-source.py

Toolchain is genuine restored Java8/Forge14.23.5.2768/MCPstable39 with locally cached runtime dependencies. No new downloads or denied-route retries were needed.

## Continuation and recovery

Remote main remains published CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570; no push. CP81 was durably preserved in the existing recovery Library item version9 before this work. The separate unapproved custom-potion candidate remains isolated at fb96ddf6 and must not be integrated without its legacy config/JSON/save migration contract. Sound access/AT changes remain parked. Existing visual compatibility gates and unfinished machine/energy/radiation integration remain explicit.

Use the cumulative archive on a fresh exact CP75 checkout, not on top of an earlier cumulative patch. It includes local history objects. Back up first, git apply --check, stop on conflict/CRLF refusal; never force/reset/normalize or overwrite later user edits. Windows instructions are in the archive; Windows execution is not tested. No further push is authorized.
