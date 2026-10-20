# Native vanilla sound alias provenance

## Result and scope

PASS: all 15 scoped legacy aliases reproduce their official Minecraft 1.7.10 event dictionaries exactly, including category and the ordered sample list. None of these 15 names exists in the official Minecraft 1.12.2 sounds definition. Each selected native SoundEvents field resolves to the event stated in the parent manifest. The old and native definitions have the same 22 sample slots, containing 22 distinct SHA-1 asset identifiers and matching byte sizes in both official indexes.

All scoped entries are bare sample-name strings. Both serializers give them effective `type=file`, `volume=1`, `pitch=1`, `weight=1`, and `stream=false`; the included bytecode listings establish those defaults. Multiset comparison preserves duplicate multiplicity and includes every effective parameter. An additional ordered comparison establishes that the exact sample order is unchanged.

This is finite offline JSON, official index, and archived bytecode evidence, not an audio or client execution test. No OGG was downloaded, read, decoded, or played. Hash equality is between official asset-index records, not a new hash of downloaded sample payloads. No production file was modified by this evidence task. No new network request or dependency installation occurred.

## Replay

From the repository root:

```sh
python3 ntm-port-1.12.2/porting/tests/native-vanilla-sound-aliases/provenance/verify_provenance.py
python3 ntm-port-1.12.2/porting/tests/native-vanilla-sound-aliases/provenance/test_verify_provenance.py
```

Both scripts use only the Python standard library. The first script is read-only unless `--report PATH` is supplied. Paths default relative to the script, so replay is independent of the shell working directory. It validates the parent `manifest.json` and production `src/main/resources/assets/minecraft/sounds.json`. `--manifest`, `--sounds`, and `--evidence` support isolated replay fixtures. The second script makes temporary copies within this evidence directory, runs 14 tests, and cleans up those copies.

`report.json` is the full successful validator receipt, including every sample URL, SHA-1, and byte size. `test-output.txt` records the successful positive, negative, and multiset tests. Negative cases reject category/volume/sample mutations, missing/extra aliases, duplicate JSON members, wrong native/category mappings, changed index bytes even if their local receipt is altered, the extra native-version newline, altered source URLs, and changed native registry evidence.

## Official version and resource chain

The legacy metadata version is 1.7.10. The native metadata version is 1.12.2, which explicitly selects the asset index named **1.12**. Each version's index URL, SHA-1, and byte size are checked; each index's `minecraft/sounds.json` SHA-1 and size are checked; every fetched file's SHA-1 is checked against its content-addressed URL and its download receipt. SHA-256 values are supplied for independent local integrity checking.

`downloads.json` is the unchanged receipt for the five previously fetched files. `version-manifest.json` is an existing local launcher manifest snapshot retaining both official version links, not a newly fetched resource. Its local SHA-1 is pinned to `20c7d2dd973dcc13fa7c98548214b113eb83a88a` and SHA-256 to `39c54c6cf6e65a06a813655189866d583979772fe087225f311ec78a2c235df6`.

### Native version recovery

The existing 1.12.2 metadata cache had one additional final LF byte. Its cached SHA-1 was `e9614ac2eb9c18c27985e6135841a1353aca5683` (18,417 bytes). Removing **exactly that one LF**, without parsing or reserializing JSON, yields 18,416 bytes with SHA-1 `832d95b9f40699d4961394dcf6cf549e65f15dc5`, exactly the official version URL hash. Those recovered bytes are retained as `native-version.json`. `local-evidence.json` records both hashes, the source path, and the precise operation; replay verifies that appending one LF reproduces the recorded cached hash. This is recovered, content-authenticated metadata, not a claim that a fresh native version download was performed.

### Resource hashes

- `legacy-version.json`: 14,777 bytes
  - URL: https://piston-meta.mojang.com/v1/packages/ed5d8789ed29872ea2ef1c348302b0c55e3f3468/1.7.10.json
  - SHA-1: `ed5d8789ed29872ea2ef1c348302b0c55e3f3468`
  - SHA-256: `265c7e47aad4c74f6085a49667d3a828f48d63f484def2ae70b5e426e5b7cba6`
- `native-version.json`: 18,416 bytes
  - URL: https://piston-meta.mojang.com/v1/packages/832d95b9f40699d4961394dcf6cf549e65f15dc5/1.12.2.json
  - SHA-1: `832d95b9f40699d4961394dcf6cf549e65f15dc5`
  - SHA-256: `56dac2e1caa872d7e37a7b5523056cd812dca94f912ad2bd652817bee60edca4`
