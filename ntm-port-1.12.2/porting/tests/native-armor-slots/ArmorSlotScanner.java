import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Native compiler ownership/override closure, including rejected old signatures. */
public class ArmorSlotScanner {
 static String esc(Object o) { return String.valueOf(o).replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n"); }
 public static void main(String[] args) throws Exception {
  Path root=Paths.get(args[0]).toAbsolutePath();
  List<String> paths=new ArrayList<>();try(java.util.stream.Stream<Path> w=Files.walk(root.resolve("src/main/java"))){w.filter(p->p.toString().endsWith(".java")).sorted().forEach(p->paths.add(p.toString()));}
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,StandardCharsets.UTF_8);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-encoding","UTF-8","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(paths));
  Trees trees=Trees.instance(task); SourcePositions positions=trees.getSourcePositions(); List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  if(diagnostics.getDiagnostics().stream().anyMatch(e->e.getKind()==Diagnostic.Kind.ERROR)) throw new AssertionError("parse errors");task.analyze();
  System.err.println("Parsed "+units.size()+" units; native attribution diagnostics="+diagnostics.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());
  TypeElement item=task.getElements().getTypeElement("net.minecraft.item.Item");
  TypeElement armor=task.getElements().getTypeElement("net.minecraft.item.ItemArmor");
  Set<String> hooks=new HashSet<>(Arrays.asList("getArmorModel","getArmorTexture","isValidArmor"));
  for(final CompilationUnitTree unit:units) {
   final String rel=root.relativize(Paths.get(unit.getSourceFile().toUri())).toString(),source=unit.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>() {
    void row(String kind,Tree t,Element e,String extra) {
     long s=positions.getStartPosition(unit,t),end=positions.getEndPosition(unit,t); if(s<0||end<s)return;
     TypeMirror ty=trees.getTypeMirror(getCurrentPath());
     System.out.println(String.join("\t",esc(kind),esc(rel),""+unit.getLineMap().getLineNumber(s),""+s,""+end,esc(e==null?null:e.getKind()),esc(e==null?null:e.getEnclosingElement()),esc(e),esc(ty),esc(extra),Base64.getEncoder().encodeToString(source.substring((int)s,(int)end).getBytes(StandardCharsets.UTF_8))));
    }
    void field(Tree t,String name) {if(!name.equals("armorType"))return;Element e=trees.getElement(getCurrentPath());row("armorType",t,e,"");}
    @Override public Void visitIdentifier(IdentifierTree t,Void p){field(t,t.getName().toString());return super.visitIdentifier(t,p);}
    @Override public Void visitMemberSelect(MemberSelectTree t,Void p){field(t,t.getIdentifier().toString());return super.visitMemberSelect(t,p);}
    @Override public Void visitMethod(MethodTree t,Void p) {
     Element methodElement=trees.getElement(getCurrentPath());
     if(t.getName().contentEquals("<init>") && methodElement!=null && methodElement.getEnclosingElement() instanceof TypeElement && task.getTypes().isSubtype(methodElement.getEnclosingElement().asType(),armor.asType())) row("constructor-declaration",t,methodElement,"");
     if(hooks.contains(t.getName().toString())) {Element e=trees.getElement(getCurrentPath());String extra="";
      if(e instanceof ExecutableElement && e.getEnclosingElement() instanceof TypeElement) {TypeElement owner=(TypeElement)e.getEnclosingElement();List<String> overridden=new ArrayList<>();for(Element m:task.getElements().getAllMembers(item))if(m instanceof ExecutableElement&&task.getElements().overrides((ExecutableElement)e,(ExecutableElement)m,owner))overridden.add(m.toString());extra=String.join(";",overridden);}
      row("hook-declaration",t,e,extra);
     }return super.visitMethod(t,p);
    }
    @Override public Void visitNewClass(NewClassTree t,Void p) {
     Element e=trees.getElement(getCurrentPath());
     if(e!=null && e.getEnclosingElement() instanceof TypeElement && task.getTypes().isSubtype(e.getEnclosingElement().asType(),armor.asType())) row("construction",t,e,"");
     return super.visitNewClass(t,p);
    }
    @Override public Void visitMethodInvocation(MethodInvocationTree t,Void p) {
     String name=t.getMethodSelect() instanceof MemberSelectTree?((MemberSelectTree)t.getMethodSelect()).getIdentifier().toString():t.getMethodSelect().toString();
     Element e=trees.getElement(new TreePath(getCurrentPath(),t.getMethodSelect()));
     if(hooks.contains(name)||name.equals("super")||name.equals("fromIndex")||name.equals("index")) {
      TreePath cp=getCurrentPath();while(cp!=null&&!(cp.getLeaf() instanceof ClassTree))cp=cp.getParentPath();Element ce=cp==null?null:trees.getElement(cp);
      String extra=ce instanceof TypeElement?ce+" extends "+((TypeElement)ce).getSuperclass():"";
      if(!name.equals("super") || extra.endsWith("extends net.minecraft.item.ItemArmor"))row("call",t,e,extra);
      else if(ce instanceof TypeElement && task.getTypes().isSubtype(ce.asType(),armor.asType()))row("constructor-chain",t,e,extra);
     }return super.visitMethodInvocation(t,p);
    }
   }.scan(unit,null);
  }
  try(java.io.PrintWriter out=new java.io.PrintWriter(args[2],"UTF-8")){for(Diagnostic<? extends JavaFileObject>d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR){String rel=d.getSource()==null?"null":root.relativize(Paths.get(d.getSource().toUri())).toString();out.println(esc(rel)+"\t"+d.getLineNumber()+"\t"+esc(d.getCode())+"\t"+esc(d.getMessage(Locale.ROOT)));}}
  fm.close();
 }
}
