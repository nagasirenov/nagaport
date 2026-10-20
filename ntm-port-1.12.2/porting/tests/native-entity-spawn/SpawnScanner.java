import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Exact World receiver + native vanilla EntityItem construction only; no name-based receiver guessing. */
public final class SpawnScanner {
 public static void main(String[] a)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  Trees trees=Trees.instance(task);SourcePositions pos=trees.getSourcePositions();List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+e);
  task.analyze();String name=a.length>2?"spawnEntity":"spawnEntityInWorld";
  for(CompilationUnitTree u:units)new TreePathScanner<Void,Void>(){
   public Void visitMethodInvocation(MethodInvocationTree t,Void v){
    if(t.getMethodSelect() instanceof MemberSelectTree&&t.getArguments().size()==1&&t.getArguments().get(0) instanceof NewClassTree){
     MemberSelectTree m=(MemberSelectTree)t.getMethodSelect();NewClassTree n=(NewClassTree)t.getArguments().get(0);
     if(m.getIdentifier().contentEquals(name)&&n.getClassBody()==null){
      TreePath mp=new TreePath(getCurrentPath(),m),rp=new TreePath(mp,m.getExpression()),np=new TreePath(getCurrentPath(),n);
      TypeMirror r=trees.getTypeMirror(rp);Element re=trees.getElement(rp),ctor=trees.getElement(np);
      boolean ok=r!=null&&r.getKind()==TypeKind.DECLARED&&r.toString().equals("net.minecraft.world.World")&&re instanceof VariableElement;
      ok &= ctor instanceof ExecutableElement&&ctor.getEnclosingElement().toString().equals("net.minecraft.entity.item.EntityItem")&&ctor.toString().equals("EntityItem(net.minecraft.world.World,double,double,double,net.minecraft.item.ItemStack)");
      if(a.length>2){Element e=trees.getElement(mp);ok &= e instanceof ExecutableElement&&e.getEnclosingElement().toString().equals("net.minecraft.world.World");}
      if(ok){long end=pos.getEndPosition(u,m);System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+(end-name.length())+"\t"+end+"\t"+r+"\t"+ctor);}
     }
    }return super.visitMethodInvocation(t,v);
   }
  }.scan(u,null);
  System.err.println("Attribution diagnostics, not full compilation: "+errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
