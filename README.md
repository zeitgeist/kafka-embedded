# kafka-embedded [![Build Status](https://travis-ci.org/miguno/kafka-embedded.png?branch=develop)](https://travis-ci.org/miguno/kafka-embedded)

Runs embedded, in-memory [Apache Kafka](http://kafka.apache.org) and [Apache ZooKeeper](http://zookeeper.apache.org)
instances.  Helpful for integration testing.

The code in this project was extracted from [kafka-storm-starter](https://github.com/miguno/kafka-storm-starter/) to
allow for easier reuse in other projects.

---

Table of Contents

* <a href="#Features">Features</a>
* <a href="#Requirements">Requirements</a>
* <a href="#Usage">Usage</a>
* <a href="#Changelog">Change log</a>
* <a href="#Contributing">Contributing</a>
* <a href="#License">License</a>

---

<a name="Features"></a>

# Features

With this project you can run in-memory instances of:

* [Kafka](http://kafka.apache.org/) --
  see [KafkaEmbedded](src/main/scala/com/miguno/kafka_embedded/kafka/KafkaEmbedded.scala)
* [ZooKeeper](http://zookeeper.apache.org) --
  see [ZooKeeperEmbedded](src/main/scala/com/miguno/kafka_embedded/zookeeper/ZooKeeperEmbedded.scala)
* A ready-to-use Kafka and ZooKeeper "cluster" --
  see [KafkaZooKeeperCluster](src/main/scala/com/miguno/kafka_embedded/kafka/KafkaZooKeeperCluster.scala)


<a name="Requirements"></a>

# Requirements

* Java 7
* Scala 2.10


<a name="Usage"></a>

# Usage

## Build dependencies

You must add two dependencies to use this project:

1. The dependency for kafka-embedded itself, see below for details.
2. The dependency for the specific Kafka version 0.8+ you want to use.  (Kafka-embedded sets its Kafka dependency to
   "provided" so that users can pull in whichever Kafka version they need.)


### Step 1: Adding a dependency for kafka-embedded

This project is published via [Sonatype](https://oss.sonatype.org/).

* Snapshots are available in the Maven repository at
  [https://oss.sonatype.org/content/repositories/snapshots](https://oss.sonatype.org/content/repositories/snapshots)
* Releases are available on [Maven Central](http://search.maven.org/).

When using a snapshot:

```scala
// In build.sbt
resolvers ++= Seq("sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")

libraryDependencies ++= Seq("com.miguno" % "kafka-embedded_2.10" % "0.1.0-SNAPSHOT")
```

When using a release (note: no release has been published yet!):

```scala
// In build.sbt
libraryDependencies ++= Seq("com.miguno" % "kafka-embedded_2.10" % "0.1.0")
```


### Step 2: Adding a dependency for Apache Kafka

You also need to pick a specific version of Kafka for your build.

Example:

```scala
// In build.sbt
libraryDependencies ++= Seq("org.apache.kafka" %% "kafka" % "0.8.1.1")
```

In many cases you want to exclude certain transitive dependencies of Kafka, e.g. Zookeeper.
See [build.sbt](build.sbt) for an example list of such excludes for Kafka.


## Examples

A full example of an integration test using in-memory instances of Kafka and ZooKeeper is available at
[KafkaZooKeeperClusterSpec](src/test/scala/com/miguno/kafka_embedded/integration/KafkaZooKeeperClusterSpec.scala).


<a name="Changelog"></a>

# Change log

See [CHANGELOG](CHANGELOG.md).


<a name="Contributing"></a>

# Contributing to kafka-embedded

Code contributions, bug reports, feature requests etc. are all welcome.

If you are new to GitHub please read [Contributing to a project](https://help.github.com/articles/fork-a-repo) for how
to send patches and pull requests to kafka-embedded.


<a name="License"></a>

# License

Copyright © 2014 Michael G. Noll

See [LICENSE](LICENSE) for licensing information.
