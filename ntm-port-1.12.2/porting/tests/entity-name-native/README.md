# Bounded raw entity-name migration

Baseline: CP84 `502f0b9d` (the full authoritative commit is in manifest.json).
This is intentionally **not** a blanket `getCommandSenderName` rename.

## Three production edits

- `EntityThrowableNT.java:321–325`: the existing `instanceof EntityPlayer` guard proves the receiver of the one owner-name capture. Only the virtual selector becomes `getName`; the saved-name precedence, null/empty handling, and `ownerName` NBT key are byte-identical.
- `CompatExternal.java:219–223`: the target predicate is registered under `EntityPlayer.class`. The actual in-repository dispatcher (`TileEntityTurretBaseNT.java:594–599`) checks `Class.isAssignableFrom(e.getClass())` before invoking it. Only the name selector changes. The literal `Target Practice`, sentry condition, and return values are untouched. This proof covers that dispatcher; it does not claim arbitrary third-party direct invocation of the public callback with nonplayers is safe.
- `EntityDummy.java:30–31`: only the override declaration becomes `getName`. The exact health/max-health calculation stays intact, retaining its precedence over custom names and its virtual Entity/ICommandSender dispatch.

The pinned original `yz.b_()Ljava/lang/String;` and native `EntityPlayer.getName()Ljava/lang/String;` both directly return `GameProfile.getName()`. Neither uses formatted display names. Production still calls the virtual name method, not GameProfile directly, so custom player overrides are not flattened.

## Complete audited old-selector closure

The javac census finds 34 actual invocations and the source scan finds three local declarations. Two invocations and the Dummy declaration are selected. The remaining 32 calls and both minecart declarations are unchanged and individually recorded in manifest.json. The commented test print is not a production call.

- Twenty-four bomb-placement logs have a receiver named `player` but statically typed `EntityLivingBase`, as does the syringe logger. Those 25 calls are not silently treated as players.
- `EntityMinecartDeobf.java:1000–1002` extends Entity. `EntityMinecartModBase.java:1235–1238` extends vanilla EntityMinecart; `EntityMinecartTest` is its local descendant. Their own non-null entityName values, including empty strings, differ from native Entity's nonempty custom-name channel. The old null fallback reaches legacy entity translation (or vanilla cart's separate name field), while a native `super.getName` can observe a native custom tag. Neither override nor either super call is changed. No local descendants of EntityMinecartDeobf were found.
- Broad Entity calls remain at `ModEventHandler.java:367`, `EntityBufPacket.java:54`, and `ItemRenderFolly.java:255`. Both ICommandSender logs in `CommandPacketInfo.java:46,50` remain too. Real native Entity adds a custom-name branch that original Entity lacked. Native EntityItem also adds that branch; horse naming has a changed class structure. Raw command sender names match for concrete Server/Rcon/command blocks, but an arbitrary sender can still dispatch through an Entity or CommandSenderWrapper.
- **Unresolved entity/inventory dispatch collision:** `EntityRailCarCargo.java:164` overrides `hasCustomName`, and `:186–188` already declares `getName` returning its inventory entityName or `super.getName`. Generic entity migration would select this method although the original generic legacy name call selected a different inherited method. This existing file is audited and unchanged, not silently fixed or endorsed by this patch.

No Item/Block translation-name evidence is used as proof for this root. No helper/shim, cast, source exclusion, binary copy, config change, or lifecycle adaptation is added.

## Reproduce offline

From the project root:

    python3 porting/tests/entity-name-native/source.py
    python3 porting/tests/entity-name-native/engine.py
    python3 porting/tests/entity-name-native/runtime.py
    python3 porting/tests/entity-name-native/attribute.py

Defaults use the existing Java 8 toolchain, native classpath file, original client archive, and SRG archive. Engine path options are documented by `engine.py --help`; other tests accept `JAVA8_HOME`, `NATIVE_CP_FILE`, and (runtime) `LEGACY_CLIENT_JAR`. No downloads are needed. No proprietary binary is retained as evidence.

- Engine replay checks 34 pinned classes, 41 relevant method declarations/bytecode checks, and 34 exact SRG mapping lines.
- Exact forward and inverse reconstruction protects all three complete changed source files; 12 wrong-selector/unrelated-drift controls are rejected. Complete hashes also protect the audited exclusions and targeting dispatch source.
- Actual original signed engine player classes execute 16 assertions for raw String identity, including null, empty, case, Unicode, and formatting-code values.
- Native runtime executes 194 assertions on actual player name methods and real NBTTagCompound strings. It compiles exact projections of the selected owner-name guard, targeting expression, and Dummy method, then exercises custom player and Dummy virtual dispatch plus a display-name negative control. It does not compile or instantiate the complete still-unported EntityThrowableNT, CompatExternal, or EntityDummy owners. Constructor-bypassed player fixtures are only used for the profile-field getter; the health-status fixture uses a real native EntityLiving constructor and attributes.
- `attribute.py` parses/analyzes all 4,065 source units without code generation, checks the two actual native Entity.getName method attributions, and compares normalized diagnostics. The sealed summary records 12,066 → 12,063 errors, the three exact removed diagnostics, and zero added errors. Nondeterministic javac capture IDs are normalized only when comparing otherwise unchanged selector attribution records. It is not a successful whole-mod build or gameplay test.

Whole-mod readiness, unrestricted foreign overrides, generic entity/command naming, all minecart custom-name/NBT lifecycles, and the railcar collision remain unresolved.
