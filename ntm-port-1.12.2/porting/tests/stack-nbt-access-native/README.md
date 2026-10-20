# Receiver-attributed ItemStack NBT field migration

Baseline: CP51 `077492a6c31c910f34697783f1aa0cfabd8ff88e`. This finite change is intended for the checkpoint after CP52; the coordinator owns the full build.

## Scope and proof

- 826 lexical candidates in 114 eligible files. Exclusions are explicit in `inventory.py`.
- 647 reads and 141 simple discarded-value writes migrated in 108 files: 788 native field accesses represented by 787 disjoint fragments. One assignment contains a separately attributed RHS read.
- 38 unresolved receivers remain unchanged. They are listed verbatim with reasons in `manifest.json` and `evidence-scope.json`.
- `StackNbtAccessScanner.java` uses javac parse/attribution. Every accepted receiver must resolve to the DECLARED `net.minecraft.item.ItemStack` type, AND the selected field must resolve to the native ItemStack-owned field symbol. No variable-name heuristic or regex ownership is used.
- Only the entire value-discarding assignment becomes `receiver.setTagCompound(rhs)`. Reads become `receiver.getTagCompound()`. Used assignment values, compound writes, unsupported unary contexts, unresolved or unrelated receivers are not rewritten. Parentheses around an lvalue are accounted for.
- Targeted attribution of the 114 candidates had 8,301 diagnostics before and 7,513 after, exactly 788 fewer in this targeted analysis. This is NOT a successful full compile. The post-migration scan finds only the same 38 unresolved accesses.
- Source bytes, including existing CRLF, are preserved outside the exact fragments. The inverse manifest reconstructs all 108 CP51 files byte for byte. This is structural evidence, NOT proof that all production bodies work at runtime.
- No stack count, unrelated API, source-set, shim, or gameplay changes are included.

## Actual runtime evidence

`verification.log` records 59 passing assertions against the real mapped Forge 1.12.2 / 14.23.5.2768 runtime using the existing classpath and Java 8. Two actual, unextracted production classes/interfaces are compiled and executed:

- `cofh.api.energy.ItemEnergyContainer`: real receive, extract, and stored-energy methods, absent tags, aliasing, simulated and committed transfer, capacity boundary, unrelated tags, persistence, null exception. Existing simulation behavior intentionally allocates an empty tag on receive; this was NOT changed.
- `com.hbm.items.ISatChip`: real default get/set frequency and static dispatch, absent tags, shared reference mutation, negative/zero values, unrelated tags, non-chip and null handling. A test Item implements this actual interface; no platform classes are replaced.

The remaining 106 edited production types are NOT whole-body compiled or executed here. Exact list: `evidence-scope.json`. Client and dedicated-server tests are NOT_RUN.

Native platform tests cover raw null vs empty tags, setter/getter reference identity, alias mutation, explicit clearing, native copy behavior, nested and unknown tags, in-memory write/read aliasing, actual compressed binary round trip, raw EMPTY behavior with restoration, receiver/RHS single evaluation and ordering, null and throwing receiver/RHS behavior, array/index order and bounds failures. A tiny non-platform `Cell` supplies only a Java field-assignment evaluation-order oracle.

Important observed native detail: `ItemStack.writeToNBT` followed directly by `new ItemStack(nbt)` shares the in-memory tag reference. Compressed binary serialization preserves values but detaches that in-memory reference. The test distinguishes these rather than imposing a copy.

Native bytecode is also checked: ItemStack is final; `getTagCompound` is exactly `aload_0/getfield/areturn`, and `setTagCompound` exactly `aload_0/aload_1/putfield/return`. Thus there is no EMPTY normalization, tag allocation, copying, or override dispatch introduced by these accessors.

Attribution controls exercise native local/parameter/array/method/field receivers, same-named nonnative fields, shadowed ItemStack types, used assignment values, parenthesized lvalues, strings and comments. The fixtures deliberately contain illegal private-field accesses for attribution; they are not production or platform replacements.

## Reproduce

From the project root:

```
JAVA_HOME=/workspace/shared/nagaport-toolchain/java8 \
MINECRAFT_CLASSPATH="$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
bash porting/tests/stack-nbt-access-native/run.sh
```

`verify.py` proves the manifest inverse against the baseline and checks that all owned transformed fragments still exist in the current tree; unrelated concurrent edits may coexist. The run does not claim successful compilation of other mod bodies.

The native jar on the supplied classpath is the existing `.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`; no dependency downloads are used.

## Merge without overwriting other workers

Run the isolated evidence folder's script with the target project root:

```
python3 merge_fragments.py /path/to/ntm-port-1.12.2
python3 merge_fragments.py /path/to/ntm-port-1.12.2 --apply
```

Default is a read-only dry run. It loads CP51 from git, verifies baseline hashes, maps ONLY unchanged owned fragments onto the current source, and rejects any changed owned fragment before writing anything. Unrelated edits, including Blocks/Items constants and stack count fragments, are retained. No whole-file copy is required. The recorded main-tree dry run passed for all 108 files.

Copy this new evidence directory separately. A conventional source-only patch is also supplied by the worker, but the fragment merger is preferred where other workers changed surrounding lines.

CRLF files are intentionally not normalized. Plain `git diff --check` reports their pre-existing CR bytes as trailing whitespace; use `git -c core.whitespace=cr-at-eol diff --check` to account for the preserved line endings. That check still reports pre-existing mixed space/tab indentation in touched lines (`diff-check.log`, exit 2); the exact-fragment inverse proves the surrounding whitespace was not introduced or normalized by this change.
