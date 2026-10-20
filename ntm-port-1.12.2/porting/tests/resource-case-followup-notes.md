# Bounded resource-case follow-up, checkpoint 9

Exactly 32 assets are renamed to the native ResourceLocation canonical lowercase path: 21 texture PNGs, ten ruins NBT files, and one shader-noise PNG. Contents are unchanged. `resource-case-followup-manifest.json` freezes their original SHA-256 and exact before/after paths, alongside the 36 verified full-path Java references. Checkpoint 8's independent eleven-rename manifest is unchanged; its audit now inspects exact stored entry names for case-insensitive filesystem portability.

The audit scanned 4,735 UTF-8 source/resource files, including JSON, model, material, shader, language and disk text. All 36 full-path occurrences are in Java and either construct ResourceLocation or return tank_NONE.png to immediately wrapped ResourceLocation callers. ResourceManager field consumers bind or return ResourceLocation, rather than opening unnormalized paths. No case-fold collision exists among the 8,645 assets; none of these canonical destinations existed before the batch. The mixed-case count decreases from 801 to 769. This number is bookkeeping, not acceptance of remaining assets.

## Raw-loader exclusions

NBTStructure:94 builds its classloader path from normalized ResourceLocation namespace/path, so its raw classloader reader also needs lowercase ruins filenames. Its File constructor and structure editor/save paths read external structures, not the packaged files. QMAWLoader's raw ZIP/filesystem readers are limited to assets/hbm/manual JSON. Other raw filesystem readers serve configuration, saves, screenshots or HTTP, not these candidate resources. `resource-case-followup-audit.json` records this bounded audit.

Do not lowercase unrelated symbols: GLSL iChannel1 uniforms, MrKimkimora OBJ/renderPart names, NTMRuinsA-J jigsaw IDs, bobble attribution strings, or NMine.mtl/Prototype.mtl OBJ material declarations. Those are not candidate filenames. The shader PNG flows through TextureManager binding; the shader uniform spelling is independent.

## Native test and acceptance boundary

Run:

    python3 porting/tests/resource-case-followup-probe.py --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8
    python3 porting/tests/check-resource-case.py

The saved classpath is specific to the verified workspace. On another machine, use Java8 and resolve its real project classpath with `gradle --offline -I porting/print-compile-classpath.init.gradle printPortCompileClasspath --console=plain`, then pass the value printed after `COMPILE_CLASSPATH=` to `--classpath`; select that machine's Java8 with `--java-home`. This does not compile production.

The follow-up probe compiles only its standalone test fixture against the existing actual pinned target classpath, not production Java, and invokes no Gradle task. It checks original-byte hashes, old-path absence, canonical path presence, unique destinations, asset count and collisions. Actual target ResourceLocation must normalize the unchanged production literals to the expected paths. Actual target FolderResourcePack reads all 32 from the REAL repository resource directory, not copies or a case-insensitive search. ImageIO decodes all 22 PNGs. An isolated Java classloader resolves all ten normalized packaged structure paths and actual target CompressedStreamTools decodes their NBT. The original read-only audit also demonstrated that all 32 canonical native lookups failed before renaming and byte-identical staged lowercase copies succeeded.

These checks do not compile or instantiate production NBTStructure, upload textures, compile shaders, run resource-pack reloads, generate structures in a world, or establish rendered-game acceptance. Production legacy accessor/API porting remains separate. Checkpoint 8's eleven-rename test also passes with its exact-entry audit portability fix.

## Windows requirement and native ZIP/JAR regression

The user runs Windows. The audit no longer uses `Path.exists()` to reject old mixed-case names, because that aliases the canonical file on case-insensitive filesystems. Both checkpoint 8 and follow-up scripts enumerate the actual stored directory-entry names and compare their strings exactly. The Java subprocess classpath uses the host's `os.pathsep`. These changes were executed on Linux only; no Windows execution is claimed.

The native fixture additionally tests all 43 checkpoint 8 + 9 resources through the actual target `FileResourcePack`, not Python ZIP lookup. Python constructs temporary JAR/ZIP archives from the hash-verified real repository bytes. Canonical archive entry names preserve actual stored directory entries; a separate historical-case archive reconstructs the original names recorded in the independent manifests. For every mixed-case production constructor literal, native ResourceLocation normalizes it; native `FileResourcePack.resourceExists` rejects the historical-case archive and `getInputStream` throws. The canonical archive resolves and returns the original SHA-256 bytes. All ten ruins also fail normalized classloader lookup in the historical-case JAR and resolve/hash/decode through the canonical JAR and actual native NBT reader.

Final result: 32 real repository native folder lookups, 22 PNG decodes, ten directory classloader/native NBT decodes, 43 native archive failure/success regressions, and ten JAR classloader/native NBT decodes pass. The final superset log is `porting/continuation-2026-10-19/resource-case-followup-checks.log`, generated using the saved actual project compile classpath. ZIP entry-name behavior explains why this packaging fix remains relevant even when a Windows loose directory accepts case aliases. This is not a Windows launch or full mod-build/packaging acceptance test, and does not add asset renames.

## Explicitly excluded follow-up groups

No additional assets were renamed. The earlier literal inventory was incomplete: GUIBombMulti's one-argument concatenated ResourceLocation for textures/gui/weapon/bombGeneric.png is outside this batch. RenderFluidTank computes additional tank_<fluid>.png names; other computed textures/atlas names require producer/caller enumeration against native TextureMap/TextureManager resolution. Sounds need sounds.json and native sound loader path semantics checked. Languages need native language discovery/locale conventions checked. Disks need their filesystem/consumer semantics checked. Remaining models and arbitrary external/runtime-built names are not certified. Do not blanket-rename the remaining 769 mixed-case files.
