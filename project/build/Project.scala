import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) {

  val appengineVersion = "1.3.8"

  lazy val cache = project("cache", "Cache", new CacheProject(_))

  class CacheProject(info: ProjectInfo) extends DefaultProject(info) {

    val appengineApi = "com.google.appengine" % "appengine-api-1.0-sdk" % appengineVersion
    val ehCache = "net.sf.ehcache" % "ehcache" % "2.0.0"
    val javaxTransaction = "javax.transaction" % "jta" % "1.1"
    val slf4j = "org.slf4j" % "slf4j-api" % "1.6.0"

    val appengineTesting = "com.google.appengine" % "appengine-testing" % appengineVersion % "test"
    val appengineApiStubs = "com.google.appengine" % "appengine-api-stubs" % appengineVersion % "test"

    val scalaTest = "org.scalatest" % "scalatest" % "1.2" % "test" withSources()

  }


}