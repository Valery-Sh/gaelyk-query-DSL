package groovyx.gaelyk.dsl.query;

import groovyx.gaelyk.dsl.query.dummy.Dummy_123;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ModuleNode;
import java.security.CodeSource;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationUnit;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.ResolveVisitor;
import java.util.ArrayList;
import org.codehaus.groovy.ast.expr.*;

import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import static groovyx.gaelyk.dsl.query.Helper.*;

/**
 * The class manages the process of AST transformation of a closure expressions.
 * A closure to be transformed must contains a sequence of 
 * <code>ExpressionStatement</code> that is recognized as a 
 * <code>query DSL</code>. <br/>
 * First a <code>ClosureExpression</code> is converted to a string,
 * which is a source code for the query in the GAE way. Then 
 * <code>AstBuilder</code> builds AST from source. <br/>
 * Suppose we have code:
 * <pre>
 *   def persons = datastore.query {
 *      select all, limit(10)
 *      from Person
 *   }
 * </pre>
 * In the example above there is a closure with two method call expressions:
 * <ul>
 *   <li>select</li>
 *   <li>from</li>.
 * </ul>
 * The result of conversion the closure into a string might look like:
 * <code>
 * <pre>
 *   def markerProperty__123= null
 *   def whereFields__123 = "" 
 *   def orderByFields__123 = "" 
 *   def pojo__123= null
 *   def pojoClass__123= null
 *   def entity__123= null
 *   def fetchOptions__123 = FetchOptions.Builder.withDefaults() 
 *   def select__123= null
 *   def cursor__123= null
 *   def kind__123= null
 *   def all__123= null
 *   def keys__123= null
 *   def single__123= null
 *   def count__123= null
 *   def ancestorKey__123= null
 *   def limit__123= null
 *   def offset__123= null
 *   def chunkSize__123= null
 *   def prefetchSize__123= null
 *   def deadLine__123= null
 *   def startCursor__123= null
 *   def endCursor__123= null
 *   def childOf__123= null
 *   def query__123= null
 *   def preparedQuery__123= null
  
 *   select__123 = 'select'
 *   all__123 = 'all'
 *   limit__123 = 10
 *   fetchOptions__123.limit(limit__123)
 *   kind__123 = "Person"
 *   if (kind__123) { 
 *       query__123 = new com.google.appengine.api.datastore.Query(kind__123)
 *      if (childOf__123) { 
 *           if (childOf__123 instanceof Key ) { 
 *               query__123.setAncestor(childOf__123)
 *           } else { 
 *               query__123.setAncestor(childOf__123.key)
 *           }
 *        }
 *   } else {
 *       query__123 = new com.google.appengine.api.datastore.Query()
 *       if (childOf__123) { 
 *           if (childOf__123 instanceof Key ) { 
 *               query__123.setAncestor(childOf__123)
 *           } else { 
 *               query__123.setAncestor(childOf__123.key)
 *           }
 *       }
 *   }
 *   if (keys__123) {
 *       query__123.setKeysOnly()
 *   }
 *   preparedQuery__123 = it.prepare(query__123)
 *   if (cursor__123) {
 *       if (startCursor__123) {
 *           if ((startCursor__123 instanceof String) && startCursor__123.trim() != 'null'  && ! startCursor__123.trim().isEmpty() ) {
 *               fetchOptions__123.startCursor(Cursor.fromWebSafeString(startCursor__123))
 *           } else if (startCursor__123 instanceof Cursor ) { 
 *               fetchOptions__123.startCursor(startCursor__123)
 *           }
 *       }
 *       preparedQuery__123.asQueryResultList(fetchOptions__123)
 *   } else if (all__123) { 
 *       preparedQuery__123.asList(fetchOptions__123)
 *   } else if (count__123) {
 *       preparedQuery__123.countEntities(fetchOptions__123)
 *   } else if (single__123) {
 *       preparedQuery__123.asSingleEntity()
 *   } else {
 *       preparedQuery__123.asList(fetchOptions__123)
 *   }
 * </pre>  
 * </code>
 * The script above may be used by AstBuilder to perform transformation
 * from string. <p/>
 * We see that the internally defined fields have a suffix "__123". 
 * Actually the value of the suffix is defined by the constant
 * @{link Helper#NAME_SUFFIX} and may contain,  for example some kind of UID.
 * The use of the suffix makes our code a "black box"  and it helps prevent 
 * conflicts of names. Thus we can write the script:
 * <code>
 *  <pre>
 *      def limit = 15 
 *      def all = "alalala"
 *      ..............
 *      ..............
 *      datastore.query{
 *          select all, limit=limit
 *          from Person
 *      }
 *  </pre>
 * </code>
 * 
 * <p/>
 * The class implements the interface @{link VisitClosureHandler} with a single
 * method <code>success</code>. When <code>visit()</code> method of the class
 * <code>QueryASTTransformation</code> finds a closure which it considers as 
 * to be transformed it notifies this class by calling  the method 
 * <code>success()</code>.
 * 
 * @author V. Shyshkin
 */
public class QueryTransformer implements VisitClosureHandler  {
    /**
     * An instance of the <code>SourceUnit</code> whose closures
     * might be transformed.
     */
    protected SourceUnit sourceUnit;
    /**
     * An instance of the @{link TransformState} is created for each closure
     * to be transformed. 
     */
    protected List<TransformState> stateList;
    
    protected QueryASTResolveVisitor resolveVisitor;
    /**
     * Since the class @{link DefaultExpressionConverter} implements 
     * @{link ExpressionConverter} it is used as a converter by default.
     */
    protected ExpressionConverter converter;
    /**
     * Method @{link #transform} scans a stateList and performs transformation 
     * for each element. Here is an index of the current item.
     */
    protected int currentClosure;
    
