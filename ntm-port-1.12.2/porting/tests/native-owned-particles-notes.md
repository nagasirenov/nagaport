# Native owned particle family (checkpoint 24)

Baseline: checkpoint 23, 1d8a9f74f3f0191269bc39d350e892c46304562c. Ten complete production classes now use native Particle/ParticleSmokeNormal: GasFlame, Debug, DebugLine, Text, Foundry, RadiationFog, Problem, Haze, RBMKMush and Contrail. ParticleVertexFormats supplies the native 32-byte position/UV/color/lightmap/normal layout for owned draws. Debug lines use native 28-byte particle format with explicit unused UV0 because the native writer cannot represent UV1 without UV0. No legacy platform classes or compile exclusions were introduced.

## Evidence and boundaries

- Full selected production classes compile against real Forge/Minecraft, not rewritten platform stubs. 105 native lifecycle/buffer assertions pass. The controlled headless World fixtures use Unsafe allocation; they are not a running Minecraft world.
- Eight custom update bodies match the exact checkpoint-23 source after the documented expiry/height/particle-enum API mappings. All 60 ordered quad position/UV expression tuples match. Source audit runs without Git or the original checkout. Ten byte-exact inverse/forward boundaries and one retained new native file reject 33 drift mutations; prior manifests are unchanged.
- Tests exercise native smoke lifetime/color/motion, text/debug movement, stationary effects and original inclusive/exclusive expiry boundaries, contrail alpha, Haze lava emission including its expiry tick, line vertex coordinates/colors/lightmap and native custom vertex layout.
- Raw texture resources exist, including correcting the original Problem reference from absent textures/entity/theproblem.png to existing textures/particle/theproblem.png. No new texture asset or particle gameplay mechanic.
- The user approved simpler particle visuals and native manager scheduling/caps/initial placement. Smoke uses the native superclass. Contrail uses identityHashCode for its purely visual seed, not the old shared Entity ID counter. Cameras now use the supplied camera with local-player fallback. These are visual adaptations, not exact old superclass/RNG parity claims.
- GlStateManager replaces cached state mutations where appropriate; debug raw GL attribute changes are restored. Owned GL callbacks and font rendering have NOT been exercised with a graphics context. Full client rendering, server/save integration and all remaining colliding particles remain open. This is not permission to simplify machine models or gameplay.
- Fresh common clean compile FAIL929, previously949, in25s. Eleven changed/new Java files match3897 generated compiler inputs. No runnable full mod/JAR yet. Full log and hashes are in continuation-2026-10-19.

## Reproduce

From the module directory, using the established Java8/Gradle4.10.3/Forge2768 toolchain and local dependencies:

    python3 porting/tests/owned-particle-source-semantics.py
    python3 porting/tests/check-native-owned-particles-source.py
    gradle --offline -I porting/tests/owned-particle-probe.init.gradle testPortOwnedParticles

On Windows PowerShell use the configured Gradle command and -PportPython=python, optionally -PportJavaHome=C:\path\to\jdk8. The Python runner also accepts --java-home and --classpath directly and supports java.exe/javac.exe. Do not use an empty or stale classpath file under build after clean. Windows execution has not been tested.

Common compile remains the unchanged command:

    gradle --offline clean -I porting/compile-diagnostics.init.gradle compileJava --console=plain

Next priorities are remaining declaration-level roots, especially native GUI versus EntityItem rendering and block renderer families; diagnostics still mask later method-body errors. Particle settings and all machine/energy/radiation semantics remain in scope.
