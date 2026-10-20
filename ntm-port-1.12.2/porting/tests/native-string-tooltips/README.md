# Native single-string tooltip companion

## Exact finite scope

Base: **CP66 `c986d3f35683a7b81fe0e2caf928f6a47a7a6b5f`**.
Exactly 12 method-name substitutions in 10 Java files:
`drawCreativeTabHoveringText(String,int,int)` → native
`GuiScreen.drawHoveringText(String,int,int)`.

All text expressions, localization keys, formatting, coordinates and clamps, mouse
conditions, caller order, font source, slot/item operations, rendering, and packets
remain byte-identical apart from the 12 names. This batch does not include the
separate 80-site List rename. On this exact CP66 base those 80 legacy List calls
remain untouched, as do four pre-existing native List calls. Eleven selected calls
use `this`; CombustionEngine alone is implicit. There are no relevant source
String/List/font overload overrides or depth writes in the ten selected callers.

Production patches use one line of context to compose with the adjacent List-call
edits in CP67. Strict whole-file evidence remains **CP66-specific**; after combining
families, generate a composed-source receipt rather than calling this inverse
proof applicable to different files.

## Original/native contract

`exact-method-mapping.txt` records the original obfuscated→SRG and Forge1614
SRG→MCP String name, plus native stable39 mapping. Actual old `bdw.a(String,int,int)`
and native `GuiScreen.drawHoveringText(String,int,int)` have identical instruction
structure after names/constant-pool indexes are normalized: allocate a one-element
String array, wrap with Arrays.asList, and virtually call the List overload with
unchanged x/y. They do not filter null/empty strings, split lines, strip formatting,
look up an item font, or directly change rendering/depth state. Both wrappers
produce a fresh fixed-size but element-replaceable list containing the original
String reference. Native access is public rather than protected.

The original Forge patch does not change the String wrapper. Its List method
forwards to the screen-font hook, as the native List overload also does. This
therefore reuses the accepted native List-path adaptation: default wrapping and
margins, genuine Forge tooltip hooks, and depth-field preservation instead of the
old forced-zero reset. No hook is disabled in production, and exact old pixels are
not claimed. The detailed prior List-path proof is in
`../native-gui-tooltips/native-path-review.md` after the separately delivered List
family is applied. Its runtime suite was **not rerun** for this companion.

## Executed bounded proof

- Full Java 8 attribution over 4,063 production files selects exactly 12 String
  sites, and all 12 post-edit expressions resolve to the genuine native String
  overload. All 96 tooltip-related calls and their surrounding methods/if-subtrees
  are compared, including excluded List calls.
- Ten complete file inverses match both original SHA256s and exact CP66 Git blobs.
  Twelve selector-corruption controls fail. Eleven independently attributed
  controls accept explicit/implicit/lexical outer cases and reject foreign
  receiver, List argument, wrong arity, String/List/font override, custom legacy
  method, and inner shadow cases.
- Actual old and native wrapper methods are executed for null, empty, ordinary,
  formatted and multiline/unicode Strings at zero, negative and extreme integer
  coordinates. Genuine engine subclasses intercept the virtual List call; no
  Minecraft/Forge replacement class is defined. Fresh-list identity, original
  String identity, fixed-size mutability, call count and coordinate parity pass.
- Actual native String→List→font-hook forwarding preserves the exact screen font.
- Four **complete original if-statement subtrees**, extracted by javac source
  positions and checked against production, execute in a real GuiScreen subclass:
  CombustionEngine's locked/unlocked tooltip with numeric text and two clamps,
  plus RecipeSelector's three independent formatted-label bounds. MathHelper and
  TextFormatting are genuine native classes. Tests exercise included/excluded
  edges, misses and locked far-outside inputs.
- **763 assertions pass, with 61 genuine native RenderTooltipEvent.Pre events.**
  Native text/coordinates/font/dimensions/item-cache identity and caller depth
  fields are observed; cached item count/metadata stay unchanged. A separately
  compiled clamp-removal mutant fails the native event coordinate assertion.

The event listener cancels only this test's nonempty rendering path before GL.
These are exact conditional-subtree tests, **not full drawScreen or whole-GUI
execution**. Localized recipe-fallback and ArmorTable slot/cursor branches are
source-audited, not executed. Real FontRenderer/RenderItem instances are allocated
without constructors for identity/depth observation, not rendering emulation.
No pixel, client, gameplay, or successful whole-mod claim is made.

The existing unsigned old-client test archive is reused after verifying every
non-META-INF entry against the supplied original. Only signature metadata differs;
no original class/resource code is substituted. Neither downloads nor new cache
copies are needed. Original/native class hashes and runtime input hashes are saved.

Recorded full-source attribution diagnostics fall **15,386 → 15,374**. Both runs
remain incomplete/failed mod attribution, not successful mod compilation.

## Separate actionable defect, intentionally NOT fixed

`src/main/java/com/hbm/inventory/gui/GUIArmorTable.java:38` checks
`this.mc.player.inventory.getItemStack() == null` before its tooltip loop (renamed
call at line 58). Ordinary native empty-cursor state uses ItemStack.EMPTY, so this
branch remains skipped even after the method-name fix. **Tooltip visibility is
not fixed by this migration.**

Actual native InventoryPlayer bytecode (`native-InventoryPlayer.javap.txt`) shows
constructor offsets 70–74 initialize `itemStack=ItemStack.EMPTY`, getter offsets
0–4 return it, and setter offsets 0–5 store the supplied reference without null
normalization. Null cannot be declared impossible while legacy setters remain.
A separate bounded fix should use native empty-stack semantics at this guard,
with empty/held-stack tests and a decision about compatibility with null setters.
Do not bundle a global null→EMPTY migration into this tooltip family.

## Reproduce

```sh
P=ntm-port-1.12.2
E=$P/porting/tests/native-string-tooltips
python3 "$E/check.py" --project "$P" --reattribute
python3 "$E/run.py" --project "$P" --out /tmp/native-string-tooltips-recheck
```

Defaults select the existing Java8 toolchain, native classpath file, original
client, and `/tmp/attribute-legacy-unsigned.jar`; options allow explicit paths.
`run.py` verifies the unsigned archive and reuses CP65's genuine Forge event
launcher from `../native-gui-sounds/ForgeEventNativeLauncher.java`. No full List or
text-widget suites are repeated. `contract-review.md` details source-audit limits.
