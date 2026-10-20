# Entity render-distance migration, bounded CP86 candidate

Base: CP85 `abe006a0845275e8be52417967ac8ebf6f283a52`.

## Why not the native setter?

The original `sa.j` / `Entity.field_70155_l` is a **public instance double**, initialized to `1.0D` by the real Entity constructor. Native 1.12.2 `Entity.renderDistanceWeight`, `getRenderDistanceWeight`, and `setRenderDistanceWeight` are **static**. The native getter/setter are exact global field load/store operations. Replacing these legacy constructor writes with that setter would change every entity's rendering and would make Ghost construction repeatedly multiply global state. Production never calls either native accessor.

A complete ASM scan of the original 1,803 classes finds two field-read methods: Entity's range method and the unrelated ItemFrame range override. None of the selected classes inherits ItemFrame. Other original writes are Entity's initializer, the client-player constructor, and arrow constructors; none is an intermediate superclass of these owners. The complete native 4,876-class scan finds only the native Entity global initializer, getter, setter and range method accessing its private field.

Original Entity's 3D range method virtually invokes its range method. Native `Render.shouldRender` and `RenderGlobal.renderEntities` virtually invoke `Entity.isInRangeToRender3d`, which virtually invokes `isInRangeToRenderDist`. Actual native runtime checks exercise that inherited 3D-to-owner override dispatch, not merely isolated arithmetic.

## Exact finite production scope

Thirty old accesses in eleven files: twenty-nine simple constructor writes and Ghost's one constructor read-modify-write. Ghost's implicit read observes the proven original `1.0D` initialization. No standalone reads, local declarations, later mutations, or references outside these owners occur in the 4,065-source-unit baseline. The source census is parsed by actual Java 8 javac, without attribution or code generation, and paired by exact file and line with all thirty CP85 native compiler diagnostics attributing the inaccessible field to Entity.

Eight owners receive a complete local range override with the original evaluation order:

    double range = this.getEntityBoundingBox().getAverageEdgeLength();
    range *= 64.0D * weight;
    return distance < range * range;

There is no new helper, fake Entity superclass, reflection, access transformer, visibility workaround, engine mutation, or global camera/render multiplier substitution. Bounding-box reads use the already-migrated native accessor. No NaN fallback is inserted: the original engine comparison is false for a NaN range, unlike native Entity's new fallback.

- Ghost: original `1.0D *= 10` becomes a fixed local range weight of `10.0D`.
- Tankbot: fixed `24.0D`.
- WormBaseNT: fixed `15.0D`. BOTPrimeBase's duplicate `15.0D` write is removed; BOTPrimeHead and Body inherit the same Worm override.
- BulletBeamBase: fixed `10.0D`; its additional constructors delegate through the one writing constructor.
- Bullet, ExplosiveBeam, Rocket and Schrab: each has one extra direct-super constructor that never assigned the legacy field. A private, owner-local `renderRangeWeight` starts at `1.0D`, and the twenty existing writes retain their original positions and `10.0D` values. Thus those four default-weight paths remain `1.0D`, while the other constructors retain `10.0D`. This state is not a public compatibility field, is not shared globally, and adds no serialization behavior.
- BulletBaseNT's four writes and BulletBaseMK4's one write are removed, not moved into the native global setter. Their existing EntityThrowableInterp -> EntityThrowableNT chain already overrides range with `edge * 4.0D`, then `* 64.0D`, and never consumes the old weight. That complete method and its native projected execution remain unchanged. CombineBallNT and BulletBaseMK4CL inherit that same behavior.

The four variable-weight owners' `entityInit` overrides contain only one or three `dataManager.register(..., Byte.valueOf((byte) 0))` calls, with no local descendants. The actual native `register` and private `setEntry` bytecode do not read the Entity or call back into it. Thus these repository callbacks do not observe the private field's temporary Java default `0.0D` before its initializer runs. Foreign constructor callbacks (including external Forge handlers), or a new subclass invoking range from `entityInit`, are not supported by this proof. Fixed-constant owners likewise make no promise about arbitrary foreign construction-time rendering.

The complete local ancestry/descendant closure is recorded in `dispatch-closure.json`. Its seven otherwise unchanged ancestor/descendant source files are byte-pinned along with the eleven complete changed files. Intermediate original and native EntityLivingBase, EntityLiving, EntityCreature and EntityMob classes declare no range override. No existing production range override is overwritten.

## Offline reproduction

From the module root:

    python3 porting/tests/entity-render-distance-native/source.py
    python3 porting/tests/entity-render-distance-native/closure.py
    python3 porting/tests/entity-render-distance-native/engine.py
    python3 porting/tests/entity-render-distance-native/init_audit.py
    python3 porting/tests/entity-render-distance-native/run.py

`JAVA8_HOME`, `NATIVE_CP_FILE`, `LEGACY_CLIENT_JAR`, and `LEGACY_SRG_ZIP` override the existing local inputs. No downloads. `closure.py` needs the baseline commit in local Git history and extracts only its source tree into a temporary directory. It parses source only, never compiles the mod. The engine census compiles only its test scanner.

Evidence:

- Eleven exact forward/inverse complete-file reconstructions, seven unchanged closure files, 59 wrong-weight, selector, formula, comparison and unrelated-drift controls.
- Thirty exact CP85 compiler diagnostic positions, with parsed constructor-write ownership/usage classification. This is not a new whole-project attribution run or an asserted whole-project error reduction.
- Twelve pinned original/native engine classes, eight exact SRG lines, and replayable complete engine field/callsite scans.
- Thirty-nine original constructor bodies checked against the exact permitted edits. Runtime uses thirty range-owner constructor-path state projections plus the unchanged throwable method, not those complete gameplay constructors.
- Nine complete production range-method projections compiled against real native Entity, World, AxisAlignedBB and NBT types. No platform stubs. Actual native setters/getters execute raw-bit controls that demonstrate global sharing; production does not use them.
- 5,580 range decisions match the actual original Entity binary byte-for-byte, covering default/assigned weights, twelve box shapes, strict threshold neighbors, negative distances, signed zero, tiny/large finite values, NaN and infinities. Each decision is unchanged under six native global weights. One bounding-box read per call, null-box behavior, per-entity state isolation, and actual native inherited 3D virtual dispatch are checked.
- The original engine oracle calls its real constructor and range method. A temporary container omits only JAR signature metadata so a default-package test subclass can load; all original class bytes remain untouched. No proprietary archive is retained in this evidence.

## Limits

This proves the finite repository's post-construction render-range behavior. It does not preserve an external mod's ability to read or mutate the removed 1.7 public field, nor claim arbitrary foreign subclasses/overrides or constructor re-entrancy are compatible. The private owner-local fields are deliberately not a substitute public API. Native global camera/render-distance scaling is deliberately not substituted for the old per-entity values. Full affected entity owners remain blocked by other unmigrated APIs; complete owner compilation, full-mod compilation, gameplay, server/client loading and visual perfection are not claimed.
