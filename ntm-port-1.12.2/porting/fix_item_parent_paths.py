import pathlib

root = pathlib.Path('src/main/resources/assets/hbm/models/item')
replacements = {
    b'"hbm:bf_log_log"': b'"hbm:block/bf_log_log"',
    b'"hbm:eu_log_log"': b'"hbm:block/eu_log_log"',
    b'"hbm:lattice_log_log"': b'"hbm:block/lattice_log_log"',
    b'"hbm:pink_log_log"': b'"hbm:block/pink_log_log"',
    b'"hbm:primed_log_log"': b'"hbm:block/primed_log_log"',
    b'"hbm:pvc_log_log"': b'"hbm:block/pvc_log_log"',
    b'"hbm:vinyl_log_log"': b'"hbm:block/vinyl_log_log"',
    b'"hbm:furnace"': b'"hbm:block/furnace"',
    b'"hbm:lit_furnace"': b'"hbm:block/lit_furnace"',
    b'"hbm:sapling_pvc"': b'"hbm:block/sapling_pvc"',
}
for path in root.glob('*.json'):
    data = path.read_bytes()
    new = data
    for old, replacement in replacements.items():
        new = new.replace(old, replacement)
    if new != data:
        path.write_bytes(new)
        print(path.name)
