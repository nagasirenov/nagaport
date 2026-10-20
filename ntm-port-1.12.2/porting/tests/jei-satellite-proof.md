# Satellite native JEI proof

Baseline: 5f720e0f82340aa29f2613ff71fc706689d7c3aa.
SatelliteHandler SHA-256 after owner navigation API correction:
1ca677e1fe33b8ac7cce3fc75ef0b9d713f8adf54220fda7546fb4e217c928b7.

`python3 porting/tests/jei-satellite-native-test.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/obj-probe-classpath.txt)"`

Result: 407 assertions pass. Complete real SatelliteHandler and three shared core classes compile. Minecraft, Forge, JEI and NEI are actual dependency binaries; WeightedRandomChestContent and LegacyWeightedLoot are whole production classes. Temporary NTM-only item/block registries, pool lookup and cargo lookup are explicit fixtures. The tooltip collaborator embeds the verbatim current production addTooltipToStack method, not the whole ItemStackUtil class. GUI proxies record native API calls without rendering/OpenGL. This is not whole-mod compilation or a gameplay test.

Covered: all/input/output queries, dynamic/null cargo mapping, explicit input pools, item-only usage matching, duplicate outputs and duplicate matches, source identity, NBT copies and weighted lore/count, overflow sizes 0/1/17/18/19/35/36/37/55, output coordinates, decorative dock excluded from logical ingredients, original integer-weight sum overflow arithmetic, no phantom navigation region and native EMPTY immutability.

`python3 porting/tests/satellite-native-source-parity.py` separately passes 540 source/slot mathematics assertions. It is not runtime evidence.

## Legacy null-padding investigation

Upstream source inspected at:
https://github.com/GTNewHorizons/NotEnoughItems/blob/7163630cb16b005957a435b71c79f61e5871ad80/src/main/java/codechicken/nei/PositionedStack.java

Downloaded source SHA-256: aca1f50bcb36cac8bf3509c28bd8f073f6b37a2572f43c9e41cc161da1584a7d.
Lines 67, 75 and 95 show default constructor enabling permutation generation, null entries being skipped, and the resulting item array being compacted. Under that legacy implementation Satellite's old padded arrays do not produce blank page cells; nonempty alternatives persist, matching native JEI lists.

Limitation: pristine build.gradle pins Chicken-Bones NEI 1.7.10-1.0.3.74, not this modern GTNH source. A subsequent historical-source fetch was cancelled before network approval and was not executed. Exact pinned historical artifact behavior is not established by this evidence. No new coherent blank-page behavior was invented and shared core was not edited by the Satellite worker.
