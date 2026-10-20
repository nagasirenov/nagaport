# Default-item metadata model regression

Baseline: CP82 `df18fc077648dd18e6d0c1055d82c11dc2ebdb78`.
Native target: Minecraft 1.12.2 / Forge 14.23.5.2768 / stable_39 / JDK 8.

## Defect and exact production change

The old 1.7.10 `Item.getIconFromDamage(int)` returns the stored `itemIcon` without examining damage. `getIconFromDamageForRenderPass(int,int)` delegates to it. The previous native foundation installed a constant mesh only for subtype items, but installed a metadata-zero model for other items. Native `ItemModelMesher.getMetadata(ItemStack)` collapses damage to zero only when `getMaxDamage() > 0`. Therefore a non-damageable, non-subtyped item with damage 1 lost its original icon.

The sole production change installs the existing constant `stack -> inventoryLocation` mesh for **all already-eligible, explicitly declared, hbm-registered Item instances with no existing custom mesh/model route**. Eligibility, ownership checks, priorities, atlas/bake code, resource-pack preservation and specialized wrappers are byte-for-byte unchanged. No vanilla/global mesher hook, metadata normalization, ItemStack mutation, gameplay change, new asset, or custom-icon replacement is introduced.

`run.py` verifies the complete production file equals CP82 plus exactly that one branch substitution. It also verifies 11 CP82 registered `ItemDrop` texture declarations and their registration statements. `ItemDrop` extends native Item without a constructor, max-damage/subtype modifier or visual callback override; the 11 fluent receivers likewise do not change damage/subtypes. No named render registration refers to those fields. The exact declarations are in `reachable-owners.json`. These are source-reachable owners, not a claim that whole `ModItems` initialization or complete `ItemDrop` gameplay classes compile.

## Proof

- `native-test.log`: 3,933,740 regression assertions and 786,693 genuine `ModelLoader.onRegisterItems -> ItemModelMesher.getItemModel` lookups. Four combinations of maxDamage 0/256 and hasSubtypes false/true. Exhaustive constructor metadata 0..65535; additionally exhaustive raw metadata -32768..-1, which native `isEmpty` accepts but ordinary constructors/setDamage clamp. Negative constructor inputs and above-domain empty stacks are explicitly checked, not misrepresented as original-item stacks.
- Every exhaustive lookup checks item identity, count, damage, metadata, NBT identity and NBT content before/after. The mesh does not modify them. The test alone uses reflection to create the otherwise constructor-normalized negative raw-state control.
- Eleven named existing ItemDrop receiver fixtures use their original texture/translation/full3D facts and boundary samples 0, 1, 2, 255, 256, 257, 32766, 32767, 32768, 65534, 65535.
- Actual Forge event subscriber transformation, registered item identities, native generated model loading and native model/mesher routing. Directory then packaged-resource reloads; fresh models/sprites; a declaration registered between rounds; repeated provider registration; unknown custom mesh identity; unknown partial metadata-route nonexpansion; undeclared, foreign, ItemBlock, enum and callback exclusions; nonmissing resource model and specialized callback wrapper/TEISR ownership.
- Existing native item-foundation suite: 196 assertions, including genuine Forge FancyMissingModel handling, generated alpha extrusion, native tooltip/enum callbacks and server-side stripping. Only its plain-route expectation changed and a non-subtyped metadata sample was added.
- Original unmodified 1.7.10 Item bytecode: 294,922 assertions verifying texture-setter identity, single icon registration, and metadata/render-pass independent icon identity. `original-item-mapping.txt` is extracted and checked against the pinned MCP 1.7.10 `joined.srg`; archive/JAR and compiled-input hashes are in `runtime-inputs.json`.
- `baseline-negative.log`: the same behavioral probe with the unmodified CP82 handler and foundation fixture must fail at `hbm:metadata_matrix_0_false damage=1`. This is a native missing-model regression, not an absent-source or compilation negative control.

No fake Minecraft/Forge classes or NTM collaborator stubs. Named ItemDrop cases use explicit native Item receiver fixtures, not pretend complete ItemDrop constructors. Headless real atlas objects and decoded assets are used, not GL upload, client visual acceptance, server launch or full-mod build. Existing broader gates remain unchanged.

## Reproduce offline

From the module root:

```sh
python3 porting/tests/plain-item-metadata-native/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /tmp/nagaport-ae2-native-classpath.txt
# Expected failure at the original non-subtyped damage-1 lookup:
python3 porting/tests/plain-item-metadata-native/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /tmp/nagaport-ae2-native-classpath.txt --baseline
```

For a sparse isolated worktree, add `--support-root /workspace/shared/nagaport/ntm-port-1.12.2` to obtain unchanged production dependencies and assets without duplicating them. Production Java inputs outside the one changed handler are checked against CP82 before compiling. `--legacy-jar` and `--mapping-zip` can override the documented existing offline artifact locations.