    protected Dummy_123 dummy;
    /**
     * Creates a new instance for a given source unit.
     * @param sourceUnit the source unit which closures may be transformed.
     */
    public QueryTransformer(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
        stateList = new ArrayList<TransformState>();
    }
    /**
     * @return an object of type <code>String</code> which represents
     *    the initial part of a source for AST transformation. 
     */
    protected String initTransform() {
        return 
                defVariable("markerProperty")
                + ("def whereFields" + NAME_SUFFIX + " = \"\" \n")
                + ("def orderByFields" + NAME_SUFFIX + " = \"\" \n")
                + defVariable("pojo")
                + defVariable("pojoClass")
                + defVariable("entity")
                + "def fetchOptions" + NAME_SUFFIX + " = FetchOptions.Builder.withDefaults() \n"
                + defVariable("select")
                + defVariable("cursor")
                + defVariable("kind")
                + defVariable("all")
                + defVariable("keys")
                + defVariable("single")
                + defVariable("count")
                + defVariable("ancestorKey")
                + defVariable("limit")
                + defVariable("offset")
                + defVariable("chunkSize")
                + defVariable("prefetchSize")
                + defVariable("deadLine")
                + defVariable("startCursor")
                + defVariable("endCursor")
                + defVariable("childOf")
                + defVariable("query")
                + defVariable("preparedQuery");
    }
    /**
     * The method is invoked at the end of the method 
     * @{link #transformFetchOptionsArguments }.  
     * 
     * @return a string with a part of source code for <code>AstBuilder</code>.
     */
    protected String postFetchOptionsTransform() {
        return //"print 'class=' + it.getClass() \n"      +
                "if (" + trname("chunkSize") + ") { \n"
                + "   fetchOptions" + NAME_SUFFIX + ".chunkSize(" + trname("chunkSize") + ")\n"
                + "}\n "
                + "if (" + trname("prefetchSize") + ") { \n"
                + trname("fetchOptions") + ".prefetchSize(" + trname("prefetchSize") + ")\n"
                + "}\n ";
    }
    
    /**
     * The method is invoked at the end of the method 
     * @{link #transformFromArguments }.  
     * 
     * @return a string with a part of source code for <code>AstBuilder</code>.
     */
    protected String postFromTransform() {
        return 
                "if (" + trname("kind") + ") { \n"
                + trname("query") + " = new com.google.appengine.api.datastore.Query(" + trname("kind") + ")\n"
                //               assign("query", "QueryCreator.create(" + trname("kind") + ")\n") +                 
                + "    if (" + trname("childOf") + ") { \n"
                + "       if (childOf" + NAME_SUFFIX + " instanceof Key ) { \n"
                + "           query" + NAME_SUFFIX + ".setAncestor(" + "childOf" + NAME_SUFFIX + ")\n"
                + "       } else { \n"
                + "           query" + NAME_SUFFIX + ".setAncestor(" + "childOf" + NAME_SUFFIX + ".key)\n"
                + "       }\n"
                + "    }\n"
                + "} else {\n"
                + trname("query") + " = new com.google.appengine.api.datastore.Query()\n"
                + "    if (" + trname("childOf") + ") { \n"
                + "       if (childOf" + NAME_SUFFIX + " instanceof Key ) { \n"
                + "           query" + NAME_SUFFIX + ".setAncestor(" + "childOf" + NAME_SUFFIX + ")\n"
                + "       } else { \n"
                + "           query" + NAME_SUFFIX + ".setAncestor(" + "childOf" + NAME_SUFFIX + ".key)\n"
                + "       }\n"
                + "    }\n"
                + "}\n"
                + "if (" + trname("keys") + ") {\n"
                + "query" + NAME_SUFFIX + ".setKeysOnly()\n"
                + "}\n";

    }
    /**
     * The method is invoked when the last statement in the closure 
     * is treated.
     * 
     * @return a string with a final part of source code for <code>AstBuilder</code>.
     */
    protected String finalTransform() {
        return trname("preparedQuery") + " = it.prepare(" + "query" + NAME_SUFFIX + ")\n"
                //+ "if ("  + "all" + NAME_SUFFIX + " && " 
                + "if ("
                + "cursor" + NAME_SUFFIX + ") {\n"
                + "     if (" + "startCursor" + NAME_SUFFIX + ") {\n"
                + "           if (" + "(startCursor" + NAME_SUFFIX
                + " instanceof String) && startCursor" + NAME_SUFFIX + ".trim() != 'null' "
                + " && ! startCursor" + NAME_SUFFIX + ".trim().isEmpty() ) {\n"
                + "                fetchOptions" + NAME_SUFFIX + ".startCursor("
                + "Cursor.fromWebSafeString(startCursor" + NAME_SUFFIX + "))\n"
                + "           } else if (startCursor" + NAME_SUFFIX + " instanceof Cursor ) { \n"
                + "                fetchOptions" + NAME_SUFFIX + ".startCursor("
                + "startCursor" + NAME_SUFFIX + ")\n"
                + "           }\n"
                //                + "        }\n"                
                + "     }\n"
                + "    preparedQuery" + NAME_SUFFIX + ".asQueryResultList(fetchOptions" + NAME_SUFFIX + ")\n"
                + "}\n"
                + "else if (" + "all" + NAME_SUFFIX + ") { \n"
                + "    preparedQuery" + NAME_SUFFIX + ".asList(fetchOptions" + NAME_SUFFIX + ")\n"
                + "}\n"
                + "else if (" + "count" + NAME_SUFFIX + ") {\n"
                + "    preparedQuery" + NAME_SUFFIX + ".countEntities(fetchOptions" + NAME_SUFFIX + ")\n"
                + "}\n"
                + "else if (" + "single" + NAME_SUFFIX + ") {\n"
                + "    preparedQuery" + NAME_SUFFIX + ".asSingleEntity()\n"
                + "}\n"
                + "else {\n"
                + "    preparedQuery" + NAME_SUFFIX + ".asList(fetchOptions" + NAME_SUFFIX + ")\n"
                + "}\n";
//                + " pojoClass";
    }

