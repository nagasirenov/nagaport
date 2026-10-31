"""Convert remaining Tessellator line-graph GUIs to BufferBuilder."""

def color_rgba(hexstr):
    c = int(hexstr, 16)
    return (c >> 16) & 255, (c >> 8) & 255, c & 255

def convert(path, mode, hexcolor, draw_var='tess'):
    with open(path, 'rb') as f:
        lines = f.read().decode('utf-8').split('\r\n')
    r, g, b = color_rgba(hexcolor)
    out = []
    i = 0
    while i < len(lines):
        l = lines[i]
        s = l.strip()
        indent = l[:len(l) - len(l.lstrip())]
        if 'startDrawing(' in l:
            out.append(indent + 'net.minecraft.client.renderer.BufferBuilder buf = %s.getBuffer();' % draw_var)
            out.append(indent + 'buf.begin(%s, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);' % mode)
            i += 1
            continue
        if 'setColorOpaque_I(' in l:
            i += 1  # drop; color applied per vertex
            continue
        if '%s.addVertex(' % draw_var in l:
            l = l.replace('%s.addVertex(' % draw_var, 'buf.pos(')
            l = l.replace(');', ').color(%d, %d, %d, 255).endVertex();' % (r, g, b))
            out.append(l)
            i += 1
            continue
        out.append(l)
        i += 1
    with open(path, 'wb') as f:
        f.write('\r\n'.join(out).encode('utf-8'))
    print('converted:', path)

convert('src/main/java/com/hbm/inventory/gui/GUIRBMKConsole.java', 'GL11.GL_LINES', '00ff00')
convert('src/main/java/com/hbm/inventory/gui/GUIReactorControl.java', '3', '08FF00')

# GUIPADipole: helper rewrite + begin
p = 'src/main/java/com/hbm/inventory/gui/GUIPADipole.java'
with open(p, 'rb') as f:
    d = f.read().decode('utf-8')
old = ('\tpublic void addLine(Tessellator tess, int x, int y, int color, Vec3NT vec, float yaw) {\r\n'
       '\t\tvec.setComponents(0, 6, 0);\r\n'
       '\t\tvec.rotateAroundZDeg(yaw);\r\n'
       '\t\ttess.setColorOpaque_I(color);\r\n'
       '\t\ttess.addVertex(guiLeft + x, guiTop + y, this.zLevel);\r\n'
       '\t\ttess.addVertex(guiLeft + x + vec.x, guiTop + y + vec.y, this.zLevel);\r\n'
       '\t}\r\n')
new = ('\tpublic void addLine(Tessellator tess, int x, int y, int color, Vec3NT vec, float yaw) {\r\n'
       '\t\tvec.setComponents(0, 6, 0);\r\n'
       '\t\tvec.rotateAroundZDeg(yaw);\r\n'
       '\t\tnet.minecraft.client.renderer.BufferBuilder buf = tess.getBuffer();\r\n'
       '\t\tbuf.pos(guiLeft + x, guiTop + y, this.zLevel).color(color >> 16 & 255, color >> 8 & 255, color & 255, 255).endVertex();\r\n'
       '\t\tbuf.pos(guiLeft + x + vec.x, guiTop + y + vec.y, this.zLevel).color(color >> 16 & 255, color >> 8 & 255, color & 255, 255).endVertex();\r\n'
       '\t}\r\n')
if old in d:
    d = d.replace(old, new)
    d = d.replace('tessellator.startDrawing(1);',
                  'tessellator.getBuffer().begin(GL11.GL_LINES, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);')
    with open(p, 'wb') as f:
        f.write(d.encode('utf-8'))
    print('PADipole fixed')
else:
    print('PADipole pattern mismatch')
