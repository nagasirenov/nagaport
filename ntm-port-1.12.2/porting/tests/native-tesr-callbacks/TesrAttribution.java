import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Independent whole-source hierarchy, method and call audit, including honest ERROR attribution. */
public final class TesrAttribution {
 static String esc(Object o){return String.valueOf(o).replace("\\","\\\\").replace("\t","\\t").replace("\r","\\r").replace("\n","\\n");}
 static void row(PrintWriter out,Object... fields){List<String> a=new ArrayList<>();for(Object f:fields)a.add(esc(f));out.println(String.join("\t",a));}
 static String text(CompilationUnitTree u,Tree t,SourcePositions p,String s){long a=p.getStartPosition(u,t),b=p.getEndPosition(u,t);return a<0||b<a||b>s.length()?t.toString():s.substring((int)a,(int)b);}
 public static void main(String[] args)throws Exception{
  Path root=Paths.get(args[0]).toAbsolutePath(),out=Paths.get(args[2]);Files.createDirectories(out);
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,StandardCharsets.UTF_8);
  List<String> files=new ArrayList<>();try(java.util.stream.Stream<Path> w=Files.walk(root.resolve("src/main/java"))){w.filter(p->p.toString().endsWith(".java")).sorted().forEach(p->files.add(p.toString()));}
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-encoding","UTF-8","-source","8","-Xmaxerrs","100000","-classpath",new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8).trim()),null,fm.getJavaFileObjectsFromStrings(files));
  Trees trees=Trees.instance(task);SourcePositions positions=trees.getSourcePositions();List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  long errors=diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count();if(errors!=0)throw new AssertionError("parse errors "+errors);task.analyze();
  Elements elements=task.getElements();Types types=task.getTypes();TypeElement block=elements.getTypeElement("net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer");ExecutableElement opacity=null;
  for(Element e:block.getEnclosedElements())if(e.getKind()==ElementKind.METHOD&&e.getSimpleName().contentEquals("render")&&((ExecutableElement)e).getParameters().size()==7)opacity=(ExecutableElement)e;
  if(opacity==null)throw new AssertionError("Missing genuine native target");final ExecutableElement target=opacity;
  try(PrintWriter classes=new PrintWriter(out.resolve("classes.tsv").toFile(),"UTF-8");PrintWriter methods=new PrintWriter(out.resolve("methods.tsv").toFile(),"UTF-8");PrintWriter calls=new PrintWriter(out.resolve("calls.tsv").toFile(),"UTF-8");PrintWriter errs=new PrintWriter(out.resolve("errors.tsv").toFile(),"UTF-8")){
   row(classes,"path","line","owner","superclass","superkind","tesr_subtype","interfaces");row(methods,"path","line","owner","signature","native_override","parameters","parameter_kinds","body","source");row(calls,"path","line","receiver","receiver_type","receiver_kind","method_owner","method_signature","method_kind","arguments","source");
   for(final CompilationUnitTree unit:units){final String path=root.relativize(Paths.get(unit.getSourceFile().toUri())).toString(),source=unit.getSourceFile().getCharContent(true).toString();new TreePathScanner<Void,Void>(){
    long line(Tree t){return unit.getLineMap().getLineNumber(positions.getStartPosition(unit,t));}
    public Void visitClass(ClassTree t,Void unused){Element e=trees.getElement(getCurrentPath());if(e instanceof TypeElement){TypeElement c=(TypeElement)e;row(classes,path,line(t),c.getQualifiedName(),c.getSuperclass(),c.getSuperclass().getKind(),types.isSubtype(types.erasure(c.asType()),types.erasure(block.asType())),c.getInterfaces());}return super.visitClass(t,unused);}
    public Void visitMethod(MethodTree t,Void unused){if(t.getName().contentEquals("render") || t.getName().contentEquals("renderTileEntityAt")){Element e=trees.getElement(getCurrentPath());if(!(e instanceof ExecutableElement))throw new AssertionError("Missing method");ExecutableElement m=(ExecutableElement)e;TypeElement c=(TypeElement)m.getEnclosingElement();List<String> p=new ArrayList<>(),k=new ArrayList<>();for(VariableElement v:m.getParameters()){p.add(v.asType().toString());k.add(v.asType().getKind().toString());}row(methods,path,line(t),c.getQualifiedName(),m,elements.overrides(m,target,c),String.join(" || ",p),String.join(" || ",k),t.getBody()==null?"null":text(unit,t.getBody(),positions,source),text(unit,t,positions,source));}return super.visitMethod(t,unused);}
    public Void visitMethodInvocation(MethodInvocationTree t,Void unused){Tree sel=t.getMethodSelect();String name=sel instanceof MemberSelectTree?((MemberSelectTree)sel).getIdentifier().toString():sel.toString();if(name.equals("render") || name.equals("renderTileEntityAt")){TreePath sp=new TreePath(getCurrentPath(),sel);Element m=trees.getElement(sp);String receiver="<implicit>",rt="",rk="";if(sel instanceof MemberSelectTree){ExpressionTree r=((MemberSelectTree)sel).getExpression();TypeMirror type=trees.getTypeMirror(new TreePath(sp,r));receiver=text(unit,r,positions,source);rt=String.valueOf(type);rk=type==null?"null":type.getKind().toString();}List<String> as=new ArrayList<>();for(ExpressionTree a:t.getArguments())as.add(text(unit,a,positions,source));row(calls,path,line(t),receiver,rt,rk,m==null?"null":m.getEnclosingElement(),m,m==null?"null":m.getKind(),String.join(" || ",as),text(unit,t,positions,source));}return super.visitMethodInvocation(t,unused);}
   }.scan(unit,null);}
   for(Diagnostic<? extends JavaFileObject>d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)row(errs,d.getSource()==null?"null":root.relativize(Paths.get(d.getSource().toUri())),d.getLineNumber(),d.getCode(),d.getMessage(Locale.ROOT));
  }
  System.out.println("Parsed "+units.size()+" units; parse errors=0; attributed source errors="+diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
