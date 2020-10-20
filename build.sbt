name := "HW2"

version := "0.1"

scalaVersion := "2.13.3"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
scalacOptions := Seq("-target:jvm-1.8")
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "com.typesafe" % "config" % "1.4.0"
libraryDependencies += "junit" % "junit" % "4.13"
libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"
//libraryDependencies += "org.apache.hadoop" % "hadoop-core" % "1.2.1"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.3.0"
// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-common
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.3.0"
// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-mapreduce-client-core
libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.0" exclude("org.slf4j", "slf4j-log4j12")
// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-mapreduce-client-jobclient
//libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % "3.3.0" % "provided"


