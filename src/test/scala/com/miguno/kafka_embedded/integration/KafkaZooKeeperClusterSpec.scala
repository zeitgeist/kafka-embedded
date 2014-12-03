package com.miguno.kafka_embedded.integration

import java.util.Properties
import java.util.concurrent.Executors

import _root_.kafka.message.MessageAndMetadata
import com.miguno.kafka_embedded.kafka.{KafkaZooKeeperCluster, KafkaTopic}
import com.miguno.kafka_embedded.logging.LazyLogging
import kafka.consumer.{Consumer, ConsumerConfig}
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import kafka.serializer.DefaultDecoder
import org.scalatest._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.reflectiveCalls

@DoNotDiscover
class KafkaZooKeeperClusterSpec extends FunSpec with Matchers with GivenWhenThen with LazyLogging {

  describe("KafkaZooKeeperCluster") {

    it("should provide Kafka topics for reading and writing data", IntegrationTest) {
      Given("a ZooKeeper instance")
      And("a Kafka broker instance")
      And("a Kafka topic")
      val topicName = "testing"
      val cluster = new KafkaZooKeeperCluster(topics = Seq(KafkaTopic(topicName)))
      cluster.start()
      And("some words")
      val words = Seq("protoss", "terran", "zerg")
      And("a Kafka producer")
      val producer = {
        val config = {
          val c = new Properties
          c.load(this.getClass.getResourceAsStream("/producer-defaults.properties"))
          c.put("metadata.broker.list", cluster.kafka.brokerList)
          c.put("producer.type", "sync")
          c.put("client.id", "test-sync-producer")
          c.put("request.required.acks", "1")
          c
        }
        new Producer[Array[Byte], Array[Byte]](new ProducerConfig(config))
      }
      And("a Kafka consumer")
      // The Kafka consumer must be running before the first messages are being sent to the topic.
      val consumer = new ConsumerApp(cluster.kafka.zookeeperConnect, topicName)
      val waitForConsumerStartup = 300.millis
      logger.debug(s"Waiting $waitForConsumerStartup for consumer threads to launch")
      Thread.sleep(waitForConsumerStartup.toMillis)
      logger.debug("Finished waiting for consumer threads to launch")

      When(s"I send the words to the topic")
      words foreach {
        case word =>
          logger.debug(s"Synchronously sending word $word to topic $topicName")
          val msg = new KeyedMessage[Array[Byte], Array[Byte]](topicName, StringCodec.encode(word))
          producer.send(msg)
      }

      Then("the consumer receives the words")
      val waitForConsumerToReadProducerOutput = 300.millis
      logger.debug(s"Waiting $waitForConsumerToReadProducerOutput for consumer threads to read messages")
      Thread.sleep(waitForConsumerToReadProducerOutput.toMillis)
      logger.debug("Finished waiting for consumer threads to read messages")
      consumer.receivedWords should be(words)

      // Cleanup
      consumer.shutdown()
      cluster.stop()
    }

  }

}

object StringCodec {

  def encode(s: String): Array[Byte] = s.getBytes

  def decode(bytes: Array[Byte]): String = new String(bytes)

}

class ConsumerApp(val zookeeperConnect: String,
                  val topic: String,
                  val numStreams: Int = 1 /* A single thread ensures we see incoming messages in the correct order. */)
  extends LazyLogging {

  private val receivedMessages = new mutable.SynchronizedQueue[String]
  private val executor = Executors.newFixedThreadPool(numStreams)

  private val consumerConnector = {
    val config = {
      val c = new Properties
      c.load(this.getClass.getResourceAsStream("/consumer-defaults.properties"))
      c.put("zookeeper.connect", zookeeperConnect)
      c
    }
    Consumer.create(new ConsumerConfig(config))
  }

  private val consumerThreads = {
    val consumerMap = {
      val topicCountMap = Map(topic -> numStreams)
      val keyDecoder = new DefaultDecoder
      val valueDecoder = new DefaultDecoder
      consumerConnector.createMessageStreams(topicCountMap, keyDecoder, valueDecoder)
    }
    consumerMap.get(topic) match {
      case Some(streams) => streams.view.zipWithIndex map {
        case (stream, threadId) =>
          new Runnable {
            override def run() {
              stream foreach {
                case msg: MessageAndMetadata[_, _] =>
                  logger.debug(s"Consumer thread $threadId received message: " + msg)
                  val word = StringCodec.decode(msg.message())
                  receivedMessages += word
                case _ => logger.debug(s"Received unexpected message type from broker")
              }
            }
          }
      }
      case _ => Seq()
    }
  }
  consumerThreads foreach executor.submit

  def receivedWords: Seq[String] = receivedMessages.toSeq

  def shutdown() {
    consumerConnector.shutdown()
    executor.shutdown()
  }

}