    /**
     * Helper method to create a string representation of the Groovy
     * declaration for a given variable name whose value is initialized with
     * <code>null</code>. 
     * @param varName the variable name for which a declaration must be
     *  created.
     * @return a string like "def varName" + NAME_SUFFIX + "= null"
     */
    protected String defVariable(String varName) {
        return "def " + varName + NAME_SUFFIX + "= " + "null\n";
    }
    /**
     * Converts a given name to a new name .
     * The new name is a concatenation of the oldName and a 
     * @{link Helper#NAME_SUFFIX } constant.
     * 
     * @param oldName the name to be converted
     * @return the converted name
     */
    protected String trname(String oldName) {
        String s1 = oldName.substring(0, 1).toLowerCase() + oldName.substring(1);
        return s1 + NAME_SUFFIX;
    }
    /**
     * Cycles through the contents of the @{link #stateList} and 
     * transforms each closure.
     */
    public void transform() {
        if (stateList.isEmpty()) {
            return;
        }
        for (int i = 0; i < stateList.size(); i++) {
            TransformState st = stateList.get(i);
            currentClosure = i;
            String result = transformClosure(st.getClosure());
            st.setTransformAsString(result);
//System.out.println(result);            
        }

        for (int i = 0; i < stateList.size(); i++) {
            doTransformations(stateList.get(i).getClosure(), stateList.get(i).getTransformAsString());
        }

    }
    /**
     * Performs AST Transformation for a given closure expression and 
     * a string as a source code. 
     * @param closureExpr a closure expression to be transformed. 
     * @param transformBuilderString  a source code for <code>AstBuilder</code>.
     */
    protected void doTransformations(ClosureExpression closureExpr, String transformBuilderString) {
        BlockStatement qblock = (BlockStatement) closureExpr.getCode();
        AstBuilder b = new AstBuilder();

        List<ASTNode> astNodes = b.buildFromString(CompilePhase.CONVERSION, true, transformBuilderString);
        List<Statement> stmtList = new ArrayList<Statement>(astNodes.size());
        for (ASTNode s : astNodes) {
            stmtList.add((Statement) s);
        }

        BlockStatement newBlock = new BlockStatement(stmtList, qblock.getVariableScope());
        closureExpr.setCode(newBlock);

    }
    /**
     * Transforms a given closure expression to a string as a source code for 
     * <code>AstBuilder</code>.
     * The method creates a list of <code>MethodCallExpressions</code>
     * and invokes the method @{link #transformClosureStatements } with
     * the list as a parameter.
     * 
     * @param closureExpr an object to be transformed
     * @return an object of type <code>String</code> which content
     *  represents source code for <code>AstBuilder</code>
     */
    public String transformClosure(ClosureExpression closureExpr) {
        List<Statement> statements = ((BlockStatement) closureExpr.getCode()).getStatements();
        List<MethodCallExpression> exprList = new ArrayList(5);
        String method = "";
        for (Statement statement : statements) {
            Expression expr = ((ExpressionStatement) statement).getExpression();
            if (expr instanceof MethodCallExpression) {
                method = ((MethodCallExpression) expr).getMethod().getText();
                String tx = ((MethodCallExpression) expr).getText();
            } else {
                addError("Unsupported expressioon: " + expr.getText(), expr);
            }
            if (!isSupported(method)) {
                addError("Unsupported name: " + method, expr);
            }
            exprList.add((MethodCallExpression) expr);
        }//for

        return transformClosureStatements(exprList);
    }
    /**
     * Iterates over a given list of <code>MethodCallExpression</code>
     * and for each expression calls the method @{link #transformArguments).
     * 
     * @param calls a list of <code>MethodCallExpression</code>
     * @return an object of type <code>String</code> 
     */
    public String transformClosureStatements(List<MethodCallExpression> calls) {
        String result = initTransform();
        MethodCallExpression call = null;
        for (MethodCallExpression expr : calls) {
            call = (MethodCallExpression) expr;
            Expression argsExpr = call.getArguments();
            if (argsExpr instanceof ArgumentListExpression) {
                result += transformArguments(call, (ArgumentListExpression) argsExpr);
            }
        }
        result += finalTransform();
//        System.out.println(result);
        return result;
    }
    /**
     * Transforms a given  argument list expression of the given
     * method call expression.
     * 
     * @param call a method call for which an argument list is to be 
     * transformed
     * 
     * @param argList argument list expression of the method call
     * @return an object of type <code>String</code> that represents source code
     *  for <code>AstBuilder</code>
     *  
     */ 
    public String transformArguments(MethodCallExpression call, ArgumentListExpression argList) {
        String result = "";
        String nm = call.getMethod().getText();
        if ("select".equals(nm) || "Select".equals(nm)) {
            result += transformSelectArguments(call, argList);
            stateList.get(currentClosure).setCursor(false);
        } else if ("cursor".equals(nm) || "Cursor".equals(nm)) {
            result += transformCursorArguments(call, argList);
            stateList.get(currentClosure).setCursor(true);
        } else if ("from".equals(nm) || "From".equals(nm)) {
            result += transformFromArguments(call, argList);
        } else if ("where".equals(nm) || "Where".equals(nm)) {
            result += transformWhereArguments(call, argList);
        } else if ("orderBy".equals(nm) || "OrderBy".equals(nm)) {
            result += transformOrderByArguments(call, argList);
        } else if ("options".equals(nm) || "Options".equals(nm)
                || "fetchOptions".equals(nm) || "FetchOptions".equals(nm)) {
            result += transformFetchOptionsArguments(call, argList);
        }
        return result;
    }
    /**
     * Transforms a given  argument list expression of the given
     * method call expression of the "cursor" method call.
     * 
     * @param call a method call for which an argument list is to be 
     * transformed
     * 
     * @param argList argument list expression of the method call
     * @return an object of type <code>String</code> that represents source code
     *  for <code>AstBuilder</code>
     *  
     */ 
    public String transformCursorArguments(MethodCallExpression call, ArgumentListExpression argList) {
        List<Expression> exprList = argList.getExpressions();
        String result = assignExprAsString("cursor", "'cursor'");
        this.stateList.get(currentClosure).setCursor(true);
        int start = 0;
        String nm = "all";
        if (exprList.get(0) instanceof VariableExpression) {
            nm = ((VariableExpression) exprList.get(0)).getName();
            if (!("all".equals(nm) || "All".equals(nm)
                    || "keys".equals(nm) || "Keys".equals(nm))) {
                addError("'cursor' may only have the first argument as one of [all,keys]", exprList.get(0));
            }
            start = 1;
        }
        result += assignExprAsString(nm, "'" + nm + "'");

        for (int i = start; i < exprList.size(); i++) {
            if (!((exprList.get(i) instanceof BinaryExpression)
                    || (exprList.get(i) instanceof MethodCallExpression))) {
                addError("'cursor' doesn't support the argument expression: '" + exprList.get(i).getText() + "' for 'cursor' ", exprList.get(i));
            }
            nm = null;
            if (exprList.get(i) instanceof MethodCallExpression) {
                nm = ((MethodCallExpression) exprList.get(i)).getMethod().getText();
                if (!("startCursor".equals(nm) || "endCursor".equals(nm) || "limit".equals(nm) || "Limit".equals(nm))) {
                    addError("'cursor' doesn't support the argument expression: '" + nm + "'", exprList.get(i));
                }
                result += assignExprAsString(nm, getArgumentValue((MethodCallExpression) exprList.get(i)));
                if ("limit".equals(nm) || "Limit".equals(nm)) {
                    result += "fetchOptions" + NAME_SUFFIX + "." + lowCaseFirstLetter(nm)
                            + "(" + lowCaseFirstLetter(nm) + NAME_SUFFIX + ")\n";
                }
            } else {
                Expression left = ((BinaryExpression) exprList.get(i)).getLeftExpression();
                if (left instanceof VariableExpression) {
                    nm = ((VariableExpression) left).getName();
                    if (!("startCursor".equals(nm) || "endCursor".equals(nm) || "limit".equals(nm) || "Limit".equals(nm))) {
                        addError("'cursor' doesn't support the argument expression: '" + nm + "' ", exprList.get(i));
                    }
                    String op = ((BinaryExpression) exprList.get(i)).getOperation().getText();
                    Expression right = ((BinaryExpression) exprList.get(i)).getRightExpression();
                    if (!("=".equals(op))) {
                        addError("'cursor' doesn't support operation '" + op + "' (" + nm + " " + op + " " + right.getText() + ") ", exprList.get(i));
                    }
                    result += assignExprAsString(nm, convert(right) + "\n");
                    if ("limit".equals(nm) || "Limit".equals(nm)) {
                        result += "fetchOptions" + NAME_SUFFIX + "." + lowCaseFirstLetter(nm)
                                + "(" + lowCaseFirstLetter(nm) + NAME_SUFFIX + ")\n";
                    }
                } else {
                    addError("'cursor' doesn't support the argument expression: ", exprList.get(i));
                }
            }
        }
        return result;
    }
    /**
     * Transforms a given  argument list expression of the given
     * method call expression of the "options" method call.
     * 
     * @param call a method call for which an argument list is to be 
     * transformed
     * 
     * @param argList argument list expression of the method call
     * @return an object of type <code>String</code> that represents source code
     *  for <code>AstBuilder</code>
     *  
     */ 
    public String transformFetchOptionsArguments(MethodCallExpression call, ArgumentListExpression argList) {
        List<Expression> exprList = argList.getExpressions();
        String result = "";

        //---------------------------------------
        // fetchOptions may contain chunkSize,prefetchSize, 
        //---------------------------------------
        for (int i = 0; i < exprList.size(); i++) {
            if (!((exprList.get(i) instanceof BinaryExpression)
                    || (exprList.get(i) instanceof MethodCallExpression))) {
                addError("'fetchOptions' doesn't support argument expression: '" + exprList.get(i).getText(), exprList.get(i));
            }
            String nm = null;
            if (exprList.get(i) instanceof MethodCallExpression) {
                nm = ((MethodCallExpression) exprList.get(i)).getMethod().getText();
                if (!("chunkSize".equals(nm) || "ChunkSize".equals(nm)
                        || "prefetchSize".equals(nm) || "PrefetchSize".equals(nm))) {
                    addError("'fetchOptions' doesn't support argument expression: '" + nm, exprList.get(i));
                }

                result += assignExprAsString(nm, getArgumentValue((MethodCallExpression) exprList.get(i)));
            } else {
                Expression left = ((BinaryExpression) exprList.get(i)).getLeftExpression();

                if (left instanceof VariableExpression) {

                    nm = ((VariableExpression) left).getName();
                    if (!("chunkSize".equals(nm) || "ChunkSize".equals(nm)
                            || "prefetchSize".equals(nm) || "PrefetchSize".equals(nm))) {
                        addError("'fetchOptions' doesn't support argument expression: '" + nm, exprList.get(i));
                    }

                    String op = ((BinaryExpression) exprList.get(i)).getOperation().getText();
                    Expression right = ((BinaryExpression) exprList.get(i)).getRightExpression();
                    if (!"=".equals(op)) {
                        addError("'fetchOptions' doesn't support operation '" + op + "' (" + nm + " " + op + " " + right.getText() + ") ", exprList.get(i));
                    }
                    //result += assignExprAsString(nm, converter.convert(right));
                    result += assignExprAsString(nm, convert(right));
                    //transformVariableName((BinaryExpression) exprList.get(i));
                } else {
                    addError("Unsupported argument expression for 'fetchOptions'", exprList.get(i));
                }
            }

        }
        result += postFetchOptionsTransform();
        return result;
    }
    public String transformFromArguments(MethodCallExpression call, ArgumentListExpression argList) {
        //String result = transformString;
        String result = "";

        List<Expression> exprs = argList.getExpressions();
        if (exprs == null || exprs.size() == 0 || exprs.size() > 2) {
            addError("'from' must have at least one parameter", call);
        }

        if (!((exprs.get(0) instanceof VariableExpression) || (exprs.get(0) instanceof MethodCallExpression)
                || (exprs.get(0) instanceof BinaryExpression) || (exprs.get(0) instanceof CastExpression))) {
            addError("Unsupported argument expression for 'from'", exprs.get(0));
        }
        String kind = "";
        //
        // Check if kindless ancestor query
        //
        String childOfValue = this.getChildOfValue(exprs.get(0));

        if (childOfValue != null) {
            // Must be kindless ancestor query
            if (exprs.size() > 1) {
                addError("'from' contains too many parameters for kindless query", exprs.get(0));
            }
            result += assignExprAsString("childOf", childOfValue);

        } else if (exprs.get(0) instanceof VariableExpression) {
            kind = exprs.get(0).getText();
            result += assignExprAsString("kind", "\"" + kind + "\"");
        }
        if (exprs.get(0) instanceof CastExpression) {
            // Person as pojo
            String pojo = "";

            CastExpression castExpr = (CastExpression) exprs.get(0);
            Expression pojoExpr = castExpr.getExpression();
            kind = getKindFromCastExpression(castExpr.getText());

            if (!(pojoExpr instanceof VariableExpression)) {
                addError("'from' doesn't support the argument expression: " + pojoExpr.getText() + " (left part)", pojoExpr);
            }
            pojo = pojoExpr.getText();
            if (pojo.toUpperCase().contains("POJO")
                    || pojo.toUpperCase().contains("BEAN")) {
                pojo = "pojo";
            } else if (pojo.toUpperCase().contains("ENTITY")) {
                pojo = "entity";
            } else {
                addError("'from' can contain 'pojo' or 'bean' or 'entity'", castExpr);
            }
            boolean resolved = resolve(castExpr, kind);
            if (!resolved) {
                addError("Can't resolve the type '" + kind + "'", castExpr);
            }
            //if ( pojo)
            result += assignExprAsString("pojoClass", kind + "\n");
            result += assignExprAsString("kind", "\"" + kind + "\"\n");
            result += assignExprAsString("pojo", "\"" + pojo + "\"\n");

        }

        if (exprs.size() > 1 && !kind.isEmpty()) {
            childOfValue = this.getChildOfValue(exprs.get(1));
            if (childOfValue != null) {
                result += assignExprAsString("childOf", childOfValue);
            } else {
                addError("The second argument contains an expression that 'from' doesn't support", call);
            }
        } else if (kind.isEmpty() && childOfValue == null) {
            addError("Unsupported 'from' expression", call);
        }
        if (kind.isEmpty()) {
            // when set than Query cannot include filters on properties
            stateList.get(currentClosure).setKindlessAncestorQuery(true);
        }
        result += postFromTransform();

        return result;
    }
    
