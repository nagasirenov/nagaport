# Native toaster and computer owner family

## Production scope

`BlockDecoToaster`, `BlockDecoModel`, their inherited `BlockDecoContainer` callback owner, `RenderToaster`, `RenderBlockDecoModel`, `DecoApplianceBakedModel`, `DecoApplianceModelHandler`, and the toaster/computer blockstate JSONs form the native owner/model path. No original mesh or texture bytes change. The filing cabinet retains its invisible block/TESR rendering route; its existing renderer, tile engine, inventory GUI and lock implementation are not claimed migrated by this checkpoint.

Two tightly bounded shared fixes make the complete owners compile: `BlockMulti.damageDropped(IBlockState)` dispatches through the unchanged virtual integer hook; `BlockEnumMulti` invokes native translation-key getters. The exact shared-base delta and recursive descendant inventory are recorded by `audit-appliance-shared-bases.py`. Other descendants retain their existing formulas and remain outside this native acceptance.

Native metadata retains all 16 states. Toaster variant/yaw layout remains variant*4+yaw, including the original modulo-12 material/drop behavior. Computer and cabinet retain low two variant bits, high two orientation bits, original ad-hoc yaw mapping, structure transforms, and float-precision rotated selection/collision bounds. Non-full block faces remain non-supporting. Cabinet callbacks retain reflective tile creation, tile event delegation, lock/key dispatch, original random item splitting with copied NBT, per-slot comparator notifications and conditional superclass cleanup.

The two complete legacy renderer declarations are replaced in place with native geometry selectors. Original OBJ whole-stream ordering, duplicated third corner, shading/UV behavior, toaster inventory -90-degree yaw and -0.25 Y offset, computer 1.2 inventory scale and +0.1 Y offset, and world orientation are preserved via the already-native AtlasObjGeometry helper. Inventory is recentered by +0.5 per axis for native RenderItem. World geometry remains unculled, AO/diffuse-disabled with own-block packed light. Atlas textures remain the original iron/steel/wood toaster and computer materials. A native model event registers actual ItemBlock identities only after registration; the loader rereads resource-pack OBJ data on reload.

## Verification

- `porting/tests/native-appliance-proof.log`: 22 complete production classes compile against actual Forge 1.12.2. 67,688 passing assertions. The harness verifies 16-state owner round trips and variant drops, native collision bounds, 582 placement combinations, cabinet tile event dispatch, inventory depletion/NBT preservation/comparator cleanup, inherited/overridden shared drop dispatch, ordered vertices for both meshes across 16 states and inventory/world paths, and resource-loader material/bake routes.
- Four explicit external NTM collaborators isolate `MainRegistry`, `ModItems`, `ItemLock`, and `TileEntityLockableBase`. The actual complete block owners and both shared bases compile; no Minecraft/Forge facade or source-body extraction is used. A native World subclass records state mutations, tile access, comparator/drop output. This is callback testing, not a running server/Chunk persistence test. GUI/network and real lock authorization are not exercised.
- `porting/tests/appliance-shared-base-audit.log`: exact shared deltas, descendant audit, and byte-identical original assets.
- `porting/tests/appliance-atlas-regression.log`: existing shared atlas geometry regression passes 385,021 assertions including real Forge/vanilla flat light, native item model routes, directory/JAR resources, resource changes/reloads/missing resources. That existing suite uses bounded external owner collaborators; it validates the reused helper, not appliance registration itself.
- The appliance loader is directly tested using real resources and native IModel bake. Automatic event registration and full ItemModelMesher integration for these new owners are not claimed runtime-tested. No full-mod compilation success, graphical Minecraft session, or screenshot parity is claimed.

## Parent integration

Remove the old ClientProxy block-handler registrations for RenderToaster and RenderBlockDecoModel. Use native Material.IRON and remove obsolete setBlockTextureName calls for deco_toaster, deco_computer and filing_cabinet initializers. Do not remove RenderCRT registration.

## Deferred assessed roots

CRT is intentionally unchanged: its Screen forces packed light 240 (block light 15, sky light 0). Native Forge baked light takes the maximum with environmental light; vanilla flat rendering replaces the light field. A baked-only emissive rewrite would not preserve this. A native separate screen render path with exact light and appropriate lifecycle is required, not an altered-light shortcut.

RTTY involves ten block owner classes and eight tile classes (1,774 lines), tile-state material selection and redstone/network/GUI consumers. RBMK control/reflector/rod requires the shared BlockDummyable/RBMKBase multiblock and neutron/tile lifecycle plus lids/pipes. Neither is silently partially declared migrated. RTG is separately owned by the parent/energy worker.
