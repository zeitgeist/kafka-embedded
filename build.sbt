organization := "com.miguno"

name := "kafka-embedded"

crossScalaVersions := Seq("2.10.5", "2.11.8")

scalaVersion := "2.11.8"


resolvers ++= Seq(
  "typesafe-repository" at "http://repo.typesafe.com/typesafe/releases/",
  "clojars-repository" at "https://clojars.org/repo"
)

val kafkaVersion = "0.9.0.1"

libraryDependencies ++= Seq(
  // The excludes of jms, jmxtools and jmxri are required as per https://issues.apache.org/jira/browse/KAFKA-974.
  // The exclude of slf4j-simple is because it overlaps with our use of logback with slf4j facade;  without the exclude
  // we get slf4j warnings and logback's configuration is not picked up.
  "org.apache.kafka" %% "kafka" % kafkaVersion % "provided"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
    exclude("org.slf4j", "slf4j-simple")
    exclude("log4j", "log4j")
    exclude("org.apache.zookeeper", "zookeeper")
    exclude("com.101tec", "zkclient"),
  "com.101tec" % "zkclient" % "0.6"
    exclude("org.apache.zookeeper", "zookeeper"),
  "org.apache.curator" % "curator-test" % "2.9.0"
    exclude("org.jboss.netty", "netty")
    exclude("org.slf4j", "slf4j-log4j12"),
  "commons-io" % "commons-io" % "2.4",
  // Logback with slf4j facade
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  // Test dependencies
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)

// Required IntelliJ workaround.  This tells `sbt gen-idea` to include scala-reflect as a compile dependency (and not
// merely as a test dependency), which we need for TypeTag usage.
//libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)


// ---------------------------------------------------------------------------------------------------------------------
// "Runtime" settings
// ---------------------------------------------------------------------------------------------------------------------

// Enable forking (see sbt docs) because our full build (including tests) uses many threads.
fork := true

parallelExecution in ThisBuild := false

// The following options are passed to forked JVMs.
//
// Note: If you need to pass options to the JVM used by sbt (i.e. the "parent" JVM), then you should modify `.sbtopts`.
javaOptions ++= Seq(
  "-Xms256m",
  "-Xmx512m",
  "-XX:+UseG1GC",
  "-XX:MaxGCPauseMillis=20",
  "-XX:InitiatingHeapOccupancyPercent=35",
  "-Djava.awt.headless=true",
  "-Djava.net.preferIPv4Stack=true")


// ---------------------------------------------------------------------------------------------------------------------
// Compiler settings
// ---------------------------------------------------------------------------------------------------------------------

javacOptions in Compile ++= Seq(
  "-source", "1.7",
  "-target", "1.7",
  "-Xlint:unchecked",
  "-Xlint:deprecation")

scalacOptions ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

scalacOptions in Compile ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature",  // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code",
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

scalacOptions in Test ~= { (options: Seq[String]) =>
  options.filterNot(_ == "-Ywarn-value-discard").filterNot(_ == "-Ywarn-dead-code" /* to fix warnings due to Mockito */)
}

scalacOptions in ScoverageTest ~= { (options: Seq[String]) =>
  options.filterNot(_ == "-Ywarn-value-discard").filterNot(_ == "-Ywarn-dead-code" /* to fix warnings due to Mockito */)
}


// ---------------------------------------------------------------------------------------------------------------------
// Sonatype settings
// ---------------------------------------------------------------------------------------------------------------------

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/miguno/kafka-embedded</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:miguno/kafka-embedded.git</url>
    <connection>scm:git:git@github.com:miguno/kafka-embedded.git</connection>
  </scm>
  <developers>
    <developer>
      <id>miguno</id>
      <name>Michael G. Noll</name>
      <url>http://www.michael-noll.com/</url>
    </developer>
  </developers>)

// ---------------------------------------------------------------------------------------------------------------------
// ScalaTest settings
// ---------------------------------------------------------------------------------------------------------------------

// Write test results to file in JUnit XML format
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports/junitxml")

// Write test results to console.
//
// Tip: If you need to troubleshoot test runs, it helps to use the following reporting setup for ScalaTest.
//      Notably these suggested settings will ensure that all test output is written sequentially so that it is easier
//      to understand sequences of events, particularly cause and effect.
//      (cf. http://www.scalatest.org/user_guide/using_the_runner, section "Configuring reporters")
//
//        testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oUDT", "-eUDT")
//
//        // This variant also disables ANSI color output in the terminal, which is helpful if you want to capture the
//        // test output to file and then run grep/awk/sed/etc. on it.
//        testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oWUDT", "-eWUDT")
//
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-o")

// ---------------------------------------------------------------------------------------------------------------------
// scoverage settings
// ---------------------------------------------------------------------------------------------------------------------
// See https://github.com/scoverage/scalac-scoverage-plugin
instrumentSettings
