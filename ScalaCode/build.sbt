name := "ScalaCode"

version := "0.1"

scalaVersion := "2.12.15"

val sparkVersion = "3.2.0"

resolvers ++= Seq(
  "apache-snapshots" at "https://repository.apache.org/snapshots/",
  "maven-central" at "https://repo1.maven.org/maven2/"
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

// Add these settings to ensure proper dependency resolution
fork in run := true
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:+CMSClassUnloadingEnabled")