# Bounded native potion constant and argument migration

Exact base CP73 `a4739afa41966b75e2012bccd7df919e51d5c3a0`. 217 expressions in 57 production files. No new production class, legacy holder, registry edit, numeric-ID shim, custom potion rewrite, duration/amplifier formula change, or effect/balance adjustment.

The javac scanner accepts only explicit `Potion` receivers attributed to native `net.minecraft.potion.Potion`, with conservative import and whole-unit shadow rejection. It replaces class-qualified constants with fully qualified `MobEffects` singletons. Legacy `.id`/`.getId()` is removed only inside the first argument of native two/three-argument PotionEffect constructors, or one-argument active/query/removal methods on an attributed EntityLivingBase subtype. Numeric-ID consumers elsewhere remain untouched. Constructor conditional alternatives are preserved; both selected production ternaries contain only WITHER/POISON alternatives. Static imports, comments, strings, custom HbmPotion, unrelated/shadow receivers and unresolved receivers are not selected. Exact UTF16 offsets and full-file hashes preserve original UTF8/CRLF bytes and complete reversibility. Existing indentation warnings are retained original whitespace, not formatting cleanup.

## Identity and runtime evidence

15 mappings have explicit original obfuscated/SRG identities, numeric IDs and translation names in identity.json, and native registry names in map.tsv. Original client bytecode and cached genuine native bytecode are retained as text, not binaries. Runtime loads actual original `rv`/`rw` via a separate URLClassLoader and executes actual native Bootstrap/Potion/PotionEffect. No stub registry or Potion class is used. The original JAR is hash pinned.

8,827 assertions compare actual old/native singleton IDs, registry identity, old translation identity, bad-effect/instant/color metadata, attribute UUID/operation/amount and amplified amount, tick readiness, constructor ID/duration/amplifier/ambient values, and actual native NBT roundtrips. Explicit negative guards prove original/native strength and weakness amounts differ.

49 additional assertions execute actual native PotionEffect.onUpdate, duration decrement, poison health floor, wither damage source, regeneration cap and effect merging. EntityArmorStand construction/data/attributes remain real native code; damage and heal are overridden observation boundaries, not world/combat simulation. These tests do not assert all engine behavior is identical between versions. SPEED/SLOWNESS modifier metadata is identical, but native shared-attribute clamp differences remain an independent open gate; this slice does not fix or hide that gate. Potion resistance interaction with all changed combat rules, complete production owner execution, multiplayer, rendering, client/server gameplay and full mod compilation are not certified.

The original complete-source javac attribution reported 13,595 unfinished-port diagnostics. That is owner evidence, not successful compilation or a diagnostic-reduction claim. All source replacements are reversibly checked; 17 attribution fixtures cover ownership, imports, inherited/variable shadows, custom/unknown receivers, excluded fields, constructor arity, ID contexts and UTF16/escaped identifiers. Changed-source scanning proves idempotence. Optional full re-attribution reconstructs every original source and verifies the exact selection.

## Explicit continuation gates

- damageBoost / STRENGTH: original amplified amount 1.3*(level+1), operation 2; native 3*(level+1), operation 0.
- weakness / WEAKNESS: original -0.5*(level+1); native -4*(level+1).
- hunger / HUNGER: original performEffect exhaustion 0.025*(level+1); native 0.005*(level+1).
- digSpeed / HASTE and digSlowdown / MINING_FATIGUE: native adds attack-speed attribute modifiers absent from original.
- jump / JUMP_BOOST: actual original/native liquid colors differ. No appearance change silently accepted.
- Six old four-argument vanilla constructors remain unchanged. Native requires an explicit fifth particle flag; that boundary is not guessed here.
- Custom HbmPotion constructors/registration, potionTypes array, unrelated numeric-ID consumers and legacy food setPotionEffect signatures remain separate work.

## Offline reproduction

From this retained worktree or integrated repository with the exact Git base available:

    python3 porting/tests/native-potion-constants/run.py JAVA8 CLASSPATH_FILE ORIGINAL_CLIENT_JAR --verify-attribution

Local verified inputs: Java8 `/workspace/shared/nagaport-toolchain/java8`, cached classpath `/tmp/nagaport-ae2-native-classpath.txt`, original client `/tmp/nagaport-legacy-client.jar`. No dependency downloads or remote changes. run.py uses temporary directories and does not modify production. Full source attribution needs about 3 GiB Java heap; source copy is temporary. final.log records the completed test run. The full mod compile remains the parent checkpoint's independent responsibility.
