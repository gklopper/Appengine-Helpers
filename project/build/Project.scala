import java.io.File
import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) {

  lazy val cache = project("cache", "Cache", new CacheProject(_))
  lazy val urlfetcher = project("urlfetcher", "UrlFetcher", new UrlFetcherProject(_))

  class UrlFetcherProject(info : ProjectInfo) extends AppengineProject(info) {

  }

  class CacheProject(info: ProjectInfo) extends AppengineProject(info) {
    val ehCache = "net.sf.ehcache" % "ehcache" % "2.0.0"
    val javaxTransaction = "javax.transaction" % "jta" % "1.1"
    val slf4j = "org.slf4j" % "slf4j-api" % "1.6.0"

    val scalaTest = "org.scalatest" % "scalatest" % "1.2" % "test" withSources()
  }

  class AppengineProject(info: ProjectInfo) extends DefaultProject(info) {

    override def managedStyle = ManagedStyle.Maven
    lazy val publishTo = Resolver.file("Github", new File("../mvn.github.com/repository/"))

    val appengineVersion = "1.4.2"
    val appengineApi = "com.google.appengine" % "appengine-api-1.0-sdk" % appengineVersion
    val appengineTesting = "com.google.appengine" % "appengine-testing" % appengineVersion % "test"
    val appengineApiStubs = "com.google.appengine" % "appengine-api-stubs" % appengineVersion % "test"
  }


}