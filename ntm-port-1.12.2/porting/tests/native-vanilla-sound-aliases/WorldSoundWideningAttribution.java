import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** All real CP59 production sources are parsed and analyzed. Sparse overlay is optional.
 * Diagnostic coordinates only locate candidates; complete ASTs select receivers/arguments.
 */
public final class WorldSoundWideningAttribution {
  static final String PREFIX="ntm-port-1.12.2/src/main/java/";
  static final class Source extends SimpleJavaFileObject {
    final String path, source;
    Source(String path, String source) { super(URI.create("mem:///"+path), Kind.SOURCE);this.path=path;this.source=source; }
    @Override public CharSequence getCharContent(boolean ignoreErrors) {return source;}
  }
  static String esc(Object o) { return String.valueOf(o).replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n"); }
  static String text(CompilationUnitTree u, Tree t, SourcePositions p, String source) {
    long s=p.getStartPosition(u,t),e=p.getEndPosition(u,t);
    if(s<0||e<s||e>source.length())throw new AssertionError("Missing complete original source range for "+t.getKind()+": "+s+".."+e);
    return source.substring((int)s,(int)e);
  }
  static String type(Trees trees,TreePath p) {TypeMirror t=trees.getTypeMirror(p);return t==null?"null":t.toString();}
  static String kind(Trees trees,TreePath p) {TypeMirror t=trees.getTypeMirror(p);return t==null?"null":t.getKind().toString();}
  static String join(List<String> values){return String.join(" || ",values);}
  public static void main(String[] args)throws Exception {
    final boolean fullUniverse=args[2].equals("-");
    Map<String,Set<Long>> sites=new TreeMap<>();
    if(!fullUniverse)for(String row:Files.readAllLines(Paths.get(args[2]),StandardCharsets.UTF_8)){String[] a=row.split("\t");sites.computeIfAbsent(a[0],k->new TreeSet<>()).add(Long.valueOf(a[1]));}
    Map<String,Source> sources=new TreeMap<>();
    try(ZipFile zip=new ZipFile(args[0])) {
      Enumeration<? extends ZipEntry> entries=zip.entries();
      while(entries.hasMoreElements()){ZipEntry e=entries.nextElement();if(e.getName().startsWith(PREFIX)&&e.getName().endsWith(".java")){
        String path=e.getName().substring(PREFIX.length());ByteArrayOutputStream b=new ByteArrayOutputStream();try(InputStream in=zip.getInputStream(e)){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)b.write(buf,0,n);}sources.put(path,new Source(path,new String(b.toByteArray(),StandardCharsets.UTF_8)));
      }}
    }
    int overlayCount=0;
    if(args.length>4&&!args[4].equals("-")) {
      Path overlay=Paths.get(args[4]).toAbsolutePath();List<Path> files=new ArrayList<>();
      try(java.util.stream.Stream<Path> walk=Files.walk(overlay)){walk.filter(p->p.toString().endsWith(".java")).sorted().forEach(files::add);}
      for(Path p:files){String path=overlay.relativize(p).toString();sources.put(path,new Source(path,new String(Files.readAllBytes(p),StandardCharsets.UTF_8)));overlayCount++;}
    }
    JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
    StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,StandardCharsets.UTF_8);
    List<String> options=Arrays.asList("-proc:none","-encoding","UTF-8","-source","8","-target","8","-sourcepath","/tmp/player-sound-attribution/empty-sourcepath","-classpath",new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8).trim(),"-Xmaxerrs","100000","-Xmaxwarns","100000");
    JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,options,null,sources.values());
    Trees trees=Trees.instance(task);SourcePositions positions=trees.getSourcePositions();List<CompilationUnitTree> units=new ArrayList<>();
    for(CompilationUnitTree u:task.parse())units.add(u);
    long parseErrors=diagnostics.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count();
    System.err.println("Parsed "+units.size()+" genuine production units; sparse overlay units="+overlayCount+"; parse errors="+parseErrors);
    if(parseErrors!=0)throw new AssertionError("Production syntax errors prevent attribution");
    task.analyze();
    System.err.println("Full JavacTask.analyze completed; source errors="+diagnostics.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());
    final int[] found={0};
    System.out.println("path\tline\tstart\tend\tselect_start\tselect_end\treceiver_start\treceiver_end\tmethod\treceiver_source\treceiver_tree_kind\treceiver_kind\treceiver_type\treceiver_element_kind\treceiver_element\tmethod_element_kind\tmethod_owner\tmethod_signature\targument_count\targuments_source\targuments_tree_kind\targuments_kind\targuments_type\targuments_start\targuments_end\tstring_literals\tfull_invocation\tenclosing_method_start\tenclosing_method_end\tenclosing_method_signature\tenclosing_method_source");
    for(final CompilationUnitTree unit:units) {
      final String relative=unit.getSourceFile().toUri().getPath().substring(1);final Set<Long> lines=sites.get(relative);if(!fullUniverse&&lines==null)continue;
      final String source=unit.getSourceFile().getCharContent(true).toString();
      new TreePathScanner<Void,Void>() {
        @Override public Void visitMethodInvocation(MethodInvocationTree t,Void unused){
          long start=positions.getStartPosition(unit,t),line=unit.getLineMap().getLineNumber(start);
          if((fullUniverse||lines.contains(line))&&t.getMethodSelect() instanceof MemberSelectTree){
            MemberSelectTree select=(MemberSelectTree)t.getMethodSelect();String name=select.getIdentifier().toString();
            if(name.equals("playSoundEffect")||(!fullUniverse&&name.equals("play"))){
              TreePath mp=new TreePath(getCurrentPath(),select),rp=new TreePath(mp,select.getExpression());
              Element re=trees.getElement(rp),me=trees.getElement(mp);
              List<String> as=new ArrayList<>(),atk=new ArrayList<>(),ak=new ArrayList<>(),at=new ArrayList<>(),ast=new ArrayList<>(),aen=new ArrayList<>(),lit=new ArrayList<>();
              for(ExpressionTree a:t.getArguments()){
                TreePath ap=new TreePath(getCurrentPath(),a);as.add(text(unit,a,positions,source));atk.add(a.getKind().toString());ak.add(kind(trees,ap));at.add(type(trees,ap));ast.add(String.valueOf(positions.getStartPosition(unit,a)));aen.add(String.valueOf(positions.getEndPosition(unit,a)));lit.add(a instanceof LiteralTree&&((LiteralTree)a).getValue() instanceof String?(String)((LiteralTree)a).getValue():"<not-string-literal>");
              }
              TreePath method=getCurrentPath();while(method!=null&&!(method.getLeaf() instanceof MethodTree))method=method.getParentPath();
              if(method==null){method=getCurrentPath();while(method.getParentPath()!=null && !(method.getLeaf() instanceof ClassTree))method=method.getParentPath();}
              List<Object> row=Arrays.<Object>asList(relative,line,start,positions.getEndPosition(unit,t),positions.getStartPosition(unit,select),positions.getEndPosition(unit,select),positions.getStartPosition(unit,select.getExpression()),positions.getEndPosition(unit,select.getExpression()),name,text(unit,select.getExpression(),positions,source),select.getExpression().getKind(),kind(trees,rp),type(trees,rp),re==null?"null":re.getKind(),re,me==null?"null":me.getKind(),me==null?"null":me.getEnclosingElement(),me,t.getArguments().size(),join(as),join(atk),join(ak),join(at),join(ast),join(aen),join(lit),text(unit,t,positions,source),positions.getStartPosition(unit,method.getLeaf()),positions.getEndPosition(unit,method.getLeaf()),trees.getElement(method),text(unit,method.getLeaf(),positions,source));
              List<String> fields=new ArrayList<>();for(Object o:row)fields.add(esc(o));System.out.println(String.join("\t",fields));found[0]++;
            }
          }
          return super.visitMethodInvocation(t,unused);
        }
      }.scan(unit,null);
    }
    System.err.println("Inspected candidate invocation count="+found[0]);
    try(PrintWriter out=new PrintWriter(args[3],"UTF-8")){
      out.println("path\tline\tstart\tend\tcode\tmessage");
      for(Diagnostic<? extends JavaFileObject>d:diagnostics.getDiagnostics()){
        if(d.getKind()!=Diagnostic.Kind.ERROR)continue;String rel=d.getSource()==null?"null":d.getSource().toUri().getPath().substring(1);
        out.println(esc(rel)+"\t"+d.getLineNumber()+"\t"+d.getStartPosition()+"\t"+d.getEndPosition()+"\t"+esc(d.getCode())+"\t"+esc(d.getMessage(Locale.ROOT)));
      }
    }
    fm.close();
  }
}
