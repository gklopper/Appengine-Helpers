Appengine version      Helpers version
1.4.0                  1.1
1.4.2                  1.2-SNAPSHOT
1.4.2                  1.3-SNAPSHOT - changed to use a simple cache interface.


//Maven repository
val githubMvn = "Github repository" at "http://mvn.github.com/repository"

//Dependencies
val hybridCache = "appengine-helpers" %% "cache" % "1.3-SNAPSHOT"
val urlFetcher = "appengine-helpers" %% "urlfetcher" % "1.3-SNAPSHOT"