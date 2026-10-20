# Native normal-cube queries from CP92

Baseline: `0675e599e5ee31a60776b560af792372df191790`. Production is exactly 68
edit ranges in 30 files: 47 zero-argument queries plus one native capability
signature. No platform shim, new production helper, default-state substitution,
world reread, source exclusion, or whole-project build was introduced here.

## Finite source rule

- 19 inline `WorldBlockState.read(...).getBlock().isNormalCube()` calls use
  `WorldBlockState.read(...).isNormalCube()` at the same expression site.
- Six calls reuse the CP92 air state that originally supplied their Block.
- 22 calls use 20 newly captured states. The original world read and `getBlock`
  stay at their exact evaluation site; the original Block variable and all
  intervening code stay intact. Queries call `block.isNormalCube(capturedState)`.
- PowerDetector's exact constant-true capability body gains the genuine native
  `canProvidePower(IBlockState)` signature. Original and current owner closure
  contains no PowerDetector descendant. No weak-power-value behavior is changed.

Mutation snapshots matter: ExplosionNT queries its original receiver after the
explosion callback, BlockGasRadonTomb after possible terrain replacement, and
EntityFalloutRain after configuration callbacks/spawns. They retain the original
captured owner/state, never reread the mutated world. OreCave shares each local
capture across its original branches. Every coordinate expression and short
circuit remains in place. The sole assignment-form capture is ExplosionNT;
the other 19 are local declaration captures.

Excluded: BlockMutatorBulkie's Block-parameter query, Autosaw's four-argument
contextual overload, and all placement/hardness families. Five already-native
queries remain unchanged. The seven inventory exclusions include those five
native sites. OilWell's intentionally mismatched replaceability calls remain
untouched. No world-aware normal-cube overload is substituted.

## Dispatch and source evidence

`engine/` inventories the original NTM, original engine/Forge and actual native
classpath implementations. Original NTM has no normal-cube override. Native
state dispatch is live and virtual, not cached; the actual extended-state
implementation inherits the same final owner getter and query dispatch. AE2's
real final one-argument predicate is retained and differs from its contextual
predicate. The full-cube/material/power secondary owner closure is accounted for;
PowerDetector was its only still-legacy power declaration. All current secondary
callback bodies and block inheritance rows are pinned independently in
`current-callback-closure.json`; the genuine MaterialGas source is pinned too.

Thirty complete byte-exact inverses/round trips pass, with 90 rejected drift
mutants. Every reconstructed file was checked against the sealed CP92 Git object.
All 47 old receivers attribute as actual Block; the resulting 19 state calls and
28 Block/state calls resolve their genuine native signatures. PowerDetector's
method is a genuine native override. Partial javac diagnostics are 636 before,
589 after. This attribution is not successful compilation of the containing
caller classes or PowerDetector class.

## Bounded actual runtime proof

The scoped compile includes the full unchanged production BlockNTMGlass,
BlockBeamBase, WorldBlockState and ForgeDirection. Runtime passes 11,137 assertions,
including 5,485 actual native registry states, actual AEBaseBlock final methods,
actual Forge extended-state dispatch and the actual guarded world-state helper.
A minimal concrete subclass supplies only Beam's otherwise-unused abstract tile
factory; no substitute NTM registries or production dependencies are used.

The original signed engine's real `aji.r()` predicate runs a material/full/power
fixture matrix. The exact original PowerDetector method bytecode and exact
current constructor/capability source are separately projected and compared.
These are explicitly isolated capability projections, not execution of the full
PowerDetector. Stateful test subclasses check live dispatch, captured nondefault
metadata, mutation snapshots, short circuits, null/throw ordering and original
helper bounds. They are test fixtures, not mod owner implementations.

No startup, whole gameplay, arbitrary third-party state implementation, universal
vanilla-version equivalence, rendered pixels or full-machine proof is claimed.

## Reproduce

From the repository root, using the retained official artifacts:

```sh
python3 ntm-port-1.12.2/porting/tests/native-normal-cube-queries/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt \
  --original-jar /workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar \
  --legacy-client /tmp/nagaport-legacy-client.jar \
  --forge1614 /tmp/nagaport-forge1614-userdev.jar --verify-attribution
```

All genuine dependency/archive bytes and API class bytes are pinned. The two
ForgeGradle dummy ZIPs are the sole container-hash exception: each must contain
exactly one `dummyThing` entry with its pinned payload and therefore no classes.
Ordinary offline builds regenerate their ZIP timestamps. No dependency is modified
by these tests. Engine audit verification compares freshly regenerated outputs
with retained evidence by default; explicit `--record` is only for producing a
new audited snapshot, not routine verification.
