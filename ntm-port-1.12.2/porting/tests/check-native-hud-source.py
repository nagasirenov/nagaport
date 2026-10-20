#!/usr/bin/env python3
"""Exact API-only inverse for all three HUD classes; complements real native draws."""
from pathlib import Path
import hashlib,json,re
root=Path(__file__).resolve().parents[2]
manifest=json.loads(Path(__file__).with_name('native-hud-scope.json').read_text())
for name,record in manifest['production'].items():
 raw=(root/name).read_bytes();assert hashlib.sha256(raw).hexdigest()==record['sha256'],name
 s=raw.decode().replace('event.getResolution()','event.resolution').replace('mc.getRenderItem().renderItemAndEffectIntoGUI(', 'itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), ')
 s=s.replace('import net.minecraft.client.renderer.GlStateManager;\n','')
 for token,suffix in [('GL11.GL_BLEND','Blend'),('GL11.GL_ALPHA_TEST','Alpha'),('GL11.GL_DEPTH_TEST','Depth'),('GL12.GL_RESCALE_NORMAL','RescaleNormal')]:
  for a,b in [('Enable','enable'),('Disable','disable')]:s=s.replace('GlStateManager.'+b+suffix+'()', 'GL11.gl'+a+'('+token+')')
 for a,b in [('glColor4f','color'),('glDepthMask','depthMask')]:s=s.replace('GlStateManager.'+b+'(', 'GL11.'+a+'(')
 s=s.replace('GlStateManager.tryBlendFuncSeparate(', 'OpenGlHelper.glBlendFunc(')
 if name.endswith('HUDComponentAmmoCounter.java'):
  s=s.replace('import net.minecraft.client.renderer.RenderHelper;','import net.minecraft.client.renderer.RenderHelper;\nimport net.minecraft.client.renderer.entity.RenderItem;')
  line='\tprivate static final ResourceLocation misc = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_misc.png");'
  s=s.replace(line,line+'\n\tprotected static final RenderItem itemRenderer = RenderItem.getInstance();')
 if name.endswith('RenderScreenOverlay.java'):
  s=s.replace('import net.minecraft.client.renderer.BufferBuilder;\nimport net.minecraft.client.renderer.vertex.DefaultVertexFormats;','import net.minecraft.client.renderer.entity.RenderItem;')
  line='\tpublic static final ResourceLocation misc = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_misc.png");'
  s=s.replace(line,line+'\n\tpublic static final RenderItem itemRenderer = RenderItem.getInstance();')
  s=s.replace('Gui.ICONS','Gui.icons').replace('Minecraft.getMinecraft().player','Minecraft.getMinecraft().thePlayer')
  head,tail=s.split('public static void renderFlashbangOverlay',1)
  def undo(part,var):
   part=part.replace('Tessellator '+var+' = Tessellator.getInstance();\n\t\tBufferBuilder buffer = '+var+'.getBuffer();','Tessellator '+var+' = Tessellator.instance;')
   part=part.replace('buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);',var+'.startDrawingQuads();')
   return re.sub(r'buffer\.pos\((.*?)\)\.tex\((.*?)\)\.endVertex\(\);',lambda m:var+'.addVertexWithUV('+m[1]+', '+m[2]+');',part)
  s=undo(head,'tess')+'public static void renderFlashbangOverlay'+undo(tail,'tessellator')
 assert hashlib.sha256(s.encode()).hexdigest()==record['before_sha256'],(name,'API inverse failed; unrelated logic changed')
print('PASS 3 complete HUD class API-only inverses; all original formulas/branches/GL calls outside listed API mapping byte-identical.')
