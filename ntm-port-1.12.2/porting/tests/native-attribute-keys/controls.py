#!/usr/bin/env python3
import pathlib,subprocess,tempfile,sys,os
h=pathlib.Path(__file__).resolve().parent;j=pathlib.Path(sys.argv[1]);cp=pathlib.Path(sys.argv[2]);tools=str(j/'lib/tools.jar')
cases=[
 ('import net.minecraft.entity.SharedMonsterAttributes; class T {String a=SharedMonsterAttributes.maxHealth.getAttributeUnlocalizedName();String b=SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName();String c=SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName();}',6),
 ('class T {String x=net.minecraft.entity.SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName();}',2),
 ('class T {static class SharedMonsterAttributes {static A maxHealth;}static class A{String getAttributeUnlocalizedName(){return "";}}String x=SharedMonsterAttributes.maxHealth.getAttributeUnlocalizedName();}',0),
 ('import net.minecraft.entity.SharedMonsterAttributes; class T {String x(SharedMonsterAttributes SharedMonsterAttributes){return SharedMonsterAttributes.maxHealth.getAttributeUnlocalizedName();}}',0),
 ('class T {Object x=SharedMonsterAttributes.maxHealth.getAttributeUnlocalizedName();}',0),
 ('class T {String x="SharedMonsterAttributes.maxHealth.getAttributeUnlocalizedName()";/* SharedMonsterAttributes.maxHealth.getAttributeUnlocalizedName() */}',0),
 ('import static net.minecraft.entity.SharedMonsterAttributes.maxHealth;class T{Object x=maxHealth.getAttributeUnlocalizedName();}',0),
 ('import net.minecraft.entity.SharedMonsterAttributes; class T {Object x=SharedMonsterAttributes.maxHealth;}',0),
 ('import net.minecraft.entity.SharedMonsterAttributes; class T {Object x=SharedMonsterAttributes.maxHealth.getAttributeUnlocalizedName(1);}',0),
 ('import net.minecraft.entity.SharedMonsterAttributes; class T {Object x=(SharedMonsterAttributes.maxHealth).getAttributeUnlocalizedName();}',0),
 ('import net.minecraft.entity.SharedMonsterAttributes; class T {Object x=SharedMonsterAttributes.MAX_HEALTH.getAttributeUnlocalizedName();}',0),
 ('import net.minecraft.entity.SharedMonsterAttributes; class T {void x(net.minecraft.entity.ai.attributes.AttributeMap m){m.registerAttribute(SharedMonsterAttributes.maxHealth);}}',0),
 ('import net.minecraft.entity.SharedMonsterAttributes; class T {void x(net.minecraft.entity.EntityLivingBase e){e.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15000);}}',0),
 ('import net.minecraft.entity.SharedMonsterAttributes; class T {Object x=SharedMonsterAttributes.movementSpeed.getDefaultValue();}',0),
]
with tempfile.TemporaryDirectory(prefix='attribute-key-controls-') as td:
 d=pathlib.Path(td);subprocess.run([str(j/'bin/javac'),'-cp',tools,'-d',str(d),str(h/'AttributeKeyScanner.java')],check=True)
 paths=[]
 for i,(src,n) in enumerate(cases):
  path=d/'src/main/java'/('Control'+str(i)+'.java');path.parent.mkdir(parents=True,exist_ok=True);path.write_text(__import__('re').sub(r'\bT\b','Control'+str(i),src));paths.append(str(path))
 inp=d/'inputs';inp.write_text('\n'.join(paths));result=subprocess.run([str(j/'bin/java'),'-cp',str(d)+os.pathsep+tools,'AttributeKeyScanner',str(inp),str(cp)],text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=True);rows=result.stdout.splitlines()
 for i,(src,n) in enumerate(cases):assert len([r for r in rows if r.startswith('src/main/java/Control'+str(i)+'.java\t')])==n,(i,rows,result.stderr)
 print('PASS 14 native owner, qualified/variable/shadow/unresolved/import/text/arity/registration/value exclusion controls')
