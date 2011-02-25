package appenginehelpers

import com.google.appengine.api.utils.SystemProperty
import net.sf.ehcache.CacheManager
import com.google.appengine.api.memcache._
import net.sf.ehcache.config.{CacheConfiguration, Configuration}
import java.util.logging.Logger

trait HybridCache {
  private val logger = Logger.getLogger(classOf[HybridCache].getName)

  implicit def int2expiringInt(timeToLiveInSeconds: Int) = ExpiringInt(timeToLiveInSeconds)

  case class ExpiringInt(timeToLiveInSeconds: Int) {
    lazy val seconds = Expiration.byDeltaSeconds(timeToLiveInSeconds)
    lazy val second = seconds
    lazy val minutes = Expiration.byDeltaSeconds(timeToLiveInSeconds * 60)
    lazy val minute = minutes
    lazy val hours = Expiration.byDeltaSeconds(timeToLiveInSeconds * 60 * 60)
    lazy val hour = hours
    lazy val days = Expiration.byDeltaSeconds(timeToLiveInSeconds * 60 * 60 * 24)
    lazy val day = days
  }

  lazy val cache = Option(SystemProperty.version.get) match {
    case Some(_) => {
      logger.info("Using App Engine cache")
      new SimpleCache(MemcacheServiceFactory.getMemcacheService)
    }
    case None => {
      logger.info("Using EHCache")
      new SimpleCache(new EhCacheWrapper(EhCacheConfig.getCache))
    }
  }
}

private object EhCacheConfig {
  val cacheConfiguration = new CacheConfiguration("default", 5000)
  cacheConfiguration.setDiskPersistent(false)
  cacheConfiguration.setEternal(false)
  cacheConfiguration.setOverflowToDisk(false)
  val configuration = new Configuration()
  configuration.setDefaultCacheConfiguration(cacheConfiguration)

  val cacheManager = new CacheManager(configuration)
  cacheManager.addCache("default")

  def getCache = cacheManager.getCache("default")
}

class SimpleCache(cache: MemcacheService) {

  def put(key: AnyRef, value: AnyRef) = cache.put(key, value)

  def put(key: AnyRef, value: AnyRef, expiration: Expiration) = cache.put(key, value, expiration)

  def get(key: AnyRef) = Option(cache.get(key)) match {
    case None => None
    case hit => hit
  }

  def delete(key: AnyRef) = cache.delete(key)

  def contains(key: AnyRef) = cache.contains(key)

  def clearAll = cache.clearAll

}
