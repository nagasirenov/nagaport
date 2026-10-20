# Native callback item family (checkpoint 26)

Baseline: checkpoint25,40f6979edd0a80cb7a94cfa91410f40aa5fd02a6.

The mod-owned ItemRenderBase is now a real TileEntityItemStackRenderer, not IItemRenderer or a replacement Forge API. Its existing ordered renderCommon/renderCommonWithStack callbacks receive the actual unchanged stack. The 74 provider/interface declarations now return this native NTM type. Original provider/library callback bodies remain unchanged, including stack/NBT/metadata, animations and material selection.

CallbackItemModel is a native built-in IBakedModel. It composes previously derived perspective matrices with the original inventory/non-inventory matrix callbacks. Those callbacks were audited to perform only matrix operations (including time-dependent rotations and the ArmorFSB matrix helpers). Each transform is captured in a balanced local GL matrix scope on the render thread, including exceptional exits. There is no global last stack, holder or render context. Holder/sneaking adjustment uses an immutable per-holder model. Actual common geometry/material callbacks run in the native TEISR, not empty stand-ins.

Registration retains item-keyed ownership and library -> tile-provider -> item-provider -> explicit battery-pack precedence. All metadata use the owner's actual stack; callbacks are not replaced with a single fixed metadata sample. The native model is rebuilt on resource reload; an already available model's atlas particle is retained. If that native base particle is absent, the engine missing sprite is retained and an explicit warning records this unresolved texture-registration gate. The actual mesh is not omitted. Full texture/particle coverage is NOT certified until client loading is available.

The existing exact-vanilla-only dropped-item ownership seam now receives the union of static and callback item identities in one registration. No stack count is rewritten; unknown third-party EntityItem renderer ownership is still preserved and warned about. Custom item-frame translation correction uses the previously approved frame policy. This is not a universal compatibility claim.

## Evidence and limits

- Fresh common clean compile:925 ->771 diagnostics, still failed. Declaration roots remain, so this does not prove every provider's body has passed semantic compilation.
- Native callback probe:42 checks of real native model/TEISR APIs, actual offscreen GL matrix/state restoration, animated matrix callbacks, exact stack identity/NBT/count, callback ordering and exceptional paths, native model registration and resource reload. The draw callbacks are explicitly controlled test renderers. This is NOT all90 anonymous callbacks rendered, not a full machine draw and not an in-game test.
- Native probe command: `python3 porting/tests/probe-native-callback-items.py --java-home JAVA8_HOME --classpath NATIVE_CLASSPATH`. Uses cached Linux LWJGL/Mesa EGL, not a verified Windows GL runner.
- Historical source composition and exact provider-body audits separately certify source preservation, not runtime behavior.

Remaining old direct item renderers, RenderBlocks/ISimpleBlockRenderingHandler, achievements and particles still block the full build. No excluded production sources or platform stubs. Machine operation, world/save/server, inventory and dropped-item irradiation acceptance remain open. The independent machine-item display draft is parked outside production while declaration roots are prioritized.
