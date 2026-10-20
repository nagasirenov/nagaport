#!/usr/bin/env python3
"""Exact, opt-in source inverse and finite legacy-promotion ownership checks."""
import hashlib
import json
import re
import zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent


def sha(data):
    return hashlib.sha256(data).hexdigest()


def restore_base(path, data):
    """Return CP89/CP90 bytes; return None for this patch's sole added source.

    Unowned paths pass through unchanged. Owned paths must match the complete
    recorded after-file, not just the new methods. No files are written.
    """
    manifest = json.loads((HERE / "manifest.json").read_text())
    if path in manifest["added"]:
        assert sha(data) == manifest["added"][path], path
        return None
    if path not in manifest["files"]:
        return data
    entry = manifest["files"][path]
    assert sha(data) == entry["after_sha256"], path
    start, inserted = entry["start"], entry["inserted"].encode()
    assert data[start:start + len(inserted)] == inserted, path
    inverse = data[:start] + data[start + len(inserted):]
    assert sha(inverse) == entry["baseline_sha256"], path
    assert sha((HERE / "baseline.zip").read_bytes()) == manifest["baseline_zip_sha256"]
    with zipfile.ZipFile(HERE / "baseline.zip") as archive:
        assert inverse == archive.read(path), path
    return inverse


def code_only(text):
    # Preserve offsets/line numbers while excluding Java comments and literals.
    pattern = r'/\*[\s\S]*?\*/|//[^\n]*|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\''
    return re.sub(pattern, lambda m: re.sub(r"[^\n]", " ", m[0]), text)


def verify(module, controls=True):
    manifest = json.loads((HERE / "manifest.json").read_text())
    for path in list(manifest["files"]) + list(manifest["added"]):
        data = (module / path).read_bytes()
        restore_base(path, data)
        if controls:
            try:
                restore_base(path, data + b"\n// unrelated mutation\n")
            except AssertionError:
                pass
            else:
                raise AssertionError("unrelated source edit escaped inverse: " + path)
    texts = {str(p.relative_to(module)): code_only(p.read_text())
             for p in (module / "src/main/java").rglob("*.java")}
    blocks = ["src/main/java/com/hbm/blocks/machine/Machine" + n + ".java"
              for n in ("Press", "EPress")]
    tiles = ["src/main/java/com/hbm/tileentity/machine/TileEntityMachine" + n + ".java"
             for n in ("Press", "EPress")]
    base = "src/main/java/com/hbm/blocks/BlockDummyable.java"
    for name in ("isLegacyMonoblock", "fixLegacyMonoblock"):
        definitions = sorted(p for p, s in texts.items()
                             if re.search(r"(?:protected|public)\s+(?:boolean|void)\s+" + name + r"\s*\(", s))
        assert definitions == sorted(blocks + [base]), (name, definitions)
    users = sorted(p for p, s in texts.items() if re.search(r"\bwithLegacyPromotion\s*\(", s))
    assert users == sorted(blocks + tiles), users
    for path in users:
        assert len(re.findall(r"\bwithLegacyPromotion\s*\(", texts[path])) == 1, path
    for name in ("MachinePress", "MachineEPress", "TileEntityMachinePress", "TileEntityMachineEPress"):
        assert not any(re.search(r"\bextends\s+(?:[\w$]+\.)*" + name + r"\b", s) for s in texts.values()), name
    for name in ("TileEntityMachineBase", "TileEntityLoadedBase"):
        assert not re.search(r"\bshouldRefresh\s*\(", texts["src/main/java/com/hbm/tileentity/" + name + ".java"])
    registrations = texts["src/main/java/com/hbm/blocks/ModBlocks.java"]
    for name in ("MachinePress", "MachineEPress"):
        assert len(re.findall(r"\bnew\s+" + name + r"\s*\(", registrations)) == 1, name
    assert re.search(r"\boffset\s*=\s*10\s*;", texts[base])
    print("PASS exact five-source inverse; only two source-owned promotion opt-ins; no source descendants or new guard callers")
    if controls:
        print("PASS five unrelated-file mutation controls rejected by complete-file inverse")


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", type=Path, default=HERE.parents[2])
    verify(parser.parse_args().module)
