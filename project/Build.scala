import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "reservationStats"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    "com.typesafe.slick" % "slick_2.10" % "1.0.0",
    "mysql" % "mysql-connector-java" % "5.1.21",
    "org.elasticsearch" % "elasticsearch" % "0.90.1",
    "org.webjars" %% "webjars-play" % "2.1.0-2",
    "org.webjars" % "bootstrap" % "2.1.1",
    "org.webjars" % "angularjs" % "1.1.5-1"


  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "nexus CPD" at "http://nexus.cestpasdur.com/nexus/content/groups/everything/"


  )

}
