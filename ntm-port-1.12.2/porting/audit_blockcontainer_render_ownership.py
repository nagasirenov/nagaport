from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parents[1]
BLOCKS = ROOT / "src/main/java/com/hbm/blocks"
CLIENT_PROXY = ROOT / "src/main/java/com/hbm/main/ClientProxy.java"

CLASS_RE = re.compile(r"public\s+(?:abstract\s+)?class\s+(\w+)\s+extends\s+BlockContainer\b")
ICON_RE = re.compile(r"\bgetIcon\s*\(\s*int\s+\w+\s*,\s*int\s+\w+\s*\)")
RENDER_RE = re.compile(
    r"\bgetRenderType\s*\(\s*(?:net\.minecraft\.block\.state\.)?IBlockState\s+\w+\s*\)"
    r"\s*\{\s*return\s+(?:net\.minecraft\.util\.)?EnumBlockRenderType\.(\w+)",
    re.S,
)
TILE_RE = re.compile(r"return\s+new\s+(TileEntity\w+)\s*\(")
BIND_RE = re.compile(
    r"bindTileEntitySpecialRenderer\(\s*(TileEntity\w+)\.class\s*,\s*new\s+(\w+)\s*\("
)

MODEL_BODY_CLASSES = {
    "BlockFireworks",
    "BlockAirPump",
    "BlockClorineSeal",
    "BlockGlyphidSpawner",
    "BlockVent",
    "BlockWandStructure",
    "BlockCustomMachine",
    "BlockCybercrab",
    "BlockDecon",
    "MachineAutocrafter",
    "MachineBattery",
    "MachineDeuteriumExtractor",
    "MachineICFController",
    "MachineICFPress",
    "MachineKeyForge",
    "MachinePWRController",
    "MachineReactorControl",
    "MachineSatLinker",
    "MachineShredder",
    "MachineSiren",
    "MachineTeleporter",
    "MachineTurbine",
    "PowerDetector",
    "WasteDrum",
    "CableDetector",
    "CableSwitch",
    "BlockConverterHeRf",
    "BlockConverterRfHe",
}


def scan():
    bindings = dict(BIND_RE.findall(CLIENT_PROXY.read_text(encoding="utf-8", errors="ignore")))
    rows = {}
    for path in BLOCKS.rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        match = CLASS_RE.search(text)
        if not match:
            continue
        name = match.group(1)
        render = RENDER_RE.search(text)
        tiles = sorted(set(TILE_RE.findall(text)))
        tesrs = sorted({bindings[tile] for tile in tiles if tile in bindings})
        has_icon = bool(ICON_RE.search(text))
        if render:
            category = "EXPLICIT_" + render.group(1)
        elif has_icon and tesrs:
            category = "TESR_REVIEW"
        elif has_icon:
            category = "BAKED_CANDIDATE"
        else:
            category = "OTHER"
        rows[name] = {
            "path": path.relative_to(ROOT).as_posix(),
            "render": render.group(1) if render else None,
            "tiles": tiles,
            "tesrs": tesrs,
            "icon": has_icon,
            "category": category,
        }
    return rows


def require(rows, class_name, *, render=None, category=None, tesr=None):
    row = rows.get(class_name)
    if row is None:
        raise AssertionError(f"missing direct BlockContainer class: {class_name}")
    if render is not None and row["render"] != render:
        raise AssertionError(f"{class_name}: expected render={render}, got {row['render']}")
    if category is not None and row["category"] != category:
        raise AssertionError(f"{class_name}: expected category={category}, got {row['category']}")
    if tesr is not None and tesr not in row["tesrs"]:
        raise AssertionError(f"{class_name}: expected TESR {tesr}, got {row['tesrs']}")


def main():
    rows = scan()

    # Concrete ownership checks for the current invisible-block defect.
    for class_name in sorted(MODEL_BODY_CLASSES):
        require(rows, class_name, render="MODEL", category="EXPLICIT_MODEL")

    require(rows, "BlockStorageCrate", render="MODEL", category="EXPLICIT_MODEL")
    require(rows, "DroneCrate", render="MODEL", category="EXPLICIT_MODEL")
    require(rows, "DroneDock", render="MODEL", category="EXPLICIT_MODEL")
    require(rows, "BlockMassStorage", render="MODEL", category="EXPLICIT_MODEL", tesr="RenderMassStorage")

    # Controls: explicit TESR-only ownership stays non-model. These three have
    # auxiliary TESRs while their block bodies stay baked MODEL geometry.
    require(rows, "BlockOrrery", render="INVISIBLE", category="EXPLICIT_INVISIBLE", tesr="RenderOrrery")
    require(rows, "BlockCustomMachine", render="MODEL", category="EXPLICIT_MODEL", tesr="RenderCustomMachine")
    require(rows, "BlockWandStructure", render="MODEL", category="EXPLICIT_MODEL", tesr="RenderWandStructure")
    require(rows, "MachineICFController", render="MODEL", category="EXPLICIT_MODEL", tesr="RenderICFController")

    unresolved = [row for row in rows.values() if row["category"] in {"BAKED_CANDIDATE", "TESR_REVIEW"}]
    print("classification checks: PASS")
    print(f"direct BlockContainer classes: {len(rows)}")
    print(f"unresolved legacy-icon classes: {len(unresolved)}")
    for row in sorted(unresolved, key=lambda item: item["path"]):
        suffix = f" TESR={','.join(row['tesrs'])}" if row["tesrs"] else ""
        print(f"{row['category']:15} {row['path']}{suffix}")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except AssertionError as failure:
        print(f"classification checks: FAIL: {failure}", file=sys.stderr)
        sys.exit(1)
