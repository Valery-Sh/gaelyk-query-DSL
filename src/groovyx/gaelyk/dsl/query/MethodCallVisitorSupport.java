package groovyx.gaelyk.dsl.query;

import java.util.ArrayList;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;

import java.util.List;
import org.codehaus.groovy.ast.CodeVisitorSupport;

/**
 * The purpose of the class is to find a closure and define 
 * either that closure represents a "query DSL" or not.
 * When found than a registered object of type @{link VisitClosureHandler}
 * is notified by invoking a method <code>success</code> on it.
 * <p/>
 * For now a closure is considered to be a "query DSL" if
 * it has a method call expression with a method name <code>"from"</code> 
 * and which accepts one or two parameters. I think it is a 
 * bad decision. It would be better to look for a method call 
 * expression with a method name "query" and <code>ObjectExpression</code>
 * as <code>"datastore"</code>.
 * 
 * @author V. Shyshkin
 */
public class MethodCallVisitorSupport extends CodeVisitorSupport {

    public static final int MAX_STATEMENT_COUNT = 6;
    public static final int MIN_STATEMENT_COUNT = 2;
    protected VisitClosureHandler visitHandler;

    /**
     * Registers a handler to be notified when a closure is a "query DSL"
     * closure.
     * 
     * @param handler a handler to be registered
     */
    public void addVisitClosureHandler(VisitClosureHandler handler) {
        this.visitHandler = handler;
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Returns an object of type <code>CastExpression</code> for a given
     * method call expression and a given argument list expression. 
     * @param call method call with a name "from"
     * @param argList an object of type <code>ArgumentListExpression</code>
     * @return 
     */
    public CastExpression getCastExpression(MethodCallExpression call, ArgumentListExpression argList) {

        List<Expression> exprs = argList.getExpressions();
        if (exprs == null || exprs.isEmpty() || exprs.size() > 2) {
            return null;
        }

        if (!(exprs.get(0) instanceof CastExpression)) {
            return null;
        }


        String kind = "";
        String pojo = "";
        CastExpression castExpr = (CastExpression) exprs.get(0);
        Expression pojoExpr = castExpr.getExpression();
        kind = getKindFromCastExpression(castExpr.getText());
        if (!(pojoExpr instanceof VariableExpression)) {
            return null;
        }
        pojo = pojoExpr.getText();
        if (pojo.toUpperCase().contains("POJO")
                || pojo.toUpperCase().contains("BEAN")) {
            pojo = "pojo";
        } else if (pojo.toUpperCase().contains("ENTITY")) {
            pojo = "entity";
        } else {
            return null;
        }

        return castExpr;
    }

    public CastExpression getCastExpression(ClosureExpression expression) {

        List<Statement> statements = ((BlockStatement) expression.getCode()).getStatements();
        MethodCallExpression fromCall = null;
        List<Expression> exprList = new ArrayList(5);
        CastExpression castExpr = null;

        for (Statement statement : statements) {
            if (!(statement instanceof ExpressionStatement)) {
                break;
            }
            Expression expr = ((ExpressionStatement) statement).getExpression();
            if (expr instanceof VariableExpression) {
                String nm = ((VariableExpression) expr).getName();
                String tx = ((VariableExpression) expr).getText();

                if (!(Helper.isSupported(nm) && nm.equals(tx))) {
                    break;
                }
            } else if (expr instanceof MethodCallExpression) {
                String nm = ((MethodCallExpression) expr).getMethod().getText();
                //String tx = ((MethodCallExpression) expr).getText();
                if (!(Helper.isSupported(nm))) {
                    break;
                }
                if (nm.equals("from") || nm.equals("From")) {
                    fromCall = (MethodCallExpression) expr;
                }
            }
            exprList.add(expr);

            if (fromCall != null && (fromCall instanceof MethodCallExpression)) {
                Expression fromArgsExpr = fromCall.getArguments();
                if (fromArgsExpr != null && fromArgsExpr instanceof ArgumentListExpression) {
                    if (isAcceptable((ArgumentListExpression) fromArgsExpr)) {
                        castExpr = getCastExpression(fromCall, (ArgumentListExpression) fromArgsExpr);
                    }
                }
            }
        }
        return castExpr;
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

    /**
     * @param fromArgs
     * @return 
     */
    public boolean isAcceptable(ArgumentListExpression fromArgs) {

        boolean result = true;
        List<Expression> exprs = fromArgs.getExpressions();
        if (exprs == null || exprs.isEmpty() || exprs.size() > 2) {
            return false;
        }

        if (!((exprs.get(0) instanceof VariableExpression)
                || (exprs.get(0) instanceof MethodCallExpression)
                || (exprs.get(0) instanceof BinaryExpression)
                || (exprs.get(0) instanceof CastExpression))) {
            return false;
        }
        if (exprs.get(0) instanceof CastExpression) {
            String kind = "";
            String pojo = "";
            CastExpression castExpr = (CastExpression) exprs.get(0);
            Expression pojoExpr = castExpr.getExpression();
            kind = getKindFromCastExpression(castExpr.getText());
            if (!(pojoExpr instanceof VariableExpression)) {
                return false;
            }

            pojo = pojoExpr.getText();
            if (pojo.toUpperCase().contains("POJO")
                    || pojo.toUpperCase().contains("BEAN")) {
                pojo = "pojo";
            } else if (pojo.toUpperCase().contains("ENTITY")) {
                pojo = "entity";
            } else {
                return false;
            }
        }

        return result;
    }

    /**
     * Try to recognize a closure as a "query DSL".
     * @param expression a method call expression to be treated
     */
    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        boolean found = false;
        if (call.getObjectExpression() instanceof VariableExpression) {
            VariableExpression vexpr = (VariableExpression) call.getObjectExpression();
            if (vexpr.getText().equals("datastore") || vexpr.getText().equals("Entitystore")) {
                if (call.getMethod() instanceof ConstantExpression) {
                    ConstantExpression cexpr = (ConstantExpression) call.getMethod();
                    if (cexpr.getValue().equals("executeQuery") || cexpr.getValue().equals("defineQuery")) {
                        if (call.getArguments() instanceof ArgumentListExpression) {
                            ArgumentListExpression argListExpr = (ArgumentListExpression) call.getArguments();
                            if (argListExpr.getExpressions().size() == 1 && (argListExpr.getExpressions().get(0) instanceof ClosureExpression)) {
                                found = true;
                                ClosureExpression closureExpr = (ClosureExpression) argListExpr.getExpressions().get(0);
                                visitHandler.success(closureExpr, getCastExpression(closureExpr));
                            }
                        }

                    }//if
                }//if
            }//if
        }//if
        if (!found) {
            super.visitMethodCallExpression(call);
        }

    }
}//class ClosureVisitorSupport
