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

This project is published via [Sonatype](https://oss.sonatype.org/).

* Snapshots are available in the Maven repository at
  [https://oss.sonatype.org/content/repositories/snapshots](https://oss.sonatype.org/content/repositories/snapshots)
* Releases are available on [Maven Central](http://search.maven.org/).

Example 1, using a snapshot:

```scala
// In build.sbt
resolvers ++= Seq("sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")

libraryDependencies ++= Seq("com.miguno" % "kafka_embedded_2.10" % "0.1.0-SNAPSHOT")
```

Example 2, using a release (note: no release has been published yet!):

```scala
// In build.sbt
libraryDependencies ++= Seq("com.miguno" % "kafka_embedded_2.10" % "0.1.0")
```


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
