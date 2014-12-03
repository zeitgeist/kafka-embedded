package com.miguno.kafka_embedded.kafka

import java.util.Properties

import com.miguno.kafka_embedded.logging.LazyLogging
import com.miguno.kafka_embedded.zookeeper.ZooKeeperEmbedded
import org.apache.curator.test.InstanceSpec

/**
 * Starts embedded instances of a Kafka broker and a ZooKeeper server.  Used only for testing.
 *
 * @param zookeeperPort ZooKeeper port
 * @param kafkaPort: Kafka Port
 * @param topics Kafka topics to be created
 * @param brokerConfig Kafka broker configuration
 */
class EmbeddedKafkaZooKeeperCluster(zookeeperPort: Integer = InstanceSpec.getRandomPort,
                                    kafkaPort: Integer = InstanceSpec.getRandomPort,
                                    topics: Seq[KafkaTopic] = Seq(),
                                    brokerConfig: Properties = new Properties) extends LazyLogging {

  type Key = Array[Byte]
  type Val = Array[Byte]

  var zookeeper: ZooKeeperEmbedded = _
  var kafka: KafkaEmbedded = _

  // We intentionally use a fail-fast approach here, i.e. we do not use e.g. [[Option]] when creating ZooKeeper or Kafka
  // instances.  This makes the downstream test code simpler because we want our tests to fail immediately in case we
  // run into problems here.
  // TODO: Do we need idempotency for the start() method, i.e. don't let clients run start() twice?
  def start() {
    logger.debug(s"Initiating cluster startup")
    logger.debug(s"Starting a ZooKeeper instance on port $zookeeperPort ...")
    zookeeper = new ZooKeeperEmbedded(zookeeperPort)
    logger.debug(s"ZooKeeper instance is running at ${zookeeper.connectString}")

    logger.info(s"Starting a Kafka instance on port $kafkaPort ...")
    kafka = {
      val config = {
        val p = new Properties
        p.put("zookeeper.connect", zookeeper.connectString)
        p.put("port", kafkaPort.toString)
        p.putAll(brokerConfig)
        p
      }
      new KafkaEmbedded(config)
    }
    kafka.start()
    logger.debug(s"Kafka instance is running at ${kafka.brokerList}, connected to ZooKeeper at ${kafka.zookeeperConnect}")
    logger.debug("Creating topics")
    createTopics()
    logger.debug("Cluster startup completed")
  }

  private def createTopics() {
    if (topics.nonEmpty) {
      for (topic <- topics) {
        kafka.createTopic(topic.name, topic.partitions, topic.replication, topic.config)
      }
    }
  }

  def stop() {
    logger.debug(s"Initiating cluster shutdown")
    logger.debug(s"Stopping the Kafka instance running at ${kafka.brokerList}")
    kafka.stop()
    logger.debug(s"Stopping the ZooKeeper instance running at port ${zookeeper.connectString}")
    zookeeper.stop()
    logger.debug(s"Cluster shutdown completed")
  }

}

case class KafkaTopic(name: String, partitions: Int = 1, replication: Int = 1, config: Properties = new Properties) {

  require(partitions > 0)
  require(replication > 0)

}