    /**
     * Transforms a given  argument list expression of the given
     * method call expression of the "orderBy" method call.
     * 
     * @param call a method call for which an argument list is to be 
     * transformed
     * 
     * @param argList argument list expression of the method call
     * @return an object of type <code>String</code> that represents source code
     *  for <code>AstBuilder</code>
     *  
     */ 
    public String transformOrderByArguments(MethodCallExpression call, ArgumentListExpression argListExpr) {
        String result = "";
        List<Expression> argExprs = argListExpr.getExpressions();

        for (int i = 0; i < argExprs.size(); i++) {
            Expression expr = argExprs.get(i);
            if (!((expr instanceof CastExpression) || (expr instanceof VariableExpression))) {
                addError("'orderBy' doesn't support the argument expression: " + expr.getText(), expr);
            }
            String direction = "";
            String fieldName = "";

            if (expr instanceof CastExpression) {

                CastExpression cexpr = (CastExpression) expr;
                Expression fieldNameExpr = cexpr.getExpression();
                if (!(fieldNameExpr instanceof VariableExpression)) {
                    addError("'orderBy' doesn't support the argument expression: " + expr.getText() + " (left part)", fieldNameExpr);
                }
                fieldName = fieldNameExpr.getText();
                if (cexpr.getText().toUpperCase().contains("(ASC")) {
                    direction = "ASC";
                } else if (cexpr.getText().toUpperCase().contains("(DESC")) {
                    direction = "DESC";
                } else {
                    addError("'orderBy' direction must be 'ASC' or 'DESC'", cexpr);
                }
            } else {
                fieldName = expr.getText();
                direction = "ASC";
            }

            CastExpression castKind = stateList.get(currentClosure).getKindCast();
            Class clazz = null;
            if (castKind != null) {
                try {
                    clazz = this.getPojoType(castKind);
                } catch (Exception ex) {
                }
            }

            if (clazz != null && !Helper.hasField(clazz, fieldName)) {
                addError("Class '" + clazz.getSimpleName() + "' doesn't contain a field with a name '" + fieldName + "'", argExprs.get(0));
            }
            String inequalityField = stateList.get(currentClosure).getInequalityOperationField();


            if (stateList.get(currentClosure).isKindlessAncestorQuery()) {
                if (!"KEY_RESERVED_PROPERTY".equals(fieldName)) {
                    addError("Kindless ancestor query can be only sorted on  'KEY_RESERVED_PROPERTY'  ( field = '" + inequalityField + "').", argExprs.get(0));
                } else if (direction.equals("DESC")) {
                    addError("Only 'Ascending' direction on KEY_RESERVED_PROPERTY is supported  ( field = '" + inequalityField + " as DESC').", argExprs.get(0));
                } else {
                    fieldName = "Entity.KEY_RESERVED_PROPERTY";
                }
            }

            if (i == 0 && inequalityField != null && !inequalityField.equals(fieldName)) {
                addError("Properties in inequality filters must be ordered before other sort orders ( '" + fieldName + "' - now; '"
                        + inequalityField + "' must be ).", argExprs.get(0));
            }


            result += "orderByFields" + NAME_SUFFIX + " += '," + fieldName + "'\n";
            if ("KEY_RESERVED_PROPERTY".equals(fieldName) || stateList.get(currentClosure).isKindlessAncestorQuery()) {
                result += "query" + NAME_SUFFIX + ".addSort(Entity.KEY_RESERVED_PROPERTY,"
                        + orderDirMapping(direction) + ")\n";
            } else {
                result += "query" + NAME_SUFFIX + ".addSort(" + "\"" + fieldName + "\","
                        + orderDirMapping(direction) + ")\n";
            }
        }

        return result;

    }
public String transformSelectArguments(MethodCallExpression call, ArgumentListExpression argList) {
        List<Expression> exprList = argList.getExpressions();
        //String result = assignExprAsString("select","select" ); 
        String result = "select" + NAME_SUFFIX + " = 'select'\n";
        this.stateList.get(currentClosure).setCursor(false);
        if (!(exprList.get(0) instanceof VariableExpression)) {
            addError("'select' must have the first argument as one of [all,keys,single,count]", exprList.get(0));
        } else {
            String nm = ((VariableExpression) exprList.get(0)).getName();
            if (!("all".equals(nm) || "All".equals(nm)
                    || "single".equals(nm) || "Single".equals(nm)
                    || "count".equals(nm) || "Count".equals(nm)
                    || "keys".equals(nm) || "Keys".equals(nm))) {
                addError("'select' must have the first argument as one of [all,keys,single,count]", exprList.get(0));
            } else {
                //result += assignExprAsString(nm,"\"" + nm + "\"");
                result += nm + NAME_SUFFIX + " = '" + nm + "'\n";

            }
        }
        //---------------------------------------
        // select may contain limit or offset
        //---------------------------------------
        for (int i = 1; i < exprList.size(); i++) {
            if (!((exprList.get(i) instanceof BinaryExpression)
                    || (exprList.get(i) instanceof MethodCallExpression))) {
                addError("Unsupported argument expression: '" + exprList.get(i).getText() + "' for 'select' ", exprList.get(i));
            }
            String nm = null;
            if (exprList.get(i) instanceof MethodCallExpression) {
                nm = ((MethodCallExpression) exprList.get(i)).getMethod().getText();
                if (!("limit".equals(nm) || "Limit".equals(nm)
                        || "offset".equals(nm) || "Offset".equals(nm))) {
                    addError("Unsupported argument expression: '" + nm + "' in 'select'", exprList.get(i));
                }

                result += assignExprAsString(nm, getArgumentValue((MethodCallExpression) exprList.get(i)));
                result += "fetchOptions" + NAME_SUFFIX + "." + lowCaseFirstLetter(nm)
                        + "(" + lowCaseFirstLetter(nm) + NAME_SUFFIX + ")\n";
            } else {
                Expression left = ((BinaryExpression) exprList.get(i)).getLeftExpression();

                if (left instanceof VariableExpression) {

                    nm = ((VariableExpression) left).getName();
                    if (!("limit".equals(nm) || "Limit".equals(nm)
                            || "offset".equals(nm) || "Offset".equals(nm))) {
                        addError("Unsupported argument expression: '" + nm + "' in 'select'", exprList.get(i));
                    }

                    String op = ((BinaryExpression) exprList.get(i)).getOperation().getText();
                    Expression right = ((BinaryExpression) exprList.get(i)).getRightExpression();
                    if (!"=".equals(op)) {
                        addError("'select' doesn't support operation '" + op + "' (" + nm + " " + op + " " + right.getText() + ") ", exprList.get(i));
                    }
                    //result += assignExprAsString(nm, converter.convert(right));
                    result += assignExprAsString(nm, convert(right));
                    result += "fetchOptions" + NAME_SUFFIX + "." + lowCaseFirstLetter(nm)
                            + "(" + lowCaseFirstLetter(nm) + NAME_SUFFIX + ")\n";
                } else {
                    addError("Unsupported argument expression for 'select' method", exprList.get(i));
                }
            }

        }
        return result;
    }

