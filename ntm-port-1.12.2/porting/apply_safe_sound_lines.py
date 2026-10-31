from pathlib import Path
import re

p = Path('src/main/java/com/hbm/blocks/ModBlocks.java')
lines = p.read_bytes().decode('utf-8').splitlines()

target_vars = [
    'laythe_kelp',
    'geysir_water',
    'geysir_chlorine',
    'geysir_vapor',
    'geysir_nether',
    'geysir_volcanic',
    'furnace',
    'lit_furnace',
    'pa_source',
    'pa_beamline',
    'pa_rfc',
    'pa_quadrupole',
    'pa_dipole',
    'pa_detector',
    'crate_ammo',
    'mass_storage',
    'rubber_grass',
    'astroturf'
]

transformed = 0
for idx, line in enumerate(lines):
    for var in target_vars:
        if line.strip().startswith(f'{var} =') and 'setStepSound' in line:
            m = re.search(r'\.setStepSound\(([^)]+)\)', line)
            if m:
                sound_arg = m.group(1)
                newline = line.replace(m.group(0), '')
                # Insert .withSoundType(sound_arg) right after new ClassName(...)
                m_new = re.search(r'new\s+[A-Za-z0-9_]+(?:\([^)]*\)|\(\))', newline)
                if m_new:
                    idx_ins = m_new.end()
                    newline = newline[:idx_ins] + f'.withSoundType({sound_arg})' + newline[idx_ins:]
                    lines[idx] = newline
                    transformed += 1
                    print(f'Transformed {var} on line {idx+1}')

print(f'Total transformed: {transformed}')
crlf_text = '\r\n'.join(lines) + '\r\n'
p.write_bytes(crlf_text.encode('utf-8'))
