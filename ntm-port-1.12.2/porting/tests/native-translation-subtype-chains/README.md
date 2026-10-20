# Fresh armor-subtype translation-name chains (CP73)

Base a4739afa41966b75e2012bccd7df919e51d5c3a0. This bounded continuation changes
44 method identifier tokens in ModItemsArmor.java, and no other production bytes.
The 16 fresh concrete ArmorFSB descendants have an attributable constructor and
one inherited cloneStats call immediately before the naming setter. Already
native chains are observed but not edited. Longer builder chains, anonymous or
dynamic receivers, factories, ItemBlock, domain naming and the four custom
texture-side-effect setter families remain excluded.

SubtypeScanner attributes all 4,065 actual production source files against cached
native Forge/stable_39 dependencies. It requires the exact fresh subtype, no
anonymous class, inherited ArmorFSB.cloneStats, a String/null key argument,
no legacy setter and no non-Item native setter in its complete member hierarchy.
It also rejects any cloneStats or setRadResist override. ReturnProof parses the
actual unchanged cloneStats body: 13 assignments, one ignored-result
this.setRadResist(original.radResist), then return this. Thus the fresh runtime
class remains exact after the builder; no substituted receiver, base-method
bypass or changed virtual dispatch occurs. Every selected destination resolves
post-edit to Item.setTranslationKey(String), returning Item. Constructor and
argument evaluation, exceptions, radiation side effects and subsequent texture
calls remain in their original positions. Every armor source is pinned unchanged.

Independent SubtypeDestinationScanner separately enumerates receiver AST nodes,
constructor/method/field owners and argument records. All receiver/argument
records are byte-identical before and after, and all 44 outer destinations are
native Item methods. Eight controls accept the inherited fresh subtype and reject
dynamic, factory, anonymous, cloneStats override, legacy setter override, native
setter override and wrong-argument cases. Exact CP73 whole-file inversion and 44
mutation guards pass. This is a token migration, not new builder behavior.

Freshly executed probes use real archived 1.7.10 and native Forge classes:
- 53 armor checks cover inherited setter ownership, raw String identity,
  receiver identity, null/empty/Unicode/prefixed keys and real stack overloads.
- 161 base naming checks cover native Item/Block/ItemBlock delegation and virtual
  no-argument naming dispatch, with the original client as the reference.
- Four original/native armor hierarchy checks and exact production return proof.
The new armor fixture is explicitly a test-only native ItemArmor descendant,
not a compiled NTM armor implementation. The native stack getter uses its raw key,
not the custom no-argument override; the oracle asserts that distinction.
Historical native-translation-names bytecode equivalence proofs are hash-pinned
and reused, not presented as newly executed bytecode checks.

Both full-source AST passes report 13,595 errors: resolving naming calls exposes
subsequent unported texture calls, so no net error reduction is claimed. Armor
constructors still need native slot conversion; full NTM owners, texture/model
integration, radiation/armor gameplay, full builds and client/server acceptance
remain unproven. No resources, name arguments, prefixes, display strings,
registration, balance, dependencies, source exclusions or shims change.

Reproduce from module root with existing local Java8 and cached dependencies:

    bash porting/tests/native-translation-subtype-chains/run.sh

The script validates checked-in before evidence against the sealed Git base,
runs fresh native and post-attribution proofs and verifies the exact patch.
To regenerate before evidence, run SubtypeScanner and SubtypeDestinationScanner
against an isolated CP73 Java-only source list before running verify.py --apply.
Do not run --apply against unknown source content: the guard rejects it.
