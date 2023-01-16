package com.flink.config

import com.typesafe.config.{Config, ConfigFactory}

trait BaseConfig extends Serializable {
  val configFactory: Config = ConfigFactory.load()
}

trait KafkaConfig extends BaseConfig with Serializable {
  private lazy val kafkaConfig: Config = configFactory.getConfig("kafka")
  lazy val bootstrapServer: String = kafkaConfig.getString("bootstrap.server")
}

trait TopicConfig extends BaseConfig with Serializable {
  private lazy val topicConfig: Config = configFactory.getConfig("topic")
  lazy val topicName: String = topicConfig.getString("name")
  lazy val groupId: String = topicConfig.getString("group.id")
  lazy val partitions: Int = topicConfig.getInt("partitions")
  lazy val replicationFactor: Short = topicConfig.getInt("replication.factor").toShort
}

trait TimescaleDbConfig extends BaseConfig with Serializable {
  private lazy val postgresConfig: Config = configFactory.getConfig("timescaledb")
  lazy val pgUser: String = postgresConfig.getString("user")
  lazy val pgPass: String = postgresConfig.getString("pass")
  lazy val pgUrl: String = postgresConfig.getString("url")
}

trait JobConfig extends BaseConfig with Serializable {
  private lazy val jobConfig: Config = configFactory.getConfig("job")
  lazy val maxParallelism: Int = jobConfig.getInt("max.parallelism")
  lazy val numStreamThreads: Int = jobConfig.getInt("num.stream.threads")
}

class AppConfig extends TimescaleDbConfig with KafkaConfig with TopicConfig with JobConfig with Serializable