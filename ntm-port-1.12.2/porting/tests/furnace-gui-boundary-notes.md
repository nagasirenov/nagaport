# Native furnace GUI boundary

Baseline: checkpoint 17, local revision `48bd00359478c16dc9c47891073e700cb8107cb6`.
Only GUIHandler, GuiInfoContainer and GUIMachineElectricFurnace production Java files
belong to this boundary. No tile, inventory, container, upgrade, registry or model
production file was changed here. One texture was renamed without changing bytes.
No old Forge API, Minecraft class, NEI API or placeholder renderer was introduced.

## Changes and exact behavior

- GUIHandler retains tile → block → main-hand item → entity priority, immediate
  return even when a provider returns null, all provider arguments, and entity
  lookup in the player's world rather than the supplied GUI world. Native block
  state/BlockPos and native main-hand/EMPTY boundaries replace removed APIs.
- The base retains all prior public/protected helper signatures. Slot hitboxes
  still use the native one-pixel expanded region, while custom information/click
  boxes retain their distinct left-inclusive/right-exclusive and
  top-exclusive/bottom-inclusive edges. GUI offsets, scissor expression,
  colors, gradient rectangles, item positions, labels, layers and direct GL
  operation order are unchanged by source audit.
- Native RenderItem uses renderItemAndEffectIntoGUI(ItemStack,int,int) and
  renderItemOverlayIntoGUI(FontRenderer,ItemStack,int,int,String), not an
  approximate renderer. Per-item font selection and fallback remain where they
  existed. The nullable drawItemStack helper converts null to native EMPTY only
  after the original font-selection branch, at the native renderer boundary.
- 1.7.10 GuiContainer (official client obfuscated class bex) rendered its hovered
  slot tooltip near the end of drawScreen, before final GL lighting/depth
  restoration. Native GuiContainer separates this into renderHoveredToolTip.
  GuiInfoContainer now calls that native method after super.drawScreen, before
  descendant custom overlays. A whole-source 166-file descendant closure found
  no existing native hover calls and no renderToolTip/getItemToolTip/
  drawHoveringText overrides. No descendant helper was renamed. Existing
  isMouseOverSlot overrides in GUIDiFurnace/GUIAutocrafter and drawItemStack in
  GUIPWR retain their dispatch contracts and remain separately unported classes.
- Native GuiScreen supplies the exact NORMAL/ADVANCED ITooltipFlag enum,
  rarity-color first line, gray subsequent lines, and custom font handling.
  These are inherited, not reimplemented. Real native flag/color and hover gates
  are tested. Native GuiUtils restores lighting/depth after drawing, as verified
  in cached bytecode. This is not a visual or GL-state acceptance result.
- Button sound maps gui.button.press to SoundEvents.UI_BUTTON_CLICK using the
  actual native master record: pitch 1, volume 0.25, no repeat, no attenuation.
  Official 1.7.10 PositionedSoundRecord.bso bytecode confirms the old supplied
  volume/pitch/repeat/attenuation values. No audio engine playback was attempted.
- NEI 2.4.3.245's actual configured INEIGuiHandler, VisibilityData and
  TaggedInventoryArea classes are used, without substitute third-party APIs.
  Pattern drag retains slot number, native ItemStack NBT, source aliases and
  tile position. Null/native EMPTY drags do not produce a packet. Packet dispatch
  and control-packet serialization beyond this GUI call are explicitly controlled
  NTM dependencies and are not validated as a working network here.
- Furnace dimensions, custom/noncustom name branch, label colors/centering,
  invalid-tile two-lookup refresh and replacement type check, power scale 52,
  progress scale 24, flame block identity, progress +1 pixel and info icon remain.
  No clamping, active-state approximation or replacement-tile caching was added.

## Concrete EMPTY display limitation

Native ItemStack.getCount() returns zero and getItem() hides the original item
for empty stacks, including a formerly positive item set to count zero and
negative-count stacks. The real native item and overlay renderers return without
rendering them. GuiInfoContainer retains its explicit red zero-count background
and gray interior, but cannot recover the old zero-count item icon from the
native public stack API. It does not fabricate an icon or mutate a display stack
to count one. Native negative counts also appear empty/red rather than retaining
legacy negative-count distinctions. This is a known remaining domain-display
boundary, not full visual parity. Existing producers in GUILaunchPadLarge,
GUITurretBase, GUITurretArty and GUIHydroponic still assign legacy stackSize=0 and
need a separately owned display descriptor migration. This furnace itself does
not call drawStackText. Normal positive stack rendering retains its native path.

