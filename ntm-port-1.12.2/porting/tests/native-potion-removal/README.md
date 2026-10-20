# Native potion removal-only continuation

Base CP79 `77b595c32b46e2f9309bcf9924d654ddc29a3c95`. Six exact argument edits in ItemPill and ItemSyringe complete their existing native cure sequences. No applications, durations, amplifiers, branching, receiver expressions, invocation order, custom potion calls, registry mutations, or ID/array consumers change. The native singleton argument has no nullable lookup. A null player still fails at the same unchanged invocation; no guard or swallowed failure is introduced.

Only explicit native Potion class receivers, verified by genuine javac attribution, are selected. The containing invocation must be removePotionEffect on an attributed EntityLivingBase subtype. Entire-source attribution accepts six expressions with 12,686 unfinished-port diagnostics: this is attribution evidence, not successful compilation or a diagnostic reduction measurement. Custom/foreign/unresolved/shadow receivers, object expressions, effect constructors and other numeric consumers are rejected. UTF16 offsets and whole-file hashes prove exact source preservation and complete inverse.

## Actual original/native evidence

300 assertions execute the actual original sv.m(int) and native EntityLivingBase.removePotionEffect(Potion) methods with real entity/map/effect construction. Cases cover present and absent effects, unrelated resistance retained, callback identity/count, unchanged duration/amplifier, and repeated removal. Three identities are compared using original singleton IDs/translation names and native registry keys/IDs. There is no reflection. Original JAR SHA256 is pinned; its .class entries are extracted byte-identically to a temporary directory because a default-package test subclass cannot share a signed default package through the JAR classloader. No original class bytecode is modified.

Test subclasses override lifecycle callbacks as observation boundaries, avoiding a fabricated World. Therefore world callbacks, attribute cleanup side effects, actual item execution, networking and full gameplay are not certified. Original/native removal bytecode both remove the map entry, invoke the finish callback only when an entry existed, and pass the removed effect. This test certifies that boundary, not equal effect formulas. A future NTM-owned legacy effect implementation must also revisit these cure sequences if application identity changes; this patch removes the native vanilla effects exactly as the original calls targeted vanilla identities.

Cached MCP 1.7.10 joined.srg establishes:

- rv/f -> Potion/field_76419_f (digSlowdown); ID 4, potion.digSlowDown -> minecraft:mining_fatigue.
- rv/s -> Potion/field_76438_s (hunger); ID 17, potion.hunger -> minecraft:hunger.
- rv/t -> Potion/field_76437_t (weakness); ID 18, potion.weakness -> minecraft:weakness.
- sv/m (I)V -> EntityLivingBase/func_82170_o (I)V (removePotionEffect).

10 attribution fixtures cover native ownership, inherited and variable shadows, foreign receivers, imports and excluded application/ID/object contexts. Full original-source re-attribution and changed-source idempotence pass.

## Preserved gates

All remaining damageBoost, jump, digSpeed, digSlowdown, hunger and weakness application references stay unchanged. Existing CP74 findings remain: strength operation/scaling differs; weakness scaling differs; hunger exhaustion differs; haste/fatigue add attack-speed modifiers; jump color differs. Six four-argument vanilla PotionEffect constructors, custom HbmPotion, potionTypes and numeric food arguments remain separate boundaries. No balance difference is hidden to reduce compiler counts.

## Reproduction

Run from this repository:

    python3 ntm-port-1.12.2/porting/tests/native-potion-removal/run.py /workspace/shared/nagaport-toolchain/java8 /tmp/nagaport-ae2-native-classpath.txt /tmp/nagaport-legacy-client.jar

No downloads, pushes, reflection, mock registry or production shim. final.log retains the successful run. Full-mod compile remains the coordinator's independent gate.
