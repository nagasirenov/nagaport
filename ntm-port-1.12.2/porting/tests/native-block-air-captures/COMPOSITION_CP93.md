# CP93 composition without changing the CP92 air proof

Run `python3 porting/tests/native-block-air-captures/check_cp93_composition.py`
from the module directory. An optional `--module PATH` selects the source tree.

The wrapper pins the original air checker/manifest and the consolidated CP93
strict inverse checker/manifest. Only their six-file intersection is reconstructed
in a temporary source tree: BlockVolcano, EntityMissileCustom, EntityMissileTier0,
EntityCreeperTainted and HbmPotion from normal-cube queries, plus TTree from the
World air-query family. The remaining eight air-proof files pass through unchanged.
The original CP92 checker then validates all 14 files against its unmodified
historical manifest. Eighteen CP93 mutation controls and all 42 original air
mutation controls pass. No production files or historical air artifacts are edited.
