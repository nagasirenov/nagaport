from pathlib import Path
import argparse,hashlib,json,re,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);a=p.parse_args();r=a.module
api=r/'src/api/java/invtweaks/api/container/ChestContainer.java'
names=['ContainerCrateIron','ContainerCrateSteel','ContainerCrateTungsten','ContainerCrateDesh','ContainerSafe','ContainerCrateTemplate'];rows=[9,9,9,13,5,9]
with tempfile.TemporaryDirectory(prefix='invtweaks-api-') as d:
 t=Path(d);files=[str(api)]; declarations=[]
 for name,row in zip(names,rows):
  source=(r/'src/main/java/com/hbm/inventory/container'/f'{name}.java').read_text();marker=re.search(r'@invtweaks\.api\.container\.ChestContainer\([^\n]+\)',source).group();assert marker==f'@invtweaks.api.container.ChestContainer(rowSize = {row}, isLargeChest = false)';declarations.append(marker+' class '+name+' {}')
 java='''import invtweaks.api.container.ChestContainer;
public class ApiMetadataProbe {
 public static void main(String[] args)throws Exception {
  Class<?>[] owners={'''+','.join(x+'.class' for x in names)+'''};int[] rows={9,9,9,13,5,9};int checks=0;
  for(int i=0;i<owners.length;i++){ChestContainer a=owners[i].getAnnotation(ChestContainer.class);if(a==null||a.rowSize()!=rows[i]||a.isLargeChest()||!a.showButtons())throw new AssertionError(owners[i]);checks+=4;}
  if(ChestContainer.class.getAnnotation(java.lang.annotation.Retention.class).value()!=java.lang.annotation.RetentionPolicy.RUNTIME)throw new AssertionError();checks++;
  for(Class<?> c:new Class<?>[]{ChestContainer.RowSizeCallback.class,ChestContainer.IsLargeCallback.class}){if(c.getAnnotation(java.lang.annotation.Retention.class).value()!=java.lang.annotation.RetentionPolicy.RUNTIME||c.getAnnotation(java.lang.annotation.Target.class).value()[0]!=java.lang.annotation.ElementType.METHOD)throw new AssertionError();checks+=2;}
  System.out.println("PASS "+checks+" actual official annotation metadata checks; extracted markers, not complete containers");
 }
}
'''+ '\n'.join(declarations)
 f=t/'ApiMetadataProbe.java';f.write_text(java);files.append(str(f));subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-d',d]+files,check=True);subprocess.run([str(a.java_home/'bin/java'),'-cp',d,'ApiMetadataProbe'],check=True)
print('Official API SHA256',hashlib.sha256(api.read_bytes()).hexdigest())
