# RenderManager construction and registration boundary

Assessment date: 2026-10-19. Read-only inspection of production sources, official Forge sources and actual mapped target bytecode. This document is **not a client startup/runtime pass**.

## Target and reproduction

Target: Minecraft 1.12.2, Forge 14.23.5.2768, MCP stable_39.

Mapped target jar in this checkout:
`.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`.

Official Forge source archive in the resolved cache:
`caches/minecraft/net/minecraftforge/forge/1.12.2-14.23.5.2768/userdev/sources.zip`.
The current cache root is `/workspace/shared/nagaport-toolchain/gradle-user-home`.

Commands, run from the project with the target JDK's `javap` on PATH:

```sh
TARGET='.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar'
javap -c -private -classpath "$TARGET" net.minecraft.client.Minecraft
javap -c -classpath "$TARGET" net.minecraft.client.renderer.entity.RenderManager
javap -private -classpath "$TARGET" net.minecraft.client.renderer.entity.Render
unzip -p "$FORGE_SOURCES" net/minecraftforge/fml/client/FMLClientHandler.java
unzip -p "$FORGE_SOURCES" net/minecraftforge/fml/client/registry/RenderingRegistry.java
```

## Actual ordering

1. `MainRegistry.java:253` handles `FMLPreInitializationEvent` in `PreLoad`. At line 287 it invokes `proxy.registerRenderInfo()`.
2. `ClientProxy.java:165` implements that hook; at line 180 it calls `registerEntityRenderer()`, defined at line 651. Existing registrations eagerly construct their renderer instances there.
3. Target `Minecraft.init()` bytecode offset **263** invokes `FMLClientHandler.beginMinecraftLoading(...)`.
4. Official `FMLClientHandler.java:214–252` shows `beginMinecraftLoading` calling `Loader.instance().preinitializeMods()`. Thus the NTM calls above occur inside this earlier stage.
5. Only later, `Minecraft.init()` offsets **823–838** allocate `RenderManager(TextureManager, RenderItem)` and assign the `Minecraft.renderManager` field.
6. `RenderManager`'s own constructor at offset **1808** invokes `RenderingRegistry.loadEntityRenderers(this, entityRenderMap)`. The manager supplied to each factory is valid, but `Minecraft.getRenderManager()` has not yet received this new object while its constructor runs.
7. Still later, `Minecraft.init()` offset **1024** invokes `FMLClientHandler.finishMinecraftLoading()`. Official source lines 327–363 shows `initializeMods()` followed by `RenderingRegistry.loadEntityRenderers(Minecraft.getMinecraft().getRenderManager().entityRenderMap)` for the deprecated instance registrations.
8. Native `Render` requires `Render(RenderManager)` and stores its manager in a final field. There is no legitimate delayed setter equivalent to constructing it with a null manager.

## Native registry semantics

In the exact target `RenderingRegistry.java`:

- The deprecated overload accepts a constructed `Render` and immediately stores it in `entityRenderersOld`. Its documented stage is initialization. The one-argument `loadEntityRenderers(Map)` later performs `putAll` into the real map.
- The factory overload accepts `IRenderFactory<? super T>` and stores it in `entityRenderers`. Its documented stage is preinitialization.
- The two-argument loader iterates that factory **HashMap** and calls `factory.createRenderFor(manager)` for each entry. Factory invocation order is not source registration order.
- Capturing a factory does not preserve eager constructor/class-initializer resource loading. Using the supplied manager is mandatory; fetching the global Minecraft manager inside a factory is not equivalent.

## Incorrect and acceptable patterns

**Do not** add `super(Minecraft.getMinecraft().getRenderManager())` to a constructor still invoked by current preinit direct-instance registration. The global field is not initialized yet. Do not assign a dummy manager or add a fake Minecraft/Forge compatibility class.

For constructors/initializers with no externally observable work, add an explicit `RenderManager` argument and pass the factory-supplied manager through `super(manager)`. Keep separate instances for separate entity registrations. Preserve existing field values, rendering bodies and fluent configuration; only change the construction/registration boundary.

For eager resource loaders, perform the same resource lookup/preload at the original preinit registration point, in original statement order. Capture that resource snapshot in a factory and pass it to `Renderer(manager, preloadedMesh)`. Do not silently move it to first render, perform duplicate loads, or subscribe previously non-reloaded models to a new reload path. Static resource fields require explicit consideration because class initialization itself may otherwise move later.