    /**
     * Transforms a given  argument list expression of the given
     * method call expression of the "where" method call.
     * 
     * @param call a method call for which an argument list is to be 
     * transformed
     * 
     * @param argList argument list expression of the method call
     * @return an object of type <code>String</code> that represents source code
     *  for <code>AstBuilder</code>
     *  
     */ 
    public String transformWhereArguments(MethodCallExpression call, ArgumentListExpression argListExpr) {
        String result = "";
        List<Expression> argExprs = argListExpr.getExpressions();
        String inequalityField = null;
        //String inequalityOperation = null;
        int inequalityCount = 0;
        boolean alreadyHasNOTEqualFilter = false;
        /*
         * iterate over "where" predicates
         */
        for (Expression expr : argExprs) {
            if (!(expr instanceof BinaryExpression)) {
                addError("'where' doesn't support the argument expression: " + expr.getText(), expr);
            }
            BinaryExpression bexpr = (BinaryExpression) expr;
            Expression leftExpr = bexpr.getLeftExpression();
            if (!(leftExpr instanceof VariableExpression)) {
                addError("'where' doesn't support the argument expression: " + expr.getText() + " (left part)", leftExpr);
            }
            String fieldName = ((VariableExpression) leftExpr).getName();

            String operation = filterOperationMapping(bexpr.getOperation().getText());

            if (operation == null) {
                addError("'where' doesn't support the operation: " + bexpr.getOperation().getText() + " (" + bexpr.getText() + ")", leftExpr);
            }

            Expression rexpr = bexpr.getRightExpression();
            String rexprStr = convert(rexpr);            

            CastExpression castKind = stateList.get(currentClosure).getKindCast();
            Class clazz = null;
            if (castKind != null) {
                try {
                    clazz = this.getPojoType(castKind);
                } catch (Exception ex) {
                    System.out.println("Cannot create  a kind class: " + ex.getMessage());
                }
            }

            if (clazz != null && !Helper.hasField(clazz, fieldName)) {
                addError("Class '" + clazz.getSimpleName() + "' doesn't contain a field with a name '" + fieldName + "'", argExprs.get(0));
            }
            String whereOperation = bexpr.getOperation().getText();
            if (!("==".equals(whereOperation) || "in".equals(whereOperation))) {
                if (inequalityField != null && !inequalityField.equals(fieldName)) {
                    addError("A query may only use inequality filters (<, <=, >=, >, !=) on one property. ('" + inequalityField + "' - exists; '" + fieldName + "' - new ).", argExprs.get(0));
                } else if (alreadyHasNOTEqualFilter || "!=".equals(whereOperation) && inequalityCount > 0) {
                    addError("A query can only have one not-equal filter and cannot have other inequality filters", argExprs.get(0));
                } else {
                    inequalityField = fieldName;
                }
                inequalityCount++;
                if (inequalityCount > 2) {
                    addError("Too many inequality filter expressions (<, <=, >=, >, !=). ( field = '" + inequalityField + "').", argExprs.get(0));
                }
                if ("!=".equals(whereOperation)) {
                    alreadyHasNOTEqualFilter = true;
                }
            }
            if (stateList.get(currentClosure).isKindlessAncestorQuery()) {
                if (!"KEY_RESERVED_PROPERTY".equals(fieldName)) {
                    addError("Kindless queries cannot include filters on properties.  ( field = '" + inequalityField + "').", argExprs.get(0));
                } else {
                    fieldName = "Entity.KEY_RESERVED_PROPERTY";
                }
            }
            if (stateList.get(currentClosure).isCursor() && ( whereOperation.equals("in") 
                    || whereOperation.equals("!=") )) 
            {
               addError("Cursor query can't contain 'in' or '!=' filter operation. ('" + inequalityField + "'). ", argExprs.get(0));
            }            
            stateList.get(currentClosure).setInequalityOperationField(inequalityField);
            result += "whereFields" + NAME_SUFFIX + " += '," + fieldName + "'\n";
            if ("KEY_RESERVED_PROPERTY".equals(fieldName) || stateList.get(currentClosure).isKindlessAncestorQuery()) {
                result += "query" + NAME_SUFFIX + ".addFilter(Entity.KEY_RESERVED_PROPERTY,"
                        + operation + "," + rexprStr + ")\n";
            } else {
                result += "query" + NAME_SUFFIX + ".addFilter(" + "\"" + fieldName + "\","
                        + operation + "," + rexprStr + ")\n";
            }

        }//for

        return result;
    }
    /**
     * Maps a sort direction parameter value such as <code>asc, desc</code> to a
     * GAE value.
     * @param dir one of string values <code>"asc", "desc"</code>. Is case insensitive. So
     * they may be, for example <code>"ASC"</code> or <code>"Asc"</code> etc.
     * @return <code>Query.SortDirection.ASCENDING</code>  or 
     *      <code>Query.SortDirection.DESCENDING</code>.
     */
    public String orderDirMapping(String dir) {
        String result = null;
        if ("ASC".equals(dir.toUpperCase())) {
            result = "Query.SortDirection.ASCENDING";
        } else {
            result = "Query.SortDirection.DESCENDING";
        }
        return result;

    }
    /**
     * Maps comparison operation to the GAE representation.
     * 
     * @param op comparison operation. One of <code>"==,<,<=,>,>=,!=,in"</code>
     * @return <code>Query.FilterOperator.EQUAL or Query.FilterOperator.NOT_EQUAL
     *  or Query.FilterOperator.LESS_THAN or Query.FilterOperator.LESS_THAN_OR_EQUAL
     *  or Query.FilterOperator.GREATER_THAN or  Query.FilterOperator.GREATER_THAN_OR_EQUAL
     *  or Query.FilterOperator.IN</code>
     */
    public String filterOperationMapping(String op) {
        String result = null;
        if ("==".equals(op)) {
            result = "Query.FilterOperator.EQUAL";
        } else if ("!=".equals(op)) {
            result = "Query.FilterOperator.NOT_EQUAL";
        } else if ("<".equals(op)) {
            result = "Query.FilterOperator.LESS_THAN";
        } else if ("<=".equals(op)) {
            result = "Query.FilterOperator.LESS_THAN_OR_EQUAL";
        } else if (">".equals(op)) {
            result = "Query.FilterOperator.GREATER_THAN";
        } else if (">=".equals(op)) {
            result = "Query.FilterOperator.GREATER_THAN_OR_EQUAL";
        } else if ("in".equals(op) || "<<".equals(op)) {
            result = "Query.FilterOperator.IN";
        }
        return result;
    }
    /**
     * Helper method to create a Groovy assign expression for a given
     * left part and a given value.
     * Adds a suffix @{link Helper#NAME_SUFFIX} to the name that the 
     * <code>left</code> parameter contains and uses that new name as 
     * a left side of the result assign expression.
     * 
     * @param left the left part side of an expression to be created
     * @param value the right part side of an expression to be created
     * @return a string representation of the assign expression
     */
    protected String assignExprAsString(String left, String value) {
        String s1 = left.substring(0, 1).toLowerCase() + left.substring(1);
        String s2 = s1 + NAME_SUFFIX;
        return s2 + " = " + value + "\n"; // to make not null 
    }
    /**
     * Converts a given expression to a string.
     * Creates a new instance of the @{link ExpressionConverter} if needed and
     * calls it's <code>convert()</code> method.
     * @param expression an expression to be converted
     * @return a string representation of the expression
     */
    protected String convert(Expression expression) {
        if ( converter == null ) {
            converter = new DefaultExpressionConverter();
        }
        return converter.convert(expression);
    }

