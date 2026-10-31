import os, re

sounds = set()
for root, dirs, files in os.walk('src/main/java'):
    for f in files:
        if f.endswith('.java'):
            p = os.path.join(root, f)
            with open(p, 'r', encoding='utf-8', errors='ignore') as fl:
                c = fl.read()
            for m in re.finditer(r'ModSoundEvents\.resolve\(\"([^\"]+)\"\)', c):
                sounds.add(m.group(1))

print(f'Total resolve calls with string literal: {len(sounds)}')
for s in sorted(sounds):
    print(s)
