# Official Inventory Tweaks compile-time API

Upstream: https://github.com/Inventory-Tweaks/inventory-tweaks
Release tag: 1.63, peeled commit 220f184e98d4d938a3edc9e3e244f702032ef15f.
Exact source: https://github.com/Inventory-Tweaks/inventory-tweaks/blob/220f184e98d4d938a3edc9e3e244f702032ef15f/src/main/java/invtweaks/api/container/ChestContainer.java
License: original upstream LICENSE.md copied alongside this file (MIT).

src/api/java/invtweaks/api/container/ChestContainer.java is byte-identical to the official API, including both nested callback annotations and every default. It is not an invented stub or a rewritten compatibility layer. Only this genuinely referenced official API declaration is vendored; no Inventory Tweaks mod implementation is bundled.

Existing ForgeGradle api source set compiles it and includes its output in the main compiler classpath. Actual Gradle jar source inspection confirms no ChestContainer class is included in the runtime mod jar. The optional installed Inventory Tweaks mod supplies the runtime annotation when present. Six existing crate/safe annotations remain unchanged, including row sizes and button mode. This restores compile-time dependency metadata, not container sorting/gameplay acceptance. Full containers still depend on their separate ongoing native inventory migration.
