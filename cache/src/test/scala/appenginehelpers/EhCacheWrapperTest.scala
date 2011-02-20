package appenginehelpers

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import com.google.appengine.tools.development.testing.{LocalServiceTestHelper, LocalDatastoreServiceTestConfig}

class EhCacheWrapperTest extends FunSuite with ShouldMatchers with BeforeAndAfterEach {

  val appengineEnvironment = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())

  override def beforeEach = {
    System.clearProperty("com.google.appengine.runtime.version")
    new Object with HybridCache {cache.clearAll}
  }

  override def afterEach = beforeEach

  def testBoth(message: String)(testFunction: => Unit) {
    
    test(message + " (EhCahe)") { testFunction }

    /*
    These tests will fail in 1.4.2
    http://code.google.com/p/googleappengine/issues/detail?id=4579

    test(message + " (Appengine cache)") {
      System.setProperty("com.google.appengine.runtime.version", "1.1.1")
      appengineEnvironment.setUp
      try testFunction finally appengineEnvironment.tearDown
    }
    */
  }


  testBoth("should put and get a value") {
    new Object with HybridCache {
      cache.put("key", "value")
      cache.get("key") should equal("value")
    }
  }

  testBoth("should return null if not found") {
    new Object with HybridCache {
      cache.get("key") should equal(null)
    }
  }

  testBoth("should clear a value") {
    new Object with HybridCache {
      cache.put("key", "value")
      cache.delete("key") should equal(true)
      cache.delete("key") should equal(false)
      cache.get("key") should equal(null)
    }
  }

  testBoth("should know if something is in the cache") {
    new Object with HybridCache {
      cache.put("key", "value")
      cache.contains("key") should equal(true)
      cache.contains("missing") should equal(false)
    }
  }

  testBoth("should remove all items") {
    new Object with HybridCache {
      cache.put("key", "value")
      cache.put("key2", "value2")
      cache.clearAll
      cache.contains("key") should equal(false)
      cache.contains("key2") should equal(false)
    }
  }

  testBoth("should use same cache") {

      new Object with HybridCache {
        cache.put("key", "value")
      }

      new Object with HybridCache {
        cache.contains("key") should equal(true)
      }

  }

  testBoth("should add with expiration") {
    new Object with HybridCache {
      cache.put("key", "value", 1 second)
      cache.get("key") should equal("value")
      Thread.sleep(2000)
      cache.get("key") should equal(null)
    }
  }
}