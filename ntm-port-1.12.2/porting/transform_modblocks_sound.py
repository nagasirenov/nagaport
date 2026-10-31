from pathlib import Path
import re

p = Path('src/main/java/com/hbm/blocks/ModBlocks.java')
lines = p.read_bytes().decode('utf-8').splitlines()

transformed_count = 0
for idx, line in enumerate(lines):
    if 'setStepSound' in line:
        m_sound = re.search(r'\.setStepSound\(([^)]+)\)', line)
        if not m_sound:
            print(f'Line {idx+1}: Could not find setStepSound call!')
            continue
        sound_arg = m_sound.group(1)

        # Replace new BlockFalling with new BlockHazardFalling
        newline = line.replace('new BlockFalling(', 'new BlockHazardFalling(')
        # Remove .setStepSound(...)
        newline = newline.replace(m_sound.group(0), '')

        # Find new ClassName(...)
        # Pattern: new ClassName(...) or new ClassName<...>(...)
        # We need to find the matching closing paren of new ClassName(...)
        m_new = re.search(r'new\s+[A-Za-z0-9_]+(?:\([^)]*\)|\(\))', newline)
        if not m_new:
            # might have nested parens e.g. (atmosphere) -> atmosphere.hasFluid(...)
            # Let's do a paren counter
            m_start = re.search(r'new\s+[A-Za-z0-9_]+\(', newline)
            if not m_start:
                print(f'Line {idx+1}: Could not match new ClassName in: {newline}')
                continue
            start_pos = m_start.end() - 1
            paren_depth = 0
            end_pos = -1
            for i in range(start_pos, len(newline)):
                if newline[i] == '(':
                    paren_depth += 1
                elif newline[i] == ')':
                    paren_depth -= 1
                    if paren_depth == 0:
                        end_pos = i + 1
                        break
            if end_pos == -1:
                print(f'Line {idx+1}: Could not balance parens: {newline}')
                continue
            newline = newline[:end_pos] + f'.withSoundType({sound_arg})' + newline[end_pos:]
        else:
            idx_insert = m_new.end()
            newline = newline[:idx_insert] + f'.withSoundType({sound_arg})' + newline[idx_insert:]

        lines[idx] = newline
        transformed_count += 1

print(f'Successfully transformed {transformed_count} of 85 lines in ModBlocks.java')
crlf_text = '\r\n'.join(lines) + '\r\n'
p.write_bytes(crlf_text.encode('utf-8'))
