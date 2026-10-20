# Native TESR callback signature root

Baseline: sealed CP70 `ea933e28fe3a1a9a60e9d438411a02182b2ab22a`.

Production scope: 230 exact `public void renderTileEntityAt(TileEntity,double,double,double,float)` declarations become native `render(TileEntity,double,double,double,float,int,float)`; one direct Crucible actor call becomes `render(..., interp, -1, 1.0F)`. No exclusions. Every selected callback body is byte-identical, including whitespace. No rendering logic, mesh, texture, lighting, GL state, alpha scaling, destruction overlay or early return is introduced. The two unused parameters use `nativeDestroyStage` and `nativeAlpha`, identifiers absent from the entire original selected files, so existing body locals or field references are not captured. Raw generic inheritance is retained, not widened or narrowed.

214 classes directly extend raw native TileEntitySpecialRenderer; ten inherit through RenderTurretBase and six through RenderPylonBase. Both intermediate bases are raw native TESRs and have no legacy callback declaration. Whole-source javac attribution checks native erased subtype identity and `Elements.overrides` for all 230 methods. No selected class has a source descendant, and none has an existing `render` overload. Similar-name Press/EPress `renderTileEntityAt2/3` helpers are outside the exact family and unchanged. Existing typed/native callbacks, including RenderFoundry and the approved RenderFoundryFluid destruction overlay, are unchanged byte-for-byte.

## Engine evidence and semantic boundary

The original client `bml.a(aor,DDD,F)` is joined.srg `TileEntitySpecialRenderer.func_147500_a`. Original `bmk.a(aor,DDD,F)` resolves the renderer and invokes that virtual callback with unchanged tile, render-relative coordinates, and partial ticks. The native dispatcher performs the analogous virtual call to `TileEntitySpecialRenderer.render(TileEntity,DDD,F,I,F)`.

Native dispatcher five-argument convenience rendering delegates to its alpha overload with `1.0F`; that overload delegates to the full callback with destruction stage `-1`. The full dispatcher forwards both extra arguments directly. This is the evidence for the Crucible actor's normal, non-breaking context. It is not a license to multiply existing colors by alpha, implement crack textures, skip breaking calls, or call the base renderer and add nameplate behavior. Existing rendering stays unchanged. Normal/destruction/alpha arguments are tested at the genuine engine dispatch boundary, not in unresolved production renderer bodies.

## Offline replay

Run `python3 porting/tests/native-tesr-callbacks/run.py JAVA_HOME CLASSPATH_FILE LEGACY_CLIENT_JAR JOINED_SRG_ZIP OUTPUT_DIRECTORY` from the patched module. Supply the already-resolved genuine native classpath and Java 8 JDK (including tools.jar). No downloads, installs, source exclusions, production stubs or alternate runtime dependencies. The runner extracts source from the recorded git commit, reconstructs every selected file using exact edits, checks every untouched source file, attributes all 4,064 source units before/after, and runs genuine original/native dispatchers. Absolute input paths are runtime parameters and are not embedded in the artifact.

`DispatchParity.java` deliberately uses bounded recording callback fixtures, not replacement production renderer implementations. Real original and native engine classes execute their actual dispatcher logic. Fixtures cover typed, raw and inherited callback signatures; tile subclass lookup; four partial ticks; negative/fractional coordinates; normal opacity; and stages -1/0/5/9 with non-default alpha. 336 assertions passed. Legacy archive class bytes are copied unchanged, with META-INF signatures omitted solely to let unsigned default-package probes coexist with the original obfuscated classes.

Whole-source attribution: 4,064 parsed units, zero parse errors both ways; 14,612 baseline errors become 14,382. Exactly 230 `compiler.err.method.does.not.override.superclass` diagnostics disappear, no diagnostic is added, and all 230 selected methods now override the actual native target. All 230 body strings extracted by javac match byte-for-byte. Crucible's direct call attributes to its new seven-argument method.

This finite root restores callback linkage only. The 14,382 remaining whole-source errors include unresolved rendering bodies and unrelated APIs. No claim of full compilation, successful game launch, visual parity, or complete renderer migration is made. The unchanged foundry overlay is protected by full-source equality rather than rerunning or claiming its independent GL visual tests.

On a later integration checkpoint containing unrelated edits (e.g. POSITION_TEX conversions), apply the production patch normally with contextual hunks, never replace complete files from the manifest. Exact reconstruction replay intentionally requires CP70 plus this patch; later integration requires its own whole-source build and source-family audit.
