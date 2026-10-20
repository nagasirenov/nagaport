# Native item renderer assessment: core_angel

Read-only production assessment at 24dab0c, 2026-10-19. No production changes or Gradle execution. This document is not a claim of client rendering parity.

## Evidence provenance

Pristine build.gradle line 40 pins Forge 1.7.10-10.13.4.1614-1.7.10. Official build-specific source was inspected from:
https://maven.minecraftforge.net/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/forge-1.7.10-10.13.4.1614-1.7.10-userdev.jar
SHA256: 6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91.
Embedded src/main/java/net/minecraftforge/client/ForgeHooksClient.java SHA256: a9361c180f54836f84527b2abcc65f90c4ad27aff527113504ac754a406d78cd.
Embedded forgepatches.zip supplies ItemRenderer, RenderItem, RenderPlayer and renderer/tileentity/RenderItemFrame patches. Current official GitHub 1.7.10 branch was useful for readable names, but build-specific source above is authoritative.

Original vanilla caller bytecode: official client already supplied at /tmp/nagaport-legacy-client.jar, SHA256 a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce. Official MCP mapping archive https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/1.7.10/mcp-1.7.10-srg.zip identifies bly=ItemRenderer, bny=RenderItem, bop=RenderPlayer, bnx=renderer/tileentity/RenderItemFrame. No downloaded executable was run; javap read class bytecode. Do not distribute the client.

Native bytecode: .gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar, classes RenderItem, ItemRenderer, RenderEntityItem, LayerHeldItem, RenderItemFrame. Forge native sources.zip verifies ForgeHooksClient.handleCameraTransforms multiplies the supplied matrix directly (with flip-X conjugation for left hands). Do NOT run a directly derived matrix through additional block-center/corner conversion.

## Bounded real slice and ownership

ClientProxy:601 registers core_angel -> ItemRenderAngelCore. ModItems:4293 makes an ItemBattery, max stack 1; :5923 registers it. ItemBattery has no full-3D flag, use animation, or enchantability override. ItemRenderBase is unchanged against pristine. AngelCore supplies inventory T(0,-3,0), S(4); common S(4), smooth-shaded bfangel body and bfangel_tex. ResourceManager:436/:949 name models/mobs/balefire_angel.obj and textures/entity/bfa_body.png. Preserve actual mesh group and texture.

Use native ModelRegistryEvent/ModelBakeEvent registration for this item only and Item.setTileEntityItemStackRenderer. Existing model handlers must retain ownership of their existing items. Native IBakedModel isBuiltInRenderer=true dispatches to TEISR after T(-.5,-.5,-.5); getQuads can legitimately be empty only because TEISR draws the complete real mesh. For this static slice put every geometric operation in perspective matrices and let TEISR draw the raw mesh, with no extra S(4). This removes any need for mutable global current-perspective state. Core has no entity-dependent animation; do not build generalized entity-context infrastructure prematurely.

## Exact ordered transforms

Column vectors; products listed left-to-right in OpenGL call order. T is translation, S uniform scale unless three components supplied, Rx/Ry/Rz degrees. C=T(.5,.5,.5), D=inverse(C). Native total is caller * P * D * rawMesh. Matrices below include ALL original AngelCore/Base scales, including common S(4); applying the old renderer again would double-transform.

Original equipped non-block helper from ForgeHooksClient.renderEquippedItem:
H = T(0,-.3,0) S(1.5) Ry(50) Rz(335) T(-.9375,-.0625,0).
This helper runs even though EQUIPPED_BLOCK=false. That false does NOT mean identity.

### GUI

Original caller: T(x,y,zLevel-3).
Original full renderer: T(8,10,0) Rx(-30) Ry(45) S(-1) T(0,-3,0) S(16).
Native caller: T(x+8,y+8,zLevel+100) S(16,-16,16).
Therefore direct perspective matrix:
P_GUI = S(1/16,-1/16,1/16) T(0,2,-103) Rx(-30) Ry(45) S(-1) T(0,-3,0) S(16) C.
Both effect-GUI entry points add 50 to zLevel, so that common increment cancels. The -103 is deliberate exact old-vs-new depth placement, not an arbitrary aesthetic offset. Modern GUI depth ordering must still be visually checked; do not silently remove it. Lighting is separately restored with the equivalent GUI-standard lights.

### Dropped ENTITY -> GROUND

Original non-block helper uses S(.5), does NOT loop multiple copies, and preserves requested bobbing and rotation. Original total after world translation/bobbing and Y rotation: S(.5) S(1.5) S(.25) Ry(90) S(4) = S(.75) Ry(90).
Native RenderEntityItem adds 0.25 * getItemCameraTransforms().ground.scale.y to Y before perspective, besides the same bobbing. With that scale.y=1:
P_GROUND = T(0,-.25,0) S(.75) Ry(90) C.
Return isGui3d=true, identity camera transforms, and keep the ground camera scale fixed at 1; otherwise recompute lift correction. Valid core stacks have count 1. Native illegally oversized command/NBT stacks can produce multiple copies unlike original; strict support for those requires separate dropped-item handling, not a fake isGui3d trick.

### Ordinary right-hand FIRST_PERSON

