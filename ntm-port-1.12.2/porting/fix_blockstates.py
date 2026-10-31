import os, json, re

bs_dir = "src/main/resources/assets/hbm/blockstates"
models_block = set(os.listdir("src/main/resources/assets/hbm/models/block"))
models_item = set(os.listdir("src/main/resources/assets/hbm/models/item"))

candidates = []
for f in sorted(os.listdir(bs_dir)):
    if not f.endswith(".json"): continue
    path = os.path.join(bs_dir, f)
    with open(path, "r", encoding="utf-8") as fp:
        c = fp.read()
    matches = re.findall(r'"model"\s*:\s*"hbm:([^"]+)"', c)
    bad = [m for m in matches if not m.startswith("block/") and not m.startswith("item/") and not m.startswith("native_")]
    if bad:
        all_in_block = all((m + ".json") in models_block for m in bad)
        candidates.append((f, sorted(set(bad)), all_in_block))

print(f"Found {len(candidates)} blockstate files with unprefixed hbm models:")
for f, bad, all_in_block in candidates:
    has_item = f in models_item
    print(f"{f}: {bad} -> in models/block? {all_in_block}, has item model? {has_item}")
