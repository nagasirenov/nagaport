# Native material particle-atlas correction

## Defect in checkpoint 21

Checkpoint `290af844793ced6c20380adfaa83aab764ae6197` registered
`hbm:entity/bfa_body` using ordinary `TextureMap.registerSprite`.
Its actual source PNG is 116 by 52, with no animation metadata. The unchanged
Forge 14.23.5.2768 `TextureAtlasSprite.loadSprite(PngSizeInfo, false)` throws
`broken aspect ratio and not an animation` for this image. The previous
`AngelCoreNativeTest.Sprite` constructor supplied only a name, never decoded
the PNG, and therefore did not detect this real lifecycle defect.
The checkpoint was not a runnable/full-build or visual-parity claim, but its
particle registration was nevertheless defective and is corrected here.

## Production correction and exact scope

`MaterialTextureAtlasSprite` loads the real material through the native
resource manager. It returns `true` from `hasCustomLoader` and **false** from
`load`, which tells this Forge version to continue through its native mipmap
generation and stitch path. Returning true would instead skip stitching.

The particle-only canvas is square and rounded up to a multiple of
`1 << TextureMap.getMipmapLevels()`. Every pixel in the original source
rectangle is copied unchanged, with the source orientation retained. Outside
that rectangle only, the last source column/row is repeated. No production
PNG, mesh, mesh UV, mesh texture binding, or render geometry is changed.
The native particle metadata sees the padded canvas, not an unrelated item
icon. Source dimensions are retained separately for diagnostics and tests.

Two additional actual-native constraints require that canvas:

- Forge warns about unsupported mip dimensions but does not lower them before
  generating the sprite's mip data. A raw 116 by 52 frame produces an invalid
  94-element level-three buffer instead of the required 14 by 6 = 84 elements.
- Native `Stitcher.Holder` can rotate rectangular allocations, while
  `TextureAtlasSprite.initSprite` and native atlas upload do not rotate the
  source pixel buffer or swap its dimensions. A square canvas avoids that
  allocation/upload mismatch.

The frame array has `mipmapLevels + 1` entries, initially only level zero
populated. The production helper does not replace or override native mipmap
generation. Dedicated `<namespace>:native_material/<material path>` atlas
keys prevent ownership collisions with normal block/item sprites that use the
same underlying PNG. Registration deduplicates identical material keys;
resource reload replaces the old frame instead of appending to it.

At configured mip level four, the current special sources are core 116 by 52
to 128-square, original geiger 55-square to 64-square, and original solar
mirror 36-square to 48-square. The family worker owns decisions about which
original icons use this helper. This correction does not claim that every
family item uses mesh material particles.

## Durable tests and reusable native GL environment

Use the existing cached Forge 2768 classpath and JDK 8. From the project root:

```sh
python3 porting/tests/probe-angel-core-native.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"

python3 porting/tests/probe-material-atlas-native.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"
```

The atlas script compiles only the actual production helper plus
`MaterialTextureAtlasSpriteNativeTest.java` against unchanged native jars.
It extracts existing Linux LWJGL native libraries from the classpath's
`lwjgl-platform` natives jar into its temporary directory and sets
`java.library.path`. It does not download/install software or replace native
classes. Existing dependencies are JNA 4.4.0, LWJGL 2.9.4, native `libEGL`, and
Mesa desktop OpenGL. `MESA_SHADER_CACHE_DISABLE=true` avoids writing to the
read-only home cache. `java.awt.headless=true` still permits PNG decoding.

The reusable setup is implemented in the Java test's `EGL` interface and
`Context` class: `eglGetPlatformDisplay(EGL_PLATFORM_SURFACELESS_MESA)`,
`eglInitialize`, bind desktop OpenGL, choose a pbuffer-capable OpenGL config,
create a one-pixel pbuffer/context, make it current, and register it with
unchanged LWJGL via `GLContext.useContext`. Cleanup releases the context,
surface, and display. No X server, desktop, window, or network is required.

Native `TextureUtil` initializes a real dynamic GL texture and reads
`Minecraft.getMinecraft().gameSettings.anaglyph`; the test therefore invokes
the real `Minecraft(GameConfiguration)` and `GameSettings()` constructors,
without entering the game loop. It supplies the actual Forge jar's path to
the public `FMLSanityChecker.fmlLocation` launcher field so the native splash
helper can query the genuine GL maximum texture size. It invokes the real
private `TextureMap.initMissingImage` reflectively, then the public unchanged
`TextureMap.loadTextureAtlas`. Global pre-stitch subscribers are intentionally
outside this isolated test because they require the rest of game startup.
There is no Unsafe allocation, fake Minecraft/Forge class, bytecode patch,
mock native mipmap generator, or fabricated PNG.

The atlas test first reproduces the ordinary-loader rejection using the real
core PNG and verifies that a dedicated material sprite does not overwrite an
ordinary sprite key. At mip levels zero through four, it then checks the
actual native custom-loader branch, inherited mipmap generator, native
Stitcher, native GL upload, and GPU pixel readback. It verifies all source
pixels and edge padding, mip buffer dimensions, atlas boundaries, pairwise
non-overlapping allocations, lookup identity, deduplication, reload frame
replacement, unchanged configured mip level, and no GL errors.

Default assets are core, the first five furnace/heater mesh materials, and
the actual geiger/solar-mirror original particle icons. Extra bounded asset
sets can be supplied with repeated `--material hbm:textures/...png` options.
No full-family material sweep was run or is needed for the current icon route.

## Recorded result

- AngelCore: **27,060** independent matrix/native model/actual asset checks.
  Its former name-only Sprite fixture is removed and the real PNG is loaded.
- Atlas: **1,661,476** assertions over **8 actual PNGs**, configured mip levels
  **0, 1, 2, 3, and 4**, using OpenGL 4.5 Compatibility Profile Mesa
  25.0.7-2+deb13u1. The real native atlas is 512 by 512 at each tested level.
- Logs: `porting/continuation-2026-10-19/angel-core-material-fixed-native-probe.log`
  and `porting/continuation-2026-10-19/material-atlas-native-probe.log`.

These are isolated native atlas and existing matrix/asset proofs, not a game
launch, full-client resource-pack lifecycle, whole-mod build, mesh draw, or
visual parity claim. Configured mip levels above four and arbitrary external
resource packs have not been validated. Square padding is deliberately
particle-atlas-only and may change the particle metadata's sampled canvas.
