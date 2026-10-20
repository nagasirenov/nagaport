# Three already-native knockback name getters

This is an independent suffix to the frozen CP79 native-attribute-keys patch. It changes exactly three method identifiers, not fields, modifiers, values or callbacks:

* ArmorLiquidator:57
* ArmorDiesel:42
* ItemModIron:37

Each receiver is directly `SharedMonsterAttributes.KNOCKBACK_RESISTANCE`; javac verifies the actual native class qualifier and public static final IAttribute field. The only change is the zero-argument legacy getAttributeUnlocalizedName → native IAttribute.getName. No dynamic/custom receiver or overridden foreign getter is included. After this suffix, no legacy getAttributeUnlocalizedName call remains in current production sources.

The original `yj/c` mapping to field_111266_c and actual key `generic.knockbackResistance` were established by the existing native-attributes proof. The tiny suffix probe independently compares the actual original/native returned name and hash and verifies real Guava map lookup by the original string. Native AttributeModifier UUID/operation/amount remain exact, including ArmorLiquidator's existing 100D amount. No modifier is applied, no clamp is changed, and no stat/gameplay equivalence is claimed.

The scanner attributes the three complete production source files against genuine native dependencies. Unrelated unresolved project owners/methods are expected and explicitly reported: this is an owner/getter binding proof, not full class compilation. Source receipts preserve every other byte. The suffix guard proves three complete-file inverses and six token/whole-file drift negatives. Its runner reconstructs exact before/after files and requires all three matching compiler rows, executes five real-engine key/map assertions, then reverses this suffix before validating every primary 19-file source inverse. No old baseline hashes are regenerated.

Apply attribute-keys-production.patch, then attribute-key-suffix-production.patch. For integrated historical source guards, reverse this suffix before the primary native-attribute-keys layer; ArmorLiquidator overlaps. The other two files were not part of the primary patch. Both guards expose reverse_source(relative, text) and reverse(raw, row).

Offline reproduction:

    python3 run.py JAVA8 NATIVE_CLASSPATH_FILE ORIGINAL_CLIENT_JAR

No download, production helper/reflection, attribute registration, cap design, full mod/game/client launch, or broader parity claim. Original signed JAR metadata is omitted only in a temporary test container; every original class byte remains unchanged.
