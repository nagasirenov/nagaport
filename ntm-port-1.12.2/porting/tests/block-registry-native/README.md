# CP51 native block-registry construction batch

Base: `077492a6c31c910f34697783f1aa0cfabd8ff88e`. Production scope: only
`com/hbm/blocks/ModBlocks.java` and the explicitly approved `BlockBase.java` owner.
No registry queue, existing native family renderer/model, asset, other production
owner, build source selection, or toolchain change is included.

## Implemented

- Audit all 834 legacy naming setters. Map the 647 receivers that inherit native
  Block naming to `setTranslationKey`, but retain all 187 genuine NTM
  `BlockBase.setBlockName` calls and their existing texture/registry side effects.
- Map 643 zero-argument Block name getters, one BarbedWire setter, 557 Material
  constants, six MapColor constants, four exact vanilla FARMLAND references,
  and ten sound constant kinds. Keep all seven Forge Fluid naming setters.
- Original metal uses stone samples at pitch 1.5, not native METAL: use the
  already-existing `ModSoundTypes.legacyMetal`. Piston maps to STONE, grass to
  PLANT, and gravel to GROUND, as verified against archived 1.7.10 bytecode.
- Map thick-foliage piston mobility 1 to `EnumPushReaction.DESTROY`. Keep its
  burnability/tool requirement, liquid materials, and corium immovability.
- Expose the real protected native `setSoundType` through a public covariant
  override on the actual NTM `BlockBase` owner. Preserve original setter order
  at 156 construction sites using audited receiver casts, not reordered chains.
- Restore original explicit texture assignment in that owner. Its actual atlas
  callback now uses the assigned texture, not the unrelated `hbm:tile.*` registry
  key. Translate the old block-only atlas root to the native shared atlas:
  `hbm:foo` resolves to `hbm:blocks/foo` and `textures/blocks/foo.png`.
  Preserve the previous registry fallback for cases with no assigned texture;
  no new broad compatibility API is added.

## Evidence and exact limits

Run from the project root:

```sh
python porting/tests/block-registry-native/run-native.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
  --legacy-jar /tmp/nagaport-legacy-client.jar
```

`native-test.log`: **PASS 10,197 checks**, real Forge 14.23.5.2768/stable_39 and
Java 8, with Mesa offscreen OpenGL. No downloads or altered Minecraft/Forge types.

- Twelve complete production files are compiled, including the genuine queue,
  BlockBase, BlockGeneric, icon adapter/wrapper and complete atlas event handler.
- Exactly seven construction statements and their seven registration statements
  are extracted verbatim from the changed ModBlocks source into the explicitly
  named `ModBlocksConstructionSlice`, alongside six exact material fields.
  It is NOT a replacement ModBlocks class, whole-source compilation, or a claim
  that excluded/unported owners compile. The only NTM fixture provides
  MainRegistry's logger and Random; no content behavior or platform API is stubbed.
- Actual native Block and Item registry events flush the unchanged production
  queue and populate Forge's real Block-to-Item mapping. Test the original
  `hbm:tile.*` names, standard ItemBlock choices, configured hardness/resistance,
  hidden creative-tab state, and sound/texture independence.
- Genuine TextureStitchEvent.Pre dispatch invokes the complete production handler
  and BlockBase callback. Actual TextureMap decoding, mip generation, stitching,
  GL upload and readback reproduce all original pixels for structure_anchor,
  crystal_hardened, block_deprecated (four blocks), and pink_planks. Repeat after
  resource reload and check wrapper replacement and shared-texture deduplication.
  The event is posted directly because the whole-client dynamic-bucket bootstrap
  in ForgeHooksClient needs a complete Minecraft resource-manager initialization;
  this test is not a whole-client startup or an in-game rendering claim.
- Archived 1.7.10 client bytecode independently verifies eight behavior flags and
  piston mobility for 19 materials, RGB/index for six explicit MapColor values,
  and the ten old sound constant kinds' sample labels, volumes and pitches.
- JavaCompiler parses the complete changed production files under Java 8 syntax.
  This is separately labelled syntax-only, not dependency attribution/compilation.
- `source-test.log` and reversible source manifest prove exact CP51 recovery,
  original CRLF preservation, constructor/string/numeric values and full fluent
  evaluation order, all 651 direct registry calls/ItemBlock choices, all 676
  texture assignments, and the unchanged real registry queue.
- `texture-resource-audit.json`: 145 assigned base-owned texture statements,
  129 standalone PNG references byte-identical to pristine. The 16 remaining
  names are existing variant stems handled by specialized callbacks, not missing
  assets invented by this patch; those callback owners remain pending.

## Honest remaining work

- Full ModBlocks dependency/semantic compilation and whole mod/client/server
  startup are **NOT_RUN** here. The coordinator owns the real full-source compile.
- 531 non-BlockBase explicit texture calls and 148 old sound setters remain.
  `pending-owners.json` records 590 statements across 373 actual owners, including
  existing non-base native sound setters whose protected access needs audited
  owner-level adaptation. No texture setter was simply removed.
- Specialized enum/multi-side/TNT/CM icon callbacks are not falsely covered by
  the seven ordinary blocks' atlas result; they still need their own native atlas
  path and runtime proof. Existing native handlers were not changed.
- Native material default map-color differences are real and **not** counted as
  parity success: WOOD 6837042 -> 9402184; GROUND 12020271 -> 9923917; CLOTH
  10987431 -> 13092807. The genuine native Material identities are retained.
  Exact original map appearance needs bounded owner-level map-color overrides
  later; no synthetic replacement Material hierarchy was introduced.
- No CP37 lighting/boss-HUD gate, save/load, dedicated server, multiplayer,
  reobfuscated JAR, or full gameplay parity result is implied.

Standard `git diff --check` reports preserved CRLF as trailing whitespace in
this repository; use `git -c core.whitespace=cr-at-eol diff --check` to assess
new whitespace without normalizing the original bytes.
