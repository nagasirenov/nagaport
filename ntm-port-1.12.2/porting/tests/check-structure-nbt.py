#!/usr/bin/env python3
"""Check serialization payload parity against pristine; this is not a game test."""
from pathlib import Path
import re

project = Path(__file__).resolve().parents[2]
baseline = project.parent / "ntm-original-pristine"
package = Path("src/main/java/com/hbm/world/gen")


def bodies(text, method):
    result = []
    pattern = r"(?:protected|public) void " + method + r"\([^)]*\)\s*\{"
    for match in re.finditer(pattern, text):
        start = match.end()
        end, depth = start, 1
        while depth:
            depth += (text[end] == "{") - (text[end] == "}")
            end += 1
        body = text[start:end - 1]
        body = body.replace("func_143012_a", "writeStructureToNBT")
        body = body.replace("func_143011_b(nbt)", "readStructureFromNBT(nbt, templateManager)")
        result.append(re.sub(r"\s+", "", body))
    return result


count = 0
for name in ("Component", "BunkerComponents", "CivilianFeatures", "OfficeFeatures", "SiloComponent"):
    path = package / "component" / (name + ".java")
    old = (baseline / path).read_text()
    new = (project / path).read_text()
    for legacy, target in (("func_143012_a", "writeStructureToNBT"), ("func_143011_b", "readStructureFromNBT")):
        expected = bodies(old, legacy)
        actual = bodies(new, target)
        assert expected and expected == actual, (path, target, "NBT payload or superclass delegation changed")
        assert legacy not in new, (path, "obsolete callback remains")
        count += len(actual)
    reads = re.findall(r"protected void readStructureFromNBT\(([^)]*)\)", new)
    assert all(args == "NBTTagCompound nbt, TemplateManager templateManager" for args in reads), path
    assert "import net.minecraft.world.gen.structure.template.TemplateManager;" in new, path
    print("PASS payload and hook signatures:", path.name)

for relative in ("component/BunkerComponents.java", "component/CivilianFeatures.java", "component/OfficeFeatures.java", "component/RuinFeatures.java", "nbt/NBTStructure.java"):
    path = package / relative
    old = (baseline / path).read_text()
    new = (project / path).read_text()
    old_ids = re.findall(r"MapGenStructureIO\.func_143031_a\(([^;]+)\);", old)
    new_ids = re.findall(r"MapGenStructureIO\.registerStructureComponent\(([^;]+)\);", new)
    assert old_ids and old_ids == new_ids, (path, "component registration IDs changed")
    assert "MapGenStructureIO.func_143031_a" not in new, path

path = package / "ProceduralStructureStart.java"
old = (baseline / path).read_text()
new = (project / path).read_text()
for legacy, target in (("func_143017_b", "readFromNBT"), ("func_143022_a", "writeToNBT")):
    assert bodies(old, legacy) == bodies(new, target), (path, target)
assert "func_143019_e" not in new and "func_143018_f" not in new
print("PASS:", count, "component NBT hooks; registration IDs; procedural start payloads.")
print("STATIC ONLY: full component compilation and world save/reload are separate checks.")
