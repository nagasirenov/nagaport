import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Only discarded native InventoryPlayer main/armor writes; no lookalike names. */
public final class InventoryWriteScanner {
 static String q(Object o) { if(o==null)return "null"; return "\""+o.toString().replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\""; }
 public static void main(String[] args)throws Exception {
  JavaCompiler c=ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> ds=new DiagnosticCollector<>();
  StandardJavaFileManager fm=c.getStandardFileManager(ds,null,null);
  JavacTask task=(JavacTask)c.getTask(null,fm,ds,Arrays.asList("-proc:none","-source","8","-encoding","UTF-8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-sourcepath","","-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  final Trees trees=Trees.instance(task); final SourcePositions pos=trees.getSourcePositions(); List<CompilationUnitTree> units=new ArrayList<>();
  for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> d:ds.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure "+d);
  task.analyze();
  for(final CompilationUnitTree u:units)new TreePathScanner<Void,Void>() {
   boolean field(Tree t) {
    if(!(t instanceof MemberSelectTree))return false;
    MemberSelectTree m=(MemberSelectTree)t; Element e=trees.getElement(TreePath.getPath(u,m));
    return e instanceof VariableElement && e.getEnclosingElement().toString().equals("net.minecraft.entity.player.InventoryPlayer")
     && (e.getSimpleName().contentEquals("mainInventory")||e.getSimpleName().contentEquals("armorInventory"))
     && e.asType().toString().equals("net.minecraft.util.NonNullList<net.minecraft.item.ItemStack>") && e.getModifiers().contains(Modifier.FINAL);
   }
   boolean discarded(){return getCurrentPath().getParentPath().getLeaf() instanceof ExpressionStatementTree;}
   boolean safeRhs(ExpressionTree rhs) {
    if(rhs.getKind()==Tree.Kind.NULL_LITERAL)return true;
    if(rhs instanceof NewClassTree){NewClassTree ctor=(NewClassTree)rhs;Element type=trees.getElement(TreePath.getPath(u,ctor.getIdentifier()));return ctor.getClassBody()==null && type instanceof TypeElement && ((TypeElement)type).getQualifiedName().contentEquals("net.minecraft.item.ItemStack");}
    TypeMirror type=trees.getTypeMirror(TreePath.getPath(u,rhs));
    if(type==null || !type.toString().equals("net.minecraft.item.ItemStack"))return false;
    if(rhs instanceof MethodInvocationTree){MethodInvocationTree call=(MethodInvocationTree)rhs;Element e=trees.getElement(TreePath.getPath(u,call.getMethodSelect()));
     return call.getArguments().isEmpty() && e instanceof ExecutableElement && e.getEnclosingElement().toString().equals("net.minecraft.item.ItemStack") && e.getSimpleName().contentEquals("copy");
    }return false;
   }
   void emit(String mode,Tree call,Tree list,Tree index,Tree rhs,boolean accepted) {
    Element owner=trees.getElement(TreePath.getPath(u,list));
    String text;try{text=u.getSourceFile().getCharContent(true).toString();}catch(Exception ex){throw new RuntimeException(ex);}
    long start=pos.getStartPosition(u,call),end=pos.getEndPosition(u,call);
    Element copy=rhs instanceof MethodInvocationTree?trees.getElement(TreePath.getPath(u,((MethodInvocationTree)rhs).getMethodSelect())):null;
    TypeMirror rt=trees.getTypeMirror(TreePath.getPath(u,rhs));
    System.out.println("{\"path\":"+q(Paths.get(u.getSourceFile().toUri()))+",\"mode\":"+q(mode)+",\"accept\":"+accepted+",\"start\":"+start+",\"end\":"+end+",\"line\":"+u.getLineMap().getLineNumber(start)+",\"owner\":"+q(owner==null?null:owner.getEnclosingElement())+",\"field\":"+q(owner==null?null:owner.getSimpleName())+",\"list_start\":"+pos.getStartPosition(u,list)+",\"list_end\":"+pos.getEndPosition(u,list)+",\"index_start\":"+pos.getStartPosition(u,index)+",\"index_end\":"+pos.getEndPosition(u,index)+",\"rhs_start\":"+pos.getStartPosition(u,rhs)+",\"rhs_end\":"+pos.getEndPosition(u,rhs)+",\"rhs_kind\":"+q(rhs.getKind())+",\"rhs_type\":"+q(rt)+",\"rhs_method_owner\":"+q(copy==null?null:copy.getEnclosingElement())+",\"discarded\":"+discarded()+",\"source\":"+q(text.substring((int)start,(int)end))+"}");
   }
   public Void visitAssignment(AssignmentTree a,Void unused) {
    if(a.getVariable() instanceof ArrayAccessTree){ArrayAccessTree aa=(ArrayAccessTree)a.getVariable();
     if(aa.getExpression() instanceof MemberSelectTree){String name=((MemberSelectTree)aa.getExpression()).getIdentifier().toString();
      if(name.equals("mainInventory")||name.equals("armorInventory"))emit("legacy",a,aa.getExpression(),aa.getIndex(),a.getExpression(),field(aa.getExpression())&&discarded()&&safeRhs(a.getExpression()));
     }
    }return super.visitAssignment(a,unused);
   }
   public Void visitMethodInvocation(MethodInvocationTree a,Void unused) {
    if(a.getMethodSelect() instanceof MemberSelectTree){MemberSelectTree m=(MemberSelectTree)a.getMethodSelect();
     if(m.getIdentifier().contentEquals("set") && a.getArguments().size()==2 && field(m.getExpression())) {
      Element method=trees.getElement(TreePath.getPath(u,m));
      boolean nativeMethod=method instanceof ExecutableElement && method.getEnclosingElement().toString().equals("net.minecraft.util.NonNullList") && method.getSimpleName().contentEquals("set");
      emit("native",a,m.getExpression(),a.getArguments().get(0),a.getArguments().get(1),nativeMethod&&discarded());
     }
    }return super.visitMethodInvocation(a,unused);
   }
  }.scan(u,null);
  for(Diagnostic<? extends JavaFileObject> d:ds.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)System.err.println("ERROR\t"+(d.getSource()==null?"":Paths.get(d.getSource().toUri()))+"\t"+d.getPosition()+"\t"+d.getCode()+"\t"+d.getMessage(Locale.ROOT).replace('\n',' '));
  System.err.println("Attribution only; no class generation. Errors: "+ds.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count()); fm.close();
 }
}
