# Native machine player callbacks

Bounded implementation and standalone checks, 2026-10-19. No Gradle or commit was
run by this work. The parent owns the common compile checkpoint.

## Production scope and behavior

- Rename MachineBase and six custom usability overrides to the native callback.
  MachineBase retains tile identity and centered squared distance 128; ICF 256;
  FusionTorus/LaunchPadRocket 1024; Radar/RocketAssembly identity only; Stardar
  unconditional true. Independent CrateBase retains identity and distance 64.
- Root and four subclass open/close pairs now receive EntityPlayer. Three count
  owners retain server-only increments/decrements, including negative unmatched
  closes. The root remains a no-op. Five paired containers pass invPlayer.player
  on construction and the actual supplied close player, after superclass close.
- Migrate the exact 101 machine-typed container usability calls, ContainerBase's
  IInventory dispatch, ContainerMassStorage, and 35 descendant permission calls.
- ProxyCombo forwards the same player only when inventory is enabled and the core
  is ISidedInventory. ProxyInventory and BlockPWR's separate wrapper retain their
  deliberate false/no-op paths. TileEntityPWRController itself is a MachineBase
  descendant, not an independent storage root.
- Turret calls native world.playSound with null excluded player on BOTH sides,
  canonical registered events, BLOCKS, centered coordinates and 1F volume/pitch.
  No local-client branch is added. The official 1.7.10 binary evidence explaining
  server broadcast/client no-op parity remains in the callback assessment.
- Two registered events and six byte-preserving case-only sound renames complete
  native canonical resource lookup. sounds.json variants and weights are frozen.

Storage/EMPTY/NBT/energy/recipes and unrelated inventories are not migrated.
CrateBase/MassStorage open/close and sounds remain a separate legacy edge. ProxyCombo
can reach independent cores; this does not certify those cores as fully native.

## Repeatable acceptance

Use an existing Java 8 installation and actual Forge/Minecraft dependency classpath:

```text
python porting/tests/check-machine-player-source.py
python porting/tests/machine-player-native-test.py --java-home <JDK8> --classpath <native-classpath>
python porting/tests/machine-player-sound-native-test.py --java-home <JDK8> --classpath <native-classpath>
```

Executed results:

- 147 exact whole-file callback reversals, plus drift rejection for every file;
  root + 182 descendant census; exact declaration/self-call/typed-caller mapping.
- 311 native behavioral assertions using actual Minecraft World/EntityPlayer
  subclasses, native IInventory dispatch, verbatim production callback extraction,
  all 35 permission bodies, and the complete production inventory base/registry.
- Real registry identities and native FolderResourcePack/FileResourcePack lookup
  and original hash checks for all six samples. See machine-player-sound-notes.md.
- Older naming/sided guards, reusable tick and NBT guards pass with callback-first
  reversal. Native naming (5888 assertions) and sided (1505 assertions) regressions
  also pass. Earlier full-source manifest hashes were not regenerated.

Logs: porting/machine-player-source-test.log,
porting/machine-player-native-test.log,
porting/machine-player-sound-native-test.log.

The sole earlier guard edited is check-machine-naming-source.py: it validates and
reverses the callback layer before its own frozen naming layer, which in turn is
already reversed before sided and older payload audits. The world-field reusable
verify_bridge intentionally checks receiver lines and ownership, not untouched
whole-file snapshots. Thus ModSoundEvents' addition is checked by its independent
sound full-file manifest, not by pretending the historical world-field verify()
untouched_sha256 snapshot remains current. Historical strict snapshots are not
rebaselined or claimed as current whole-repository acceptance.

Python/Java entry points use UTF-8, native executable suffixes and os.pathsep;
CrateBase and PneumoTube original CRLF bytes remain preserved. git diff --check
reports those existing line endings as trailing whitespace on touched lines; no
unrelated line-ending normalization was made. Tests ran on Linux, not Windows.

These are extracted callback, source-parity and native resource/registry checks.
They do not establish complete machine compilation, game startup, FML event-bus
registration, sound reload, audible client playback or gameplay. Forge namespace
warnings in standalone tests reflect the absent active mod container, not changed
registered IDs.