- `legacy-index.json`: 72,996 bytes
  - URL: https://launchermeta.mojang.com/v1/packages/1863782e33ce7b584fc45b037325a1964e095d3e/1.7.10.json
  - SHA-1: `1863782e33ce7b584fc45b037325a1964e095d3e`
  - SHA-256: `48d191b37f391edfcdc77c51f2dce7ee4a9a97c48c0069af5b4f93a25c698f4f`
- `native-index.json`: 143,136 bytes
  - URL: https://piston-meta.mojang.com/v1/packages/a21e1ded1a24ea1548dd8db0cf30b6acb02655a9/1.12.json
  - SHA-1: `a21e1ded1a24ea1548dd8db0cf30b6acb02655a9`
  - SHA-256: `7a87ae9278d129fc60285547332b8c17c1ae7f72b9c8017cb0a6898df743eb2f`
- `legacy-sounds.json`: 34,020 bytes
  - URL: https://resources.download.minecraft.net/fc/fce0bb3e14ade7b292a7c1d5fa785533d8106d82
  - SHA-1: `fce0bb3e14ade7b292a7c1d5fa785533d8106d82`
  - SHA-256: `14ef20dbd81eeb5eec0ec002f5d6fcb047e7f53203b20c79ba33836faa0f6f44`
- `native-sounds.json`: 105,658 bytes
  - URL: https://resources.download.minecraft.net/97/975a99ed9870f51bbae348533d775d730e3fee18
  - SHA-1: `975a99ed9870f51bbae348533d775d730e3fee18`
  - SHA-256: `b33ac54ddcbe81c8f627e8e7eb642f269e0b54686849a2acb4048812eb06711e`

## Alias and category mapping

The last column is the manifest/transport SoundCategory enum selected to preserve the original category. It is not a category read from native sounds.json: 1.12.2 event definitions no longer carry that category field. Correct runtime transport of the enum is covered by the parent task, not by this JSON provenance check. Native subtitle additions are deliberately not copied into the exact original aliases and are listed in `report.json`.

| Original name | Native SoundEvents field | Native event | Samples | Original category → transport enum |
|---|---|---|---:|---|
| `ambient.weather.thunder` | `ENTITY_LIGHTNING_THUNDER` | `entity.lightning.thunder` | 3 | `weather` → `WEATHER` |
| `random.explode` | `ENTITY_GENERIC_EXPLODE` | `entity.generic.explode` | 4 | `block` → `BLOCKS` |
| `random.fizz` | `BLOCK_FIRE_EXTINGUISH` | `block.fire.extinguish` | 1 | `block` → `BLOCKS` |
| `random.click` | `BLOCK_LEVER_CLICK` | `block.lever.click` | 1 | `block` → `BLOCKS` |
| `fire.fire` | `BLOCK_FIRE_AMBIENT` | `block.fire.ambient` | 1 | `block` → `BLOCKS` |
| `random.break` | `ENTITY_ITEM_BREAK` | `entity.item.break` | 1 | `player` → `PLAYERS` |
| `mob.zombie.woodbreak` | `ENTITY_ZOMBIE_BREAK_DOOR_WOOD` | `entity.zombie.break_door_wood` | 1 | `hostile` → `HOSTILE` |
| `mob.endermen.portal` | `ENTITY_ENDERMEN_TELEPORT` | `entity.endermen.teleport` | 2 | `hostile` → `HOSTILE` |
| `fireworks.blast` | `ENTITY_FIREWORK_BLAST` | `entity.firework.blast` | 1 | `ambient` → `AMBIENT` |
| `fire.ignite` | `ITEM_FLINTANDSTEEL_USE` | `item.flintandsteel.use` | 1 | `block` → `BLOCKS` |
| `tile.piston.out` | `BLOCK_PISTON_EXTEND` | `block.piston.extend` | 1 | `block` → `BLOCKS` |
| `tile.piston.in` | `BLOCK_PISTON_CONTRACT` | `block.piston.contract` | 1 | `block` → `BLOCKS` |
| `minecart.base` | `ENTITY_MINECART_RIDING` | `entity.minecart.riding` | 1 | `neutral` → `NEUTRAL` |
| `game.neutral.swim.splash` | `ENTITY_GENERIC_SPLASH` | `entity.generic.splash` | 2 | `neutral` → `NEUTRAL` |
| `random.orb` | `ENTITY_EXPERIENCE_ORB_PICKUP` | `entity.experience_orb.pickup` | 1 | `player` → `PLAYERS` |

## Every sample asset

