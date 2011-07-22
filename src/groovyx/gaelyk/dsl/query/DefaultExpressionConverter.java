package groovyx.gaelyk.dsl.query;

import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.*;

import java.util.List;
import org.codehaus.groovy.ast.CodeVisitorSupport;

/**
 * The class helps to build a string from an AST that is a 
 * valid Groovy expression.
 * Most of the Groovy AST Expressions have a method with a name
 * "getText()" or like. As a rule the result of such method cannot be used 
 * as a Groovy  code. For example a string constant is represented as 
 * an identifier without quotes. This class helps to convert AST string
 * to a Groovy code string.
 * 
 * 
 * @author V. Shyshkin
 */
public class DefaultExpressionConverter extends CodeVisitorSupport implements GroovyCodeVisitor, ExpressionConverter {
    
    private String result = "";
    
    @Override
    public String convert(Expression expression) {
        return getResult(expression);
    }

    public String getResult(Expression expression) {
        result = "";
        expression.visit(this);
        return result;
    }
    public String getResult(List<? extends Expression> expressions) {
        result = "";
        this.visitListOfExpressions(expressions);
        return result;
    }
    
    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        Expression oe = call.getObjectExpression();
        Expression me = call.getMethod();
        if (oe instanceof VariableExpression) {
            this.result += oe.getText();
        } else {
            // ??? call.getObjectExpression().visit(this);
            oe.visit(this);
        }
        result += ".";
        if (me instanceof VariableExpression) {
            this.result += me.getText();
        } else {
            // ??? call.getMethod().visit(this);
            me.visit(this);
        }
        call.getArguments().visit(this);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression prop) {
        Expression oe = prop.getObjectExpression();
        Expression pe = prop.getProperty();
        if (oe instanceof VariableExpression) {
            this.result += oe.getText();
        } else {
            oe.visit(this);
        }
        this.result += ".";

        if (pe instanceof VariableExpression) {
            this.result += pe.getText();
        } else {
            pe.visit(this);
        }
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        Object value = expression.getValue();
        if (value instanceof String) {
            result += "\"" + value + "\"";
        } else {
            result += value;
        }
    }
    @Override
    public void visitVariableExpression(VariableExpression expression) {
        result += expression.getText();
    }
    
    @Override
    public void visitClassExpression(ClassExpression expression) {
        result += expression.getText();
    }


    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        call.getArguments().visit(this);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        result += "new " + call.getType().getName();
        call.getArguments().visit(this);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expr) {
        Expression le = expr.getLeftExpression();
        Expression re = expr.getRightExpression();
        result += "(";
        if (le instanceof VariableExpression) {
            this.result += le.getText();
        } else {
            expr.getLeftExpression().visit(this);
        }
        this.result += expr.getOperation().getText();

        if (re instanceof VariableExpression) {
            this.result += re.getText();
        } else {
            expr.getRightExpression().visit(this);
        }
        if ( expr.getOperation().getText() == "[" ) {
            result += "]";
        }
        result += ")";
    }
    @Override
    public void visitListExpression(ListExpression listExpr) {
        result += "[";
        visitListOfExpressions(listExpr.getExpressions());
        result += "]";
    }

    @Override
    protected void visitListOfExpressions(List<? extends Expression> list) {
        if (list == null) {
            return;
        }
        int i = 0;
        for (Expression expression : list) {
            if (expression instanceof SpreadExpression) {
                Expression spread = ((SpreadExpression) expression).getExpression();
                spread.visit(this);
            } else {
                if (expression instanceof VariableExpression) {
                    result += expression.getText();
                } else {
                    expression.visit(this);
                }

            }
            if (i++ != list.size() - 1) {
                result += ",";
            }
            
        }
    }
    @Override
    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        result += "(";
        visitTupleExpression(ale);
        result += ")";

    }
    
    /*
     *  Not yet implemented ( overriden )
     */
/*    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
        expression.getBooleanExpression().visit(this);
        expression.getTrueExpression().visit(this);
        expression.getFalseExpression().visit(this);
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitNotExpression(NotExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    
    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        visitListOfExpressions(expression.getExpressions());
        visitListOfExpressions(expression.getSizeExpression());
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
        visitListOfExpressions(expression.getMapEntryExpressions());

    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
        expression.getKeyExpression().visit(this);
        expression.getValueExpression().visit(this);

    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
        expression.getFrom().visit(this);
        expression.getTo().visit(this);
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        expression.getExpression().visit(this);
    }
*/    
}
