import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Independent full-expression audit. Input sites are manifest locations, never regex-selected receivers. */
public final class ArmorTextureAttribution {
  static String esc(Object o) { return String.valueOf(o).replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n"); }
  static String text(CompilationUnitTree u, Tree tree, SourcePositions positions, String source) {
    long s=positions.getStartPosition(u,tree), e=positions.getEndPosition(u,tree);
    if (s<0 || e<s || e>source.length()) return tree.toString();
    return source.substring((int)s,(int)e);
  }
  public static void main(String[] args) throws Exception {
    final Path root=Paths.get(args[0]).toAbsolutePath();
    Map<String,Set<Long>> sites=new TreeMap<>();
    for(String row:Files.readAllLines(Paths.get(args[2]), StandardCharsets.UTF_8)) { String[] a=row.split("\t"); sites.computeIfAbsent(a[0],k->new TreeSet<>()).add(Long.valueOf(a[1])); }
    List<String> paths=new ArrayList<>();
    try(java.util.stream.Stream<Path> walk=Files.walk(root.resolve("src/main/java"))) {walk.filter(p->p.toString().endsWith(".java")).sorted().forEach(p->paths.add(p.toString()));}
    JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
    StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,StandardCharsets.UTF_8);
    JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-encoding","UTF-8","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(paths));
    Trees trees=Trees.instance(task);SourcePositions positions=trees.getSourcePositions();
    List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
    long parseErrors=diagnostics.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count();
    System.err.println("Parsed "+units.size()+" units; parse errors="+parseErrors);
    if(parseErrors!=0)throw new AssertionError("Source syntax errors prevent audit");
    task.analyze();
    System.err.println("Attribution completed; source errors="+diagnostics.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());
    if(args.length>4) try(java.io.PrintWriter out=new java.io.PrintWriter(args[4],"UTF-8")) {
      TypeElement item=task.getElements().getTypeElement("net.minecraft.item.Item");
      TypeElement provider=task.getElements().getTypeElement("com.hbm.render.tileentity.IItemRendererProvider");
      Set<String> names=new HashSet<>(Arrays.asList("registerIcons","getIcon","getIconFromDamage","getIconFromDamageForRenderPass","requiresMultipleRenderPasses","getRenderPasses","getColorFromItemStack","setTextureName","getIconString"));
      for(String name:Files.readAllLines(Paths.get(args[5]),StandardCharsets.UTF_8)) {
        TypeElement owner=task.getElements().getTypeElement(name);if(owner==null)throw new AssertionError(name);
        List<String> conflicts=new ArrayList<>();
        for(Element member:task.getElements().getAllMembers(owner))if(member instanceof ExecutableElement&&names.contains(member.getSimpleName().toString()))conflicts.add(member.getEnclosingElement()+"#"+member);
        out.println(name+"\t"+owner.getSuperclass()+"\t"+task.getTypes().isSubtype(owner.asType(),item.asType())+"\t"+task.getTypes().isSubtype(owner.asType(),provider.asType())+"\t"+String.join(";",conflicts));
      }
    }
    final int[] found={0};
    System.out.println("path\tline\tstart\tend\tmethod\treceiver_source\treceiver_kind\treceiver_type\treceiver_element_kind\treceiver_element\tmethod_element_kind\tmethod_owner\tmethod_signature\targument_count\targuments_source\targuments_kind\targuments_type\tfull_invocation");
    for(final CompilationUnitTree unit:units) {
      final String relative=root.relativize(Paths.get(unit.getSourceFile().toUri())).toString();
      final Set<Long> lines=sites.get(relative);if(lines==null)continue;
      final String source=unit.getSourceFile().getCharContent(true).toString();
      new TreePathScanner<Void,Void>() {
        public Void visitMethodInvocation(MethodInvocationTree t,Void unused) {
          long start=positions.getStartPosition(unit,t);long line=unit.getLineMap().getLineNumber(start);
          if(t.getMethodSelect() instanceof MemberSelectTree) {
            MemberSelectTree select=(MemberSelectTree)t.getMethodSelect();
            String name=select.getIdentifier().toString();
            if(name.equals("setTextureName") || name.equals("texture")) {
              TreePath mp=new TreePath(getCurrentPath(),select), rp=new TreePath(mp,select.getExpression());
              TypeMirror rt=trees.getTypeMirror(rp);Element re=trees.getElement(rp), me=trees.getElement(mp);
              List<String> as=new ArrayList<>(),ak=new ArrayList<>(),at=new ArrayList<>();
              for(ExpressionTree a:t.getArguments()) {TypeMirror type=trees.getTypeMirror(new TreePath(getCurrentPath(),a));as.add(text(unit,a,positions,source));ak.add(type==null?"null":type.getKind().toString());at.add(String.valueOf(type));}
              List<Object> row=Arrays.<Object>asList(relative,line,start,positions.getEndPosition(unit,t),name,text(unit,select.getExpression(),positions,source),rt==null?"null":rt.getKind(),rt,re==null?"null":re.getKind(),re,me==null?"null":me.getKind(),me==null?"null":me.getEnclosingElement(),me,t.getArguments().size(),String.join(" || ",as),String.join(" || ",ak),String.join(" || ",at),text(unit,t,positions,source));
              List<String> fields=new ArrayList<>();for(Object o:row)fields.add(esc(o));System.out.println(String.join("\t",fields));found[0]++;
            }
          }
          return super.visitMethodInvocation(t,unused);
        }
      }.scan(unit,null);
    }
    System.err.println("Selected invocation count="+found[0]);
    try(java.io.PrintWriter out=new java.io.PrintWriter(args[3],"UTF-8")) { for(Diagnostic<? extends JavaFileObject> d:diagnostics.getDiagnostics()) {if(d.getKind()!=Diagnostic.Kind.ERROR)continue;Path p=d.getSource()==null?null:Paths.get(d.getSource().toUri());String rel=p==null?"null":root.relativize(p).toString();if(sites.containsKey(rel))out.println(esc(rel)+"\t"+d.getLineNumber()+"\t"+esc(d.getCode())+"\t"+esc(d.getMessage(Locale.ROOT)));} }
    fm.close();
  }
}
