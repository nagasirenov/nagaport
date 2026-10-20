import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
public class MathCallScanner {
 public static void main(String[] a)throws Exception {
  Map<String,String[]> map=new HashMap<>();for(String l:Files.readAllLines(Paths.get(a[0]))){String[] r=l.split("\t");map.put(r[0],r);}
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> ds=new DiagnosticCollector<>();StandardJavaFileManager fm=c.getStandardFileManager(ds,null,null);
  JavacTask task=(JavacTask)c.getTask(null,fm,ds,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[2]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[1]))));
  Trees trees=Trees.instance(task);SourcePositions p=trees.getSourcePositions();List<CompilationUnitTree> us=new ArrayList<>();for(CompilationUnitTree u:task.parse())us.add(u);for(Diagnostic<?> d:ds.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError(d);task.analyze();
  for(CompilationUnitTree u:us){String source=u.getSourceFile().getCharContent(true).toString();Set<String> shadows=new HashSet<>();new TreeScanner<Void,Void>(){public Void visitVariable(VariableTree t,Void v){shadows.add(t.getName().toString());return super.visitVariable(t,v);}public Void visitClass(ClassTree t,Void v){shadows.add(t.getSimpleName().toString());return super.visitClass(t,v);}public Void visitTypeParameter(TypeParameterTree t,Void v){shadows.add(t.getName().toString());return super.visitTypeParameter(t,v);}}.scan(u,null);
   new TreePathScanner<Void,Void>(){public Void visitMethodInvocation(MethodInvocationTree t,Void v){
    if(t.getMethodSelect() instanceof MemberSelectTree){MemberSelectTree s=(MemberSelectTree)t.getMethodSelect();String old=s.getIdentifier().toString();String[] r=map.get(old);String receiver=s.getExpression().toString();TreePath sp=new TreePath(getCurrentPath(),s);Element e=trees.getElement(new TreePath(sp,s.getExpression()));
     if(r!=null&&e instanceof TypeElement&&((TypeElement)e).getQualifiedName().contentEquals("net.minecraft.util.math.MathHelper")&&((receiver.equals("MathHelper")&&!shadows.contains("MathHelper"))||(receiver.equals("net.minecraft.util.math.MathHelper")&&!shadows.contains("net")))) {
      long end=p.getEndPosition(u,s),start=end-old.length();String args="";boolean valid=t.getArguments().size()==r[3].indexOf(')')-1;
      int i=0;for(ExpressionTree arg:t.getArguments()){TypeMirror ty=trees.getTypeMirror(new TreePath(getCurrentPath(),arg));String k=ty==null?"ERROR":ty.getKind().name();char want=r[3].charAt(++i);boolean ok=(want=='D'&&Arrays.asList("BYTE","SHORT","CHAR","INT","LONG","FLOAT","DOUBLE").contains(k))||(want=='F'&&Arrays.asList("BYTE","SHORT","CHAR","INT","LONG","FLOAT").contains(k))||(want=='I'&&Arrays.asList("BYTE","SHORT","CHAR","INT").contains(k));valid&=ok;args+="\t"+p.getStartPosition(u,arg)+":"+p.getEndPosition(u,arg)+":"+k+":"+want;}
      if(valid&&start>=0&&source.substring((int)start,(int)end).equals(old))System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+old+"\t"+r[1]+args);
     }
    }return super.visitMethodInvocation(t,v);}
   }.scan(u,null);
  }System.err.println("Expected unfinished-port attribution diagnostics: "+ds.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
