# Native scene previews

Finite native callback migration from original NTM, not a RenderBlocks emulator.

Production ownership:
- `GuiWorldInAJar.java`: native scene dispatch, native GUI item/sound/player APIs, state-cache-safe GL operations.
- `GUIScreenSlicePrinter.java`: native live-world access and finite scene dispatch, native data directory/player APIs.
- `RenderOverhead.java`: native tag/thermal/marker buffers and entity queries; native colorless scene preview.
- `NativeSceneBlockDraw.java`: small dispatcher entry, slice destination metadata conversion, and complete native BLOCK → no-COLOR upload.
- `JarScript.java`: only `MathHelper.clamp_float` → `MathHelper.clamp`, preserving the float overload and operation order.

The coordinator-owned `INtmBossDisplay.java` is a compile dependency, not part of this patch. The RenderOverhead import/predicate uses that actual helper to include NTM display bosses and vanilla dragon/wither without changing `isNonBoss`, portals, combat, or the still-pending boss HUD policy.

## Reproduce

From the module, using Java 8 and an existing native Forge 1.12.2 dependency classpath:

    python3 porting/tests/scene-previews-native/run.py --java-home JAVA8 --classpath CLASSPATH

`--compile-only` runs the complete-source native API check without GL. The test requires the sibling `ntm-original-pristine` source tree for exact pristine geometry and transform oracles. No Gradle build or download occurs. The runner discards repository build/classes directories and compiles all listed production inputs afresh. All Minecraft, Forge, LWJGL, native block/model/buffer/client/world/entity classes are actual existing dependency classes, not substitutes.

The complete native callbacks compile alongside real WorldInAJar, JarScript/JarScene, ActorFancyPanel, actor interface, cannery base, GUI item renderer helpers, marker key, direction, clock and boss marker. Four NTM-only collaborators are explicit fixtures:
- BobMathUtil: the unchanged `x + (y - x) * interp` float interpolation expression only.
- Global I18n service: identity text and simple no-wrap output. Translation content/autowrap are not tested.
- PWR owner: native Block/TileEntity data holder with `block` restoration member; PWR reactor mechanics are not tested.
- Wiki screenshot writer: records filename, directory, region, dimensions and chroma key only. The existing wiki screen/writer is not native-complete and is not certified here.

Font glyphs, item meshes and sound playback are recording boundaries implemented as subclasses of actual native classes. No-key keyboard input is injected into the real LWJGL input state; no OS keyboard polling is claimed. The actual native dispatcher/model renderer is called by the capturing subclass; Tessellator uploads are actual GL draws.

## Evidence and preserved behaviors

`native-proof.log` records complete production input SHA256 values, original source oracle SHA256 values and the assertion result.

- 8,806 assertions at this checkpoint; real surfaceless EGL/desktop GL 4.5 Mesa context.
- Real native FaceBakery cube with original `hbm:blocks/concrete` material, actual BlockRendererDispatcher/BlockModelRenderer, native Forge and vanilla light pipelines, AO off/on, raw light integers 0/15/240/0x00F000F0, neighbor face culling, and byte-for-byte equality to direct native dispatcher output.
- Preview COLOR is removed only after native BLOCK lighting; positions, UVs and lightmap bytes retained exactly. Native `noColor()` is deliberately not used: its white upload would erase red/cyan global tint.
- Actual EGL pixels compare colorless native uploads to original no-color vertex emission at both tints and AO modes. This is geometry/upload proof, not a claim that old and native light solvers are equal.
- Whole jar drawScreen, block x/y/z order, block upload before original HashMap actor order, foreground invocation, play/pause/back/forward actual JarScript controls and original click bounds, button atlas rectangles, mixed text/item placement, zero-count item previews and shared renderer z restoration.
- Pristine source transform bodies run as independent GL matrix oracles: 32 varied jar interpolation/rotation/zoom/offset cases, all seven slice directions. Geometry expressions for tag/thermal/marker boxes are extracted verbatim from pristine source and compared to actual uploaded coordinates.
- Slice normalization, whitelist-before-PWR-restoration order, restored source block, all four swizzles and exact destination metadata quirk. In particular, the source block is converted using metadata from live world `(dx, 0, dz)`, not from its source location or saved PWR metadata.
- Original output call arguments and completion notice, slice advancement, deferred preview clearing, per-cell foreign model exception continuation, raw jar light15, original preview flat/no-blend post-state.
- Tag deadmau5 offset, quarter-alpha shadow, distance boundary and foreground/shadow order; thermal self/range exclusions, original class priority/colors, vanilla and NTM boss display classification, five-tick orb alternation and dead-entity black override; native passenger-list tag suppression.
- Marker queue drain and render-before-expire/distance removal, strict `now > expire`, exact line colors/geometry.

## Explicit open boundaries

This removes the legacy API roots, not the full-port acceptance gates. It does not certify full original-vs-native lighting equivalence, all foreign/native model behavior, every NTM registry model, full game launch, screenshots saved by the unported wiki helper, custom fonts/item meshes, shaders, Windows, or the pending CP37 lighting/boss-HUD policies. Native model routing continues to use the registered model and actual/extended states; no global light solver, missing-model geometry replacement, scenario/actor rewrite, shader patch or mechanics change is introduced. WorldInAJar is unchanged, including its original raw `lightlevel` contract. Existing materials/geometry stay with their native registered owners and original assets.
