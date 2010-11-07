package appenginehelpers

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class EhCacheWrapperTest extends FunSuite with ShouldMatchers with BeforeAndAfterEach {

  //ensure we are not operating on a google memcache
  System.clearProperty("com.google.appengine.runtime.version")

  override def beforeEach = new Object with ChameleonCache {cache.clearAll}
  override def afterEach = beforeEach

  test("should put and get a value") {
    new Object with ChameleonCache {
      cache.put("key", "value")
      cache.get("key") should equal ("value")
    }
  }

  test("should return null if not found") {
    new Object with ChameleonCache {
      cache.get("key2") should equal (null)
    }
  }

  test("should clear a value") {
    new Object with ChameleonCache {
      cache.put("key", "value")
      cache.delete("key") should equal (true)
      cache.delete("key") should equal (false)
      cache.get("key") should equal (null)
    }
  }

  test("should know if something is in the cache") {
    new Object with ChameleonCache {
      cache.put("key", "value")
      cache.contains("key") should equal (true)
      cache.contains("missing") should equal (false)
    }
  }

  test("should remove all items") {
    new Object with ChameleonCache {
      cache.put("key", "value")
      cache.put("key2", "value2")
      cache.clearAll
      cache.contains("key") should equal (false)
      cache.contains("key2") should equal (false)
    }
  }

  test("should use same cache") {
    new Object with ChameleonCache {
      cache.put("key", "value")
    }

    new Object with ChameleonCache {
      cache.contains("key") should equal (true)
    }
  }

  test("should add with expiration") {
    new Object with ChameleonCache {
      cache.put("key", "value", 1 second)
      cache.get("key") should equal ("value")
      Thread.sleep(1000)
      cache.get("key") should equal (null)
    }
  }
}