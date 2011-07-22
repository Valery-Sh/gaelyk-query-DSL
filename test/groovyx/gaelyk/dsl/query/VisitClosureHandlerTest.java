package groovyx.gaelyk.dsl.query;

import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Valery
 */
public class VisitClosureHandlerTest {
    
    public VisitClosureHandlerTest() {
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
     * Test of success method, of class VisitClosureHandler.
     */
    @Test
    public void testSuccess() {
        System.out.println("success");
        ClosureExpression closure = null;
        VisitClosureHandler instance = new VisitClosureHandlerImpl();
        //instance.success(closure);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    public class VisitClosureHandlerImpl implements VisitClosureHandler {
        @Override
        public void success(ClosureExpression closure, CastExpression fromKind){
        }
    }
}//class
