package com.flink.kafka

import com.flink.config.AppConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}

import java.util
import java.util.Properties

trait KafkaClient {

  private lazy val config = new AppConfig

  private def getClient(): AdminClient = {
    val props = new Properties()
    props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServer)
    AdminClient.create(props)
  }

  def createTopic(): Unit = {
    val client = getClient()
    val topic = new NewTopic(config.topicName, config.partitions, config.replicationFactor)
    val list = new util.ArrayList[NewTopic]()
    list.add(topic)
    client.createTopics(list)
    client.close()
  }
}
