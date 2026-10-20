# Bounded native hazard-block sound construction

Exact base: CP66 `c986d3f35683a7b81fe0e2caf928f6a47a7a6b5f`.

## Production and scope

26 existing construction expressions in ModBlocks: 21 fresh BlockHazard, two fresh BlockHotHazard, and three fresh BlockHazardFalling. Two small public NTM-owner `withSoundType(SoundType)` builders invoke the genuine protected native `super.setSoundType` and return the same owner. No native Block access change, reflection, foreign-object shim, superclass substitution, registry change, model change, new sound event or resource change is made.

The complete preceding receiver is cast to the proven owner domain at the original sound setter position. Earlier setters and argument evaluation remain in their original order. BlockHotHazard has an implicit constructor invoking the unchanged Hazard constructor and does not override any selected builder. Native setters and the original NTM makeBeaconable/setDisplayEffect return their exact receiver. The inserted casts therefore cannot fail at these named non-anonymous fresh-constructor sites. This does not assert safety for arbitrary Block receivers or unknown subclasses.

Inventory was 139 remaining ModBlocks calls across 57 constructor owners, plus two owner-local calls. Fourteen vanilla BlockFalling constructions are not changed. All unselected sites, including Sellafield's independently unported constructor and owner-local calls, remain unchanged. BlockBase and both earlier plain-cube owner builders remain untouched. ModBlocks CRLF and every unrelated byte are preserved; use `git -c core.whitespace=cr-at-eol diff --check` rather than treating original CRLF as new whitespace.

## Evidence and honest build outcome

- Complete Java8 attribution of all 4,063 source units, before and after, records 26 named constructor executable identities and every preceding receiver, virtual method executable owner/return, sound argument and destination. All 26 native builder invocations resolve. No syntax errors.
- Diagnostic count stays 15,386: exactly 26 old setStepSound diagnostics disappear and exactly 26 previously masked setBlockTextureName diagnostics appear. All other diagnostic message multisets are unchanged. This fixes a construction root but is not a net compile-diagnostic reduction, full build or functioning texture claim.
- Native runner compiles all 26 exact construction prefixes through their existing setters immediately before the still-unported texture call. Exact production constructor bodies/fields, makeBeaconable, setDisplayEffect, the new builders, and implicit HotHazard constructor inheritance are extracted into explicitly bounded NTM-only fixtures. Native Block and BlockFalling and every other Minecraft/Forge dependency are genuine. Full production ModSoundType, ModSoundTypes, ModSoundEvents and RefStrings compile, unchanged.
- 751 finite assertions verify exact assigned sound references, actual native ordinary/contextual getters, break/place/step/hit/fall identities, volume/pitch, constructor material, name, receiver identity, null values, beacon/display field assignments, and evaluation order. A further 12 existing original-bytecode sound oracle assertions check the already-established legacy metal stone samples and pitch 1.5. The same original jar SHA256 is enforced.
- Three runtime mutation controls reject dropped assignment, substituted sound argument and null builder return. The controls must compile first; compiler failure does not count as a kill.
- The existing plain-cube-sound proof and all its files are hash-identically reused, not rerun or recast as new full-rendering acceptance. Existing sound mapping semantics are retained, not remapped. Source hashes also pin unchanged actual sound classes and native launcher/oracle.
- Exact full-file inversion returns all three modified production files to baseline. Fragment replay additionally passes forward/reapply/reverse and preservation of independent edits, enabling coordinator integration with overlapping ModBlocks or drop-callback changes.

Unrelated hazard gameplay, radiation, full owner callbacks, registry/model loading, audible output, full client/server and complete mod compilation remain unverified. No Minecraft/Forge stub or placeholder is shipped. The NTM fixture MainRegistry supplies only the existing tab and RNG inputs and is not production code. The source guard is the isolated three-file baseline guard; a later composed main requires the included fragment receipt, not a silent rebase of these assertions.

## Reproduction

Run from the module with existing cached dependencies only:

    python3 porting/tests/native-hazard-sound/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/native-hazard-sound/check-source.py
    python3 porting/tests/native-hazard-sound/check-replay.py
    python3 porting/tests/native-hazard-sound/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar
    python3 porting/tests/native-hazard-sound/check-mutants.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar

For composed integration, first run `apply-fragments.py --root TARGET_MODULE` in dry-run mode. Then add `--apply --receipt RECEIPT_JSON`. No target file is written before all fragments are validated. `--reverse` removes only this batch's fragments; already-applied/reversed fragments are idempotent. The coordinator owns the combined full build.
