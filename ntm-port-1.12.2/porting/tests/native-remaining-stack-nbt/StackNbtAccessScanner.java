import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Targeted javac attribution only. Native receiver AND native private field symbol required. */
public final class StackNbtAccessScanner {
  static String q(Object value) {
    if(value==null)return "null";
    return "\""+value.toString().replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";
  }
  public static void main(String[] args)throws Exception {
    JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
    StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,null);
    List<String> paths=Files.readAllLines(Paths.get(args[0]));
    JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-source","8","-encoding","UTF-8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-sourcepath","","-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(paths));
    final Trees trees=Trees.instance(task);final SourcePositions positions=trees.getSourcePositions();
    List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
    for(Diagnostic<?> d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("Parse failure: "+d);
    task.analyze(); // Expected unresolved-port diagnostics; not a successful compile, no class emission.
    for(final CompilationUnitTree unit:units) {
      final String source=unit.getSourceFile().getCharContent(true).toString();
      new TreePathScanner<Void,Void>() {
        public Void visitMemberSelect(MemberSelectTree t,Void v) {
          if(!t.getIdentifier().contentEquals("stackTagCompound"))return super.visitMemberSelect(t,v);
          TreePath path=getCurrentPath(),rp=new TreePath(path,t.getExpression());
          TypeMirror type=trees.getTypeMirror(rp);Element field=trees.getElement(path);
          boolean nativeType=type!=null&&type.getKind()==TypeKind.DECLARED&&((DeclaredType)type).asElement() instanceof TypeElement&&((TypeElement)((DeclaredType)type).asElement()).getQualifiedName().contentEquals("net.minecraft.item.ItemStack");
          boolean nativeField=field instanceof VariableElement&&field.getKind()==ElementKind.FIELD&&field.getEnclosingElement() instanceof TypeElement&&((TypeElement)field.getEnclosingElement()).getQualifiedName().contentEquals("net.minecraft.item.ItemStack");
          TreePath valuePath=path; while(valuePath.getParentPath().getLeaf() instanceof ParenthesizedTree) valuePath=valuePath.getParentPath();
          Tree value=valuePath.getLeaf(),parent=valuePath.getParentPath().getLeaf();String kind="read",reason="";Tree rhs=null;
          if(!nativeType||!nativeField){kind="reject";reason="receiver/field not resolved to native ItemStack";}
          else if(parent instanceof AssignmentTree&&((AssignmentTree)parent).getVariable()==value) {
            if(valuePath.getParentPath().getParentPath().getLeaf() instanceof ExpressionStatementTree){kind="write";rhs=((AssignmentTree)parent).getExpression();}
            else {kind="reject";reason="assignment value is used";}
          } else if((parent instanceof CompoundAssignmentTree&&((CompoundAssignmentTree)parent).getVariable()==value)||parent instanceof UnaryTree) {kind="reject";reason="non-simple write or unary context";}
          long start=positions.getStartPosition(unit,t),end=positions.getEndPosition(unit,t);
          long rs=positions.getStartPosition(unit,t.getExpression()),re=positions.getEndPosition(unit,t.getExpression());
          long as=rhs==null?-1:positions.getStartPosition(unit,parent),ae=rhs==null?-1:positions.getEndPosition(unit,parent),vs=rhs==null?-1:positions.getStartPosition(unit,rhs),ve=rhs==null?-1:positions.getEndPosition(unit,rhs);
          if(start<0||end<0||re<0){kind="reject";reason="no reliable source positions";}
          System.out.println("{\"path\":"+q(Paths.get(unit.getSourceFile().toUri()))+",\"kind\":"+q(kind)+",\"reason\":"+q(reason)+",\"start\":"+start+",\"end\":"+end+",\"receiverStart\":"+rs+",\"receiverEnd\":"+re+",\"assignmentStart\":"+as+",\"assignmentEnd\":"+ae+",\"rhsStart\":"+vs+",\"rhsEnd\":"+ve+",\"receiver\":"+q(t.getExpression())+",\"receiverType\":"+q(type)+",\"fieldOwner\":"+q(field==null?null:field.getEnclosingElement())+",\"line\":"+unit.getLineMap().getLineNumber(start)+"}");
          return super.visitMemberSelect(t,v);
        }
      }.scan(unit,null);
    }
    long errors=diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count();
    System.err.println("Targeted attribution diagnostics (NOT a successful compile): "+errors);
    fm.close();
  }
}
