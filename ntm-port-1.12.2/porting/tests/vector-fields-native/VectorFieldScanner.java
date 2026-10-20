import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Select only proven Entity descendants without either legacy or destination field shadows. */
public final class VectorFieldScanner {
 public static void main(String[] a)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  final Trees trees=Trees.instance(task);final SourcePositions pos=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+e);
  task.analyze();final Types types=task.getTypes();
  for(final CompilationUnitTree u:units){final String src=u.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>(){
    boolean vector(TypeMirror type){
     if(type==null||type.getKind()!=TypeKind.DECLARED)return false;
     TypeElement e=(TypeElement)((DeclaredType)type).asElement();
     if(!e.getQualifiedName().contentEquals("net.minecraft.util.math.Vec3d"))return false;
     for(String name:Arrays.asList("x","y","z")){
      boolean ok=false;for(Element f:e.getEnclosedElements())if(f.getKind()==ElementKind.FIELD&&f.getSimpleName().contentEquals(name))ok=f.asType().getKind()==TypeKind.DOUBLE&&f.getModifiers().containsAll(Arrays.asList(Modifier.PUBLIC,Modifier.FINAL));
      if(!ok)return false;
     }return true;
    }
    boolean write(){Tree leaf=getCurrentPath().getLeaf();TreePath p=getCurrentPath().getParentPath();
     while(p!=null&&p.getLeaf() instanceof ParenthesizedTree){leaf=p.getLeaf();p=p.getParentPath();}
     if(p==null)return false;Tree t=p.getLeaf();
     if(t instanceof AssignmentTree)return ((AssignmentTree)t).getVariable()==leaf;
     if(t instanceof CompoundAssignmentTree)return ((CompoundAssignmentTree)t).getVariable()==leaf;
     return t instanceof UnaryTree&&Arrays.asList(Tree.Kind.PREFIX_INCREMENT,Tree.Kind.PREFIX_DECREMENT,Tree.Kind.POSTFIX_INCREMENT,Tree.Kind.POSTFIX_DECREMENT).contains(t.getKind());
    }
    public Void visitImport(ImportTree t,Void v){return null;}
    public Void visitMemberSelect(MemberSelectTree t,Void v){String old=t.getIdentifier().toString();if(Arrays.asList("xCoord","yCoord","zCoord").contains(old)&&!write()){
     TreePath p=new TreePath(getCurrentPath(),t.getExpression());TypeMirror receiver=trees.getTypeMirror(p);Element e=trees.getElement(p);
     if(!(e instanceof TypeElement)&&vector(receiver)){long end=pos.getEndPosition(u,t),start=end-old.length();if(start>=0&&end<=src.length()&&src.substring((int)start,(int)end).equals(old))System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+old.substring(0,1)+"\t"+receiver+"\t"+old);}
    }return super.visitMemberSelect(t,v);}
   }.scan(u,null);
  }
  System.err.println("Attribution diagnostics (unfinished source, not compile success): "+errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
