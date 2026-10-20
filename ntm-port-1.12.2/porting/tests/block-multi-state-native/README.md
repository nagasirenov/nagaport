# BlockMulti native four-bit state owner

CP83 baseline: `bdb4c897`. One production file changes: `BlockMulti.java` gains a genuine native `PropertyInteger` and the state-container/metadata conversion trio. It preserves raw four-bit world metadata instead of rectifying subtype indices. Existing damage dispatch, subtype lists, icons, registration and gameplay methods are unchanged. There is no new legacy helper or broadened BlockBase state contract.

The inheritance audit covers **33 classes and 36 registered construction sites**. **29 registered instances** newly inherit the codec; seven instances retain their existing complete native state owners. There are 25 affected concrete class definitions, 24 instantiated. Two tall-plant owners require bit 8 independently of subtype count. No constructor reads the virtual subtype count or assigns the shared property into an overriding descendant's state container.

## Reproduce

From `ntm-port-1.12.2`, using already-installed genuine Java 8/Minecraft/Forge dependencies:

    python porting/tests/block-multi-state-native/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python porting/tests/block-multi-state-native/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --negative-control

No downloads, Gradle/full build, source-selection edits, platform replacements, Unsafe, or proprietary binaries in this evidence. The baseline ZIP contains only original NTM/port Java sources. Its SHA256 is frozen in the runner. Ordinary runs compare the manifest and complete CP83 source baseline; they never regenerate evidence or need unpublished Git objects.

## Native execution

The positive run passes **4,230 assertions** with genuine Forge 14.23.5.2768 and Minecraft 1.12.2. The negative control compiles the **complete unchanged CP83 BlockMulti** from the pinned ZIP and the same harness. It fails the actual runtime sixteen-state-container assertion, proving the test distinguishes the original metadata-zero defect.

- **29 complete production Java files compile**, including real BlockBase, BlockMulti, BlockEnumMulti, all six representative concrete types, actual ItemBlockBase, its tooltip/persistent-NBT interfaces, legacy icon adapter and LegacyBaseBakedModel. No production owner is shortened or reimplemented in the test.
- The **six ModBlocks construction statements are verbatim projections**, not the complete ModBlocks class. They instantiate the actual full BlockMeteorOre, BlockConcreteColoredExt, BlockLightstone, BlockEnumMulti, BlockCM and BlockICFLaserComponent classes. Exact names, native registry identities, hardness and resistance are checked. The construction slice is named distinctly; no fake ModBlocks class is compiled.
- MainRegistry is an explicit static creative-tab/bootstrap fixture. Its translation backend throws if reached. CompatExternal's external multiblock lookup is a throwing fixture. Neither omitted backend executes. Actual ItemBlockBase and IPersistentNBT classes compile unchanged, but translation/tooltip and persistent-tile mechanics are not claimed.
- Each representative has sixteen distinct native states, a zero default, lossless 0–15 state-ID roundtrips, and low-four-bit masking of inputs -32 through 47. The complete native ItemBlockBase retains subtype metadata; the real native placement callback and ItemBlock.placeBlockAt put it into an actual World/Chunk.
- Native same-state no-op, metadata change, restoration, drops, pick, creative subtype list and removal are exercised. Drop comparison executes the pristine BlockMulti.damageDropped and IBlockMulti.rectify bodies as a separately generated oracle. Only the interface-only `default` declaration modifier is removed from the oracle; original bodies are byte-for-byte source checked.
- Actual AnvilChunkLoader writer/reader and compressed NBT serialization restore all 96 placed states into a fresh native chunk/world. These six owners are non-tile; no tile is invented. This is genuine section serialization, not a handwritten state codec. Disk region IO and whole-server save migration are outside scope.
- Actual DefaultStateMapper creates all sixteen `metadata=N` model locations. Actual LegacyBaseBakedModel/FaceBakery select nonzero polymorphic icons on six faces, preserve the metadata cache, and produce inventory subtype faces. Striped-concrete face selection, lightstone top variants and ICF shared top/distinct side variants are checked. The real icon adapter registers genuine TextureAtlasSprite objects; controlled native sprite dimensions supply UVs. Resource PNG decoding, stitching/GL upload, and whole-client model-event integration are not run.
- Actual full BlockBase and BlockGeneric still have one property-free state and collapse nonzero inputs to zero. Actual full BlockDecoToaster construction retains its independent `META` property, orientation codec, shape and drop override. All other existing state-owning descendants plus RadioTorchBase and DetCord are byte-for-byte preserved in the source proof, not claimed as runtime-tested here.

