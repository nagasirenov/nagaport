# Native living-event damage payload accessors

Baseline: recovered, sealed checkpoint `d5ef20a8f1cb65fbd06faa86a4901870c8bab3f2`.
Production scope is exactly 52 compiler-attributed accesses in 14 existing source
files, all on a local parameter with the genuine native event type:

| Owner | Amount reads | Simple stores | `*=` | `-=` | Source reads |
|---|---:|---:|---:|---:|---:|
| `LivingHurtEvent` | 7 | 10 | 7 | 3 | 15 |
| `LivingAttackEvent` | 4 | 0 | 0 | 0 | 6 |

The immutable attack event never receives a setter. No other event family, wrapper,
legacy shim, listener registration, priority, cancellation call, branch, damage
formula, item/armor ordering or production dependency is changed.

## Mapping and scope

Authentic Forge 1.7.10-1614 source and embedded binaries establish direct public
payload fields: `ammount` is mutable only on Hurt, and `source` is final on both.
Native Forge 1.12.2 getters are exact `ALOAD 0; GETFIELD; FRETURN/ARETURN`;
Hurt's setter is exact `ALOAD 0; FLOAD 1; PUTFIELD; RETURN`. They neither clamp nor
normalize damage, change source identity, invoke callbacks, or alter cancellation.
`native-*.javap.txt` and `original-*.javap.txt` retain authentic disassembly;
`dependency-sha256.json` records the actual resolved inputs.

Reads become genuine getters. A statement `event.ammount = rhs` becomes
`event.setAmount(rhs)`. A compound store becomes, for example,
`event.setAmount((float) (event.getAmount() * (rhs)))`. The explicit float cast
preserves the compound assignment's narrowing conversion, including a double RHS.
Parentheses preserve RHS precedence. The local parameter can be loaded twice
without another receiver effect; both loads occur before the RHS. The outer target
is captured even if the RHS reassigns the parameter. A compound null target throws
at the getter before evaluating the RHS, whereas a simple store evaluates the RHS
before its null-target failure, just as the original field statement did.
Assignment expressions whose result is used and effectful/non-parameter receivers
are not transformed. Unsupported operators, source writes, attack writes,
unqualified references, subtype/shadow receivers and reachable source accessor
overrides are excluded. The full mapped pre-inventory separately confirms that
all 52 current consumer accesses meet the local-parameter/exact-owner restriction.
There are no source descendants of either event class. All 67 resolved binary
JARs were inspected with ASM using first-classpath-definition precedence; there
are no overriding accessors in either event hierarchy.

This is native API adoption within that resolved source/dependency closure, not
behavioral equality for arbitrary external mod subclasses. A genuine runtime
subclass explicitly demonstrates changed read/store behavior when it overrides
the accessors. No equivalence is claimed for that negative witness.

## Reproduce

From the module root:

    python3 porting/tests/native-living-payloads/run.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
      --forge1614 /tmp/nagaport-forge1614-userdev.jar --verify-attribution

No downloads, Gradle invocation, full-mod compile or new production dependency is
used. The tests run genuine native event objects, not stand-in Minecraft/Forge
classes. Test-only reflection seeds an existing cancellation flag because raw
headless events lack LaunchWrapper's cancellation transformation. That verifies
payload accessors leave the flag alone, not a cancellable event-bus lifecycle.

The finite proof includes:

- Exact source reversals for all 52 accesses, preserving every unrelated byte.
- Native payload field contracts, exact accessor bytecode, source/null identity,
  signed zero, subnormals, infinities and positive/negative NaN payload bits.
- 31 real compiler fixtures, including rejected writes/receivers/shadows/overrides.
- All 20 transformed production store expressions, extracted by the manifest's
  compiler spans and compiled against real native events. Arithmetic is compared
  to the original Java compound operation. `calculateDamage` is an instrumented
  callback boundary that checks its original inputs, call count and returned value;
  this does not execute or certify the real damage calculation implementation.
- Compiler-transformed fixtures for RHS mutation/throws, receiver reassignment,
  double-to-float compound narrowing, and exact null/exception order.
- Full post-change native declaring-method attribution at all 62 generated method
  name positions, with exact owner, receiver and enclosing method matches.

The isolated complete source analysis reports 12,835 errors versus 12,887 at the
sealed baseline, a reduction of exactly 52. This is javac analysis of unfinished
source, not a successful full-module build. Full handlers, armor lifecycles, event
dispatch, Forge/client/server launches, and damage/gameplay simulation remain
outside this evidence.

## Integration

Prefer applying the contextual production patch and copying this evidence folder;
never replace whole production files over other edits. The original manifest
hashes are against the sealed baseline. First run `check_source.py`, then the
complete proof above. If unrelated changes touched one of the 14 files, reattribute
against the destination's current full source list rather than changing hashes to
hide the difference:

1. Compile `PayloadScanner.java` with Java 8 `tools.jar`.
2. Run `PayloadScanner <all-source-list> <native-classpath-file>` and capture TSV.
3. Run `python3 porting/tests/native-living-payloads/migrate.py <captured-tsv>`.
   It checks the approved per-owner counts and existing path/method/receiver/kind
   inventory, then touches only fresh compiler spans and records new inverse hashes.
4. Rerun `run.py --verify-attribution` on the integrated tree.

The migrator deliberately rejects an already-migrated tree rather than making
silent repeated changes. Keep the original manifest or commit as provenance when
rebasing onto unrelated overlapping source edits. Test fixtures and generated
classes stay outside production source roots; Python bytecode caches are not part
of this deliverable.
