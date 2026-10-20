# Private DiFurnace raw quantity fallback

This completes the scoped furnace migration without accepting changed quantities
for dynamic-nonempty/cached-empty repaired stacks. No shared helper/storage or
other machines are changed.

The private quantity method evaluates its supplied stack once. Native nonzero
getCount is already the raw value; zero falls back to read-only Field.getInt.
The lazy nested holder resolves stackSize OR field_77994_a using genuine Forge
ReflectionHelper. IllegalAccessException becomes explicit IllegalStateException;
missing-field failure is not hidden. Null still throws NPE on getCount. The
reader itself returns actual raw values, including negative counts and EMPTY1;
it does NOT normalize or silently invent absence.

Only three preexisting reads use it: both capacity operands and merge-result
quantity. Explicit dynamic-empty guards exclude EMPTY/null/AIR/invalid stacks
before those reads. The second recipe lookup now also rejects null/isEmpty
BEFORE any output/input mutation, preventing an EMPTY raw1 leak if the recipe
changes between lookups. No additional lookup is introduced. Fuel/progress in
update may already advance before this second check; no transaction/balance
rewrite is made. Fuel's post-shrink getCount==0 remains native because shrink
updates the cache at that exact point.

A repaired raw64 output now rejects +3 as the prior raw capacity test did. All
raw addition/overflow/aliases remain unchanged. Dynamic-empty malformed states
are still handled under the user's furnace-local approval; this read fallback
does not resurrect them or affect admission policy.

## Mapping and runtime evidence

Official cached stable39 fields.csv and mcp-srg.srg map stackSize to
field_77994_a. Official notch-srg maps aip/c to the same SRG field. Genuine Forge
DeobfuscationTransformer uses FMLRemappingAdapter in production; ReflectionHelper
findField tries supplied field names literally, sets access, and returns the
first present field. It does NOT automatically translate our strings. Both
literal names are therefore supplied. mapping-evidence.txt retains the relevant
native javap and exact mapping lines.

run.py first runs the complete machine suite against the genuine MCP native
class. It then takes the actual native ItemStack.class bytes from the cached
classpath and remaps ONLY the declared private quantity field and all references
to it to the official SRG name. All other fields, methods and logic remain the
real implementation. A separate JVM loads this remapped class first and runs
THE SAME complete machine suite, exercising real Field.getInt fallback with the
MCP field absent. Native source/result class-byte hashes are logged. This is
not a fake ItemStack, synthetic field-only fixture, or a claim that the whole
production obfuscated game was launched. It is executable proof of the exact
SRG field layout/lookup path, combined with official mapping/transformer evidence.
RemapQuantityField is test-only and never ships as production behavior.

PASS504 checks on each layout include reader signed-int/null/EMPTY values,
public stale-cache repair, actual no-overstack capacity rejection, all prior
positive mechanics, and second lookup changing to zero/negative/positive or
null while preserving lookup count and already-advanced fuel/progress timing.
Seven wrong production-class mutants fail: energy, merge, fuel receiver,
capacity comparison, input cost, cached-capacity substitution, invalid second
result accepted. No reflective writes, Unsafe, AT, global stack mutation,
downloads or production API fakes.

Run: python3 porting/tests/difurnace-native/run.py
