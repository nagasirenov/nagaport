import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Attribution of every private widget reference within the two actual nested GUI owners. */
public class NestedFieldScanner {
 public static void main(String[] args)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",args[1],"-Xmaxerrs","100000"),null,fm.getJavaFileObjects(args[0]));Trees trees=Trees.instance(task);List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);task.analyze();
  for(CompilationUnitTree u:units)new TreePathScanner<Void,Void>() {
   boolean widget(Element e){return e!=null&&e.getKind()==ElementKind.FIELD&&e.asType().toString().equals("net.minecraft.client.gui.GuiTextField")&&(e.getEnclosingElement().getSimpleName().contentEquals("GuiStructureSave")||e.getEnclosingElement().getSimpleName().contentEquals("GuiStructureLoad"));}
   String method(){for(TreePath p=getCurrentPath();p!=null;p=p.getParentPath())if(p.getLeaf() instanceof MethodTree)return ((MethodTree)p.getLeaf()).getName().toString();return "<none>";}
   public Void visitVariable(VariableTree n,Void v){Element e=trees.getElement(getCurrentPath());if(widget(e))System.out.println("FIELD\t"+e.getEnclosingElement().getSimpleName()+"\t"+e.getSimpleName()+"\t"+e.getModifiers());return super.visitVariable(n,v);}
   void use(Tree node){Element e=trees.getElement(getCurrentPath());if(!widget(e))return;TreePath p=getCurrentPath().getParentPath();String kind="ESCAPE",target="";
    if(p.getLeaf() instanceof AssignmentTree&&((AssignmentTree)p.getLeaf()).getVariable()==node){kind="ASSIGN";target=((AssignmentTree)p.getLeaf()).getExpression().toString();}
    else if(p.getLeaf() instanceof MemberSelectTree&&((MemberSelectTree)p.getLeaf()).getExpression()==node&&p.getParentPath().getLeaf() instanceof MethodInvocationTree){Element called=trees.getElement(p.getParentPath());kind="CALL";target=(called==null?"unresolved":called.getEnclosingElement()+":"+called);}
    System.out.println("USE\t"+e.getEnclosingElement().getSimpleName()+"\t"+e.getSimpleName()+"\t"+method()+"\t"+kind+"\t"+target);
   }
   public Void visitIdentifier(IdentifierTree n,Void v){use(n);return super.visitIdentifier(n,v);}
   public Void visitMemberSelect(MemberSelectTree n,Void v){use(n);return super.visitMemberSelect(n,v);}
  }.scan(u,null);
  System.err.println("Single-source attribution errors from unrelated block/tile APIs: "+errors.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count()+" (not whole-owner compilation)");
 }
}
