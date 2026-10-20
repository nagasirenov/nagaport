import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Read-only full-source attribution. Never infers native ownership from spelling. */
public class OrderedAabbScanner {
 static int nativePositionWrites, nativePositionSetterOverrides;
 static javax.lang.model.util.Elements elements; static ExecutableElement positionSetter;
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
  String nativeCoordinate(ExpressionTree n){
   n=un(n); if(!(n instanceof MethodInvocationTree))return null;
   MethodInvocationTree call=(MethodInvocationTree)n;
   if(!call.getArguments().isEmpty() || !(call.getMethodSelect() instanceof MemberSelectTree))return null;
   MemberSelectTree select=(MemberSelectTree)call.getMethodSelect();
   Element method=tr.getElement(p(call));
   if(!(method instanceof ExecutableElement)||!method.getEnclosingElement().toString().equals("net.minecraft.util.math.Vec3i")||type(call)!=TypeKind.INT||!Arrays.asList("getX","getY","getZ").contains(method.getSimpleName().toString()))return null;
   ExpressionTree receiver=un(select.getExpression());
   if(!(receiver instanceof MemberSelectTree)||!((MemberSelectTree)receiver).getExpression().toString().equals("this"))return null;
   Element field=tr.getElement(p(receiver));
   if(field==null||field.getKind()!=ElementKind.FIELD||!field.getEnclosingElement().toString().equals("net.minecraft.tileentity.TileEntity")||!field.getSimpleName().contentEquals("pos")||!field.asType().toString().equals("net.minecraft.util.math.BlockPos")||field.getModifiers().contains(Modifier.VOLATILE))return null;
   return "native-TileEntity.pos."+method.getSimpleName();
  }
  static class Aff {String base;double offset;Aff(String b,double o){base=b;offset=o;}}
  Aff affine(ExpressionTree n){n=un(n);String c=nativeCoordinate(n);if(c!=null)return new Aff(c,0);
   Element local=local(n); if(local!=null&&Arrays.asList(TypeKind.BYTE,TypeKind.SHORT,TypeKind.INT,TypeKind.CHAR).contains(type(n)))return new Aff("local:"+local,0);
   if(n instanceof TypeCastTree&&type(n)==TypeKind.DOUBLE)return affine(((TypeCastTree)n).getExpression());
   if((n.getKind()==Tree.Kind.PLUS||n.getKind()==Tree.Kind.MINUS)&&type(n)==TypeKind.DOUBLE){BinaryTree b=(BinaryTree)n;Aff a=affine(b.getLeftOperand());Double k=literal(b.getRightOperand());if(a!=null&&k!=null&&Double.isFinite(k)&&Math.abs(k)<=1024&&k*65536==Math.rint(k*65536)){a.offset+=n.getKind()==Tree.Kind.MINUS?-k:k;if(Math.abs(a.offset)<=1024)return a;}}return null;}
  double[] range(ExpressionTree n){n=un(n);Double v=literal(n);if(v!=null&&Double.isFinite(v)&&Double.doubleToRawLongBits(v)!=Long.MIN_VALUE)return new double[]{v,v};
   if(n instanceof ConditionalExpressionTree){ConditionalExpressionTree c=(ConditionalExpressionTree)n;ExpressionTree test=un(c.getCondition());Element cond=tr.getElement(p(test));if(!(cond instanceof VariableElement)||!Arrays.asList(ElementKind.LOCAL_VARIABLE,ElementKind.PARAMETER).contains(cond.getKind())||type(test)!=TypeKind.BOOLEAN)return null;double[] a=range(c.getTrueExpression()),b=range(c.getFalseExpression());if(a!=null&&b!=null)return new double[]{Math.min(a[0],b[0]),Math.max(a[1],b[1])};}return null;}
  String proof(ExpressionTree lo,ExpressionTree hi){double[] a=range(lo),b=range(hi);if(a!=null&&b!=null&&a[1]<=b[0]&&!(a[1]==0&&b[0]==0&&Double.doubleToRawLongBits(a[1])==0&&Double.doubleToRawLongBits(b[0])!=0))return "finite-literal-or-boolean-conditional";
   Aff x=affine(lo),y=affine(hi);if(x!=null&&y!=null&&x.base.equals(y.base)&&x.offset<=y.offset&&Math.abs(x.offset)<=1024&&Math.abs(y.offset)<=1024)return "finite-int-double-affine:"+x.base+":"+x.offset+":"+y.offset;return "unproved";}
  public Void visitAssignment(AssignmentTree n,Void v){Element e=tr.getElement(p(n.getVariable()));if(e!=null&&e.getKind()==ElementKind.FIELD&&e.getEnclosingElement().toString().equals("net.minecraft.tileentity.TileEntity")&&e.getSimpleName().contentEquals("pos")){nativePositionWrites++;System.err.println("Native position assignment: "+u.getSourceFile().getName()+":"+u.getLineMap().getLineNumber(tr.getSourcePositions().getStartPosition(u,n)));}return super.visitAssignment(n,v);}
  public Void visitMethod(MethodTree n,Void v){Element e=tr.getElement(p(n));if(e instanceof ExecutableElement&&positionSetter!=null&&elements.overrides((ExecutableElement)e,positionSetter,(TypeElement)e.getEnclosingElement())){nativePositionSetterOverrides++;System.err.println("Native position setter override: "+e.getEnclosingElement());}return super.visitMethod(n,v);}
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
  Trees tr=Trees.instance(t);List<CompilationUnitTree> us=new ArrayList<>();for(CompilationUnitTree u:t.parse())us.add(u);t.analyze();elements=t.getElements();TypeElement te=elements.getTypeElement("net.minecraft.tileentity.TileEntity");if(te!=null)for(Element e:te.getEnclosedElements())if(e instanceof ExecutableElement&&e.getSimpleName().contentEquals("setPos")&&((ExecutableElement)e).getParameters().size()==1)positionSetter=(ExecutableElement)e;for(CompilationUnitTree u:us)new Scan(tr,u,a.length>2&&a[2].equals("after")).scan(u,null);
  if(a.length>3){List<String> lines=new ArrayList<>();for(Diagnostic<? extends JavaFileObject> d0:d.getDiagnostics())if(d0.getKind()==Diagnostic.Kind.ERROR)lines.add("{\"path\":"+q(d0.getSource()==null?"":Paths.get(d0.getSource().toUri()))+",\"start\":"+d0.getStartPosition()+",\"position\":"+d0.getPosition()+",\"end\":"+d0.getEndPosition()+",\"line\":"+d0.getLineNumber()+",\"code\":"+q(d0.getCode())+",\"message\":"+q(d0.getMessage(Locale.ROOT))+"}");Files.write(Paths.get(a[3]),lines);}
  System.err.println("Attribution only; production inputs="+us.size()+"; unfinished-port errors="+d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count());System.err.println("Native TileEntity position source writes="+nativePositionWrites+"; setPos overrides="+nativePositionSetterOverrides);f.close();
 }
}
