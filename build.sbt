import play.PlayJava

name := """ZipChat"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.4"


libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "redis.clients" % "jedis" % "2.6.0"
)

resolvers ++= Seq(
  "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk"
)

libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

libraryDependencies += "com.typesafe.play.plugins" %% "play-plugins-redis" % "2.3.1"

libraryDependencies += "com.ganyo" % "gcm-server" % "1.0.2"
