# Native dispenser registry field

CP73 baseline a4739afa41966b75e2012bccd7df919e51d5c3a0. Exactly 49 identifier replacements across DispenserBehaviorHandler and ItemModMinecart; registration keys, values, evaluation order, callback bodies and captured bucket behavior remain byte-identical. Javac attributes the explicit qualifier to native BlockDispenser, not an unresolved member symbol. Source reversal, shadow/value/string controls and actual positive/reversed native compilation verify scope.

Original akm/a and native DISPENSE_BEHAVIOR_REGISTRY share SRG field_149943_a. Actual original/native global registries execute missing-key defaults, put/get identity, replacement and retained captured delegates; fresh test keys do not replace vanilla behaviors. Ten runtime checks include invocation of stored test delegates. This proves registry access, not NTM callbacks, entity construction, item initialization timing, dispenser firing, Forge event integration or a runnable mod. Remaining callback and ItemStack migrations are not hidden or excluded from full compilation.

Offline reproduction from any directory (Java 8, existing native classpath, authentic original client and mapping artifacts):

    python3 run.py JAVA_HOME CLASSPATH_FILE OLD_CLIENT_JAR SRG_ZIP FIELDS_CSV

The temporary unsigned original-client copy removes only META-INF signatures; class bytes are unchanged and the source JAR is untouched. Runtime probe uses genuine native Bootstrap registration in its isolated process. No binaries or new dependencies are distributed.
