package com.flink.config

import com.typesafe.config.{Config, ConfigFactory}

trait BaseConfig extends Serializable {
  val configFactory: Config = ConfigFactory.load()
}

trait KafkaConfig extends BaseConfig with Serializable {
  private lazy val kafkaConfig: Config = configFactory.getConfig("kafka")
  lazy val bootstrapServer: String = kafkaConfig.getString("bootstrap.server")
  lazy val groupId: String = kafkaConfig.getString("group.id")
  lazy val crimesSource: String = kafkaConfig.getString("crimes.source")
  lazy val logsSource: String = kafkaConfig.getString("logs.source")
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

class AppConfig extends TimescaleDbConfig with KafkaConfig with JobConfig with Serializable