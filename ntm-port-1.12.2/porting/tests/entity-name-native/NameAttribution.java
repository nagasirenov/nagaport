import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Javac parse/analyze only, no code generation or build. Retains unresolved calls honestly. */
public final class NameAttribution {
 static String esc(Object o){return String.valueOf(o).replace("\\","\\\\").replace("\t","\\t").replace("\r","\\r").replace("\n","\\n");}
 static void row(PrintWriter out,Object... fields){List<String>a=new ArrayList<>();for(Object f:fields)a.add(esc(f));out.println(String.join("\t",a));}
 static String text(CompilationUnitTree u,Tree t,SourcePositions p,String s){long a=p.getStartPosition(u,t),b=p.getEndPosition(u,t);return a<0||b<a||b>s.length()?t.toString():s.substring((int)a,(int)b);}
 public static void main(String[]args)throws Exception {
  Path root=Paths.get(args[0]).toAbsolutePath(),out=Paths.get(args[2]);Files.createDirectories(out);
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,StandardCharsets.UTF_8);
  List<String> files=new ArrayList<>();try(java.util.stream.Stream<Path>w=Files.walk(root.resolve("src/main/java"))){w.filter(p->p.toString().endsWith(".java")).sorted().forEach(p->files.add(p.toString()));}
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-encoding","UTF-8","-source","8","-Xmaxerrs","100000","-classpath",new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8).trim()),null,fm.getJavaFileObjectsFromStrings(files));
  Trees trees=Trees.instance(task);SourcePositions positions=trees.getSourcePositions();List<CompilationUnitTree>units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  long parseErrors=diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count();if(parseErrors!=0)throw new AssertionError("parse errors "+parseErrors);task.analyze();
  Set<String> names=new HashSet<>(Arrays.asList("getCommandSenderName","getName"));
  try(PrintWriter calls=new PrintWriter(out.resolve("calls.tsv").toFile(),"UTF-8");PrintWriter errs=new PrintWriter(out.resolve("errors.tsv").toFile(),"UTF-8")) {
   row(calls,"path","line","receiver","receiver_type","receiver_kind","method_owner","method_signature","method_kind","arguments","source");
   for(final CompilationUnitTree unit:units){final String path=root.relativize(Paths.get(unit.getSourceFile().toUri())).toString(),source=unit.getSourceFile().getCharContent(true).toString();new TreePathScanner<Void,Void>() {
    public Void visitMethodInvocation(MethodInvocationTree t,Void ignored){Tree sel=t.getMethodSelect();String name=sel instanceof MemberSelectTree?((MemberSelectTree)sel).getIdentifier().toString():sel.toString();if(names.contains(name)){
     TreePath sp=new TreePath(getCurrentPath(),sel);Element m=trees.getElement(sp);String receiver="<implicit>",rt="",rk="";
     if(sel instanceof MemberSelectTree){ExpressionTree r=((MemberSelectTree)sel).getExpression();TypeMirror type=trees.getTypeMirror(new TreePath(sp,r));receiver=text(unit,r,positions,source);rt=String.valueOf(type);rk=type==null?"null":type.getKind().toString();}
     List<String>as=new ArrayList<>();for(ExpressionTree a:t.getArguments())as.add(text(unit,a,positions,source));row(calls,path,unit.getLineMap().getLineNumber(positions.getStartPosition(unit,t)),receiver,rt,rk,m==null?"null":m.getEnclosingElement(),m,m==null?"null":m.getKind(),String.join(" || ",as),text(unit,t,positions,source));
    }return super.visitMethodInvocation(t,ignored);}
   }.scan(unit,null);}
   for(Diagnostic<? extends JavaFileObject>d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)row(errs,d.getSource()==null?"null":root.relativize(Paths.get(d.getSource().toUri())),d.getLineNumber(),d.getCode(),d.getMessage(Locale.ROOT));
  }
  System.out.println("Parsed "+units.size()+" units; parse errors=0; analyzed errors="+diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count()+"; code generation not invoked.");fm.close();
 }
}
