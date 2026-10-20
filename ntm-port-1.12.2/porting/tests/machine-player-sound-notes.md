# Machine player callback sound closure

Implemented the two turret sound identities with `ModSoundEvents.BLOCK_OPENC` and
`ModSoundEvents.BLOCK_CLOSEC`, registered as `hbm:block.openc` and `hbm:block.closec`.
The original SLICER initializer and registration are unchanged. The existing
subscriber annotation remains. Six OGG stored filenames were lowercased without
changing their bytes. `sounds.json` is unchanged, including all selected variants.

## Repeatable tests

Run the standalone Python runner with an existing Java 8 toolchain and native
Minecraft/Forge 1.12.2 dependency classpath (no Gradle needed):

```text
python porting/tests/machine-player-sound-native-test.py --java-home <JDK8> --classpath <native-classpath>
python porting/tests/machine-player-sound-native-test.py --source-only
```

The runner uses `.exe` on Windows, `os.pathsep`, explicit UTF-8 text I/O and javac
encoding. Actual stored directory-entry names, not `exists()` under an obsolete
spelling, establish exact filename case. ZIP entries preserve exact names across
host platforms. Executed on the Linux host on 2026-10-19; not executed on Windows.

Passing coverage:

- Compile the complete production ModSoundEvents and RefStrings against actual
  Minecraft/Forge classes, without fabricated Minecraft API classes.
- Initialize native Bootstrap, invoke the production registry callback against
  the actual Forge sound-event registry, and establish a size increase of exactly
  three and exact object identity/name for SLICER and both turret events.
- Read the real unchanged sounds.json through FolderResourcePack. Native
  ResourceLocation normalization matches its historical mixed-case event keys
  to canonical registered IDs; preserve the block category and three variants
  per event.
- Resolve all six canonical sample paths with actual FolderResourcePack against
  repository resources and FileResourcePack against an archive of actual stored
  filenames, comparing both to frozen original SHA-256 bytes.
- Reconstruct historical ZIP names with those same original bytes and establish
  that all six canonical native ZIP resource lookups fail under the old names.
- Reverse only two explicit registry additions and compare the entire reversed
  file to its pre-edit SHA-256; independently compare its complete migrated hash.
  Freeze the complete sounds.json hash and six original sample hashes. No Git
  baseline or working-tree state is needed by this audit.

The standalone Forge process emits expected namespace warnings because there is
no active HBM mod container. The verified names remain hbm names. This is not an
FML event-bus launch, sound reload, audible playback, full-mod compilation, or
in-game acceptance. Server broadcast/client no-op parity remains supported by the
historical binary evidence in `porting/machine-player-callback-assessment.md`,
not by the resource/registry test alone.
