# Native machine item previews and world GUI

## Frozen scope

`native-machine-item-scope.json` lists the exact 29 production files and hashes.
The contextual RenderItem owner/installer is a separate, coordinated slice. This
slice never installs another global renderer and never caches a RenderItem or a
RenderEntityItem constructed during pre-init. ItemRendererHot has exactly one
base-draw call adapted; its removed IItemRenderer root is still unported.

The 14 complete TESR consumers are Press, EPress, SolderingStation, OreSlopper,
MassStorage, DecoBlockAlt, ArcWelder, PistonInserter, VacuumCircuit,
AssemblyFactory, AssemblyMachine, PrecAss, SkeletonHolder, and PedestalTile.
Their native render signatures, item/world/position accessors and complete
BufferBuilder draws compile against genuine Forge 2768. No fake platform classes
or broad TESR adapter is used. Machine storage, recipes, resource mesh draws and
other NTM collaborators are explicitly listed test fixtures, not full-mod proof.

## Preserved ownership and branches

- RenderItemStack deliberately renders twice, first with the original animation
  scale and then without it. Effect and no-effect remain different paths. Its
  former dedicated renderer's zero GUI depth is local and restored on return.
- Forge 1.7.10's renderItemIntoGUI bypassed custom IItemRenderer dispatch, whereas
  renderItemAndEffectIntoGUI tried the custom renderer first. The no-effect path
  therefore resolves the original full baked model, not its particle sprite.
  Immutable NativeItemBaseModel ownership is captured at native model bake and
  preserved through holder/perspective copies and resource reloads. A genuinely
  absent original entry uses the native ModelManager missing model, as before
  replacement. No stack/NBT stripping is used to suppress enchant effects.
- The world GUI retains generated-icon pixel placement, per-layer tint, flat
  two-pass glint with polygon offset, and the original centered cube placement.
  Ordinary cube branches have no generic enchant glint. Fancy custom items use
  the current installed native renderer; non-fancy uses the retained base.
- DECORATION_FRAME means the original RenderDecoItem: one item, no bob or spread.
  ENTITY_FRAME means the original normal RenderManager call: original bob,
  cube/flat copy thresholds 1/5/20/40, fancy icon thresholds 2/16/32, seed 187 and
  original spread/RNG consumption. No global renderInFrame state remains in these
  consumers. The original custom non-BLOCK_3D callbacks still draw one copy.
- Generated icons are recognized by comparing every native generated front,
  back and side quad, not only a sprite or a front face. Unknown custom geometry
  remains complete on the native GROUND route. The model cache accepts only
  native immutable BakedItemModel and has weak keys for resource reload release.
- MassStorage keeps the original stock label thresholds, font/shadow layout,
  direction transforms, gauge vertices/color and local GUI depth. Its raw GL
  attrib pop was reproduced restoring rescale-normal behind GlStateManager's
  cache, making the following native disable a no-op. Only this class's boundary
  was corrected: native entry rescale/normalize setters and reconciliation of
  touched enable, individual-light, alpha/blend, color-material, shade and color
  caches after the raw pop. Original restored GL values are not replaced with
  arbitrary defaults. The frozen contextual seam was not changed.

The source oracle was the pinned original NTM implementation, official cached
1.7.10 RenderItem bytecode and Forge 1614's RenderItem/ForgeHooksClient patches.
In particular, those patches establish custom/base GUI dispatch, per-pass glint,
shouldBob, mini-count overrides and the otherwise-unused Z RNG consumption.
Native platform adaptation uses IBakedModel.isGui3d and BlockRenderLayer instead
of removed render IDs/sprite-atlas numbers. Arbitrary third-party models and
custom renderer replacements are not asserted to have original visual parity;
no unsupported model is reduced to its particle sprite.

## Terminal native evidence

Run `probe-native-machine-items.py` with the existing native classpath file and
Java 8 toolchain. It downloads/installs nothing and needs no Git or cloud history.
The full terminal output is
`porting/continuation-2026-10-19/native-machine-items-FINAL.log`.

- All 14 complete production TESR classes compile against the genuine native jar.
- 41 real generated-model ownership and actual GL frame-matrix checks.
- 72 genuine RenderItem/mesher/TextureManager/framebuffer draw checks. Enchanted
  no-effect pixels equal a genuine native unenchanted draw; effect changes actual
  pixels. Both intentional draw counts, zero-depth ownership, actual cubes,
  custom/base split, no recursive redraw, unchanged stack/NBT and replacing the
  runtime renderer after machine construction are tested. The actual production
  ContextRenderItem/ContextItemModel route receives GUI and GROUND, not TEISR NONE.
- 26 actual owner bake/reload/original-model identity checks, including override
  and perspective copies. These invoke real event methods, not a new event-bus
  dispatch claim; the existing event/ownership suites cover that separately.
- 111 complete press/mass-storage draw checks with native GL and native fonts,
  covering animation matrices, source stack, all directions and label boundaries,
  plus subsequent native enable/disable/color/alpha/blend/light calls after the
  MassStorage raw attrib scope. This is not merely comparing raw GL values.
- Existing helper regression runs passed: callback 42, direct-mesh 531,
  native quad/overlay 64; manual panels still compile with declared NTM fixtures.
  Runner changes only add the real shared interface/quad helper dependency.

The tests use Mesa EGL and the already cached native LWJGL jars, genuine native
client/model/texture/render constructors, native framebuffer pixels, and synthetic
controlled item geometry/textures. They do not certify all resource-pack visuals,
all 14 machines in a running game, arbitrary third-party rendering compatibility,
or full-mod/game/radiation acceptance.
