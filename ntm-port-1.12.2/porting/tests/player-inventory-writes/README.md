# CP86: discarded player inventory writes

Baseline: CP85 `abe006a0845275e8be52417967ac8ebf6f283a52`.

Exactly 21 assignments in nine owners change from native `InventoryPlayer` main/armor array syntax to `NonNullList.set(index, rhs)`. Five literal-null removals use the explicit canonical `ItemStack.EMPTY` sentinel. Fifteen constructor RHS expressions and one native `copy()` expression remain byte-for-byte unchanged. No quantity operations, branches, copies, imports, helpers, or other source bytes are changed. In particular, the legacy `stack.stackSize` expression in `ItemFolderPacket` is intentionally not fixed here.

## Reproduce the bounded checks

From this module's root, with the cached toolchain and classpath available:

```sh
python3 porting/tests/player-inventory-writes/run.py --attribute
```

The defaults are `/workspace/shared/nagaport-toolchain/java8` and `/workspace/shared/nagaport-toolchain/native-classpath.txt`; command-line overrides are available. No Gradle build, network access, or push is performed.

- `check-source.py` checks exact full-file post-edit SHA-256, reverses every token, and compares all nine baseline files byte-for-byte, including mixed line endings. Twenty-seven corruptions fail closed. `reverse_source(relative, text, strict=False)` is exported for the enclosing CP86 combined inverse guard; it only reverses exact, uniquely occurring write tokens and does not itself claim whole-file equivalence.
- Javac AST attribution checks the actual declaring owner `net.minecraft.entity.player.InventoryPlayer`, final native `NonNullList<ItemStack>` main/armor fields, and discarded expression statements. It excludes result-used assignments, unrelated/shadowed/unresolved fields, offhand, and arbitrary potentially-null RHS values. Constructors must name the actual native `ItemStack` type; the existing unresolved machine superclass prevents complete constructor-overload typing in the bounded nine-owner probe and is not claimed as compiled. The single `copy()` call resolves to native `ItemStack.copy`. All 21 replacements resolve to `NonNullList.set`.
- The native runtime has 1,309 assertions using actual `InventoryPlayer`, `NonNullList`, `ItemStack`, and NBT. It tests all 36 main and four armor slots, replacement/removal, unchanged offhand, exact object/count/damage/NBT alias preservation, canonical EMPTY, zero/negative/air/noncanonical empty values, and overstack counts. Twenty exact post-edit production statements are compiled with explicit data-only collaborators. The packet's unmigrated quantity-dependent RHS is replaced by an explicit supplied-stack observation seam in that boundary test only. No full packet handler is claimed.
- Evaluation probes cover receiver/list-field capture before index and RHS, `set` after RHS, changed inventory aliases in RHS, and null receiver/inventory/list, bounds, and throwing evaluation. A real `NonNullList` subclass is used only to observe its call order; storage/validation delegate to the native implementation. Actual Java array assignments independently establish the original null/bounds cut points.
- Native `NonNullList` rejects null, validating it before bounds. This rewrite is deliberately restricted to literal-null→EMPTY, constructors, and native `copy`; it is not a general nullable-RHS adapter. The returned old value of `set` is ignored because all selected assignments were statements.
- Important Forge behavior: the cached native `ItemStack.copy()` always constructs a new stack, including a tagged zero-count/noncanonical-empty source. The native test verifies that this returned object is not normalized to `EMPTY`. `native-engine-bytecode.txt` records the real constructor/list/copy behavior; `native-runtime-inputs.json` pins the actual platform class providers and bytes.
- The standalone nine-owner compiler diagnostic probe is attribution only, with no class generation. Errors go from 952 to 931: exactly the 21 selected array-required bracket errors disappear; every other diagnostic code, message, and source position matches after an exact token-position inverse. Eight unrelated indexed-read errors remain in this isolated write batch. This does not establish whole-owner or project compilation.

When other reviewed CP86 source edits are composed, first run the enclosing combined source guard, then use `run.py --attribute --composed-source` for the native runtime and exact write-token/AST checks. This explicit mode does not claim the standalone full-file hashes or standalone diagnostic delta still apply to combined sources.

## Evidence and limits

`source-manifest.json` is the merge contract: files are keyed relative to the module root and edits carry `start`/`end` Python-codepoint offsets, `old`, `new`, and exact list/index/RHS substrings. Attribution JSONL offsets are javac UTF-16 positions. `baseline.zip`, proof log, diagnostic proof, and runtime provenance are retained.

No full handler, gameplay world, radiation, event lifecycle, bootstrap, or server execution is claimed. Original surrounding logic and remaining port errors are deliberately outside this change. The production files use their original line endings; check whitespace with `git -c core.whitespace=cr-at-eol diff --check` so preserved CRLF is not misreported as new trailing whitespace.
