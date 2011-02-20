package appenginehelpers

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.google.appengine.tools.development.testing.{LocalDatastoreServiceTestConfig, LocalServiceTestHelper}

class UrlFetcherTest extends FunSuite with ShouldMatchers {

  test("should fetch url") {
    System clearProperty "com.google.appengine.runtime.version"
    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/param?val=test+string")

      response should equal (Some("test string"))
    }
  }

  test("should not cache result") {
    System clearProperty "com.google.appengine.runtime.version"
    new UrlFetcher {
      val firstResponse = GET("http://functional-tests.appspot.com/random")
      val secondResponse = GET("http://functional-tests.appspot.com/random")

      firstResponse should not equal secondResponse
    }
  }

  test("should return None on error") {
    System clearProperty "com.google.appengine.runtime.version"
    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/404")

      response should be (None)
    }
  }

  /*

  These tests will fail in 1.4.2
  http://code.google.com/p/googleappengine/issues/detail?id=4579

  test("should fetch remotely if not in cache") {
    System setProperty ("com.google.appengine.runtime.version", "1.3.0")
    appengineHelper setUp

    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/param?val=test")
      response should equal (Some("test"))
    }
  }


  test("should fetch from cache") {
    System setProperty ("com.google.appengine.runtime.version", "1.3.0")
    appengineHelper setUp

    new UrlFetcher {
      val firstResponse = GET("http://functional-tests.appspot.com/random", 10 seconds)
      val secondResponse = GET("http://functional-tests.appspot.com/random")
      firstResponse should equal (secondResponse)
    }
  }
  */

  def appengineHelper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())

}