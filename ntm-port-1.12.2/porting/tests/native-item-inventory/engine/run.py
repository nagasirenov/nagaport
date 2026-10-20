#!/usr/bin/env python3
"""Cached actual-engine oracle; temporary output only, no platform stubs."""
import hashlib
import json
import pathlib
import subprocess
import tempfile
import zipfile

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[3]
TOOLS = pathlib.Path("/workspace/shared/nagaport-toolchain")
JAVA = str(TOOLS / "java8/bin/java")
JAVAC = str(TOOLS / "java8/bin/javac")
CP = (TOOLS / "native-classpath.txt").read_text().strip()
OLD = pathlib.Path("/tmp/nagaport-legacy-client.jar")
MOD = pathlib.Path("/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar")
MAPPINGS = pathlib.Path("/tmp/nagaport1710-srg.zip")

def sha(data):
    return hashlib.sha256(data).hexdigest()

def run(args, cwd):
    result = subprocess.run(args, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    print(result.stdout, end="", flush=True)
    if result.returncode:
        raise RuntimeError("Oracle command failed with status %s: %s" % (result.returncode, args[0]))

receipt = {"scope": "Actual engine and original full classfile evidence; not native production inventory/gameplay proof",
           "input_sha256": {str(p): sha(p.read_bytes()) for p in (OLD, MOD, MAPPINGS)}}
receipt["original_nested_constructors"] = {
    "classes": ["ItemLeadBox$InventoryLeadBox", "ItemToolBox$InventoryToolBox", "ItemRebarPlacer$InventoryRebar"],
    "owner_counts": [-2147483648, -1, 0], "stored_signed_byte_counts": [-128, -1, 0],
    "cases": 27, "player": None,
    "assertions": "Existing compound/list identity and content retained; raw owner count retained; nonpositive stored item/NBT loaded; actual capacity and sparse null slots"}
receipt["oracle_source_sha256"] = {p.name: sha(p.read_bytes()) for p in HERE.glob("*.java")}
mapping_dir = TOOLS / "gradle-user-home/caches/minecraft/de/oceanlabs/mcp/mcp_stable/39/1.12.2/srgs"
mapping_lines = []
for name in ("mcp-srg.srg", "notch-srg.srg"):
    path = mapping_dir / name
    mapping_lines += [str(path) + ":" + line for line in path.read_text().splitlines()
                      if line.startswith("FD:") and line.endswith("net/minecraft/item/ItemStack/field_77994_a")]
assert any("ItemStack/stackSize " in line for line in mapping_lines)
assert any("FD: aip/c " in line for line in mapping_lines)
(HERE / "mapping-evidence.txt").write_text("\n".join(mapping_lines) + "\n")
with tempfile.TemporaryDirectory(prefix="item-inventory-engine-") as td:
    tmp = pathlib.Path(td)
    classes = tmp / "classes"; classes.mkdir()
    mapper = ROOT / "porting/tests/difurnace-raw-reader/RemapQuantityField.java"
    run([JAVAC, "-encoding", "UTF-8", "-cp", CP, "-d", str(classes)] +
        [str(HERE / name) for name in ("NativeEngineOracle.java", "OriginalEngineOracle.java", "RemapOriginalInventory.java")] +
        [str(mapper)], tmp)
    with zipfile.ZipFile(MAPPINGS) as z:
        mapping = tmp / "joined.srg"; mapping.write_bytes(z.read("joined.srg"))
    legacy_classes = tmp / "legacy-classes"
    run([JAVA, "-cp", str(classes) + ":" + CP, "inventoryengine.RemapOriginalInventory",
         str(mapping), str(MOD), str(legacy_classes)], tmp)
    dependencies = [p for p in CP.split(":") if pathlib.Path(p).is_file() and "forgeBin-" not in p
                    and "deobfedDeps/" not in p]
    legacy_cp = ":".join([str(legacy_classes), str(classes), str(OLD), str(MOD)] + dependencies)
    run([JAVA, "-Xmx400m", "-cp", legacy_cp, "inventoryengine.OriginalEngineOracle", str(tmp / "old")], tmp)
    run([JAVA, "-Xmx400m", "-cp", str(classes) + ":" + CP, "inventoryengine.NativeEngineOracle", str(tmp / "native")], tmp)
    original = None
    for entry in CP.split(":"):
        p = pathlib.Path(entry)
        if p.is_file() and zipfile.is_zipfile(p):
            with zipfile.ZipFile(p) as z:
                if "net/minecraft/item/ItemStack.class" in z.namelist():
                    original = z.read("net/minecraft/item/ItemStack.class"); break
    assert original is not None
    raw = tmp / "ItemStack.class"; raw.write_bytes(original)
    renamed = tmp / "srg/net/minecraft/item/ItemStack.class"
    run([JAVA, "-cp", str(classes) + ":" + CP, "RemapQuantityField", str(raw), str(renamed)], tmp)
    run([JAVA, "-Xmx400m", "-DexpectedQuantityField=field_77994_a", "-cp",
         str(tmp / "srg") + ":" + str(classes) + ":" + CP, "inventoryengine.NativeEngineOracle", str(tmp / "srg-data")], tmp)
    receipt["native_class_sha256"] = sha(original)
    receipt["native_srg_field_class_sha256"] = sha(renamed.read_bytes())
    receipt["remapped_original_class_sha256"] = {
        str(p.relative_to(legacy_classes)): sha(p.read_bytes()) for p in legacy_classes.rglob("*.class")}
    sizes = []
    for n in range(5700, 6101):
        old = (tmp / "old" / (str(n) + ".nbt.gz")).read_bytes()
        native = (tmp / "native" / (str(n) + ".nbt.gz")).read_bytes()
        srg = (tmp / "srg-data" / (str(n) + ".nbt.gz")).read_bytes()
        assert old == native == srg, ("compressed bytes differ", n, len(old), len(native))
        sizes.append((n, len(old)))
    transitions = [(n, size) for n, size in sizes if 5999 <= size <= 6001]
    assert {size for n, size in transitions} == {5999, 6000, 6001}, transitions
    receipt["compression"] = {"identical_cases": len(sizes), "threshold_cases": transitions,
                              "byte_stream_sha256": sha(b"".join((tmp / "old" / (str(n) + ".nbt.gz")).read_bytes() for n, _ in sizes))}
    print("PASS 401 byte-identical original/native/SRG compressed compound payloads; exact 5999/6000/6001 boundaries", flush=True)
(HERE / "receipt.json").write_text(json.dumps(receipt, indent=2) + "\n")
print("PASS engine oracle; no native production inventory or spawned-world ejection claimed")
