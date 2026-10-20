# Recovered frozen cable/CT palette placements

CURRENT STATUS: source reconstruction and the reconstructed finite44 native probe are independently verified in the replacement workspace. The fresh2026-10-20 native run exits0 with13 attribution controls,44 native bindings,496 owner/storage checks,759 total checks,522 ordered events and six rejected helper mutants. See reconstructed-native-current.log and NATIVE-RECONSTRUCTION.md. This is not a full mod build or client acceptance.

## Exact recovered production scope

The restored CP76 source tree is `b51f8971c483858304306df2f0cbbafade512a77`, held by local recovery commit `d25dcb9f0908d76b1e9eca96b75e58f6397de355`. The pre-reset historical baseline was `0aeda80b`.

Only 44 six-argument setBlock statements are recovered: 27 red_cable, 10 red_wire_coated and seven direct deco_steel calls, all metadata0/flags3. Antenna has7, Ruin001 has3, Ruin002 has14, Spaceship has20. The replacement invokes the already-existing DungeonToolbox.placeUnorientedStructureBlock, leaving its complete bytes unchanged. All four before-file and all four after-file SHA256 hashes exactly match the values retained in conversation from the original frozen44 batch. manifest.json records them.

No native owner, registry name, resource, helper, build configuration or AT was changed. No steel-poles placements are included. The later20-site steel-poles work was lost and is not silently reconstructed here.

The unchanged helper captures World/coordinates/current mutable block before entering its body, checks null receiver before original bounds, resolves the actual native metadata0 state only after bounds, dispatches flags3 and ignores the return. The recovered calls preserve argument interiors and their order. Neither a default-state shortcut nor a generic legacy World API is introduced.

## Current, reproducible evidence

- recover.py verifies all eight retained hashes, exact44 selected expressions, source reconstruction and complete inverse before accepting any output.
- source-proof.py independently checks four whole-source inverses and44 per-site mutation controls.
- check-closure.py scans4065 tracked Java sources: one exact constructor assignment per selected field, exact static declarations, no subclasses of the three owners and six rejected source-closure mutations. It checks all44 statements against the independent pristine original in unchanged order, compares the construction/registry identities and checks that the native owner/context/helper sources equal the restored CP76 baseline.
- verify-integration.py is a read-only exact-hash check for an already-integrated main tree. merge.py is for applying the recovered expressions to still-unmodified selected lines; its refusal on an already-applied tree is expected.
- recovery-*.log files were produced in this replacement workspace. They contain source-only results and do not imply native compilation or gameplay.

Original constructor comparison accounts only for the already-ported Material.IRON/name method spellings and the removal of the old texture setter. It does not claim texture/model equivalence. Existing CT destruction/lighting, energy-node lifecycle and full-client gates remain open.

## Historical results and fresh independent revalidation

Before workspace replacement, the frozen44 native probe was reported as759 checks and522 ordered native/original events, including16-state owner storage, bounded cable-tile lifecycle and six semantic mutants. The reported Java8 attribution controls numbered13. Those original logs and native classpath were lost, and no recreated historical log is supplied. The new reconstructed-native-current.log independently reproduces759 checks and522 events on the restored real native classpath; it is a fresh result, not the lost log.

The original native fixture used complete actual BlockCable, WireCoated, BlockDecoCT and DungeonToolbox, with bounded NTM registry/tab/tile/Library collaborators. It did not execute production energy-node lifecycle, whole generators or a graphical client. This limitation still applies to the historical result.

PaletteOwner.java and ownership.py were restored, then genuinely executed as part of the fresh native run. All13 attribution controls and all44 selected production-expression bindings pass. The required native classpath and pinned original client jar were restored by the coordinator before launch. This worker performed no downloads, installs or production changes.

## Reproduce current source-only checks

From the module directory:

    E=porting/tests/cable-palette-native
    python3 "$E/recover.py"
    python3 "$E/source-proof.py"
    python3 "$E/check-closure.py" --original-root /workspace/shared/nagaport/ntm-original-pristine
    python3 "$E/verify-integration.py" --module /workspace/shared/nagaport/ntm-port-1.12.2

Do not pass recover.py --apply against later independent edits. For a later tree with unchanged selected lines, use merge.py --module MODULE first, then --apply --receipt RECEIPT.json; it preserves all independent bytes by exact inverse.
