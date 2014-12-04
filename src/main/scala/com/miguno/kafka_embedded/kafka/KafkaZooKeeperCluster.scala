package com.miguno.kafka_embedded.kafka

import java.util.Properties

import com.miguno.kafka_embedded.logging.LazyLogging
import com.miguno.kafka_embedded.zookeeper.ZooKeeperEmbedded
import kafka.message.MessageAndMetadata
import org.apache.curator.test.InstanceSpec

import scala.concurrent.duration._
import scala.util.{Success, Try}

/**
 * Starts embedded instances of a Kafka broker and a ZooKeeper server.  Used only for testing.
 *
 * @param zookeeperPort ZooKeeper port
 * @param kafkaPort: Kafka port
 * @param topics Kafka topics to be created
 * @param brokerConfig Kafka broker configuration
 */
class KafkaZooKeeperCluster(zookeeperPort: Integer = InstanceSpec.getRandomPort,
                            kafkaPort: Integer = InstanceSpec.getRandomPort,
                            topics: Seq[KafkaTopic] = Seq(),
                            brokerConfig: Properties = new Properties) extends LazyLogging {

  var zookeeper: ZooKeeperEmbedded = _
  var kafka: KafkaEmbedded = _


  val consumerApps = collection.mutable.Buffer[KafkaConsumerApp[_]]()
  val producerApps = collection.mutable.Buffer[KafkaProducerApp]()

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

  /**
   * For the moment we only allow the creation of new producers if the backing Kafka broker is up and running.
   *
   * @param topic Kafka topic to write to.
   * @param config Additional producer configuration settings.
   * @return
   */
  def createProducer(topic: String, config: Properties): Try[KafkaProducerApp] = {
    require(kafka != null, "Kafka broker must be running")
    val producer = new KafkaProducerApp(kafka.brokerList, config, Option(topic))
    producerApps += producer
    logger.debug(s"Producer created for topic $topic.  Total consumers running: ${producerApps.size}.")
    Success(producer)
  }

  /**
   * For the moment we only allow the creation of new consumers if the backing ZooKeeper instance and the Kafka broker
   * are up and running.  Also, each consumer (group) is currently using a single thread only to read from Kafka.
   *
   * @param topic Kafka topic to read from.
   * @param consume The function that is run on every incoming message.
   * @param startupWaitTime Sleep time to allow the consumer to start up properly.
   * @tparam T Allows for passing along context/state information.
   * @return
   */
  def createAndStartConsumer[T](topic: String,
                                consume: (MessageAndMetadata[Array[Byte], Array[Byte]], ConsumerTaskContext) => Unit,
                                startupWaitTime: FiniteDuration = 300.millis): Try[KafkaConsumerApp[T]] = {
    require(zookeeper != null, "ZooKeeper must be running")
    require(kafka != null, "Kafka broker must be running")
    val consumerApp = {
      val numStreams = 1
      val config = {
        val c = new Properties
        c.put("group.id", "kafka-storm-starter-test-consumer")
        c
      }
      new KafkaConsumerApp[T](topic, zookeeper.connectString, numStreams, config)
    }
    consumerApp.startConsumers(f = (m: MessageAndMetadata[Array[Byte], Array[Byte]], c: ConsumerTaskContext, n: Option[T]) => consume(m, c))
    Thread.sleep(startupWaitTime.toMillis)

    consumerApps += consumerApp
    logger.debug(s"Consumer created for topic $topic.  Total consumers running: ${consumerApps.size}.")
    Success(consumerApp)
  }

  def stop() {
    logger.debug(s"Initiating cluster shutdown")

    logger.debug(s"Shutting down ${consumerApps.length} consumer apps")
    for (app <- consumerApps) {
      Try(app.shutdown())
    }

    logger.debug(s"Shutting down ${producerApps.length} producer apps")
    for (producer <- producerApps) {
      Try(producer.shutdown())
    }

    logger.debug(s"Stopping the Kafka instance running at ${kafka.brokerList}")
    kafka.stop()
    kafka = null
    logger.debug(s"Stopping the ZooKeeper instance running at port ${zookeeper.connectString}")
    zookeeper.stop()
    zookeeper = null
    logger.debug(s"Cluster shutdown completed")
  }

}

case class KafkaTopic(name: String, partitions: Int = 1, replication: Int = 1, config: Properties = new Properties) {

  require(partitions > 0)
  require(replication > 0)

}