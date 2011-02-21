package appenginehelpers

import org.scalatest.matchers.ShouldMatchers
import com.google.appengine.tools.development.testing.{LocalDatastoreServiceTestConfig, LocalServiceTestHelper}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class UrlFetcherTest extends FunSuite with ShouldMatchers with BeforeAndAfterEach {

  val appengineHelper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
  val appengineVersion = "com.google.appengine.runtime.version"

  override def beforeEach = appengineHelper.setUp

  override def afterEach = appengineHelper.tearDown

  test("should fetch url") {

    System clearProperty appengineVersion

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
    System clearProperty appengineVersion
    new UrlFetcher {
      val firstResponse = GET("http://functional-tests.appspot.com/random")
      val secondResponse = GET("http://functional-tests.appspot.com/random")

      firstResponse should not equal secondResponse
    }
  }

  test("should return None on error") {
    System clearProperty appengineVersion
    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/404")

      response match {
        case Response(code, body, _) =>
          code should equal (404)
          body should be (None)
      }
    }
  }

  test("should fetch remotely if not in cache") {
    System setProperty (appengineVersion, "1.3.0")

    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/param?val=test")
      response match {
        case Response(code, Some(body), _) =>
          code should equal (200)
          body should equal ("test")
      }
    }
  }


  test("should fetch from cache") {
    System setProperty (appengineVersion, "1.3.0")

    new UrlFetcher {
      val firstResponse = GET("http://functional-tests.appspot.com/random", cacheFor = 10 seconds)
      val secondResponse = GET("http://functional-tests.appspot.com/random")
      firstResponse should equal (secondResponse)
    }
  }

  test("should build and encode parameters from map") {
    System clearProperty appengineVersion

    new UrlFetcher {
      val response = GET("http://functional-tests.appspot.com/param", params = Map("val" -> "hello world"))

      response match {
        case Response(code, Some(body), _) =>
          code should equal(200)
          body should equal ("hello world")
      }
    }

  }

  test("should include headers") {
    System clearProperty appengineVersion

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
    System setProperty (appengineVersion, "1.3.0")

    new UrlFetcher {
      val firstResponse = GET("http://functional-tests.appspot.com/random", params = Map("foo" -> "bar"), cacheFor = 10 seconds)
      val secondResponse = GET("http://functional-tests.appspot.com/random", params = Map("foo" -> "bar"))
      firstResponse should equal (secondResponse)
    }
  }
}