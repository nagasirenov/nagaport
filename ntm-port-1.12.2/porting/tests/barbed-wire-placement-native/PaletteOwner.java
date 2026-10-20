import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.nio.file.*;
import java.util.*;
/** Native javac attribution. Unresolved methods do not establish ownership. */
public final class PaletteOwner {
 static String q(String s){return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";}
 public static void main(String[] args)throws Exception {
  JavaCompiler c=ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> ds=new DiagnosticCollector<>(); StandardJavaFileManager fm=c.getStandardFileManager(ds,null,null);
  JavacTask task=(JavacTask)c.getTask(null,fm,ds,Arrays.asList("-proc:none","-source","8","-classpath",args[0],"-sourcepath","","-Xmaxerrs","100000"),null,fm.getJavaFileObjects(Arrays.copyOfRange(args,1,args.length)));
  Trees trees=Trees.instance(task); List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> d:ds.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError(d.toString());
  task.analyze();
  for(CompilationUnitTree u:units){String source=u.getSourceFile().getCharContent(true).toString();SourcePositions positions=trees.getSourcePositions();
   new TreePathScanner<Void,Void>() {
    String raw(Tree t){return source.substring((int)positions.getStartPosition(u,t),(int)positions.getEndPosition(u,t));}
    public Void visitMethodInvocation(MethodInvocationTree t,Void p){
     if(!(t.getMethodSelect() instanceof MemberSelectTree))return super.visitMethodInvocation(t,p);
     MemberSelectTree m=(MemberSelectTree)t.getMethodSelect(); if(!m.getIdentifier().contentEquals("setBlock"))return super.visitMethodInvocation(t,p);
     List<? extends ExpressionTree> a=t.getArguments(); if(a.size()!=4&&a.size()!=6)return super.visitMethodInvocation(t,p);
     TypeMirror recv=trees.getTypeMirror(new TreePath(getCurrentPath(),m.getExpression()));
     Element block=trees.getElement(new TreePath(getCurrentPath(),a.get(3)));
     boolean owner=recv!=null&&recv.getKind()==TypeKind.DECLARED&&recv.toString().equals("net.minecraft.world.World");
     boolean vanilla=block instanceof VariableElement && block.getKind()==ElementKind.FIELD && block.asType().toString().equals("net.minecraft.block.Block") && block.getEnclosingElement().toString().equals("com.hbm.blocks.ModBlocks") && block.getSimpleName().contentEquals("barbed_wire");
     boolean ints=true;for(int i:new int[]{0,1,2,4,5})if(i<a.size()){TypeMirror ty=trees.getTypeMirror(new TreePath(getCurrentPath(),a.get(i)));ints&=ty!=null&&ty.getKind()==TypeKind.INT;}
     TreePath enclosing=getCurrentPath();while(enclosing!=null&&!(enclosing.getLeaf() instanceof MethodTree))enclosing=enclosing.getParentPath();String method=enclosing==null?"":((MethodTree)enclosing.getLeaf()).getName().toString();
     System.out.print("{\"method\":"+q(method)+",\"file\":"+q(Paths.get(u.getSourceFile().toUri()).toString())+",\"start\":"+positions.getStartPosition(u,t)+",\"end\":"+positions.getEndPosition(u,t)+",\"receiver\":"+q(raw(m.getExpression()))+",\"world\":"+owner+",\"vanilla\":"+vanilla+",\"ints\":"+ints+",\"statement\":"+(getCurrentPath().getParentPath().getLeaf() instanceof ExpressionStatementTree)+",\"block\":"+q(vanilla?block.getSimpleName().toString():"")+",\"args\":[");
     boolean first=true;for(ExpressionTree e:a){if(!first)System.out.print(',');first=false;System.out.print(q(raw(e)));}System.out.println("]}");
     return super.visitMethodInvocation(t,p);
    }
   }.scan(u,null);
  }
  System.err.println("Expected unresolved-port attribution diagnostics: "+ds.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