Let s=swing progress; a=sin(pi*s*s), b=sin(pi*sqrt(s)); equipDown=1-originalInterpolatedEquippedProgress, matching native equip argument. Common caller translation is:
V = T(-.4*b, .2*sin(2*pi*sqrt(s)), -.2*sin(pi*s)) T(.56,-.52-.6*equipDown,-.72).
Common rotations W=Ry(45-20*a) Rz(-20*b) Rx(-80*b).
Original caller=V W S(.4); native caller=V W Ry(-45).
Original renderer suffix after H is T(.5,.25,0) Ry(90), because S(.25) and common S(4) cancel.
P_FIRST_RIGHT = Ry(45) S(.4) H T(.5,.25,0) Ry(90) C.
This is constant for the ordinary non-use core item even during swing/equip. No RenderSpecificHandEvent override is needed for these geometry transforms. Equip timing/state progression and broader camera motion are separate runtime checks, not proven by matrix algebra.

### Ordinary standing right-hand EQUIPPED -> THIRD_PERSON_RIGHT_HAND

Measure relative to the already posed right-arm matrix A, not world origin. Original RenderPlayer plain-item branch:
O3 = T(-.0625,.4375,.0625) T(.25,.1875,-.1875) S(.375) Rz(60) Rx(-90) Rz(20) H T(.5,.25,0).
Native LayerHeldItem right-hand suffix:
N3 = Rx(-90) Ry(180) T(.0625,.125,-.625).
P_THIRD_RIGHT = inverse(N3) O3 C.
Then A N3 P_THIRD_RIGHT D = A O3. Do not use the full-3D tool branch: core_angel is a plain ItemBattery. Sneaking native LayerHeldItem adds T(0,.2,0) BEFORE A; original/player root and posed-arm differences need a separate pose comparison. Likewise slim arms, child mobs, and non-player holders are not certified by this standing-player derivation.

## Item-frame finding: existing behavior, not a new context

Original RenderItemFrame constructs a count-1 EntityItem, sets hoverStart=0, and calls RenderManager with partialTicks=0 while RenderItem.renderInFrame=true. Original Forge custom core route takes the non-block branch, so its frame-specific block helper does NOT execute. Its effective inner draw is T(0,.1,0) S(.75) Ry(90), from age=0 bobbing and the same core entity mesh. The outer original frame path includes yaw, -90*rotation and rotation-dependent XY offsets: r1=(-.16,-.16), r2=(0,-.32), r3=(.16,-.16), r0=0, plus the frame's wall-relative placement.

Native FIXED uses frame facing rotation, +45*rotation, then S(.5), without a synthetic EntityItem. Consequently FIXED is NOT simply GROUND, and one context-only constant cannot encode all legacy rotation-dependent XY offsets unless their combination with differing outer frames is first proven equivalent. Frame event exposes the actual entity and its rotation and can implement any necessary per-frame correction. Existing ModEventHandlerClient.renderFrame only replaces flame_pony/paper; core was not intercepted. The old four orientation placements must be derived/verified against new eight rotations before claiming whole-item completeness. No user choice is needed to preserve the old placements; policy for newly available 45-degree placements is a separate extension.

## Glint and overlay contract

Original core mesh draws no glint. ItemBattery does not override hasEffect/enchantability; a normal core has no enchantment effect. More importantly, original custom Forge dispatch bypasses vanilla equipped/entity glint, and GUI patch explicitly disables old global effect overlay, requiring custom renderers to implement effects themselves. Core does not. Even a forced enchanted-tag core must retain this no-glint mesh behavior for parity. Native TEISR's bypass of standard glint is therefore correct for THIS item, not a regression to fix. Preserve ItemBattery durability/charge bar and native outer inventory overlays; TEISR must not draw duplicate bars.

## Verification performed and remaining

Executed an ephemeral NumPy matrix comparison (no project-code execution): 101 swing/equip samples, 101 ground rotations/translations, one GUI and one standing arm-local third-person equality, 204 comparisons total. Maximum absolute difference 2.220446049250313e-16. This validates composition algebra under the stated extracted-caller assumptions, not rendered pixels or animation timings. Tests should be committed as independent expected-original versus actual-native matrix tests in the implementation slice, using float tolerances and known basis vertices rather than self-comparing a matrix factory.

Production next step can implement the four explicitly derived original contexts with actual asset/group tests and native registration tests, without a user design decision. Do not call this whole-item rendering complete until item frames, sneaking/holder poses, GL lighting/state, reload and actual baseline visual comparisons pass. New left/offhand handling, HEAD, NONE, and frame intermediate angles must remain explicitly classified, not silently map to an old enum. A fallback must be a real, existing original baked item model where appropriate; an invisible/missing model is not acceptable.

HEAD/NONE can also occur through nonstandard equipment or direct caller APIs; their chosen presentation is not established by the four old callback types. Left-hand mirroring via Forge flip-X is available technically, but not evidence that mirroring is correct for every NTM item or weapon. The core slice does not justify broad weapon conversion. Sedna custom projection, entity state, recursive template rendering, glint-owning renderers and per-item helper policies remain separate families.

Required client checks: original/native images for inventory and hotbar (including charge bars), dropped stationary/moving camera, swing/equip transition, standing/sneaking third-person, all old frame orientations; lighting directions, smooth normals, culling, depth, neighboring-item state leakage, reload. Headless success is not a working client/gameplay claim.
