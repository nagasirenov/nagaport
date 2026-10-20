# Native List tooltip method root

## Delivered production change

Exact base: `5e6ba284ef32b46ad5f81cd1e1570c08b53cb24f` (CP65).
Exactly **80 method-name substitutions in 39 Java files**:
`func_146283_a(List<String>, int, int)` becomes the genuine native
`GuiScreen.drawHoveringText(List<String>, int, int)`.
No imports, arguments, list construction, content, font selection, conditions,
control flow, call order, fields, slots, items, packets, or unrelated GUI code change.
The four existing native List calls and **12** legacy single-string calls remain
untouched. The initial compiler-root estimate of 11 single-string sites was not a
complete source count; this full scan establishes 12.

Full Java 8 attribution scanned all **4,063** production Java sources. All 80
selected receivers are GuiScreen subtypes with exactly List<String>,int,int
arguments; all 80 migrated expressions resolve to the native List overload.
The 54 explicit `this` and 26 implicit receivers include three calls in nested
FolderButton classes. Those three resolve lexically to their owning Bobmazon,
Designator, and TemplateFolder screens, not to the buttons.

No source tooltip overrides exist, including the four-argument virtual font hook.
The two captured gradient overrides are unrelated WIaj actor classes. All member
closures contain only genuine GuiScreen overloads. Existing GuiInfoContainer
List helper calls use the same native entry point. The 40 selected caller methods
contain no direct depth-field assignments. See `native-path-review.md` for the
bounded caller/inherited-helper depth review.

## Original-to-native semantics and accepted adaptation

The original client method's exact SRG identity and original Forge 1614 font-hook
patch are retained. Old Forge's three-argument method forwarded the original
list/x/y and screen font to its four-argument virtual hook. Native does the same,
then delegates to genuine Forge GuiUtils with screen width/height and maxWidth=-1.

This intentionally adopts standard 1.12 UI behavior, not old-pixel emulation:
- Native default wrapping, screen-edge margins and top/bottom layout differ.
- Native RenderTooltipEvent Pre/Color/PostBackground/PostText hooks remain enabled;
  Pre cancellation, mutable coordinates/dimensions/font and normal item-cache
  context are not bypassed. No production event listener was added.
- Old nonempty rendering forced both caller depth fields to 300 and then **zero**,
  not their incoming values. Native draws its background with explicit z=300 and
  does not write Gui.zLevel or RenderItem.zLevel. Incoming values are preserved.
  No selected source caller relies on the old forced-zero reset; its separate
  depth writes already reset themselves on normal flow. This does not claim
  exception-safe GL restoration or protection against third-party hook mutations.

Detailed layout/hook ordering, original mapping, authentic class hashes, original
patch and both engine bytecode dumps are retained as evidence. No item stack is
changed by this production patch. The untouched TemplateFolder item-tooltip branch
keeps its original renderToolTip dispatch.

## Executed evidence

`check.py --reattribute` passes:
- Independent full-tree selection of exactly 80 sites from all 96 tooltip-related
  invocations, and comparison of every before/after invocation.
- 39 complete byte-for-byte file inverses, preserving all unrelated source.
- Native method attribution, lexical receiver and override closure, unchanged
  arguments/types/font source/order, untouched native/single-string calls.
- 80 independently corrupted selector controls rejected.
- 10 real javac-attributed controls: explicit, implicit and lexical-outer accepted;
  foreign receiver, String argument, wrong arity, three/four-argument override,
  legacy custom method and inner shadow all rejected.

`run.py` compiles actual native classes with the existing genuine Forge event
launcher. It extracts four **complete** production methods and checks that their
exact post-edit text occurs in source: the three FolderButton drawString methods
and GUIRBMKConsole.drawCustomInfoStat. They run in narrow fixture owners extending
real GuiScreen, not fabricated Minecraft/Forge classes and not a full GUI build.

The runtime proves exact list alias forwarding to the four-argument virtual hook;
content/order/coordinates/font and screen dimensions at genuine native Pre events;
normal listener mutation/cancellation; unmodifiable backed line views; null and
empty behavior; cached item identity with no count/metadata changes; field-depth
preservation for negative, zero and positive values; nested outer ownership;
original null/empty guards and item branch; and all 49 points around asymmetric
RBMK hover boundaries. A separately compiled coordinate-swap mutant is rejected
by the native event assertion.

The runtime stops nonempty native calls through an actual cancellable Pre listener
before GL. Empty calls run their complete native early-return path. Full bytecode
inspection establishes that the uncanceled native tooltip body also does not write
caller depth fields. **Uncanceled GL rendering and pixel layout are not exercised.**
FontRenderer/RenderItem are real engine classes allocated without constructors for
identity/depth observation, not rendering substitutes. No widget tests were rerun.

Recorded attribution diagnostics drop from **15,608 to 15,528**, exactly 80. Both
are failed/incomplete whole-mod attribution runs; neither is a successful mod
compile. See `source-proof.log` and `runtime-proof.log` for actual pass counts.

## Reproduce

From the repository root:

```sh
P=ntm-port-1.12.2
E=$P/porting/tests/native-gui-tooltips
python3 "$E/check.py" --project "$P" --reattribute
python3 "$E/run.py" --project "$P" --out /tmp/native-gui-tooltips-recheck
```

Defaults use `/workspace/shared/nagaport-toolchain/java8` and the existing
`/tmp/nagaport-ae2-native-classpath.txt`; neither command downloads or installs.
The runtime reuses `native-gui-sounds/ForgeEventNativeLauncher.java` from CP65.
The strict whole-file receipt intentionally fails after unrelated composition;
parent integration should independently check the surgical fragments and generate
its composed-source receipt rather than reinterpret this exact-base proof.

`git diff --check` reports the pre-existing space-before-tab indentation on the
renamed SatInterface line. The inverse guard proves that indentation is unchanged;
it is deliberately not reformatted in this bounded method-name patch. The verbatim
original Forge patch is evidence and likewise retains its original whitespace.
