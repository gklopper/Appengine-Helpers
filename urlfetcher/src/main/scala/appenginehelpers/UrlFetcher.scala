package appenginehelpers

import com.google.appengine.api.utils.SystemProperty
import java.net.{HttpURLConnection, URLConnection, URL}
import java.io.{BufferedInputStream, InputStream}
import java.util.logging.Logger._
import com.google.appengine.api.memcache.{Expiration, MemcacheServiceFactory}

trait UrlFetcher {

  private lazy val log = getLogger (classOf[UrlFetcher].getName)

  private lazy val inAppengine = SystemProperty.environment.value match {
    case SystemProperty.Environment.Value.Production => true
    case _ => false
  }
  private lazy val appVersion = SystemProperty.applicationVersion.get
  private lazy val cache = MemcacheServiceFactory.getMemcacheService(appVersion)

  implicit def string2url(url: String) = new URL(url)
  implicit def int2expiration(expirationSeconds: Int) = ExpirationSeconds(expirationSeconds)

  def GET(url: URL): Option[String] = GET(url, 0 seconds)

  def GET(url: URL, expiration: ExpirationSeconds): Option[String] = {

    if (inAppengine) Option(cache.get(url)) match {
      case Some(result: String) => Some(result)
      case None => {
        val result = fetchRemote(url)
        cache.put(url, result, Expiration.byDeltaSeconds(expiration.expirationSeconds))
        result
      }
    } else {
      fetchRemote(url)
    }
  }

  private def fetchRemote(url: URL) = {
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.getResponseCode match {
      case 200 => Some(readAsString(connection.getInputStream))
      case _ => None
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
}