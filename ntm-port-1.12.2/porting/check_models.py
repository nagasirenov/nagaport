import os, re, json

bs_dir = "src/main/resources/assets/hbm/blockstates"
for f in sorted(os.listdir(bs_dir)):
    if not f.endswith(".json"): continue
    path = os.path.join(bs_dir, f)
    with open(path, "r", encoding="utf-8") as fp:
        try:
            data = json.load(fp)
        except Exception as e:
            continue
    content = json.dumps(data)
    for m in set(re.findall(r'"model"\s*:\s*"([^"]+)"', content)):
        if m.startswith("hbm:") and not ("native_" in m or m.startswith("hbm:block/") or m.startswith("hbm:item/")):
            print(f"{f} -> {m}")
