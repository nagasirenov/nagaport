# Native DamageSource source-entity accessors

Baseline: sealed `d5ef20a8f1cb65fbd06faa86a4901870c8bab3f2` (CP77).
Finite production scope: 37 name tokens in 21 files: 30 `getEntity` calls,
5 `getSourceOfDamage` calls, and the two Sedna overrides. One previously native
`EntityMovingItem.getTrueSource` call is explicitly left unchanged. No receiver,
cast, evaluation order, condition, body, constructor, field, flags, damage amount,
event API, or unrelated byte is changed. Existing CRLF is preserved.

## Semantic evidence

The pinned original 1.7.10 client, Forge 1614 MCP CSV, original joined SRG,
1.12.2 stable-39 CSV/SRG, and actual Forge-mapped native class bytecode establish:

- `getEntity` / `func_76346_g` becomes `getTrueSource`.
- `getSourceOfDamage` / `func_76364_f` becomes `getImmediateSource`.
- The base true-source getter returns null. Its immediate-source getter invokes
  the virtual true-source getter; this forwarding is retained in both versions.
- Direct EntityDamageSource stores constructor argument 2 and returns it as true
  source; inherited immediate-source forwarding therefore returns the same entity.
- Indirect EntityDamageSourceIndirect stores constructor argument 2 as projectile
  and argument 3 as shooter. Immediate source returns projectile; true source
  returns shooter, including null with NO projectile fallback.
- Both complete production Sedna classes compile against actual native APIs. The
  two renamed overrides retain their exact original field-return bodies. Calls
  through DamageSource dynamically dispatch to them; public field mutations and
  nulls remain visible. Omitting the override migration would silently break that
  attribution even after caller fixes, so it is included in this indivisible scope.

Full-source javac attribution identifies exactly three source descendants:
ModDamageSource, DamageSourceSednaNoAttacker, DamageSourceSednaWithAttacker.
Only the last declares these accessors. All resolved dependency JARs and classpath
class directories are inspected for descendants and accessor declarations. The
reviewed binary closure contains the three native classes and expected accessors.
No arbitrary external mod hierarchy is certified.

The original pristine NTM source has exactly the same legacy-accessor inventory
in all 21 touched files; its two Sedna accessor bodies are compared literally.
Every pre-migration file hash independently matches its sealed baseline git blob.
The inverse checker reconstructs each entire pre-migration file exactly, retaining
unrelated migrations and original line endings. Post-attribution checks every
changed token's exact UTF-16 position, receiver type, enclosing method and actual
native/custom declaring class. This is not regex replacement of all getEntity
uses: Forge event, ticket and other unrelated getEntity methods are untouched.

## Reproduce

    python3 porting/tests/native-damage-source-accessors/run.py \
      --original /workspace/shared/nagaport/ntm-original-pristine \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
      --verify-attribution

No downloads, Gradle invocation, old-Forge source shims, production placeholders,
or scope exclusions. Tests use genuine platform classes. Test-only Unsafe merely
allocates identity-bearing entity objects without launching a world. Original
client code runs unmodified from its signed jar; its reflective probe is in a
non-default package to avoid signer conflicts. There is no signature stripping.

Proof includes 14 actual original-client runtime assertions, 50 native runtime
assertions (including both complete production Sedna classes), eight real javac
attribution fixtures, five exact getter bytecode checks and two constructor storage
checks. The caller migration is proved by exact source inverses and native method
attribution, NOT by claiming complete execution of the still-unported callers.
No full mod build, game launch, combat lifecycle or rendered gameplay is certified.

Isolated javac attribution errors: 12,887 before; 12,850 after. Exact reduction 37.
This is full-source compiler analysis, not successful full-module compilation.

## Fragment-safe integration with the event accessor worker

Do not copy entire overlapping production files. ModEventHandler, ItemModBathwater,
and ArmorTrenchmaster also contain independent event-field migrations.

1. Copy this test directory only to the destination (or run it from this worktree).
2. Compile DamageSourceScanner.java against Java 8 tools.jar. Run it against a
   freshly enumerated destination source list and the genuine native classpath:

       java -Xmx3g -cp <scanner-classes>:<java-home>/lib/tools.jar \
         DamageSourceScanner <source-list> <classpath-file> > <fresh-attribution.tsv>

3. With production edits paused during the scan and migration, run:

       python3 migrate.py --module <destination-module> \
         --attribution <fresh-attribution.tsv> \
         --reference source-manifest.json --output <integrated-manifest.json>

   This rejects a changed receiver/path/enclosing-method/accessor inventory and
   modifies ONLY current name-token spans. Event-field getter edits survive.
   The integrated manifest records its own exact input/output hashes and inverse.
   A second migration fails instead of silently accepting already changed input.
4. Run run.py with --module, --manifest <integrated-manifest.json> and
   --verify-attribution, then the parent's combined boundary/full-compile checks.

The isolated source-manifest.json remains sealed-baseline provenance; use the
integrated manifest for exact inverses on the combined tree. Do not relabel the
isolated diagnostic counts as combined-tree counts.

## Generated dummy-container pin correction after combined Gradle compile

Gradle regenerated compileDummy.jar and providedDummy.jar, changing their whole-ZIP
hashes. Their original hashes remain in dependency-sha256.json as provenance. Only
these two exact paths use an entry/content pin instead: exactly one dummyThing entry
containing byte FF, no duplicates or other entries. This content is independently
established by the installed official ForgeGradle TaskDepDummy.makeEmptyJar
bytecode (dummy-generator-bytecode.txt); both its complete JAR and class hashes are
pinned. All real dependency JARs, class-directory files and mapping hashes retain
the original strict checks. No dependency or production file was modified. The old
ZIP bytes were not retained, so this does not claim a byte-for-byte archive inverse;
it proves the regenerated classless container's exact canonical semantic payload.
