import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Reports positions only for statically resolved native class qualifiers; no edits. */
public final class AttributeOwners {
 public static void main(String[] args) throws Exception {
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();
  StandardJavaFileManager fm=c.getStandardFileManager(null,null,null);
  DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
  JavacTask task=(JavacTask)c.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-classpath",args[0],"-sourcepath","","-Xmaxerrs","10000"),null,fm.getJavaFileObjects(Arrays.copyOfRange(args,1,args.length)));
  List<CompilationUnitTree> units=new ArrayList<>(); for(CompilationUnitTree u:task.parse()) units.add(u);
  task.analyze(); Trees trees=Trees.instance(task);
  for(final CompilationUnitTree u:units) new TreePathScanner<Void,Void>() {
   public Void visitImport(ImportTree n,Void p) { return null; }
   public Void visitMemberSelect(MemberSelectTree n,Void p) {
    String s=n.getIdentifier().toString();
    if(s.equals("followRange")||s.equals("knockbackResistance")) {
     Element owner=trees.getElement(new TreePath(getCurrentPath(),n.getExpression()));
     if(owner instanceof TypeElement && owner.getKind()==ElementKind.CLASS && ((TypeElement)owner).getQualifiedName().contentEquals("net.minecraft.entity.SharedMonsterAttributes")) {
      long end=trees.getSourcePositions().getEndPosition(u,n);
      System.out.println(u.getSourceFile().toUri().getPath()+"\t"+(end-s.length())+"\t"+end+"\t"+s);
     }
    } return super.visitMemberSelect(n,p);
   }
  }.scan(u,null);
  fm.close();
 }
}
