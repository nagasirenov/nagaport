# Thermal furnace GUI family native boundary

Production ownership is exactly GUIFurnaceBrick, GUIFurnaceIron and
GUIFurnaceSteel. No tile, container, helper, renderer, block or resource was edited
by this boundary. Test-only NTM collaborators are not production shims.

## One family transformation

thermal-gui-migration.py applies one preflight-counted whitelist transformation to
all three files. The original complete source bytes, before/after SHA256 values,
exact replacement/count rules, checkpoint revision and resource hashes are frozen
in thermal-gui-source-manifest.json. All files are validated before any production
file is written. The one-time --apply operation refuses to overwrite its manifest.
The default invocation verifies current full-file hashes, reverses every exact
rule, compares original full bytes/hashes, rejects added-byte drift, and separately
checks original strings, numeric literals, rectangles, custom tooltip expressions
and GL calls. reverse_source(relative,text) and WHITELIST are available for a
combined source audit; reverse this layer before older overlapping GUI audits.

Changes are native hasCustomName/getName/fontRenderer in all three classes;
brick getWorld/getPos for the original invalid-tile type check and two lookups;
and iron mc.player, native cursor isEmpty and native drawHoveringText. There is no
new super call or hover implementation: GuiInfoContainer already renders the
native hovered-slot tooltip once, before descendant overlays. GUI dimensions,
colors, text, input/output/fuel coordinates, all heat/progress/burn/bonus scale
expressions, native font selection, centering, integer rounding, edge comparisons
and the absence of new clamps are retained. Custom names remain unlocalized;
default names and the inventory label use actual native I18n.

The three resource paths already have canonical lowercase spelling and real
256x256 PNG files. No texture rename or pixel change was necessary. Actual native
FolderResourcePack and temporary ZIP FileResourcePack resolve each production
path and return bytes matching the frozen SHA256. This is not a packaged mod JAR
or a visual rendering test.

## Verification

Run from the project directory:

    python3 porting/tests/thermal-gui-migration.py
    python3 porting/tests/thermal-gui-native-probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"

The runner copies and compiles complete, byte-identical production classes:
GUIFurnaceBrick, GUIFurnaceIron, GUIFurnaceSteel, GuiInfoContainer, SlotPattern,
IUpgradeInfoProvider, RefStrings and ItemMachineUpgrade. Each input hash is logged.
Minecraft, Forge, NEI and LWJGL are real native dependencies, never replaced by
source stubs; runtime class origins for relevant native/third-party classes are
logged and checked to come from dependency JARs.

Explicit controlled NTM-only collaborators are ContainerBase and the three furnace
containers, the three furnace tiles, ModuleBurnTime, ModItems, BobMathUtil,
I18nUtil, PacketDispatcher and NBTControlPacket. Native InventoryBasic and Slot
back the fixture inventories. Controlled steel processTime/maxHeat constants match
the production values (40,000/100,000). Fuel description lists and canSmelt are
controlled, so these tests do not establish fuel accounting or recipe behavior.

6,735 checks execute complete production constructors and foreground methods with
a recording subclass of the real FontRenderer; actual native localization and
hovered-slot behavior; and verbatim production non-GL suffixes with recording
rendering sinks. Coverage includes:

- Constructor inventory/tile identity and 176x166 dimensions in all three GUIs;
  custom/default title branches, centering, white brick versus dark iron/steel
  title/inventory colors, and inventory-label coordinates.
- Brick native BlockPos refresh with exactly two successful lookups, one failed
  incompatible/missing lookup, no valid-tile lookup, burn-gated progress, plus-one
  pixel behavior, original rounding and intentionally unclamped scale values.
- Iron guarded processing/burn denominators, canSmelt icon, native empty/zero/
  negative/positive cursor cases, both fuel-slot hitboxes and their one-pixel
  native borders, occupied/empty fuel slots, empty bonus lists, original list
  alias, progress percentage, second truncation, custom tooltip edges and the
  actual inherited native carried-stack/empty-slot tooltip suppression.
- Steel all three row positions and icon flags, progress/bonus/heat arithmetic,
  unclamped bonus, literal TU/Bonus text, comma formatting under a non-US default
  locale and exact heat-tooltip edges.
- Native folder/ZIP texture lookups, byte hashes and PNG atlas dimensions.

Background suffixes begin strictly after color, texture binding and base panel.
Overlay suffixes begin strictly after super.drawScreen, using the complete
production GuiInfoContainer for native/custom hit testing. These suffix tests are
NOT execution of complete background/drawScreen methods. Complete classes still
compile without source edits or method removal. No GL context, visual acceptance,
actual mouse input client, packet transmission, real tile tick integration,
world save, runnable mod or playable-machine claim is made. Unsafe is restricted
to headless test allocation of native Minecraft, client player and recording font.
Windows execution is untested. No full Gradle build, commit or push was performed.

Evidence files in porting/continuation-2026-10-19:
thermal-gui-source.log, thermal-gui-native.log, thermal-gui-api-evidence.log.
