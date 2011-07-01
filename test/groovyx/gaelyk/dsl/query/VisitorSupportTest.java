package groovyx.gaelyk.dsl.query;

import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author V. Shyshkin
 */
public class VisitorSupportTest {
    
    public VisitorSupportTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of addVisitClosureHandler method, of class ClosureVisitorSupport.
     */
    @Test
    public void testAddVisitClosureHandler() {
        System.out.println("addVisitClosureHandler");
        VisitClosureHandler handler = null;
        ClosureVisitorSupport instance = new ClosureVisitorSupport();
        instance.addVisitClosureHandler(handler);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of visitClosureExpression method, of class ClosureVisitorSupport.
     */
    @Test
    public void testVisitClosureExpression() {
        System.out.println("visitClosureExpression");
        ClosureExpression expression = null;
        ClosureVisitorSupport instance = new ClosureVisitorSupport();
        instance.visitClosureExpression(expression);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAcceptable method, of class ClosureVisitorSupport.
     */
    @Test
    public void testIsAcceptable() {
        System.out.println("isAcceptable");
        ArgumentListExpression fromArgs = null;
        ClosureVisitorSupport instance = new ClosureVisitorSupport();
        boolean expResult = false;
        boolean result = instance.isAcceptable(fromArgs);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
