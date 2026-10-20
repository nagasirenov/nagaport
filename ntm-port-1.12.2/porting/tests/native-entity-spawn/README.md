# Bounded native entity spawning (CP57 baseline)

Production: **43 call-name tokens in 17 files** change `World.spawnEntityInWorld` to actual `World.spawnEntity`. Owned source World subclass audit finds only the original FauxWorld, with no spawn override. No adapter, new engine class, interception, exception swallowing, filtering, source exclusion, or registry/event change. All receivers are compiler-attributed **exact World** variables/fields; arguments are immediate, non-anonymous, exact vanilla five-argument `EntityItem` constructions. All selected return values were already ignored. Receivers, constructor arguments, side effects, control flow, stack identity and invocation count are byte-for-byte unchanged by the exact inverse audit.

This is a bounded standalone NTM path migration, **not universal equivalence for arbitrary entities, World subclasses, or third-party constructor hooks**. Existing native calls in EntityMovingItem and BlockSupplyCrate are unchanged controls. Reused entities, deserialized entities, transfers, arbitrary mod entity constructors, custom World receiver types, factory results, player spawns and local-variable preparation paths remain outside production scope.

## Genuine old/native boundary findings

* Supplied vanilla 1.7.10 `ahb.d(sa)` computes floor(x/16), floor(z/16), honors forceSpawn and player bypass, rejects unloaded chunks before callbacks, adds the player before sleeping-flag update, then chunk/list/callback and true. `mt` (WorldServer) inherits it. Supplied Forge1614 World patch adds server null/item-snapshot suppression and EntityJoinWorldEvent cancellation unless forced/player. Its WorldServer/WorldClient patches do not override this spawn method.
* Actual native2768 World preserves that base sequence, including cancelable event before chunk/list/callback, force/player bypass, server snapshot item suppression, and native boolean return. Its WorldServer **first** rejects dead entities or duplicate UUIDs, with special existing-unloaded/player identity handling. This is a real incompatible boundary, not a mapping rename assumption.
* Both actual client implementations track attempted entities and queue failed additions; successful non-minecart item spawning avoids the separate minecart audio path. Native World calls the native Entity.onAddedToWorld lifecycle callback; native server also updates its UUID index. Those target-native lifecycle mechanisms are deliberately retained, not emulated with legacy lists.
* Spawn does not change an entity's dimension or implement a dimension transfer. Native test checks a deliberately differing dimension remains unchanged. Negative chunk coordinates and existing chunk ownership are tested. Unloaded server non-forced rejection is executed; forced unloaded **server chunk generation is not exercised**, while forced unloaded client insertion/queue behavior is.
* Exact vanilla EntityItem constructor runs Entity superclass setup, stores the same ItemStack, and calls getEntityLifespan. Supplied Forge1614 constructor patch adds the same lifespan hook. Neither owned vanilla constructor marks the entity dead or intentionally reuses a UUID. Original NTM's only EntityConstructing subscriber branches on EntityPlayer/EntityLivingBase, not EntityItem. Current equivalent subscriber only adjusts player ashes; HbmEntityProps capability attachment is restricted to living/player entities. NTM's sole getEntityLifespan override is ItemNuclearWaste, returning Integer.MAX_VALUE without entity mutation. The selected calls have no intervening entity setter or callback after construction.

### Explicit compatibility gate

Both Forge versions post EntityConstructing. Third-party handlers (or target capability hooks) may mark even a newly constructed EntityItem dead, change its UUID, or register it already. Native WorldServer then rejects it before EntityJoinWorldEvent; legacy World did not have that rejection. Native tests execute a constructor-hook counterexample. Generated UUID collision with an existing entity is also a native identity boundary. **Fresh syntax alone is not a universal proof**. This patch is justified only for audited standalone owned paths with ordinary fresh identity, not arbitrary extension behavior. Do not expand it to reused/dead/identity-controlled entity paths or bypass native server checks to hide the difference.

## Evidence and coverage

`run.py` runs:

* 127 native assertions using **actual WorldServer, WorldClient, native chunk providers, Chunk.addEntity, native callbacks and genuine Forge event dispatch**. Test world instances are Unsafe-allocated and required world infrastructure initialized; their spawn/callback implementations are not overridden. A small EntityPlayer subclass supplies abstract predicates for the player-flag control; production scope does not include players.
* Fresh success, cancellation, forceSpawn override, unloaded rejection, client failure queues, snapshot behavior, player cancellation bypass, same entity/stack identity, server UUID indexing, native added-to-world callback, dimension retention, negative coordinates, duplicate/dead rejection, constructor-hook incompatibility, receiver/argument order, null evaluation and actual return checks.
* One exact selected BlockMassStorage spawn statement extracted from current production source, compiled against native APIs and executed for both accepted/canceled joins. This is a bounded statement test, not whole BlockMassStorage compilation.
* 49 checks invoking **actual supplied vanilla 1.7.10 bytecode**, including loaded/unloaded/force/dead cases, client attempted/queue sets and real old callbacks. Forge1614 additions are independently audited from the authentic supplied patch, **not claimed executed by that vanilla oracle**.
* Two positive and six negative typed scanner fixtures (non-World receiver, World subtype/shadow dispatch risk, reused entity, anonymous EntityItem subclass, different constructor and shadow class). Full native post-attribution proves all 43 selected sites resolve to actual World.spawnEntity and EntityItem constructors; two preexisting native sites are unchanged controls. Javac attribution diagnostics are expected from unfinished unrelated source and are not a compile success claim.
* Exact per-file inverse hashes against CP57, proving no modifications besides the 43 method-name tokens. `original-source-provenance.json` records supplied-original source hashes and matching inline-call counts for the 17 source files; CP57 whole-file before hashes remain the precise patch baseline.

`audit_dependencies.py` pins original binary, Forge1614 userdev, mappings and actual five native class byte arrays. `boundary-bytecode.txt` and three small Forge1614 patch excerpts retain inspected semantic evidence. No dependency download or pristine tree copy is required.

## Reproduction

From project root, provide your already-resolved classpath and existing dependencies:

    python3 porting/tests/native-entity-spawn/run.py --java-home JAVA8 --classpath-file CLASSPATH_FILE --legacy-client LEGACY_CLIENT_JAR --attribute
    python3 porting/tests/native-entity-spawn/audit_dependencies.py --legacy-client LEGACY_CLIENT_JAR --legacy-forge FORGE1614_USERDEV_JAR --legacy-mappings MCP1710_SRG_ZIP --classpath-file CLASSPATH_FILE

All paths are arguments; no machine-specific path is required by the test code. Proof log records the executed local run. Native full-mod compilation, client graphics/session, multiplayer, integrated mod Forge handlers, server chunk generation and saved-world acceptance are **not** established. The remaining legacy spawning sites remain intentionally unresolved for later behavioral work.
