# Native Forge event dispatch proof

## Result and scope

The actual cached Forge 1.12.2 event bus passes 10 assertions with the genuine
`EventSubscriptionTransformer` registered in the genuine `LaunchClassLoader`.
There are no platform stubs, patched dependency JARs, manually edited bytecode,
Unsafe allocations, or direct subscriber invocations. No download or install is
needed. Native event-registration warnings are absent from the successful run.

The test explicitly proves:

- Harness, Forge `Event`, and transformer share the native launch classloader.
- Forge injects the no-argument constructors required by `EventBus.register`
  into actual `AttachCapabilitiesEvent` and `PlayerEvent.Clone` classes.
- Those two event types have distinct real listener lists.
- `EventBus.post` dispatches an `AttachCapabilitiesEvent<Entity>` only to its
  matching generic subscriber, not to the `ItemStack` generic subscriber.
- The converse `ItemStack` event dispatch works without reaching the entity handler.
- `PlayerEvent.Clone` reaches its own subscriber and a parent `PlayerEvent` subscriber.
- Native `EventBus.unregister` prevents subsequent delivery.

Both subscribers and posted events are native-framework objects loaded in the
same isolated loader. Entity payloads are deliberately null: this is event-bus
infrastructure proof, not entity construction, provider persistence, clone data
copying, world lifecycle, automatic mod subscriber discovery, a full NTM compile,
or gameplay proof. Entity-capability tests can use the launcher separately with
real entity instances. The test uses Forge's real `Loader.setupTestHarness` to
supply listener ownership; it does not run full Forge mod discovery.

## Reproduce without temporary-file dependencies

Use the existing Java 8/ForgeGradle toolchain and populated local dependency
cache. In the project root:

```sh
JAVA_HOME=/workspace/shared/nagaport-toolchain/java8 \
GRADLE_USER_HOME=/workspace/shared/nagaport-toolchain/gradle-user-home \
/workspace/shared/nagaport-toolchain/gradle-4.10.3/bin/gradle \
  --offline --no-daemon -I porting/tests/forge-event-classpath.init.gradle \
  writeForgeEventNativeClasspath

python3 porting/tests/probe-forge-event-dispatch.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file build/porting-tests/forge-event-classpath.txt \
  --report build/porting-tests/forge-event-result.json
```

Replace the toolchain paths on other machines. Windows `.exe` Java tools and
native classpath separators are supported. `--classpath` can be supplied instead
of `--classpath-file`. The read-only Gradle helper exports the existing compile
classpath without compiling NTM or changing production source sets. It does not
install, remap, or substitute any dependencies. The probe itself compiles just
the two checked-in Java test files and runs from a temporary working directory,
so native logs do not pollute the checkout.

The exact recorded classpath, expanded Java/Javac argument arrays, Java version,
JAR hash, and runtime output are in `forge-event-dispatch-native-result.json`.
Its temporary output directory is historical run evidence only, not a dependency.
The repeatable command form is:

```sh
"$JAVA_HOME/bin/javac" -source 8 -target 8 -proc:none -implicit:none \
  -encoding UTF-8 -cp "$CP" -d "$CLASSES" \
  porting/tests/ForgeEventNativeLauncher.java porting/tests/ForgeEventNativeTest.java
"$JAVA_HOME/bin/java" -cp "$CLASSES:$CP" \
  ForgeEventNativeLauncher ForgeEventNativeTest
```

Recorded native JAR:
`.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`

SHA-256: `8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e`

Java: Temurin OpenJDK `1.8.0_504-b01`; Forge `14.23.5.2768`;
LaunchWrapper `1.12`; ASM `5.2`. The dependency JAR remains unchanged.

## Reusing the launcher

Compile `ForgeEventNativeLauncher.java` alongside another native test and invoke
`ForgeEventNativeLauncher YourNativeTestMain [test arguments]`. The outer launcher
must not initialize Forge or Minecraft. The invoked test initializes them inside
the isolated classloader, before registering subscribers:

```java
net.minecraft.init.Bootstrap.register();
FMLCommonHandler.instance().beginLoading(FMLServerHandler.instance());
Loader.instance().setupTestHarness(new DummyModContainer(metadata));
```

The launcher installs only the real event-subscription transformer. It does not
install every transformer from a complete Forge launch, nor optional API
transformations needed by unrelated mods. The transformer class is excluded from
its own transformation, not delegated to the parent loader: its implementation
resolves event superclasses through its own classloader, so parent-loader
instantiation or an unordered offline transform pass can miss required parent
transformations. Ordinary platform event classes and their parent event classes
are transformed as the native loader resolves them, without a modified JAR.
