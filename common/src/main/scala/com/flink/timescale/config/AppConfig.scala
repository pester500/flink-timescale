package com.flink.timescale.config

import com.typesafe.config.{Config, ConfigFactory}

trait BaseConfig extends Serializable {
  val configFactory: Config = ConfigFactory.load()
}

trait KafkaConfig extends BaseConfig with Serializable {
  private lazy val kafkaConfig: Config = configFactory.getConfig("kafka")
  lazy val bootstrapServer: String = kafkaConfig.getString("bootstrap.server")
  lazy val groupId: String = kafkaConfig.getString("group.id")
  lazy val source: String = kafkaConfig.getString("source")
}

trait TimescaleDbConfig extends BaseConfig with Serializable {
  private lazy val postgresConfig: Config = configFactory.getConfig("timescaledb")
  lazy val user: String = postgresConfig.getString("user")
  lazy val pass: String = postgresConfig.getString("pass")
  lazy val url: String = postgresConfig.getString("url")
}

trait JobCconfig extends BaseConfig with Serializable {
  private lazy val jobConfig: Config = configFactory.getConfig("job")
  lazy val maxParallelism: Int = jobConfig.getInt("max.parallelism")
  lazy val numStreamThreads: Int = jobConfig.getInt("num.stream.threads")
}

class AppConfig extends TimescaleDbConfig with KafkaConfig with JobCconfig with Serializable