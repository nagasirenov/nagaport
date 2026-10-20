# Native conveyor item/package renderer boundary

Baseline: `e03449f2b4042bd79cfe100acaac97743b0078a9`.

## Changes

Only two production classes change: `RenderMovingItem` and `RenderMovingPackage`.
Both invoke the existing real `NativeMachineItemRenderer` with `Display.ENTITY_FRAME`,
not the decoration variant. The shared renderer and `EntityMovingItem` mechanics are
unchanged and hash-checked by the portable probe.

- Moving items retain the original position, entity-ID-seeded `nextDouble() * .0625`
  vertical jitter, flat-item 90-degree X rotation, -.1875 local translation, and
  fast-graphics 180-degree Y flip. Nested yaw and partial ticks remain zero, as in
  the original. The cached `EntityItem` is reused and its hover start is reset.
- Packages retain a single cached one-crate stack, construct their original fresh
  `EntityItem` for each draw in the source world, reset hover, translate Y by -.0125,
  and scale by two. Ordinary entity frame count/bob/spread is not silently replaced
  with decoration behavior.
- Each consumer owns its GL matrix with try/finally, including early/failed model
  classification and native frame callback failures. This is not a guarantee that
  arbitrary third-party callbacks balance their own nested GL pushes.
- Both classes use the native generic Render API, the supplied RenderManager, and
  native Entity.world. No RenderBlocks, global renderInFrame flag, old renderer
  import, RenderManager.instance, or copied renderer implementation remains.

## Orientation ownership audit

The original moving-item orientation is based on ItemBlock and its ordinary
inventory-render shape, not whether an optional custom item renderer is 3-D.
Non-ItemBlocks therefore remain flat even when a native custom wrapper advertises
isGui3d=true. ItemBlocks use their metadata-translated native state to reject
INVISIBLE shapes. FrameBaseModel selects a context-declining base; retained
NativeItemBaseModel ownership bypasses custom wrapper gui3d flags for orientation
without bypassing the custom renderer for the actual draw.

The probe independently reads the genuine native JAR's item JSON parents for 12
representative categories. Torch, redstone torch, lever, ladder, vine, hopper,
cauldron, brewing stand and oak sapling remain item/generated; stone and oak fence
retain block parents; chest retains builtin/entity. These assets support the old
flat-versus-block distinction without reintroducing numeric legacy render IDs.
Unknown external models/resource packs keep their complete native model behavior;
this boundary does not claim universal pixel identity with arbitrary 1.7 models.

The manifest embeds the exact two pristine original class sources and hashes for
portable review, plus hashes of the two current complete consumers and unchanged
entity/helper collaborators. The preexisting frozen native-machine manifest is
also checked, and all of that harness's production classes remain whole.

## Reproduction

From the port project root, with an existing genuine Minecraft/Forge 1.12.2
classpath, Java 8 and existing Linux LWJGL native JARs:

    python3 porting/tests/probe-native-conveyor-renderers.py \
      --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
      --java-home /workspace/shared/nagaport-toolchain/java8

No downloads, installation, source-body slicing, fake Minecraft/Forge classes,
full Gradle build, push, or main-worktree changes were used for this boundary.
The script uses its own location to resolve sources, a temporary compile/run tree,
and the supplied classpath/JDK, so the paths above are only local examples.

## Verified evidence

`tests/native-conveyor-renderer-proof.log` records a successful run using actual
Mesa OpenGL 4.5 and genuine native Minecraft/Forge/LWJGL APIs:

- Both full conveyor renderer classes compile with the real frame helper, native
  model wrappers and all 14 preexisting machine consumers.
- The unchanged native machine regression executable passes its 26 ownership,
  111 complete press/mass-storage, 192 full native machine/GUI, and 41 generated
  model/frame transformation checks.
- 2,134 conveyor assertions pass. Tests invoke complete production doRender bodies
  and observe actual model callbacks, RenderItem/TEISR calls, native cube getQuads
  matrices, and real framebuffer pixels. The expected outer transforms are
  independently replayed from the pristine consumers, never extracted from or
  substituted for the production bodies.
- Coverage includes fancy/fast graphics; multiple fixed entity IDs and repeat
  draws; differing incoming yaw/partial values; retained flat custom ItemBlock
  bases; FrameBaseModel selective wrappers; native custom TEISR routing; empty
  stack/absent cache; original counts at each block threshold and icon threshold;
  age-zero and age-71 bob/rotation; per-copy seeded spread; ItemBlock metadata
  mapping and INVISIBLE state; original stack object/count/damage/enchantment/NBT;
  cached package stack/fresh entity; package scale/offset/bob; and failure matrix
  unwinding. No GL errors remain after the complete draw suite.

## Explicit limits

This is native renderer boundary proof, not full-game acceptance. The NTM
EntityMovingItem collaborator is a declared storage-only fixture extending the
real native Entity. Real native EntityItem, ItemStack, NBT, RenderItem, model mesher,
textures, buffer/tessellation, and GL are used. Conveyor movement, synchronization,
inventory transfer and server mechanics are not reimplemented or retested here.
The crate registration fixture is backed by native Blocks.STONE to exercise the
ordinary block draw/caching path; actual ModBlocks registration and crate artwork
remain game-level acceptance work. Existing machine fixtures and their limitations
are unchanged. The full source-tree compiler result belongs to the coordinator's
combined snapshot, not this proof.
