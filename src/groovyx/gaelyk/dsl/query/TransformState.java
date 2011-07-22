package groovyx.gaelyk.dsl.query;

import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;

/**
 * The instance of the class is created for each closure which is recognized
 * as a "query DSL".
 * Statements that are part of the closure are treated consistently.
 * Processing the next statement may depend on the  processing results of 
 * previous statements. For example, the processing result 
 * of the statement <code>orderBy</code> depends on either the closure represents 
 * a <code>cursor</code> or <code>select</code>. <p/>
 * Thus, instances of the class gather various information on each step 
 * of the transformation.
 * 
 * @author V. Shyshkin
 */
public class TransformState {
    /**
     * A closure for which this instance was created.
     */
    private ClosureExpression closure;
    /**
     * When <code>from</code> expression statement 
     * looks like <code>from entity as Person</code> or <code>null</code>.
     */
    private CastExpression kindCast;
    /**
     * The result of the transformation as a string
     */
    private String transformAsString;
    /**
     * A store of the <code>cursor</code> property 
     */
    private boolean cursor;
    /**
     * Used to  control fields in a orderBy if present
     */
    private String inequalityOperationField;
    private String inequalityOperation;
    private boolean kindlessAncestorQuery;
    /**
     * Creates an instance of the class.
     * @param closure an object to be transformed
     * @param kindCast <code>null or CastExpression</code>.
     */
    public TransformState(ClosureExpression closure, CastExpression kindCast) {
        this.closure = closure;
        this.kindCast = kindCast;
    }
    /**
     * @return 
     */
    public ClosureExpression getClosure() {
        return closure;
    }

    public CastExpression getKindCast() {
        return kindCast;
    }

    public String getTransformAsString() {
        return transformAsString;
    }

    public void setTransformAsString(String transformAsString) {
        this.transformAsString = transformAsString;
    }
    /**
     * @return <code>true</code> if the closure represents a cursor. 
     *      <code>false</code> otherwise
     */
    public boolean isCursor() {
        return cursor;
    }

    public void setCursor(boolean cursor) {
        this.cursor = cursor;
    }
    /**
     * @return a name of the field that participate in an inequality
     * expression of the <code>where</code> statement. <code>null</code>
     * if not present.
     */
    public String getInequalityOperationField() {
        return inequalityOperationField;
    }

    public void setInequalityOperationField(String inequalityOperationField) {
        this.inequalityOperationField = inequalityOperationField;
    }

    public String getInequalityOperation() {
        return inequalityOperation;
    }

    public void setInequalityOperation(String inequalityOperation) {
        this.inequalityOperation = inequalityOperation;
    }

    public boolean isKindlessAncestorQuery() {
        return kindlessAncestorQuery;
    }

    public void setKindlessAncestorQuery(boolean kindlessAncestorQuery) {
        this.kindlessAncestorQuery = kindlessAncestorQuery;
    }
    
}