## Source and scope controls

`run-native.py` removes only the explicit added imports/property/methods and restores the prior qualified signature. The resulting **entire BlockMulti file must equal CP83 byte-for-byte**. Every other archived closure/route file remains byte-identical, including BlockBase, Generic, RadioTorchBase, DetCord, existing specialized owners, model handler and actual item owner. The live 33-class inheritance closure and complete-state-override roots must match the frozen manifest. Six construction statements and all 29 compiled production files have independent frozen source checks.

`source-closure-audit.md` contains the complete descendant list, all 36 construction sites, specialized-state ownership, model routes, upper-half bits and tile-provider lifecycle boundaries. Native Forge already supplies ITileEntityProvider dispatch; no new shared tile bridge is added.

## Explicit remaining limits

This fixes the **shared native state representation**, not every descendant's remaining 1.7 callback. Resource-stone special drops, sand falling, tall-plant placement/growth, crashed-bomb behavior, tile lifecycle and other unmigrated callbacks remain separate work. There are no migrated world-write call sites in this patch and no claim of reduced compiler diagnostics. BlockBase/Generic nonzero metadata remains intentionally unsupported.

LegacyBaseModelHandler is source-audited only: its actual full registry/bake events and presence of every generated world variant key are not executed. The full native model objects and mapper are tested, but that does not claim successful client rendering or art assets. Full ModBlocks initialization, full build, client/server startup, multiplayer, world generation, lighting, live gameplay, and game readiness are **NOT_RUN**.

## Exact scheduled-update composition receipt

The concurrently authorized scheduling batch changes 37 call sites in 21 files. Its complete verifier was run at capture: all 21 full files reverse exactly to CP83 and 132 source mutations are rejected. The only overlaps with this state proof's frozen dependency closure are **BlockAbsorber and BlockNTMSand, two calls each**. Neither is among this suite's 29 compiled representative files; their exact source/lifecycle closure is still checked, not presented as gameplay execution.

`composition-scheduled-updates.json` pins the complete scheduling manifest, verifier and production-patch SHA256 values and contains only those two dependency records. Its own SHA256 is fixed in the state runner. Explicit `--compose-scheduled-updates` accepts only their exact scheduled revisions and applies positional, whole-file-hash-checked inverses before comparing the original CP83 snapshot. It does not rebaseline the snapshot, bypass other source controls, accept arbitrary drift, or change compiled production sources. Normal isolated runs remain strict CP83-plus-BlockMulti.

The combined proof was rerun against **actual integrated `/workspace/shared/nagaport/ntm-port-1.12.2` sources**, not a copied stub or patched API: **4,230 native assertions pass**. The separate composition guard rejects 16 wrong-target/delay/coordinate, missing-composition and unrelated-source mutations. Logs are `composed-native-test.log` and `composition-controls.log`.

    python porting/tests/block-multi-state-native/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --source-root /workspace/shared/nagaport/ntm-port-1.12.2 --compose-scheduled-updates
    python porting/tests/block-multi-state-native/check-composition.py --source-root /workspace/shared/nagaport/ntm-port-1.12.2

`unlocked-write-audit.md` records the read-only next-step census: 30 direct newly state-capable target sites, eight separately supported alias/this/metadata candidates, and seven mixed sinks requiring additional proof. No write site is migrated by the state patch. The broader 260 four-argument World.setBlock calls remain only a future candidate pool; no blanket dynamic-owner authorization or parity claim follows from their implicit metadata0/flags3.
