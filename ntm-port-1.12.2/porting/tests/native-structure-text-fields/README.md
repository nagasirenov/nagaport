# Remaining structure-wand text fields

Exact patch base: CP64 `27dceb0ef9e5815de3dc5f0494ca43aef9997370`.
BlockWandStructure is byte-identical at CP63, CP64, and the parent's CP65 freeze.
This companion is intended after the separate 50-site GUI constructor proof
(commit `432773012d28719ec3aa67b9f296a1c84a9a529a`), whose evidence it reuses.
No primary 106,372-assertion widget lifecycle suite was rerun or counted as new.

Only five leading constructor ID arguments change in one production file.
GuiStructureSave owns four private widgets: name=0, sizeX=1, sizeY=2, sizeZ=3.
GuiStructureLoad separately owns private name=0. Neither uses a GuiResponder,
reads an ID, or passes widgets out of its nested owner. GuiButton's existing ID0
is a separate button dispatch space and is untouched; no GuiPageButtonList or
shared ID registry is introduced. All five default canLoseFocus=true, so the
previously observed locked-focus engine difference is not reachable here.

The whole-file byte inverse proves preservation of the entire containing block,
tile, structure storage, file filter/list, key/mouse order, save/load button,
NBT/packet logic and close behavior. The seven already-native Jigsaw/Tandem
constructor IDs and the previously migrated 50 sites remain unchanged.

## New evidence

- Full-source actual Java8 attribution preserves the complete 62-constructor
  inventory and resolves these five calls to native GuiTextField(int,
  FontRenderer,int,int,int,int). Isolated attribution diagnostics decrease
  15,902 -> 15,897. This is not a Gradle/full-mod compilation result.
- A separate actual-symbol closure scan proves five private widget fields and
  all 34 references: construction, native configuration, rendering, text reads,
  ordered input dispatch, and file-selection setText. No reference escapes its
  owning screen, attaches a responder, changes canLoseFocus, or observes an ID.
  Single-source block/tile attribution has 123 unrelated diagnostics; native
  widget symbols resolve. It is not whole-owner compilation.
- Real-javac escape and responder-attachment mutants are rejected.
- 231 new actual-native assertions exercise constructor/configuration values,
  identities/IDs, all five fields, three screen widths, copied production
  ordered click/key prefixes, ordinary typing, outside-click focus, stable
  reinitialization, and the exact selectFile callback. One independently
  compiled duplicate-ID mutant is rejected.

The runtime fixture copies verbatim production constructor/configuration and
widget-dispatch statements and the complete selectFile method. It does not
execute super.keyTyped/mouseClicked, file-list rendering, button display,
file enumeration, close packets, structure save/load, or full nested GUI classes.
Those boundaries remain byte-exact source evidence, not runtime acceptance.
Actual FontRenderer and Minecraft instances use the same explicit bounded
object-state fixture technique as the earlier proof; no native classes are
replaced, GL rendering is not invoked, and no fake platform APIs are added.
New native screen focus propagation and keyboard shortcut handling remain native
platform behavior; original-engine side-effect parity is not claimed.

## Reproduce

From the project root with existing offline dependencies:

```sh
python3 porting/tests/native-structure-text-fields/check.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /tmp/nagaport-ae2-native-classpath.txt
python3 porting/tests/native-structure-text-fields/probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
  --native-library-dir /tmp/material-atlas-native
```

Replace environment paths as appropriate. No download or installation is needed.
No whole-mod, GPU, save-compatibility or client/server execution claim is made.
