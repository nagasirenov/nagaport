# Rewrites blockstate model refs for the 1.12.2 vanilla Variant deserializer.
#
# Vanilla Variant$Deserializer.getResourceLocationBlock prepends "block/" after
# the namespace of EVERY "model" reference inside a blockstate JSON. References
# written as "hbm:block/X" therefore resolve to "hbm:block/block/X" and miss
# their JSON (models/block/block/X.json does not exist). Correct 1.12.2 form:
# "hbm:X" -> "hbm:block/X" -> models/block/X.json.
#
# Barbed wire additionally gets per-variant refs (each variant keeps its own
# _model texture) and six forge:obj wrapper models, sharing the single
# barbed_wire.obj geometry. Binary replace; CRLF preserved as-is.
import os

R = r'C:/Users/nagas/Desktop/port/ntm-port-1.12.2'
BS = R + '/src/main/resources/assets/hbm/blockstates'
MB = R + '/src/main/resources/assets/hbm/models/block'

files = [line.strip() for line in open(R + '/porting/tools/_files.txt', encoding='utf-8') if line.strip()]

wire_suffixes = ['', '_acid', '_fire', '_poison', '_wither', '_ultradeath']

rewritten = []
for f in files:
    p = BS + '/' + f
    data = open(p, 'rb').read()
    if f.startswith('barbed_wire'):
        target = f[:-len('.json')]
        new = data.replace(b'"hbm:block/barbed_wire"', ('"hbm:%s"' % target).encode('ascii'))
    else:
        new = data.replace(b'"hbm:block/', b'"hbm:')
    if new != data:
        open(p, 'wb').write(new)
        rewritten.append(f)

print('rewritten blockstates: %d of %d' % (len(rewritten), len(files)))

created = []
for suffix in wire_suffixes:
    name = 'barbed_wire' + suffix
    p = MB + '/' + name + '.json'
    if os.path.exists(p):
        continue
    tex = 'hbm:blocks/' + name + ('_model' if not suffix else '_model')
    body = (
        '{\n'
        '  "loader": "forge:obj",\n'
        '  "model": "hbm:models/blocks/barbed_wire.obj",\n'
        '  "flip-v": true,\n'
        '  "textures": {\n'
        '    "particle": "%s",\n'
        '    "OBJModel.Default.Texture.Name": "%s",\n'
        '    "OBJModel.White.Texture.Name": "%s"\n'
        '  }\n'
        '}\n' % (tex, tex, tex)
    )
    open(p, 'wb').write(body.replace('\n', '\r\n').encode('ascii'))
    created.append(name)

print('created obj wrappers: %s' % created)
