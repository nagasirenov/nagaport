# Native coordinate world sound effects (CP56 base)

Original development baseline: `149469c7403653ca6576515b730bc033a1574cfe`.

Integration baseline: `324f35ad53f98e42045ad81c7ec4b45410d663df` (CP57). The embedded composition receipt preserves all CP57 changes in10 shared consumer files; inverse hashes target the exact CP57 input, while original CP56 hashes remain in that receipt. Only sound-call receiver-to-first-argument replacements are composed. All96 original call statements still match unchanged.

## Corrected receiver-selection regression

The first CP58 candidate was NOT accepted: its broad compile found a malformed XFactoryAccelerator.java147 receiver. A regex had matched the suffix world inside ctx.getPlayer().world; the inverse checker reused that selector and missed the defect. The corrected candidate passes the full expression as the first argument. Independent full-source javac parse+analyze now verifies all96 BEFORE receivers and all96 AFTER executable owners/first arguments and all six argument expressions. Exactly one initial site was malformed. See RECEIVER-CORRECTION.md and receiver-attribution/REPORT.md; original failed-candidate evidence is retained, not relabelled as a passing result.

## Bounded production scope

96 compiler-attributed `World.playSoundEffect(double,double,double,String,float,float)` calls in67 files now invoke `WorldSoundEffects.play`. The only call-site change is moving the original receiver into the first argument of the static helper; every coordinate, literal, volume and pitch expression stays byte-for-byte in order. The manifest reverses each changed whole file exactly to CP56. Explicit entity coordinates are included; entity-overload calls, vanilla aliases, dynamic strings and other signatures are not.

45 existing bundled HBM sound names are registered as real native SoundEvents, preserving the previous17 events and their objects. Duplicate normalized declarations reuse the same object; repeated registration does not re-register an existing identical object, while a foreign object occupying an owned name fails explicitly. No fallback or missing-resource event is added. All45 real native resource accessors select a nonmissing sample in the headless probe. Sounds JSON and samples are unchanged.

25 otherwise same-signature HBM diagnostics stay excluded:21 item.upgradePlug and1 entity.slicer already share approved native paths;2 special nullChopper/nullCrashing loop triggers;1 entity.vomit has no bundled entry. Their source is not repaired by guessing an alias. The source manifest enumerates them individually.

## Exact callback contract, not a general World shim

The old coordinate method iterated `World.worldAccesses`, calling each `IWorldAccess.playSound` in index order. It did NOT invoke the Forge entity sound hook. Its client RenderGlobal callback was a no-op, including on a client before player initialization. Native `World.playSound(null,...)` is not equivalent: it adds the Forge entity hook, and WorldClient's null-player equality can accidentally cause playback.

The bounded helper reads only World-declared `eventListeners` (`field_73021_x`) using Forge's native MCP/SRG reflection lookup. It caches the Field descriptor, not any World/list. Each loop size/get rereads the live field exactly like the original indexed loop, retaining duplicate listeners, callback mutation, field replacement and empty-list/FauxWorld-style silence. Native custom observers receive the original raw volume/pitch, explicit coordinates, null excluded player, real event and bundled category. It never replaces the list or broadcasts around it. The native RenderGlobal callback is still silent. There is no new Forge PlaySoundAtEntityEvent; this deliberately preserves the original coordinate contract.

Only an exact native ServerWorldEventHandler receives pitch quantized to `max(0,min(255,(int)(pitch*63F)))/63F`, reproducing the old S29 wire field. That real native handler determines dimension, uses `(double)(16F*volume)` when volume>1F (otherwise16D), excludes no player, and sends SPacketSoundEffect with the registered event. The actual PlayerList path preserves strict distance comparison and dimension filtering. Native coordinates still truncate after multiplication by8 and decode via float precision, including negative/large edge cases.

Compatibility boundary: arbitrary third-party custom World overrides of the removed method cannot be recreated. Native listener subclasses own their callback behavior. An overriding ServerWorldEventHandler subclass receives raw pitch; a subclass inheriting or calling native super therefore uses native full-float packet pitch, not the old byte quantization. This is explicitly exercised and NOT claimed equivalent. No NTM subclass/additional observer was found by the recorded source audit. Empty native World lists represent FauxWorld's sound behavior; the still-unported complete FauxWorld class is not claimed compiled or instantiated.

## Client categories and resource packs

A client-only Forge event subscriber applies only to the45 registered migrated names, exact native PositionedSoundRecord class, and only when the current result is still the original sound. It preserves prior cancellation (including existing vacuum suppression), custom replacements/subclasses, every delegated accessor/sample/position/volume/pitch/repeat/attenuation field, and all previously approved sound paths. Only category reads go through the existing unchanged SoundCategoryResolver, preserving resource-pack append/replace/reload behavior. There is no arbitrary-mod or all-HBM sound override.

The full native registered packet encode/decode -> NetHandlerPlayClient -> WorldClient -> SoundHandler -> SoundManager -> ForgeHooksClient -> production category subscriber chain is tested, ending at an explicit final event sink before audio hardware. A receiving-client resource override changes BLOCKS to WEATHER, proving server category does not erase local pack semantics. Resource sample validation and accessor selection run with native classes. No launched-game, audio-device, perceived-volume or exact audible-parity claim is made; the two engine audio backends/sample scaling remain engine-specific.

## Reproduce

Run from any directory, using existing cached native classpath and Java8:

    python3 porting/tests/native-world-sound/run-native.py --java-home /path/to/java8 --classpath "$(cat /path/to/native-classpath.txt)" --legacy-jar /path/to/1.7.10-client.jar

The script compiles the complete actual production helper/registry/category classes plus unchanged resolver, never Minecraft/Forge stubs; runs71 inverse/source negative controls and55 independent nested-receiver AST/type fixture checks, native method-signature compile positives/negatives, registry/category/resource tests, native callback/client/server tests and independent old/new packet encode/decode comparisons. See native-proof.log, source-guard.log, attribution-before.log, engine evidence and input hashes. The broad clean mod compile is the integrating coordinator's responsibility; scoped helper compilation does not mean the67 consumer classes or entire mod compile.

Previous native block sound proof was replayed:9,979 assertions (registration count legitimately updated from17 to62). Previous native loop runtime proof was replayed:5,512 checks plus417 old handler/category oracle checks. Its historical whole-file inverse fails on SoundCategoryResolver already at unchanged CP56; existing-loop-sound.log records that failure. Runtime-only replay intentionally omits that stale historical inverse and does NOT claim it passed; unchanged CP56 resolver pin and current inverse are independently checked. The loop runner now includes the real ModSoundEvents/RefStrings dependencies of newly added sound classes. No production source exclusions were introduced.
