# Forge 1.12.2 scaffold

This build scaffold is based only on original NTM: Space commit
`cf6a396d6c3858880c5da39982cce5ebba9582c9`. It is a project-configuration
milestone, not a source port: Java sources, binary assets, and GPL notices have
not been changed. The only source resource adjusted is the legacy access
transformer configuration, whose original content is archived below.

## Toolchain

| Component | Pinned choice |
| --- | --- |
| Minecraft | 1.12.2 |
| Minecraft Forge | 14.23.5.2768 |
| ForgeGradle | `2.3-SNAPSHOT` from `https://maven.minecraftforge.net/` (resolved banner: `2.3.4-gfc67182`) |
| Gradle wrapper | 4.10.3 |
| MCP mappings | `stable_39` |
| Java | Temurin OpenJDK 8u504 (source and target compatibility 1.8) |

Forge 14.23.5.2768 is a stable Forge build selected for compatibility with the
legacy ForgeGradle 2.3 userdev format. The newer stable 14.23.5.2860 was tried
first, but its legacy `-userdev.jar` artifact returns 404 from the Forge Maven
repository; Gradle 4.10.3 and ForgeGradle 2.3 cannot set up that version. The
wrapper distribution is pinned, while ForgeGradle itself is published as a
snapshot by the Forge Maven repository.

The complete original 1.7.10 access-transformer file is preserved at
`porting/legacy-HBM_at.cfg`. The resource path
`src/main/resources/META-INF/HBM_at.cfg` remains as a comment-only placeholder:
ForgeGradle automatically applies this file, and the original obsolete
mappings make the 1.12.2 remapper fail. Its entries must be reviewed and
remapped as part of the later source port. The scaffold JAR does not register
an `FMLAT` manifest attribute yet.

## Compile-time integrations

The original pinned 1.7.10 integration coordinates were removed because they
cannot serve as dependencies for a 1.12.2 workspace. The scaffold declares the
1.12.2 NotEnoughItems API (`codechicken:NotEnoughItems:1.12.2-2.4.3.245`,
ModMaven) and OpenComputers API (`li.cil.oc:OpenComputers:MC1.12.2-1.7.5.192`,
OpenComputers Maven) as deobfuscated compile dependencies. No source from
Community or Extended variants was used.

## Initial compile diagnostic

Command run from the repository root:

```powershell
java -version  # Verify Java 8 is selected.
.\gradlew.bat compileJava --no-daemon --console=plain
```

Console output from the initial 14.23.5.2860 attempt is captured in
`porting/compile-diagnostics.log`; the compile after switching to 14.23.5.2768
is captured in `porting/compile-diagnostics-2768.log`. The intermediate 2768
failure on the unported access transformer is retained as
`porting/compile-diagnostics-2768-at.log`. The final compile reached javac and
failed at the compiler's 100-error cap. Representative categories are missing
1.7.10 FML packages (`cpw.mods.fml.*`), removed vanilla types (`IIcon`,
`EnumChatFormatting`, `MovingObjectPosition`), and removed Forge types such as
`ForgeDirection`. ForgeGradle and the declared NEI/OpenComputers compile
dependencies resolved far enough for javac to run; the source remains
unported. A client launch is not possible until the source and AT mappings are
migrated.
