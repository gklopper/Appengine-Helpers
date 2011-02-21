package appenginehelpers

import com.google.appengine.api.utils.SystemProperty
import java.net.{HttpURLConnection, URL}
import java.util.logging.Logger._
import com.google.appengine.api.memcache.{Expiration, MemcacheServiceFactory}
import java.net.URLEncoder
import java.io.{Serializable, BufferedInputStream, InputStream}
import scala.collection.JavaConversions._

trait UrlFetcher {

  private lazy val log = getLogger (classOf[UrlFetcher].getName)

  private lazy val inAppengine = appVersion.isDefined
  private lazy val appVersion = Option(SystemProperty.version.get)
  private lazy val cache = MemcacheServiceFactory.getMemcacheService("url-fetcher-" + appVersion.getOrElse("no-version"))

  implicit def string2url(url: String) = new URL(url)
  implicit def int2expiration(expirationSeconds: Int) = ExpirationSeconds(expirationSeconds)
  implicit def map2option(map: Map[String, String]) = Some(map)

  def GET(url: String, params: Option[Map[String, String]] = None, cacheFor: ExpirationSeconds = 0 seconds): Response = {

    val paramString = params match {
      case Some(p) => p.foldLeft("?")({case (original, (key, value)) => original + key + "=" + URLEncoder.encode(value, "UTF-8") + "&"})
      case _ => ""
    }

    if (inAppengine) handlePotentiallyCached(url + paramString, cacheFor)
      else fetchRemote(url + paramString)
  }

  private def fetchRemote(url: URL) = {
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.getResponseCode match {
      case 200 => {
        log.info("remote fetch succeeded: " + url)
        Response(200, Some(readAsString(connection.getInputStream)), extractHeaders(connection.getHeaderFields))
      }
      case differentCode => {
        log.info("remote fetch failed " + differentCode + ": " + url)
        Response(differentCode, None, extractHeaders(connection.getHeaderFields))
      }
    }
  }

  private def handlePotentiallyCached(url: URL, expiration: ExpirationSeconds): Response = {
    Option(cache.get(url)) match {
      case Some(response: Response) => {
        log.info("cache hit: " + url)
        response
      }
      case None => {
        log.info("cache miss: " + url)
        val response = fetchRemote(url)
        if (expiration.shouldCache && response.cacheable) {
          log.info("caching: " + url)
          cache.put(url, response, Expiration.byDeltaSeconds(expiration.expirationSeconds))
        }
        response
      }
    }
  }

  private def readAsString(in: InputStream) = {
    val builder = new StringBuilder
    val buff = new BufferedInputStream(in)
    Stream.continually(buff.read).takeWhile(_ != -1).foreach(i => builder.append(i.toChar))
    builder toString
  }

  private def extractHeaders(headers: java.util.Map[String, java.util.List[String]]) = {
    headers.map({case (key, values) => key -> values.last}).toMap
  }
}

case class Response(responseCode: Int, body: Option[String], headers: Map[String, String]) extends Serializable {
  lazy val cacheable = responseCode == 200
}

case class ExpirationSeconds(expirationSeconds: Int) {
  lazy val seconds = ExpirationSeconds(expirationSeconds)
  lazy val minutes = ExpirationSeconds(expirationSeconds * 60)
  lazy val hours = ExpirationSeconds(expirationSeconds * 60 * 60)

  lazy val shouldCache = expirationSeconds > 0
}