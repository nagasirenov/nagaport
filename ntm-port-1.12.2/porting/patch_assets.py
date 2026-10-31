import os, json

base = r'C:\Users\nagas\Desktop\port\ntm-port-1.12.2'

# 1. mush.json, laythe_glowgrass.json, laythe_seagrass.json in models/block
block_models = {
    r'src\main\resources\assets\hbm\models\block\mush.json': 'hbm:blocks/mush',
    r'src\main\resources\assets\hbm\models\block\laythe_glowgrass.json': 'hbm:blocks/laythe_glowgrass',
    r'src\main\resources\assets\hbm\models\block\laythe_seagrass.json': 'hbm:blocks/laythe_seagrass',
}

for rel, tex in block_models.items():
    p = os.path.join(base, rel)
    with open(p, 'r', encoding='utf-8') as f:
        d = json.load(f)
    d['textures']['cross'] = tex
    d['textures']['particle'] = tex
    with open(p, 'wb') as f:
        f.write((json.dumps(d, indent=2) + '\r\n').encode('utf-8'))
    print('Updated cross in', rel)

# 2. Fix 20 item models with "parent": "item/generated" -> "parent": "minecraft:item/generated"
item_dir = os.path.join(base, r'src\main\resources\assets\hbm\models\item')
for f in os.listdir(item_dir):
    if f.endswith('.json'):
        fp = os.path.join(item_dir, f)
        with open(fp, 'r', encoding='utf-8') as fl:
            try:
                d = json.load(fl)
            except:
                continue
        if d.get('parent') == 'item/generated':
            d['parent'] = 'minecraft:item/generated'
            with open(fp, 'wb') as fl:
                fl.write((json.dumps(d, indent=2) + '\r\n').encode('utf-8'))
            print('Fixed parent in item model:', f)

# 3. Item models for mush, laythe plants, sapling
flat_item_models = {
    'tile.mush.json': 'hbm:blocks/mush',
    'tile.laythe_glowgrass.json': 'hbm:blocks/laythe_glowgrass',
    'tile.laythe_seagrass.json': 'hbm:blocks/laythe_seagrass',
    'tile.sapling.json': 'hbm:blocks/sapling_pvc',
}

for fname, tex in flat_item_models.items():
    fp = os.path.join(item_dir, fname)
    d = {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": tex
        }
    }
    with open(fp, 'wb') as fl:
        fl.write((json.dumps(d, indent=2) + '\r\n').encode('utf-8'))
    print('Generated 2D flat item model for:', fname)

# 4. War controller blockstate
war_bs = os.path.join(base, r'src\main\resources\assets\hbm\blockstates\tile.war_controller.json')
war_data = {
    "variants": {
        "facing=0": { "model": "hbm:war_controller" },
        "facing=1": { "model": "hbm:war_controller" },
        "facing=2": { "model": "hbm:war_controller" },
        "facing=3": { "model": "hbm:war_controller", "y": 180 },
        "facing=4": { "model": "hbm:war_controller", "y": 270 },
        "facing=5": { "model": "hbm:war_controller", "y": 90 },
        "normal": { "model": "hbm:war_controller" },
        "inventory": { "model": "hbm:war_controller" }
    }
}
with open(war_bs, 'wb') as f:
    f.write((json.dumps(war_data, indent=2) + '\r\n').encode('utf-8'))
print('Updated tile.war_controller.json blockstate')

# 5. pink_slab.json, pink_slab_top.json, vinyl_vine.json
slab_path = os.path.join(base, r'src\main\resources\assets\hbm\models\block\pink_slab.json')
slab_top_path = os.path.join(base, r'src\main\resources\assets\hbm\models\block\pink_slab_top.json')
vine_path = os.path.join(base, r'src\main\resources\assets\hbm\models\block\vinyl_vine.json')

with open(slab_path, 'r', encoding='utf-8') as f:
    d = json.load(f)
d['parent'] = 'minecraft:block/half_slab'
with open(slab_path, 'wb') as f:
    f.write((json.dumps(d, indent=2) + '\r\n').encode('utf-8'))
print('Updated pink_slab.json')

with open(slab_top_path, 'r', encoding='utf-8') as f:
    d = json.load(f)
d['parent'] = 'minecraft:block/upper_slab'
with open(slab_top_path, 'wb') as f:
    f.write((json.dumps(d, indent=2) + '\r\n').encode('utf-8'))
print('Updated pink_slab_top.json')

with open(vine_path, 'r', encoding='utf-8') as f:
    d = json.load(f)
d['parent'] = 'minecraft:block/vine_1'
with open(vine_path, 'wb') as f:
    f.write((json.dumps(d, indent=2) + '\r\n').encode('utf-8'))
print('Updated vinyl_vine.json')
