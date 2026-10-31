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

def process_file(p):
    data = p.read_bytes()
    crlf = b'\r\n' in data
    text = data.decode('utf-8', errors='replace')

    if not ('.spawnEntityInWorld(' in text or
            'SharedMonsterAttributes.maxHealth' in text or
            'SharedMonsterAttributes.movementSpeed' in text or
            'SharedMonsterAttributes.attackDamage' in text or
            '.getCommandSenderName()' in text or
            '.getEntityItem()' in text):
        return False

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

            new_line = transform_line(line)
            if new_line != line:
                file_changed = True
            new_lines.append(new_line)
        else:
            new_lines.append(line)

    if file_changed:
        sep = b'\r\n' if crlf else b'\n'
        out_bytes = sep.join(line.encode('utf-8') for line in new_lines)
        p.write_bytes(out_bytes)
        return True
    return False

if __name__ == '__main__':
    modified = []
    for p in sorted(Path('src/main/java').rglob('*.java')):
        if process_file(p):
            modified.append(p)
    print(f'Successfully applied transformations to {len(modified)} files.')
