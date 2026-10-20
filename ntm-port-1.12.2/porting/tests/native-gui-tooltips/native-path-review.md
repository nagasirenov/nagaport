# Native List tooltip path review

## Finding

The 80 selected calls in 39 source files correctly reach the normal Forge 1.12.2 List-tooltip path. This is a native behavior migration, not pixel- or state-identical emulation of 1.7.10. Under the stated acceptance of normal 1.12 layout and hooks, this bounded audit found no source-level blocker. The important state difference is that the old renderer reset the caller's two depth fields to zero; the native utility leaves them alone.

## Evidence and dispatch

Reviewed the supplied bytecode dumps `/tmp/tooltip-original-screen.javap`, `/tmp/tooltip-native-screen.javap`, `/tmp/tooltip-native-utils.javap`; `net/minecraft/client/gui/GuiScreen.java.patch` inside `/tmp/nagaport-forge1614-patches.zip`; and `porting/tests/native-gui-tooltips/{before,after}-attribution.tsv`, scanner, manifest, and relevant source. Base/HEAD was `5e6ba284ef32b46ad5f81cd1e1570c08b53cb24f`.

- Old bytecode: `bdw.a(java.util.List,int,int)`, descriptor `(Ljava/util/List;II)V`. The existing `/tmp/bullet-joined.srg` independently maps this to `GuiScreen.func_146283_a`; `bbw.e` is `Gui.field_73735_i` and `bny.f` is `RenderItem.field_77023_b` (the depth fields). Forge 1614's patch moves this body's rendering to `protected drawHoveringText(List,int,int,FontRenderer)` and delegates the three-argument method with `field_146289_q`. The patch changes font accesses, not layout/depth logic, and adds no tooltip event calls.
- Native bytecode: `public GuiScreen.drawHoveringText(List<String>,int,int)` invokes `protected GuiScreen.drawHoveringText(List<String>,int,int,FontRenderer)` with the screen's `fontRenderer`. That overload calls `GuiUtils.drawHoveringText(List<String>,int,int,int,int,int,FontRenderer)` with the same list/x/y, screen `width`, screen `height`, and `maxWidth=-1`. The utility delegates to its `ItemStack,List,...` overload using `cachedTooltipStack`.
- Of the 96 recorded tooltip-related invocations, exactly 80 selected legacy calls were unresolved before and resolve after to `net.minecraft.client.gui.GuiScreen:drawHoveringText(java.util.List<java.lang.String>,int,int)`. All 80 have attributed argument types `[java.util.List<java.lang.String>, int, int]`, GuiScreen-subtype receivers, and only native GuiScreen tooltip overloads in their member closures. Receivers are 54 explicit `this` and 26 implicit. The other four pre-existing native List calls are not part of this migration.
- Source declaration capture contains no `func_146283_a` or `drawHoveringText` overrides. Its only two `drawGradientRect` declarations are in unrelated `ActorBasicPanel` and `ActorFancyPanel`, not the selected GUI hierarchy. Native utility backgrounds are static calls, so no selected source gradient override is bypassed.

## Default layout and hooks

Assume a nonempty list and no event-handler modifications. Both versions measure formatted string widths with their selected font, start at `(mouseX+12, mouseY-12)`, use white shadowed text, 10-pixel line advances, an 8-pixel one-line content height, and the same default colors: background `0xF0100010`, border start `0x505000FF`, border end `0x5028007F`.

- Old: no wrapping; move left to `mouseX-16-textWidth` only if `x+textWidth > screenWidth`. Multiple-line height is `8+2+10*(lineCount-1)`, with the extra 2 pixels after the first line. Bottom overflow sets `y=screenHeight-height-6`; no top clamp.
- Native: right-edge test includes a 4-pixel margin. If moving left also leaves `x<4`, choose available width on the mouse's side and wrap using `FontRenderer.listFormattedStringToWidth`; positive `maxWidth` can also force wrapping, although the screen supplies -1. Re-measure wrapped lines and place them on the chosen side. Add the 2-pixel title gap only after all wrapped pieces of the original first line and only include it in height when body lines remain. Set `y=4` when initial y is below 4; otherwise bottom overflow sets `y=screenHeight-height-4`. This is not a general guarantee that over-tall tooltips fit.
- Native posts cancellable `RenderTooltipEvent.Pre` before its GL changes and reads back x/y, screen dimensions, maximum width, and font. It then posts `Color`, consumes the returned colors, draws the background, posts `PostBackground`, draws text, and posts `PostText`. Empty lists post no events; canceled Pre returns without this utility's GL changes. The plain List path does not perform item-specific font lookup or itself call pre/post item-tooltip cache setup; normally its cached stack is `ItemStack.EMPTY`.
- The source deliberately passing `(0,900)` in `GUIScreenRecipeSelector` now receives native bottom clamping/wrapping, including the 4- rather than 6-pixel bottom margin. This is an observable accepted layout change, not preserved old pixels.

## Depth fields and render state

Old nonempty tooltip bytecode writes caller `Gui.zLevel=300` and the screen's `RenderItem.zLevel=300` before background rendering, then writes both to 0 on normal completion (offsets 185–196 and 563–572). These are forced resets, not restoration of saved incoming values. Empty lists do not write them.

Neither native GuiScreen overload nor the native GuiUtils tooltip body writes either depth field. Each native background calls static `GuiUtils.drawGradientRect(int zLevel,int left,int top,int right,int bottom,int startColor,int endColor)` with explicit z=300. Text goes through `FontRenderer.drawStringWithShadow`. Therefore incoming nonzero field values are preserved by the native implementation itself rather than reset to zero; third-party event handlers can of course change state independently.

The 80 calls occur in 40 source methods; none of those methods directly assigns either depth field. In selected files, all direct writes are separately balanced on normal control flow: six machine GUI background methods set `this.zLevel` to 300 then 0, and `GUIScreenRecipeSelector.renderItem` sets item-render depth to 100 then 0. In inherited `GuiInfoContainer`, `drawItemStack`, `renderItem`, and custom `drawStackText` reset their own depth writes to 0. Its `setZLevel` has no callers in main source. No selected caller was found relying on the old tooltip to clean up its own depth writes. This is source evidence, not proof against external mods or exceptional paths.

Both old and native successful nonempty paths disable rescale-normal, standard item lighting, lighting, and depth for drawing, then enable lighting, depth, standard item lighting, and rescale-normal. Neither is a general save/restore of incoming GL state or exception-safe finally cleanup. The native event hooks introduce intentional additional third-party extension points.

## Limits

This was read-only analysis plus this report, with no main-source edits, downloads, installs, pushes, fabricated native classes, or GL/pixel test. Attribution logs explicitly report a still-incomplete whole-mod compile (15,608 diagnostics before; 15,528 after). Correct resolution of these 80 calls does not establish a successful mod build or runtime rendering. Exact old margins, wrapping, hook absence, or zero-reset behavior must not be claimed preserved.
