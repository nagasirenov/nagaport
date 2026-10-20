# Native connected-texture geometry proof

Run from any directory, with local Java 8, the real cached 1.12.2 Forge/Minecraft compile classpath, pristine original source checkout, and cached official 1.7.10 client/SRG archive:

```sh
python3 porting/tests/ct-native/probe.py \
  --java-home "$JAVA8_HOME" \
  --classpath "$NATIVE_MINECRAFT_CLASSPATH" \
  --original-root "$ORIGINAL_1710_SOURCE" \
  --legacy-client "$ORIGINAL_1710_CLIENT_JAR" \
  --legacy-srg "$ORIGINAL_1710_SRG_ZIP"
```

The probe never downloads artifacts, builds the entire mod, modifies production, or installs fake Minecraft/Forge classes. It compiles only the real production CT model/context/geometry and their native geometry helper, then runs native Forge/Minecraft APIs in a headless JVM. All original source and legacy binary inputs are SHA-256 pinned in `original-manifest.json`.

## Independent oracle

The generated original-source oracle executes the pristine neighbor orientation tables, corner truth table, four-quadrant selection expressions, IconCT constructor arithmetic, six face coordinate bodies, subdivision, and vertex order. Only API bindings change: ForgeDirection becomes EnumFacing, original icon bounds are backed by real native sprites, and a plain test-local recorder receives the original vertex calls. The world-geometry oracle runs with original AO disabled; it does not pretend that a recorder replaces the engine light solver. No production geometry/selection calculation is reused to generate the expected result.

The inventory proof separately loads and executes the actual cached official 1.7.10 RenderBlocks and Tessellator classes. A Java proxy implements the actual old IIcon interface. Unsafe only skips the game singleton renderer constructor; the original six face methods and actual tessellator raw buffer execute unmodified. Names are checked against the pinned original SRG archive. The original CT inventory source pins the +90-degree Y rotation and six superclass face calls. Native inventory quads must match the resulting positions, UV float bits, outward normals and cyclic winding.

## Coverage

- All 256 eight-neighbor masks, all six original face orientations, all four quadrants and all twenty fragment regions.
- Real native IBlockAccess capture, lexical coordinates, default identity connectivity, immutable context snapshots, defensive input-array capture, material selection and extended-state publication.
- Both material layers, two atlas layouts (including non-power-of-two asymmetric dimensions), direct native quad generation and real IBakedModel access.
- Exact original position and UV float bits, parent-sprite identity, fragment ordering, normals, winding, four unsimplified quads per world face and six unsplit inventory faces.
- Real Forge and vanilla flat/smooth chunk rendering, asymmetric native light response, all-face culling against solid neighbors and no duplicate general-bucket geometry.
- Actual Forge Flat/Smooth full-corner samples from an independent native cuboid reference, with midpoint/center expected values calculated by the pristine original VertInfo.avg body. Tests cover every mask/face and parent restoration after normal operation and an injected corner-sampling exception.
- Actual native SimpleBakedModel destruction builder and BakedQuadRetextured execution through both renderers. This smoke test explicitly measures the loss of the CT light wrapper; it does not approve that difference.
- Eight parallel request streams, each with 1,024 independent six-face contexts.

The asymmetric atlas fixture found and prevented a one-ULP regression caused by replacing IconCT's sequential min/max arithmetic with getInterpolatedU/V. The test remains bit-exact rather than weakening its tolerance.

## Explicit limits

The complete original 1.7.10 RenderBlocks AO/light solver is not compared against the complete 1.12.2 solver. Native lighting invocation, fragment interpolation checks, and original geometry parity must not be described as full old/new lighting or pixel parity. The vanilla fallback has its own native fragment-light behavior and is not evidence of matching original midpoint/center interpolation.

The native destruction-builder smoke test confirms that BakedQuadRetextured removes CTLightQuad and therefore bypasses its original midpoint/center interpolation. This is a measured open acceptance gate, not an approved substitute. Crack-texture visual parity, original destruction-overlay behavior, resource lifecycle, owner registration, and end-to-end gameplay screenshots are not established by these tests. Owner/loader/resource integration is covered by the separate CT integration probe when supplied.

## Owner and loader integration

Run `owners.py` with `--java-home`, `--classpath`, and `--original-root` as above. It compiles the complete native BlockDecoCT, BlockNTMGlass/CT, BlockHadronCoil, WireCoated, ITooltipProvider, CT model loader and CT rendering classes. It extracts only the native CT render hooks and unchanged connectivity predicates from BlockICF/BlockPWR into explicitly bounded native owners; it does not compile or certify the full reactor classes. The registry table fixture, coated-wire tile constructor endpoint, and localization dispatch are explicit NTM-only collaborators. No Minecraft or Forge replacement classes are installed. The fixture omits the already-separately-tested legacy-metal sound setter when constructing test blocks.

The integration probe verifies 133,065 assertions: all30 original registered variants and sixteen metadata values, exact mixed steel/hadron/controller joins, metadata1 port material selection, glass culling/full collision/layer/drop contracts, coated-wire tile type, hadron factor tooltip, actual native loader/480 world-state and480 item metadata registrations, original asset availability and wrong-owner rejection. All64 original full/connected PNGs are byte-identical. Exact unchanged mechanics methods and complete ICF/PWR nested-tile bodies are compared to pinned CP48 source in the supplied small ZIP; this is a source-invariance check, not native reactor gameplay proof.

Shared ModBlocks/ClientProxy fragments are recorded in ../../ct-native-shared-edits.json. Original default ItemBlock versus ItemBlockBase registrations are retained. The paired 30 base and two port material resources are declared directly to the native model loader; per-stitch global fragment receivers are no longer used by these owners. Existing dead legacy icon utility classes are not repurposed as engine facades.

Additional visual/runtime gates remain open: no client/GL screenshot, complete item draw or inherited inventory GL-current-color comparison, dedicated server, world-save, energy/reactor integration or foreign lighter subclass proof. Original CT world rendering ignored destruction texture overrides; native destruction changes both that material policy and wrapper lighting. It is not approved or silently accepted. No tile entity was added to previously tile-less blocks to work around it.
