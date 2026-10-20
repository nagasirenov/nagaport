# Earlier generic inventory metadata-route defect (not changed here)

The initial native probe included ModItemsArmor.beta, whose actual constructor is
ItemDrop rather than an armor type. A valid native Item stack with damage 1 did
not resolve its declared model, while damage 0 did. The failure is retained in
beta-exclusion-control.log. This batch deliberately leaves beta unchanged. ArmorNo9 also sets max damage
to zero and has no custom mesh, so its no9 declaration is likewise excluded.
Taurun and Trenchmaster disable durability too, but their existing custom callback
mesh definitions cover all metadata and pass the actual native model probe.

The mechanism is broader than ItemDrop. NativeItemTextureModelHandler.register
currently installs metadata 0 only when getHasSubtypes is false. Real native
ItemModelMesher preserves stack metadata for non-damageable items, so that route
misses nonzero metadata. Original Item.getIconFromDamage returns its itemIcon for
all metadata. Damageable armor instead maps inventory metadata to 0 and passes
all tested damage values; this is why the bounded armor migration is independent.

Read-only current-source reachability established these 11 already-migrated
ItemDrop instances in ModItems.java use the same deficient default route:
cell_antimatter, cell_anti_schrabidium, singularity,
singularity_counter_resonant, singularity_super_heated, black_hole,
singularity_spark, pellet_antimatter, crystal_xen, detonator_deadman, detonator_de.
Their texture declarations intentionally include mismatched texture names, which
are not corrected here. ItemDrop has no constructor, max-damage/subtype setter,
icon callback, or custom-render provider; these initialization chains add no
damageability or subtypes. No field is separately named in render registration
sources. Ordinary damage-0 stacks work. Nonzero-metadata stacks can be constructed
through the native API; this audit does not claim that normal recipes generate
them or that these full NTM gameplay classes currently compile.

A separate task owns the eligible generic-handler fix. This batch changes neither
the shared handler nor vanilla/modded item mesher behavior, and does not treat the
armor test success as resolution of the older defect.

Combined validation update: the independent exact default-item mesh correction
from f1b2c7f617a16175a606b99506193782b913cbaf is now exercised by the combined armor
suite. The historical defect description above remains its original rationale,
not a claim that the corrected handler still has the defect. Beta and no9 source
declarations remain deferred in this finite armor patch. See the README's strict
composition receipt and the companion task's complete generic-metadata coverage.
