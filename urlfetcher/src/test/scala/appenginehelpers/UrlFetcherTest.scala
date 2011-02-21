package appenginehelpers

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.google.appengine.tools.development.testing.{LocalDatastoreServiceTestConfig, LocalServiceTestHelper}

class UrlFetcherTest extends FunSuite with ShouldMatchers {

  test("should fetch url") {
    System clearProperty "com.google.appengine.runtime.version"
    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/param?val=test+string")

      response match {
        case Response(code, Some(body), headers) =>
          code should equal (200)
          body should equal ("test string")
      }
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

      response match {
        case Response(code, body, headers) =>
          code should equal (404)
          body should be (None)
      }
    }
  }

  test("should fetch remotely if not in cache") {
    System setProperty ("com.google.appengine.runtime.version", "1.3.0")
    appengineHelper setUp

    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/param?val=test")
      response match {
        case Response(code, Some(body), headers) =>
          code should equal (200)
          body should equal ("test")
      }
    }
  }


  test("should fetch from cache") {
    System setProperty ("com.google.appengine.runtime.version", "1.3.0")
    appengineHelper setUp

    new UrlFetcher {
      val firstResponse = GET("http://functional-tests.appspot.com/random", cacheFor = 10 seconds)
      val secondResponse = GET("http://functional-tests.appspot.com/random")
      firstResponse should equal (secondResponse)
    }
  }

  test("should build and encode parameters from map") {
    System clearProperty "com.google.appengine.runtime.version"

    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/param", params = Map("val" -> "hello world"))

      response match {
        case Response(code, Some(body), headers) =>
          code should equal(200)
          body should equal ("hello world")
      }
    }

  }

  test("should include headers") {
    System clearProperty "com.google.appengine.runtime.version"

    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/header", params = Map("val" -> "2000"))

      response match {
        case Response(code, _, headers) =>
          code should equal(200)
          headers("custom-header") should equal ("2000")
      }
    }
  }

  test("should fetch from cache with params") {
    System setProperty ("com.google.appengine.runtime.version", "1.3.0")
    appengineHelper setUp

    new UrlFetcher {
      val firstResponse = GET("http://functional-tests.appspot.com/random", params = Map("foo" -> "bar"), cacheFor = 10 seconds)
      val secondResponse = GET("http://functional-tests.appspot.com/random", params = Map("foo" -> "bar"))
      firstResponse should equal (secondResponse)
    }
  }

  def appengineHelper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())

}