For each row below the original index and native index give exactly the same asset key, byte size, and SHA-1. The URL is the official content-addressed asset location, included for traceability only. No sample URL was fetched by this task.

### ambient.weather.thunder → entity.lightning.thunder (3 samples)

- `minecraft/sounds/ambient/weather/thunder1.ogg`: 24,468 bytes
  - SHA-1: `671ef0ae7b59014a2f637d7af6d781324ab6309f`
  - URL: https://resources.download.minecraft.net/67/671ef0ae7b59014a2f637d7af6d781324ab6309f
- `minecraft/sounds/ambient/weather/thunder2.ogg`: 25,571 bytes
  - SHA-1: `a20cbf2ddce541eaed520d7ec55665fe97428be6`
  - URL: https://resources.download.minecraft.net/a2/a20cbf2ddce541eaed520d7ec55665fe97428be6
- `minecraft/sounds/ambient/weather/thunder3.ogg`: 27,653 bytes
  - SHA-1: `9ead5c928f9122e3c2f5fc4f6ce7fb4054a2b578`
  - URL: https://resources.download.minecraft.net/9e/9ead5c928f9122e3c2f5fc4f6ce7fb4054a2b578

### random.explode → entity.generic.explode (4 samples)

- `minecraft/sounds/random/explode1.ogg`: 19,732 bytes
  - SHA-1: `cd46e41023887558b134547e28327de6e7df189a`
  - URL: https://resources.download.minecraft.net/cd/cd46e41023887558b134547e28327de6e7df189a
- `minecraft/sounds/random/explode2.ogg`: 25,246 bytes
  - SHA-1: `a116e396d95a0ee245000dd4cdcc333d38ea9e3b`
  - URL: https://resources.download.minecraft.net/a1/a116e396d95a0ee245000dd4cdcc333d38ea9e3b
- `minecraft/sounds/random/explode3.ogg`: 25,298 bytes
  - SHA-1: `a94a69e56568f5789cab05382cfd81f601189fd1`
  - URL: https://resources.download.minecraft.net/a9/a94a69e56568f5789cab05382cfd81f601189fd1
- `minecraft/sounds/random/explode4.ogg`: 25,090 bytes
  - SHA-1: `f259be40364341edcaf88e339bc24ab01e49845a`
  - URL: https://resources.download.minecraft.net/f2/f259be40364341edcaf88e339bc24ab01e49845a

### random.fizz → block.fire.extinguish (1 sample)

- `minecraft/sounds/random/fizz.ogg`: 17,002 bytes
  - SHA-1: `c649e60ea9a99c97501a50d2dc4e579343e91ea8`
  - URL: https://resources.download.minecraft.net/c6/c649e60ea9a99c97501a50d2dc4e579343e91ea8

### random.click → block.lever.click (1 sample)

- `minecraft/sounds/random/click.ogg`: 7,046 bytes
  - SHA-1: `2fe092579d9637e2d160319820ee08e60a237bb7`
  - URL: https://resources.download.minecraft.net/2f/2fe092579d9637e2d160319820ee08e60a237bb7

### fire.fire → block.fire.ambient (1 sample)

- `minecraft/sounds/fire/fire.ogg`: 28,689 bytes
  - SHA-1: `8b260108a73470c16cd244325242d4780cfb7d78`
  - URL: https://resources.download.minecraft.net/8b/8b260108a73470c16cd244325242d4780cfb7d78

### random.break → entity.item.break (1 sample)

- `minecraft/sounds/random/break.ogg`: 6,551 bytes
  - SHA-1: `8bf3ecb0fe9a6a615f5995e3dd21b6c39adc21d5`
  - URL: https://resources.download.minecraft.net/8b/8bf3ecb0fe9a6a615f5995e3dd21b6c39adc21d5

### mob.zombie.woodbreak → entity.zombie.break_door_wood (1 sample)

- `minecraft/sounds/mob/zombie/woodbreak.ogg`: 10,929 bytes
  - SHA-1: `4e94ed53b6059d150c3bb2322b4af9ca9514bae1`
  - URL: https://resources.download.minecraft.net/4e/4e94ed53b6059d150c3bb2322b4af9ca9514bae1

### mob.endermen.portal → entity.endermen.teleport (2 samples)

- `minecraft/sounds/mob/endermen/portal.ogg`: 10,010 bytes
  - SHA-1: `7b4b5323ef066caa1ae43cbe66fffd9dfce4ed32`
  - URL: https://resources.download.minecraft.net/7b/7b4b5323ef066caa1ae43cbe66fffd9dfce4ed32
