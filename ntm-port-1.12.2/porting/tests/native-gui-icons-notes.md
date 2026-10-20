# Native GUI item boundary and icon extrusion (checkpoint 25)

Baseline: checkpoint 24, `13c8930411cbff2653a9f75e9818c8af2b900394`.

Two complete consumers, QComponentLink and ActorFancyPanel, now use the registered native GUI item renderer, native sound/screen APIs and BufferBuilder. NativeGuiItemDraw restores the shared renderer's zLevel even on draw/overlay exceptions. Null/EMPTY draws are skipped. A nonempty-item zero-count stack gets a presentation copy of count one and a zero overlay; the source stack, tags and capabilities are not mutated. A count-sensitive custom renderer sees this presentation copy, not the original zero count. Arbitrary malformed negative-count parity is not claimed.

NativeItemQuadRenderer is an unactivated building block for later no-glint paths: native model face order/seed, tint/anaglyph and Forge quad output without automatic glint. It does not yet implement model selection, GUI transforms, custom TEISR recursion protection or the complete old RenderItemStack/RenderDecoItem routes.

NativeIconExtrusion replaces all ten calls to the removed ItemRenderer.renderItemIn2D in six existing consumers. Original UV, size and depth arguments are retained. Six face groups are emitted into one native POSITION_TEX_NORMAL buffer, preserving ordered position/UV/normal values and current GL color. The six entire consumers still have other unported APIs; these call-site edits are not a claim that their full classes or visuals work.

## Reproduction

Run from ntm-port-1.12.2 with Java 8 and the genuine Forge/Minecraft dependency classpath. Existing toolchain and AE2 prerequisites are in continuation-2026-10-19/RESUME.md. These Python runners accept --java-home and --classpath:

- `probe-native-item-quads.py`: 64 checks against native RenderItem's actual private quad method, plus controlled renderer callbacks testing overlay ordering, source identity/count/NBT and z restoration. Uses cached Linux LWJGL and a genuine offscreen Mesa/EGL context only to initialize native texture code. Does not draw the GUI or select a real item model. Not a Windows GL runner.
- `probe-native-manual-panels.py`: complete six production classes compile against native APIs. Six explicit NTM navigation/world/localization fixtures, not replacement Minecraft/Forge classes. Not a complete tutorial/manual runtime test.
- `probe-native-icon-extrusion.py --legacy-client PATH`: 75,445 value comparisons between real native BufferBuilder output and the original 1.7.10 extrusion method. The original bytecode is copied with only its vertex-output sink redirected to a recorder; this is not whole legacy-engine execution. Requires official client SHA256 a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce, not bundled. No GL draw is claimed.
- `check-native-gui-icons-source.py`: exact reversible eight-file changes plus three retained new sources and 33 drift rejections, composed ahead of previous historical source guards. Source identity is not behavior proof.

No full mod build, JAR, client/server boot, world/save, machine operation or visual acceptance. Do not turn these selected probes into whole-port readiness claims. The separate unactivated machine-item renderer draft is intentionally not part of this checkpoint; its model ownership, frame and glint behavior still need checks.