Moving all construction to initialization and retaining the deprecated overload would avoid null managers, but changes all constructor/field-initialization/resource/RNG/GL timing at once. It is not the preferred blanket fix.

## Coordinated constructor-root inventory

At this assessment snapshot there are **77 direct raw `extends Render` classes**, all without an explicit `super(...)` call: 31 have explicit constructors, 46 rely on implicit constructors. `RenderQuasar extends RenderBlackHole` is the one additional descendant. `MultiCloudRenderer` has the only constructor overload/delegation chain in this set. All **122** construction expressions for the 78-class closure are in `ClientProxy`; they cover 77 distinct classes (`RenderBalls` is not constructed there). These counts exclude the separate RenderLiving/RenderBiped/RenderMinecart families.

A conservative **52-class / 94-construction-expression** subset has no resource IO, GL allocation, RNG/clock sampling or model construction in the reviewed constructor/field/static initialization. Their initialized fields are literals, ResourceLocation values, or RenderTorex's comparator. MultiCloudRenderer only assigns provided item-array/int parameters and delegates constructors; capture/evaluate registration arguments at the original site rather than moving arbitrary expressions into factory bodies.

Exact subset, relative to `src/main/java/com/hbm/render/entity/`:

- `RenderEmpty.java`
- `effect/{FogRenderer,MultiCloudRenderer,RenderCloudTom,RenderFlare,RenderMist,RenderSpear,RenderTorex}.java`
- `item/{RenderBomber,RenderC130,RenderDeliveryDrone,RenderMagnusCartus,RenderMovingItem,RenderMovingPackage,RenderNeoCart,RenderParachuteCrate,RenderTrainCargoTram,RenderTrainCargoTramTrailer}.java`
- `mob/{RenderBFA,RenderDrone,RenderSiegeCraft,RenderSiegeUFO,RenderUFO}.java`
- `projectile/{RenderArtilleryRocket,RenderArtilleryShell,RenderBeam,RenderBoxcar,RenderBulletMK4,RenderChemical,RenderCog,RenderFOEQ,RenderGenericGrenade,RenderGrenade,RenderMeteor,RenderRBMKDebris,RenderSawblade,RenderSiegeLaser,RenderTom,RenderVortexBeam,RenderZirnoxDebris}.java`
- `rocket/{RenderCombatDropPod,RenderMinerRocket,RenderMissileCustom,RenderMissileGeneric,RenderMissileHuge,RenderMissileNuclear,RenderMissileShuttle,RenderMissileStrong,RenderMissileTaint,RenderRocketCustom,RenderSoyuz,RenderSoyuzCapsule}.java`

This subset is constructor-lifecycle safe by inspection, **not otherwise compiled or renderer-complete**. Its bodies still contain legacy tessellation/entity/texture APIs. No custom entity fields should be fabricated merely to compile them.

Excluded for explicit handling:

- Constructor resource loads: RenderBlackHole, RenderCloudFleija, RenderCloudRainbow, RenderCloudSolinium, RenderEMPBlast, RenderFallingNuke, RenderBombletTheta.
- Static resource/registration loads: RenderDeathBlast, RenderBalls, RenderPlasticBag, RenderSiegeTunneler, RenderWormBody, RenderWormHead; RenderCoin additionally allocates VBOs. HFR constructors register models for reload.
- Time/RNG: RenderBeam5 creates Random; RenderFallout creates Random and samples System.nanoTime.
- Old RenderBlocks construction: RenderFallingBlockNT, RenderTNTPrimedBase.
- Model construction requiring separate constructor audit: RenderHunterChopper, RenderBoatRubber, RenderChopperMine, RenderRubble, RenderRocket, RenderShrapnel, RenderBullet.
- RenderQuasar must receive a constructor when its RenderBlackHole parent changes and must share that parent's explicit preload policy.

## Validation boundary for a constructor-root patch

