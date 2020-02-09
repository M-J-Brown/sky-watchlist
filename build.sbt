name := """sky-watchlist"""
organization := "dev.matthewbrown"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalacOptions += "-language:implicitConversions"

scalaVersion := "2.13.1"

libraryDependencies += guice

libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
libraryDependencies += "org.typelevel" %% "mouse" % "0.24"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.scalamock" %% "scalamock" % "4.4.0" % Test
