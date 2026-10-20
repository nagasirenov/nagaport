# Recovered transport nullable cargo NBT boundary

This evidence package was reconstructed after loss of the original workspace.
See `recovery-status.json` for the distinction between historical results and
current verification. The lost native verification log has **not** been fabricated.

## Recovery and current verification status

Original CP76 base: `0aeda80b859b80afc8af26398366ef05f68478af`.
Recovered source commit: `d25dcb9f0908d76b1e9eca96b75e58f6397de355`.
The reconstructed production patch SHA-256 is
`f46e60c965d65ec63c3f702680c7493bdc87d01fe3a81d6f02a702faeb1ea2fa`,
identical to the original frozen seven-owner production patch. All seven original
before/after file hashes and the unchanged production helper hash also match.
DeliveryDrone's original CRLF/mixed endings are preserved byte-for-byte.

The source verification script has been run in the replacement workspace. Its real
output is `recovered-source-verification.log`. It verifies exact source reversals,
pristine ownership/read/write contracts, and generates 14 production storage slices.
`run.sh` also passes shell syntax checking. **The reconstructed Java tests have not
been compiled or run in this replacement workspace.** Java 8 and the genuine mapped
Forge 1.12.2 classpath must be restored before that claim can be made.

Before workspace loss, the conversation recorded a successful run with 222 transport
assertions and a successful rerun of the unchanged 191-assertion native helper probe.
Those are historical reports, not results of this reconstruction. The prior 123-check
1.7 loader evidence and machine/bomb logs remain hash-pinned historical inputs.
No dependency installation, download, fabricated log, mock native API, or full-build
success claim was used for recovery. Exact byte recovery is asserted for production,
not for the reconstructed evidence package as a whole.

## Finite ownership audit

- DeliveryDrone owns nullable 18-entry `slots`; `Items`/unsigned-masked `Slot`
  preserves its existing case, bounds, replacement array, and non-null save filter.
- MovingPackage owns nullable `contents`; `contents`/unsigned-masked `slot` and
  integer `count` preserve dynamic replacement sizing. The writer omits nulls.
  Existing unchecked dereferences in other package consumers are unchanged.
- RequestDrone's nullable `heldItem` is transferred/dropped/cleared as cargo.
  Present `held` replaces it, absent `held` retains it. A null save neither writes
  a new held record nor removes an existing held record from a reused compound.
- Soyuz/SoyuzCapsule own/share their payload array. Their lowercase `items` lists
  and signed lowercase `slot` bytes are unchanged. Neither loader replaces its
  array; sparse reads retain untouched entries. Writers omit null entries.
- Bobmazon's nullable payload is checked before delivery. Missing `payload` still
  fails before assignment, wrong type still fails at the cast, invalid registry
  data still assigns null, and its original null writer still throws.
- ParachuteCrate clears its owned `items` list in place, then appends all decoded
  entries including null. Its original writer still throws on null entries. No
  filtering, replacement list, schema repair, or exception guard was invented.

These are NTM storage boundaries, not native `NonNullList` or inventory return
contracts. The unchanged `NullableInventoryStackNbt` validates named item IDs then
uses the genuine native ItemStack constructor. Registered zero/negative records
remain distinct non-null stacks. Public native getters/serialization retain native
empty-stack semantics. Old numeric-ID 1.7 worlds are deliberately not imported.
`scope-audit.json` accounts for 37 baseline source calls, seven selected and 30
remaining. Deferrals are finite-scope choices, not incompatibility conclusions.

## What the reconstructed tests exercise

`verify_and_extract.py` pins each CP76 before/after file and pristine source hash,
reverses only the utility import and loader substitution, and compares all selected
reader/writer slices with the pristine source. Individual ownership declarations,
nullable save filters or deliberate unguarded writers, and array/list transfers are
checked. A supplied native classpath must contain exactly the sealed real mapped
`ItemStack.class` hash, not a shadowing stub.

Four generated hosts contain complete storage reader/writer bodies: MovingPackage,
SoyuzCapsule, Bobmazon and ParachuteCrate. DeliveryDrone uses exact contiguous storage
portions plus its actual size method, excluding superclass/fluid/chunk/data-manager
logic. RequestDrone uses the exact held-cargo conditional blocks only. Soyuz uses
exact list declarations, load/save loops and list write, omitting skin/destination
accessors rather than replacing them with mocks. Host wrappers are synthetic; Soyuz's
host constructor uses its original array-allocation statement. These are explicit
source-slice tests, not compiled or instantiated production entities.

`NativeTransportNbtTest.java` reconstructs coverage for reset/retained arrays and list
aliasing, case-sensitive keys, sparse and duplicate records, signed/unsigned bounds,
registered nonpositive counts, raw signed storage versus native emptiness, native
save serialization, NBT tag aliases and populated roundtrips. It includes dynamic
package lengths/negative-length failure, Bobmazon/ParachuteCrate's preexisting failure
paths, RequestDrone's missing/present tag distinction, and the capsule discriminator.
The unchanged native helper probe adds genuine native Item update hooks and Forge
capability initialization/roundtrip checks when rerun.

This does not establish whole-owner compilation, inventory API completion, entity
behavior, routing, collision, GUI/network behavior, full-mod compilation, client or
server startup, or in-world correctness. Excluded state is protected by exact source
reversal, not runtime-tested by these hosts.

## Rerun when the genuine native classpath is restored

Set Java 8 `JAVA_HOME` and actual mapped Forge 1.12.2 `MINECRAFT_CLASSPATH`; run
`./run.sh` and capture its fresh output separately. Record the fresh result before
updating `recovery-status.json`. Never copy the historical success text as a new log.
For preserved source endings, use `git -c core.whitespace=cr-at-eol diff --check`.
