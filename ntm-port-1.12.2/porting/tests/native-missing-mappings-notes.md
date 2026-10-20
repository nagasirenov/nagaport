# Native missing-registry mappings

The removed FMLMissingMappingsEvent owner is replaced by a complete native common-side @Mod.EventBusSubscriber with separate typed Block and Item listeners. The entire original preparation body (40,780 bytes) is retained exactly, including every ignore entry, generated gas-flame names, the ammo-prefix rule and all five item remaps. Its independent original fixture SHA256 is 75a2655949272d4d7062de565c17a0bcc071e479eda3b31dbd4b525678408c0f, extracted from checkpoint32 e8970b85's MainRegistry before editing.

Native ResourceLocation canonicalization is applied to retired IDs, including original uppercase names. The exact target object is selected anew on each event. Item remaps are never applied to the block registry. Unknown IDs, foreign namespaces and absent targets retain the registry's default action, as before. No automatic discard of unknown data, global ignore policy or manufactured fallback is added.

The genuine Forge EventSubscriptionTransformer and EventBus dispatch both generic event types against real native registry Mapping objects. The complete new production owner compiles; 9808 assertions cover all original retired IDs, the five registered target identities, prefix boundaries, unknown/foreign IDs, repeat dispatch after a target changes, and a missing target. The only NTM registry fixture supplies the five explicitly registered item identities. MainRegistry as a whole remains blocked by unrelated legacy APIs; its source edit removes only the old handler/data fields and their now-unused imports.

Reproduce:

```
python3 porting/tests/probe-native-missing-mappings.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8
```

Evidence: continuation-2026-10-19/native-missing-mappings-FINAL.log. This proves native event decisions and original mapping data, NOT loading or converting an old saved world. Full world/save acceptance remains open.
