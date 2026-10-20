#!/usr/bin/env python3
"""Full native recipe/model units, independent original matrix/asset oracle, and exact source draw guard.
The bootstrap's actual ModBlocks/ResourceManager wiring is source-checked here, compiled by the parent full build.
No substitute Minecraft/Forge classes or pretend GL drawing.
"""
import argparse, pathlib, subprocess, tempfile, json, re, hashlib, os, importlib.util
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2];src=root/'src/main/java/com/hbm';native=src/'render/item/nativeitem';family=native/'staticfamily'
manifest=json.loads((root/'porting/tests/static-item-family-source-manifest.json').read_text());resource=(src/'main/ResourceManager.java').read_text();recipes=(family/'StaticItemFamilyDescriptors.java').read_text();bootstrap=(family/'StaticItemFamilyBootstrap.java').read_text();assets={};checks=0
compact=lambda text:re.sub(r'\s+','',text)
particles={row['owner']:row for row in json.loads((root/'porting/tests/static-item-family-particles.json').read_text())}
for row in particles.values():
    if row['sprite'].startswith('hbm:'):
        png=root/row['png'];assert hashlib.sha256(png.read_bytes()).hexdigest()==row['sha256'],('particle source pixels',row['owner']);checks+=1
        assert png.read_bytes()[:8]==b'\x89PNG\r\n\x1a\n';checks+=1
        # IHDR width/height are independent of Pillow availability or the native loader.
        import struct
        width,height=struct.unpack('>II',png.read_bytes()[16:24]);assert [width,height]==row['size'] and width==height;checks+=1
        if row.get('case_rename_from'):
            old=root/row['case_rename_from'];assert old.name not in {entry.name for entry in old.parent.iterdir()};checks+=1

for recipe in manifest['recipes']:
    assert recipe['particle']==particles[recipe['owner']];checks+=1
    name=recipe['method'];match=re.search(r'public static StaticItemDescriptor '+name+r'\(.*?\n    \}',recipes,re.S);assert match,name
    assert 'new ResourceLocation("'+recipe['particle']['sprite']+'")'+(', true' if recipe['particle']['mip_aligned_custom_sprite'] else '')+', () -> {' in match.group(0);checks+=1
    body=match.group(0).split('() -> {',1)[1].rsplit('});',1)[0].strip()
    for mesh in recipe['mesh_fields']:body=re.sub(r'\bm_'+mesh+r'\b','ResourceManager.'+mesh,body)
    for texture in recipe['texture_fields']:body=re.sub(r'\bt_'+texture+r'\b','ResourceManager.'+texture,body)
    assert compact(body)==compact(recipe['common']),('ordered original draw mismatch',name);checks+=1
    binding='StaticItemFamilyDescriptors.'+name+'(Item.getItemFromBlock('+recipe['owner']+'), '+', '.join('ResourceManager.'+s for s in recipe['mesh_fields']+recipe['texture_fields'])+')'
    assert binding in bootstrap,('actual item/resource binding',name);checks+=1
    for field in recipe['mesh_fields']+recipe['texture_fields']:
        assignment=re.search(r'\b'+field+r'\s*=\s*(.*?);',resource,re.S);assert assignment,field
        location=re.search(r'new ResourceLocation\([^,]+,\s*"([^"]+)"\)',assignment.group(1)) or re.search(r'new \w+\("([^"]+)"\)',assignment.group(1));assert location,(field,assignment.group(1))
        path=location.group(1).lower();assets[field]=path
        assert (root/'src/main/resources/assets/hbm'/path).is_file(),(field,path);checks+=1
spec=importlib.util.spec_from_file_location('native_core_lifecycle_source',root/'porting/tests/check-native-core-lifecycle-source.py')
history=importlib.util.module_from_spec(spec);spec.loader.exec_module(history)
for path,entry in manifest['files'].items():
    assert hashlib.sha256(history.reverse_source(path,(root/path).read_bytes().decode('utf-8')).encode('utf-8')).hexdigest()==entry['after_sha256'],('unreviewed migration source change',path);checks+=1
assert len(manifest['recipes'])==manifest['effective_count']
assert len({r['owner'] for r in manifest['recipes']})==manifest['effective_count'];checks+=2
files=[src/'render/util/NativeItemBaseModel.java']+list(native.glob('*.java'))+[f for f in family.glob('*.java') if f.name!='StaticItemFamilyBootstrap.java']+[src/'render/item/ItemRenderAngelCore.java',src/'render/mesh/MeshModel.java',src/'render/mesh/ObjMesh.java',src/'render/mesh/ObjMeshParser.java',root/'porting/tests/StaticItemFamilyNativeTest.java']
with tempfile.TemporaryDirectory(prefix='static-item-family-') as tmp:
    data=pathlib.Path(tmp)/'original-recipe-data.json';data.write_text(json.dumps({'recipes':manifest['recipes'],'assets':assets}))
    ext='.exe' if os.name=='nt' else ''
    subprocess.run([str(a.java_home/('bin/javac'+ext)),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
    subprocess.run([str(a.java_home/('bin/java'+ext)),'-Xmx2G','-cp',tmp+os.pathsep+a.classpath,'StaticItemFamilyNativeTest',str(data),str(root)],cwd=tmp,check=True)
print('PASS',checks,'separately labeled original ordered-draw/resource/bootstrap/source guards')
