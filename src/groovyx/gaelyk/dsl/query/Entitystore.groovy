package groovyx.gaelyk.dsl.query

import com.google.appengine.api.datastore.DatastoreServiceFactory
/**
 *
 * @author V. Shyshkin
 */
class Entitystore {
    static Object executeQuery(Closure closure) {
        closure(DatastoreServiceFactory.datastoreService)
    }
    
/*    Object query(Closure closure) {
        closure(DatastoreServiceFactory.datastoreService)
    }
*/    

}

