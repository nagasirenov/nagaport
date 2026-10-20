#!/usr/bin/env python3
"""Pinned original damage semantics and genuine native special-route/skip evidence, no GL execution."""
import argparse,hashlib,re,subprocess
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args()
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def bytecode(cp,name):return subprocess.check_output([str(a.java_home/'bin/javap'),'-classpath',str(cp),'-p','-c',name],text=True)
def method(text,signature):
 start=text.index('  '+signature+';');end=re.search(r'\n  (?:public|private|protected|static) ',text[start+1:]);return text[start:start+1+end.start()] if end else text[start:]
evidence=[]
old=method(bytecode(a.legacy_client,'bma'),'public void a(bmh, yz, float)')
assert old.index('Method bmh.c:()V')<old.index('Method blm.a:(Laji;IIILrf;)V')
for word in ['sipush        774','sipush        768','float 0.5f','float -3.0f','float 0.1f','glPolygonOffset','glAlphaFunc']:assert word in old,word
evidence.append('ORIGINAL damage pass color-disable before block renderer, blend/offset/alpha:\n'+old)
tess=bytecode(a.legacy_client,'bmh');disable=method(tess,'public void c()');color=method(tess,'public void a(int, int, int, int)')
assert re.search(r'iconst_1\s+\d+: putfield.*Field r:Z',disable)
assert re.search(r'getfield.*Field r:Z\s+\d+: ifeq\s+8\s+7: return',color)
evidence.append('ORIGINAL color suppression:\n'+disable+'\n'+color[:650])
native=bytecode(a.classpath,'net.minecraft.client.renderer.RenderGlobal');at=native.index('IBlockState.hasCustomBreakingProgress:()Z');snippet=native[at-300:at+750]
assert re.search(r'hasCustomBreakingProgress:\(\)Z\s+\d+: ifeq\s+1532',snippet)
assert 'DestroyBlockProgress.getPartialBlockDamage:()I' in snippet and 'TileEntityRendererDispatcher.render:' in snippet
evidence.append('NATIVE custom damage route and stage dispatch:\n'+snippet)
damage=method(native,'public void drawBlockDamageTexture(net.minecraft.client.renderer.Tessellator, net.minecraft.client.renderer.BufferBuilder, net.minecraft.entity.Entity, float)')
assert re.search(r'TileEntity.canRenderBreaking:\(\)Z\s+271: ifeq\s+278\s+274: iconst_1',damage)
assert re.search(r'281: iload\s+22\s+283: ifne\s+384',damage)
assert re.search(r'381: invokevirtual.*BlockRendererDispatcher.renderBlockDamage:',damage)
evidence.append('NATIVE standard retexture skipped for canRenderBreaking tiles:\n'+damage[damage.index('256:'):damage.index('384:')+80])
dispatcher=bytecode(a.classpath,'net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher');entry=method(dispatcher,'public void render(net.minecraft.tileentity.TileEntity, double, double, double, float, int, float)')
assert 'TileEntitySpecialRenderer.render:(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V' in entry
assert 'GlStateManager' not in entry and 'org/lwjgl' not in entry
evidence.append('NATIVE exact dispatcher overload executed in CPU tests:\n'+entry)
Path(__file__).with_name('breaking-routing-bytecode.txt').write_text('\n\n'.join(evidence))
print('PASS pinned original damage color-disable/blend plus native custom damage route, standard retexture skip and exact dispatcher overload bytecode.')
