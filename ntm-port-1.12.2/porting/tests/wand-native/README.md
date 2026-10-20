# Native wand structure-marker proof

This isolated slice replaces only BlockWand and RenderBlockWand. The pristine
registry contains one actual owner, wand_air, whose exportAs is vanilla AIR.
There are no original descendants. No placeholder/shim/CE implementation is used.
No full Gradle compile is performed by this runner.

## Integration

Copy the four Java production files in this slice, plus
assets/hbm/blockstates/tile.wand_air.json. The included original wand_air.png is
byte-identical and need not be overwritten in the main tree. Reuse the existing
ConveyorCuboidGeometry helper, which this runner compiles and independently
checks against all six original face methods.

The exact two shared-file byte replacements are supplied in
porting/wand-native-shared-edits.json. The coordinator may apply them with
porting/apply-wand-native-shared-edits.py MODULE. The existing register(wand_air)
call is untouched. Block and item registration remain deferred; no ItemBlock is
constructed in the owner, declaration, model or event handler.

## Run

python3 porting/tests/wand-native/run.py --module MODULE --java-home JAVA8 --classpath CLASSPATH --legacy-client ORIGINAL_MINECRAFT_1710_JAR --draw-order

In this isolated overlay, additionally pass --base-module MAIN_MODULE so the
unchanged helper and RefStrings are taken from the current main source tree.
--original PRISTINE_SOURCE is optional; otherwise the pinned bundled source
archive is used. The original client JAR is separately hash verified and is not
bundled. No download or added dependency is needed. --draw-order additionally
uses the already-installed Mesa desktop OpenGL through surfaceless EGL.

## Evidence and scope

- Complete real native owner, model, handler, helper and registry APIs, with zero
  NTM collaborator stubs. The native World/Chunk path stores all sixteen legacy
  metadata values, has no tile or collision, retains the inset selection bounds,
  glass material, default sound/hardness/drops and export-as AIR identity.
- Same-owner-only side suppression is checked for all 64 neighbor masks, all
  metadata, with air, opaque stone, glass and distinct BlockWand instances.
- Pinned pristine source and the actual original Minecraft 1.7.10 face bytecode
  form the independent oracle. Six faces x two inside/outside passes x sixteen
  metadata match exact float XYZ/UV and winding, up to the helper's documented
  cyclic native-AO ordering. Inside rendering reflects only the in-plane position
  axis. It does not reverse UVs or invert the original outward light normal.
- Actual Forge and vanilla native flat/smooth renderers consume the models.
  Same-owner culling, output count, finite positions and asymmetric AO response
  are checked. The uniform original directional shading is visibly retained;
  native Forge rounds the down-face channel to 0x80, versus vanilla 0x7f.
- The real ModelRegistryEvent, model loader, state mapper, item model mesher and
  BlockModelShapes are exercised from folder and JAR resource packs, including
  reload and wrong-owner/class/export rejection. Item rendering uses the real
  native ItemLayerModel and original PNG, not the world cube.
- Actual Mesa OpenGL RGBA8 and depth buffers compare the native per-side
  inside/outside batching against global inside-first/outside-second batching.
  Streams are exported from actual native renderer invocations, not generated
  from production source by the raster test. Inside/outside/near/distant cameras,
  representative side masks, flat/smooth, uniform/asymmetric vertex light,
  nearest/linear/mipmap sampling are covered. A controlled lightmap multiplier
  isolates order, rather than claiming to reproduce Minecraft's lightmap.

## Layer choice

The original PNG has 112 fully opaque and 144 fully transparent texels. Legacy
pass 0 renders with alpha test enabled; native SOLID explicitly disables it.
CUTOUT_MIPPED is therefore intentional: it preserves alpha-test, no blending,
normal depth writes and the legacy terrain atlas's configurable mipmaps.
Both engine versions use alpha GREATER 0.5 during terrain, then lower it to 0.1
after terrain. See layer-bytecode-notes.md. This is a native API mapping, not a
content change or move to translucent rendering.

## Explicit unresolved gates

The old complete renderStandardBlock flat/AO light solver was not executed and
compared to the native solvers. Geometry/UV/visibility equality and working native
lighting are not an ordinary nondefault-light parity claim. Native Forge's
known 127-versus-128 uniform down-face quantization is also not hidden. This
slice does not accept any user policy for lighting changes, and does not resolve
or borrow approval from the unrelated CP37 plant lighting gate.

Damage models are separately checked for native retexture data and face count.
For this specific wand fixture, uniform Forge flat damage and ordinary lighting
matched on all 48 vertices. That does not close CP44's existing cross-family
damage-light discrepancy or prove nondefault damage lighting parity.

The ordering framebuffer test is genuine controlled OpenGL rasterization, not a
live Minecraft screenshot or a shader-pack assertion. Full-mod compile, launch,
world integration, server distribution and live-game visual acceptance remain
coordinator/integration gates. No blanket parity or complete-port claim is made.
