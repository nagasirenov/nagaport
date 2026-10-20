import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Complete parsed call/loop census used to pin the repository's bounded slot domain. */
public final class SlotDomainScanner {
 static String q(Object o){return "\""+o.toString().replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";}
 static final Set<String> names=new HashSet<>(Arrays.asList("getEquipmentNullable","getArmorNullable","checkArmorPiece","checkArmorNull","damageSuit","hasAllProtection","hasAnyProtection","hasProtection"));
 public static void main(String[] args)throws Exception {
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();StandardJavaFileManager fm=c.getStandardFileManager(null,null,null);JavacTask task=(JavacTask)c.getTask(null,fm,null,Arrays.asList("-proc:none","-source","8","-encoding","UTF-8"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  final Trees trees=Trees.instance(task);final SourcePositions pos=trees.getSourcePositions();
  for(final CompilationUnitTree unit:task.parse()){final String source=unit.getSourceFile().getCharContent(true).toString();new TreePathScanner<Void,Void>() {
   public Void visitMemberReference(MemberReferenceTree t,Void v){if(names.contains(t.getName().toString()))throw new AssertionError("Unbounded method reference: "+t);return super.visitMemberReference(t,v);}
   public Void visitMethodInvocation(MethodInvocationTree call,Void v){String name=call.getMethodSelect() instanceof MemberSelectTree?((MemberSelectTree)call.getMethodSelect()).getIdentifier().toString():call.getMethodSelect().toString();if(!names.contains(name))return super.visitMethodInvocation(call,v);
    String method="";List<String> loops=new ArrayList<>();for(TreePath p=getCurrentPath().getParentPath();p!=null;p=p.getParentPath()){if(p.getLeaf() instanceof MethodTree)method=((MethodTree)p.getLeaf()).getName().toString();if(p.getLeaf() instanceof ForLoopTree){ForLoopTree loop=(ForLoopTree)p.getLeaf();loops.add("["+loop.getInitializer()+"; "+loop.getCondition()+"; "+loop.getUpdate()+"]");}}
    List<String> arguments=new ArrayList<>();for(ExpressionTree arg:call.getArguments())arguments.add(source.substring((int)pos.getStartPosition(unit,arg),(int)pos.getEndPosition(unit,arg)));
    StringBuilder out=new StringBuilder("{\"file\":"+q(Paths.get(unit.getSourceFile().toUri()))+",\"method\":"+q(method)+",\"name\":"+q(name)+",\"call\":"+q(source.substring((int)pos.getStartPosition(unit,call),(int)pos.getEndPosition(unit,call)))+",\"args\":[");for(int i=0;i<arguments.size();i++){if(i>0)out.append(',');out.append(q(arguments.get(i)));}out.append("],\"loops\":[");for(int i=0;i<loops.size();i++){if(i>0)out.append(',');out.append(q(loops.get(i)));}System.out.println(out+"]}");return super.visitMethodInvocation(call,v);
   }
  }.scan(unit,null);}
 }
}
