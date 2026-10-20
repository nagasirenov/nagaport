import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Bounded finite-local AABB attribution; exact symbols, not name matching. */
public class FiniteAabbScanner {
 static String q(Object s){return "\""+String.valueOf(s).replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";}
 static class Range { final double lo,hi; Range(double l,double h){lo=l;hi=h;} }
 static class Scan extends TreePathScanner<Void,Void> {
  final Trees tr; final CompilationUnitTree u; final String source; final boolean after;
  final Map<Element,Range> cache=new HashMap<>(); final Set<Element> active=new HashSet<>();
  final List<String> evidence=new ArrayList<>();
  Scan(Trees t,CompilationUnitTree u,boolean a)throws Exception{tr=t;this.u=u;source=u.getSourceFile().getCharContent(true).toString();after=a;}
  TreePath p(Tree n){return TreePath.getPath(u,n);} Element el(Tree n){return tr.getElement(p(n));}
  TypeKind type(Tree n){TypeMirror t=tr.getTypeMirror(p(n));return t==null?TypeKind.NONE:t.getKind();}
  String txt(Tree n){return source.substring((int)tr.getSourcePositions().getStartPosition(u,n),(int)tr.getSourcePositions().getEndPosition(u,n));}
  ExpressionTree un(ExpressionTree n){while(n instanceof ParenthesizedTree)n=((ParenthesizedTree)n).getExpression();return n;}
  Range safe(double lo,double hi){return Double.isFinite(lo)&&Double.isFinite(hi)&&lo<=hi&&lo>=-0x1p40&&hi<=0x1p40?new Range(lo,hi):null;}
  Range range(ExpressionTree n){n=un(n);TypeKind k=type(n);
   // Integral values are bounded even if their already-evaluated initializer used calls or overflow.
   if(k==TypeKind.BYTE)return new Range(Byte.MIN_VALUE,Byte.MAX_VALUE);
   if(k==TypeKind.SHORT)return new Range(Short.MIN_VALUE,Short.MAX_VALUE);
   if(k==TypeKind.CHAR)return new Range(Character.MIN_VALUE,Character.MAX_VALUE);
   if(k==TypeKind.INT){Element e=el(n);evidence.add("integral:"+txt(n)+":"+(e==null?"expression":e.getEnclosingElement()+"."+e));return new Range(Integer.MIN_VALUE,Integer.MAX_VALUE);}
   if(k!=TypeKind.DOUBLE)return null;
   if(n instanceof LiteralTree){Object v=((LiteralTree)n).getValue();return v instanceof Number?safe(((Number)v).doubleValue(),((Number)v).doubleValue()):null;}
   if(n.getKind()==Tree.Kind.IDENTIFIER)return local(el(n));
   if(n.getKind()==Tree.Kind.UNARY_MINUS){Range r=range(((UnaryTree)n).getExpression());return r==null?null:safe(-r.hi,-r.lo);}
   if(n.getKind()==Tree.Kind.PLUS||n.getKind()==Tree.Kind.MINUS||n.getKind()==Tree.Kind.MULTIPLY){BinaryTree b=(BinaryTree)n;Range x=range(b.getLeftOperand()),y=range(b.getRightOperand());if(x==null||y==null)return null;
    if(n.getKind()==Tree.Kind.PLUS)return safe(x.lo+y.lo,x.hi+y.hi);
    if(n.getKind()==Tree.Kind.MINUS)return safe(x.lo-y.hi,x.hi-y.lo);
    double[] a={x.lo*y.lo,x.lo*y.hi,x.hi*y.lo,x.hi*y.hi};double lo=a[0],hi=a[0];for(double v:a){lo=Math.min(lo,v);hi=Math.max(hi,v);}return safe(lo,hi);
   }return null;
  }
  Range local(final Element e){if(e==null||e.getKind()!=ElementKind.LOCAL_VARIABLE||e.asType().getKind()!=TypeKind.DOUBLE)return null;
   if(cache.containsKey(e))return cache.get(e);if(!active.add(e))return null;
   TreePath decl=tr.getPath(e);if(decl==null||!(decl.getLeaf() instanceof VariableTree)){active.remove(e);return null;}
   VariableTree v=(VariableTree)decl.getLeaf();TreePath method=decl;while(method!=null&&!(method.getLeaf() instanceof MethodTree))method=method.getParentPath();
   if(method==null||v.getInitializer()==null){active.remove(e);return null;}
   final List<ExpressionTree> definitions=new ArrayList<>();definitions.add(v.getInitializer());final boolean[] unsupported={false};
   new TreePathScanner<Void,Void>(){
    public Void visitAssignment(AssignmentTree n,Void x){if(e.equals(el(n.getVariable())))definitions.add(n.getExpression());return super.visitAssignment(n,x);}
    public Void visitCompoundAssignment(CompoundAssignmentTree n,Void x){if(e.equals(el(n.getVariable())))unsupported[0]=true;return super.visitCompoundAssignment(n,x);}
    public Void visitUnary(UnaryTree n,Void x){if(Arrays.asList(Tree.Kind.PREFIX_INCREMENT,Tree.Kind.PREFIX_DECREMENT,Tree.Kind.POSTFIX_INCREMENT,Tree.Kind.POSTFIX_DECREMENT).contains(n.getKind())&&e.equals(el(n.getExpression())))unsupported[0]=true;return super.visitUnary(n,x);}
   }.scan(method,null);
   Range result=null;if(!unsupported[0])for(ExpressionTree d:definitions){Range r=range(d);if(r==null){result=null;break;}result=result==null?r:safe(Math.min(result.lo,r.lo),Math.max(result.hi,r.hi));if(result==null)break;}
   active.remove(e);cache.put(e,result);
   if(result!=null){List<String> defs=new ArrayList<>();for(ExpressionTree d:definitions)defs.add(txt(d));evidence.add("local:"+e+"@"+tr.getSourcePositions().getStartPosition(u,v)+":"+defs+":"+result.lo+":"+result.hi);}
   return result;
  }
  Element operand(ExpressionTree n,double offset){n=un(n);if(n.getKind()!=Tree.Kind.PLUS||type(n)!=TypeKind.DOUBLE)return null;BinaryTree b=(BinaryTree)n;ExpressionTree left=un(b.getLeftOperand()),right=un(b.getRightOperand());if(left.getKind()!=Tree.Kind.IDENTIFIER||type(left)!=TypeKind.DOUBLE||!(right instanceof LiteralTree)||type(right)!=TypeKind.DOUBLE)return null;Object v=((LiteralTree)right).getValue();if(!(v instanceof Double)||((Double)v)!=offset)return null;Element e=el(left);return e!=null&&e.getKind()==ElementKind.LOCAL_VARIABLE?e:null;}
  public Void visitMethodInvocation(MethodInvocationTree n,Void v){if(!after&&n.getMethodSelect() instanceof MemberSelectTree){MemberSelectTree s=(MemberSelectTree)n.getMethodSelect();if(s.getIdentifier().contentEquals("getBoundingBox")){
   Element receiver=el(s.getExpression());boolean nativeOwner=receiver instanceof TypeElement&&((TypeElement)receiver).getQualifiedName().contentEquals("net.minecraft.util.math.AxisAlignedBB");boolean good=nativeOwner&&n.getArguments().size()==6;cache.clear();active.clear();evidence.clear();List<String> proofs=new ArrayList<>();
   if(n.getArguments().size()==6)for(int i=0;i<3;i++){Element a=operand(n.getArguments().get(i),0.1875D),b=operand(n.getArguments().get(i+3),0.8125D);Range r=a!=null&&a.equals(b)?local(a):null;good&=r!=null;proofs.add(r==null?"unproved":"same-finite-double-local:"+a+":"+r.lo+":"+r.hi);}
   List<String> args=new ArrayList<>(),types=new ArrayList<>();for(ExpressionTree a:n.getArguments()){args.add(q(txt(a)));types.add(q(type(a)));}List<String> ev=new ArrayList<>();for(String e:evidence)ev.add(q(e));List<String> pp=new ArrayList<>();for(String e:proofs)pp.add(q(e));
   System.out.println("{\"path\":"+q(Paths.get(u.getSourceFile().toUri()))+",\"start\":"+tr.getSourcePositions().getStartPosition(u,n)+",\"selectorEnd\":"+tr.getSourcePositions().getEndPosition(u,s)+",\"end\":"+tr.getSourcePositions().getEndPosition(u,n)+",\"owner\":"+q(receiver==null?"null":receiver.getKind()+":"+receiver)+",\"selected\":"+good+",\"proof\":["+String.join(",",pp)+"],\"types\":["+String.join(",",types)+"],\"arguments\":["+String.join(",",args)+"],\"flow\":["+String.join(",",ev)+"]}");
  }}return super.visitMethodInvocation(n,v);}
  public Void visitNewClass(NewClassTree n,Void v){if(after){Element e=tr.getElement(getCurrentPath());if(e instanceof ExecutableElement&&e.getEnclosingElement().toString().equals("net.minecraft.util.math.AxisAlignedBB")&&((ExecutableElement)e).getParameters().size()==6){boolean good=true;for(VariableElement a:((ExecutableElement)e).getParameters())good&=a.asType().getKind()==TypeKind.DOUBLE;if(good)System.out.println("{\"path\":"+q(Paths.get(u.getSourceFile().toUri()))+",\"start\":"+tr.getSourcePositions().getStartPosition(u,n)+",\"end\":"+tr.getSourcePositions().getEndPosition(u,n)+",\"descriptor\":\"(DDDDDD)V\"}");}}return super.visitNewClass(n,v);}
 }
 public static void main(String[] a)throws Exception{
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager f=c.getStandardFileManager(d,null,null);
  JavacTask t=(JavacTask)c.getTask(null,f,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,f.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  Trees tr=Trees.instance(t);List<CompilationUnitTree> us=new ArrayList<>();for(CompilationUnitTree u:t.parse())us.add(u);t.analyze();for(CompilationUnitTree u:us)new Scan(tr,u,a.length>2&&a[2].equals("after")).scan(u,null);
  if(a.length>3){List<String> lines=new ArrayList<>();for(Diagnostic<? extends JavaFileObject> x:d.getDiagnostics())if(x.getKind()==Diagnostic.Kind.ERROR)lines.add("{\"path\":"+q(x.getSource()==null?"":Paths.get(x.getSource().toUri()))+",\"start\":"+x.getStartPosition()+",\"position\":"+x.getPosition()+",\"end\":"+x.getEndPosition()+",\"line\":"+x.getLineNumber()+",\"code\":"+q(x.getCode())+",\"message\":"+q(x.getMessage(Locale.ROOT))+"}");Files.write(Paths.get(a[3]),lines);}
  System.err.println("Attribution only; production inputs="+us.size()+"; unfinished-port errors="+d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count());f.close();
 }
}
