# Shared-attribute string keys, not attribute values (CP79)

Base `77b595c32b46e2f9309bcf9924d654ddc29a3c95`. Exactly 29 directly class-qualified attribute-name expressions in 19 files change, using 58 identifier-token edits:

* 17 movementSpeed/getAttributeUnlocalizedName pairs → MOVEMENT_SPEED/getName.
* 10 attackDamage/getAttributeUnlocalizedName pairs → ATTACK_DAMAGE/getName.
* 2 maxHealth/getAttributeUnlocalizedName pairs → MAX_HEALTH/getName.

This intentionally **does not migrate the 87 registration/value consumers**. The prior native-attributes evidence demonstrated real original/native clamp differences, so a broad rename is not truthful gameplay parity. Original ranges reach Double.MAX_VALUE, while native max-health/movement cap at 1024 and attack damage caps at 2048; native max-health also has a Float.MIN_VALUE floor. Existing BOTPrime 15000, UFO 20000 and BFAngel 10000 health setters are untouched. Configured statistics, modifier application, attribute registration and cap-preservation design remain separate unresolved work. No global attribute mutation, reflection, new attribute or compatibility shim is added.

## Why this boundary is safe

The selected operations read only a name used as a modifier-map key or containsKey argument. Actual original and native attribute singletons return exactly `generic.maxHealth`, `generic.movementSpeed` and `generic.attackDamage`, with identical hashes. Their differing numeric clamps are never called by these expressions. All modifiers, UUIDs, arithmetic, operation codes, map calls, receiver expressions and unrelated bytes remain unchanged.

The original joined.srg identifies yj/a → field_111267_a, yj/d → field_111263_d, yj/e → field_111264_e, and th/a → func_111108_a. Actual native MCP mappings and fields identify MAX_HEALTH, MOVEMENT_SPEED, ATTACK_DAMAGE and IAttribute.getName. Both original BaseAttribute.a and native BaseAttribute.getName have precisely `aload_0; getfield [String name]; areturn` bytecode. Actual runtime objects supply the name/default/watch/range evidence. The range mismatch is asserted as a known exclusion, not hidden by narrow sample values.

## Source attribution and tests

AttributeKeyScanner attributes all production sources using genuine native dependencies. It requires the qualifier to be the exact native SharedMonsterAttributes class TypeElement, a zero-argument immediate name getter, and genuine public static final IAttribute destination fields. Post-attribution verifies every selected field owner and IAttribute.getName binding. Variable-qualified statics, shadow types, unresolved/foreign qualifiers, static imports, arity changes, parenthesized unsupported forms and every registration/value consumer are excluded. Fourteen compiler controls cover these boundaries.

The isolated native diagnostic total moves 12686 → 12657. This is unfinished-source attribution, not a successful full-mod build. The source guard proves 19 exact complete-file byte inverses and rejects 77 whole-file/token mutations. CRLF is retained. Historical manifests are not rewritten; reverse_source(relative, text) supplies the exact inverse layer for composed integration audits.

The actual original/native test passes 345 checks: all 29 extracted production name expressions, exact key/hash/retained-string identity, metadata and documented range differences, plus complete extracted ItemSwordAbility.getItemAttributeModifiers and ItemModV1.getModifiers method bodies. The latter run with real Guava multimaps and native AttributeModifier objects. The Sword UUID field and V1 superclass map callback are explicitly test seams, not compiled complete mod items or production aliases. Inputs include extreme, negative, signed-zero, infinite and NaN amounts; the exact modifier amounts, UUIDs, operation codes and float promotion survive unchanged. Tests do not apply modifiers to attributes or assert statistical/gameplay equality.

No platform class is replaced. A temporary legacy test JAR removes signature metadata only so default-package fixtures may coexist with original obfuscated classes; every original class byte remains unchanged. No downloads, production reflection or full world/client launch.

Offline reproduction:

    python3 check_source.py
    python3 controls.py JAVA8 NATIVE_CLASSPATH_FILE
    python3 run.py --java-home JAVA8 --classpath-file NATIVE_CLASSPATH_FILE --legacy-client ORIGINAL_CLIENT_JAR --legacy-srg ORIGINAL_SRG_ZIP --verify-attribution

The untouched 87 references are recorded in excluded-value-consumers.json. original-source-review.json preserves pristine-source provenance for the selected consumers. The separate already-native-knockback getter suffix, if integrated, must be inverted before running this frozen primary source guard.
