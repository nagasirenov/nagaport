# Native entity registration and spawn-egg proof

Run with JDK 8 and the native 1.12.2 Forge 14.23.5.2768 compile classpath:

    python3 porting/tests/entity-registry-native/probe.py \
      --module /path/to/ntm-port-1.12.2 \
      --original /path/to/ntm-original-pristine \
      --java-home /path/to/jdk8 \
      --classpath "$NATIVE_CLASSPATH" \
      --output /path/to/new-proof-output

All parameters except --output are explicit. With no --output, a new temporary directory is used. Classes/logs never go into the module. No prebuilt falling output or staging owner is needed. The script compiles these five complete production sources directly from --module against native Forge/Minecraft:

- com/hbm/entity/ModEntityList.java
- com/hbm/lib/RefStrings.java
- com/hbm/blocks/BlockFallingNT.java
- com/hbm/entity/item/EntityFallingBlockNT.java
- com/hbm/blocks/ISpotlight.java

Two harness sources: this folder's NativeEntityRegistryTest.java and module porting/tests/ForgeEventNativeLauncher.java. Empty sourcepath prevents implicit compilation. No substitute production owners, Minecraft/Forge classes, registry maps or unrelated collaborator fixtures. Test-only entity subclasses exercise egg/overload branches, while the complete real EntityFallingBlockNT proves native registration/creation.

The runner pins pristine ModEntityList, EntityMappings and English localization evidence. All 212 mapping calls are compared in actual registration order (ordinary entities then mobs), including exact option arguments. The checked manifest proves unique native classes/keys, preserving local numeric IDs and raw legacy names. Native display translation names retain hbm.<legacyName>, matching original entity.hbm.<legacyName>.name keys. Only two pre-existing collisions are separated: EntityMinecartDestroyer ID143 gets hbm:entity_ntm_cart_destroyer (Crate ID142 keeps its key), and EntityCloudSolinium ID62 gets hbm:entity_cloud_solinium (FleijaRainbow ID58 keeps its key). Both pairs retain raw/display names and options.

PASS 38 native assertions: no native falling identity before registration; genuine Forge EntityRegistry/EntityList identity after registration; actual falling factory instance; original falling ID158/range1000/update1/velocitytrue; original wrapper creation gate; all four registration overloads; color1 sentinel versus hasEgg distinction; default/custom tracking; native egg colors and ItemMonsterPlacer EntityTag.id; deep-copy isolation; ItemStack NBT roundtrip; true native EntityEntry factory routing; duplicate ID/class/key rejection without mutation; safe unknown IDs; no-egg/null returns; public late egg helper without changing wrapper creation gate.

The test uses genuine Forge Loader.setupTestHarness/DummyModContainer and EventSubscriptionTransformer, not live mod discovery/startup. It does not compile all 212 mapped entities, prove runtime factories for the four unported collision-pair classes, launch a game, or promise 1.7-world import. Complete collision freedom is source-manifest evidence, and the actual falling owner is representative native runtime integration. No native world spawn from ItemMonsterPlacer is claimed.

A successful run writes evidence-input-sha256.json for every source actually compiled. The included portable-proof.log and evidence-input-sha256.json record the verified integrated-module run. deliverable-sha256.json pins this finite portable bundle.