- `minecraft/sounds/mob/endermen/portal2.ogg`: 7,442 bytes
  - SHA-1: `35461b6a4253db40973549e82d91f267c686be85`
  - URL: https://resources.download.minecraft.net/35/35461b6a4253db40973549e82d91f267c686be85

### fireworks.blast → entity.firework.blast (1 sample)

- `minecraft/sounds/fireworks/blast1.ogg`: 11,071 bytes
  - SHA-1: `d9d7511e8944da0c7ab4ed2d3e3d964f98dd6997`
  - URL: https://resources.download.minecraft.net/d9/d9d7511e8944da0c7ab4ed2d3e3d964f98dd6997

### fire.ignite → item.flintandsteel.use (1 sample)

- `minecraft/sounds/fire/ignite.ogg`: 6,418 bytes
  - SHA-1: `a0d1fbff7e938e92c10c1576339dda668f40e111`
  - URL: https://resources.download.minecraft.net/a0/a0d1fbff7e938e92c10c1576339dda668f40e111

### tile.piston.out → block.piston.extend (1 sample)

- `minecraft/sounds/tile/piston/out.ogg`: 6,059 bytes
  - SHA-1: `cb017131516de9359cb81fb991f5b3694044f8db`
  - URL: https://resources.download.minecraft.net/cb/cb017131516de9359cb81fb991f5b3694044f8db

### tile.piston.in → block.piston.contract (1 sample)

- `minecraft/sounds/tile/piston/in.ogg`: 5,900 bytes
  - SHA-1: `594be3eaea9a2b92095eeeb3b1e01e755dcb8ce2`
  - URL: https://resources.download.minecraft.net/59/594be3eaea9a2b92095eeeb3b1e01e755dcb8ce2

### minecart.base → entity.minecart.riding (1 sample)

- `minecraft/sounds/minecart/base.ogg`: 15,354 bytes
  - SHA-1: `28f9973fcd8366ac9837dbe190b129a8fce15857`
  - URL: https://resources.download.minecraft.net/28/28f9973fcd8366ac9837dbe190b129a8fce15857

### game.neutral.swim.splash → entity.generic.splash (2 samples)

- `minecraft/sounds/liquid/splash.ogg`: 16,702 bytes
  - SHA-1: `4c14c40542e249c902d0cac710ab4c6d6debba28`
  - URL: https://resources.download.minecraft.net/4c/4c14c40542e249c902d0cac710ab4c6d6debba28
- `minecraft/sounds/liquid/splash2.ogg`: 36,747 bytes
  - SHA-1: `857abbbfb58186c2f1b5510a4072630950e518f6`
  - URL: https://resources.download.minecraft.net/85/857abbbfb58186c2f1b5510a4072630950e518f6

### random.orb → entity.experience_orb.pickup (1 sample)

- `minecraft/sounds/random/orb.ogg`: 7,139 bytes
  - SHA-1: `e9833a1512b57bcf88ac4fdcc8df4e5a7e9d701d`
  - URL: https://resources.download.minecraft.net/e9/e9833a1512b57bcf88ac4fdcc8df4e5a7e9d701d

## Registry/default bytecode support

The bytecode listings were generated read-only with the already-installed Java 8 `javap -p -c`:

- `native-SoundEvents.javap.txt`: the actual build classpath maps all 15 selected fields through `getRegisteredSoundEvent` to their documented native event names.
- `native-SoundListSerializer.javap.txt`: bare strings construct Sound with two `fconst_1` operands, `iconst_1` weight, FILE type, and `iconst_0` stream.
- `legacy-SoundListSerializer.javap.txt`: bare strings allocate the original sound entry and set its name.
- `legacy-SoundEntry.javap.txt`: the original constructor initializes volume/pitch to 1, weight to 1, file type, and stream to false.
- `legacy-SoundEntryType.javap.txt`: the original default enum field is FILE.

`local-evidence.json` records each exact command, class name, source JAR path and SHA-1/SHA-256, class-byte SHA-1/SHA-256, and archived listing hash. The original client JAR SHA-1 equals the official 1.7.10 version download entry (`e80d9b3bf5085002218d4be59e668bac718abbc6`). Native listings come from the existing deobfuscated Forge 1.12.2 build classpath and are not misrepresented as byte-identical to the official client JAR. The replay script pins and checks the archived listings; it does not need either original JAR or Java to replay and does not rerun the disassembler.

Audio behavior beyond these exact definitions/defaults, resource-pack overrides, native subtitle availability, listener dispatch, packet delivery, mixer/category handling, sample decoding, and live playback remain separate runtime concerns. This evidence does not claim those were executed.
