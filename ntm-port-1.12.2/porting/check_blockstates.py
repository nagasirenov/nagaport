import os, re

bs_dir = r'C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\resources\assets\hbm\blockstates'
existing_bs = set(os.listdir(bs_dir))

with open(r'C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\blocks\ModBlocks.java', 'r', encoding='utf-8') as f:
    lines = f.readlines()

missing_blocks = []
for line in lines:
    line = line.strip()
    if 'GameRegistry.registerBlock' in line and 'getTranslationKey()' in line:
        # Extract var name
        m = re.search(r'GameRegistry\.registerBlock\s*\(\s*(\w+)', line)
        if m:
            var = m.group(1)
            # Find setTranslationKey or setBlockName for this var
            # Scan backwards or entire file
            # Let's search in text
            name_match = re.search(r'\b' + var + r'\s*=.*?\.(?:setTranslationKey|setBlockName)\s*\(\s*\"([^\"]+)\"', ''.join(lines))
            if name_match:
                name = name_match.group(1)
                candidates = [f'{name}.json', f'tile.{name}.json']
                if not any(c in existing_bs for c in candidates):
                    missing_blocks.append((var, name, f'tile.{name}.json'))
            else:
                missing_blocks.append((var, 'UNKNOWN', ''))

print(f'Total missing blockstates with getTranslationKey: {len(missing_blocks)}')
for var, name, bs in missing_blocks:
    print(f'{var}: name={name}, expected={bs}')