    protected String lowCaseFirstLetter(String str) {
        if (str == null) {
            return null;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Expects an argument list with one and only one argument.
     * @param call
     * @return 
     */
    protected String getArgumentValue(MethodCallExpression call) {
        String result = null;
        Expression argExpr = call.getArguments();
        ArgumentListExpression argListExpr = null;
        if (argExpr != null && (argExpr instanceof ArgumentListExpression)) {
            argListExpr = (ArgumentListExpression) argExpr;
        }
        Expression expr = null;
        if (argListExpr != null && argListExpr.getExpressions() != null && argListExpr.getExpressions().size() == 1) {
            expr = argListExpr.getExpressions().get(0);
            result = convert(expr);
        }
        if (result == null) {
            addError("Invalid argument value: " + call.getText(), call);
        }

        return result;
    }

    
    protected String getKindFromCastExpression(String cast) {
        String result = "";
        int left = cast.indexOf('(');
        int right = cast.indexOf("->");
        if (right < 0) {
            right = cast.indexOf(')');
        }
        result = cast.substring(left + 1, right).trim();
        int p = result.lastIndexOf('.');
        if (p > 0) {
            result = result.substring(p + 1).trim();
        }
        return result;
    }

    protected Class getPojoType(CastExpression castExpression) {
        String cast = castExpression.getText();

        int left = cast.indexOf('(');
        int right = cast.indexOf("->");
        if (right < 0) {
            right = cast.indexOf(')');
        }
        String className = cast.substring(left + 1, right).trim();
        Class clazz = null;
        try {
            clazz = sourceUnit.getAST().getUnit().getClassLoader().loadClass(className);
        } catch (Exception ex) {
        }
        return clazz;
    }

    protected String getChildOfValue(Expression expr) {
        String result = null;
        if (expr instanceof BinaryExpression) {
            BinaryExpression bexpr = (BinaryExpression) expr;
            if (!(bexpr.getLeftExpression() instanceof VariableExpression)) {
                addError("The second argument contains expression that 'from' doesn't support", expr);
            }
            VariableExpression vexpr = (VariableExpression) bexpr.getLeftExpression();
            String nm = vexpr.getName();
            if (!("childOf".equals(nm) || "ChildOf".equals(nm)
                    || "asChildOf".equals(nm) || "asChildsOf".equals(nm)
                    || "parent".equals(nm) || "ancestor".equals(nm)
                    || "childsOf".equals(nm) || "ChildsOf".equals(nm))) {
                addError("The second argument contains expression that 'from' doesn't support", expr);

            } else {
                //result = converter.convert(bexpr.getRightExpression());
                result = convert(bexpr.getRightExpression());
            }
        } else if (expr instanceof MethodCallExpression) {
            MethodCallExpression mcexpr = (MethodCallExpression) expr;
            String nm = mcexpr.getMethod().getText();
            if ("childOf".equals(nm) || "ChildOf".equals(nm)
                    || "asChildOf".equals(nm) || "asChildsOf".equals(nm)
                    || "parent".equals(nm) || "ancestor".equals(nm)
                    || "childsOf".equals(nm) || "ChildsOf".equals(nm)) {
                result = getArgumentValue(mcexpr);
            }
        }
        return result;

    }


    public boolean resolve(CastExpression cast, String kind) {
        boolean result = false;
        if (cast == null) {
            return true;
        }

        ClassNode dummyNode = null;
        FieldNode castNode = null;
        if (resolveVisitor == null) {
            resolveVisitor = new QueryASTResolveVisitor(sourceUnit);
        }
        dummyNode = ClassHelper.make(Dummy_123.class);
        dummyNode.setModule(sourceUnit.getAST());
        //ExpressionStatement exprStmt = new ExpressionStatement(castExpression);
        castNode = new FieldNode("castField", 0, ClassHelper.make(kind), dummyNode, cast);
        dummyNode.addField(castNode);

        try {
            resolveVisitor.startResolving(dummyNode);
            result = true;
        } catch (Exception e) {
            addError("The type '" + kind + " cannot be resolved", cast);
        } finally {
            dummyNode.setModule(null);
            dummyNode.getFields().clear();
        }
        return result;
    }

    public void addError(String msg, ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        sourceUnit.getErrorCollector().addError(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), sourceUnit));
    }