## Resource evidence

`assets/hbm/textures/gui/GUIElectricFurnace.png` was renamed to
`assets/hbm/textures/gui/guielectricfurnace.png`. Original and current SHA256:
`c87ded82d7234f23f096b3d89638f13a5d2cccd42a74d3362675321c976083bd`.
The sole internal filename alias is GUIMachineElectricFurnace. Native
ResourceLocation lowercases both historical and canonical aliases. Actual native
FolderResourcePack and ZIP FileResourcePack fail the historical stored name and
open the new name with the same SHA256. PNG decode retains its 256-pixel atlas
width. This is an actual temporary ZIP resource lookup, not a built mod JAR.

## Tests and scope

Run from the project root, with JDK 8 and the resolved native classpath:

    python3 porting/tests/check-furnace-gui-source.py
    python3 porting/tests/probe-furnace-gui.py --java-home /path/to/jdk8 --classpath "<native classpath>"

On this workspace the classpath file is
`porting/continuation-2026-10-19/native-probe-classpath.txt`. Regenerate on another
machine; do not copy Linux separators onto Windows. The runner supports .exe,
os.pathsep, UTF-8 and java.home ending in jre. Optional Gradle init tasks
checkPortFurnaceGui/testPortFurnaceGui are supplied in furnace-gui-probe.init.gradle;
they were not redundantly run. No full Gradle build was run by this boundary.

The runner copies and compiles COMPLETE byte-identical production GUIHandler,
GuiInfoContainer, GUIMachineElectricFurnace, SlotPattern, IGUIProvider,
IUpgradeInfoProvider, RefStrings and the current parallel-owned ItemMachineUpgrade.
Every copied source SHA256 is logged. No methods are removed from those classes.
Controlled NTM-only collaborators are explicitly enumerated in the runner/log:
ModItems identities, BobMathUtil formatting, I18nUtil localization, ContainerBase,
ContainerElectricFurnace, TileEntityMachineElectricFurnace, ModBlocks identities,
PacketDispatcher and NBTControlPacket. Native Minecraft/Forge/NEI classes are real.

785 checks currently cover production non-GL helpers, native tooltip behavior,
router priority/argument values, offsets/edges, native NBT and alias preservation,
sound-record values, complete furnace constructor/foreground methods with a
recording native FontRenderer, and actual native resource packs. A verbatim
background tail tests refresh/power/flame/progress/info drawing parameters with
recording sinks; it begins after the production GL color/texture binding/base
background prefix and is NOT execution of the complete background method. Tile
scale math is controlled; the test verifies supplied scale arguments and GUI
coordinate arithmetic, not real furnace tick accounting. Native EMPTY renderer
early-return behavior is executed without a GL context.

Logs: furnace-gui-native.log, furnace-gui-source.log and
furnace-gui-api-evidence.log in continuation-2026-10-19. Unsafe is used solely to
allocate native headless Minecraft, sound, font and player fixtures without
starting their real client subsystems. No GL display, actual audio playback,
NEI installation/event integration, packet delivery, runnable mod, client/server
session or world-save acceptance is claimed. Windows execution is untested.
The original mixed CRLF/LF spelling of GuiInfoContainer is preserved; default
git diff --check reports its CRLF as trailing whitespace. With cr-at-eol enabled,
these three source diffs pass. No original newline normalization was performed.

## Integration ordering

check-furnace-gui-source.py exports FILES and reverse_source(relative,text).
Compose this NEW exact layer before checkpoint 17's EMPTY boundary, then the
existing player/naming/sided and earlier audit layers. The three GUI files are
not owned by the checkpoint 17 EMPTY group, so the existing guards need no GUI
file payload changes. Keep all old hashes unchanged. The new checker rejects
current full-file drift before returning the frozen baseline text and checks
baseline-text SHA256 independently, without requiring a local Git object.
The same manifest separately owns the exact stored resource rename and hash.
