import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Read-only full-source attribution. Never infers native ownership from spelling. */
public class BoundScanner {
 static String q(Object s) {return "\""+String.valueOf(s).replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";}
 static final Set<TypeKind> NUM=new HashSet<>(Arrays.asList(TypeKind.BYTE,TypeKind.SHORT,TypeKind.INT,TypeKind.LONG,TypeKind.FLOAT,TypeKind.DOUBLE,TypeKind.CHAR));
 static final Set<TypeKind> INT=new HashSet<>(Arrays.asList(TypeKind.BYTE,TypeKind.SHORT,TypeKind.INT,TypeKind.LONG,TypeKind.CHAR));
 static class Scan extends TreePathScanner<Void,Void> {
  final Trees tr; final CompilationUnitTree u; final String source; final boolean after;
  final Map<String,String> vars=new TreeMap<>();
  Scan(Trees t,CompilationUnitTree u,boolean a)throws Exception{tr=t;this.u=u;source=u.getSourceFile().getCharContent(true).toString();after=a;}
  TreePath p(Tree n){return TreePath.getPath(u,n);} TypeKind type(Tree n){TypeMirror t=tr.getTypeMirror(p(n));return t==null?TypeKind.NONE:t.getKind();}
  Element local(Tree n){if(n.getKind()!=Tree.Kind.IDENTIFIER)return null;Element e=tr.getElement(p(n));if(e!=null&&(e.getKind()==ElementKind.PARAMETER||e.getKind()==ElementKind.LOCAL_VARIABLE)&&NUM.contains(type(n))){vars.put(e.getSimpleName().toString(),e.asType().toString());return e;}return null;}
  ExpressionTree un(ExpressionTree n){while(n instanceof ParenthesizedTree)n=((ParenthesizedTree)n).getExpression();return n;}
  boolean pure(ExpressionTree n){n=un(n);if(!NUM.contains(type(n)))return false;switch(n.getKind()){
   case INT_LITERAL:case LONG_LITERAL:case FLOAT_LITERAL:case DOUBLE_LITERAL:case CHAR_LITERAL:return true;
   case IDENTIFIER:return local(n)!=null;
   case TYPE_CAST:return pure(((TypeCastTree)n).getExpression());
   case UNARY_MINUS:case UNARY_PLUS:case BITWISE_COMPLEMENT:return pure(((UnaryTree)n).getExpression());
   case PLUS:case MINUS:case MULTIPLY:case DIVIDE:case REMAINDER:case LEFT_SHIFT:case RIGHT_SHIFT:case UNSIGNED_RIGHT_SHIFT:case AND:case OR:case XOR:return pure(((BinaryTree)n).getLeftOperand())&&pure(((BinaryTree)n).getRightOperand());
   default:return false;
  }}
  Double literal(ExpressionTree n){n=un(n);if(n instanceof LiteralTree){Object v=((LiteralTree)n).getValue();return v instanceof Number?((Number)v).doubleValue():null;}if(n.getKind()==Tree.Kind.UNARY_MINUS||n.getKind()==Tree.Kind.UNARY_PLUS){Double v=literal(((UnaryTree)n).getExpression());return v==null?null:n.getKind()==Tree.Kind.UNARY_MINUS?-v:v;}return null;}
  static class Aff {Element var;double offset;Aff(Element v,double o){var=v;offset=o;}}
  Aff base(ExpressionTree n){n=un(n);if(n instanceof TypeCastTree&&type(n)==TypeKind.DOUBLE)n=un(((TypeCastTree)n).getExpression());Element e=local(n);return e!=null&&INT.contains(type(n))?new Aff(e,0):null;}
  Aff affine(ExpressionTree n){n=un(n);Aff a=base(n);if(a!=null)return a;
   if((n.getKind()==Tree.Kind.PLUS||n.getKind()==Tree.Kind.MINUS)&&type(n)==TypeKind.DOUBLE){BinaryTree b=(BinaryTree)n;a=base(b.getLeftOperand());Double c=literal(b.getRightOperand());if(a!=null&&c!=null&&Double.isFinite(c)){a.offset=n.getKind()==Tree.Kind.MINUS?-c:c;return a;}}return null;}
  String proof(ExpressionTree lo,ExpressionTree hi){if(pure(lo)&&pure(hi)&&lo.toString().equals(hi.toString()))return "identical-pure-local";Aff a=affine(lo),b=affine(hi);if(a!=null&&b!=null&&a.var.equals(b.var)&&a.offset<=b.offset&&Math.abs(a.offset)<=1e100&&Math.abs(b.offset)<=1e100)return "finite-integral-local-double-affine";return "unproved";}
  String txt(Tree n){return source.substring((int)tr.getSourcePositions().getStartPosition(u,n),(int)tr.getSourcePositions().getEndPosition(u,n));}
  public Void visitMethodInvocation(MethodInvocationTree n,Void v){if(!after&&n.getMethodSelect() instanceof MemberSelectTree){MemberSelectTree s=(MemberSelectTree)n.getMethodSelect();if(s.getIdentifier().contentEquals("getBoundingBox")){
   Element e=tr.getElement(p(s.getExpression()));boolean owner=e instanceof TypeElement&&((TypeElement)e).getQualifiedName().contentEquals("net.minecraft.util.math.AxisAlignedBB");vars.clear();List<String> pr=new ArrayList<>();boolean good=owner&&n.getArguments().size()==6;
   if(n.getArguments().size()==6)for(int i=0;i<3;i++){String z=proof(n.getArguments().get(i),n.getArguments().get(i+3));pr.add(z);good&=!z.equals("unproved");}
   List<String> types=new ArrayList<>();List<String> args=new ArrayList<>();for(ExpressionTree a:n.getArguments()){types.add(q(type(a)));args.add(q(txt(a)));good&=NUM.contains(type(a));}
   List<String> vv=new ArrayList<>();for(Map.Entry<String,String> a:vars.entrySet())vv.add(q(a.getKey())+":"+q(a.getValue()));
   System.out.println("{\"path\":"+q(Paths.get(u.getSourceFile().toUri()))+",\"start\":"+tr.getSourcePositions().getStartPosition(u,n)+",\"selectorEnd\":"+tr.getSourcePositions().getEndPosition(u,s)+",\"end\":"+tr.getSourcePositions().getEndPosition(u,n)+",\"owner\":"+q(e==null?"null":e.getKind()+":"+e.toString())+",\"selected\":"+good+",\"proof\":["+String.join(",",Arrays.asList(q(pr.size()>0?pr.get(0):"arity"),q(pr.size()>1?pr.get(1):"arity"),q(pr.size()>2?pr.get(2):"arity")))+"],\"types\":["+String.join(",",types)+"],\"arguments\":["+String.join(",",args)+"],\"variables\":{"+String.join(",",vv)+"}}");
  }}return super.visitMethodInvocation(n,v);}
  public Void visitNewClass(NewClassTree n,Void v){if(after){Element e=tr.getElement(getCurrentPath());if(e instanceof ExecutableElement&&e.getEnclosingElement().toString().equals("net.minecraft.util.math.AxisAlignedBB")&&((ExecutableElement)e).getParameters().size()==6){boolean good=true;for(VariableElement a:((ExecutableElement)e).getParameters())good&=a.asType().getKind()==TypeKind.DOUBLE;if(good)System.out.println("{\"path\":"+q(Paths.get(u.getSourceFile().toUri()))+",\"start\":"+tr.getSourcePositions().getStartPosition(u,n)+",\"end\":"+tr.getSourcePositions().getEndPosition(u,n)+",\"descriptor\":\"(DDDDDD)V\"}");}}return super.visitNewClass(n,v);}
 }
 public static void main(String[] a)throws Exception{
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager f=c.getStandardFileManager(d,null,null);
  JavacTask t=(JavacTask)c.getTask(null,f,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,f.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  Trees tr=Trees.instance(t);List<CompilationUnitTree> us=new ArrayList<>();for(CompilationUnitTree u:t.parse())us.add(u);t.analyze();for(CompilationUnitTree u:us)new Scan(tr,u,a.length>2).scan(u,null);
  System.err.println("Attribution only; production inputs="+us.size()+"; unfinished-port errors="+d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count());f.close();
 }
}
