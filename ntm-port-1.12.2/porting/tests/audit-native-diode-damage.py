#!/usr/bin/env python3
"""Finite original/native bytecode evidence. No game/GL execution is claimed."""
import hashlib,io,re,subprocess,zipfile
from pathlib import Path

def audit(java_home,classpath,legacy_client,legacy_forge,output):
    sha=lambda p:hashlib.sha256(Path(p).read_bytes()).hexdigest()
    assert sha(legacy_client)=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce','Expected pristine Minecraft 1.7.10 client'
    assert sha(legacy_forge)=='6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91','Expected original Forge 1614 userdev'
    evidence=[]
    def bytecode(cp,name):return subprocess.check_output([str(java_home/'bin/javap'),'-classpath',str(cp),'-p','-c',name],text=True)
    def method(source,signature):
        start=source.index('  '+signature+';');end=re.search(r'\n  (?:public|private|protected|static) ',source[start+1:]);return source[start:start+1+end.start()] if end else source[start:]
    def record(title,body):evidence.append(title+'\n'+body)
    original=method(bytecode(legacy_client,'bma'),'public void a(bmh, yz, float)')
    assert original.index('Method bmh.c:()V')<original.index('Method blm.a:(Laji;IIILrf;)V')
    for fragment in ['sipush        774','sipush        768','float 0.5f','float -3.0f','float 0.1f','glPolygonOffset','glAlphaFunc']:assert fragment in original,fragment
    record('ORIGINAL RenderGlobal damage: blend, polygon offset, color suppression before RenderBlocks',original)
    tess=bytecode(legacy_client,'bmh');disable=method(tess,'public void c()');color=method(tess,'public void a(int, int, int, int)')
    assert re.search(r'iconst_1\s+\d+: putfield.*Field r:Z',disable)
    assert re.search(r'getfield.*Field r:Z\s+\d+: ifeq\s+8\s+7: return',color)
    record('ORIGINAL Tessellator disableColor flag and guarded color setter',disable+'\n'+color[:650])
    override=method(bytecode(legacy_client,'blm'),'public void a(aji, int, int, int, rf)')
    assert override.index('Method a:(Lrf;)V')<override.index('Method b:(Laji;III)Z')<override.index('Method a:()V')
    record('ORIGINAL RenderBlocks override / renderBlockByType / clear',override)
    with zipfile.ZipFile(legacy_forge) as archive:
        with zipfile.ZipFile(io.BytesIO(archive.read('fmlpatches.zip'))) as patches:patch=patches.read('net/minecraft/client/renderer/RenderBlocks.java.patch').decode()
    dispatch_line=next(line for line in patch.splitlines() if 'default: return FMLRenderAccessLibrary.renderWorldBlock' in line)
    record('ORIGINAL Forge 1614 custom render type reachability',dispatch_line)
    native=bytecode(classpath,'net.minecraft.client.renderer.RenderGlobal')
    special=native.index('IBlockState.hasCustomBreakingProgress:()Z');snippet=native[special-300:special+750]
    assert re.search(r'hasCustomBreakingProgress:\(\)Z\s+\d+: ifeq\s+1532',snippet)
    assert 'DestroyBlockProgress.getPartialBlockDamage:()I' in snippet and 'TileEntityRendererDispatcher.render:' in snippet
    record('NATIVE RenderGlobal exact custom breaking predicate and stage dispatch',snippet)
    damage=method(native,'public void drawBlockDamageTexture(net.minecraft.client.renderer.Tessellator, net.minecraft.client.renderer.BufferBuilder, net.minecraft.entity.Entity, float)')
    assert re.search(r'TileEntity.canRenderBreaking:\(\)Z\s+271: ifeq\s+278\s+274: iconst_1',damage)
    assert re.search(r'281: iload\s+22\s+283: ifne\s+384',damage)
    assert re.search(r'381: invokevirtual.*BlockRendererDispatcher.renderBlockDamage:',damage)
    record('NATIVE RenderGlobal ordinary damage skip predicate',damage[damage.index('256:'):damage.index('384:')+80])
    dispatcher=bytecode(classpath,'net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher')
    route=method(dispatcher,'public void render(net.minecraft.tileentity.TileEntity, float, int)')
    assert 'Method render:(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V' in route
    record('NATIVE tile dispatcher camera-relative stage forwarding (GL setup audited, not executed)',route)
    entry=method(dispatcher,'public void render(net.minecraft.tileentity.TileEntity, double, double, double, float, int, float)')
    assert 'TileEntitySpecialRenderer.render:(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V' in entry
    assert 'GlStateManager' not in entry and 'org/lwjgl' not in entry
    record('NATIVE dispatcher overload actually invoked in headless fixture',entry)
    draw=method(bytecode(classpath,'com.hbm.render.tileentity.NetworkBreakingRenderer'),'protected void draw(net.minecraft.client.renderer.BufferBuilder, net.minecraft.client.renderer.texture.TextureMap)')
    for operation in ['glPushAttrib','glPushClientAttrib','glPopClientAttrib','glPopAttrib','glDisableClientState','OpenGlHelper.setActiveTexture','WorldVertexBufferUploader.draw','Exception table:']:assert operation in draw,operation
    assert 'GlStateManager' not in draw
    record('PRODUCTION GPU state bracket: bytecode audit only; no live GL claim',draw)
    Path(output).write_text('Pinned original client SHA256 '+sha(legacy_client)+'\nPinned Forge1614 SHA256 '+sha(legacy_forge)+'\n\n'+'\n\n'.join(evidence))
    print('PASS original damage reachability/no-color/override bytecode; native RenderGlobal route/skip and dispatcher forwarding; production GL bracket audit (not live GL).',flush=True)
