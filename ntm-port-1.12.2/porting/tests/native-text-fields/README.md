# Native GuiTextField constructor root (50 sites)

Application base: CP63 `0bbba4f47800cea8dd60370f697450597f27c8a6`.
This is a constructor-only migration in 35 `com.hbm.inventory.gui` files.
It inserts one explicit, stable ID into each of 50 original five-argument calls.
No widget configuration, existing argument, callback, GUI dispatch, slot,
network payload, or sound call is modified. Original/pristine is not edited.

## Original and native contract

The real supplied 1.7.10 client declares `bcd(bbu, int, int, int, int)`, mapped
by the cached official joined.srg to GuiTextField(FontRenderer,x,y,width,height).
Native 1.12.2 declares
`GuiTextField(int id, FontRenderer fontRenderer, int x, int y, int width, int height)`.
Both constructors store the same original five values and initialize text,
length, cursor, selection, focus, drawing, enabled/visible flags, and colors
identically. Native additionally stores the ID and an always-true validator;
the responder starts null. Actual class hashes and complete javap output are
retained. Runtime tests use actual engine class bytes, not reconstructed widgets.

The added ID is read only by getId and write/delete callback dispatch in the
native class. Full production javac attribution finds no GuiTextField getId,
setGuiResponder, or setResponderEntryValue calls. Existing fields remain local
to their screens and the unchanged standard-config helpers. No callbacks are
attached by this patch, and all seven already-native constructor IDs outside
this family remain untouched. Third-party reflective access is not certified.

IDs are stable per screen, not globally unique:

- One-field screens: 0.
- HeaterHeatex: cycles 0, delay 1. RadioTelex: transmit 0, receive 1.
- SatCoord: x 0, y 1, z 2.
- CounterTorch and RBMKControlAuto: array index i (3 and 4 fields).
- ReactorControl: rod fields i for i=0..1; heat fields i+2 for i=0..1.
- RBMKGauge: color i, label i+4, rtty i+8, min i+12, max i+16; i=0..3.
- RBMKKeyPad: color i, label i+4, rtty i+8, command i+12; i=0..3.
- RadioTorch and RadioTorchLogic: primary frequency 0, remap/map i+1; i=0..15.
- RadioTorchReader: frequencies i, names i+8; i=0..7.

This is 128 distinct fields across one initialization of each of 35 owners.
Every ID expression is side-effect-free. The byte loop index in ReactorControl
is widened by the actual int constructor parameter. No cast or mutable ID
counter is added. IDs do not consume button IDs or a global GUI registry.

## Source proof

The real Java 8 compiler parses and attributes all 4,062 production Java files,
with all production packages included. It independently records every one of
62 named GuiTextField constructor expressions, complete argument strings/types,
assignment owner, enclosing method/loop, and actual constructor resolution.
50 plain five-argument sites in inventory/gui are selected. The remaining 12
are seven already-native Jigsaw/Tandem calls and five still-legacy Structure
calls; none are changed or claimed fixed.

Before/after attribution errors are 15,952 -> 15,902, exactly 50. These are
javac attribution diagnostics, not a full Gradle compile, distinct defect count,
or completion percentage. The main project still does not compile.

`check.py` verifies all retained AST facts, native constructor symbols, unchanged
original argument ordering, collision-free IDs at each actual loop extent,
whole-file SHA256 inverse for every changed file, and unchanged outside-scope
constructors. Fifty independent wrong-ID source mutations are rejected. Seven
real-javac control cases accept two valid old native calls and reject existing
six-argument, anonymous, wrong-font, wrong-method, and shadow-class calls.

`--fragments` is only for integration after independently audited changes in the
same files, notably sounds. It checks constructor fragments but intentionally
DOES NOT assert old whole-file hashes. Such integration requires a separate
combined source guard; it is not a replacement for the strict isolated proof.

## Real widget runtime probe

`probe.py` extracts each exact production constructor expression into a bounded
fixture and compiles it against the actual native classpath. Corresponding
original expressions are compiled against the supplied real legacy class,
with only class/font type names mapped. Three layout configurations and every
actual array index exercise 384 construction instances.

The probe compares original/native font identity, coordinates, dimensions,
defaults, maximum-length truncation, text filtering, selections, replacement,
backspace, word deletion, cursor bounds/scrolling, ordinary key input, rejected
characters, focus reset/repeat, mouse boundary/cursor behavior, enabled/visible
state and ignored unfocused input. Native responder tests cover attachment,
exact constructor-ID routing, no programmatic-setText callback, edit/delete,
validator rejection, and detachment. Four independently compiled runtime
mutants (ID, coordinates, font, dimensions) must fail.

Actual FontRenderer objects are allocated without their resource/GL-dependent
constructors, with identical deterministic character/glyph width arrays. The
real trim/width implementations execute. A real Minecraft object is allocated
without its application constructor, only to provide its currentScreen slot;
a genuine GuiScreen subclass exercises native focus propagation. These are
explicit bounded object-state fixtures, not a launched game or rendering test.
No native class is replaced and no whole mod/gameplay claim is made. The legacy
jar's signature metadata is removed in a temporary test-only copy because its
classes and the harness share the default package. Every remaining entry is
byte-checked against the supplied jar; no class bytes are changed or delivered.

### Deliberately observed native differences

Native setFocused additionally updates the current GuiScreen.focused flag.
This is tested as native lifecycle behavior, not described as an original-engine
side effect. Native keyboard control-combo recognition is not the old control-
character dispatch; OS clipboard/modifier-key behavior is outside this probe.

A real differential initially found that the original widget moves its cursor
on an outside left click when canLoseFocus=false, while native ignores that
click. A retained runtime control asserts both exact different outcomes.
This difference is unreachable through the scoped original GUI code:
GUICalculator is the sole canLoseFocus=false owner, its inputField is private,
and it has no mouseClicked override or forwarding/escape of that field.
The source guard verifies those conditions. No native compatibility shim,
new calculator click support, or silent behavior policy was introduced.

No GPU/visual/font-resource acceptance, full GUI class compilation, OS keyboard
or clipboard integration, complete machine network path, or full mod/client/
server execution is certified by these bounded constructor/lifecycle tests.
Existing CP37+ policy gates remain unchanged.

## Reproduce offline

From the project root, with existing Java8/native dependencies and original jar:

```sh
python3 porting/tests/native-text-fields/check.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /tmp/nagaport-ae2-native-classpath.txt
python3 porting/tests/native-text-fields/probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
  --legacy-client /tmp/nagaport-legacy-client.jar \
  --native-library-dir /tmp/material-atlas-native --mutants
```

The native-library directory must contain the already available matching LWJGL
native library. No network, install, download, or production cache copy is used.
For portable reproduction replace these environment paths with corresponding
local JDK8, resolved native classpath, original client, and native library paths.
`check.py` without Java options verifies retained attribution and the exact
current source inverse without needing either the original Git object or Java.
