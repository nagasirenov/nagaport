import os, json, re

bs_dir = "src/main/resources/assets/hbm/blockstates"
models_block_dir = "src/main/resources/assets/hbm/models/block"
models_item_dir = "src/main/resources/assets/hbm/models/item"

models_block = set(os.listdir(models_block_dir))
models_item = set(os.listdir(models_item_dir))

patched_count = 0

for f in sorted(os.listdir(bs_dir)):
    if not f.endswith(".json"): continue
    path = os.path.join(bs_dir, f)
    with open(path, "r", encoding="utf-8") as fp:
        raw = fp.read()
    
    modified = False
    
    # 1. Fix "model": "hbm:<name>" where <name>.json is in models/block
    def replace_model(m):
        full = m.group(0)
        target = m.group(1)
        if target.startswith("block/") or target.startswith("item/") or target.startswith("native_"):
            return full
        if (target + ".json") in models_block:
            return f'"model": "hbm:block/{target}"'
        return full

    new_raw = re.sub(r'"model"\s*:\s*"hbm:([^"]+)"', replace_model, raw)
    if new_raw != raw:
        raw = new_raw
        modified = True
        
    # 2. Check if variants lacks inventory
    try:
        data = json.loads(raw)
        if "variants" in data and isinstance(data["variants"], dict):
            variants = data["variants"]
            if "inventory" not in variants:
                item_name = f[:-5] # strip .json
                if f in models_item:
                    variants["inventory"] = {"model": f"hbm:item/{item_name}"}
                    modified = True
                else:
                    # check if any model in variants can serve as inventory
                    first_model = None
                    for k, v in variants.items():
                        if isinstance(v, dict) and "model" in v:
                            first_model = v["model"]
                            break
                        elif isinstance(v, list) and len(v) > 0 and "model" in v[0]:
                            first_model = v[0]["model"]
                            break
                    if first_model and first_model.startswith("hbm:block/"):
                        variants["inventory"] = {"model": first_model}
                        modified = True
        if modified:
            raw = json.dumps(data, indent=2) + "\n"
    except Exception as e:
        print(f"Error parsing JSON in {f}: {e}")

    if modified:
        with open(path, "w", encoding="utf-8") as fp:
            fp.write(raw)
        patched_count += 1
        print(f"Patched {f}")

print(f"Total patched blockstates: {patched_count}")
