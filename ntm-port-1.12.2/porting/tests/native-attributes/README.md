# Native shared-attribute constants: bounded CP56 slice

Baseline: `149469c7403653ca6576515b730bc033a1574cfe`. Production patch contains exactly 16 identifier replacements in 12 Java files. No API bridges, new attributes, registration changes, formula changes, fake Minecraft classes, source exclusions or balance changes.

## Inclusion and identity

Only explicit class-qualified uses whose qualifier javac resolves to the native `net.minecraft.entity.SharedMonsterAttributes` TypeElement are migrated. The old members cannot themselves resolve against native classes; we do not count those error symbols as evidence. The owner, original SRG identity, native MCP mapping and actual runtime attribute objects are independently checked. The manifest identifies exact baseline offsets; rerunning the verifier attributes all offsets afresh and reconstructs the complete changed files byte-for-byte (text encoding is UTF-8).

- `followRange` -> `FOLLOW_RANGE`: original `yj/b`, SRG `field_111265_b`, runtime name `generic.followRange`; default 32, clamp [0, 2048], watched false.
- `knockbackResistance` -> `KNOCKBACK_RESISTANCE`: original `yj/c`, SRG `field_111266_c`, runtime name `generic.knockbackResistance`; default 0, clamp [0, 1], watched false.

These are class-owned native fields, not an enum-name guess. The names/defaults/watch flags and clamp results come from actual original/native objects. Unrelated same-named members, shadow types, variable-qualified statics, unresolved qualifiers, comments, strings and imports are excluded; synthetic negative controls exercise each category. Imports are not edits.

## Important exclusions, not balance fixes

The authentic Forge 10.13.4.1614 userdev's nested forgepatches.zip and fmlpatches.zip contain no attribute-class patches. Inspection of both sets found no cap adjustment; original and current mod Java source contained no `RangedAttribute`, `maximumValue`, `field_111118_b` or `SharedMonsterAttributes.class` cap modification. This is evidence of those sources, not a guarantee about an arbitrary installed coremod or external server configuration.

Real original/native objects have matching names/defaults/watch flags but different clamps for:

- maxHealth: original [0, Double.MAX_VALUE], native [Float.MIN_VALUE, 1024]. BOTPrime's preserved 15000 health is genuinely affected. Fish 15, WarBehemoth 200 and MaskMan 1000 are within both upper bounds, but that alone does not establish all modifier/configuration outcomes for those entities.
- movementSpeed: original [0, Double.MAX_VALUE], native [0, 1024]. Ordinary mod values 0.15, 0.25 and 0.3 fit both, but global clamp equivalence is false.
- attackDamage: original [0, Double.MAX_VALUE], native [0, 2048]. Ordinary mod values 1, 5 and 15 fit both, but global clamp equivalence is false.

All three old constants remain untouched in this slice, including in files that also contain included fields. A later bounded migration must explicitly resolve cap preservation rather than silently truncate bosses or assume configured/modifier values stay in range. TileEntityForceField's unrelated maxHealth remains untouched. Existing `getAttributeUnlocalizedName()` calls in armor and broader callback incompatibilities are not fixed or claimed compiled here.

## Offline reproduction

Run from any directory with the baseline Git object available:

```
python3 porting/tests/native-attributes/run.py \
  /workspace/shared/nagaport-toolchain/java8 \
  /tmp/nagaport-ae2-native-classpath.txt \
  /tmp/nagaport-legacy-client.jar /tmp/nagaport1710-srg.zip \
  /tmp/nagaport-forge1614-userdev.jar \
  /workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/de/oceanlabs/mcp/mcp_stable/39/fields.csv
```

The runner creates a temporary original-client copy with META-INF omitted to avoid signed-default-package conflicts. All original class bytes stay unchanged, the source JAR remains unchanged, and the copy is deleted afterward. No binaries are included in the patches.

Evidence: 996 real old/native attribute assertions, including lookup by singleton and name; stored base versus clamped computed value; infinities, NaN and signed-zero clamp cases; modifier operations 0/1/2 with UUID lookup/removal and base restoration. Assertions retain peculiar existing values such as knockback base 100 rather than normalizing them.

The two complete production method bodies `EntityDamageUtil.knockBack` and `setBeenAttacked` are extracted directly, compiled unchanged and executed against native EntityArmorStand and AttributeMap. Constructor bypass initializes only fields used in these methods (random and attribute map); resistance zero/one and velocity/motion effects are checked. This is not entity-construction, world, multiplayer, mob AI or gameplay evidence. Reversing the new constant makes both methods fail native javac; independent positive/reverse compiler controls cover both renamed fields. The test deliberately does not compile the full EntityDamageUtil class or any full mob/armor consumer.

No full Gradle run, game launch, client/server test, runnable mod claim, production diagnostic reduction claim or push. `final.log` captures the successful complete run; earlier local harness setup errors were corrected before this run.
