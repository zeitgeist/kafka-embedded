package com.miguno.kafka_embedded.integration

import org.scalatest.Stepwise

class IntegrationSuite extends Stepwise(
  new KafkaZooKeeperClusterSpec
)