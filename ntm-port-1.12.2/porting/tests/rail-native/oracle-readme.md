# Independent original-renderer rail geometry oracle

This proof covers every original `RenderRail` consumer: eleven custom rail owners
and `MachineFunnel`. The vanilla `RailGeneric`, booster, wood, and high-speed rail
families do not belong to this renderer. No production source is changed here.

## Reproduce

From `ntm-port-1.12.2`:

    python3 porting/tests/rail-native/oracle-run.py \
      --original-root /workspace/shared/nagaport/ntm-original-pristine \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /tmp/nagaport-ae2-native-classpath.txt

No Gradle invocation, game/client launch, network request, download, or legacy
renderer in production is required. The test compiles the five actual native
production classes against the cached real Minecraft 1.12.2/Forge classpath.
Recording stand-ins exist only inside the generated original oracle, never in
production or as replacements for the native Minecraft/Forge classes under test.

`oracle-build.py` verifies 38 original SHA-256 pins and the 20 byte-identical
production assets, and extracts these original methods without changing their
bodies:

- Both `RenderRail` dispatch methods.
- All 24 owner `renderWorld`/`renderInventory` methods.
- All six whole/part `ObjUtil` render overloads.
- `S_Face.calculateFaceNormal`.

Only surrounding dependencies are recording stand-ins. `ObjUtil` calls enter a
small recorder and delegate to the exact original bodies. An independently written
source-order OBJ reader supplies original float coordinates, flipped V coordinates,
and the source-extracted original face-normal implementation. It never calls the
production OBJ parser, mesh geometry, selectors, or transform routines. Legacy
`Vec3` arithmetic uses float `MathHelper` table values promoted to doubles and the
legacy float square-root normalization. That rotation convention has also been
checked against the actual cached original client by the pre-existing atlas proof.

The manifest records each owner's exact original registration, mesh and texture
selection, GL operations, primitive counts, and bounding coordinates, as well as
all relevant source and asset hashes. The test's native enum mapping chooses only
which production renderer to test; expected metadata logic, sign selection,
transforms, UVs, colors, and vertex streams are determined by the original source.

## Coverage and acceptance

- Twelve owners x sixteen metadata values x world/inventory x four tile cases:
  1,536 states, including missing tile, straight switch, turned switch, and wrong
  tile type. The two absence cases both map to native sign state zero.
- 1,703,808 ordered emitted vertices, all primitive counts and material order.
- XYZ, atlas UV, and packed RGB are bit-exact for both world and inventory.
- Real native BLOCK/ITEM vertex formats, untinted quads, and diffuse flags.
- Balanced original tessellator translations and original own-block brightness.
- Inventory normals rotate the original signed-byte normal, preserving its
  quantized magnitude. Native baked normals require a second byte encoding after
  the original GL rotation: the accepted error is at most one signed-byte step
  (1/127 per component, plus 1e-5 of that step for float representation).

The extra native normal byte encoding is visible and quantified, not an exact
lighting claim. The test does not prove GPU matrix/driver precision, GL normalize
or rescale-normal state, raster output, item camera/display presets, atlas reload
lifecycle, registration, client/server behavior, or the native world lighting
pipeline. CP37 lighting approval remains pending; this is not a silent visual
waiver and does not authorize a change in behavior or balance.

## Original geometry facts and parity pitfalls

All twelve mesh assets are entirely triangulated, so the original third-corner
duplication makes one degenerate quad per face. No asset in this scope exercises
whole-OBJ's unusual five-emitted-vertices-per-source-quad behavior; it should not
be "corrected" in the shared helper.

| Asset family | Faces / emitted quads |
| --- | ---: |
| Narrow straight | 80 |
| Narrow bend | 232 |
| Standard straight | 248 |
| Standard short | 56 |
| Standard bend | 312 |
| Standard wide7 bend | 440 |
| Standard wide9 bend | 632 |
| Standard ramp | 348 |
| Standard buffer | 372 |
| Each standard switch | Rail 1,260; SignStraight 22; SignTurn 22 |
| Funnel | Top 2; Bottom 2; Side 16 |

- Narrow straight and funnel render all metadata 0–15. Other owners are invisible
  in world for metadata below 12, but still render their inventories.
- Narrow straight yaw is 90 degrees only for 2, 3, 12, 13. Narrow bend maps
  12→90, 13→270, 14→180, 15→0. Standard straight/short use yaw 90 only for 14/15.
  Remaining standard shapes use 12→180, 13→0, 14→270, 15→90.
- Straight, short, ramp, buffer, and switches add half-block offsets for metadata
  12–15. Curves and narrow straight do not. All world models then translate by
  the block center in X/Z but not Y.
- A switch inventory is only its `Rail` group. A world switch sign is emitted
  only when the tile is the original `TileEntityRailSwitch` class. Flipped rails
  use a distinct sign texture, not a mirrored generic switch texture.
- Funnel uses `Top`, `Bottom`, `Side` in that draw order, despite source OBJ order
  being Bottom, Top, Side. Its particle/side texture cannot replace its top/bottom.
- Whole shader is max(.45, (ny+.7)*.9 - abs(nx)*.1 + abs(nz)*.1). Part shader is
  max(.45, ny*.3+.7 - abs(nx)*.1 + abs(nz)*.1). Both then multiply by 255,
  truncate to integers, and clamp to [0,255]. Inventory uses unshaded white.
- UV multiplication is float * 16 for whole mesh, double * 16 for named parts;
  the original V flip and uncontracted atlas interpolation must be retained.
- Item GL calls postmultiply. Thus large bends are S*Ry45*Rx60*T; narrow bend is
  S*T; straight/ramp/buffer are T*Ry90*S. Translation before or after a scale is
  not interchangeable. The unflipped switch has inventory Z translation -.1875;
  flipped switch has zero. Funnel translates Y by -.5, not -.0625.
- Native world sampling/diffuse suppression is a separate proof obligation.
  Testing only front-face geometry or assuming ordinary directional AO would
  miss the original dispatcher’s own-block packed-brightness contract.

## Reuse

Import `generate(original_root, native_root=None)` from `oracle-build.py` and compile
its returned `OracleRailOriginal.java`. Call `load(originalResources)` once and
`render(ownerClassName, metadata, inventory, tileKind, spriteMap)` for expectations.
The sprite map uses full atlas IDs such as `hbm:blocks/rail_narrow_neo`.
`Result.vertices` contains original-source-derived XYZ/UV/color/texture/normal and
brightness data; `Result.calls` exposes the independently recorded model/group/
material/yaw/translation boundaries if testing a narrower helper contract.

## Mutation sensitivity

`oracle-mutations.py` takes the same arguments as `oracle-run.py`. It compiles eight
intentional production defects only in temporary directories: narrow yaw, dummy
visibility, inventory matrix, sign selection, funnel material, whole-mesh shader,
triangle duplication, and inventory normal rotation. Every variant must fail an
assertion in the independently generated original oracle. In particular, forcing
`SignStraight` for the switched state is first caught by exact UV comparison,
not merely by primitive counts. The retained result is `oracle-mutation-proof.log`.
