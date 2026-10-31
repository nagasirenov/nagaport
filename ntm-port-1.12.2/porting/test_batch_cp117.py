import re
from pathlib import Path

def transform_line(line):
    # 1. spawnEntityInWorld -> spawnEntity
    line = re.sub(r'\.spawnEntityInWorld\(', '.spawnEntity(', line)

    # 2. SharedMonsterAttributes
    line = re.sub(r'\bSharedMonsterAttributes\.maxHealth\b', 'SharedMonsterAttributes.MAX_HEALTH', line)
    line = re.sub(r'\bSharedMonsterAttributes\.movementSpeed\b', 'SharedMonsterAttributes.MOVEMENT_SPEED', line)
    line = re.sub(r'\bSharedMonsterAttributes\.attackDamage\b', 'SharedMonsterAttributes.ATTACK_DAMAGE', line)

    # 3. getCommandSenderName() -> getName()
    line = re.sub(r'\.getCommandSenderName\(\)', '.getName()', line)

    # 4. getEntityItem() -> getItem()
    line = re.sub(r'\.getEntityItem\(\)', '.getItem()', line)

    return line

stats = {
    'spawnEntityInWorld': 0,
    'SharedMonsterAttributes': 0,
    'getCommandSenderName': 0,
    'getEntityItem': 0,
}

files_changed = 0

for p in sorted(Path('src/main/java').rglob('*.java')):
    data = p.read_bytes()
    crlf = b'\r\n' in data
    text = data.decode('utf-8', errors='replace')

    # Quick check
    if not ('.spawnEntityInWorld(' in text or
            'SharedMonsterAttributes.maxHealth' in text or
            'SharedMonsterAttributes.movementSpeed' in text or
            'SharedMonsterAttributes.attackDamage' in text or
            '.getCommandSenderName()' in text or
            '.getEntityItem()' in text):
        continue

    lines = text.split('\r\n' if crlf else '\n')
    new_lines = []
    file_changed = False
    for line in lines:
        if ('.spawnEntityInWorld(' in line or
            'SharedMonsterAttributes.maxHealth' in line or
            'SharedMonsterAttributes.movementSpeed' in line or
            'SharedMonsterAttributes.attackDamage' in line or
            '.getCommandSenderName()' in line or
            '.getEntityItem()' in line):

            if '.spawnEntityInWorld(' in line:
                stats['spawnEntityInWorld'] += len(re.findall(r'\.spawnEntityInWorld\(', line))
            if 'SharedMonsterAttributes' in line:
                stats['SharedMonsterAttributes'] += len(re.findall(r'SharedMonsterAttributes\.(?:maxHealth|movementSpeed|attackDamage)', line))
            if '.getCommandSenderName()' in line:
                stats['getCommandSenderName'] += len(re.findall(r'\.getCommandSenderName\(\)', line))
            if '.getEntityItem()' in line:
                stats['getEntityItem'] += len(re.findall(r'\.getEntityItem\(\)', line))

            new_line = transform_line(line)
            if new_line != line:
                file_changed = True
            new_lines.append(new_line)
        else:
            new_lines.append(line)

    if file_changed:
        files_changed += 1

print(f'Files changed: {files_changed}')
for k, v in stats.items():
    print(f'  {k:25s}: {v}')
print(f'Total replacements: {sum(stats.values())}')