    public boolean validateEntityFieldName(CastExpression cast) {
        boolean result = false;
        if (cast == null) {
            return true;
        }
        ClassNode dummyNode = null;
        FieldNode castNode = null;
        if (resolveVisitor == null) {
            resolveVisitor = new QueryASTResolveVisitor(sourceUnit);
            dummyNode = ClassHelper.make(Dummy_123.class);
            dummyNode.setModule(sourceUnit.getAST());
            castNode = new FieldNode("castField", 0, ClassHelper.make("Person1"), dummyNode, cast);
            dummyNode.addField(castNode);

        }
        try {
            resolveVisitor.startResolving(dummyNode);

        } catch (Exception e) {
            addError("Valery", cast);
        }

        dummyNode.setModule(null);
        dummyNode.getFields().clear();
        return result;
    }

    @Override
    public void success(ClosureExpression closure, CastExpression cast) {
        this.stateList.add(new TransformState(closure, cast));
    }


    class QueryASTClassLoader extends GroovyClassLoader {

        private CompilationUnit compilationUnit;

        public QueryASTClassLoader(ClassLoader loader) {
            super(loader);
        }

        @Override
        protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource codeSource) {
            CompilationUnit cu = super.createCompilationUnit(config, codeSource);
            this.compilationUnit = cu;
            //cu.addPhaseOperation(new MySourceUnitOperation(), Phases.CONVERSION);
            return cu;
        }

        public CompilationUnit getCompilationUnit() {
            return compilationUnit;
        }
    }

    protected class QueryASTResolveVisitor {

        private SourceUnit sourceUnit;
        private ResolveVisitor resolveVisitor;

        public QueryASTResolveVisitor(SourceUnit sourceUnit) {
            ModuleNode moduleNode = sourceUnit.getAST();
            CompilerConfiguration config = moduleNode.getUnit().getConfig();
            GroovyClassLoader cl = moduleNode.getUnit().getClassLoader();
            QueryASTClassLoader mcl = new QueryASTClassLoader(cl);
            CompilationUnit compilationUnit = mcl.createCompilationUnit(config, moduleNode.getUnit().getCodeSource());
            resolveVisitor = new ResolveVisitor(compilationUnit);
        }

        public void startResolving(ClassNode classNode) {
            resolveVisitor.startResolving(classNode, sourceUnit);
        }
    }
}//class
