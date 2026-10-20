# Native crate naming root

Sealed base: `112cf44057e5cd1d1173561e66dbcca97edc9bdb` (CP71).
This is independent of the separately delivered lifecycle patch, which is not
reverted or incorporated into this source slice.

## Production scope

19 files: TileEntityCrateBase, its eight descendants (Iron, Desh, Steel, Tungsten,
Template, Safe, FileCabinet and MassStorage), ItemBlockStorageCrate.InventoryCrate,
eight corresponding GUI callers, and BlockStorageCrate drop/overlay callers.
The existing shared tile base implements the native custom-name predicate and
real display component. Each descendant's native getName preserves its original
getter body. The item-crate class uses the same literal/translation decision and
its existing tile-name delegation. No placeholder display name is added.

The six ordinary crates use the custom string only when non-null and nonempty.
FileCabinet and MassStorage deliberately retain their fixed key even with a
custom flag: that means a literal key, not translated text, in that case. Changing
this odd behavior would not be a parity-preserving migration. A named item with
an empty display string also preserves the original difference between the item
custom flag and the tile's nonempty custom predicate.

GUI title expressions remain otherwise byte-identical; their rendering,
localization call, colors, coordinates and layout are not changed. Storage block
custom-name drop propagation and overlay gating retain their exact operations.

## Audit exclusions

MachineBase and InventoryBase already expose native name, custom-name and display
methods with compatibility callers. Their storage and lifecycle bodies are not
changed here. Direct machine/bomb naming roots remain separate. RBMK and request
network roots already use getName as a default-name hook, so blindly renaming
getInventoryName there would create collisions/recursion. Proxies include null or
empty fallback names and require separate invalid-core behavior analysis. Entity,
train and minecart roots interact with native Entity naming and require a distinct
audit. Item toolbox/lead/rebar and direct bag inventory families have GUI-specific
custom-title logic, so they are not generalized from the crate rule.

Native IInventory additionally requires valid EMPTY boundaries, fields, clear,
lifecycle and IWorldNameable methods. This naming slice does not fix or claim
those unrelated obligations. No whole-machine, full-port, render or gameplay
readiness claim is made.

## Proof

- verify.py reverses all changes to exact CP71 bytes, preserving original line
  endings; existing name and custom-predicate bodies match pristine exactly.
- The verifier checks the complete eight-descendant closure and generates exact
  method slices. Native InventoryBasic supplies unrelated inventory operations.
  This is not compilation of the complete production owners.
- 388 assertions use actual native IInventory dispatch, TextComponentString,
  TextComponentTranslation, LanguageMap and SPacketOpenWindow serialization.
  Tests cover every descendant, null/empty names, whitespace, Unicode, formatting
  codes, keys used as literal custom names, overriding descendants, and item
  custom-name delegation including empty named items.
- The item fixture's registry factory and markDirty are explicitly bounded
  collaborators. Registry selection, tile construction, drops and block rendering
  are not executed by the native test.
- Original GuiChest bytecode uses the custom flag to choose a literal string or
  I18n translation. Native GuiChest reads getDisplayName().getUnformattedText();
  native InventoryBasic performs the same literal/translation component split.
  Both actual contracts and original SRG identifiers are retained in proof.

Replay on CP71 plus this production patch, before unrelated integration:

    export JAVA_HOME=/path/to/existing/java8
    export NATIVE_CLASSPATH_FILE=/path/to/existing/native-classpath.txt
    bash ntm-port-1.12.2/porting/tests/native-crate-names/run.sh /tmp/crate-name-proof
    git -c core.whitespace=cr-at-eol diff --check

No downloads, installations, pushes, game launches or fake platform interfaces.
