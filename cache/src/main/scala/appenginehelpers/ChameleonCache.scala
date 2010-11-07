package appenginehelpers

import com.google.appengine.api.utils.SystemProperty
import net.sf.ehcache.CacheManager
import com.google.appengine.api.memcache._
import net.sf.ehcache.config.{CacheConfiguration, Configuration}
import java.util.logging.Logger

trait ChameleonCache {

  private val logger = Logger.getLogger(classOf[ChameleonCache].getName)

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
      MemcacheServiceFactory.getMemcacheService
    }
    case None => {
      logger.info("Using EHCache")
      new EhCacheWrapper(CacheConfig.getCache)
    }
  }
}

private object CacheConfig {

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