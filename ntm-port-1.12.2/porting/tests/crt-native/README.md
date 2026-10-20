# CRT native bounded proof

This suite independently compiles the complete migrated `BlockDecoCRT`, `RenderCRT`, `CRTBakedModel`, `CRTPackedLightQuad`, and `CRTModelHandler`, plus their complete production dependencies. It uses real cached Minecraft 1.12.2/Forge APIs, no replacement game classes, no owner-block facades, and no NTM collaborator stubs. It does not run Gradle, install anything, or fetch dependencies.

## Run

From the 1.12.2 module:

```sh
python3 porting/tests/crt-native/run.py \
  --module . \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --legacy-client /tmp/nagaport-legacy-client.jar
```

Optional `--original ../ntm-original-pristine` verifies a pristine checkout rather than reading the bundled source archive. The supplied JDK must have `bin/java`, `bin/javac`, and `bin/javap`. The native classpath is an external cached dependency input; there are no downloads or hardcoded dependency directories in the runner. The only fixed requirement is real 1.12.2 Minecraft/Forge-compatible dependencies.

Generate the oracle by itself:

```sh
python3 porting/tests/crt-native/build_oracle.py \
  --legacy-client /tmp/nagaport-legacy-client.jar \
  --output /tmp/OriginalCRTOracle.java
```

## Independent original evidence

`original-sha256.json` pins seven pristine Java files and six original model/texture/animation files. `pristine-crt-sources.zip` includes these byte-identical inputs. The archive preserves the original project licensing; copies of the license files are included alongside it.

`build_oracle.py` never reads native production sources. It inserts the original renderer callbacks and CRT drop/placement/subcount/structure-transform methods unchanged into a recording fixture. The two original ObjUtil shading bodies are also extracted unchanged. The fixture only records original call order, group selection, icon selection, yaw, translation, and packed light. It does not replace native Minecraft or Forge in the production process.

`CRTNativeTest` independently parses the pinned pristine OBJ into ordered source faces and compares every native quad vertex, UV, original quantized color, item normal, and material order. Controlled replacements additionally test nontriangular faces, whole-object continuous streams, incomplete tails, duplicate group last-match semantics, exact-case names, and absent Screen's original no-op behavior.

`LegacyCRTRotationTest` loads the hash-pinned cached original 1.7.10 client in an isolated classloader. It executes original Vec3 Z/Y rotation bytecode and confirms 7,776 exact double comparisons against the independent reference transform. The original client SHA-256 is `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.

## Native evidence

- Complete owner and inherited BlockMulti/BlockBase behavior, 16 actual native block states, four creative variants, drop/pick variant identity, and structure metadata transforms.
- 17,284 genuine World/Chunk placement calls with four variants and half-degree yaw samples, comparing original metadata and notification flags.
- Original whole-object inventory rotation and translation, two named world groups, and all 16 world / four item sprite/route combinations.
- Both folder and JAR packs, original textures and animation metadata, actual ModelLoaderRegistry, native blockstate JSON parsing, actual BlockModelShapes and ItemModelMesher dispatch.
- Native Forge VertexLighterFlat final BufferBuilder bytes: original environment packed light for Monitor and unlit Screen; exactly decimal 240, not 0xF000F0, for lit Screen at metadata 8–15.
- Forced SmoothAo exercises the adapter too: ordinary helper output is unchanged for Monitor, while lit Screen still receives exactly 240. CRT itself is a non-AO model; forced smooth environment lighting is not asserted to equal the original flat lighting.
- Direct non-lighter consumer delivery, source-byte immutability, subsequent Monitor light isolation, and exact parent restoration after a deliberately failing downstream consumer.
- Changed/restored/missing OBJ resource lifecycle; retained baked lists stay unchanged. Loader prefix normalization and malformed route rejection. Exact owner identity, unknown/foreign owners, replacement classes, and subclasses remain bounded.
- FIXED perspective matrix uses the ordinary ItemBlock quarter-scale helper, with defensive matrix copying. Other transforms retain the native block defaults.

## Explicit acceptance boundaries

This runner is CPU-headless native compilation and buffer-draw evidence, not a complete game build or live visual acceptance.

Two known visual gates are deliberately asserted rather than concealed: vanilla's non-Forge block renderer ignores quad `pipe`, and native damage retexturing discards the CRT light wrapper. Each loses the special packed light at eight Screen vertices per lit model. Geometry and native retexturing are verified, but lighting parity for those paths remains open.

This runner verifies inventory and FIXED matrix data but does not itself prove live GL. The separate `frame-run.py` covers bounded native GL ordinary and machine frames with its own declared fixtures and assertions. Full-game, held, GUI, and other entity visual acceptance remain open. No global lighting fix is used to make this test pass.
