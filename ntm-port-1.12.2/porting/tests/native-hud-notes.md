# Native HUD callback root

Three complete production classes now use native GUI RenderItem, overlay-event accessors, native player/icon fields, BufferBuilder POSITION_TEX and GlStateManager cached state setters. Geometry, original arithmetic, thresholds, strings, durability/count rules and shield/radiation display formulas remain unchanged. No player hazard mechanic is modified. Native renderer is obtained on each icon invocation, avoiding stale ownership when the contextual item renderer installs. Repository-wide search found no readers of the retired static renderer fields and no ammo-counter subclasses.

694 native checks passed. A separate API-only inverse reconstructs all three complete checkpoint-35 source files byte-for-byte, proving untouched display formulas and branches remain unchanged.

Native probe compiles complete RenderScreenOverlay, both HUD components, IHUDComponent and Crosshair against cached Forge2768/MCP39. Explicit collaborators supply mod configuration, magazine state, weapon config and player properties. Actual Forge overlay events, Minecraft GUI resolution, FontRenderer, item model mesher, RenderItem, texture loading, BufferBuilder and Mesa EGL execute. Icon spy delegates to real RenderItem drawing. Counters/mirroring/no-counter/non-HOTBAR branches, original icon identity/count/NBT, gun index, durability values and offsets are checked. Renderer replacement is observed dynamically. Native cached blend/alpha/depth/rescale/color state is checked against the actual GPU, respecting the native -1 invalid-color sentinel. A temporary controlled mutation restoring the old raw durability-color call fails that genuine cache check; production files are never mutated. Scope framebuffer is compared against independently written original quad/UV equations for four aspect ratios, with a nonblank guard; flashbang checks genuine drawing and original exit state.

The broader overlay class compiles completely; only enumerated branches run. No screenshot, full client startup, player radiation integration, actual weapon inventory/server/save or Windows acceptance. Native narrator library warning is unrelated to the tested GUI; no missing texture error was observed.

Run from module root:

```sh
python3 porting/tests/check-native-hud-source.py
python3 porting/tests/probe-native-hud.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"
```

Manifest `native-hud-scope.json` pins the three production files. Final run log is `../continuation-2026-10-19/native-hud-FINAL.log`.
