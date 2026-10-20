# Native snowball renderer factory root

Production scope is exactly the 42 original `ClientProxy.registerEntityRenderer`
`RenderSnowball` registrations. Each still owns the original entity/item pair, in
original order, with native count one and metadata zero. AcidBomb's obsolete
`Items.slime_ball` becomes the native `Items.SLIME_BALL`. No specialized grenade,
mesh, player, mob, or neighboring renderer is substituted or removed.

## Lifecycle finding

The actual pinned Forge 14.23.5.2768 jar invokes the typed factory overload of
`RenderingRegistry.loadEntityRenderers(RenderManager, Map)` **inside the native
RenderManager constructor**. Minecraft assigns its native RenderItem immediately
before constructing RenderManager, and only assigns its global RenderManager
field after the constructor returns. Therefore the factory must use its supplied
manager, never `Minecraft.getMinecraft().getRenderManager()`. The item renderer
lookup is likewise deferred into that factory. The later FMLClientHandler load
is the *legacy map overload*, not the typed factory call.

NTM registers these factories in preinitialization, after its item declarations.
The test additionally registers before any client/item fixture exists to prove
that no eager item renderer access remains. Real RenderManager construction then
creates all 42 renderers. A second real manager with another real RenderItem
proves the callback resolves live owners rather than capturing registration-time
objects. Finally the existing production NativeItemRendererOwnership transaction
successfully replaces all 42 native caches at the load-complete seam. Keeping
exact RenderSnowball class identity is essential to its audited ownership rules.

## Original appearance and ownership

The manifest enumerates all original pairs, the 41 mod texture files, their
unchanged SHA-256 hashes, item classes, metadata and typed CP83 diagnostic headers.
The only mod item classes in this family are ItemGrenade and ItemCustomLore, which
have no icon callback/custom item renderer in this family. The existing native
texture model foundation retains ownership. The acid projectile retains vanilla
slimeball. Six neighboring custom grenade renderer registrations remain intact.

The original 1.7.10 RenderSnowball is found from the supplied MCP class mapping
and inspected in the cached original client jar. Its one-argument constructor
uses metadata zero, and its flat billboard has scale 0.5 and local vertical
baseline 0.25. Native item/generated GROUND uses scale 0.5 and translation 2/16:
its centered [0,1] generated face therefore preserves width/height 0.5 and local
vertical extent [-0.125, 0.375]. Native generated alpha-edge extrusion and native
front-facing third-person camera conventions are retained, not presented as
pixel-identical legacy flat-quad rasterization. No extra scale multiplier is
introduced.

## Evidence and limits

`run.py` first checks pristine source pairs/order, original textures, item/custom
renderer ownership, preinitialization wiring, and the exact lazy native factory
statements. With `--diagnostics`, CP83 attribution is **41 constructor-arity
errors plus 1 obsolete vanilla item symbol**, not a guessed 41-error total.

The Java probe compiles and executes the exact production registration statements
against unchanged native Forge/Minecraft jars, together with the complete
production texture-model provider and existing item ownership dependency slice.
The unrelated application entity class tokens and ModItems slots are controlled
fixture inputs. They are explicitly not substitutes for validating grenade game
logic or compiling the whole ClientProxy.

The probe constructs real Minecraft settings, RenderItem and RenderManager
objects, loads the original mod PNGs plus native vanilla slimeball, stitches a real
128x128 atlas, bakes actual native generated item models through the existing
production provider, and renders all 42 through real RenderSnowball and RenderItem
on Mesa surfaceless EGL. Delegating model observers record the native draw matrices
and GROUND perspective without replacing native renderer APIs. Three camera modes
verify transforms, generated geometry submission, balanced matrix state and no GL
errors. The whole native startup ordering is also checked in actual class bytecode.

Result: **1,145 checks passed**, including actual offscreen GL execution. This is
not a full mod compile, client boot, in-game screenshot, visual acceptance test,
or proof of all entity simulation behavior. No source exclusions, network calls,
proprietary binaries, or whole-build diagnostics are changed by this patch.

## Reproduce from the project directory

Use the cached toolchain and supplied classpath, supplying local Minecraft jars
and the cached original MCP mapping archive. No downloads are attempted.

```
python3 porting/tests/snowball-factories-native/run.py \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --vanilla /workspace/shared/nagaport-toolchain/archives/minecraft-1.12.2-client.jar \
  --legacy-vanilla /workspace/shared/nagaport-toolchain/archives/minecraft-1.7.10-client.jar \
  --legacy-mappings /workspace/shared/nagaport-toolchain/gradle-user-home/caches/modules-2/files-2.1/de.oceanlabs.mcp/mcp/1.7.10/10b95bb342f55d681025bf2d698c893d9c4b4201/mcp-1.7.10-srg.zip \
  --diagnostics /workspace/shared/cp83-full-compile.log \
  --manifest /tmp/snowball-source-manifest.json
```
