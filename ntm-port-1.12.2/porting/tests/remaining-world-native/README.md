# Remaining compiler-attributed World pointer reads (CP78)

Frozen base: `89b6bfc71a8b532c2fe7cdbd43bb66b7991ee365`. Exactly **31 read tokens in 9 files** change. The 34 locations come from the unmodified CP78 full compiler; this is not another broad legacy-name migration.

* Four `Minecraft.getMinecraft().player.worldObj` reads become the genuine inherited public `Entity.world` field, preserving the entire receiver. These became attributable after the earlier client-field migration.
* Nine bare Tesla/FusionKlystron field reads become `this.world`, not a local variable named `world`. Their other methods' genuine `worldObj` locals/parameters stay unchanged. The earlier bulk tile scanner intentionally excluded entire owners when it found such names; this batch attributes the exact scopes instead.
* Five RBMK inner-class reads become `TileEntityRBMKGauge.this.world` / `TileEntityRBMKKeyPad.this.world`. Javac walks the exact lexical outer instance, checks intermediate lexical field shadows/static boundaries and binds the native TileEntity field after replacement. The earlier tile migration intentionally excluded implicit outer-instance accesses.
* Three explicit `this.worldObj` reads in MapGenNTMFeatures become the genuine protected `MapGenBase.world` field, inherited through MapGenStructure.
* Ten Chunk world-pointer reads in radiation become `Chunk.getWorld()`. The native field is private final; the actual method bytecode is precisely `aload_0; getfield world; areturn`. There are no source Chunk descendants. Among 48,243 actual native dependency classes, the sole Chunk descendant is EmptyChunk, which does not override the getter. This is a **finite audited source/dependency closure**, not a guarantee for unknown external mods or future Chunk subclasses overriding the virtual method. No production reflection, helper, alias or shim is added.

Three `provider.worldObj` reads in CelestialBodyWorldSavedData remain deferred: native WorldProvider.world is protected, and the earlier provider-field proof explicitly excluded these external accesses. They require a separately justified API design, not blindly exposing or substituting the field. No writes, unresolved owners, hidden fields or unknown receiver families are included.

## Evidence and reproduction

`RemainingWorldScanner.java` attributes all current production sources against the real mapped Forge+AE2 classpath. Its location allowlist is the exact 34 CP78 diagnostics, not text matches across arbitrary files. It verifies read/write context, owner ancestry, destination-field ownership, static/lexical/local/hidden/interface fields and rejects source Chunk getter overrides. Before attribution exactly reproduces 12,775 unfinished-port diagnostics; after attribution has 12,748. This is **not** a successful full-mod compile. All 31 native destinations resolve to their exact native field or Chunk getter owner.

`controls.py` supplies 21 positive/negative compiler-attribution fixtures, including parenthesized writes, inner shadow fields, local identifiers, static contexts and virtual Chunk overrides. `check_source.py` verifies 9 full-file byte inverses and 40 token/whole-file mutation rejections. Historical earlier manifests are not rewritten. Its `reverse_source(relative, text)` / `reverse(raw, row)` can participate in a composed historical audit; unrelated later edits must first be undone through their own exact receipts. Only the 31 recorded tokens change; all radiation formulas, World calls, chunk creation/admission/unload operations and unrelated legacy aliases remain byte-for-byte unchanged. CRLF in ChunkRadiationHandlerNT is intentionally preserved.

`run.py` compiles and executes 29 native and 21 actual original-engine checks. It extracts exact changed world-pointer expressions from the current production source, then executes them in finite fixtures backed by actual Minecraft objects. Native Chunk/EmptyChunk constructors execute and preserve exact World/null identity. Original Chunk objects use constructor-bypassing allocation solely to test field reads. Tests cover nested receivers, single receiver evaluation, null failure before subsequent arguments, native entity/player replacement, direct fields despite misleading overridden tile getters, subclass-hidden fields, explicit outer-instance binding and null/rebinding. Native Minecraft.getMinecraft().player.world is executed with a controlled actual Minecraft singleton; no client launch is claimed. Test-only Unsafe bypasses unrelated World/player constructors; no platform class is replaced. The temporary original-engine JAR strips signature metadata only to allow default-package test fixtures; all original class bytes remain identical.

The original 1.7.10 client and official joined.srg identify `sa/o` (Entity), `aor/b` (TileEntity), `aqx/c` (MapGenBase), and `apx/e` (Chunk). Original Chunk storage is public mutable while native Chunk storage is private final: this batch changes **reads only**, not storage mutation or lifecycle behavior. Test evidence does not claim equivalence for original Chunk world-pointer writes.

Run, without downloads:

    python3 check_source.py
    python3 controls.py JAVA8 NATIVE_CLASSPATH_FILE
    python3 run.py --java-home JAVA8 --classpath-file NATIVE_CLASSPATH_FILE --legacy-client ORIGINAL_CLIENT_JAR --legacy-srg ORIGINAL_SRG_ZIP --verify-closure --verify-attribution

## Explicit limits

This proves only the bounded World-pointer access boundary. It does **not** execute the complete radiation algorithm, pocket rebuilds, diffusion, chunk loading/unloading, terrain generation, the RBMK machines, rendering, networking, save migration or full client/server gameplay. The unchanged downstream legacy World methods still require separate migration and may become newly visible compiler errors once the pointer resolves. No lifecycle, full production-class compilation, runtime acceptance, unknown-mod dispatch equivalence or global parity claim is made.
