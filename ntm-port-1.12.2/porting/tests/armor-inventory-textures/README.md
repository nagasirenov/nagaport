# Native armor inventory texture declarations

Baseline CP82: df18fc077648dd18e6d0c1055d82c11dc2ebdb78.
Production scope: ModItemsArmor.java and one ArmorFSB constructor statement.

## Finite change

105 complete compiler-attributed armor initialization receivers now declare their
exact original inventory texture through NativeItemTextureRegistry.texture.
ArmorFSB's original constructor-time hbm:armor default uses that same native
registry at precisely its previous point, after the unchanged worn-texture field
assignment. The helper returns the same receiver and preserves receiver-before-
argument evaluation, concrete generic type and existing repeated assignments.
Two original repeated jackt/jackt2 constructions remain; 105 declarations yield
103 final fields. No stats, formulas, materials, slots, callbacks, worn textures,
assets or registration table entries change. This armor batch does not edit
native render handlers; the separately audited metadata-route fix is composed
strictly for the current combined runtime gate, as documented below.

Twenty-one setUnlocalizedName-poisoned full receivers remain byte-identical.
They are ERROR types under native javac and are not concealed behind a helper.
The non-armor ItemDrop declaration beta and non-damageable ArmorNo9 declaration
no9 are also excluded because native runtime testing exposed a pre-existing
nonzero-metadata provider defect. See
known-earlier-itemdrop-defect.md; a separate task owns the generic route fix.

## Closure and proof

- before.tsv/after.tsv: independent full-source Java8 analysis only, without
  bytecode generation or a whole-mod build. All 106 selected whole receivers /
  helper first arguments have DECLARED native Item or ArmorFSB types. Every new
  call resolves the actual generic native helper. Original arguments, including
  rpa textures deliberately shared by ncrpa and the t51 armor placeholder,
  remain exact. Diagnostic count decreases 12,359 to 12,253, exactly 106.
- migrate.py: compiler-offset/source-shape driven transformation, byte-exact
  inverse for both complete files, immutable baseline and every excluded chain.
  No blind global regex replacement or line-ending normalization.
- owners.tsv / closure-audit.json: 29 compiler-resolved concrete, ancestor and
  additional ArmorFSB-descendant classes. No original/current custom legacy icon,
  texture setter, enum, tint or multipass callback is flattened. Selected material
  identities are IRON, CHAIN, or original separately-created enums, never CLOTH.
  The real original ItemArmor conditional single-pass/white-tint behavior is
  independently exercised. Native class inheritance plus native provider
  detection and exact ClientProxy/CallbackItemRegistration integration remain
  protected. 44 selected final items retain custom callback/TEISR ownership. The fixture
  preserves actual nondamageability for Taurun and Trenchmaster, which remain
  metadata-independent through their native custom mesh routes.
- audit.py: 98 selected PNGs exactly equal pristine original bytes and decode;
  all owner and dispatch sources are frozen, including all custom worn models and
  unchanged ModItems registration ordering. Source drift controls reject unrelated
  additions, truncation and texture mutation.
- run.py / native.log: 3,077 native runtime checks for 105 declarations / 103 final
  fields and two original overwritten identities. Real Forge item registry queue,
  event bus/subscriber transformation, texture atlas sprite population, generated
  alpha-extruded models, ModelLoader routes, ItemModelMesher lookup, custom callback
  wrapper/TEISR identity, directory and packaged-JAR resource reloads. The existing
  foundation controls additionally protect unknown custom routes, resource models,
  orphan, enum, dynamic icon and foreign owners.
- OriginalArmorIcons.java: 1,576 checks execute unmodified original Item/ItemArmor
  bytecode: same texture String/receiver identity, correct original PNG icon,
  metadata-independent pass-0 icon, no leather overlay, white tint and leather
  multipass negative control. Original client and Forge1614 mapping evidence are
  captured; no legacy rendering formulas are reimplemented.

## Honest runtime boundaries

NTM armor classes still contain unrelated unported material/slot/worn-model and
other APIs. This is NOT whole-owner or whole-ModItemsArmor compilation. Runtime
receivers are actual native ItemArmor instances with a representative IRON/HEAD
constructor, not lookalike net.minecraft or NTM gameplay stubs. Runtime registration
statements are exact production slices. The ArmorFSB test extracts the exact
production constructor body tail but supplies an explicitly native super-call
fixture; it does not certify the unchanged old int slot constructor. Source
closure/attribution proves the original actual receiver owners are eligible.
Specialized rendering tests use actual production wrapper/TEISR classes and
separate renderer identities, not execution of complete armor GL draw callbacks.
Headless atlas population decodes the actual PNGs; there is no GPU upload, launched
client/server, armor gameplay, worn rendering, or visual parity acceptance claim.

## Reproduce using cached inputs only

python3 porting/tests/armor-inventory-textures/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
python3 porting/tests/armor-inventory-textures/migrate.py
python3 porting/tests/armor-inventory-textures/dependency_composition.py
python3 porting/tests/armor-inventory-textures/audit.py
python3 porting/tests/armor-inventory-textures/run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar --evidence-prefix combined-

No network, Gradle whole-mod build, push, or main-worktree modification is needed.

## Combined CP83 metadata-route validation

The frozen armor106 change is independently recorded by commit
7906fe376ed8c7d9a657a799ca45e232932114c8 and historical native.log. The current
runtime command additionally requires the exact companion fix from
f1b2c7f617a16175a606b99506193782b913cbaf and its corrected foundation fixture.
It does not migrate beta/no9 or change the finite armor source scope.

NativeItemTextureModelHandler SHA-256 must be exactly
c2b5cba0fb608e58d7833ca3a5009ac1dc19254a307820c074ff12dc9686d761.
The foundation fixture must be exactly
1b7f34d19c9bf3035af47c70233fb3a6be130d7831bbb8005b7af709528a7675.

The historical closure manifest is NOT repinned. dependency_composition.py first
requires that exact fixed handler hash, reverses the single recorded replacement
in memory, then requires byte identity to CP82 and its original handler hash
eda9237a3df2c6d55d7758c852e007b3534db8aa77f3cb9c2e83b15c85344ac7.
Only those reconstructed bytes reach the historical dependency guard. Exact CP82
bytes are also accepted by the source-only guard to retain historical standalone
verification. Five controls reject appended/truncated bytes, bypassed custom-route
or eligibility guards, and a duplicated/reintroduced branch. Arbitrary drift and
all other protected dependency edits still fail. The receipt includes both exact
replacement blocks and hashes; it is checked against constants and immutable git
baseline evidence, not trusted as a new arbitrary baseline.

The combined runtime assertions require a customMeshDefinitions entry and NO
customModels metadata-0 table for every plain armor item. Specialized armor still
must keep the exact CallbackItemModel/TEISR owner and custom mesh without a default
table. The corrected foundation control also tests non-subtyped plain Item stacks
at nonzero metadata. combined-native.log passes the same 3,077 armor assertions,
1,576 actual original-bytecode assertions and two resource reloads against the
actual fixed shared production handler. combined-runtime-inputs.json identifies
all compiled inputs, including the exact new helper and foundation fixture.
combined-source-proof.log records the strict inverse and the unchanged full
armor closure audit. No part of this adaptation is a production armor or handler
edit; it is evidence for the two independently implemented changes together.
