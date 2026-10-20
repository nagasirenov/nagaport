# Compiler-attributed native vanilla constant mapping

Base CP51 `077492a6c31c910f34697783f1aa0cfabd8ff88e`. Exactly 3,073 static constant accesses change in 278 production files. Reserved registry/item foundation/dungeon files are excluded. No algorithms, method calls, receivers, metadata, flags or resources change.

The map is not guessed by uppercasing. The original `ajn`/`ade` initializer bytecode identifies registry strings and obfuscated fields. Original joined.srg and native MCP stable39 fields.csv identify the corresponding native field. Both complete initializer disassemblies and mapping hashes are included. Every one of 336 mappings was then executed against the actual isolated 1.7.10 engine and actual native 1.12.2 engine: registry names, object identity and numeric IDs match. Six fields without this exact mapping remain excluded, including renamed wooden doors/fences and cooked_fished. This proves constant identity, not that every block/item implementation behaves identically between versions.

`ConstantAccessScanner` uses javac parsing and attribution. It accepts only field-select expressions whose receiver resolves to the actual native Blocks/Items TypeElement. Imports and conservative whole-unit variable/class/type-parameter shadow checks further restrict selection. It never edits comments, strings, arbitrary similarly named fields, static-import bare identifiers, unresolved receivers or already-native names. Java UTF16 positions are applied without normalizing UTF8/newlines. Full before/after hashes and exact inverse edits pin all unrelated bytes.

The initial attribution analyzed 3,573 selected sources and reported 48,159 unfinished-subset diagnostics. This is NOT successful compilation or the whole-mod count (the independent CP51 full run reported45,144). Only resolved native class-owner accesses were accepted. `attributed-accesses.tsv` is the captured owner evidence, not a claim that containing methods execute.

Reproduce with existing dependencies, no downloads:

    python3 porting/tests/native-vanilla-constants/run.py --java-home JAVA8 --classpath NATIVE_CLASSPATH --legacy-client ORIGINAL_1710_CLIENT_JAR

The runner compiles the actual native constant/registry proof and scanner, runs 336 old/native identity comparisons,15 ownership/import/shadow/inherited-field/Unicode fixtures and834 byte-drift/reintroduction controls. Add `--verify-attribution` for the more expensive full owner re-attribution in a temporary reconstructed source copy; it never rewrites production and requires about3GB heap. Expected unfinished-source diagnostics are not suppressed as successful compile results. Full mod compile belongs to the frozen checkpoint evidence.

No original Minecraft binary, native dependencies or credentials are redistributed. Original client SHA256 is pinned in constant-map.json. Windows execution, full client/server/gameplay and vanilla implementation parity remain unverified.
