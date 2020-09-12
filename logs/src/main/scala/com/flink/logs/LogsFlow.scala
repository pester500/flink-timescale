package com.flink.logs

import java.util.Properties

import com.flink.timescale.config.Constants
import com.flink.timescale.config.AppConfig
import grizzled.slf4j.Logging
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.flywaydb.core.Flyway

class LogsFlow extends Constants with Logging {

  private lazy val config = new AppConfig

  def execute(): Unit = {
    // Create schema for JDBC output
    logger.info("Starting the flyway migration")
    lazy val flyway = Flyway.configure.dataSource(config.url, config.user, config.pass).load()
    flyway.migrate()
    logger.info("Finished the flyway migration")

    // Create Kafka consumer with properties
    val properties = new Properties()
    properties.setProperty(BOOTSTRAP_SERVERS, config.bootstrapServer)
    properties.setProperty(GROUP_ID, config.groupId)
    properties.setProperty(AUTO_OFFSET_RESET, EARLIEST)
    lazy val kafkaConsumer = new FlinkKafkaConsumer[String](config.source, KafkaStringSchema, properties)
  }

}
