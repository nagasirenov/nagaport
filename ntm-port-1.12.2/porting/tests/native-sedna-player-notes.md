# Native Sedna player/armor companion

PASS 145 selected genuine-native checks, exit 0, in `../continuation-2026-10-19/sedna-player-native-FROZEN.log`. No production edits were required by the final added checks. Six companion production files and the parent-owned ClientProxy integration hash are recorded in `native-sedna-player-scope.json`. This companion intentionally supersedes only the intermediate ModEventHandlerRenderer hash in the root manifest. The 51 consumer substitutions, exact 67 registrations and 44 effect-vertex source contract were rechecked after this migration.

## Ownership and activation

The existing CP30 contextual RenderItem installer/registration are unchanged. After its success, a separate Sedna transaction requires that exact installed ContextRenderItem identity, exact native RenderManager, every reachable native RenderPlayer, exact native ModelPlayer/ModelBiped and known native layer ownership. It copies native model fields, retaining native ModelRenderer and cached head identities. Unknown model/layer/renderer ownership rejects with an explicit warning, preserving the successful Boltgun registration and leaving Sedna TEISRs/models/player layers unchanged. The transaction includes all player/armor/layer assignments, model registration activation and hand subscription, with rollback on failure.

The exact production ClientProxy.onLoadComplete method is extracted verbatim into a selected harness, not reimplemented. Executed cases prove success, player-companion rejection, contextual-renderer rejection, the committed-only one-mesh union, deferred dropped-item owner construction, repeated-load idempotence and missing-registration rejection. The complete surrounding legacy ClientProxy is not compiled by this probe. Parent's frozen full-tree diagnostic compile is separate evidence.

## Actual render phases and retained semantics

The probe calls actual native RenderPlayer.doRender and real native armor/held layers. It proves four legacy manual skin-arm opportunities (including empty armor slots), actual akimbo stack/holder delivery, left-handed and mirrored offhand routing, canceled player frames, the ordinary following frame, first-person empty-arm isolation, armor-disable hiding/restoration, and exception-safe body visibility. It uses the actual migrated ModelMan/ModelArmorBase, observing all six ModelMan parts on each of the four armor phases.

One targeted native iron chestplate test executes its ordinary draw plus both native enchantment-glint passes. The effective original standard armor pose is the recalculated bow Y (.5 + head yaw), not the temporary SetArmorModel manual skin-arm Y (.1 + head yaw); the test proves this distinction. NTM custom armor models copy the current main model through the separately tested native model root. The old right-hand akimbo matrix prefix, setup choice and actual equipped body are retained; physical left/offhand is mirrored as approved. ModelMan held items use the native held-layer implementation anchored to the actual ModelMan arm, including native item model transforms and fishing-stick substitution, rather than a legacy renderer shim.

Original armor-disable and ModelMan conditions remain. Visibility is lexical instead of a stale global Pre/Post array. The only production IArmorDisableModel implementation is pure; stateful external-mod predicates and unknown model/layer owners are not certified. Unrelated ModEventHandlerRenderer methods remain outside the moved phases and are covered by the parent's bounded source-boundary proof.

## Test boundary

Cached real Forge/Minecraft/LWJGL/EGL and genuine Forge event transformation are used; there are no fake platform classes. Actual native RenderPlayer/model/layer constructors and real native world/player subclasses execute. The shared contextual test fixture allocates Minecraft/RenderManager and selected services with Unsafe to avoid a whole client startup. This companion also allocates a native EntityRenderer with Unsafe and initializes its genuine fogColorBuffer for actual glint execution. This is not proof of launched native initialization. A normal native EntityPlayerSP constructor receives controlled real network/settings/services without opening a connection. NTM resource meshes, gun item state/config/animation collaborators and observation ModelRenderer subclasses are controlled test collaborators. Observation arms are installed only after strict production preflight succeeds.

Earlier failed fixture runs exposed absent fog-buffer and an unregistered dummy head-item model. The final fixture initializes the native fog buffer and exercises armor-disable through a chest slot, avoiding an unrelated dummy custom-head draw; the final run contains no native render errors. The separate armor-model root's constructor-based PASS 373 must not be conflated with this fixture's initialization strategy.

No launched game, pixel screenshots, all-weapon live animation/material rendering, shader compatibility, unknown third-party takeover, or complete family compilation is claimed. Root PASS 171 and model PASS 373 retain their separate scopes.

Run without installs/network:

```
python3 porting/tests/probe-native-sedna-player.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8
```

The final delivered 145-check run executes the frozen CP31 snapshot, including original static dropped-item dependencies, excluding concurrent conveyor-wand changes. All 37 emitted production dependency hashes match that snapshot. The earlier working-tree run is not the final checkpoint evidence.
