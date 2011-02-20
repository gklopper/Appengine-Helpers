package appenginehelpers

import com.google.appengine.api.utils.SystemProperty
import java.net.{HttpURLConnection, URLConnection, URL}
import java.io.{BufferedInputStream, InputStream}
import java.util.logging.Logger._
import com.google.appengine.api.memcache.{Expiration, MemcacheServiceFactory}
import java.net.URLEncoder


trait UrlFetcher {

  private lazy val log = getLogger (classOf[UrlFetcher].getName)

  private lazy val inAppengine = Option(appVersion) match {
    case Some(_) => true
    case None => false
  }

  private lazy val appVersion = SystemProperty.version.get
  private lazy val cache = MemcacheServiceFactory.getMemcacheService(appVersion)

  implicit def string2url(url: String) = new URL(url)
  implicit def int2expiration(expirationSeconds: Int) = ExpirationSeconds(expirationSeconds)

  def GET(url: String, params: Map[String, String]): Option[String] = GET(url, params, 0 seconds)

  def GET(url: URL): Option[String] = GET(url, 0 seconds)

  def GET(url: String, params: Map[String, String], expiration: ExpirationSeconds): Option[String] = {
    val paramString = params.foldLeft("?")({case (original, (key, value)) => original + key + "=" + URLEncoder.encode(value, "UTF-8") + "&"})
    GET(url + paramString, expiration)
  }

  def GET(url: URL, expiration: ExpirationSeconds): Option[String] = if (inAppengine) handlePotentiallyCached(url, expiration) else fetchRemote(url)

  private def fetchRemote(url: URL) = {
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.getResponseCode match {
      case 200 => {
        log.info("remote fetch succeded: " + url)
        Some(readAsString(connection.getInputStream))
      }
      case differentCode => {
        log.info("remote fetch failed " + differentCode + ": " + url)
        None
      }
    }
  }

  private def handlePotentiallyCached(url: URL, expiration: ExpirationSeconds): Option[String] = {
    Option(cache.get(url)) match {
      case Some(result: String) => {
        log.info("cache hit: " + url)
        Some(result)
      }
      case None => {
        log.info("cache miss: " + url)
        val result = fetchRemote(url)
        if (expiration shouldCache) {
          log.info("caching: " + url)
          result.foreach(cache.put(url, _, Expiration.byDeltaSeconds(expiration.expirationSeconds)))
        }
        result
      }
    }
  }

  private def readAsString(in: InputStream) = {
    val builder = new StringBuilder
    val buff = new BufferedInputStream(in)
    Stream.continually(buff.read).takeWhile(_ != -1).foreach(i => builder.append(i.toChar))
    builder toString
  }
}

case class ExpirationSeconds(expirationSeconds: Int) {
  def seconds = ExpirationSeconds(expirationSeconds)
  def minutes = ExpirationSeconds(expirationSeconds * 60)
  def hours = ExpirationSeconds(expirationSeconds * 60 * 60)

  lazy val shouldCache = expirationSeconds > 0
}