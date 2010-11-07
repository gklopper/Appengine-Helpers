import appenginehelpers.HybridCache
import com.google.appengine.tools.development.testing.{LocalDatastoreServiceTestConfig, LocalServiceTestHelper}
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class CacheTest extends FunSuite with ShouldMatchers{

  val appengineEnvironment = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())

  test("should use appengine cache if appengine environment is running") {

    appengineEnvironment.setUp
    System.setProperty("com.google.appengine.runtime.version", "1.3.0")

    val stub = new Object with HybridCache

    stub.cache.getClass.getName should equal ("com.google.appengine.api.memcache.MemcacheServiceImpl")

    appengineEnvironment.tearDown
  }

  test("should use EHCache if appengine environment is down") {

    System.clearProperty("com.google.appengine.runtime.version")

    val stub = new Object with HybridCache

    stub.cache.getClass.getName should equal ("appenginehelpers.EhCacheWrapper")
  }
}