- Statically compare every rendering method and non-constructor field initializer against the prior source, preserve registrations/counts and per-entity distinct instances.
- Native tests can compile the complete existing RenderEmpty and newly completed concrete mesh consumers. A fixture can invoke the real Forge factory loader with a controlled real RenderManager instance and verify supplied-manager identity, no factory construction at registration, one construction per registration, and final map entries. This does not prove Minecraft startup.
- MultiCloudRenderer's chaining and remaining constructor transformations can be checked by source/AST invariants; extracted constructor-only tests must be explicitly labeled, not presented as compilation of complete renderer classes.
- Run full production compile after the coordinated patch. Legacy method bodies and other renderer roots remain independent blockers. Client startup, resource reload, visual output and dedicated-server classloading still require later acceptance once the full mod builds.

## Implemented bounded constructor-root batch

The 52-file subset above now has explicit supplied-manager constructors; its 94 registration sites now use native factories. Four MultiCloudRenderer item arrays are evaluated immediately into final locals before factory registration. Rendering methods, field initializers and previous constructor statements are unchanged. No broad generic-type migration was necessary: native factory signatures accept the existing raw Render subclasses with compiler unchecked warnings.

`render-manager-root-manifest.json` records baseline commit f15a65995a9fcd20a8b817d190f540dd0fa0e577, normalized source SHA256 values and exact reversible constructor/import edits. `check-render-manager-root.py` does not require that Git object or Git itself: it reverses only recorded edits and compares source fingerprints, then checks all 94 registration targets, argument captures and order. This is a strict snapshot/source invariant, not a claim to have freshly compared a recipient's checkout against Git history. Unrelated EMP/PlasticBag/HMF registration edits are intentionally outside the 94-site audit.

`testPortRenderManager` compiles complete production RenderEmpty plus explicitly constructor-only excerpts of 51 other classes against actual target Render/RenderManager/Item classes. The excerpt shells omit render implementations and throw from the required texture method; they contain no fake Minecraft or custom entity classes. It compiles and invokes the native IRenderFactory signature for each of the 94 call shapes. A separate actual Forge registry test checks deferred factory invocation, supplied manager identity, separate instances and the deprecated instance map.

An actual `new RenderManager(null, null)` was attempted once headlessly. Construction reaches RenderLivingBase static initialization, which creates DynamicTexture/TextureUtil and calls GL11.glGenTextures; it failed with `UnsatisfiedLinkError: no lwjgl64 in java.library.path`. No alternate graphics implementation or repeated context setup was attempted. Registry tests therefore use an allocated instance of the actual RenderManager class with unrelated graphics services uninitialized, solely for identity forwarding. They do not test the manager constructor, startup or live rendering.

Run from the project with its pinned toolchain:

```sh
./gradlew -I porting/render-manager-probe.init.gradle testPortRenderManager
```

The implementation worker ran the Python probe directly with the resolved target classpath, not Gradle or full production compilation. Parent integration owns those checks.

Concise evidence from that single constructor attempt (not a game launch):

```text
Exception in thread "main" java.lang.UnsatisfiedLinkError: no lwjgl64 in java.library.path
    at org.lwjgl.Sys.loadLibrary(Sys.java:87)
    at org.lwjgl.Sys.<clinit>(Sys.java:117)
    at org.lwjgl.opengl.GLContext.<clinit>(GLContext.java:112)
    at org.lwjgl.opengl.GL11.glGenTextures(GL11.java:1403)
    at net.minecraft.client.renderer.GlStateManager.generateTexture(SourceFile:482)
    at net.minecraft.client.renderer.texture.TextureUtil.glGenTextures(TextureUtil.java:38)
    at net.minecraft.client.renderer.texture.AbstractTexture.getGlTextureId(SourceFile:46)
    at net.minecraft.client.renderer.texture.DynamicTexture.<init>(SourceFile:26)
    at net.minecraft.client.renderer.texture.TextureUtil.<clinit>(TextureUtil.java:26)
    at net.minecraft.client.renderer.texture.AbstractTexture.getGlTextureId(SourceFile:46)
    at net.minecraft.client.renderer.texture.DynamicTexture.<init>(SourceFile:26)
    at net.minecraft.client.renderer.entity.RenderLivingBase.<clinit>(RenderLivingBase.java:29)
    at net.minecraft.client.renderer.entity.RenderManager.<init>(RenderManager.java:145)
    at RealManagerCheck.main(RealManagerCheck.java:2)
```
